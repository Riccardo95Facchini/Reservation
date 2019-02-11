package facchini.riccardo.reservation;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Activity_Customer extends AppCompatActivity
{
    private static final String TAG = "ECCEZIONE";
    private FirebaseAuth.AuthStateListener authStateListener;
    
    private byte backButton;
    private int currentMenu = R.id.bottomHome;
    
    private BottomNavigationView bottomMenu;
    
    private boolean mLocationPermissionGranted = false;
    
    private final int PERMISSIONS_REQUEST_ENABLE_GPS = 9002;
    private final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9003;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        backButton = 0;
        setContentView(R.layout.activity_customer);
        
        bottomMenu = findViewById(R.id.bottomMenu);
        bottomMenu.setOnNavigationItemSelectedListener(selectedListener);
        
        getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.fragmentContainer, new Fragment_Customer_Home()).commit();
        setupFirebaseListener();
    }
    
    /**
     * Builds Alert to enable GPS
     */
    private void buildAlertMessageNoGps()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id)
                    {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
    
    public boolean isMapsEnabled()
    {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }
    
    private void getLocationPermission()
    {
        if (mLocationPermissionGranted)
            return;
        
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            mLocationPermissionGranted = true;
        else
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults)
    {
        mLocationPermissionGranted = false;
        switch (requestCode)
        {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    mLocationPermissionGranted = true;
            }
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case PERMISSIONS_REQUEST_ENABLE_GPS:
            {
                getLocationPermission();
            }
        }
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
                    break;
                case R.id.bottomSearch:
                    currentMenu = R.id.bottomSearch;
                    isMapsEnabled();
                    getLocationPermission();
                    selected = new Fragment_Customer_Search();
                    break;
                case R.id.bottomProfile:
                    currentMenu = R.id.bottomProfile;
                    selected = new Fragment_Customer_Profile();
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
        
        if (currentMenu == R.id.bottomSearch && isMapsEnabled())
        {
            getLocationPermission();
        }
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
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_logout, menu);
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
