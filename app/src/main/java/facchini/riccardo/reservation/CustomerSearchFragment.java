package facchini.riccardo.reservation;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomerSearchFragment extends Fragment
{
    
    View view;
    EditText searchText;
    ImageButton searchButton;
    ListView foundShopsView;
    
    ArrayList<Shop> foundShops;
    
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        view = getView();
        searchText = view.findViewById(R.id.searchText);
        searchButton = view.findViewById(R.id.searchButton);
        foundShopsView = view.findViewById(R.id.foundShopsView);
        
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
        
    }
    
    /**
     * Searches if the text in the search box corresponds to a tag in the system
     * @param text Tag to search
     */
    private void searchTag(String text)
    {
        tagsCollection.document(text).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
        {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot)
            {
                if (documentSnapshot.exists())
                {
                    Map<String, Object> map = documentSnapshot.getData();
                    displayShops(map);
                }
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
            
            }
        });
        
    }
    
    /**
     * If the tag exists pull all the info of the shops that have that tag
     * @param map Map<uid, uid>, basically the entire document with the given tag
     */
    private void displayShops(Map<String, Object> map)
    {
        foundShops = new ArrayList<>();
        final List<Shop> shops = new ArrayList<>();
        final List<String> names = new ArrayList<>();
        final ArrayAdapter<Shop> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
        for (Map.Entry<String, Object> entry : map.entrySet())
        {
            shopsQuery = shopsCollection.whereEqualTo("uid", entry.getValue());
            
            shopsQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
            {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots)
                {
                    for (QueryDocumentSnapshot snap : queryDocumentSnapshots)
                    {
                        shops.add(snap.toObject(Shop.class));
                    }
                    for (Shop s : shops)
                    {
                        adapter.add(s);
                        shops.clear();
                    }
                }
            });
            
        }
        foundShopsView.setAdapter(adapter);
    }
}
