package facchini.riccardo.reservation.Shop_Package.Fragment_Shop;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import facchini.riccardo.reservation.Customer_Package.Customer;
import facchini.riccardo.reservation.R;
import facchini.riccardo.reservation.Reservation_Package.ReservationFirestore;
import facchini.riccardo.reservation.Shop_Package.Adapter_Shop.Adapter_Shop_Home;
import facchini.riccardo.reservation.User;

public class Fragment_Shop_History extends Fragment
{
    //Firestore
    private FirebaseFirestore db;
    private CollectionReference reservationsCollection;
    
    private Calendar now;
    private String shopUid;
    private List<ReservationFirestore> resList;
    
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
        shopUid = FirebaseAuth.getInstance().getUid();
        reservationsCollection = db.collection("reservations");
        
        now = Calendar.getInstance();
        
        recyclerView = view.findViewById(R.id.pastReservations);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setVisibility(View.VISIBLE);
        
        resList = new ArrayList<>();
        adapterShopHistory = new Adapter_Shop_Home(getContext(), resList);
        recyclerView.setAdapter(adapterShopHistory);
        
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
            resList.add(new ReservationFirestore(doc.getString("customerUid"), doc.getString("customerPic"),
                    doc.getString("customerName"), doc.getLong("time")));
            
            if (resList.size() == snap.size())
                orderList();
        }
    }
    
    /**
     * Orders list to be displayed and adds elements to the adapter
     */
    private void orderList()
    {
        //Collections.sort(resList, Collections.reverseOrder(reservationComparator));
        adapterShopHistory = new Adapter_Shop_Home(getContext(), resList);
        recyclerView.setAdapter(adapterShopHistory);
    }
    
    /**
     * Defined comparator for reservations to order them
     */
//    public Comparator<ReservationFirestore> reservationComparator = new Comparator<ReservationFirestore>()
//    {
//        @Override
//        public int compare(ReservationFirestore o1, ReservationFirestore o2)
//        {
//            return o1.getDate().compareTo(o2.getDate());
//        }
//    };
}
