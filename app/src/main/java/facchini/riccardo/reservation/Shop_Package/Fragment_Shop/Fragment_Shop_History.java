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

import java.util.ArrayList;
import java.util.List;

import facchini.riccardo.reservation.R;
import facchini.riccardo.reservation.ReservationViewModel;
import facchini.riccardo.reservation.Reservation_Package.ReservationFirestore;
import facchini.riccardo.reservation.Shop_Package.Adapter_Shop.Adapter_Shop_ReservationCard;

public class Fragment_Shop_History extends Fragment
{
    
    private ReservationViewModel viewModel;
    private List<ReservationFirestore> reservations;
    
    private RecyclerView recyclerView;
    private Adapter_Shop_ReservationCard adapterShopHistory;
    
    private TextView noReservationsText;
    private ProgressBar progressBar;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        getActivity().setTitle(R.string.history);
        return inflater.inflate(R.layout.fragment_shop_history, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        recyclerView = view.findViewById(R.id.pastReservations);
        noReservationsText = view.findViewById(R.id.noReservations);
        progressBar = view.findViewById(R.id.progressBar);
    }
    
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity()).get(ReservationViewModel.class);
        
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        noReservationsText.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        
        reservations = new ArrayList<>();
        adapterShopHistory = new Adapter_Shop_ReservationCard(getContext(), reservations);
        recyclerView.setAdapter(adapterShopHistory);
        
        final CountDownTimer timer = new CountDownTimer(500000, 500)
        {
            @Override
            public void onTick(long millisUntilFinished)
            { }
            
            @Override
            public void onFinish()
            {
                if (viewModel.getPastReservations().getValue() == null || viewModel.getPastReservations().getValue().isEmpty())
                    showReservations(null);
            }
        }.start();
        
        
        viewModel.getPastReservations().observe(getActivity(), new Observer<List<ReservationFirestore>>()
        {
            @Override
            public void onChanged(List<ReservationFirestore> reservations)
            {
                if (timer != null)
                    timer.cancel();
                showReservations(reservations);
            }
        });
        
        viewModel.getIsPastEmpty().observe(getActivity(), new Observer<Boolean>()
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
    
    private void showReservations(List<ReservationFirestore> res)
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
            recyclerView.setAdapter(adapterShopHistory);
        }
    }
    
}
