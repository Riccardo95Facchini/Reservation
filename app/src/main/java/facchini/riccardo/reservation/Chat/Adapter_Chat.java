package facchini.riccardo.reservation.Chat;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

import java.util.List;

import facchini.riccardo.reservation.OnItemClickListener;
import facchini.riccardo.reservation.R;

public class Adapter_Chat extends RecyclerView.Adapter<Adapter_Chat.ChatEntry_ViewHolder>
{
    private Context context;
    private List<ChatData> chatDataList;
    private OnItemClickListener itemListener;
    
    public Adapter_Chat(Context context, List<ChatData> chatDataList)
    {
        this.context = context;
        this.chatDataList = chatDataList;
    }
    
    public void setOnItemClickListener(OnItemClickListener itemListener)
    {
        this.itemListener = itemListener;
    }
    
    @NonNull
    @Override
    public ChatEntry_ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.card_chat_entry, null);
        return new ChatEntry_ViewHolder(view, itemListener);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ChatEntry_ViewHolder holder, int pos)
    {
        ChatData chat = chatDataList.get(pos);
        
        holder.imageNew.setImageResource(R.drawable.ic_new);
        if (!chat.isRead())
            holder.imageNew.setVisibility(View.VISIBLE);
        else
            holder.imageNew.setVisibility(View.GONE);
        
        Glide.with(context).load(chat.getOtherPhoto()).placeholder(R.drawable.default_avatar).fitCenter().centerCrop().transform(new CircleCrop()).into(holder.profilePic);
        holder.textViewChatWith.setText(chat.getOtherName());
        holder.textViewWhen.setText(chat.dateFormatted());
    }
    
    @Override
    public int getItemCount()
    {
        return chatDataList.size();
    }
    
    public class ChatEntry_ViewHolder extends RecyclerView.ViewHolder
    {
        TextView textViewChatWith, textViewWhen;
        ImageView profilePic, imageNew;
        
        public ChatEntry_ViewHolder(@NonNull final View itemView, final OnItemClickListener itemClickListener)
        {
            super(itemView);
            
            textViewChatWith = itemView.findViewById(R.id.textViewShopName);
            textViewWhen = itemView.findViewById(R.id.textViewWhen);
            imageNew = itemView.findViewById(R.id.imageNew);
            profilePic = itemView.findViewById(R.id.profilePic);
            
            itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (itemClickListener != null)
                    {
                        imageNew.setVisibility(View.GONE);
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION)
                            itemListener.onItemClick(position);
                    }
                }
            });
            
        }
    }
}
