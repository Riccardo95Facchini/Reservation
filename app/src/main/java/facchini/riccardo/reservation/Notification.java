package facchini.riccardo.reservation;

import android.content.Context;

import com.google.firebase.firestore.FirebaseFirestore;

public class Notification
{
    public static final String NOTIFICATION_CHAT = "notification_chat";
    public static final String NOTIFICATION_RESERVATION = "notification_reservation";
    private String recipientUid;
    private String title;
    private String body;
    
    public Notification(String recipientUid, String senderName, String type, String body, Context context)
    {
        this.recipientUid = recipientUid;
        
        if (type.equals(NOTIFICATION_CHAT))
            this.title = senderName;
        else if (type.equals(NOTIFICATION_RESERVATION))
            this.title = context.getString(R.string.new_reservation_from) + senderName;
        
        this.body = body;
        sendNotification();
    }
    
    public String getRecipientUid() {return recipientUid;}
    
    public String getTitle() {return title;}
    
    public String getBody() {return body;}
    
    private void sendNotification()
    {
        FirebaseFirestore.getInstance().collection("notificationRequests").document().set(this);
    }
}
