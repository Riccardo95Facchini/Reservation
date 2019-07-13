package facchini.riccardo.reservation.Customer_Package.Fragment_Customer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import java.util.ArrayList;
import java.util.List;

import facchini.riccardo.reservation.CurrentUserViewModel;
import facchini.riccardo.reservation.Customer_Package.Activity_Customer.Activity_Customer_ShopInfo;
import facchini.riccardo.reservation.Customer_Package.Adapter_Customer.Adapter_Customer_ReservationCard;
import facchini.riccardo.reservation.OnItemClickListener;
import facchini.riccardo.reservation.R;
import facchini.riccardo.reservation.ReservationViewModel;
import facchini.riccardo.reservation.Reservation_Package.OnReservationListener;
import facchini.riccardo.reservation.Reservation_Package.ReservationFirestore;
import facchini.riccardo.reservation.Shop_Package.Shop;

public class Fragment_Customer_Home extends Fragment implements OnItemClickListener, OnReservationListener
{
    private ReservationViewModel reservationViewModel;
    private CurrentUserViewModel currentUserViewModel;
    private SharedPreferences pref;
    private List<ReservationFirestore> reservations;
    
    private RecyclerView recyclerView;
    private Adapter_Customer_ReservationCard adapterCustomerHome;
    
    private TextView noReservationsText;
    private ProgressBar progressBar;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        getActivity().setTitle(R.string.reservations);
        return inflater.inflate(R.layout.fragment_customer_home, container, false);
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
        
        reservationViewModel = ViewModelProviders.of(getActivity()).get(ReservationViewModel.class);
        currentUserViewModel = ViewModelProviders.of(getActivity()).get(CurrentUserViewModel.class);
        
        pref = getContext().getSharedPreferences(getString(R.string.reservations_preferences), Context.MODE_PRIVATE);
        
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        reservations = new ArrayList<>();
        adapterCustomerHome = new Adapter_Customer_ReservationCard(getContext(), reservations);
        recyclerView.setAdapter(adapterCustomerHome);
        
        noReservationsText.setVisibility(View.GONE);
        
        progressBar.setVisibility(View.VISIBLE);
        
        final CountDownTimer timer = new CountDownTimer(5000, 500)
        {
            @Override
            public void onTick(long millisUntilFinished)
            { }
            
            @Override
            public void onFinish()
            {
                if (reservationViewModel.getNextReservations().getValue() == null || reservationViewModel.getNextReservations().getValue().isEmpty())
                    showReservations(null);
            }
        }.start();
        
        reservationViewModel.getNextReservations().observe(getActivity(), new Observer<List<ReservationFirestore>>()
        {
            @Override
            public void onChanged(List<ReservationFirestore> reservations)
            {
                if (timer != null)
                    timer.cancel();
                showReservations(reservations);
            }
        });
        
        reservationViewModel.getIsNextEmpty().observe(getActivity(), new Observer<Boolean>()
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
    
    @Override
    public void onResume()
    {
        super.onResume();
        
        try
        {
            if (pref.getBoolean(getString(R.string.need_update_key), false))
            {
                pref.edit().putBoolean(getString(R.string.need_update_key), false).commit();
                reservationViewModel.updateViewModel();
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
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
            reservations.addAll(res);
            progressBar.setVisibility(View.GONE);
            recyclerView.setAdapter(adapterCustomerHome);
            adapterCustomerHome.setOnItemClickListener(this, this);
        }
    }
    
    @Override
    public void onItemClick(final int position)
    {
        final ReservationFirestore res = reservationViewModel.getNextReservations().getValue().get(position);
        new AlertDialog.Builder(getContext()).setCancelable(true)
                .setTitle(getString(R.string.areYouSure))
                .setMessage(getString(R.string.deleteReservationFor).concat(res.getShopName()).concat(getString(R.string.onWithTabs)).concat(res.timeFormatted()))
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        reservationViewModel.delete(res.getUid(), res.getShopUid());
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
                    getContext().startActivity(intent);
                    currentUserViewModel.getSelectedShop().removeObserver(this);
                    currentUserViewModel.getSelectedShop().setValue(null);
                }
            }
        };
        currentUserViewModel.getSelectedShop().observe(getActivity(), observer);
    }
}
