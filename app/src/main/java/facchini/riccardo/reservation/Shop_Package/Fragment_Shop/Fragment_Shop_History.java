package facchini.riccardo.reservation.Shop_Package.Fragment_Shop;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import facchini.riccardo.reservation.Customer_Package.Customer;
import facchini.riccardo.reservation.R;
import facchini.riccardo.reservation.Reservation_Package.Reservation_Shop_Home;
import facchini.riccardo.reservation.Shop_Package.Adapter_Shop.Adapter_Shop_Home;

public class Fragment_Shop_History extends Fragment
{
    //Firestore
    private FirebaseFirestore db;
    private CollectionReference reservationsCollection;
    
    private Calendar now;
    private String shopUid;
    private List<Reservation_Shop_Home> resList;
    
    private RecyclerView recyclerView;
    private Adapter_Shop_Home adapterShopHistory;
    
    private TextView noReservationsText;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        getActivity().setTitle(R.string.history);
        return inflater.inflate(R.layout.fragment_shop_history, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        db = FirebaseFirestore.getInstance();
        SharedPreferences pref = getContext().getSharedPreferences(getString(R.string.reservations_preferences), Context.MODE_PRIVATE);
        shopUid = pref.getString(getString(R.string.current_user_uid_key), "");
        reservationsCollection = db.collection("reservations");
        
        now = Calendar.getInstance();
        
        recyclerView = view.findViewById(R.id.pastReservations);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setVisibility(View.VISIBLE);
        
        resList = new ArrayList<>();
        
        noReservationsText = view.findViewById(R.id.noReservations);
        noReservationsText.setVisibility(View.GONE);
        
        resList = new ArrayList<>();
    }
    
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        
        reservationsCollection.whereEqualTo("shopUid", shopUid).whereLessThan("time", now.getTime())
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
        {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots)
            {
                extractPastReservations(queryDocumentSnapshots);
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                e.printStackTrace();
            }
        });
    }
    
    /**
     * Extracts and checks the past reservations of the user
     *
     * @param snap
     */
    private void extractPastReservations(final QuerySnapshot snap)
    {
        if (snap.isEmpty())
        {
            recyclerView.setVisibility(View.GONE);
            noReservationsText.setVisibility(View.VISIBLE);
            return;
        }
        
        
        for (final QueryDocumentSnapshot doc : snap)
        {
            String name = doc.get("customerName").toString().substring(0, doc.get("customerName").toString().indexOf(' '));
            String surname = doc.get("customerName").toString().substring(doc.get("customerName").toString().indexOf(' ') + 1);
            Customer c = new Customer(doc.get("customerUid").toString(), name, surname);
            resList.add(new Reservation_Shop_Home(((Timestamp) doc.get("time")).toDate(), c));
            
            if (resList.size() == snap.size())
                orderList();

//            customersCollection.document((String) doc.get("customerUid")).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
//            {
//                @Override
//                public void onSuccess(DocumentSnapshot documentSnapshot)
//                {
//                    if (documentSnapshot.exists())
//                        resList.add(new Reservation_Shop_Home(((Timestamp) doc.get("time")).toDate(),
//                                documentSnapshot.toObject(Customer.class)));
//
//                    if (resList.size() == snap.size())
//                        orderList();
//                }
//            });
        }
    }
    
    /**
     * Orders list to be displayed and adds elements to the adapter
     */
    private void orderList()
    {
        Collections.sort(resList, Collections.reverseOrder(reservationComparator));
        adapterShopHistory = new Adapter_Shop_Home(getContext(), resList);
        recyclerView.setAdapter(adapterShopHistory);
    }
    
    /**
     * Defined comparator for reservations to order them
     */
    public Comparator<Reservation_Shop_Home> reservationComparator = new Comparator<Reservation_Shop_Home>()
    {
        @Override
        public int compare(Reservation_Shop_Home o1, Reservation_Shop_Home o2)
        {
            return o1.getDate().compareTo(o2.getDate());
        }
    };
}
