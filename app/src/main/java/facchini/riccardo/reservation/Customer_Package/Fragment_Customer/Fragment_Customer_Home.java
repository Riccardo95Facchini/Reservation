package facchini.riccardo.reservation.Customer_Package.Fragment_Customer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import facchini.riccardo.reservation.Customer_Package.Adapter_Customer.Adapter_Customer_ReservationCard;
import facchini.riccardo.reservation.Customer_Package.Customer;
import facchini.riccardo.reservation.OnItemClickListener;
import facchini.riccardo.reservation.R;
import facchini.riccardo.reservation.ReservationViewModel;
import facchini.riccardo.reservation.Reservation_Package.Reservation;
import facchini.riccardo.reservation.SharedViewModel;

public class Fragment_Customer_Home extends Fragment implements OnItemClickListener
{
    private FirebaseFirestore db;
    private CollectionReference customersCollection;
    
    private String customerUid;
    private SharedViewModel sharedViewModel;
    private ReservationViewModel viewModel;
    private SharedPreferences pref;
    private List<Reservation> reservations;
    
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
                viewModel.updateViewModel();
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        recyclerView = view.findViewById(R.id.futureReservations);
        noReservationsText = view.findViewById(R.id.noReservations);
        progressBar = view.findViewById(R.id.progressBar);
    }
    
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        
        viewModel = ViewModelProviders.of(getActivity()).get(ReservationViewModel.class);
        
        db = FirebaseFirestore.getInstance();
        
        pref = getContext().getSharedPreferences(getString(R.string.reservations_preferences), Context.MODE_PRIVATE);
        customerUid = FirebaseAuth.getInstance().getUid();
        customersCollection = db.collection("customers");
        
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        reservations = new ArrayList<>();
        adapterCustomerHome = new Adapter_Customer_ReservationCard(getContext(), reservations);
        
        noReservationsText.setVisibility(View.GONE);
        
        sharedViewModel = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        
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
        
        final CountDownTimer timer = new CountDownTimer(5000, 500)
        {
            @Override
            public void onTick(long millisUntilFinished)
            { }
            
            @Override
            public void onFinish()
            {
                if (viewModel.getNextReservations(ReservationViewModel.CUSTOMER).getValue() == null || viewModel.getNextReservations(ReservationViewModel.CUSTOMER).getValue().isEmpty())
                    showReservations(null);
            }
        }.start();
        
        viewModel.getNextReservations(ReservationViewModel.CUSTOMER).observe(getActivity(), new Observer<List<Reservation>>()
        {
            @Override
            public void onChanged(List<Reservation> reservations)
            {
                if (timer != null)
                    timer.cancel();
                showReservations(reservations);
            }
        });
        
        viewModel.getIsNextEmpty().observe(getActivity(), new Observer<Boolean>()
        {
            @Override
            public void onChanged(Boolean isEmpty)
            {
                if (isEmpty)
                {
                    if (timer != null)
                        timer.cancel();
                    showReservations(null);
                }
            }
        });
    }
    
    
    private void showReservations(List<Reservation> res)
    {
        reservations.clear();
        if (res == null || res.isEmpty())
        {
            recyclerView.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            noReservationsText.setVisibility(View.VISIBLE);
        } else
        {
            reservations.addAll(res);
            progressBar.setVisibility(View.GONE);
            recyclerView.setAdapter(adapterCustomerHome);
            adapterCustomerHome.setOnItemClickListener(this);
        }
    }
    
    @Override
    public void onItemClick(final int position)
    {
        final Reservation res = viewModel.getNextReservations(ReservationViewModel.CUSTOMER).getValue().get(position);
        new AlertDialog.Builder(getContext()).setCancelable(true)
                .setTitle(getString(R.string.areYouSure))
                .setMessage(getString(R.string.deleteReservationFor).concat(res.getOtherUser().getName()).concat(getString(R.string.onWithTabs)).concat(res.getDateFormatted()))
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        viewModel.delete(res.getResUid(), res.getOtherUser().getUid());
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
