package facchini.riccardo.reservation.Customer_Package.Activity_Customer;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import facchini.riccardo.reservation.Activity_Login;
import facchini.riccardo.reservation.Customer_Package.Customer;
import facchini.riccardo.reservation.R;

public class Activity_Customer_Create extends AppCompatActivity
{
    private String uid;
    private String mail;
    private String phone;
    
    //Firestore
    private FirebaseFirestore db;
    private CollectionReference customers;
    
    //UI
    //Buttons
    private Button sendButton;
    //Text view
    private EditText nameText;
    private EditText phoneText;
    private EditText mailText;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_create);
        
        //Get intent and extra
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        mail = intent.getStringExtra("mail");
        phone = intent.getStringExtra("phone");
        
        //Initialize UI elements
        sendButton = findViewById(R.id.sendButton);
        nameText = findViewById(R.id.nameText);
        phoneText = findViewById(R.id.phoneText);
        mailText = findViewById(R.id.mailText);
        
        sendButton.setEnabled(false);
        
        sendButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendData();
            }
        });
        
        handleTextsOnCreate();
    }
    
    /**
     * If mail or phone number already given, cache them and hide the EditText.
     * Adds listeners to EditText
     */
    private void handleTextsOnCreate()
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if (!mail.isEmpty())
                        mailText.setVisibility(View.GONE);
                } catch (Exception e)
                {
                    mail = "";
                }
                
                try
                {
                    if (!phone.isEmpty())
                        phoneText.setVisibility(View.GONE);
                } catch (Exception e)
                {
                    phone = "";
                }
                
            }
        });
        
        nameText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (s.toString().trim().length() == 0)
                    sendButton.setEnabled(false);
            }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void afterTextChanged(Editable s) { }
        });
        
    }
    
    private void sendData()
    {
        db = FirebaseFirestore.getInstance();
        customers = db.collection("customers");
        
        mail = mail.isEmpty() ? mailText.getText().toString() : mail;
        phone = phone.isEmpty() ? phoneText.getText().toString() : phone;
        Customer newCustomer = new Customer(uid, nameText.getText().toString(), phone, mail);
        customers.document(uid).set(newCustomer);
        int reservations = 0;
        db.collection("reservationsUpdate").document(uid).set(reservations);
        startActivity(new Intent(this, Activity_Login.class));
    }
}
