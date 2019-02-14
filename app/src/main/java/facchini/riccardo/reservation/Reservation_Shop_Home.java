package facchini.riccardo.reservation;

import java.util.Date;

public class Reservation_Shop_Home extends Reservation
{
    private Customer customer;
    
    public Reservation_Shop_Home(Date date, Customer customer)
    {
        super(date);
        this.customer = customer;
    }
    
    public Customer getCustomer() {return customer;}
}
