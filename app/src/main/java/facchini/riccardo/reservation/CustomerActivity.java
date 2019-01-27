package facchini.riccardo.reservation;

import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

public class CustomerActivity extends AppCompatActivity
{
    
    private BottomNavigationView bottomMenu;
    
    private byte backButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        backButton = 0;
        setContentView(R.layout.activity_customer);
        
        bottomMenu = findViewById(R.id.bottomMenu);
        bottomMenu.setOnNavigationItemSelectedListener(selectedListener);
        
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new CustomerHomeFragment()).commit();
    }
    
    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener = new BottomNavigationView.OnNavigationItemSelectedListener()
    {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
        {
            Fragment selected = null;
            
            switch (menuItem.getItemId())
            {
                case R.id.bottomHome:
                    selected = new CustomerHomeFragment();
                    break;
                case R.id.bottomSearch:
                    selected = new CustomerSearchFragment();
                    break;
                case R.id.bottomProfile:
                    selected = new CustomerProfileFragment();
                    break;
            }
            
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, selected).commit();
            return true;
        }
    };
    
    @Override
    public void onBackPressed()
    {
        if (getSupportFragmentManager().findFragmentByTag("ShopSelected") != null)
            getSupportFragmentManager().popBackStackImmediate();
        else if (backButton > 0)
            finish();
        else
        {
            Toast.makeText(this, getString(R.string.pressBackToExit), Toast.LENGTH_LONG).show();
            backButton++;
            
            new CountDownTimer(2000, 1000)
            {
                @Override
                public void onTick(long millisUntilFinished)
                {
                
                }
                
                @Override
                public void onFinish()
                {
                    backButton = 0;
                }
            }.start();
        }
    }
}
