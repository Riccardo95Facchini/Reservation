package facchini.riccardo.reservation.Customer_Package.Fragment_Customer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import facchini.riccardo.reservation.CurrentUserViewModel;
import facchini.riccardo.reservation.Customer_Package.Activity_Customer.Activity_Customer_SelectedShop;
import facchini.riccardo.reservation.Customer_Package.Adapter_Customer.Adapter_Customer_SearchCard;
import facchini.riccardo.reservation.OnItemClickListener;
import facchini.riccardo.reservation.R;
import facchini.riccardo.reservation.SearchResult;
import facchini.riccardo.reservation.SearchViewModel;
import facchini.riccardo.reservation.Shop_Package.Shop;

public class Fragment_Customer_Search extends Fragment implements OnItemClickListener
{
    
    private EditText searchText;
    private ImageButton searchButton;
    private RecyclerView foundShopsView;
    private ProgressBar progressBar;
    private SeekBar distanceSeekBar;
    private TextView distanceText;
    
    private CurrentUserViewModel currentUserViewModel;
    private SearchViewModel searchViewModel;
    
    private ArrayList<SearchResult> foundShops;
    private Adapter_Customer_SearchCard adapter;
    
    private SharedPreferences sharedPreferences;
    
    //Location
    private final static int AUTOCOMPLETE_REQUEST_CODE = 1;
    private Location myLocation;
    private int distance = 1;
    
    private double deltaLng, deltaLat;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        getActivity().setTitle(R.string.search);
        return inflater.inflate(R.layout.fragment_customer_search, container, false);
    }
    
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        currentUserViewModel = ViewModelProviders.of(getActivity()).get(CurrentUserViewModel.class);
        searchViewModel = ViewModelProviders.of(this).get(SearchViewModel.class);
        
        foundShopsView.setHasFixedSize(true);
        foundShopsView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        foundShops = new ArrayList<>();
        adapter = new Adapter_Customer_SearchCard(getContext(), foundShops);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        sharedPreferences = getContext().getSharedPreferences(getString(R.string.reservations_preferences), Context.MODE_PRIVATE);
        
        searchText = view.findViewById(R.id.searchText);
        searchButton = view.findViewById(R.id.searchButton);
        progressBar = view.findViewById(R.id.progressBar);
        foundShopsView = view.findViewById(R.id.foundShopsView);
        distanceSeekBar = view.findViewById(R.id.distanceSeekBar);
        distanceText = view.findViewById(R.id.distanceText);
        
        distanceText.setText(getString(R.string.searchDistance).concat(String.valueOf(distance)).concat("Km"));
        
        distanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                distanceText.setText(getString(R.string.searchDistance).concat(String.valueOf(progress)).concat("Km"));
                distance = progress;
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        progressBar.setVisibility(View.GONE);
        
        searchText.setOnKeyListener(new View.OnKeyListener()
        {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER))
                {
                    String text = searchText.getText().toString().trim();
                    
                    if (!text.isEmpty())
                    {
                        if (myLocation != null)
                            searchTag(text);
                        else
                            getLocation();
                    }
                }
                return false;
            }
        });
        
        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event)
            {
                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
                {
                    InputMethodManager inputManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    return true;
                }
                return false;
            }
        });
        
        searchButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String text = searchText.getText().toString().trim();
                InputMethodManager inputManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                
                if (!text.isEmpty())
                {
                    if (myLocation != null)
                        searchTag(text);
                    else
                        getLocation();
                }
            }
        });
        
        searchText.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (myLocation == null && hasFocus)
                    getLocation();
            }
        });
    }
    
    @Override
    public void onItemClick(int position)
    {
        Shop selected = foundShops.get(position).getShopFound();
        getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(null);
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putParcelable("Selected", selected);
        intent.putExtras(b);
        intent.putExtra("name", currentUserViewModel.getCurrentUser().getValue().getName());
        intent.putExtra("picUrl", currentUserViewModel.getCurrentUser().getValue().getProfilePicUrl());
        intent.setClass(getContext(), Activity_Customer_SelectedShop.class);
        startActivity(intent);
    }
    
    
    /**
     * Finds the latitude and longitude of the address inserted, if it's the same of the last search it recovers the values,
     * otherwise it computes them and stores them for the next time
     */
    private void getLocation()
    {
        String oldAddress = sharedPreferences.getString(getString(R.string.last_location_string_key), "");
        if (!Places.isInitialized())
            Places.initialize(getContext().getApplicationContext(), "AIzaSyBl5HIARSrlcJzKjiDc-YyT_CUmX3H5qBQ");
        
        List<Place.Field> fields = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG);
        Intent autocomplete = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).setInitialQuery(oldAddress).build(getContext());
        startActivityForResult(autocomplete, AUTOCOMPLETE_REQUEST_CODE);
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE && resultCode == AutocompleteActivity.RESULT_OK)
        {
            myLocation = new Location("myLocation");
            Place place = Autocomplete.getPlaceFromIntent(data);
            String address = place.getAddress();
            LatLng latLng = place.getLatLng();
            
            myLocation.setLatitude(latLng.latitude);
            myLocation.setLongitude(latLng.longitude);
            
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(getString(R.string.last_location_string_key), address).apply();
        }
    }
    
    /**
     * Searches if shops with the given tag exist and displays them to the user
     *
     * @param text Tag to search
     */
    private void searchTag(String text)
    {
        progressBar.setVisibility(View.VISIBLE);
        getDeltas();
        foundShops.clear();
        searchViewModel.reset();
        
        final Fragment_Customer_Search listener = this;
        
        Observer observer = new Observer<List<SearchResult>>()
        {
            @Override
            public void onChanged(List<SearchResult> searchResults)
            {
                foundShops.addAll(searchResults);
                if (foundShops.isEmpty())
                {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), getString(R.string.noShopsFound), Toast.LENGTH_SHORT).show();
                } else
                {
                    Collections.sort(foundShops, searchResultComparator);
                    foundShopsView.setAdapter(adapter);
                    adapter.setOnItemClickListener(listener);
                    progressBar.setVisibility(View.GONE);
                }
                searchViewModel.getSearchResults().removeObserver(this);
            }
        };
        
        searchViewModel.getSearchResults().observe(getActivity(), observer);
        searchViewModel.queryForTag(text, distance, deltaLat, deltaLng, myLocation);
    }
    
    /**
     * Defined comparator for reservations to order them
     */
    public Comparator<SearchResult> searchResultComparator = new Comparator<SearchResult>()
    {
        @Override
        public int compare(SearchResult o1, SearchResult o2)
        {
            return o1.compareTo(o2);
        }
    };
    
    /**
     * Sets deltas to find minimum/maximum values of latitude/longitude for the query
     */
    private void getDeltas()
    {
        int dist = 1000 * distance;
        final double earthRadius = 6371000; //meters
        
        deltaLat = (dist / earthRadius) * (180 / Math.PI);
        deltaLng = Math.toDegrees((dist / (earthRadius * Math.cos(Math.toRadians(myLocation.getLatitude())))));
    }
}
