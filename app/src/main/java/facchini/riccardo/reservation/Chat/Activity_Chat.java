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
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.HashMap;
import java.util.Map;

import facchini.riccardo.reservation.R;


public class Activity_Chat extends AppCompatActivity
{
    LinearLayout layout;
    RelativeLayout layout_2;
    ImageView sendButton;
    EditText messageArea;
    ScrollView scrollView;
    Firebase reference;
    
    String thisUserUid, otherUserUid, thisUserUsername, otherUserUsername, nodeName;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        
        layout = findViewById(R.id.layout1);
        layout_2 = findViewById(R.id.layout2);
        sendButton = findViewById(R.id.sendButton);
        messageArea = findViewById(R.id.messageArea);
        scrollView = findViewById(R.id.scrollView);
        
        Intent pastIntent = getIntent();
        thisUserUid = pastIntent.getStringExtra("thisUserUid");
        thisUserUsername = pastIntent.getStringExtra("thisUserUsername");
        otherUserUid = pastIntent.getStringExtra("otherUserUid");
        otherUserUsername = pastIntent.getStringExtra("otherUserUsername");
        
        Firebase.setAndroidContext(this);
        
        setTitle(otherUserUsername);
        
        //First smaller string
        if (thisUserUid.compareTo(otherUserUid) <= 0)
        {
            nodeName = thisUserUid + "_" + otherUserUid;
            reference = new Firebase("https://reservation-fed21.firebaseio.com/messages/" + nodeName);
        } else
        {
            nodeName = otherUserUid + "_" + thisUserUid;
            reference = new Firebase("https://reservation-fed21.firebaseio.com/messages/" + nodeName);
        }
        
        sendButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String messageText = messageArea.getText().toString();
                
                if (!messageText.equals(""))
                {
                    Map<String, String> map = new HashMap<>();
                    map.put("message", messageText);
                    map.put("user", thisUserUsername);
                    reference.push().setValue(map);
                    messageArea.setText("");
                }
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
                
                if (userName.equals(thisUserUsername))
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
    
    public void addMessageBox(String message, int type)
    {
        TextView textView = new TextView(Activity_Chat.this);
        textView.setText(message);
        
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.weight = 1.0f;
        
        if (type == 1)
        {
            lp2.gravity = Gravity.RIGHT;
            textView.setBackgroundResource(R.drawable.bubble_in);
        } else
        {
            lp2.gravity = Gravity.LEFT;
            textView.setBackgroundResource(R.drawable.bubble_out);
        }
        textView.setLayoutParams(lp2);
        layout.addView(textView);
        scrollView.fullScroll(View.FOCUS_DOWN);
    }
}
