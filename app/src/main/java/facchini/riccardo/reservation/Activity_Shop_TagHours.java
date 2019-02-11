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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Activity_Shop_TagHours extends AppCompatActivity
{
    //Firestore
    private FirebaseFirestore db;
    private CollectionReference shopsReference;
    private CollectionReference tagsReference;
    
    //UI
    //Buttons
    private Button sendButton, mondayButton, tuesdayButton, wednesdayButton, thursdayButton, fridayButton, saturdayButton, sundayButton;
    //EditText
    private EditText tagsText;
    //TextViews
    private Map<String, TextView> hoursTexts;
    
    private ArrayList<String> tags;
    private Map<String, List<String>> hours;
    
    private String uid, mail, phone, name, address1, address2, city, zip;
    
    private ArrayAdapter<String> adapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_tag_hours);
        
        hours = new HashMap<>();
        hoursInit();
        
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
    
    /**
     * Sets UI elements
     */
    private void getUI()
    {
        sendButton = findViewById(R.id.sendButton);
        
        mondayButton = findViewById(R.id.mondayButton);
        tuesdayButton = findViewById(R.id.tuesdayButton);
        thursdayButton = findViewById(R.id.thursdayButton);
        wednesdayButton = findViewById(R.id.wednesdayButton);
        fridayButton = findViewById(R.id.fridayButton);
        saturdayButton = findViewById(R.id.saturdayButton);
        sundayButton = findViewById(R.id.sundayButton);
        
        hoursTexts = new HashMap<>();
        
        hoursTexts.put(getString(R.string.mondayText), (TextView) findViewById(R.id.textMon));
        hoursTexts.put(getString(R.string.tuesdayText), (TextView) findViewById(R.id.textTue));
        hoursTexts.put(getString(R.string.wednesdayText), (TextView) findViewById(R.id.textWed));
        hoursTexts.put(getString(R.string.thursdayText), (TextView) findViewById(R.id.textThu));
        hoursTexts.put(getString(R.string.fridayText), (TextView) findViewById(R.id.textFri));
        hoursTexts.put(getString(R.string.saturdayText), (TextView) findViewById(R.id.textSat));
        hoursTexts.put(getString(R.string.sundayText), (TextView) findViewById(R.id.textSun));
        
        tagsText = findViewById(R.id.tagsText);
        
        createSpinnerAdapter();
        
        mondayButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dayButtonListenerSet(getString(R.string.mondayText));
            }
        });
        tuesdayButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dayButtonListenerSet(getString(R.string.tuesdayText));
            }
        });
        thursdayButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dayButtonListenerSet(getString(R.string.thursdayText));
            }
        });
        wednesdayButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dayButtonListenerSet(getString(R.string.wednesdayText));
            }
        });
        fridayButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dayButtonListenerSet(getString(R.string.fridayText));
            }
        });
        saturdayButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dayButtonListenerSet(getString(R.string.saturdayText));
            }
        });
        sundayButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dayButtonListenerSet(getString(R.string.sundayText));
            }
        });
    }
    
    private void dayButtonListenerSet(final String day)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(Activity_Shop_TagHours.this);
        View view = getLayoutInflater().inflate(R.layout.alert_opening_hours, null);
        builder.setTitle(day + " hours");
        final Spinner timeSpinner1 = view.findViewById(R.id.spinner);
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
                
                if (!checkHours(t1, t2, t3, t4))
                    Toast.makeText(Activity_Shop_TagHours.this, "Wrong times selected, try again", Toast.LENGTH_LONG).show();
                else
                {
                    
                    ArrayList ret = new ArrayList<String>();
                    ret.add(timeSpinner1.getSelectedItem().toString());
                    ret.add(timeSpinner2.getSelectedItem().toString());
                    ret.add(timeSpinner3.getSelectedItem().toString());
                    ret.add(timeSpinner4.getSelectedItem().toString());
                    
                    hours.put(day, ret);
                    hoursTexts.get(day).setText(String.format("%s-%s \t %s-%s", ret.get(0), ret.get(1), ret.get(2), ret.get(3)));
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
    
    /**
     * Creates all values from 00:00 to 23:30 to be placed in the spinners at 30 minutes steps
     */
    private void createSpinnerAdapter()
    {
        ArrayList<String> spinnerText = new ArrayList<>();
        spinnerText.add("Closed");
        
        final String oClock = ":00";
        final String half = ":30";
        
        for (int hours = 0; hours < 24; hours++)
        {
            if (hours < 10)
            {
                spinnerText.add("0" + hours + oClock);
                spinnerText.add("0" + hours + half);
            } else
            {
                spinnerText.add(hours + oClock);
                spinnerText.add(hours + half);
            }
        }
        adapter = new ArrayAdapter<>(Activity_Shop_TagHours.this, android.R.layout.simple_spinner_item, spinnerText);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
    }
    
    private boolean checkHours(int t1, int t2, int t3, int t4)
    {
        if (t1 > t2 || t3 > t4)
            return false;
        if ((t1 != 0 && t2 == 0) || (t2 != 0 && t1 == 0) || (t3 != 0 && t4 == 0) || (t4 != 0 && t3 == 0))
            return false;
        if ((t1 >= t3 && t3 != 0) || (t2 >= t3 && t3 != 0))
            return false;
        
        return true;
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
     * Also adds the name of the shop to the tags
     *
     * @return false if no tag is found, true otherwise
     */
    private boolean storeTags()
    {
        
        String parsedString = (tagsText.getText().toString().concat(name))
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
        Shop newShop = new Shop(uid, name, mail, address1, address2, city, zip, phone, tags, hours);
        shopsReference.document(uid).set(newShop);
        tagsReference = db.collection("tags");
        createDocumentReservation();
        for (String t : tags)
            checkTagExists(t);
        
        startActivity(new Intent(this, Activity_Login.class));
    }
    
    /**
     * Checks if the tag already exists in the database,
     * it's used to decide whether to update an existing document in the database or insert a new one
     *
     * @param t Name of the document/tag
     */
    private void checkTagExists(final String t)
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
                Toast.makeText(Activity_Shop_TagHours.this, "Something went wrong", Toast.LENGTH_SHORT).show();
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
     * Creates the empty document to hold future reservations
     */
    private void createDocumentReservation()
    {
        Map<String, Object> docData = new HashMap<>();
        docData.put("created", "");
        db.collection("reservations").document(uid).set(docData).addOnSuccessListener(new OnSuccessListener<Void>()
        {
            @Override
            public void onSuccess(Void aVoid)
            {
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
    
    /**
     * Inits all days as closed
     */
    private void hoursInit()
    {
        ArrayList<String> closed = new ArrayList<String>(Arrays.asList(new String[]{"Closed", "Closed", "Closed", "Closed"}));
        
        hours.put(getString(R.string.mondayText), closed);
        hours.put(getString(R.string.tuesdayText), closed);
        hours.put(getString(R.string.wednesdayText), closed);
        hours.put(getString(R.string.thursdayText), closed);
        hours.put(getString(R.string.fridayText), closed);
        hours.put(getString(R.string.saturdayText), closed);
        hours.put(getString(R.string.sundayText), closed);
    }
    
}
