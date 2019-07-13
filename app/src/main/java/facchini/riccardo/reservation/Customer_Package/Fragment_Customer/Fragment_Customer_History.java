package facchini.riccardo.reservation.Customer_Package.Fragment_Customer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.List;

import facchini.riccardo.reservation.CurrentUserViewModel;
import facchini.riccardo.reservation.Customer_Package.Activity_Customer.Activity_Customer_ShopInfo;
import facchini.riccardo.reservation.Customer_Package.Adapter_Customer.Adapter_Customer_ReservationCard;
import facchini.riccardo.reservation.OnItemClickListener;
import facchini.riccardo.reservation.R;
import facchini.riccardo.reservation.Reservation_Package.OnReservationListener;
import facchini.riccardo.reservation.Reservation_Package.ReservationFirestore;
import facchini.riccardo.reservation.Shop_Package.Shop;

public class Fragment_Customer_History extends Fragment implements OnItemClickListener, OnReservationListener
{
    //Firestore
    private FirebaseFirestore db;
    private CollectionReference reservationsCollection;
    
    private CurrentUserViewModel currentUserViewModel;
    
    private Calendar now;
    private String customerUid;
    private List<ReservationFirestore> reservations;
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
        customerUid = FirebaseAuth.getInstance().getUid();
        reservationsCollection = db.collection("reservations");
        
        now = Calendar.getInstance();
        
        recyclerView = view.findViewById(R.id.pastReservations);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        reservations = new ArrayList<>();
        adapterCustomerHistory = new Adapter_Customer_ReservationCard(getContext(), reservations);
        recyclerView.setAdapter(adapterCustomerHistory);
        
        noReservationsText = view.findViewById(R.id.noReservations);
        noReservationsText.setVisibility(View.GONE);
        
        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        
    }
    
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    
        currentUserViewModel = ViewModelProviders.of(getActivity()).get(CurrentUserViewModel.class);
        
        reservationsCollection.whereEqualTo("customerUid", customerUid).whereLessThan("time", now.getTime()).orderBy("time")
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
//            shopsCollection.document((String) doc.get("shopUid")).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
//            {
//                @Override
//                public void onSuccess(DocumentSnapshot documentSnapshot)
//                {
//                    if (documentSnapshot.exists())
//                        reservations.add(
//                                new ReservationFirestore(doc.getId(), (Long) doc.get("time"),
//                                        documentSnapshot.toObject(Shop.class)));
//
//                    if (reservations.size() == snap.size())
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
        //Collections.sort(reservations, Collections.reverseOrder(reservationComparator));
        
        adapterCustomerHistory = new Adapter_Customer_ReservationCard(getContext(), reservations);
        recyclerView.setAdapter(adapterCustomerHistory);
        adapterCustomerHistory.setOnItemClickListener(this, this);
        
        progressBar.setVisibility(View.GONE);
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
    @Override
    public void onItemClick(final int position)
    {
        final ReservationFirestore res = reservations.get(position);
        new AlertDialog.Builder(getContext()).setCancelable(true)
                .setTitle(getString(R.string.areYouSure))
                .setMessage(getString(R.string.deleteReservationFor).concat(res.getShopName()).concat(getString(R.string.onWithTabs)).concat(res.timeFormatted()))
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        reservationsCollection.document(res.getUid()).delete();
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
    
    @Override
    public void onInfoClick(int position)
    {
        currentUserViewModel.querySelectedShop(reservations.get(position).getShopUid());
        Observer observer = new Observer<Shop>()
        {
            @Override
            public void onChanged(Shop shop)
            {
                if (shop != null)
                {
                    Intent intent = new Intent(getContext(), Activity_Customer_ShopInfo.class);
                    Bundle b = new Bundle();
                    b.putParcelable("Selected", shop);
                    intent.putExtras(b);
                    intent.putExtra("name", currentUserViewModel.getCurrentUser().getValue().getName());
                    intent.putExtra("picUrl", currentUserViewModel.getCurrentUser().getValue().getProfilePicUrl());
                    getContext().startActivity(intent);
                    currentUserViewModel.getSelectedShop().removeObserver(this);
                    currentUserViewModel.getSelectedShop().setValue(null);
                }
            }
        };
        currentUserViewModel.getSelectedShop().observe(getActivity(), observer);
    }
}
