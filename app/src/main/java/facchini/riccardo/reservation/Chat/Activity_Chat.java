package facchini.riccardo.reservation.Chat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import facchini.riccardo.reservation.R;


public class Activity_Chat extends AppCompatActivity
{
    private LinearLayout layout;
    private ImageView sendButton;
    private EditText messageArea;
    private ScrollView scrollView;
    private Firebase reference;
    
    private boolean justOpened = true;
    private String thisUid, otherUid, thisUsername, otherUsername, nodeName;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        
        layout = findViewById(R.id.layout1);
        sendButton = findViewById(R.id.sendButton);
        messageArea = findViewById(R.id.messageArea);
        scrollView = findViewById(R.id.scrollView);
        
        Intent pastIntent = getIntent();
        thisUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        thisUsername = pastIntent.getStringExtra("thisUsername");
        otherUid = pastIntent.getStringExtra("otherUid");
        otherUsername = pastIntent.getStringExtra("otherUsername");
        
        Firebase.setAndroidContext(this);
        
        setTitle(otherUsername);
        
        //First smaller string
        if (thisUid.compareTo(otherUid) <= 0)
        {
            nodeName = thisUid + "_" + otherUid;
            reference = new Firebase("https://reservation-fed21.firebaseio.com/messages/" + nodeName);
        } else
        {
            nodeName = otherUid + "_" + thisUid;
            reference = new Firebase("https://reservation-fed21.firebaseio.com/messages/" + nodeName);
        }
        
        sendButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String messageText = messageArea.getText().toString();
                
                if (!messageText.equals(""))
                    updateDatabase(messageText);
            }
        });
        
        reference.addChildEventListener(new ChildEventListener()
        {
            @Override
            public void onChildAdded(com.firebase.client.DataSnapshot dataSnapshot, String s)
            {
                Map map = dataSnapshot.getValue(Map.class);
                String message = map.get("message").toString();
                String userName = map.get("user").toString();
                
                if (userName.equals(thisUsername))
                {
                    addMessageBox(message, 1);
                } else
                {
                    addMessageBox(message, 2);
                }
            }
            
            @Override
            public void onChildChanged(com.firebase.client.DataSnapshot dataSnapshot, String s)
            {
                
            }
            
            @Override
            public void onChildRemoved(com.firebase.client.DataSnapshot dataSnapshot)
            {
                
            }
            
            @Override
            public void onChildMoved(com.firebase.client.DataSnapshot dataSnapshot, String s)
            {
                
            }
            
            @Override
            public void onCancelled(FirebaseError firebaseError)
            {
                
            }
        });
        
    }
    
    @Override
    protected void onStop()
    {
        setThisRead();
        super.onStop();
    }
    
    /**
     * Does all the updates of the database, which includes setting the message in
     * the Realtime DB and updating the two entry in the Cloud Firestore one
     *
     * @param messageText sent text
     */
    private void updateDatabase(String messageText)
    {
        HashMap<String, ChatData> mapThis = new HashMap<>();
        ChatData thisData = new ChatData(thisUsername, otherUsername, otherUid, messageText, "", new Date());
        thisData.setRead(true);
        mapThis.put(otherUid, thisData);
        FirebaseFirestore.getInstance().collection("chats").document(thisUid).set(mapThis, SetOptions.merge());
        
        HashMap<String, ChatData> mapOther = new HashMap<>();
        ChatData otherData = new ChatData(otherUsername, thisUsername, thisUid, messageText, "", new Date());
        mapOther.put(thisUid, otherData);
        FirebaseFirestore.getInstance().collection("chats").document(otherUid).set(mapOther, SetOptions.merge());
        
        Map<String, String> map = new HashMap<>();
        map.put("message", messageText);
        map.put("user", thisUsername);
        reference.push().setValue(map);
        messageArea.setText("");
    }
    
    /**
     * Updates the messagebox by inserting a new bubble in the chat page
     *
     * @param message text of the current bubble
     * @param type    either 1 (incoming message) or 2 (outcoming message)
     */
    public void addMessageBox(String message, int type)
    {
        if (justOpened)
            setThisRead();
        
        TextView textView = new TextView(Activity_Chat.this);
        textView.setText(message);
        
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.weight = 1.0f;
        
        if (type == 1)
        {
            lp2.gravity = Gravity.RIGHT;
            textView.setBackgroundResource(R.drawable.bubble_out);
        } else
        {
            lp2.gravity = Gravity.LEFT;
            textView.setBackgroundResource(R.drawable.bubble_in);
        }
        textView.setLayoutParams(lp2);
        layout.addView(textView);
        scrollView.fullScroll(View.FOCUS_DOWN);
    }
    
    /**
     * Sets the isRead value in the database to true if it isn't already.
     */
    private void setThisRead()
    {
        justOpened = false;
        FirebaseFirestore.getInstance().collection("chats").document(thisUid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
        {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot)
            {
                ChatData thisData = new ChatData((HashMap<String, Object>) documentSnapshot.getData().get(otherUid));
                if (thisData.isRead())
                    return;
                
                HashMap<String, ChatData> mapThis = new HashMap<>();
                thisData.setRead(true);
                mapThis.put(otherUid, thisData);
                FirebaseFirestore.getInstance().collection("chats").document(thisUid).set(mapThis, SetOptions.merge());
            }
        });
    }
    
}
