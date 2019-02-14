package facchini.riccardo.reservation;

import java.util.Date;

public class Reservation_Customer_Home extends Reservation
{
    private Shop shop;
    
    public Reservation_Customer_Home(Date date, Shop shop)
    {
        super(date);
        this.shop = shop;
    }
    
    public Shop getShop() {return shop;}
}
