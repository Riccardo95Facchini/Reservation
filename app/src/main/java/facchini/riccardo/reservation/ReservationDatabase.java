package facchini.riccardo.reservation;

import java.util.Date;

public class ReservationDatabase
{
    
    private String shopUid;
    private String customerUid;
    private String customerName;
    private Date time;
    
    public ReservationDatabase(String shopUid, String customerUid, String customerName, Date time)
    {
        this.shopUid = shopUid;
        this.customerUid = customerUid;
        this.customerName = customerName;
        this.time = time;
    }
    
    
    public String getShopUid() {return shopUid;}
    
    public String getCustomerUid() {return customerUid;}
    
    public String getCustomerName() {return customerName;}
    
    public Date getTime() {return time;}
}
