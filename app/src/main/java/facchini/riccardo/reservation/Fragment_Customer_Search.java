package facchini.riccardo.reservation;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
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
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.Locale;
import java.util.Map;

public class Fragment_Customer_Search extends Fragment
{
    
    private EditText searchText;
    private ImageButton searchButton;
    private ListView foundShopsView;
    private ProgressBar progressBar;
    
    private SharedViewModel viewModel;
    private ArrayList<Shop> foundShops = new ArrayList<>();
    
    //Location
    private FusedLocationProviderClient client;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location myLocation;
    private Geocoder geocoder;
    private boolean locationCalled = false;
    
    //Firestore
    FirebaseFirestore db;
    CollectionReference tagsCollection, shopsCollection;
    Query shopsQuery;
    
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
                Shop selected = foundShops.get(position);
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
    }
    
    private void getLocation()
    {
        locationCalled = true;
        //Find current location
        geocoder = new Geocoder(getActivity(), Locale.getDefault());
        //myLocation = new Location("myLocation");
        /* Location system #1*/
        //myLocation = getLastKnownLocation();
        try
        {
            /*Location #3*/
            client = LocationServices.getFusedLocationProviderClient(getActivity());
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                return;
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
            
            /*Location system #2*/
//            locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
//            LocationListener locationListener = new MyLocationListener();
//            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
//            {
//                return;
//            }
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 10, locationListener);
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
        if (!locationCalled)
            getLocation();
        
        text = text.replaceAll("[^a-zA-Z\\s]", "")
                .replaceAll("\\s+", " ").toLowerCase().trim();
        
        progressBar.setVisibility(View.VISIBLE);
        foundShops.clear();
        
        tagsCollection.document(text).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
        {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot)
            {
                if (documentSnapshot.exists())
                    displayShops(documentSnapshot.getData());
                else
                {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), getString(R.string.noShopsFound), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    /**
     * If the tag exists pull all the info of the shops that have that tag and displays them in the listview
     *
     * @param shopsFromTags Map<uid, uid>, basically the entire document with the given tag
     */
    private void displayShops(final Map<String, Object> shopsFromTags)
    {
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
        foundShopsView.setAdapter(adapter);
        
        for (Map.Entry<String, Object> entry : shopsFromTags.entrySet())
        {
            shopsQuery = shopsCollection.whereEqualTo("uid", entry.getValue());
            
            
            shopsQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
            {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots)
                {
                    for (QueryDocumentSnapshot snap : queryDocumentSnapshots)
                        foundShops.add(snap.toObject(Shop.class)); //Add shop to the list of found ones
                    
                    //Since it's asynchronous we don't know when it's the last one and we have to check
                    if (foundShops.size() == shopsFromTags.size())
                    {
                        Address address = null;
                        Location shopLoc = new Location("Shop Location");
                        
                        ArrayList<SearchResult> searchResults = new ArrayList<>();
                        
                        for (Shop s : foundShops)
                        {
                            try
                            {
                                //TODO: OPTIMIZE THIS PART
                                address = geocoder.getFromLocationName(s.getFullAddress(), 1).get(0);
                                shopLoc.setLatitude(address.getLatitude());
                                shopLoc.setLongitude(address.getLongitude());
                            } catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                            
                            try
                            {
                                searchResults.add(new SearchResult(s, myLocation.distanceTo(shopLoc)));
                            } catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                        orderList(searchResults);
                        
                        for (SearchResult sr : searchResults)
                            adapter.add(sr.getShopFound().getInfo().concat(String.format("\nDistance: %s", sr.getFormatDistance())));
                        
                        progressBar.setVisibility(View.GONE);
                    }
                }
            });
        }
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
    
    /*Location Systems #1 and #2*/
    // Location system #1
//    private Location getLastKnownLocation()
//    {
//        locationManager = (LocationManager) getActivity().getApplicationContext().getSystemService(LOCATION_SERVICE);
//        List<String> providers = locationManager.getProviders(true);
//        Location bestLocation = null;
//        for (String provider : providers)
//        {
//
//            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
//            {
//                return null;
//            }
//            Location l = locationManager.getLastKnownLocation(provider);
//
//            if (l == null)
//            {
//                continue;
//            }
//            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy())
//            {
//                // Found best last known location: %s", l);
//                bestLocation = l;
//            }
//        }
//        return bestLocation;
//    }
    
    
    //Listener for system #2 of location
    private class MyLocationListener implements LocationListener
    {
        @Override
        public void onLocationChanged(Location loc)
        {
            try
            {
                myLocation.setLongitude(loc.getLongitude());
                myLocation.setLatitude(loc.getLatitude());
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        
        @Override
        public void onProviderDisabled(String provider) {}
        
        @Override
        public void onProviderEnabled(String provider) {}
        
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }
}
