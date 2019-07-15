package facchini.riccardo.reservation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Arrays;

import facchini.riccardo.reservation.Customer_Package.Activity_Customer.Activity_Customer;
import facchini.riccardo.reservation.Customer_Package.Activity_Customer.Activity_Customer_Create;
import facchini.riccardo.reservation.Shop_Package.Activity_Shop.Activity_Shop;
import facchini.riccardo.reservation.Shop_Package.Activity_Shop.Activity_Shop_Create;

public class Activity_Login extends AppCompatActivity
{
    //Constants
    public static final int RC_SIGN_IN = 1;
    
    //Firebase instance variables
    //Authentication
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;
    //Firestore
    private FirebaseFirestore db;
    private CollectionReference customers;
    private CollectionReference shops;
    
    //Strings
    private String uid;
    
    //Booleans
    private boolean isCustomer = false;
    private boolean isShop = false;
    
    SharedPreferences sharedPref;
    
    //UI
    //Buttons
    private ImageButton createCustomerButton;
    private ImageButton createShopButton;
    //Progress bar
    private ProgressBar startupProgressBar;
    //Text view
    private TextView startupText, selectTypeText;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        setTitle(R.string.loading);
        
        sharedPref = getSharedPreferences(getString(R.string.reservations_preferences), Context.MODE_PRIVATE);
        
        firebaseAuth = FirebaseAuth.getInstance();
        
        uid = firebaseAuth.getUid();
        
        if (uid != null && !uid.isEmpty())
        {
            isCustomer = sharedPref.getBoolean(getString(R.string.isCustomer_key), false);
            isShop = sharedPref.getBoolean(getString(R.string.isShop_key), false);
            
            if (isCustomer || isShop)
                userTypeDecision(sharedPref.getString(getString(R.string.current_user_username_key), ""), true);
        }
        
        //firebaseStorage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();
        
        //Initialize Firebase references
        customers = db.collection("customers");
        shops = db.collection("shops");
        
        //Initialize UI elements
        createCustomerButton = findViewById(R.id.createCustomerButton);
        createShopButton = findViewById(R.id.createShopButton);
        startupProgressBar = findViewById(R.id.startupProgressBar);
        startupText = findViewById(R.id.startupText);
        selectTypeText = findViewById(R.id.selectTypeText);
        
        //Firebase Authentication
        authStateListener = new FirebaseAuth.AuthStateListener()
        {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
            {
                user = firebaseAuth.getCurrentUser();
                if (user != null)
                {
                    onSignedInInitialize(user.getDisplayName(), user.getUid());
                } else
                {
                    onSignedOutCleanup();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.GoogleBuilder().build(),
                                            new AuthUI.IdpConfig.EmailBuilder().build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
        
        addButtonsListeners();
    }
    
    /**
     * Adds listeners to the buttons in this page
     */
    private void addButtonsListeners()
    {
        createShopButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent createShopIntent = new Intent(Activity_Login.this, Activity_Shop_Create.class);
                createShopIntent.putExtra("uid", uid);
                createShopIntent.putExtra("mail", user.getEmail());
                startActivity(createShopIntent);
            }
        });
        
        createCustomerButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent createCustomerIntent = new Intent(Activity_Login.this, Activity_Customer_Create.class);
                createCustomerIntent.putExtra("uid", uid);
                createCustomerIntent.putExtra("mail", user.getEmail());
                createCustomerIntent.putExtra("phone", user.getPhoneNumber());
                startActivity(createCustomerIntent);
            }
        });
    }
    
    /**
     * Checks if the user exists as a customer, if it exists calls userTypeDecision() if not calls for check as a shop
     */
    private void checkCustomerExists()
    {
        customers.document(uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
        {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot)
            {
                String currentName = "";
                if (documentSnapshot.exists())
                {
                    isCustomer = true;
                    currentName = documentSnapshot.get("name") + " " + documentSnapshot.get("surname");
                    userTypeDecision(currentName, false);
                } else
                    checkShopExists();
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Toast.makeText(Activity_Login.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    
    /**
     * Checks if the user exists as a shop and then always calls userTypeDecision() when successful
     */
    private void checkShopExists()
    {
        shops.document(uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
        {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot)
            {
                String currentName = "";
                if (documentSnapshot.exists())
                {
                    isShop = true;
                    currentName = (String) documentSnapshot.get("name");
                }
                
                userTypeDecision(currentName, false);
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Toast.makeText(Activity_Login.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    
    /**
     * Decides what to do bases on the user type that logged in, if data already present skips the connection to the server to login.
     * Sets uid and username in shared preferences for later use.
     *
     * @param currentName Username of the user currently logged in
     * @param skipped     True if data already stored locally, used to skip server request.
     */
    private void userTypeDecision(String currentName, boolean skipped)
    {
        SharedPreferences.Editor edit = sharedPref.edit();
        if (!skipped && !currentName.isEmpty())
        {
            edit.putString(getString(R.string.current_user_username_key), currentName);
            edit.apply();
        }
        
        if (isCustomer)
        {
            FirebaseMessaging.getInstance().subscribeToTopic(uid);
            startActivity(new Intent(this, Activity_Customer.class));
            edit.putBoolean(getString(R.string.isCustomer_key), true).apply();
            edit.putBoolean(getString(R.string.isShop_key), false).apply();
            if (!skipped)
                firebaseAuth.removeAuthStateListener(authStateListener);
            finish();
        } else if (isShop)
        {
            FirebaseMessaging.getInstance().subscribeToTopic(uid);
            startActivity(new Intent(this, Activity_Shop.class));
            edit.putBoolean(getString(R.string.isCustomer_key), false).apply();
            edit.putBoolean(getString(R.string.isShop_key), true).apply();
            if (!skipped)
                firebaseAuth.removeAuthStateListener(authStateListener);
            finish();
        } else
        {
            //Stay here and make the user create a either a shop or a customer account
            disableProgressEnableButtons();
            edit.putBoolean(getString(R.string.isCustomer_key), false).apply();
            edit.putBoolean(getString(R.string.isShop_key), false).apply();
            setTitle(R.string.registrations);
        }
    }
    
    /**
     * Disables loading animation UI and enables buttons
     */
    private void disableProgressEnableButtons()
    {
        createCustomerButton.setVisibility(View.VISIBLE);
        createShopButton.setVisibility(View.VISIBLE);
        selectTypeText.setVisibility(View.VISIBLE);
        startupProgressBar.setVisibility(View.GONE);
        startupText.setVisibility(View.GONE);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        //Results of sign in
        if (requestCode == RC_SIGN_IN)
        {
            if (resultCode == RESULT_OK)
                Toast.makeText(this, "Signed in", Toast.LENGTH_SHORT).show();
            else if (resultCode == RESULT_CANCELED)
            {
                Toast.makeText(this, "Sign in canceled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    
    /**
     * Steps to do when signing out
     */
    private void onSignedOutCleanup()
    {
        try
        {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(uid);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        detachDatabaseReadListener();
        isShop = false;
        isCustomer = false;
    }
    
    /**
     * Initializes after successful signing in
     *
     * @param displayName Username of the currently logged in user
     * @param uid         UID of the currently logged in user
     */
    private void onSignedInInitialize(String displayName, String uid)
    {
        this.uid = uid;
        attachDatabaseReadListener();
        
        checkCustomerExists(); //When signed in check what type of user it is
    }
    
    /**
     * Attaches the database read listener
     */
    private void attachDatabaseReadListener()
    {
    }
    
    /**
     * Detaches the database read listener
     */
    private void detachDatabaseReadListener()
    {
    }
    
    @Override
    protected void onPause()
    {
        super.onPause();
        if (authStateListener != null)
        {
            //On pause removes the listener for the authentication
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
        detachDatabaseReadListener();
    }
    
    @Override
    protected void onResume()
    {
        super.onResume();
        //On resume adds again the listener for the authentication
        firebaseAuth.addAuthStateListener(authStateListener);
    }
    
    /**
     * Shows the menu (3 dots) when touched
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_action_bar, menu);
        return true;
    }
    
    /**
     * Action to perform when an option in the menu is selected
     *
     * @param item The selected option
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.sign_out_menu:
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
