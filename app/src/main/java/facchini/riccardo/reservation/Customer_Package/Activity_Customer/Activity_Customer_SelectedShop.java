package facchini.riccardo.reservation.Customer_Package.Activity_Customer;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import facchini.riccardo.reservation.Adapter_CardInfo;
import facchini.riccardo.reservation.Chat.Activity_Chat;
import facchini.riccardo.reservation.Fragment_DatePicker;
import facchini.riccardo.reservation.Info_Content;
import facchini.riccardo.reservation.Notification;
import facchini.riccardo.reservation.R;
import facchini.riccardo.reservation.Reservation_Package.ReservationFirestore;
import facchini.riccardo.reservation.Shop_Package.Shop;

public class Activity_Customer_SelectedShop extends AppCompatActivity implements DatePickerDialog.OnDateSetListener
{
    //Firestore
    FirebaseFirestore db;
    CollectionReference reservationsCollection;
    
    private RecyclerView recyclerView;
    private Adapter_CardInfo adapterCardInfo;
    private List<Info_Content> contents;
    
    private Shop selectedShop;
    private ArrayAdapter<String> adapter;
    private Calendar selectedDate, now;
    private String name, picUrl;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_selected_shop);
        
        db = FirebaseFirestore.getInstance();
        reservationsCollection = db.collection("reservations");
        
        now = Calendar.getInstance();
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);
        
        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        if (b != null)
            selectedShop = b.getParcelable("Selected");
        
        name = intent.getStringExtra("name");
        picUrl = intent.getStringExtra("picUrl");
        
        setTitle(selectedShop.getName());
        
        Button selectDateButton = findViewById(R.id.selectDateButton);
        ImageView profilePic = findViewById(R.id.profilePic);
        ImageButton buttonAction = findViewById(R.id.buttonAction);
        TextView textReviews = findViewById(R.id.textReviews);
        RatingBar ratingAvg = findViewById(R.id.ratingAvg);
        recyclerView = findViewById(R.id.info);
        
        contents = new ArrayList<>();
        adapterCardInfo = new Adapter_CardInfo(this, contents);
        recyclerView.setAdapter(adapterCardInfo);
        
        buttonAction.setImageResource(R.drawable.ic_chat_primary_color_32dp);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        contents.addAll(selectedShop.createInfoContentList());
        
        Glide.with(this).load(selectedShop.getProfilePicUrl()).placeholder(R.drawable.default_avatar).fitCenter().centerCrop().transform(new CircleCrop()).into(profilePic);
        textReviews.setText(String.format("(%.2f/5) %d %s", selectedShop.getAverageReviews(), selectedShop.getNumReviews(), getString(R.string.reviews)));
        ratingAvg.setRating((float) selectedShop.getAverageReviews());
        
        selectDateButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DialogFragment datePicker = new Fragment_DatePicker();
                datePicker.show(getSupportFragmentManager(), "DatePicker");
            }
        });
        
        buttonAction.setOnClickListener(new View.OnClickListener()
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
        chatIntent.putExtra("thisUsername", name);
        chatIntent.putExtra("otherUid", selectedShop.getUid());
        chatIntent.putExtra("otherUsername", selectedShop.getName());
        chatIntent.putExtra("thisPhoto", picUrl);
        chatIntent.putExtra("otherPhoto", selectedShop.getProfilePicUrl());
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
            if (selectedDate.get(Calendar.YEAR) == now.get(Calendar.YEAR) && selectedDate.get(Calendar.MONTH) == now.get(Calendar.MONTH)
                    && selectedDate.get(Calendar.DAY_OF_MONTH) == now.get(Calendar.DAY_OF_MONTH) && selectedDate.get(Calendar.DAY_OF_WEEK) == now.get(Calendar.DAY_OF_WEEK))
            {
                long l = selectedDate.getTime().getTime();
                l += timeFormat.parse(finish).getTime();
                
                if (l < now.getTime().getTime())
                    return;
                
                Calendar plusHalf = Calendar.getInstance();
                plusHalf.setTime(now.getTime());
                
                if (plusHalf.get(Calendar.MINUTE) > 30)
                {
                    plusHalf.add(Calendar.HOUR, 1);
                    plusHalf.set(Calendar.MINUTE, 0);
                } else
                    plusHalf.set(Calendar.MINUTE, 30);
                
                start = timeFormat.format(plusHalf.getTime());
            }
            
            calStart.setTime(timeFormat.parse(start));
            
        } catch (Exception e)
        {
            e.printStackTrace();
            return;
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
            default:
                return null;
            
        }
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
            e.printStackTrace();
        }
        
        String thisUid = FirebaseAuth.getInstance().getUid();
        
        new Notification(selectedShop.getUid(), name, Notification.NOTIFICATION_RESERVATION, "", this);
        final ReservationFirestore reservationFirestore = new ReservationFirestore(selectedShop.getUid(), selectedShop.getName(),
                selectedShop.getProfilePicUrl(), thisUid, picUrl, name, selectedShop.getAddress(), fullDate.getTime());
        db.collection("reservations").add(reservationFirestore).addOnSuccessListener(new OnSuccessListener<DocumentReference>()
        {
            @Override
            public void onSuccess(DocumentReference documentReference)
            {
                db.collection("reservationsUpdate").document(reservationFirestore.getCustomerUid()).update("reservations", FieldValue.increment(1));
                db.collection("reservationsUpdate").document(reservationFirestore.getShopUid()).update("reservations", FieldValue.increment(1));
            }
        });
        
        Toast.makeText(this, getString(R.string.reservationCompleted), Toast.LENGTH_LONG).show();
    }
}
