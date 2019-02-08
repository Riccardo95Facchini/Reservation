package facchini.riccardo.reservation;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class CustomerHomeFragment extends Fragment
{
    //Firestore
    private FirebaseFirestore db;
    private CollectionReference customersCollection, shopsCollection;
    
    private String userUid;
    private SharedViewModel viewModel;
    private List<Reservation> reservationList;
    private ArrayAdapter<String> adapter;
    private final Reservation dummy = new Reservation(null, null);
    
    private ListView futureReservations;
    private TextView noReservationsText;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        getActivity().setTitle(R.string.reservations);
        return inflater.inflate(R.layout.fragment_customer_home, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        db = FirebaseFirestore.getInstance();
        userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        customersCollection = db.collection("customers");
        shopsCollection = db.collection("shops");
        
        futureReservations = view.findViewById(R.id.futureReservations);
        futureReservations.setVisibility(View.VISIBLE);
        
        noReservationsText = view.findViewById(R.id.noReservations);
        noReservationsText.setVisibility(View.GONE);
        
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
        futureReservations.setAdapter(adapter);
        reservationList = new ArrayList<>();
    }
    
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        
        customersCollection.document(userUid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
        {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot)
            {
                if (documentSnapshot.exists())
                {
                    viewModel.setCurrentCustomer(new Customer(documentSnapshot.getData()));
                    extractNextReservations(((ArrayList<Map<String, Object>>) documentSnapshot.get("customerReservations")));
                }
            }
        });
    }
    
    
    /**
     * Extracts and checks the future reservations of the user
     *
     * @param customerReservations
     */
    private void extractNextReservations(final ArrayList<Map<String, Object>> customerReservations)
    {
        Date now = Calendar.getInstance().getTime();
        Date res;
        boolean noReservations = true;
        
        for (Map<String, Object> map : customerReservations)
        {
            res = (Date) map.get("date");
            
            if (now.compareTo(res) < 0)
            {
                noReservations = false;
                final Date finalRes = res;
                shopsCollection.document((String) map.get("shop")).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
                {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot)
                    {
                        if (documentSnapshot.exists())
                        {
                            reservationList.add(new Reservation(documentSnapshot.toObject(Shop.class), finalRes));
                            if (reservationList.size() == customerReservations.size())
                                orderList();
                        }
                    }
                });
            } else
                reservationList.add(dummy);
            
            if (noReservations && reservationList.size() == customerReservations.size())
            {
                futureReservations.setVisibility(View.GONE);
                noReservationsText.setVisibility(View.VISIBLE);
            }
        }
    }
    
    /**
     * Orders list to be displayed and adds elements to the adapter
     */
    private void orderList()
    {
        while (reservationList.remove(dummy))
        {
        }
        
        Collections.sort(reservationList, reservationComparator);
        
        for (Reservation r : reservationList)
            adapter.add(r.getInfo());
    }
    
    /**
     * Defined comparator for reservations to order them
     */
    public Comparator<Reservation> reservationComparator = new Comparator<Reservation>()
    {
        @Override
        public int compare(Reservation o1, Reservation o2)
        {
            return o1.getWhen().compareTo(o2.getWhen());
        }
    };
}
