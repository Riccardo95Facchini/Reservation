package facchini.riccardo.reservation.Customer_Package.Activity_Customer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import facchini.riccardo.reservation.R;
import facchini.riccardo.reservation.RatingDialog;
import facchini.riccardo.reservation.Review;
import facchini.riccardo.reservation.Shop_Package.Shop;

public class Activity_Customer_ShopInfo extends AppCompatActivity
{
    private ImageButton buttonChat;
    private CollectionReference reviewsRef;
    
    private Shop shop;
    private String userUid;
    private String reviewId;
    private long pastRating;
    private SharedPreferences pref;
    
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_shop_info);
        
        reviewsRef = FirebaseFirestore.getInstance().collection("reviews");
        
        pref = getSharedPreferences(getString(R.string.reservations_preferences), Context.MODE_PRIVATE);
        userUid = FirebaseAuth.getInstance().getUid();
        
        
        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        if (b != null)
            shop = b.getParcelable("Selected");
        
        setTitle(shop.getName());
        checkReviewExists();
        
        Button buttonRate = findViewById(R.id.buttonRate);
        TextView textReviews = findViewById(R.id.textReviews);
        buttonChat = findViewById(R.id.buttonChat);
        ImageView profilePic = findViewById(R.id.profilePic);
        RatingBar ratingAvg = findViewById(R.id.ratingAvg);
        
        textReviews.setText(String.format("(%.2f/5) %d %s", shop.getAverageReviews(), shop.getNumReviews(), getString(R.string.reviews)));
        Glide.with(this).load(shop.getProfilePicUrl()).placeholder(R.drawable.default_avatar).fitCenter().centerCrop().transform(new CircleCrop()).into(profilePic);
        ratingAvg.setRating((float) shop.getAverageReviews());
        
        buttonChat.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //startChat();
            }
        });
        
        buttonRate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startRateDialog();
            }
        });
    }
    
    private void startRateDialog()
    {
        RatingDialog ratingDialog = new RatingDialog(this, R.style.RatingDialogTheme, pastRating);
        
        ratingDialog.getRating().setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener()
        {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser)
            {
                sendReview(Math.round(rating));
            }
        });
        ratingDialog.show();
    }
    
    /**
     * Checks if this user has already made a review for this shop,
     * if so it stores the value and the id of the document
     */
    private void checkReviewExists()
    {
        pastRating = -1;
        reviewId = "";
        
        reviewsRef.whereEqualTo("shopUid", shop.getUid())
                .whereEqualTo("userUid", userUid).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
                {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots)
                    {
                        if (!queryDocumentSnapshots.isEmpty())
                        {
                            reviewId = queryDocumentSnapshots.getDocuments().get(0).getId();
                            pastRating = (long) queryDocumentSnapshots.getDocuments().get(0).get("reviewScore");
                        }
                    }
                });
    }
    
    /**
     * Sends the review and sets the boolean to force the update, still it may be faster than the calling of the server side function.
     * There is no actual way to implement a scalable update system since the listeners would constantly launch during a real deploy.
     */
    private void sendReview(int rating)
    {
        if (reviewId.isEmpty())
        {
            reviewsRef.document().set(new Review(rating, shop.getUid(), userUid));
        } else
            reviewsRef.document(reviewId).update("reviewScore", rating);
        
        pref.edit().putBoolean(getString(R.string.need_update_key), true).commit();
        this.finish();
    }
}
