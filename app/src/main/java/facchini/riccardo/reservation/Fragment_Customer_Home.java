package facchini.riccardo.reservation;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

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

public class Fragment_Customer_Home extends Fragment
{
    //Firestore
    private FirebaseFirestore db;
    private CollectionReference customersCollection, shopsCollection, reservationsCollection;
    
    private Calendar now;
    private String customerUid;
    private SharedViewModel viewModel;
    private List<Reservation_Customer_Home> resList;
    
    private RecyclerView recyclerView;
    private Adapter_Customer_Home adapterCustomerHome;
    
    private TextView noReservationsText;
    private ProgressBar progressBar;
    
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
        customerUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        customersCollection = db.collection("customers");
        shopsCollection = db.collection("shops");
        reservationsCollection = db.collection("reservations");
        
        now = Calendar.getInstance();
        
        recyclerView = view.findViewById(R.id.futureReservations);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        resList = new ArrayList<>();
        
        noReservationsText = view.findViewById(R.id.noReservations);
        noReservationsText.setVisibility(View.GONE);
        
        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        
        resList = new ArrayList<>();
    }
    
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        
        
        customersCollection.document(customerUid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
        {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot)
            {
                if (documentSnapshot.exists())
                    viewModel.setCurrentCustomer(new Customer(documentSnapshot.getData()));
            }
        });
        
        reservationsCollection.whereEqualTo("customerUid", customerUid).whereGreaterThan("time", now.getTime())
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
        progressBar.setVisibility(View.VISIBLE);
        if (snap.isEmpty())
        {
            recyclerView.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            noReservationsText.setVisibility(View.VISIBLE);
            return;
        }
        
        for (final QueryDocumentSnapshot doc : snap)
        {
            shopsCollection.document((String) doc.get("shopUid")).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
            {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot)
                {
                    try
                    {
                        if (documentSnapshot.exists())
                            resList.add(
                                    new Reservation_Customer_Home(
                                            ((Timestamp) doc.get("time")).toDate(),
                                            documentSnapshot.toObject(Shop.class)));
                        
                        if (resList.size() == snap.size())
                            orderList();
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
    
    /**
     * Orders list to be displayed and adds elements to the adapter
     */
    private void orderList()
    {
        Collections.sort(resList, reservationComparator);
        
        adapterCustomerHome = new Adapter_Customer_Home(getContext(), resList);
        recyclerView.setAdapter(adapterCustomerHome);
        
        progressBar.setVisibility(View.GONE);
    }
    
    /**
     * Defined comparator for reservations to order them
     */
    public Comparator<Reservation_Customer_Home> reservationComparator = new Comparator<Reservation_Customer_Home>()
    {
        @Override
        public int compare(Reservation_Customer_Home o1, Reservation_Customer_Home o2)
        {
            return o1.getDate().compareTo(o2.getDate());
        }
    };
}
