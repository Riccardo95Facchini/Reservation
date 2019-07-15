package facchini.riccardo.reservation.Shop_Package.Activity_Shop;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.firebase.storage.StorageTask;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import facchini.riccardo.reservation.ImageUploader;
import facchini.riccardo.reservation.R;
import facchini.riccardo.reservation.Shop_Package.Shop;

public class Activity_Shop_Create extends AppCompatActivity
{
    private String uid = "";
    private String mail = "";
    private String profilePicUrl;
    private Address address;
    
    private ImageUploader imageUploader;
    private StorageTask taskUpload;
    private ImageView profilePic;
    
    private boolean editing = false;
    private Shop currentShop = null;
    
    private EditText shopNameText, addressText, cityText, zipText, phoneText, mailText;
    
    private static final int FINISH = 0, IMAGE_REQUEST = 1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_create);
        
        //Get intent and extra
        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        currentShop = b.getParcelable("CurrentShop");
        
        if (currentShop != null)
        {
            setTitle(R.string.edit);
            profilePicUrl = currentShop.getProfilePicUrl();
            editing = true;
            uid = currentShop.getUid();
        } else
        {
            profilePicUrl = "";
            uid = intent.getStringExtra("uid");
            mail = intent.getStringExtra("mail");
        }
        
        setUI();
        handleTextsOnCreate();
    }
    
    /**
     * Sets the UI elements of this activity
     */
    private void setUI()
    {
        //Initialize UI elements
        //Text
        shopNameText = findViewById(R.id.nameText);
        addressText = findViewById(R.id.address1Text);
        cityText = findViewById(R.id.cityText);
        zipText = findViewById(R.id.zipText);
        phoneText = findViewById(R.id.phoneText);
        mailText = findViewById(R.id.mailText);
        profilePic = findViewById(R.id.profilePic);
        ProgressBar uploadBar = findViewById(R.id.uploadBar);
        Button continueButton = findViewById(R.id.continueButton);
        
        if (!mail.isEmpty())
            mailText.setText(mail);
        
        imageUploader = new ImageUploader(this, profilePic, uploadBar, uid, profilePicUrl, editing, false);
        
        if (editing)
        {
            Glide.with(this).load(profilePicUrl).placeholder(R.drawable.default_avatar).fitCenter().centerCrop().transform(new CircleCrop()).into(profilePic);
            shopNameText.setText(currentShop.getName());
            addressText.setText(currentShop.getAddress());
            cityText.setText(currentShop.getCity());
            zipText.setText(currentShop.getZip());
            phoneText.setText(currentShop.getPhone());
            mailText.setText(currentShop.getMail());
        } else
            imageUploader.uploadDefaultAvatar();
        
        profilePic.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openFilePicker();
            }
        });
        
        
        continueButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (checkImage() && checkAddress() && isFormFull())
                    continueToTags();
            }
        });
    }
    
    private void openFilePicker()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }
    
    /**
     * Adds listeners to EditText
     */
    private void handleTextsOnCreate()
    {
        if (!editing)
        {
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    if (!mail.isEmpty())
                        mailText.setText(mail);
                }
            });
        }
    }
    
    /**
     * Checks if the entire form has been filled
     *
     * @return true if all fields have at least one char, false otherwise
     */
    private boolean isFormFull()
    {
        List<EditText> texts = Arrays.asList(shopNameText, addressText, cityText, zipText, phoneText, mailText);
        boolean isFull = true;
        
        for (EditText text : texts)
        {
            if (text.getText().toString().length() <= 0)
            {
                text.setError(getString(R.string.error_empty));
                isFull = false;
            }
        }
        return isFull;
    }
    
    private void continueToTags()
    {
        Intent intent = new Intent(this, Activity_Shop_TagHours.class);
        
        String name = shopNameText.getText().toString().trim();
        String mail = mailText.getText().toString().trim();
        String address = addressText.getText().toString().trim();
        String city = cityText.getText().toString().trim();
        String zip = zipText.getText().toString().trim();
        String phone = phoneText.getText().toString().trim();
        double latitude = this.address.getLatitude();
        double longitude = this.address.getLongitude();
        
        if (!editing)
        {
            intent.putExtra("uid", uid)
                    .putExtra("name", name)
                    .putExtra("address", address)
                    .putExtra("city", city)
                    .putExtra("zip", zip)
                    .putExtra("phone", phone)
                    .putExtra("mail", mail)
                    .putExtra("latitude", latitude)
                    .putExtra("profilePicUrl", profilePicUrl)
                    .putExtra("longitude", longitude);
            startActivity(intent);
        } else
        {
            int intLongitude = (int) longitude;
            Bundle b = new Bundle();
            Shop shop = new Shop(uid, name, phone, mail, profilePicUrl, address, city, zip, latitude,
                    longitude, currentShop.getAverageReviews(), currentShop.getNumReviews(), intLongitude, currentShop.getTags(), currentShop.getHours());
            b.putParcelable("CurrentShop", shop);
            intent.putExtras(b);
            startActivityForResult(intent, FINISH);
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == FINISH && resultCode == Activity.RESULT_OK)
            finish();
        
        if (requestCode == IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null)
        {
            taskUpload = imageUploader.upload(data.getData());
        }
    }
    
    @Override
    public void onBackPressed()
    {
        if (checkImage())
        {
            if (editing)
                setResult(Activity.RESULT_OK, new Intent().putExtra("newPic", profilePicUrl));
            super.onBackPressed();
        }
    }
    
    /**
     * Check if an image has been uploaded or is being uploaded
     *
     * @return True if image uploaded or never selected (default avatar), false if it's still uploading
     */
    private boolean checkImage()
    {
        if (taskUpload != null)
        {
            if (taskUpload.isInProgress() || imageUploader.getProfilePicUrl().isEmpty())
            {
                Toast.makeText(this, getString(R.string.wait_for_image_upload), Toast.LENGTH_SHORT).show();
                return false;
            } else
            {
                profilePicUrl = imageUploader.getProfilePicUrl();
            }
        }
        
        return true;
    }
    
    /**
     * Checks if the inserted address is valid, makes toast to warn the user to fix it if wrong
     *
     * @return true if valid, false otherwise
     */
    private boolean checkAddress()
    {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        String fullAddress = String.format("%s %s %s",
                addressText.getText().toString().trim(),
                cityText.getText().toString().trim(),
                zipText.getText().toString().trim());
        
        address = null;
        
        try
        {
            address = geocoder.getFromLocationName(fullAddress, 1).get(0);
        } catch (IOException e)
        {
            Toast.makeText(this, getString(R.string.wrongAddress), Toast.LENGTH_SHORT).show();
            return false;
        } finally
        {
            if (address == null)
            {
                Toast.makeText(this, getString(R.string.wrongAddress), Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }
}
