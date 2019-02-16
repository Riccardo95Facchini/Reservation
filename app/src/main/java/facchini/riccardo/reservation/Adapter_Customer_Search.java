package facchini.riccardo.reservation;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class Adapter_Customer_Search extends RecyclerView.Adapter<Adapter_Customer_Search.Shops_ViewHolder>
{
    private Context context;
    private List<SearchResult> shopsList;
    private OnItemClickListener itemListener;
    
    
    public Adapter_Customer_Search(Context context, List<SearchResult> shopsList)
    {
        this.context = context;
        this.shopsList = shopsList;
    }
    
    public void setOnItemClickListener(OnItemClickListener itemListener)
    {
        this.itemListener = itemListener;
    }
    
    @NonNull
    @Override
    public Shops_ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.card_customer_search, null);
        return new Shops_ViewHolder(view, itemListener);
    }
    
    @Override
    public void onBindViewHolder(@NonNull Shops_ViewHolder holder, int pos)
    {
        SearchResult res = shopsList.get(pos);
        Shop shop = res.getShopFound();
        
        holder.textViewName.setText(shop.getName());
        holder.textViewAddress.setText(shop.displayFullAddress());
        holder.textViewDistance.setText(res.getFormatDistance());
        holder.textViewTimes.setText(shop.displayHoursFormat());
        holder.textViewPhoneMail.setText(String.format("Phone: %s \t Mail: %s", shop.getPhone(), shop.getMail()));
    }
    
    @Override
    public int getItemCount()
    {
        return shopsList.size();
    }
    
    class Shops_ViewHolder extends RecyclerView.ViewHolder
    {
        TextView textViewName, textViewDistance, textViewAddress, textViewTimes, textViewPhoneMail;
        
        public Shops_ViewHolder(@NonNull View itemView, final OnItemClickListener itemClickListener)
        {
            super(itemView);
            
            textViewName = itemView.findViewById(R.id.textViewCustomer);
            textViewAddress = itemView.findViewById(R.id.textViewAddress);
            textViewDistance = itemView.findViewById(R.id.textViewDistance);
            textViewTimes = itemView.findViewById(R.id.textViewTimes);
            textViewPhoneMail = itemView.findViewById(R.id.textViewPhoneMail);
            
            itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (itemClickListener != null)
                    {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION)
                            itemListener.onItemClick(position);
                    }
                }
            });
        }
    }
}
