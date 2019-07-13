package facchini.riccardo.reservation.Customer_Package.Fragment_Customer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import facchini.riccardo.reservation.CurrentUserViewModel;
import facchini.riccardo.reservation.Customer_Package.Activity_Customer.Activity_Customer_SelectedShop;
import facchini.riccardo.reservation.Customer_Package.Adapter_Customer.Adapter_Customer_SearchCard;
import facchini.riccardo.reservation.OnItemClickListener;
import facchini.riccardo.reservation.R;
import facchini.riccardo.reservation.SearchResult;
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
    
    private ArrayList<SearchResult> foundShops = new ArrayList<>();
    private Adapter_Customer_SearchCard adapter;
    
    private SharedPreferences sharedPreferences;
    
    //Location
    private Location myLocation;
    private int distance = 1;
    
    private double deltaLng, deltaLat;
    
    //Firestore
    FirebaseFirestore db;
    CollectionReference shopsCollection;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        getActivity().setTitle(R.string.search);
        
        return inflater.inflate(R.layout.fragment_customer_search, container, false);
    }
    
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        currentUserViewModel = ViewModelProviders.of(getActivity()).get(CurrentUserViewModel.class);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        sharedPreferences = getContext().getSharedPreferences(getString(R.string.reservations_preferences), Context.MODE_PRIVATE);
        
        searchText = view.findViewById(R.id.searchText);
        searchButton = view.findViewById(R.id.searchButton);
        
        foundShopsView = view.findViewById(R.id.foundShopsView);
        foundShopsView.setHasFixedSize(true);
        foundShopsView.setLayoutManager(new LinearLayoutManager(getContext()));
        
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
        
        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        
        db = FirebaseFirestore.getInstance();
        shopsCollection = db.collection("shops");
        
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
        
        searchButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
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
        final String lastLocationString = sharedPreferences.getString(getString(R.string.last_location_string_key), "");
        
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(lastLocationString);
        final Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        
        new AlertDialog.Builder(getContext()).setCancelable(false).setView(input)
                .setTitle(getString(R.string.addressSearch))
                .setMessage(getString(R.string.addressSearchInput))
                .setPositiveButton("Set", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        myLocation = new Location("myLocation");
                        if (!input.getText().toString().isEmpty() && input.getText().toString().equals(lastLocationString))
                        {
                            try
                            {
                                myLocation.setLatitude(sharedPreferences.getFloat(getString(R.string.last_latitude_key), 0f));
                                myLocation.setLongitude(sharedPreferences.getFloat(getString(R.string.last_longitude_key), 0f));
                            } catch (Exception e)
                            {
                                Toast.makeText(getContext(), "Error, address could not be found", Toast.LENGTH_SHORT).show();
                                myLocation = null;
                                searchText.clearFocus();
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(getString(R.string.last_location_string_key), "");
                            }
                        } else
                        {
                            try
                            {
                                Address selectedAddress = geocoder.getFromLocationName(input.getText().toString(), 1).get(0);
                                myLocation.setLatitude(selectedAddress.getLatitude());
                                myLocation.setLongitude(selectedAddress.getLongitude());
                                
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(getString(R.string.last_location_string_key), input.getText().toString());
                                editor.putFloat(getString(R.string.last_latitude_key), (float) selectedAddress.getLatitude());
                                editor.putFloat(getString(R.string.last_longitude_key), (float) selectedAddress.getLongitude());
                                editor.apply();
                                
                            } catch (Exception e)
                            {
                                Toast.makeText(getContext(), "Error, address could not be found", Toast.LENGTH_SHORT).show();
                                myLocation = null;
                                searchText.clearFocus();
                            }
                        }
                    }
                }).setNegativeButton("Back", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                searchText.clearFocus();
                myLocation = null;
            }
        }).show();
    }
    
    /**
     * Searches if shops with the given tag exist and displays them to the user
     *
     * @param text Tag to search
     */
    private void searchTag(String text)
    {
        text = text.replaceAll("[^a-zA-Z\\s]", " ")
                .replaceAll("\\s+", " ").toLowerCase().trim();
        
        progressBar.setVisibility(View.VISIBLE);
        foundShops.clear();
        
        getDeltas();
        
        if (myLocation != null)
        {
            final double minLat = myLocation.getLatitude() - deltaLat,
                    maxLat = myLocation.getLatitude() + deltaLat,
                    minLng = myLocation.getLongitude() - deltaLng,
                    maxLng = myLocation.getLongitude() + deltaLng;
            
            int minIntLng = (int) minLng;
            int maxIntLng = (int) maxLng;
            
            Task queryOne, queryTwo, queryThree;
            Task<List<QuerySnapshot>> allTasks;
            if (minIntLng != maxIntLng)
            {
                queryOne = shopsCollection.whereArrayContains("tags", text)
                        .whereGreaterThanOrEqualTo("latitude", minLat)
                        .whereLessThanOrEqualTo("latitude", maxLat).whereEqualTo("intLongitude", minIntLng)
                        .get();
                
                queryTwo = shopsCollection.whereArrayContains("tags", text)
                        .whereGreaterThanOrEqualTo("latitude", minLat)
                        .whereLessThanOrEqualTo("latitude", maxLat).whereEqualTo("intLongitude", maxIntLng)
                        .get();
                if (minIntLng + 1 < maxIntLng)
                {
                    queryThree = shopsCollection.whereArrayContains("tags", text)
                            .whereGreaterThanOrEqualTo("latitude", minLat)
                            .whereLessThanOrEqualTo("latitude", maxLat).whereEqualTo("intLongitude", minIntLng + 1)
                            .get();
                    allTasks = Tasks.whenAllSuccess(queryOne, queryTwo, queryThree);
                } else
                    allTasks = Tasks.whenAllSuccess(queryOne, queryTwo);
                
            } else
            {
                queryOne = shopsCollection.whereArrayContains("tags", text)
                        .whereGreaterThanOrEqualTo("latitude", minLat)
                        .whereLessThanOrEqualTo("latitude", maxLat).whereEqualTo("intLongitude", minIntLng)
                        .get();
                
                allTasks = Tasks.whenAllSuccess(queryOne);
            }
            
            allTasks.addOnSuccessListener(new OnSuccessListener<List<QuerySnapshot>>()
            {
                @Override
                public void onSuccess(List<QuerySnapshot> querySnapshots)
                {
                    Location shopLocation = new Location("ShopLocation");
                    
                    for (QuerySnapshot snapshot : querySnapshots)
                    {
                        for (QueryDocumentSnapshot doc : snapshot)
                        {
                            Shop shop = new Shop(doc.getData());
                            
                            if (!(shop.getLongitude() > maxLng) && !(shop.getLongitude() < minLng))
                            {
                                shopLocation.setLatitude(shop.getLatitude());
                                shopLocation.setLongitude(shop.getLongitude());
                                float dist = myLocation.distanceTo(shopLocation);
                                
                                if (dist <= (distance * 1000))
                                    foundShops.add(new SearchResult(shop, dist));
                            }
                        }
                    }
                    
                    if (foundShops.isEmpty())
                    {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), getString(R.string.noShopsFound), Toast.LENGTH_SHORT).show();
                    } else
                    {
                        setAdapter();
                        progressBar.setVisibility(View.GONE);
                    }
                }
            });
        }
    }
    
    private void setAdapter()
    {
        Collections.sort(foundShops, searchResultComparator);
        adapter = new Adapter_Customer_SearchCard(getContext(), foundShops);
        adapter.setOnItemClickListener(this);
        foundShopsView.setAdapter(adapter);
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
