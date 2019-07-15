package facchini.riccardo.reservation.Customer_Package.Activity_Customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import facchini.riccardo.reservation.Adapter_CardInfo;
import facchini.riccardo.reservation.Chat.Activity_Chat;
import facchini.riccardo.reservation.Dialog_Rating;
import facchini.riccardo.reservation.Info_Content;
import facchini.riccardo.reservation.R;
import facchini.riccardo.reservation.Review;
import facchini.riccardo.reservation.Shop_Package.Shop;

public class Activity_Customer_ShopInfo extends AppCompatActivity
{
    private CollectionReference reviewsRef;
    
    private RecyclerView recyclerView;
    
    private Shop shop;
    private String userUid;
    private String name;
    private String reviewId;
    private String picUrl;
    private long pastRating;
    
    private Adapter_CardInfo adapterCardInfo;
    private List<Info_Content> contents;
    
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_shop_profile);
        
        reviewsRef = FirebaseFirestore.getInstance().collection("reviews");
        userUid = FirebaseAuth.getInstance().getUid();
        
        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        if (b != null)
            shop = b.getParcelable("Selected");
        name = intent.getStringExtra("name");
        picUrl = intent.getStringExtra("picUrl");
        
        setTitle(shop.getName());
        checkReviewExists();
        
        Button buttonRate = findViewById(R.id.buttonRate);
        TextView textReviews = findViewById(R.id.textReviews);
        RatingBar ratingAvg = findViewById(R.id.ratingAvg);
        ImageView profilePic = findViewById(R.id.profilePic);
        ImageButton buttonAction = findViewById(R.id.buttonAction);
        recyclerView = findViewById(R.id.info);
        
        contents = new ArrayList<>();
        adapterCardInfo = new Adapter_CardInfo(this, contents);
        recyclerView.setAdapter(adapterCardInfo);
        
        buttonRate.setVisibility(View.VISIBLE);
        buttonAction.setImageResource(R.drawable.ic_chat_primary_color_32dp);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        contents.addAll(shop.createInfoContentList());
        
        Glide.with(this).load(shop.getProfilePicUrl()).placeholder(R.drawable.default_avatar).fitCenter().centerCrop().transform(new CircleCrop()).into(profilePic);
        textReviews.setText(String.format("(%.2f/5) %d %s", shop.getAverageReviews(), shop.getNumReviews(), getString(R.string.reviews)));
        ratingAvg.setRating((float) shop.getAverageReviews());
        
        buttonAction.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startChat();
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
        final Dialog_Rating ratingDialog = new Dialog_Rating(this, R.style.RatingDialogTheme, pastRating);
        
        ratingDialog.getRating().setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener()
        {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser)
            {
                sendReview(Math.round(rating), ratingDialog);
            }
        });
        ratingDialog.show();
    }
    
    private void startChat()
    {
        Intent chatIntent = new Intent(Activity_Customer_ShopInfo.this, Activity_Chat.class);
        chatIntent.putExtra("thisUsername", name);
        chatIntent.putExtra("otherUid", shop.getUid());
        chatIntent.putExtra("otherUsername", shop.getName());
        chatIntent.putExtra("thisPhoto", picUrl);
        chatIntent.putExtra("otherPhoto", shop.getProfilePicUrl());
        startActivity(chatIntent);
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
    private void sendReview(int rating, Dialog_Rating ratingDialog)
    {
        if (reviewId.isEmpty())
        {
            reviewsRef.document().set(new Review(rating, shop.getUid(), userUid));
        } else
            reviewsRef.document(reviewId).update("reviewScore", rating);
        
        Toast.makeText(this, getString(R.string.review_sent), Toast.LENGTH_SHORT).show();
        
        //pref.edit().putBoolean(getString(R.string.need_update_key), true).commit();
        ratingDialog.cancel();
        this.finish();
    }
}
