package facchini.riccardo.reservation.Chat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import facchini.riccardo.reservation.OnItemClickListener;
import facchini.riccardo.reservation.R;

public class Adapter_Chat extends RecyclerView.Adapter<Adapter_Chat.ChatEntry_ViewHolder>
{
    private Context context;
    private List<ChatEntry> chatEntryList;
    private OnItemClickListener itemListener;
    
    public Adapter_Chat(Context context, List<ChatEntry> chatEntryList)
    {
        this.context = context;
        this.chatEntryList = chatEntryList;
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
        ChatEntry chat = chatEntryList.get(pos);
        holder.textViewChatWith.setText(chat.getOtherName());
        holder.textViewWhen.setText(chat.getDateFormatted());
    }
    
    @Override
    public int getItemCount()
    {
        return chatEntryList.size();
    }
    
    public class ChatEntry_ViewHolder extends RecyclerView.ViewHolder
    {
        
        TextView textViewChatWith, textViewWhen;
        ImageView profilePic;
        
        public ChatEntry_ViewHolder(@NonNull View itemView, final OnItemClickListener itemClickListener)
        {
            super(itemView);
            
            textViewChatWith = itemView.findViewById(R.id.textViewChatWith);
            textViewWhen = itemView.findViewById(R.id.textViewWhen);
            //profilePic;
            
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
