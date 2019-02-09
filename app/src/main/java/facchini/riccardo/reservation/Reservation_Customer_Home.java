package facchini.riccardo.reservation;


import java.text.SimpleDateFormat;
import java.util.Date;

public class ReservationCustomer
{
    private Shop shop;
    private Date date;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    
    public ReservationCustomer(Shop shop, Date when)
    {
        this.shop = shop;
        this.date = when;
    }
    
    public Date getDate() {return date;}
    
    public Shop getShop() {return shop;}
    
    public String getInfo()
    {
        return String.format("%s    at: %s\n%s", dateFormat.format(date), timeFormat.format(date), shop.getInfo());
    }
}
