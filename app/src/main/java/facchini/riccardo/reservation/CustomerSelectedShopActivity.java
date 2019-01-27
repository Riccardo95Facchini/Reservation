package facchini.riccardo.reservation;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class CustomerSelectedShopActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener
{
    //Firestore
    FirebaseFirestore db;
    CollectionReference reservationsCollection;
    Query reservationsQuery;
    
    private Shop selectedShop;
    private ArrayAdapter<String> adapter;
    
    private TextView shopNameText, shopInfoText, shopHoursText;
    private Button selectDateButton;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_selected_shop);
        
        db = FirebaseFirestore.getInstance();
        reservationsCollection = db.collection("reservations");
        
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
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
//        c.set(Calendar.HOUR, 0);
//        c.set(Calendar.MINUTE, 0);
//        c.set(Calendar.SECOND, 0);
//        c.set(Calendar.MILLISECOND, 0);
        
        //db.collection("reservations").document(selectedShop.getUid()).update(sdf.format(c.getTime()), "NUOVO ORARIO PRESO");
        checkHoursDate(c);
    }
    
    /**
     * Checks if in the given day there are already reservations and passes them to the method that creates the spinner
     *
     * @param c Calendar object with the date
     */
    private void checkHoursDate(final Calendar c)
    {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
        reservationsCollection.document(selectedShop.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
        {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot)
            {
                if (documentSnapshot.exists())
                    createSpinnerAdapter((ArrayList<String>) documentSnapshot.get(sdf.format(c.getTime())));
                else
                    createSpinnerAdapter(null);
            }
        });
    }
    
    private void createSpinnerAdapter(ArrayList<String> takenHours)
    {
        /*
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
        adapter = new ArrayAdapter<>(CustomerSelectedShopActivity.this, android.R.layout.simple_spinner_item, spinnerText);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        */
    }
    
    private void setSpinner(final String day)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(CustomerSelectedShopActivity.this);
        View view = getLayoutInflater().inflate(R.layout.alert_select_slot, null);
        builder.setTitle(day + " hours");
        final Spinner spinner = view.findViewById(R.id.spinner);
        spinner.setAdapter(adapter);
        
        builder.setPositiveButton("Set", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                int t1 = spinner.getSelectedItemPosition();
                
                ArrayList ret = new ArrayList<String>();
                ret.add(spinner.getSelectedItem().toString());
                
                //hours.put(day, ret);
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
}
