package facchini.riccardo.reservation.Customer_Package.Fragment_Customer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.MenuItem;
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

import facchini.riccardo.reservation.Customer_Package.Adapter_Customer.Adapter_Customer_ReservationCard;
import facchini.riccardo.reservation.Customer_Package.Customer;
import facchini.riccardo.reservation.OnItemClickListener;
import facchini.riccardo.reservation.R;
import facchini.riccardo.reservation.ReservationViewModel;
import facchini.riccardo.reservation.Reservation_Package.Reservation;
import facchini.riccardo.reservation.SharedViewModel;
import facchini.riccardo.reservation.Shop_Package.Shop;

public class Fragment_Customer_Home extends Fragment implements OnItemClickListener
{
    //Firestore
    private FirebaseFirestore db;
    private CollectionReference customersCollection, shopsCollection, reservationsCollection;
    
    private Calendar now;
    private String customerUid;
    private SharedViewModel sharedViewModel;
    private ReservationViewModel viewModel;
    private List<Reservation> resList;
    private SharedPreferences pref;
    
    private RecyclerView recyclerView;
    private Adapter_Customer_ReservationCard adapterCustomerHome;
    
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
    public void onResume()
    {
        super.onResume();
        
        try
        {
            if (pref.getBoolean(getString(R.string.need_update_key), false))
            {
                pref.edit().putBoolean(getString(R.string.need_update_key), false).commit();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new Fragment_Customer_Home()).commit();
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
        customersCollection = db.collection("customers");
        shopsCollection = db.collection("shops");
        reservationsCollection = db.collection("reservations");
        
        now = Calendar.getInstance();
        
        recyclerView = view.findViewById(R.id.futureReservations);
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
        sharedViewModel = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        viewModel = ViewModelProviders.of(getActivity()).get(ReservationViewModel.class);
        
        
        customersCollection.document(customerUid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
        {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot)
            {
                if (documentSnapshot.exists())
                    sharedViewModel.setCurrentCustomer(new Customer(documentSnapshot.getData()));
            }
        });
        
        progressBar.setVisibility(View.VISIBLE);
        
        final CountDownTimer timer = new CountDownTimer(5000, 5000)
        {
            @Override
            public void onTick(long millisUntilFinished)
            { }
            
            @Override
            public void onFinish()
            {
                if (resList.isEmpty())
                    showReservations(null);
            }
        };
        
        viewModel.getNextReservations().observe(this, new Observer<List<Reservation>>()
        {
            @Override
            public void onChanged(List<Reservation> reservations)
            {
                if (timer != null)
                {
                    timer.cancel();
                }
                showReservations(reservations);
            }
        });
        viewModel.updateViewModel();
    }
    
    
    private void showReservations(List<Reservation> reservations)
    {
        if (reservations == null || reservations.isEmpty())
        {
            recyclerView.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            noReservationsText.setVisibility(View.VISIBLE);
        } else
        {
            adapterCustomerHome = new Adapter_Customer_ReservationCard(getContext(), reservations);
            recyclerView.setAdapter(adapterCustomerHome);
            adapterCustomerHome.setOnItemClickListener(this);
            
            progressBar.setVisibility(View.GONE);
        }
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.refresh_menu:
                viewModel.updateViewModel();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
                    if (documentSnapshot.exists())
                        resList.add(
                                new Reservation(doc.getId(), ((Timestamp) doc.get("time")).toDate(),
                                        documentSnapshot.toObject(Shop.class)));
                    
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
        Collections.sort(resList, reservationComparator);
        
        adapterCustomerHome = new Adapter_Customer_ReservationCard(getContext(), resList);
        recyclerView.setAdapter(adapterCustomerHome);
        adapterCustomerHome.setOnItemClickListener(this);
        
        progressBar.setVisibility(View.GONE);
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
    
    @Override
    public void onItemClick(final int position)
    {
        Reservation res = resList.get(position);
        new AlertDialog.Builder(getContext()).setCancelable(true)
                .setTitle(getString(R.string.areYouSure))
                .setMessage(getString(R.string.deleteReservationFor).concat(res.getOtherUser().getName()).concat(getString(R.string.onWithTabs)).concat(res.getDateFormatted()))
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        reservationsCollection.document(resList.get(position).getResUid()).delete();
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragmentContainer, new Fragment_Customer_Home()).commit();
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
