package facchini.riccardo.reservation.Reservation_Package;

import java.util.Date;

import facchini.riccardo.reservation.Customer_Package.Customer;
import facchini.riccardo.reservation.Reservation_Package.Reservation;

public class Reservation_Shop_Home extends Reservation
{
    private Customer customer;
    
    public Reservation_Shop_Home(Date date, Customer customer)
    {
        super(date);
        this.customer = customer;
    }
    
    public Customer getCustomer() {return customer;}
    
    public String getInfo()
    {
        return String.format("%s    at: %s\n%s", date.toString(), date.toString(), customer.displayInfo());
    }
}
