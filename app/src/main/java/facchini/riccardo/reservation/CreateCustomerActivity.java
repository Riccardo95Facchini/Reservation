package facchini.riccardo.reservation;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class CreateCustomerActivity extends AppCompatActivity
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
    private EditText firstNameText;
    private EditText surnameText;
    private EditText phoneText;
    private EditText mailText;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_customer);
        
        //Get intent and extra
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        mail = intent.getStringExtra("mail");
        phone = intent.getStringExtra("phone");
        
        //Initialize UI elements
        sendButton = findViewById(R.id.sendButton);
        firstNameText = findViewById(R.id.shopNameText);
        surnameText = findViewById(R.id.address1Text);
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
                if (!mail.isEmpty())
                    mailText.setVisibility(View.GONE);
                if (!phone.isEmpty())
                    phoneText.setVisibility(View.GONE);
            }
        });
        
        firstNameText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (s.toString().trim().length() == 0)
                    sendButton.setEnabled(false);
                else if (surnameText.getText().toString().length() > 0)
                    sendButton.setEnabled(true);
            }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void afterTextChanged(Editable s) { }
        });
        
        surnameText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (s.toString().trim().length() == 0)
                    sendButton.setEnabled(false);
                else if (firstNameText.getText().toString().length() > 0)
                    sendButton.setEnabled(true);
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
        Customer newCustomer = new Customer(uid, firstNameText.getText().toString(), surnameText.getText().toString(), phone, mail);
        customers.document(uid).set(newCustomer);
    
        startActivity(new Intent(this, MainActivity.class));
    }
}
