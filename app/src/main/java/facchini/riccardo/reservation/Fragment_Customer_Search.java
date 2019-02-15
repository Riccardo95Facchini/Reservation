package facchini.riccardo.reservation;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class Fragment_Customer_Search extends Fragment
{
    
    private EditText searchText;
    private ImageButton searchButton;
    private ListView foundShopsView;
    private ProgressBar progressBar;
    private SeekBar distanceSeekBar;
    private TextView distanceText;
    
    private SharedViewModel viewModel;
    private ArrayList<SearchResult> foundShops = new ArrayList<>();
    
    //Location
    private FusedLocationProviderClient client;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location myLocation;
    private boolean locationCalled = false;
    private int distance = 1;
    
    private double deltaLng, deltaLat;
    
    //Firestore
    FirebaseFirestore db;
    CollectionReference tagsCollection, shopsCollection;
    
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
        viewModel = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        searchText = view.findViewById(R.id.searchText);
        searchButton = view.findViewById(R.id.searchButton);
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
        
        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        
        db = FirebaseFirestore.getInstance();
        tagsCollection = db.collection("tags");
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
                        searchTag(text);
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
                    searchTag(text);
            }
        });
        
        foundShopsView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Shop selected = foundShops.get(position).getShopFound();
                viewModel.setCurrentShop(selected);
                getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(null);
                Intent intent = new Intent();
                Bundle b = new Bundle();
                b.putParcelable("Selected", selected);
                Map<String, String> customer = new HashMap<>();
                intent.putExtras(b);
                intent.putExtra("uid", viewModel.getCurrentCustomer().getUid());
                intent.putExtra("name", viewModel.getCurrentCustomer().getName());
                intent.putExtra("surname", viewModel.getCurrentCustomer().getSurname());
                intent.setClass(getContext(), Activity_Customer_SelectedShop.class);
                startActivity(intent);
            }
        });
        
        if (!locationCalled)
            getLocation();
    }
    
    private void getLocation()
    {
        locationCalled = true;
        
        /*Location manager system from https://developer.android.com/guide/topics/location/strategies#java*/
        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener()
        {
            @Override
            public void onLocationChanged(Location location)
            {
                myLocation = location;
                locationManager.removeUpdates(locationListener);
            }
            
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) { }
            
            @Override
            public void onProviderEnabled(String provider) {}
            
            @Override
            public void onProviderDisabled(String provider) {}
        };
        
        try
        {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                return;
            
            /*Location manager system from https://developer.android.com/guide/topics/location/strategies#java
             * Requires both from network and gps provider*/
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            
            //Also try to get the last known location
            myLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(), false));
            
            /*Other method using FusedLocationProvider*/
            geocoder = new Geocoder(getActivity(), Locale.getDefault());
            client = LocationServices.getFusedLocationProviderClient(getActivity());
            client.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>()
            {
                @Override
                public void onSuccess(Location location)
                {
                    myLocation = location;
                }
            }).addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    e.printStackTrace();
                }
            });
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    
    /**
     * Searches if the text in the search box corresponds to a tag in the system
     *
     * @param text Tag to search
     */
    private void searchTag(String text)
    {
        text = text.replaceAll("[^a-zA-Z\\s]", "")
                .replaceAll("\\s+", " ").toLowerCase().trim();
        
        progressBar.setVisibility(View.VISIBLE);
        foundShops.clear();
        
        getDeltas();
        
        double minLat = myLocation.getLatitude() - deltaLat,
                maxLat = myLocation.getLatitude() + deltaLat,
                minLng = myLocation.getLongitude() - deltaLng,
                maxLng = myLocation.getLongitude() + deltaLng;
        
        
        Task taskLat = shopsCollection.whereArrayContains("tags", text)
                .whereGreaterThanOrEqualTo("latitude", minLat)
                .whereLessThanOrEqualTo("latitude", maxLat)
                .get();
        
        Task taskLng = shopsCollection.whereArrayContains("tags", text)
                .whereGreaterThanOrEqualTo("longitude", minLng)
                .whereLessThanOrEqualTo("longitude", maxLng)
                .get();
        
        Task<List<QuerySnapshot>> allTasks = Tasks.whenAllSuccess(taskLat, taskLng);
        allTasks.addOnSuccessListener(new OnSuccessListener<List<QuerySnapshot>>()
        {
            @Override
            public void onSuccess(List<QuerySnapshot> querySnapshots)
            {
                try
                {
                    Set<String> queryShops = new HashSet<>();
                    Location shopLocation = new Location("ShopLocation");
                    
                    int i = 0;
                    for (QuerySnapshot snapshot : querySnapshots)
                    {
                        for (QueryDocumentSnapshot doc : snapshot)
                        {
                            String uid = (String) doc.getData().get("uid");
                            
                            if (i == 0)
                            {
                                queryShops.add(uid);
                            } else if (queryShops.contains(uid))
                            {
                                Shop shop = new Shop(doc.getData());
                                
                                shopLocation.setLatitude(shop.getLatitude());
                                shopLocation.setLongitude(shop.getLongitude());
                                float dist = myLocation.distanceTo(shopLocation);
                                
                                if (dist <= (distance * 1000))
                                    foundShops.add(new SearchResult(shop, dist));
                            }
                        }
                        i++;
                    }
                    
                    queryShops.clear();
                    
                    if (foundShops.isEmpty())
                    {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), getString(R.string.noShopsFound), Toast.LENGTH_SHORT).show();
                    } else
                    {
                        orderList(foundShops);
                        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
                        foundShopsView.setAdapter(adapter);
                        
                        for (SearchResult fs : foundShops)
                            adapter.add(fs.getShopFound().getInfo().concat(String.format("\nDistance: %s", fs.getFormatDistance())));
                        
                        progressBar.setVisibility(View.GONE);
                        
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }
    
    private void orderList(ArrayList<SearchResult> searchResults)
    {
        Collections.sort(searchResults, searchResultComparator);
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
    
    private void getDeltas()
    {
        int dist = 1000 * distance;
        try
        {
            if (myLocation == null)
            {
                Log.d("ECCEZIONE", "no location");
            } else
            {
                Log.d("ECCEZIONE", String.format("lat: %s\nlng: %s", myLocation.getLatitude(), myLocation.getLongitude()));
                locationManager.removeUpdates(locationListener); //Stop listening for locations
                final double earthRadius = 6371000; //meters
                
                deltaLat = (dist / earthRadius) * (180 / Math.PI);
                deltaLng = Math.toDegrees((dist / (earthRadius * Math.cos(Math.toRadians(myLocation.getLatitude())))));
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
