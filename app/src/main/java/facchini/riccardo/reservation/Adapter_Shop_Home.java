package facchini.riccardo.reservation;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class Adapter_Shop_Home extends RecyclerView.Adapter<Adapter_Shop_Home.Reservation_Shop_ViewHolder>
{
    
    private Context context;
    private List<Reservation_Shop_Home> reservationCustomerHomeList;
    
    public Adapter_Shop_Home(Context context, List<Reservation_Shop_Home> reservationCustomerHomeList)
    {
        this.context = context;
        this.reservationCustomerHomeList = reservationCustomerHomeList;
    }
    
    @NonNull
    @Override
    public Reservation_Shop_ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.card_shop_home, null);
        return new Reservation_Shop_ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull Reservation_Shop_ViewHolder holder, int pos)
    {
        Reservation_Shop_Home res = reservationCustomerHomeList.get(pos);
        Customer customer = res.getCustomer();
        
        holder.textViewCustomer.setText(customer.getName().concat(" ").concat(customer.getSurname()));
        holder.textViewWhen.setText(res.getDateFormatted());
    }
    
    @Override
    public int getItemCount()
    {
        return reservationCustomerHomeList.size();
    }
    
    class Reservation_Shop_ViewHolder extends RecyclerView.ViewHolder
    {
        TextView textViewCustomer, textViewWhen;
        
        public Reservation_Shop_ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            
            textViewCustomer = itemView.findViewById(R.id.textViewCustomer);
            textViewWhen = itemView.findViewById(R.id.textViewWhen);
        }
    }
}
