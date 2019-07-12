package facchini.riccardo.reservation.Shop_Package.Activity_Shop;

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
import androidx.lifecycle.ViewModelProviders;

import com.firebase.ui.auth.AuthUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Stack;

import facchini.riccardo.reservation.Activity_Login;
import facchini.riccardo.reservation.Chat.Activity_Chat_Homepage;
import facchini.riccardo.reservation.CurrentUserViewModel;
import facchini.riccardo.reservation.R;
import facchini.riccardo.reservation.ReservationViewModel;
import facchini.riccardo.reservation.Shop_Package.Fragment_Shop.Fragment_Shop_History;
import facchini.riccardo.reservation.Shop_Package.Fragment_Shop.Fragment_Shop_Home;
import facchini.riccardo.reservation.Shop_Package.Fragment_Shop.Fragment_Shop_Profile;

public class Activity_Shop extends AppCompatActivity
{
    private FirebaseAuth.AuthStateListener authStateListener;
    
    private byte backButton;
    private int lastFragment;
    private static final int HOME = 0, HISTORY = 1, PROFILE = 2;
    private Stack<Integer> bottomStack;
    
    private BottomNavigationView bottomMenu;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        backButton = 0;
        bottomStack = new Stack<>();
        setContentView(R.layout.activity_shop);
        
        ViewModelProviders.of(this).get(ReservationViewModel.class).setTag(ReservationViewModel.SHOP);
        ViewModelProviders.of(this).get(CurrentUserViewModel.class).setTag(CurrentUserViewModel.SHOP);
        
        bottomMenu = findViewById(R.id.bottomMenu);
        bottomMenu.setOnNavigationItemSelectedListener(selectedListener);
        
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new Fragment_Shop_Home()).commit();
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
                    getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fragmentContainer, new Fragment_Shop_Home()).commit();
                    bottomStack.push(lastFragment);
                    lastFragment = HOME;
                    break;
                case R.id.bottomHistory:
                    if (lastFragment == HISTORY) return false;
                    getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fragmentContainer, new Fragment_Shop_History()).commit();
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
                    Toast.makeText(Activity_Shop.this, "Logging out", Toast.LENGTH_SHORT).show();
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
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_action_bar, menu);
        
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
            case R.id.chat_menu:
                Intent intent = new Intent(getBaseContext(), Activity_Chat_Homepage.class);
                startActivity(intent);
                return true;
            case R.id.profile_menu:
                if (lastFragment == PROFILE) return false;
                bottomStack.push(lastFragment);
                bottomMenu.getMenu().getItem(lastFragment).setChecked(false);
                getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fragmentContainer, new Fragment_Shop_Profile()).commit();
                lastFragment = PROFILE;
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
