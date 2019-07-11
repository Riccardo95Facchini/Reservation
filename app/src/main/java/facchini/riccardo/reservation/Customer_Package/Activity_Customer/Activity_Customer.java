package facchini.riccardo.reservation.Customer_Package.Activity_Customer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.firebase.ui.auth.AuthUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Stack;

import facchini.riccardo.reservation.Activity_Login;
import facchini.riccardo.reservation.Chat.Activity_Chat_Homepage;
import facchini.riccardo.reservation.CurrentUserViewModel;
import facchini.riccardo.reservation.Customer_Package.Fragment_Customer.Fragment_Customer_History;
import facchini.riccardo.reservation.Customer_Package.Fragment_Customer.Fragment_Customer_Home;
import facchini.riccardo.reservation.Customer_Package.Fragment_Customer.Fragment_Customer_Profile;
import facchini.riccardo.reservation.Customer_Package.Fragment_Customer.Fragment_Customer_Search;
import facchini.riccardo.reservation.R;
import facchini.riccardo.reservation.ReservationViewModel;

public class Activity_Customer extends AppCompatActivity
{
    private FirebaseAuth.AuthStateListener authStateListener;
    
    private byte backButton;
    private int lastFragment;
    private static final int HOME = 0, SEARCH = 1, HISTORY = 2, PROFILE = 3;
    private static final String TAG_HOME = "HOME", TAG_SEARCH = "SEARCH", TAG_HISTORY = "HISTORY", TAG_PROFILE = "PROFILE";
    private Stack<Integer> bottomStack;
    private BottomNavigationView bottomMenu;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        backButton = 0;
        bottomStack = new Stack<>();
        setContentView(R.layout.activity_customer);
        
        bottomMenu = findViewById(R.id.bottomMenu);
        bottomMenu.setOnNavigationItemSelectedListener(selectedListener);
        
        ViewModelProviders.of(this).get(ReservationViewModel.class).setTag(ReservationViewModel.CUSTOMER);
        ViewModelProviders.of(this).get(CurrentUserViewModel.class).setTag(CurrentUserViewModel.CUSTOMER);
        
        Fragment home = new Fragment_Customer_Home();
        home.setHasOptionsMenu(true);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, home).commit();
        lastFragment = HOME;
        bottomStack.push(lastFragment);
        setupFirebaseListener();
    }
    
    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener = new BottomNavigationView.OnNavigationItemSelectedListener()
    {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
        {
            menuItem.setCheckable(true);
            
            switch (menuItem.getItemId())
            {
                case R.id.bottomHome:
                    if (lastFragment == HOME) return false;
                    getSupportFragmentManager().beginTransaction().addToBackStack(TAG_HOME).replace(R.id.fragmentContainer, new Fragment_Customer_Home()).commit();
                    bottomStack.push(lastFragment);
                    lastFragment = HOME;
                    break;
                case R.id.bottomSearch:
                    if (lastFragment == SEARCH) return false;
                    getSupportFragmentManager().beginTransaction().addToBackStack(TAG_SEARCH).replace(R.id.fragmentContainer, new Fragment_Customer_Search()).commit();
                    bottomStack.push(lastFragment);
                    lastFragment = SEARCH;
                    break;
                case R.id.bottomHistory:
                    if (lastFragment == HISTORY) return false;
                    getSupportFragmentManager().beginTransaction().addToBackStack(TAG_HISTORY).replace(R.id.fragmentContainer, new Fragment_Customer_History()).commit();
                    bottomStack.push(lastFragment);
                    lastFragment = HISTORY;
                    break;
            }
            
            return true;
        }
    };
    
    @Override
    public void onBackPressed()
    {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0)
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
        } else
        {
            super.onBackPressed();
            
            if (lastFragment == PROFILE)
                bottomMenu.getMenu().getItem(bottomStack.peek()).setCheckable(true);
            
            lastFragment = bottomStack.pop();
            
            if (lastFragment != PROFILE)
                bottomMenu.getMenu().getItem(lastFragment).setChecked(true);
        }
    }
    
    //region Firebase
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
    //endregion Firebase
    
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
        menuInflater.inflate(R.menu.menu_action_bar, menu);
        
        menu.getItem(1).setVisible(true);
        menu.getItem(2).setVisible(true);
        
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
                SharedPreferences.Editor edit = getSharedPreferences(getString(R.string.reservations_preferences), Context.MODE_PRIVATE).edit();
                edit.remove(getString(R.string.isCustomer_key));
                edit.remove(getString(R.string.current_user_username_key)).apply();
                AuthUI.getInstance().signOut(this);
                return true;
            case R.id.profile_menu:
                if (lastFragment == PROFILE) return false;
                bottomStack.push(lastFragment);
                bottomMenu.getMenu().getItem(lastFragment).setCheckable(false);
                getSupportFragmentManager().beginTransaction().addToBackStack(TAG_PROFILE).replace(R.id.fragmentContainer, new Fragment_Customer_Profile()).commit();
                lastFragment = PROFILE;
                return true;
            case R.id.chat_menu:
                startActivity(new Intent(getBaseContext(), Activity_Chat_Homepage.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
