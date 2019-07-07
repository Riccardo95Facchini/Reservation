package facchini.riccardo.reservation;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import facchini.riccardo.reservation.Customer_Package.Customer;
import facchini.riccardo.reservation.Shop_Package.Shop;

public class SharedViewModel extends ViewModel
{
    private MutableLiveData<Shop> currentShop = new MutableLiveData<>();
    private MutableLiveData<Customer> currentCustomer = new MutableLiveData<>();
    
    public void setCurrentShop(Shop s)
    {
        currentShop.setValue(s);
    }
    
    public void setCurrentCustomer(Customer c) {currentCustomer.setValue(c);}
    
    public Shop getCurrentShop()
    {
        return currentShop.getValue();
    }
    
    public Customer getCurrentCustomer() {return currentCustomer.getValue();}
    
    
}
