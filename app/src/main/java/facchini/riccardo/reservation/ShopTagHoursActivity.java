package facchini.riccardo.reservation;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ShopTagHoursActivity extends AppCompatActivity
{
    //Firestore
    private FirebaseFirestore db;
    private CollectionReference shopsReference;
    private CollectionReference tagsReference;
    
    //UI
    //Buttons
    private Button sendButton, mondayButton;
    //EditText
    private EditText tagsText;
    
    private ArrayList<String> tags;
    private String uid, mail, phone, name, address1, address2, city, zip;
    
    private ArrayAdapter<String> adapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_tag_hours);
        
        
        getIntentAndExtras(getIntent());
        
        getUI();
        
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
    
    private void getUI()
    {
        sendButton = findViewById(R.id.sendButton);
        mondayButton = findViewById(R.id.mondayButton);
        tagsText = findViewById(R.id.tagsText);
        
        createSpinnerAdapter();
        
        mondayButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(ShopTagHoursActivity.this);
                View view = getLayoutInflater().inflate(R.layout.alert_opening_hours, null);
                builder.setTitle("Monday hours");
                final Spinner timeSpinner1 = view.findViewById(R.id.timeSpinner1);
                final Spinner timeSpinner2 = view.findViewById(R.id.timeSpinner2);
                final Spinner timeSpinner3 = view.findViewById(R.id.timeSpinner3);
                final Spinner timeSpinner4 = view.findViewById(R.id.timeSpinner4);
                timeSpinner1.setAdapter(adapter);
                timeSpinner2.setAdapter(adapter);
                timeSpinner3.setAdapter(adapter);
                timeSpinner4.setAdapter(adapter);
                
                builder.setPositiveButton("Set", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        int t1 = timeSpinner1.getSelectedItemPosition();
                        int t2 = timeSpinner2.getSelectedItemPosition();
                        int t3 = timeSpinner3.getSelectedItemPosition();
                        int t4 = timeSpinner4.getSelectedItemPosition();
                        
                        if (!(t1 <= t2 && t1 <= t3 && t3 <= t4 && t2 <= t3))
                            Toast.makeText(ShopTagHoursActivity.this, "Wrong times selected, try again", Toast.LENGTH_LONG).show();
                        else
                        {
                            tagsText.setText(String.format("%s %s\n%s %s", timeSpinner1.getSelectedItem().toString(), timeSpinner2.getSelectedItem().toString(), timeSpinner3.getSelectedItem().toString(), timeSpinner4.getSelectedItem().toString()));
                        }
                        dialog.dismiss();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                });
                builder.setView(view);
                builder.show();
            }
        });
        
    }
    
    private void createSpinnerAdapter()
    {
        ArrayList<String> spinnerText = new ArrayList<>();
        final String oClock = ":00";
        final String quarter = ":15";
        final String half = ":30";
        final String quarterTo = ":45";
        
        for (int hours = 0; hours < 24; hours++)
        {
            if (hours < 10)
            {
                spinnerText.add("0" + hours + oClock);
                spinnerText.add("0" + hours + quarter);
                spinnerText.add("0" + hours + half);
                spinnerText.add("0" + hours + quarterTo);
            } else
            {
                spinnerText.add(hours + oClock);
                spinnerText.add(hours + quarter);
                spinnerText.add(hours + half);
                spinnerText.add(hours + quarterTo);
            }
        }
        adapter = new ArrayAdapter<>(ShopTagHoursActivity.this, android.R.layout.simple_spinner_item, spinnerText);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
    }
    
    private boolean checkHours(int t1, int t2, int t3, int t4)
    {
        return t1 <= t2 && t1 <= t3 && t3 <= t4 && t2 <= t3;
    }
    
    private void getIntentAndExtras(Intent intent)
    {
        uid = intent.getStringExtra("uid");
        mail = intent.getStringExtra("mail");
        phone = intent.getStringExtra("phone");
        name = intent.getStringExtra("name");
        address1 = intent.getStringExtra("address1");
        address2 = intent.getStringExtra("address2");
        city = intent.getStringExtra("city");
        zip = intent.getStringExtra("zip");
    }
    
    /**
     * Parses the text for the tags and stores them in an ArrayList
     *
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
                Toast.makeText(ShopTagHoursActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
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
