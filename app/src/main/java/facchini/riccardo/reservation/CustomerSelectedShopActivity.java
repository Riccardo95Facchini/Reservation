package facchini.riccardo.reservation;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerSelectedShopActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener
{
    //Firestore
    FirebaseFirestore db;
    CollectionReference reservationsCollection;
    
    private String userUid;
    private Shop selectedShop;
    private ArrayAdapter<String> adapter;
    private Calendar selectedDate;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    
    private TextView shopNameText, shopInfoText, shopHoursText;
    private Button selectDateButton;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_selected_shop);
        
        db = FirebaseFirestore.getInstance();
        reservationsCollection = db.collection("reservations");
        userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        Bundle b = this.getIntent().getExtras();
        if (b != null)
            selectedShop = b.getParcelable("Selected");
        
        shopNameText = findViewById(R.id.shopNameText);
        shopInfoText = findViewById(R.id.shopInfoText);
        shopHoursText = findViewById(R.id.shopHoursText);
        selectDateButton = findViewById(R.id.selectDateButton);
        
        shopNameText.setText(selectedShop.getName());
        shopInfoText.setText(String.format("City: %s \tAddress: %s %s", selectedShop.getCity(),
                selectedShop.getAddress1(), selectedShop.getAddress2()));
        shopHoursText.setText(selectedShop.getHoursFormat());
        
        selectDateButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(), "DatePicker");
            }
        });
    }
    
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth)
    {
        selectedDate = Calendar.getInstance();
        selectedDate.set(Calendar.YEAR, year);
        selectedDate.set(Calendar.MONTH, month);
        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        
        checkHoursDate();
    }
    
    /**
     * Checks if in the given day there are already reservations and passes them to the method that creates the spinner
     */
    private void checkHoursDate()
    {
        reservationsCollection.document(selectedShop.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
        {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot)
            {
                if (documentSnapshot.exists())
                    createSpinnerAdapter((ArrayList<Map<String, String>>) documentSnapshot.get(sdf.format(selectedDate.getTime())));
                else
                    createSpinnerAdapter(new ArrayList<Map<String, String>>());
            }
        });
    }
    
    /**
     * Creates the spinner adapter to select the reservation time based on opening hours and already reserved hours,
     * if no slots are available displays an alert to the user asking to select a different date.
     *
     * @param takenHours Already reserved hours
     */
    private void createSpinnerAdapter(ArrayList<Map<String, String>> takenHours)
    {
        ArrayList<String> spinnerText = new ArrayList<>();
        String dayOfTheWeek = getDayString();
        List<String> hoursSelectedDay;
        List<String> takenHoursList = new ArrayList<>();
        
        for (Map<String, String> map : takenHours)
            takenHoursList.add(map.get(getString(R.string.timeLowercase)));
        
        try
        {
            /*Needed for testing, some shops don't have hours registered for a closed day, resulting in a NPE.
             If a shop is registered with the system "closed" tags are generated automatically. */
            hoursSelectedDay = new ArrayList<>(selectedShop.getHours().get(dayOfTheWeek));
            
            String h1 = hoursSelectedDay.get(0),
                    h2 = hoursSelectedDay.get(1),
                    h3 = hoursSelectedDay.get(2),
                    h4 = hoursSelectedDay.get(3);
            
            if (!h1.toLowerCase().equals(getString(R.string.closedLowercase)))
                buildSpinnerArray(h1, h2, spinnerText);
            if (!h3.toLowerCase().equals(getString(R.string.closedLowercase)))
                buildSpinnerArray(h3, h4, spinnerText);
            
            if (!takenHoursList.isEmpty() && !spinnerText.isEmpty())
                spinnerText.removeAll(takenHoursList); //Removes taken hours from the spinner list
            
        } catch (NullPointerException npe)
        {
            //Nothing needs to be done since spinnerText will still be empty
        }
        
        if (spinnerText.isEmpty())
        {
            new AlertDialog.Builder(this).setTitle(getString(R.string.sorry)).setCancelable(false)
                    .setMessage(getString(R.string.noFreeSlots))
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            //Do nothing
                        }
                    }).show();
        } else
        {
            adapter = new ArrayAdapter<>(CustomerSelectedShopActivity.this, android.R.layout.simple_spinner_item, spinnerText);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
            showSpinner(dayOfTheWeek);
        }
    }
    
    /**
     * Called by the spinner dialog on positive button press, calls the update of the array in the reservation collection
     *
     * @param result Selected element of the spinner
     */
    private void setDialogResult(String result)
    {
        Map<String, String> update = new HashMap<>();
        update.put(getString(R.string.userLowercase), userUid);
        update.put(getString(R.string.timeLowercase), result);
        db.collection("reservations").document(selectedShop.getUid()).update(sdf.format(selectedDate.getTime()), FieldValue.arrayUnion(update));
        Toast.makeText(this, "Reservation completed", Toast.LENGTH_LONG).show();
    }
    
    /**
     * Builds the spinner text with all the possible hours between start and finish, finish is not added
     *
     * @param start       Opening hour
     * @param finish      Closing hour
     * @param spinnerText ArrayList containing strings to be placed in the spinner
     */
    private void buildSpinnerArray(String start, String finish, ArrayList<String> spinnerText)
    {
        String oClock = ":00", half = ":30";
        while (!start.equals(finish))
        {
            spinnerText.add(start);
            
            if (start.endsWith(oClock))
            {
                start = (start.substring(0, start.indexOf(':'))).concat(half);
                
            } else if (start.endsWith(half))
            {
                start = (start.substring(0, start.indexOf(':'))).concat(oClock);
                start = addHourToString(start);
            }
        }
    }
    
    /**
     * Increments the hour after adding 30 minutes to a value at half hour
     *
     * @param toIncrement string to increment
     * @return Incremented string
     */
    private String addHourToString(String toIncrement)
    {
        int hourValue = Integer.parseInt(toIncrement.substring(0, toIncrement.indexOf(':')));
        hourValue++;
        
        if (hourValue < 10)
            return "0".concat(Integer.toString(hourValue).concat(toIncrement.substring(toIncrement.indexOf(':'))));
        else
            return Integer.toString(hourValue).concat(toIncrement.substring(toIncrement.indexOf(':')));
    }
    
    
    /**
     * Converts day of the week int into string used by the system
     *
     * @return Sunday to Saturday as string given the selected day
     */
    private String getDayString()
    {
        switch (selectedDate.get(Calendar.DAY_OF_WEEK))
        {
            case Calendar.SUNDAY:
                return getString(R.string.sundayText);
            case Calendar.MONDAY:
                return getString(R.string.mondayText);
            case Calendar.TUESDAY:
                return getString(R.string.tuesdayText);
            case Calendar.WEDNESDAY:
                return getString(R.string.wednesdayText);
            case Calendar.THURSDAY:
                return getString(R.string.thursdayText);
            case Calendar.FRIDAY:
                return getString(R.string.fridayText);
            case Calendar.SATURDAY:
                return getString(R.string.saturdayText);
            
        }
        return null;
    }
    
    /**
     * Shows the spinner to select the reservation time
     *
     * @param day chosen day
     */
    private void showSpinner(final String day)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(CustomerSelectedShopActivity.this);
        View view = getLayoutInflater().inflate(R.layout.alert_select_slot, null);
        builder.setTitle(day + " " + getString(R.string.availableHours));
        final Spinner spinner = view.findViewById(R.id.spinner);
        spinner.setAdapter(adapter);
        
        builder.setPositiveButton(getString(R.string.set), new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                setDialogResult(spinner.getSelectedItem().toString());
                dialog.dismiss();
            }
        }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                Toast.makeText(CustomerSelectedShopActivity.this, "No reservation selected", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setView(view);
        builder.show();
    }
}
