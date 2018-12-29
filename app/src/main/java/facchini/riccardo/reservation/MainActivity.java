package facchini.riccardo.reservation;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity
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
    //Storage
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    
    //Strings
    private String uid;
    
    //Booleans
    private boolean isCustomer = false;
    private boolean isShop = false;
    
    //UI
    //Buttons
    private Button createCustomerButton;
    private Button createShopButton;
    //Progress bar
    private ProgressBar startupProgressBar;
    //Text view
    private TextView startupText;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        
        //Initialize Firebase Components
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();
        
        //Initialize Firebase references
        //databaseReference = firebaseDatabase.getReference().child("messages"); //Takes only the messages child
        //storageReference = firebaseStorage.getReference().child("chat_photos");
        customers = db.collection("customers");
        shops = db.collection("shops");
        
        //Initialize references to views
        //TODO
        
        //Initialize UI elements
        createCustomerButton = findViewById(R.id.createCustomerButton);
        createShopButton = findViewById(R.id.createShopButton);
        startupProgressBar = findViewById(R.id.startupProgressBar);
        startupText = findViewById(R.id.startupText);
        
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
        
        AddButtonsListeners();
    }
    
    /**
     * Adds listeners to the buttons in this page
     */
    private void AddButtonsListeners()
    {
        createShopButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Shop shop = TestShop();
                shops.document(uid).set(shop);
            }
        });
        
        createCustomerButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent createCustomerIntent = new Intent(MainActivity.this, CreateCustomerActivity.class);
                createCustomerIntent.putExtra("uid", uid);
                createCustomerIntent.putExtra("mail", user.getEmail());
                createCustomerIntent.putExtra("phone", user.getPhoneNumber());
                startActivity(createCustomerIntent);
                //customers.document(uid).set(new Customer(uid, "Giuseppe", "Rossi", "012345678", "P@libero.it"));
            }
        });
    }
    
    /**
     * Creates a test shop for now
     * TODO remove it
     *
     * @return
     */
    private Shop TestShop()
    {
        ArrayList<String> p = new ArrayList<>();
        p.add("Ciao");
        p.add("Pluto");
        return (new Shop(uid, "Prova", "Via pluto", "12", "Milano", "01234", p));
    }
    
    /**
     * Checks if the user exists as a customer, if it exists calls UserTypeDecision() if not calls for check as a shop
     */
    private void CheckCustomerExists()
    {
        customers.document(uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
        {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot)
            {
                if (documentSnapshot.exists())
                {
                    isCustomer = true;
                    UserTypeDecision();
                } else
                    CheckShopExists();
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    
    /**
     * Checks if the user exists as a shop and then always calls UserTypeDecision() when successful
     */
    private void CheckShopExists()
    {
        shops.document(uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
        {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot)
            {
                if (documentSnapshot.exists())
                    isShop = true;
                
                UserTypeDecision();
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    
    /**
     * Decides what to do bases on the user type that logged in
     */
    private void UserTypeDecision()
    {
        if (isCustomer)
        {
            //TODO open customer activity
            Toast.makeText(this, "CUSTOMER", Toast.LENGTH_SHORT).show();
        } else if (isShop)
        {
            //TODO open shop activity
            Toast.makeText(this, "SHOP", Toast.LENGTH_SHORT).show();
        } else
        {
            //Stay here and make the user create a either a shop or a customer account
            DisableProgressEnableButtons();
            Toast.makeText(this, "NOTHING", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Disables loading animation UI and enables buttons
     */
    private void DisableProgressEnableButtons()
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                createCustomerButton.setVisibility(View.VISIBLE);
                createShopButton.setVisibility(View.VISIBLE);
                startupProgressBar.setVisibility(View.GONE);
                startupText.setVisibility(View.GONE);
            }
        });
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
        
        CheckCustomerExists(); //When signed in check what type of user it is
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
        inflater.inflate(R.menu.main_menu, menu);
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
