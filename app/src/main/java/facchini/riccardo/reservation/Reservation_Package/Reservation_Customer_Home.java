package facchini.riccardo.reservation;

import java.util.Date;

public class Reservation_Customer_Home extends Reservation
{
    private Shop shop;
    private String resUid;
    
    public Reservation_Customer_Home(Date date, Shop shop, String resUid)
    {
        super(date);
        this.shop = shop;
        this.resUid = resUid;
    }
    
    public Shop getShop() {return shop;}
    
    public String getResUid() {return resUid;}
}
