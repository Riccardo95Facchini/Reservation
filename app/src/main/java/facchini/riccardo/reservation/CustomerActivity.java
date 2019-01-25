package facchini.riccardo.reservation;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

public class CustomerActivity extends AppCompatActivity
{
    
    BottomNavigationView bottomMenu;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
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
}
