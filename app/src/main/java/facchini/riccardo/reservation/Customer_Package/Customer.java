package facchini.riccardo.reservation.Customer_Package;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Map;

import facchini.riccardo.reservation.User;

/**
 * Customer class
 */
public class Customer extends User
{
    //region Customer.Constructors
    public Customer()
    {
        super();
    }
    
    public Customer(String uid, String name, String phone, String mail)
    {
        super(uid, name, phone, mail);
    }
    
    public Customer(String uid, String name)
    {
        super(uid, name);
    }
    
    public Customer(Map<String, Object> c)
    {
        super(c);
    }
    
    public Customer(User u)
    {
        super(u);
    }
    //endregion Customer.Constructors
    
    //region Customer.ParcelableMethods
    
    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        super.writeToParcel(dest, flags);
    }
    
    private Customer(Parcel in)
    {
        super(in);
    }
    
    
    public static final Parcelable.Creator<Customer> CREATOR = new Parcelable.Creator<Customer>()
    {
        @Override
        public Customer createFromParcel(Parcel source)
        {
            return new Customer(source);
        }
        
        @Override
        public Customer[] newArray(int size)
        {
            return new Customer[size];
        }
    };
    
    //endregion Customer.ParcelableMethods
    
    //region Customer.Display
    public String displayProfile()
    {
        return String.format("%s %s\nPhone: %s\nMail: %s", name, phone, mail);
    }
    
    public String displayInfo()
    {
        return String.format("Name: %s %s\nPhone: %s\nMail: %s", name, phone.isEmpty() ? "N/A" : phone, mail.isEmpty() ? "N/A" : mail);
    }
    //endregion Customer.Display
    
    //region Customer.FromToArrayList
    
    public ArrayList<String> toArrayList()
    {
        ArrayList<String> list = new ArrayList<>();
        list.add("CUSTOMER");
        list.addAll(super.toArrayList());
        return list;
    }
    
    public Customer(ArrayList<String> list)
    {
        super(list.get(0), list.get(1), list.get(2), list.get(3));
    }
    
    //endregion Customer.FromToArrayList
}
