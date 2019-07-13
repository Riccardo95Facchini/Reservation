package facchini.riccardo.reservation;

import android.location.Location;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import facchini.riccardo.reservation.Shop_Package.Shop;

public class SearchViewModel extends ViewModel
{
    private MutableLiveData<List<SearchResult>> searchResults;
    private CollectionReference shopsCollection;
    
    public SearchViewModel()
    {
        shopsCollection = FirebaseFirestore.getInstance().collection("shops");
        this.searchResults = new MutableLiveData<>();
    }
    
    public void queryForTag(String text, final int distance, double deltaLat, double deltaLng, final Location myLocation)
    {
        text = text.replaceAll("[^a-zA-Z\\s]", " ").replaceAll("\\s+", " ").toLowerCase().trim();
        
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
                List<SearchResult> foundShops = new ArrayList<>();
                
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
                
                if (!foundShops.isEmpty())
                    searchResults.setValue(foundShops);
            }
        });
        
    }
    
    public MutableLiveData<List<SearchResult>> getSearchResults() {return searchResults;}
}
