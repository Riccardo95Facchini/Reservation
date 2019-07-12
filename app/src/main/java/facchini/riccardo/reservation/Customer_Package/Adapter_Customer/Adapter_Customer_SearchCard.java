package facchini.riccardo.reservation.Customer_Package.Adapter_Customer;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

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
        
        holder.textName.setText(shop.getName());
        holder.textAddress.setText(shop.displayFullAddress());
        holder.textDistance.setText(res.getFormatDistance());
        holder.ratingBar.setRating((float) shop.getAverageReviews());
        Glide.with(context).load(shop.getProfilePicUrl()).placeholder(R.drawable.default_avatar).fitCenter().centerCrop().transform(new CircleCrop()).into(holder.profilePic);
        
    }
    
    @Override
    public int getItemCount()
    {
        return shopsList.size();
    }
    
    class Shops_ViewHolder extends RecyclerView.ViewHolder
    {
        TextView textName, textDistance, textAddress;
        ImageView profilePic;
        RatingBar ratingBar;
        
        public Shops_ViewHolder(@NonNull View itemView, final OnItemClickListener itemClickListener)
        {
            super(itemView);
            
            textName = itemView.findViewById(R.id.textName);
            textAddress = itemView.findViewById(R.id.textAddress);
            textDistance = itemView.findViewById(R.id.textDistance);
            profilePic = itemView.findViewById(R.id.profilePic);
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
