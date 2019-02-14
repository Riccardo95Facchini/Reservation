package facchini.riccardo.reservation;


import java.text.SimpleDateFormat;
import java.util.Date;

public class Reservation_Customer_Home
{
    private Shop shop;
    private Date date;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    private final SimpleDateFormat whenFormat = new SimpleDateFormat("EEE, d MMM HH:mm");
    
    public Reservation_Customer_Home(Shop shop, Date when)
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
    
    public String getWhen()
    {
        return whenFormat.format(date);
    }
}
