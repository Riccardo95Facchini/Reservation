package facchini.riccardo.reservation;

import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class Fragment_Customer_Search extends Fragment implements OnItemClickListener
{
    
    private EditText searchText;
    private ImageButton searchButton;
    private RecyclerView foundShopsView;
    private ProgressBar progressBar;
    private SeekBar distanceSeekBar;
    private TextView distanceText;
    
    private SharedViewModel viewModel;
    private ArrayList<SearchResult> foundShops = new ArrayList<>();
    private Adapter_Customer_Search adapter;
    
    //Location
    private Location myLocation;
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
        viewModel.setCurrentShop(selected);
        getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(null);
        Intent intent = new Intent();
        Bundle b = new Bundle();
        b.putParcelable("Selected", selected);
        intent.putExtras(b);
        intent.putExtra("uid", viewModel.getCurrentCustomer().getUid());
        intent.putExtra("name", viewModel.getCurrentCustomer().getName());
        intent.putExtra("surname", viewModel.getCurrentCustomer().getSurname());
        intent.setClass(getContext(), Activity_Customer_SelectedShop.class);
        startActivity(intent);
    }
    
    private void getLocation()
    {
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        final Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        
        new AlertDialog.Builder(getContext()).setCancelable(false).setView(input)
                .setTitle(getString(R.string.addressSearch))
                .setMessage(getString(R.string.addressSearchInput))
                .setPositiveButton("Set", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        try
                        {
                            myLocation = new Location("myLocation");
                            Address selectedAddress = geocoder.getFromLocationName(input.getText().toString(), 1).get(0);
                            myLocation.setLatitude(selectedAddress.getLatitude());
                            myLocation.setLongitude(selectedAddress.getLongitude());
                        } catch (IOException e)
                        {
                            Toast.makeText(getContext(), "Error, address could not be found", Toast.LENGTH_SHORT).show();
                            searchText.clearFocus();
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
        adapter = new Adapter_Customer_Search(getContext(), foundShops);
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
