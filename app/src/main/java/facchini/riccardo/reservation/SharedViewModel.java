package facchini.riccardo.reservation;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.ArrayList;

public class SharedViewModel extends ViewModel
{
    private MutableLiveData<Shop> selectedShop = new MutableLiveData<>();
    private MutableLiveData<Customer> currentCustomer = new MutableLiveData<>();
    
    public void setSelectedShop(Shop s)
    {
        selectedShop.setValue(s);
    }
    
    public void setCurrentCustomer(Customer c) {currentCustomer.setValue(c);}
    
    public Shop getSelectedShop()
    {
        return selectedShop.getValue();
    }
    
    public Customer getCurrentCustomer() {return currentCustomer.getValue();}
    
    
}
