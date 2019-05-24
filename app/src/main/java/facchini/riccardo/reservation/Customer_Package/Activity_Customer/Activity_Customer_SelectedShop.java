package facchini.riccardo.reservation.Customer_Package.Activity_Customer;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import facchini.riccardo.reservation.Chat.Activity_Chat;
import facchini.riccardo.reservation.Fragment_DatePicker;
import facchini.riccardo.reservation.R;
import facchini.riccardo.reservation.Reservation_Package.ReservationDatabase;
import facchini.riccardo.reservation.Shop_Package.Shop;

public class Activity_Customer_SelectedShop extends AppCompatActivity implements DatePickerDialog.OnDateSetListener
{
    //Firestore
    FirebaseFirestore db;
    CollectionReference reservationsCollection;
    
    private Shop selectedShop;
    private ArrayAdapter<String> adapter;
    private Calendar selectedDate;
    private String name, surname;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    
    private TextView shopNameText, shopInfoText, shopHoursText;
    private Button selectDateButton;
    private ImageButton startChatButton;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_selected_shop);
        
        db = FirebaseFirestore.getInstance();
        reservationsCollection = db.collection("reservations");
        
        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        if (b != null)
            selectedShop = b.getParcelable("Selected");
        
        name = intent.getStringExtra("name");
        surname = intent.getStringExtra("surname");
        
        
        shopNameText = findViewById(R.id.shopNameText);
        shopInfoText = findViewById(R.id.shopInfoText);
        shopHoursText = findViewById(R.id.shopHoursText);
        selectDateButton = findViewById(R.id.selectDateButton);
        startChatButton = findViewById(R.id.startChatButton);
        
        shopNameText.setText(selectedShop.getName());
        shopInfoText.setText(String.format("City: %s \tAddress: %s %s", selectedShop.getCity(),
                selectedShop.getAddress1(), selectedShop.getAddress2()));
        shopHoursText.setText(selectedShop.displayHoursFormat());
        
        selectDateButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DialogFragment datePicker = new Fragment_DatePicker();
                datePicker.show(getSupportFragmentManager(), "DatePicker");
            }
        });
        
        startChatButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startChat();
            }
        });
    }
    
    private void startChat()
    {
        Intent chatIntent = new Intent(Activity_Customer_SelectedShop.this, Activity_Chat.class);
        chatIntent.putExtra("thisUsername", String.format("%s %s", name, surname));
        chatIntent.putExtra("otherUid", selectedShop.getUid());
        chatIntent.putExtra("otherUsername", selectedShop.getName());
        startActivity(chatIntent);
    }
    
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth)
    {
        selectedDate = Calendar.getInstance();
        selectedDate.set(Calendar.YEAR, year);
        selectedDate.set(Calendar.MONTH, month);
        selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        selectedDate.set(Calendar.AM_PM, 0);
        selectedDate.set(Calendar.HOUR, 0);
        selectedDate.set(Calendar.MINUTE, 0);
        selectedDate.set(Calendar.SECOND, 0);
        selectedDate.set(Calendar.MILLISECOND, 0);
        
        checkHoursDate();
    }
    
    /**
     * Takes the selected day and does a query to check if there are already reservations on that day
     */
    private void checkHoursDate()
    {
        Calendar plusDay = Calendar.getInstance();
        plusDay.setTime(selectedDate.getTime());
        plusDay.add(Calendar.HOUR, 24);
        reservationsCollection.whereEqualTo("shopUid", selectedShop.getUid())
                .whereGreaterThan("time", selectedDate.getTime())
                .whereLessThan("time", plusDay.getTime()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
                {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots)
                    {
                        createSpinnerAdapter(queryDocumentSnapshots);
                    }
                }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                e.printStackTrace();
            }
        });
    }
    
    /**
     * Creates the spinner adapter to select the reservation time based on opening hours and already reserved hours,
     * if no slots are available displays an alert to the user asking to select a different date.
     *
     * @param snap result of the query, contains all reservations for the selected day
     */
    private void createSpinnerAdapter(QuerySnapshot snap)
    {
        
        List<String> reservedHours = new ArrayList<>();
        
        for (QueryDocumentSnapshot doc : snap)
            reservedHours.add(timeFormat.format(((Timestamp) doc.get("time")).toDate()));
        
        ArrayList<String> spinnerText = new ArrayList<>();
        String dayOfTheWeek = getDayString();
        List<String> hoursSelectedDay;
        
        try
        {
                /*Needed for testing, some shops don't have hours registered for a closed day, resulting in a NPE.
                 If a shop is registered with the system "closed" tags are generated automatically. */
            hoursSelectedDay = new ArrayList<>(selectedShop.getHours().get(dayOfTheWeek));
            
            String h1 = hoursSelectedDay.get(0),
                    h2 = hoursSelectedDay.get(1),
                    h3 = hoursSelectedDay.get(2),
                    h4 = hoursSelectedDay.get(3);
            
            if (!h1.equalsIgnoreCase(getString(R.string.closedLowercase)))
                buildSpinnerArray(h1, h2, spinnerText);
            if (!h3.equalsIgnoreCase(getString(R.string.closedLowercase)))
                buildSpinnerArray(h3, h4, spinnerText);
            
            if (!reservedHours.isEmpty() && !spinnerText.isEmpty())
                spinnerText.removeAll(reservedHours); //Removes taken hours from the spinner list
            
        } catch (NullPointerException npe)
        {
            //Nothing needs to be done since spinnerText will still be empty
            npe.printStackTrace();
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
            adapter = new ArrayAdapter<>(Activity_Customer_SelectedShop.this, android.R.layout.simple_spinner_item, spinnerText);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
            showSpinner(dayOfTheWeek);
        }
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
        Calendar calStart = Calendar.getInstance();
        
        try
        {
            calStart.setTime(timeFormat.parse(start));
            
        } catch (ParseException e)
        {
            e.printStackTrace();
        }
        
        while (!(timeFormat.format(calStart.getTime()).equals(finish)))
        {
            spinnerText.add(timeFormat.format(calStart.getTime()));
            calStart.add(Calendar.MINUTE, 30);
        }
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
        AlertDialog.Builder builder = new AlertDialog.Builder(Activity_Customer_SelectedShop.this);
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
                Toast.makeText(Activity_Customer_SelectedShop.this, "No reservation selected", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setView(view);
        builder.show();
    }
    
    /**
     * Called by the spinner dialog on positive button press, calls the update of the array in the reservation collection
     *
     * @param result Selected element of the spinner
     */
    private void setDialogResult(String result)
    {
        Date fullDate = new Date();
        String date = dateFormat.format(selectedDate.getTime()).concat(" ".concat(result));
        try
        {
            fullDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(date);
        } catch (ParseException e)
        {
            //TODO: handle exception
            e.printStackTrace();
        }
        
        String customerName = String.format("%s %s", name, surname);
        
        String thisUid = getSharedPreferences(getString(R.string.reservations_preferences), Context.MODE_PRIVATE)
                .getString(getString(R.string.current_user_username_key), "");
        
        ReservationDatabase reservationDatabase = new ReservationDatabase(selectedShop.getUid(), thisUid, customerName, fullDate);
        db.collection("reservations").add(reservationDatabase);
        
        Toast.makeText(this, getString(R.string.reservationCompleted), Toast.LENGTH_LONG).show();
    }
}
