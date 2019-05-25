package facchini.riccardo.reservation.Customer_Package.Adapter_Customer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import facchini.riccardo.reservation.Customer_Package.Activity_Customer.Activity_Customer_ShopInfo;
import facchini.riccardo.reservation.OnItemClickListener;
import facchini.riccardo.reservation.R;
import facchini.riccardo.reservation.Reservation_Package.Reservation_Customer_Home;
import facchini.riccardo.reservation.Shop_Package.Shop;

public class Adapter_Customer_ReservationCard extends RecyclerView.Adapter<Adapter_Customer_ReservationCard.Reservation_Customer_ViewHolder>
{
    
    private Context context;
    private List<Reservation_Customer_Home> reservationCustomerHomeList;
    private OnItemClickListener itemListener;
    
    
    public Adapter_Customer_ReservationCard(Context context, List<Reservation_Customer_Home> reservationCustomerHomeList)
    {
        this.context = context;
        this.reservationCustomerHomeList = reservationCustomerHomeList;
    }
    
    public void setOnItemClickListener(OnItemClickListener itemListener)
    {
        this.itemListener = itemListener;
    }
    
    @NonNull
    @Override
    public Reservation_Customer_ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.card_customer_home, null);
        return new Reservation_Customer_ViewHolder(view, itemListener);
    }
    
    @Override
    public void onBindViewHolder(@NonNull Reservation_Customer_ViewHolder holder, int pos)
    {
        Reservation_Customer_Home res = reservationCustomerHomeList.get(pos);
        Shop shop = res.getShop();
        
        holder.textViewShop.setText(shop.getName());
        holder.textViewAddress.setText(shop.displayFullAddress());
        holder.textViewWhen.setText(res.getDateFormatted());
    }
    
    @Override
    public int getItemCount()
    {
        return reservationCustomerHomeList.size();
    }
    
    class Reservation_Customer_ViewHolder extends RecyclerView.ViewHolder
    {
        TextView textViewShop, textViewAddress, textViewWhen;
        ImageButton infoButton;
        
        public Reservation_Customer_ViewHolder(@NonNull View itemView, final OnItemClickListener itemClickListener)
        {
            super(itemView);
            
            textViewShop = itemView.findViewById(R.id.textViewShopName);
            textViewAddress = itemView.findViewById(R.id.textViewAddress);
            textViewWhen = itemView.findViewById(R.id.textViewWhen);
            infoButton = itemView.findViewById(R.id.infoButton);
            
            infoButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Shop shop = reservationCustomerHomeList.get(getAdapterPosition()).getShop();
                    startShopInfoActivity(shop);
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
        
        /**
         * Puts shop into a bundle in the intent and launches it
         *
         * @param shop The shop for which the info are requested
         */
        private void startShopInfoActivity(Shop shop)
        {
            Intent intent = new Intent(context, Activity_Customer_ShopInfo.class);
            Bundle b = new Bundle();
            b.putParcelable("Selected", shop);
            intent.putExtras(b);
            context.startActivity(intent);
        }
    }
}
