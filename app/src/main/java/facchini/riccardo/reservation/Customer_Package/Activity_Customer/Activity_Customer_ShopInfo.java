package facchini.riccardo.reservation.Customer_Package.Activity_Customer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import facchini.riccardo.reservation.R;
import facchini.riccardo.reservation.Review;
import facchini.riccardo.reservation.Shop_Package.Shop;

public class Activity_Customer_ShopInfo extends AppCompatActivity
{
    private RatingBar ratingReview;
    private Button buttonSend;
    private CollectionReference reviewsRef;
    
    private Shop shop;
    private String userUid;
    private String reviewId;
    private long pastRating;
    private int currentRating;
    private SharedPreferences pref;
    
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_shop_info);
        
        reviewsRef = FirebaseFirestore.getInstance().collection("reviews");
        
        pref = getSharedPreferences(getString(R.string.reservations_preferences), Context.MODE_PRIVATE);
        userUid = pref.getString(getString(R.string.current_user_uid_key), "");
        
        
        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        if (b != null)
            shop = b.getParcelable("Selected");
        
        checkReviewExists();
        
        TextView textShopName = findViewById(R.id.textShopName);
        TextView textHours = findViewById(R.id.textHours);
        TextView textReviews = findViewById(R.id.textReviews);
        TextView textPhoneMail = findViewById(R.id.textPhoneMail);
        TextView textAddress = findViewById(R.id.textAddress);
        buttonSend = findViewById(R.id.buttonEdit);
        //ImageView shopPic = findViewById(R.id.shopPic);  TODO: profile pic
        ratingReview = findViewById(R.id.ratingAvg);
        RatingBar ratingAvg = findViewById(R.id.ratingAvg);
        
        textShopName.setText(shop.getName());
        textAddress.setText(String.format("%s %s %s %s", shop.getAddress1(), shop.getAddress2(),
                shop.getCity(), shop.getZip()));
        textReviews.setText(String.format("(%.2f/5) %d %s", shop.getAverageReviews(), shop.getNumReviews(), getString(R.string.reviews)));
        textPhoneMail.setText(String.format("Phone: %s\nMail: %s", shop.getPhone(), shop.getMail()));
        textHours.setText(shop.displayHoursFormat());
        //shopPic.setImageResource();
        ratingAvg.setRating((float) shop.getAverageReviews());
        
        ratingReview.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener()
        {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser)
            {
                if (rating <= 0 || rating == pastRating)
                    buttonSend.setEnabled(false);
                else
                {
                    buttonSend.setEnabled(true);
                    currentRating = (int) rating;
                }
            }
        });
        
        buttonSend.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendReview();
            }
        });
    }
    
    /**
     * Checks if this user has already made a review for this shop,
     * if so it stores the value and the id of the document before setting the stars as already filled
     */
    private void checkReviewExists()
    {
        reviewsRef.whereEqualTo("shopUid", shop.getUid())
                .whereEqualTo("userUid", userUid).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
                {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots)
                    {
                        if (queryDocumentSnapshots.isEmpty())
                        {
                            pastRating = -1;
                            reviewId = "";
                        } else
                        {
                            reviewId = queryDocumentSnapshots.getDocuments().get(0).getId();
                            pastRating = (long) queryDocumentSnapshots.getDocuments().get(0).get("reviewScore");
                            ratingReview.setRating(pastRating);
                        }
                    }
                });
    }
    
    /**
     * Sends the review and sets the boolean to force the update, still it may be faster than the calling of the server side function.
     * There is no actual way to implement a scalable update system since the listeners would constantly launch during a real deploy.
     */
    private void sendReview()
    {
        if (reviewId.isEmpty())
        {
            reviewsRef.document().set(new Review(currentRating, shop.getUid(), userUid));
        } else
            reviewsRef.document(reviewId).update("reviewScore", currentRating);
        
        pref.edit().putBoolean(getString(R.string.need_update_key), true).commit();
        this.finish();
    }
}
