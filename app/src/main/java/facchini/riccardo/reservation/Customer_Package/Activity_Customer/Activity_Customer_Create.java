package facchini.riccardo.reservation.Customer_Package.Activity_Customer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageTask;

import facchini.riccardo.reservation.Activity_Login;
import facchini.riccardo.reservation.Customer_Package.Customer;
import facchini.riccardo.reservation.ImageUploader;
import facchini.riccardo.reservation.InitReservationUpdate;
import facchini.riccardo.reservation.R;

public class Activity_Customer_Create extends AppCompatActivity
{
    private String uid;
    private String name;
    private String mail;
    private String phone;
    private String profilePicUrl;
    private ImageUploader imageUploader;
    private boolean editing;
    
    //Firestore
    private FirebaseFirestore db;
    private CollectionReference customers;
    private StorageTask taskUpload;
    
    private ImageView profilePic;
    //Text view
    private EditText nameText;
    private EditText phoneText;
    private EditText mailText;
    
    private static final int IMAGE_REQUEST = 1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_create);
        
        //Get intent and extra
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        name = intent.getStringExtra("name");
        mail = intent.getStringExtra("mail");
        phone = intent.getStringExtra("phone");
        profilePicUrl = intent.getStringExtra("profilePicUrl");
        editing = intent.getBooleanExtra("editing", false);
        
        //Initialize UI elements
        //Buttons
        Button sendButton = findViewById(R.id.sendButton);
        nameText = findViewById(R.id.nameText);
        phoneText = findViewById(R.id.phoneText);
        mailText = findViewById(R.id.mailText);
        profilePic = findViewById(R.id.profilePic);
        ProgressBar uploadBar = findViewById(R.id.uploadBar);
        
        db = FirebaseFirestore.getInstance();
        customers = db.collection("customers");
        
        imageUploader = new ImageUploader(this, profilePic, uploadBar, uid, profilePicUrl, editing, true);
        
        if (editing)
            fillFields();
        else
            imageUploader.uploadDefaultAvatar();
        
        profilePic.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openFilePicker();
            }
        });
        
        sendButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                
                if (checkImage() && checkText())
                    sendData();
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
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null)
        {
            taskUpload = imageUploader.upload(data.getData());
        }
    }
    
    private void fillFields()
    {
        Glide.with(this).load(profilePicUrl).placeholder(R.drawable.default_avatar).fitCenter().centerCrop().transform(new CircleCrop()).into(profilePic);
        nameText.setText(name);
        phoneText.setText(phone);
        mailText.setText(mail);
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
                profilePicUrl = imageUploader.getProfilePicUrl();
        }
        
        return true;
    }
    
    /**
     * Checks if the mandatory fields contain text and if the mail is correct, sets errors when false
     *
     * @return True if all mandatory fields contain text adn email is correct, false otherwise
     */
    private boolean checkText()
    {
        boolean isCheckOk = true;
        
        if (nameText.getText().toString().trim().isEmpty())
        {
            nameText.setError(getString(R.string.error_empty));
            isCheckOk = false;
        }
        
        mail = mailText.getText().toString().trim();
        
        if (mail.isEmpty())
        {
            mailText.setError(getString(R.string.error_empty));
            isCheckOk = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(mail).matches())
        {
            mailText.setError(getString(R.string.enter_valid_mail_address));
            isCheckOk = false;
        }
        
        return isCheckOk;
    }
    
    
    private void sendData()
    {
        if (!editing)
            db.collection("reservationsUpdate").document(uid).set(new InitReservationUpdate());
        phone = phone == null || phone.isEmpty() ? phoneText.getText().toString() : phone;
        Customer newCustomer = new Customer(uid, nameText.getText().toString(), phone, mail, profilePicUrl);
        customers.document(uid).set(newCustomer);
        startActivity(new Intent(this, Activity_Login.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}
