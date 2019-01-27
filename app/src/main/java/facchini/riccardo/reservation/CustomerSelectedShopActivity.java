package facchini.riccardo.reservation;

import android.app.DatePickerDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import java.util.Calendar;

public class CustomerSelectedShopActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener
{
    
    private Shop selectedShop;
    
    private TextView shopNameText, shopInfoText, shopHoursText;
    private Button selectDateButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_selected_shop);
        
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
    }
}
