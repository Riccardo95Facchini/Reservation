package facchini.riccardo.reservation;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ReservationShop
{
    
    private String shopUid;
    private String customerUid;
    private String customerName;
    private Date time;
    //private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    
    public ReservationShop(String shopUid, String customerUid, String customerName, Date time)
    {
        this.shopUid = shopUid;
        this.customerUid = customerUid;
        this.customerName = customerName;
        this.time = time;
    }
    
//    public String getInfo()
//    {
//        String hours = date.getHours() >= 10 ? Integer.toString(date.getHours()) : "0".concat(Integer.toString(date.getHours()));
//        String minutes = date.getMinutes() != 0 ? Integer.toString(date.getMinutes()) : "00";
//
//        return String.format("%s    at: %s:%s\n%s", sdf.format(date), hours, minutes, shop.getInfo());
//    }
    
    public String getShopUid() {return shopUid;}
    
    public String getCustomerUid() {return customerUid;}
    
    public String getCustomerName() {return customerName;}
    
    public Date getTime() {return time;}
    
    //public SimpleDateFormat getSdf() {return sdf;}
    
    
}
