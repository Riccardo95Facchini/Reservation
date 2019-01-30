package facchini.riccardo.reservation;

import java.util.Calendar;

public class Reservation
{
    private Calendar date;
    private Shop shop;
    
    public Reservation(Calendar date, Shop shop)
    {
        this.date = date;
        this.shop = shop;
    }
    
    public Calendar getDate() {return date;}
    
    public Shop getShop() {return shop;}
}
