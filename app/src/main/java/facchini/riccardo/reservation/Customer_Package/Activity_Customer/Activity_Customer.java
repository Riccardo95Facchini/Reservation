package facchini.riccardo.reservation.Customer_Package.Activity_Customer;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import facchini.riccardo.reservation.Activity_Login;
import facchini.riccardo.reservation.Customer_Package.Fragment_Customer.Fragment_Customer_Home;
import facchini.riccardo.reservation.Customer_Package.Fragment_Customer.Fragment_Customer_Profile;
import facchini.riccardo.reservation.Customer_Package.Fragment_Customer.Fragment_Customer_Search;
import facchini.riccardo.reservation.R;

public class Activity_Customer extends AppCompatActivity
{
    private FirebaseAuth.AuthStateListener authStateListener;
    
    private byte backButton;
    private int currentMenu = R.id.bottomHome;
    
    private BottomNavigationView bottomMenu;
    private Menu topMenu;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        backButton = 0;
        setContentView(R.layout.activity_customer);
        
        bottomMenu = findViewById(R.id.bottomMenu);
        bottomMenu.setOnNavigationItemSelectedListener(selectedListener);
        
        getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fragmentContainer, new Fragment_Customer_Home()).commit();
        currentMenu = R.id.bottomHome;
        setupFirebaseListener();
    }
    
    
    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener = new BottomNavigationView.OnNavigationItemSelectedListener()
    {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
        {
            Fragment selected = null;
            
            if (currentMenu == menuItem.getItemId())
                return false;
            
            switch (menuItem.getItemId())
            {
                case R.id.bottomHome:
                    currentMenu = R.id.bottomHome;
                    selected = new Fragment_Customer_Home();
                    topMenu.getItem(1).setVisible(true);
                    break;
                case R.id.bottomSearch:
                    currentMenu = R.id.bottomSearch;
                    selected = new Fragment_Customer_Search();
                    topMenu.getItem(1).setVisible(false);
                    break;
                case R.id.bottomProfile:
                    currentMenu = R.id.bottomProfile;
                    selected = new Fragment_Customer_Profile();
                    topMenu.getItem(1).setVisible(false);
                    break;
            }
            
            getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fragmentContainer, selected).commit();
            return true;
        }
    };
    
    @Override
    public void onBackPressed()
    {
        if (backButton > 0)
            finish();
        else
        {
            Toast.makeText(this, getString(R.string.pressBackToExit), Toast.LENGTH_LONG).show();
            backButton++;
            
            new CountDownTimer(2000, 1000)
            {
                @Override
                public void onTick(long millisUntilFinished) {}
                
                @Override
                public void onFinish()
                {
                    backButton = 0;
                }
            }.start();
        }
    }
    
    @Override
    protected void onPause()
    {
        super.onPause();
        if (authStateListener != null)
        {
            //On pause removes the listener for the authentication
            FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
        }
    }
    
    @Override
    protected void onResume()
    {
        super.onResume();
        //On resume adds again the listener for the authentication
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
    }
    
    private void setupFirebaseListener()
    {
        authStateListener = new FirebaseAuth.AuthStateListener()
        {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
            {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                
                if (user == null)
                {
                    Toast.makeText(Activity_Customer.this, "Logging out", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getBaseContext(), Activity_Login.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        };
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
        MenuInflater menuInflater = getMenuInflater();
        topMenu = menu;
        
        menuInflater.inflate(R.menu.menu_action_bar, menu);
        
        if (currentMenu == R.id.bottomHome)
            topMenu.getItem(1).setVisible(true);
        else
            topMenu.getItem(1).setVisible(false);
        
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
            case R.id.refresh_menu:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new Fragment_Customer_Home()).commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}