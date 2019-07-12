package facchini.riccardo.reservation.Shop_Package.Adapter_Shop;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import facchini.riccardo.reservation.Chat.Activity_Chat;
import facchini.riccardo.reservation.Customer_Package.Customer;
import facchini.riccardo.reservation.R;
import facchini.riccardo.reservation.Reservation_Package.Reservation;

public class Adapter_Shop_Home extends RecyclerView.Adapter<Adapter_Shop_Home.Reservation_Shop_ViewHolder>
{
    
    private Context context;
    //private String customerUid;
    private List<Reservation> reservationCustomerHomeList;
    
    public Adapter_Shop_Home(Context context, List<Reservation> reservationCustomerHomeList)
    {
        this.context = context;
        this.reservationCustomerHomeList = reservationCustomerHomeList;
    }
    
    @NonNull
    @Override
    public Reservation_Shop_ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.card_reservation, null);
        return new Reservation_Shop_ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull Reservation_Shop_ViewHolder holder, int pos)
    {
        Reservation res = reservationCustomerHomeList.get(pos);
        Customer customer = (Customer) res.getOtherUser();
        
        holder.textName.setText(customer.getName());
        holder.textWhen.setText(res.getDateFormatted());
        holder.customerUid = customer.getUid();
        Glide.with(context).load(customer.getProfilePicUrl()).placeholder(R.drawable.default_avatar).fitCenter().centerCrop().transform(new CircleCrop()).into(holder.profilePic);
    }
    
    @Override
    public int getItemCount()
    {
        return reservationCustomerHomeList.size();
    }
    
    class Reservation_Shop_ViewHolder extends RecyclerView.ViewHolder
    {
        TextView textName, textWhen, textAddress;
        ImageButton resButton;
        ImageView profilePic;
        String customerUid;
        
        public Reservation_Shop_ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            
            textName = itemView.findViewById(R.id.textName);
            textWhen = itemView.findViewById(R.id.textWhen);
            textAddress = itemView.findViewById(R.id.textAddress);
            resButton = itemView.findViewById(R.id.resButton);
            profilePic = itemView.findViewById(R.id.profilePic);
            
            textAddress.setVisibility(View.GONE);
            resButton.setImageResource(R.drawable.ic_chat_primary_color_32dp);
    
            resButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(final View v)
                {
                    startChat(v);
                }
            });
        }
        
        /**
         * Starts the chat with the selected user
         *
         * @param v Current view
         */
        void startChat(View v)
        {
            Intent chatIntent = new Intent(v.getContext(), Activity_Chat.class);
            SharedPreferences pref = context.getSharedPreferences(context.getString(R.string.reservations_preferences), Context.MODE_PRIVATE);
            String thisUsername = pref.getString(context.getString(R.string.current_user_username_key), "");
            chatIntent.putExtra("thisUsername", thisUsername);
            chatIntent.putExtra("otherUid", customerUid);
            chatIntent.putExtra("otherUsername", textName.getText());
            v.getContext().startActivity(chatIntent);
        }
    }
}
