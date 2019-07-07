package facchini.riccardo.reservation.Customer_Package.Adapter_Customer;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.List;

import facchini.riccardo.reservation.OnItemClickListener;
import facchini.riccardo.reservation.R;
import facchini.riccardo.reservation.SearchResult;
import facchini.riccardo.reservation.Shop_Package.Shop;

public class Adapter_Customer_SearchCard extends RecyclerView.Adapter<Adapter_Customer_SearchCard.Shops_ViewHolder>
{
    private Context context;
    private List<SearchResult> shopsList;
    private OnItemClickListener itemListener;
    
    
    public Adapter_Customer_SearchCard(Context context, List<SearchResult> shopsList)
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
        holder.ratingBar.setRating((float) shop.getAverageReviews());
    }
    
    @Override
    public int getItemCount()
    {
        return shopsList.size();
    }
    
    class Shops_ViewHolder extends RecyclerView.ViewHolder
    {
        TextView textViewName, textViewDistance, textViewAddress;
        RatingBar ratingBar;
        
        public Shops_ViewHolder(@NonNull View itemView, final OnItemClickListener itemClickListener)
        {
            super(itemView);
            
            textViewName = itemView.findViewById(R.id.textViewShopName);
            textViewAddress = itemView.findViewById(R.id.textViewAddress);
            textViewDistance = itemView.findViewById(R.id.textViewDistance);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            
            
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
