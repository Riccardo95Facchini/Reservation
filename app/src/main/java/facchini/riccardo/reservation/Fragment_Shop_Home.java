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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class Fragment_Shop_Home extends Fragment
{
    //Firestore
    private FirebaseFirestore db;
    private CollectionReference customersCollection, shopsCollection, reservationsCollection;
    
    private Calendar now;
    private String shopUid;
    private SharedViewModel viewModel;
    private List<Reservation_Shop_Home> reservationShopHomeList;
    private ArrayAdapter<String> adapter;
    
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
        customersCollection = db.collection("customers");
        shopsCollection = db.collection("shops");
        reservationsCollection = db.collection("reservations");
        
        now = Calendar.getInstance();
        
        futureReservations = view.findViewById(R.id.futureReservations);
        futureReservations.setVisibility(View.VISIBLE);
        
        noReservationsText = view.findViewById(R.id.noReservations);
        noReservationsText.setVisibility(View.GONE);
        
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
        futureReservations.setAdapter(adapter);
        reservationShopHomeList = new ArrayList<>();
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
            futureReservations.setVisibility(View.GONE);
            noReservationsText.setVisibility(View.VISIBLE);
            return;
        }
        
        
        for (final QueryDocumentSnapshot doc : snap)
        {
            customersCollection.document((String) doc.get("customerUid")).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
            {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot)
                {
                    if (documentSnapshot.exists())
                        reservationShopHomeList.add(
                                new Reservation_Shop_Home(
                                        (Date) doc.get("time"),
                                        documentSnapshot.toObject(Customer.class)));
                    
                    if (reservationShopHomeList.size() == snap.size())
                        orderList();
                }
            });
        }
    }
    
    /**
     * Orders list to be displayed and adds elements to the adapter
     */
    private void orderList()
    {
        Collections.sort(reservationShopHomeList, reservationComparator);
        
        //for (Reservation_Shop_Home r : reservationShopHomeList)
            //adapter.add(r.getInfo());
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
