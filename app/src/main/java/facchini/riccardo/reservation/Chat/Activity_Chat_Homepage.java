package facchini.riccardo.reservation.Chat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Debug;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import facchini.riccardo.reservation.OnItemClickListener;
import facchini.riccardo.reservation.R;

public class Activity_Chat_Homepage extends AppCompatActivity /*implements OnItemClickListener*/
{
    //Firestore
    private FirebaseFirestore db;
    private CollectionReference chatsCollection;
    
    private String userUid, userName;
    private ArrayList<ChatEntry> chatList = new ArrayList<>();
    private Adapter_Chat adapterChat;
    
    private TextView noChats;
    private RecyclerView chatsRecycleView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__chat__homepage);
        
        db = FirebaseFirestore.getInstance();
        userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
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
                        chatList.add(new ChatEntry(entry.getKey(), ((Timestamp) entry.getValue()).toDate()));
                }
                
                if (chatList.isEmpty())
                {
                    noChats.setVisibility(View.VISIBLE);
                    chatsRecycleView.setVisibility(View.GONE);
                } else
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
                                ChatEntry chatEntry = chatList.get(position);
                                Intent intent = new Intent(getBaseContext(), Activity_Chat.class);
                                intent.putExtra("thisUserUid", userUid);
                                intent.putExtra("thisUserUsername", userUid); //TODO: fix by sending username not uid
                                intent.putExtra("otherUserUid", chatEntry.getOtherUid());
                                intent.putExtra("otherUserUsername", chatEntry.getOtherName());
                                startActivity(intent);
                            } catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
    }

//    @Override
//    public void onItemClick(final int position)
//    {
//        ChatEntry chatEntry = chatList.get(position);
//        //TODO: open chat
//    }
}
