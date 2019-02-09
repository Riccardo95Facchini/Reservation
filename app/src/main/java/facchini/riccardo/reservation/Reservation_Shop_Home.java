package facchini.riccardo.reservation;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Reservation_Shop_Home
{
    private Customer customer;
    private Date date;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    
    public Reservation_Shop_Home(Customer customer, Date when)
    {
        this.customer = customer;
        this.date = when;
    }
    
    public Date getDate() {return date;}
    
    public Customer getCustomer() {return customer;}
    
    public String getInfo()
    {
        return String.format("%s    at: %s\n%s", dateFormat.format(date), timeFormat.format(date), customer.getInfo());
    }
}
