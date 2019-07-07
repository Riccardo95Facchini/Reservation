package facchini.riccardo.reservation;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;

import facchini.riccardo.reservation.Customer_Package.Customer;
import facchini.riccardo.reservation.Shop_Package.Shop;

public class Converters
{
    public static Date fromTimestamp(Long value)
    {
        return value == null ? null : new Date(value);
    }
    
    public static Long dateToTimestamp(Date date)
    {
        return date == null ? null : date.getTime();
    }
    
    
    public static String convertFromUser(User user)
    {
        
        if (user == null)
            return null;
        
        Gson gson = new Gson();
        return gson.toJson(user.toArrayList());

//        if (user.getClass() == Customer.class)
//            return convertFromCustomer((Customer) user);
//        else if (user.getClass() == Shop.class)
//            return convertFromShop((Shop) user);
//
//        return null;
    }
    
    public static User convertToUser(String value)
    {
        Type listType = new TypeToken<ArrayList<String>>() {}.getType();
        ArrayList<String> list = new Gson().fromJson(value, listType);
        
        if (list.get(0).equals("CUSTOMER"))
        {
            list.remove(0);
            return new Customer(list);
        } else
        {
            list.remove(0);
            return new Shop(list);
        }
    }

//    private static String convertFromCustomer(Customer customer)
//    {
//        Gson gson = new Gson();
//        return gson.toJson(customer.toArrayList());
//    }
//
//    private static String convertFromShop(Shop shop)
//    {
//        Gson gson = new Gson();
//        return gson.toJson(shop.toArrayList());
//    }
}
