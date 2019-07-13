package facchini.riccardo.reservation.Customer_Package.Adapter_Customer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

import java.util.List;

import facchini.riccardo.reservation.Customer_Package.Activity_Customer.Activity_Customer_ShopInfo;
import facchini.riccardo.reservation.OnItemClickListener;
import facchini.riccardo.reservation.R;
import facchini.riccardo.reservation.Reservation_Package.OnReservationListener;
import facchini.riccardo.reservation.Reservation_Package.Reservation;
import facchini.riccardo.reservation.Shop_Package.Shop;

public class Adapter_Customer_ReservationCard extends RecyclerView.Adapter<Adapter_Customer_ReservationCard.Reservation_Customer_ViewHolder>
{
    
    private Context context;
    private List<Reservation> reservationCustomerHomeList;
    private OnItemClickListener itemListener;
    private OnReservationListener infoListener;
    
    public Adapter_Customer_ReservationCard(Context context, List<Reservation> reservationCustomerHomeList)
    {
        this.context = context;
        this.reservationCustomerHomeList = reservationCustomerHomeList;
    }
    
    public void setOnItemClickListener(OnItemClickListener itemListener, OnReservationListener infoListener)
    {
        this.itemListener = itemListener;
        this.infoListener = infoListener;
    }
    
    @NonNull
    @Override
    public Reservation_Customer_ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.card_reservation, null);
        return new Reservation_Customer_ViewHolder(view, itemListener, infoListener);
    }
    
    @Override
    public void onBindViewHolder(@NonNull Reservation_Customer_ViewHolder holder, int pos)
    {
        Reservation res = reservationCustomerHomeList.get(pos);
        Shop shop = (Shop) res.getOtherUser();
        
        holder.textName.setText(shop.getName());
        holder.textAddress.setText(shop.displayFullAddress());
        holder.textWhen.setText(res.getDateFormatted());
        Glide.with(context).load(shop.getProfilePicUrl()).placeholder(R.drawable.default_avatar).fitCenter().centerCrop().transform(new CircleCrop()).into(holder.profilePic);
    }
    
    @Override
    public int getItemCount()
    {
        return reservationCustomerHomeList.size();
    }
    
    class Reservation_Customer_ViewHolder extends RecyclerView.ViewHolder
    {
        TextView textName, textAddress, textWhen;
        ImageButton infoButton;
        ImageView profilePic;
        OnReservationListener infoListener;
        
        public Reservation_Customer_ViewHolder(@NonNull View itemView, final OnItemClickListener itemClickListener, final OnReservationListener infoListener)
        {
            super(itemView);
            
            textName = itemView.findViewById(R.id.textName);
            textAddress = itemView.findViewById(R.id.textAddress);
            textWhen = itemView.findViewById(R.id.textWhen);
            infoButton = itemView.findViewById(R.id.resButton);
            profilePic = itemView.findViewById(R.id.profilePic);
            this.infoListener = infoListener;
            
            infoButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    infoListener.onInfoClick(getAdapterPosition());
//                    Shop shop = (Shop) reservationCustomerHomeList.get(getAdapterPosition()).getOtherUser();
//                    startShopInfoActivity(shop);
                }
            });
            
            itemView.setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View v)
                {
                    if (itemClickListener != null)
                    {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION)
                            itemListener.onItemClick(position);
                    }
                    return true;
                }
            });
        }
    }
}
