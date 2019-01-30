package facchini.riccardo.reservation;


import java.text.SimpleDateFormat;
import java.util.Date;

public class Reservation
{
    private Shop shop;
    private Date when;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    
    public Reservation(Shop shop, Date when)
    {
        this.shop = shop;
        this.when = when;
    }
    
    public Date getWhen() {return when;}
    
    public Shop getShop() {return shop;}
    
    public String getInfo()
    {
        String hours = when.getHours() >= 10 ? Integer.toString(when.getHours()) : "0".concat(Integer.toString(when.getHours()));
        String minutes = when.getMinutes() != 0 ? Integer.toString(when.getMinutes()) : "00";
        
        return String.format("%s    at: %s:%s\n%s", sdf.format(when), hours, minutes, shop.getInfo());
    }
}
