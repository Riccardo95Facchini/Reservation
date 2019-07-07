package facchini.riccardo.reservation.Shop_Package.Activity_Shop;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import facchini.riccardo.reservation.R;
import facchini.riccardo.reservation.Shop_Package.Shop;

public class Activity_Shop_Profile extends AppCompatActivity
{
    private Button buttonEdit;
    
    private TextView textShopName;
    private TextView textHours;
    private TextView textReviews;
    private TextView textPhoneMail;
    private TextView textAddress;
    //private ImageView shopPic;  TODO: profile pic
    private RatingBar ratingAvg;
    
    Context context;
    private Shop shop;
    private String shopUid;
    
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_profile);
        
        shopUid = FirebaseAuth.getInstance().getUid();
        
        FirebaseFirestore.getInstance().collection("shops").document(shopUid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
        {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot)
            {
                if (documentSnapshot.exists())
                {
                    shop = new Shop(documentSnapshot.getData());
                    fillProfile();
                }
            }
        });
        
        textShopName = findViewById(R.id.textShopName);
        textHours = findViewById(R.id.textHours);
        textReviews = findViewById(R.id.textReviews);
        textPhoneMail = findViewById(R.id.textPhoneMail);
        textAddress = findViewById(R.id.textAddress);
        buttonEdit = findViewById(R.id.buttonEdit);
        //ImageView shopPic = findViewById(R.id.shopPic);  TODO: profile pic
        ratingAvg = findViewById(R.id.ratingAvg);
        
        context = this;
        buttonEdit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent();
                Bundle b = new Bundle();
                b.putParcelable("CurrentShop", shop);
                intent.putExtras(b);
                intent.setClass(context, Activity_Shop_Create.class);
                startActivityForResult(intent, 0);
            }
        });
    }
    
    private void fillProfile()
    {
        textShopName.setText(shop.getName());
        textAddress.setText(String.format("%s %s %s", shop.getAddress(),
                shop.getCity(), shop.getZip()));
        textReviews.setText(String.format("(%.2f/5) %d %s", shop.getAverageReviews(), shop.getNumReviews(), getString(R.string.reviews)));
        textPhoneMail.setText(String.format("Phone: %s\nMail: %s", shop.getPhone(), shop.getMail()));
        textHours.setText(shop.displayHoursFormat());
        //shopPic.setImageResource();
        ratingAvg.setRating((float) shop.getAverageReviews());
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 0 && resultCode == Activity.RESULT_OK)
        {
            recreate();
        }
    }
}
