package facchini.riccardo.reservation.Shop_Package.Fragment_Shop;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
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
import facchini.riccardo.reservation.Reservation_Package.Reservation;
import facchini.riccardo.reservation.SharedViewModel;
import facchini.riccardo.reservation.Shop_Package.Adapter_Shop.Adapter_Shop_Home;
import facchini.riccardo.reservation.Shop_Package.Shop;
import facchini.riccardo.reservation.User;

public class Fragment_Shop_Home extends Fragment
{
    //Firestore
    private FirebaseFirestore db;
    private CollectionReference customersCollection, shopsCollection, reservationsCollection;
    
    private Calendar now;
    private String shopUid;
    private SharedViewModel viewModel;
    private List<Reservation> resList;
    
    private RecyclerView recyclerView;
    private Adapter_Shop_Home adapterShopHome;
    
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
        shopUid = FirebaseAuth.getInstance().getUid();
        customersCollection = db.collection("customers");
        shopsCollection = db.collection("shops");
        reservationsCollection = db.collection("reservations");
        
        now = Calendar.getInstance();
        
        recyclerView = view.findViewById(R.id.futureReservations);
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
        viewModel = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        
        
        shopsCollection.document(shopUid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
        {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot)
            {
                if (documentSnapshot.exists())
                    viewModel.setCurrentShop(new Shop(documentSnapshot.getData()));
            }
        });
        
        reservationsCollection.whereEqualTo("shopUid", shopUid).whereGreaterThan("time", now.getTime())
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
        {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots)
            {
                extractNextReservations(queryDocumentSnapshots);
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
     * Extracts and checks the future reservations of the user
     *
     * @param snap
     */
    private void extractNextReservations(final QuerySnapshot snap)
    {
        if (snap.isEmpty())
        {
            recyclerView.setVisibility(View.GONE);
            noReservationsText.setVisibility(View.VISIBLE);
            return;
        }
        
        
        for (final QueryDocumentSnapshot doc : snap)
        {
            Customer c = new Customer((User) doc.get("customerUid"));
            resList.add(new Reservation(doc.getId(), ((Timestamp) doc.get("time")).toDate(), c));
            
            if (resList.size() == snap.size())
                orderList();
        }
    }
    
    /**
     * Orders list to be displayed and adds elements to the adapter
     */
    private void orderList()
    {
        Collections.sort(resList, reservationComparator);
        adapterShopHome = new Adapter_Shop_Home(getContext(), resList);
        recyclerView.setAdapter(adapterShopHome);
    }
    
    /**
     * Defined comparator for reservations to order them
     */
    public Comparator<Reservation> reservationComparator = new Comparator<Reservation>()
    {
        @Override
        public int compare(Reservation o1, Reservation o2)
        {
            return o1.getDate().compareTo(o2.getDate());
        }
    };
}
