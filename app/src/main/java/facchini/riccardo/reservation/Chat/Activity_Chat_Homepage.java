package facchini.riccardo.reservation.Chat;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import facchini.riccardo.reservation.OnItemClickListener;
import facchini.riccardo.reservation.R;

public class Activity_Chat_Homepage extends AppCompatActivity
{
    //Firestore
    private FirebaseFirestore db;
    private CollectionReference chatsCollection;
    
    private String userUid;
    private ArrayList<ChatData> chatList = new ArrayList<>();
    private Adapter_Chat adapterChat;
    
    private TextView noChats;
    private RecyclerView chatsRecycleView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__chat__homepage);
        
        setTitle(R.string.conversations);
        
        db = FirebaseFirestore.getInstance();
        
        userUid = getSharedPreferences(getString(R.string.reservations_preferences), Context.MODE_PRIVATE)
                .getString(getString(R.string.current_user_uid_key), "");
        
        chatsCollection = db.collection("chats");
        
        noChats = findViewById(R.id.noChatsText);
        chatsRecycleView = findViewById(R.id.chatsRecycleView);
        chatsRecycleView.setHasFixedSize(true);
        chatsRecycleView.setLayoutManager(new LinearLayoutManager(this));
        
        chatsCollection.document(userUid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
        {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot)
            {
                if (documentSnapshot.exists())
                {
                    for (Map.Entry<String, Object> entry : documentSnapshot.getData().entrySet())
                        chatList.add(new ChatData((HashMap<String, Object>) entry.getValue()));
                }
                
                if (chatList.isEmpty())
                {
                    noChats.setVisibility(View.VISIBLE);
                    chatsRecycleView.setVisibility(View.GONE);
                } else
                {
                    fillChatCards();
                }
            }
        });
    }
    
    /**
     * Called if there are past chats, it orders them from the most recent (top) to least one (bottom)
     */
    private void fillChatCards()
    {
        Collections.sort(chatList, Collections.reverseOrder());
        adapterChat = new Adapter_Chat(getBaseContext(), chatList);
        chatsRecycleView.setAdapter(adapterChat);
        adapterChat.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(int position)
            {
                try
                {
                    ChatData chatData = chatList.get(position);
                    Intent intent = new Intent(getBaseContext(), Activity_Chat.class);
                    intent.putExtra("thisUid", userUid);
                    intent.putExtra("thisUsername", chatData.getThisName());
                    intent.putExtra("otherUid", chatData.getOtherUid());
                    intent.putExtra("otherUsername", chatData.getOtherName());
                    startActivity(intent);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }
}
