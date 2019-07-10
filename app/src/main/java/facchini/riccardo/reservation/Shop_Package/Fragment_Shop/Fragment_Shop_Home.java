package facchini.riccardo.reservation.Shop_Package.Fragment_Shop;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import facchini.riccardo.reservation.R;
import facchini.riccardo.reservation.ReservationViewModel;
import facchini.riccardo.reservation.Reservation_Package.Reservation;
import facchini.riccardo.reservation.SharedViewModel;
import facchini.riccardo.reservation.Shop_Package.Adapter_Shop.Adapter_Shop_Home;
import facchini.riccardo.reservation.Shop_Package.Shop;

public class Fragment_Shop_Home extends Fragment
{
    //Firestore
    private FirebaseFirestore db;
    private CollectionReference shopsCollection;
    
    private String shopUid;
    private SharedViewModel sharedViewModel;
    private ReservationViewModel viewModel;
    private List<Reservation> reservations;
    
    private RecyclerView recyclerView;
    private Adapter_Shop_Home adapterShopHome;
    
    private TextView noReservationsText;
    private ProgressBar progressBar;
    
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
        recyclerView = view.findViewById(R.id.futureReservations);
        noReservationsText = view.findViewById(R.id.noReservations);
        progressBar = view.findViewById(R.id.progressBar);
    }
    
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        sharedViewModel = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        viewModel = ViewModelProviders.of(getActivity()).get(ReservationViewModel.class);
        
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        noReservationsText.setVisibility(View.GONE);
        
        db = FirebaseFirestore.getInstance();
        shopUid = FirebaseAuth.getInstance().getUid();
        shopsCollection = db.collection("shops");
        
        reservations = new ArrayList<>();
        adapterShopHome = new Adapter_Shop_Home(getContext(), reservations);
        
        shopsCollection.document(shopUid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
        {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot)
            {
                if (documentSnapshot.exists())
                    sharedViewModel.setCurrentShop(new Shop(documentSnapshot.getData()));
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
                if (viewModel.getNextReservations(ReservationViewModel.SHOP).getValue() == null || viewModel.getNextReservations(ReservationViewModel.SHOP).getValue().isEmpty())
                    showReservations(null);
            }
        }.start();
        
        viewModel.getNextReservations(ReservationViewModel.SHOP).observe(getActivity(), new Observer<List<Reservation>>()
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
            recyclerView.setVisibility(View.VISIBLE);
            reservations.addAll(res);
            progressBar.setVisibility(View.GONE);
            recyclerView.setAdapter(adapterShopHome);
        }
    }
    
    /**
     * Orders list to be displayed and adds elements to the adapter
     */
    private void orderList()
    {
        Collections.sort(reservations, reservationComparator);
        adapterShopHome = new Adapter_Shop_Home(getContext(), reservations);
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
