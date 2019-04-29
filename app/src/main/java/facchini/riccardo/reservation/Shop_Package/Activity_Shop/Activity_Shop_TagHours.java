package facchini.riccardo.reservation.Shop_Package.Activity_Shop;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import facchini.riccardo.reservation.Activity_Login;
import facchini.riccardo.reservation.R;
import facchini.riccardo.reservation.Shop_Package.Shop;

public class Activity_Shop_TagHours extends AppCompatActivity
{
    //Firestore
    private FirebaseFirestore db;
    private CollectionReference shopsReference;
    
    //UI
    //Buttons
    private Button sendButton, mondayButton, tuesdayButton, wednesdayButton, thursdayButton, fridayButton, saturdayButton, sundayButton;
    //EditText
    private EditText tagsText;
    //TextViews
    private Map<String, TextView> hoursTexts;
    
    private ArrayList<String> tags;
    private Map<String, List<String>> hours;
    
    private Shop currentShop = null;
    private boolean editing = false;
    private String uid, mail, phone, name, address1, address2, city, zip;
    private double latitude, longitude;
    
    private ArrayAdapter<String> adapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_tag_hours);
        
        getIntentAndExtras(getIntent());
        
        if (!editing)
        {
            hours = new HashMap<>();
            hoursInit();
        } else
        {
            hours = currentShop.getHours();
        }
        
        getUI();
        
        sendButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (storeTags())
                    sendData();
                else
                    Toast.makeText(Activity_Shop_TagHours.this, getString(R.string.noTagWarning), Toast.LENGTH_SHORT).show();
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
        
        hoursTexts.put("Monday", (TextView) findViewById(R.id.textMon));
        hoursTexts.put("Tuesday", (TextView) findViewById(R.id.textTue));
        hoursTexts.put("Wednesday", (TextView) findViewById(R.id.textWed));
        hoursTexts.put("Thursday", (TextView) findViewById(R.id.textThu));
        hoursTexts.put("Friday", (TextView) findViewById(R.id.textFri));
        hoursTexts.put("Saturday", (TextView) findViewById(R.id.textSat));
        hoursTexts.put("Sunday", (TextView) findViewById(R.id.textSun));
        
        tagsText = findViewById(R.id.tagsText);
        
        if (editing)
        {
            setHoursTexts();
            
            try
            {
                tags = currentShop.getTags();
                tags.remove(currentShop.getName().replaceAll("[^a-zA-Z\\s]", " ")
                        .replaceAll("\\s+", " ").toLowerCase().trim());
                
            } catch (Exception e)
            {
                e.printStackTrace();
            }
            
            String name = currentShop.getName().toLowerCase().trim();
            ArrayList<String> toRemove = new ArrayList<>();
            for (String t : tags)
            {
                if (name.contains(t) || currentShop.getName().contains(t))
                    toRemove.add(t);
                else
                    tagsText.append(t.concat(" "));
            }
            tags.removeAll(toRemove);
            toRemove.clear();
        }
        
        createSpinnerAdapter();
        
        mondayButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dayButtonListenerSet("Monday");
            }
        });
        tuesdayButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            { dayButtonListenerSet("Tuesday"); }
        });
        thursdayButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {dayButtonListenerSet("Thursday");}
        });
        wednesdayButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            { dayButtonListenerSet("Wednesday"); }
        });
        fridayButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dayButtonListenerSet("Friday");
            }
        });
        saturdayButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) { dayButtonListenerSet("Saturday"); }
        });
        sundayButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dayButtonListenerSet("Sunday");
            }
        });
    }
    
    private void setHoursTexts()
    {
        hoursTexts.get("Monday").setText(currentShop.displayHoursDay("Monday"));
        hoursTexts.get("Tuesday").setText(currentShop.displayHoursDay("Tuesday"));
        hoursTexts.get("Thursday").setText(currentShop.displayHoursDay("Thursday"));
        hoursTexts.get("Wednesday").setText(currentShop.displayHoursDay("Wednesday"));
        hoursTexts.get("Friday").setText(currentShop.displayHoursDay("Friday"));
        hoursTexts.get("Saturday").setText(currentShop.displayHoursDay("Saturday"));
        hoursTexts.get("Sunday").setText(currentShop.displayHoursDay("Sunday"));
    }
    
    private void dayButtonListenerSet(final String day)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(Activity_Shop_TagHours.this);
        View view = getLayoutInflater().inflate(R.layout.alert_opening_hours, null);
        builder.setTitle(day + " hours").setCancelable(false);
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
        final String start = "00:00", finish = "23:30";
        final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        Calendar calendar = Calendar.getInstance();
        
        try
        {
            calendar.setTime(timeFormat.parse(start));
        } catch (ParseException e)
        {
            e.printStackTrace();
        }
        
        ArrayList<String> spinnerText = new ArrayList<>();
        spinnerText.add("Closed");
        
        while (!(timeFormat.format(calendar.getTime()).equals(finish)))
        {
            spinnerText.add(timeFormat.format(calendar.getTime()));
            calendar.add(Calendar.MINUTE, 30);
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
        Bundle b = intent.getExtras();
        currentShop = b.getParcelable("CurrentShop");
        if (currentShop != null)
        {
            setTitle(R.string.edit);
            editing = true;
        } else
        {
            uid = intent.getStringExtra("uid");
            mail = intent.getStringExtra("mail");
            phone = intent.getStringExtra("phone");
            name = intent.getStringExtra("name");
            address1 = intent.getStringExtra("address1");
            address2 = intent.getStringExtra("address2");
            city = intent.getStringExtra("city");
            zip = intent.getStringExtra("zip");
            latitude = intent.getDoubleExtra("latitude", 0);
            longitude = intent.getDoubleExtra("longitude", 0);
        }
    }
    
    /**
     * Parses the text for the tags and stores them in an ArrayList
     * Also adds the name of the shop to the tags
     *
     * @return false if no tag is found, true otherwise
     */
    private boolean storeTags()
    {
        if (editing)
            name = currentShop.getName();
        
        String parsedString = (tagsText.getText().toString().concat(" " + name))
                .replaceAll("[^a-zA-Z\\s]", " ")
                .replaceAll("\\s+", " ")
                .toLowerCase().trim();
        
        if (!parsedString.isEmpty())
        {
            HashSet<String> set = new HashSet<>(Arrays.asList(parsedString.split("\\s", 0)));
            set.add(name.toLowerCase().trim());
            tags = new ArrayList<>(set);
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
        final Shop shop;
        if (!editing)
        {
            int intLongitude = (int) longitude;
            shop = new Shop(uid, name, mail, address1, address2, city, zip, phone, latitude, longitude, intLongitude, tags, hours);
            shopsReference.document(uid).set(shop);
            startActivity(new Intent(this, Activity_Login.class));
        } else
        {
            uid = currentShop.getUid();
            shop = new Shop(uid, currentShop.getName(), currentShop.getMail(), currentShop.getAddress1(),
                    currentShop.getAddress2(), currentShop.getCity(), currentShop.getZip(), currentShop.getPhone(),
                    currentShop.getLatitude(), currentShop.getLongitude(), currentShop.getIntLongitude(), tags, hours);
            
            shopsReference.document(uid).set(shop);
            startActivity(new Intent(this, Activity_Shop.class));
        }
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
