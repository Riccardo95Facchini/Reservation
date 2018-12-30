package facchini.riccardo.reservation;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ShopTagActivity extends AppCompatActivity
{
    
    //Firestore
    private FirebaseFirestore db;
    private CollectionReference shopsReference;
    private CollectionReference tagsReference;
    
    //UI
    private EditText tagsText;
    private Button sendButton;
    
    private ArrayList<String> tags;
    private String uid;
    private String mail;
    private String phone;
    private String name;
    private String address1;
    private String address2;
    private String city;
    private String zip;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_tag);
        
        //Get intent and extra
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        mail = intent.getStringExtra("mail");
        phone = intent.getStringExtra("phone");
        name = intent.getStringExtra("name");
        address1 = intent.getStringExtra("address1");
        address2 = intent.getStringExtra("address2");
        city = intent.getStringExtra("city");
        zip = intent.getStringExtra("zip");
        
        tagsText = findViewById(R.id.tagsText);
        sendButton = findViewById(R.id.sendButton);
        
        sendButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (storeTags())
                {
                    sendData();
                } else
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            tagsText.setHint(getString(R.string.noTagWarning));
                        }
                    });
                }
            }
        });
    }
    
    /**
     * Parses the text for the tags and stores them in an ArrayList
     * @return false if no tag is found, true otherwise
     */
    private boolean storeTags()
    {
        String parsedString = tagsText.getText().toString()
                .replaceAll("[^a-zA-Z\\s]", "")
                .replaceAll("\\s+", " ")
                .toLowerCase().trim();
        
        if (!parsedString.isEmpty())
        {
            tags = new ArrayList<>(Arrays.asList(parsedString.split("\\s", 0)));
            return true;
        } else
            return false;
    }
    
    /**
     * Sends the new shop and its tag to the database
     */
    private void sendData()
    {
        db = FirebaseFirestore.getInstance();
        shopsReference = db.collection("shops");
        Shop newShop = new Shop(uid, name, mail, address1, address2, city, zip, phone, tags);
        shopsReference.document(uid).set(newShop);
        
        tagsReference = db.collection("tags");
        for (String t : tags)
            checkCustomerExists(t);
    }
    
    /**
     * Checks if the tag already exists in the database,
     * it's used to decide whether to update an existing document in the database or insert a new one
     *
     * @param t Name of the document/tag
     */
    private void checkCustomerExists(final String t)
    {
        tagsReference.document(t).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
        {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot)
            {
                if (documentSnapshot.exists())
                    updateDocumentTag(t);
                else
                    createDocumentTag(t);
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Toast.makeText(ShopTagActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Creates a document for a new tag in the database,
     * also places a useless field inside of it because Firestore doesn't allow for empty documents
     *
     * @param t Name of the document/tag
     */
    private void createDocumentTag(final String t)
    {
        Map<String, Object> docData = new HashMap<>();
        docData.put("created", "");
        tagsReference.document(t).set(docData).addOnSuccessListener(new OnSuccessListener<Void>()
        {
            @Override
            public void onSuccess(Void aVoid)
            {
                updateDocumentTag(t);
            }
        });
        
    }
    
    /**
     * Updates an existing document
     *
     * @param t Name of the document/tag
     */
    private void updateDocumentTag(String t)
    {
        tagsReference.document(t).update(uid, uid);
    }
    
}
