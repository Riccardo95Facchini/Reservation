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

public class CreateShopActivity extends AppCompatActivity
{
    private String uid;
    private String mail;
    
    //Firestore
    private FirebaseFirestore db;
    private CollectionReference customers;
    
    //UI
    //Buttons
    private Button continueButton;
    //Text view
    private EditText shopNameText;
    private EditText address1Text;
    private EditText address2Text;
    private EditText cityText;
    private EditText zipText;
    private EditText phoneText;
    private EditText mailText;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_shop);
        //Get intent and extra
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        mail = intent.getStringExtra("mail");
        
        //Initialize UI elements
        continueButton = findViewById(R.id.continueButton);
        shopNameText = findViewById(R.id.shopNameText);
        address1Text = findViewById(R.id.address1Text);
        address2Text = findViewById(R.id.address2Text);
        cityText = findViewById(R.id.cityText);
        zipText = findViewById(R.id.zipText);
        phoneText = findViewById(R.id.phoneText);
        mailText = findViewById(R.id.mailText);
        
        
        continueButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                continueToTags();
            }
        });
        
        continueButton.setEnabled(false);
        
        handleTextsOnCreate();
    }
    
    /**
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
                    mailText.setText(mail);
            }
        });
        
        shopNameText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (s.toString().trim().length() == 0)
                    continueButton.setEnabled(false);
                else if (isFormFull())
                    continueButton.setEnabled(true);
            }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void afterTextChanged(Editable s) { }
        });
        
        address1Text.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (s.toString().trim().length() == 0)
                    continueButton.setEnabled(false);
                else if (isFormFull())
                    continueButton.setEnabled(true);
            }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void afterTextChanged(Editable s) { }
        });
        address2Text.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (s.toString().trim().length() == 0)
                    continueButton.setEnabled(false);
                else if (isFormFull())
                    continueButton.setEnabled(true);
            }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void afterTextChanged(Editable s) { }
        });
        cityText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (s.toString().trim().length() == 0)
                    continueButton.setEnabled(false);
                else if (isFormFull())
                    continueButton.setEnabled(true);
            }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void afterTextChanged(Editable s) { }
        });
        zipText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (s.toString().trim().length() == 0)
                    continueButton.setEnabled(false);
                else if (isFormFull())
                    continueButton.setEnabled(true);
            }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void afterTextChanged(Editable s) { }
        });
        phoneText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (s.toString().trim().length() == 0)
                    continueButton.setEnabled(false);
                else if (isFormFull())
                    continueButton.setEnabled(true);
            }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void afterTextChanged(Editable s) { }
        });
        mailText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (s.toString().trim().length() == 0)
                    continueButton.setEnabled(false);
                else if (isFormFull())
                    continueButton.setEnabled(true);
            }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void afterTextChanged(Editable s) { }
        });
    }
    
    /**
     * Checks if the entire for has been filled
     *
     * @return true if all fields have at least one char, false otherwise
     */
    private boolean isFormFull()
    {
        return shopNameText.getText().toString().length() > 0 &&
                address1Text.getText().toString().length() > 0 &&
                address2Text.getText().toString().length() > 0 &&
                cityText.getText().toString().length() > 0 &&
                zipText.getText().toString().length() > 0 &&
                phoneText.getText().toString().length() > 0 &&
                mailText.getText().toString().length() > 0;
    }
    
    private void continueToTags()
    {
        Intent intent = new Intent(this, ShopTagHoursActivity.class);
        intent.putExtra("uid", uid)
                .putExtra("name", shopNameText.getText().toString().trim())
                .putExtra("address1", address1Text.getText().toString().trim())
                .putExtra("address2", address2Text.getText().toString().trim())
                .putExtra("city", cityText.getText().toString().trim())
                .putExtra("zip", zipText.getText().toString().trim())
                .putExtra("phone", phoneText.getText().toString().trim())
                .putExtra("mail", mailText.getText().toString().trim());
        startActivity(intent);
    }
}
