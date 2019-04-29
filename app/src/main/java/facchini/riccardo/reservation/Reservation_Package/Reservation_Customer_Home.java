package facchini.riccardo.reservation.Reservation_Package;

import java.util.Date;

import facchini.riccardo.reservation.Shop_Package.Shop;

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
