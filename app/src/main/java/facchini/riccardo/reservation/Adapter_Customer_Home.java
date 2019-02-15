package facchini.riccardo.reservation;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class Adapter_Customer_Home extends RecyclerView.Adapter<Adapter_Customer_Home.Reservation_Customer_ViewHolder>
{
    
    private Context context;
    private List<Reservation_Customer_Home> reservationCustomerHomeList;
    //private static OnItemClickListener itemListener;
    
    
    public Adapter_Customer_Home(Context context, List<Reservation_Customer_Home> reservationCustomerHomeList)
    {
        this.context = context;
        this.reservationCustomerHomeList = reservationCustomerHomeList;
        //this.itemListener = itemListener;
    }
    
    @NonNull
    @Override
    public Reservation_Customer_ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.card_customer_home, null);
        return new Reservation_Customer_ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull Reservation_Customer_ViewHolder holder, int pos)
    {
        Reservation_Customer_Home res = reservationCustomerHomeList.get(pos);
        Shop shop = res.getShop();
        
        holder.textViewShop.setText(shop.getName());
        holder.textViewAddress.setText(shop.getFullAddress());
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
        
        public Reservation_Customer_ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            
            textViewShop = itemView.findViewById(R.id.textViewCustomer);
            textViewAddress = itemView.findViewById(R.id.textViewAddress);
            textViewWhen = itemView.findViewById(R.id.textViewWhen);
        }
    }
}
