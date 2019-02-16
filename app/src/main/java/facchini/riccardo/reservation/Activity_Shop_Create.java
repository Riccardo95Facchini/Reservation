package facchini.riccardo.reservation;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Locale;

public class Activity_Shop_Create extends AppCompatActivity
{
    private String uid = "";
    private String mail = "";
    private Address address;
    
    private boolean editing = false;
    private Shop currentShop = null;
    
    //UI
    //Buttons
    private Button continueButton;
    //Text
    private TextView textTop;
    private EditText shopNameText, address1Text, address2Text, cityText, zipText, phoneText, mailText;
    
    
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
            editing = true;
            uid = currentShop.getUid();
        } else
        {
            uid = intent.getStringExtra("uid");
            mail = intent.getStringExtra("mail");
        }
        
        setUI();
        handleTextsOnCreate();
        
        if (editing)
            continueButton.setEnabled(true);
    }
    
    /**
     * Sets the UI elements of this activity
     */
    private void setUI()
    {
        //Initialize UI elements
        textTop = findViewById(R.id.textTop);
        shopNameText = findViewById(R.id.shopNameText);
        address1Text = findViewById(R.id.address1Text);
        address2Text = findViewById(R.id.address2Text);
        cityText = findViewById(R.id.cityText);
        zipText = findViewById(R.id.zipText);
        phoneText = findViewById(R.id.phoneText);
        mailText = findViewById(R.id.mailText);
        
        if (!mail.isEmpty())
            mailText.setText(mail);
        
        if (editing)
        {
            textTop.setText(getString(R.string.changeFieldsEditShop));
            shopNameText.setText(currentShop.getName());
            address1Text.setText(currentShop.getAddress1());
            address2Text.setText(currentShop.getAddress2());
            cityText.setText(currentShop.getCity());
            zipText.setText(currentShop.getZip());
            phoneText.setText(currentShop.getPhone());
            mailText.setText(currentShop.getMail());
        }
        
        continueButton = findViewById(R.id.continueButton);
        continueButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (checkAddress())
                    continueToTags();
            }
        });
        
        continueButton.setEnabled(false);
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
     * Checks if the entire form has been filled
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
        Intent intent = new Intent(this, Activity_Shop_TagHours.class);
        
        String name = shopNameText.getText().toString().trim();
        String mail = mailText.getText().toString().trim();
        String address1 = address1Text.getText().toString().trim();
        String address2 = address2Text.getText().toString().trim();
        String city = cityText.getText().toString().trim();
        String zip = zipText.getText().toString().trim();
        String phone = phoneText.getText().toString().trim();
        double latitude = address.getLatitude();
        double longitude = address.getLongitude();
        
        if (!editing)
        {
            intent.putExtra("uid", uid)
                    .putExtra("name", name)
                    .putExtra("address1", address1)
                    .putExtra("address2", address2)
                    .putExtra("city", city)
                    .putExtra("zip", zip)
                    .putExtra("phone", phone)
                    .putExtra("mail", mail)
                    .putExtra("latitude", latitude)
                    .putExtra("longitude", longitude);
        } else
        {
            Bundle b = new Bundle();
            Shop shop = new Shop(uid, name, mail, address1, address2, city, zip, phone, latitude, longitude, currentShop.getTags(), currentShop.getHours());
            b.putParcelable("CurrentShop", shop);
            intent.putExtras(b);
            intent.setClass(this, Activity_Shop_TagHours.class);
        }
        startActivity(intent);
    }
    
    /**
     * Checks if the inserted address is valid, makes toast to warn the user to fix it if wrong
     *
     * @return true if valid, false otherwise
     */
    private boolean checkAddress()
    {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        String fullAddress = String.format("%s %s %s %s",
                address1Text.getText().toString().trim(),
                address2Text.getText().toString().trim(),
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
