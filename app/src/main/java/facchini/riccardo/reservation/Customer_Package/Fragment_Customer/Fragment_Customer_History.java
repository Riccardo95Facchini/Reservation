package facchini.riccardo.reservation.Customer_Package.Fragment_Customer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import facchini.riccardo.reservation.Customer_Package.Adapter_Customer.Adapter_Customer_ReservationCard;
import facchini.riccardo.reservation.OnItemClickListener;
import facchini.riccardo.reservation.R;
import facchini.riccardo.reservation.Reservation_Package.Reservation_Customer_Home;
import facchini.riccardo.reservation.Shop_Package.Shop;

public class Fragment_Customer_History extends Fragment implements OnItemClickListener
{
    //Firestore
    private FirebaseFirestore db;
    private CollectionReference customersCollection, shopsCollection, reservationsCollection;
    
    private Calendar now;
    private String customerUid;
    private List<Reservation_Customer_Home> resList;
    private SharedPreferences pref;
    
    private RecyclerView recyclerView;
    private Adapter_Customer_ReservationCard adapterCustomerHistory;
    
    private TextView noReservationsText;
    private ProgressBar progressBar;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        getActivity().setTitle(R.string.past_reservations);
        return inflater.inflate(R.layout.fragment_customer_history, container, false);
    }
    
    @Override
    public void onResume()
    {
        super.onResume();
        
        try
        {
            if (pref.getBoolean(getString(R.string.need_update_key), false))
            {
                pref.edit().putBoolean(getString(R.string.need_update_key), false).commit();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new Fragment_Customer_History()).commit();
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        db = FirebaseFirestore.getInstance();
        
        pref = getContext().getSharedPreferences(getString(R.string.reservations_preferences), Context.MODE_PRIVATE);
        customerUid = pref.getString(getString(R.string.current_user_uid_key), "");
        customersCollection = db.collection("customers");
        shopsCollection = db.collection("shops");
        reservationsCollection = db.collection("reservations");
        
        now = Calendar.getInstance();
        
        recyclerView = view.findViewById(R.id.pastReservations);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
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
        
        reservationsCollection.whereEqualTo("customerUid", customerUid).whereLessThan("time", now.getTime())
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
     * @param snap QuerySnapshot returned by the server
     */
    private void extractPastReservations(final QuerySnapshot snap)
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
            //TODO: make it more efficient, no need to load each one in its entirety (change reservation structure to include essentials)
            shopsCollection.document((String) doc.get("shopUid")).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
            {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot)
                {
                    if (documentSnapshot.exists())
                        resList.add(
                                new Reservation_Customer_Home(((Timestamp) doc.get("time")).toDate(),
                                        documentSnapshot.toObject(Shop.class), doc.getId()));
                    
                    if (resList.size() == snap.size())
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
        Collections.sort(resList, Collections.reverseOrder(reservationComparator));
        
        adapterCustomerHistory = new Adapter_Customer_ReservationCard(getContext(), resList);
        recyclerView.setAdapter(adapterCustomerHistory);
        adapterCustomerHistory.setOnItemClickListener(this);
        
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
    
    @Override
    public void onItemClick(final int position)
    {
        Reservation_Customer_Home res = resList.get(position);
        new AlertDialog.Builder(getContext()).setCancelable(true)
                .setTitle(getString(R.string.areYouSure))
                .setMessage(getString(R.string.deleteReservationFor).concat(res.getShop().getName()).concat(getString(R.string.onWithTabs)).concat(res.getDateFormatted()))
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        reservationsCollection.document(resList.get(position).getResUid()).delete();
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragmentContainer, new Fragment_Customer_History()).commit();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                //Do nothing
            }
        }).show();
    }
}
