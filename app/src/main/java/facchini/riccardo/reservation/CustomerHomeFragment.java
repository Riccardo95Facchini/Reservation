package facchini.riccardo.reservation;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import java.text.ParseException;
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
    private List<Reservation> reservationList;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private ArrayAdapter<String> adapter;
    
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
        
        customersCollection.document(userUid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
        {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot)
            {
                if (documentSnapshot.exists())
                {
                    extractNextReservations(((ArrayList<Map<String, String>>) documentSnapshot.get("customerReservations")));
                }
            }
        });
    }
    
    /**
     * Extracts and checks the future reservations of the user
     * @param customerReservations
     */
    private void extractNextReservations(ArrayList<Map<String, String>> customerReservations)
    {
        Date now = Calendar.getInstance().getTime();
        Date res = null;
        String time;
        int index = 0;
        boolean noReservations = true;
        for (Map<String, String> map : customerReservations)
        {
            try
            {
                res = sdf.parse(map.get("date"));
            } catch (ParseException pe)
            {
                
            }
            time = map.get("time");
            res.setHours(Integer.parseInt(time.substring(0, time.indexOf(':'))));
            res.setMinutes(Integer.parseInt(time.substring(time.indexOf(':') + 1)));
            
            if (now.compareTo(res) < 0)
            {
                noReservations = false;
                final Date finalRes = res;
                final int finalIndex = index;
                final int size = customerReservations.size() - 1;
                shopsCollection.document(map.get("shop")).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
                {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot)
                    {
                        if (documentSnapshot.exists())
                        {
                            reservationList.add(new Reservation(documentSnapshot.toObject(Shop.class), finalRes));
                            if (finalIndex == size)
                                orderList();
                        }
                    }
                });
            }
            
            index++;
            if (noReservations && index == customerReservations.size())
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
