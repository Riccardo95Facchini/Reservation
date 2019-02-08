package facchini.riccardo.reservation;

import android.arch.lifecycle.ViewModelProviders;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class Fragment_Shop_Home extends Fragment
{
    //Firestore
    private FirebaseFirestore db;
    private CollectionReference reservationsCollection, /*customersCollection,*/
            shopsCollection;
    
    private String shopUid;
    private SharedViewModel viewModel;
    private List<ReservationCustomer> reservationCustomerList;
    private ArrayAdapter<String> adapter;
    private final ReservationCustomer dummy = new ReservationCustomer(null, null);
    
    private ListView futureReservations;
    private TextView noReservationsText;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        getActivity().setTitle(R.string.reservations);
        return inflater.inflate(R.layout.fragment_shop_home, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        db = FirebaseFirestore.getInstance();
        shopUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        reservationsCollection = db.collection("reservations");
        //customersCollection = db.collection("customers");
        shopsCollection = db.collection("shops");
        
        futureReservations = view.findViewById(R.id.futureReservations);
        futureReservations.setVisibility(View.VISIBLE);
        
        noReservationsText = view.findViewById(R.id.noReservations);
        noReservationsText.setVisibility(View.GONE);
        
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
        futureReservations.setAdapter(adapter);
        reservationCustomerList = new ArrayList<>();
    }
    
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        
        Calendar c = Calendar.getInstance();
        
        reservationsCollection.whereEqualTo("shop", shopUid)/*.whereGreaterThan("time", c.getTime())*/
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
        {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots)
            {
                extractNextReservations(queryDocumentSnapshots);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                e.printStackTrace();
            }
        });
        
//        reservationsCollection.document(shopUid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
//        {
//            @Override
//            public void onSuccess(DocumentSnapshot documentSnapshot)
//            {
//                if (documentSnapshot.exists())
//                {
//                    viewModel.setCurrentShop(new Shop((Shop) documentSnapshot.getData()));
//                    extractNextReservations(documentSnapshot);
//                }
//            }
//        });
    }
    
    
    /**
     * Extracts and checks the future reservations of the user
     *
     * @param snap
     */
    private void extractNextReservations(QuerySnapshot snap)
    {
        
        if (snap.isEmpty())
        {
            futureReservations.setVisibility(View.GONE);
            noReservationsText.setVisibility(View.VISIBLE);
            return;
        }
        
        for (QueryDocumentSnapshot doc : snap)
        {
            int i = 0;
        }
        
        Date now = Calendar.getInstance().getTime();
        Date res;
        boolean noReservations = true;
        
//        for (Map<String, Object> map : customerReservations)
//        {
//            res = (Date) map.get("date");
//
//            if (now.compareTo(res) < 0)
//            {
//                noReservations = false;
//                final Date finalRes = res;
//                shopsCollection.document((String) map.get("shop")).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
//                {
//                    @Override
//                    public void onSuccess(DocumentSnapshot documentSnapshot)
//                    {
//                        if (documentSnapshot.exists())
//                        {
//                            reservationCustomerList.add(new ReservationCustomer(documentSnapshot.toObject(Shop.class), finalRes));
//                            if (reservationCustomerList.size() == customerReservations.size())
//                                orderList();
//                        }
//                    }
//                });
//            } else
//                reservationCustomerList.add(dummy);
//
//            if (noReservations && reservationCustomerList.size() == customerReservations.size())
//            {
//                futureReservations.setVisibility(View.GONE);
//                noReservationsText.setVisibility(View.VISIBLE);
//            }
//        }
    }
    
    /**
     * Orders list to be displayed and adds elements to the adapter
     */
    private void orderList()
    {
        while (reservationCustomerList.remove(dummy))
        {
        }
        
        Collections.sort(reservationCustomerList, reservationComparator);
        
        for (ReservationCustomer r : reservationCustomerList)
            adapter.add(r.getInfo());
    }
    
    /**
     * Defined comparator for reservations to order them
     */
    public Comparator<ReservationCustomer> reservationComparator = new Comparator<ReservationCustomer>()
    {
        @Override
        public int compare(ReservationCustomer o1, ReservationCustomer o2)
        {
            return o1.getDate().compareTo(o2.getDate());
        }
    };
}
