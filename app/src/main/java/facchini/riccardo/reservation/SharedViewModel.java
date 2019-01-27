package facchini.riccardo.reservation;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.ArrayList;

public class SharedViewModel extends ViewModel
{
    private MutableLiveData<Shop> selectedShop = new MutableLiveData<>();
    //private MutableLiveData<ArrayList<Shop>> foundShops = new MutableLiveData<>();
    
    public void setSelectedShop(Shop s)
    {
        selectedShop.setValue(s);
    }
    
    //public void foundShops(ArrayList<Shop> s) { foundShops.setValue(s); }
    
    public Shop getSelectedShop()
    {
        return selectedShop.getValue();
    }
    
    //public ArrayList<Shop> getFoundShops() { return foundShops.getValue(); }
    
}
