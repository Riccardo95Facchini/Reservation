package facchini.riccardo.reservation.Reservation_Package;

import java.text.SimpleDateFormat;

public class ReservationFirestore
{
    private String uid;
    
    private String shopUid;
    private String shopName;
    private String shopPic;
    
    private String customerUid;
    private String customerPic;
    private String customerName;
    
    private String where;
    private long time;
    
    public ReservationFirestore(String shopUid, String shopName, String shopPic, String customerUid, String customerPic, String customerName, String where, long time)
    {
        this.shopUid = shopUid;
        this.shopName = shopName;
        this.shopPic = shopPic;
        this.customerUid = customerUid;
        this.customerPic = customerPic;
        this.customerName = customerName;
        this.where = where;
        this.time = time;
    }
    
    /**
     * Constructor for customers screens
     *
     * @param uid
     * @param shopUid
     * @param shopName
     * @param shopPic
     * @param where
     * @param time
     */
    public ReservationFirestore(String uid, String shopUid, String shopName, String shopPic, String where, long time)
    {
        this.uid = uid;
        this.shopUid = shopUid;
        this.shopName = shopName;
        this.shopPic = shopPic;
        this.where = where;
        this.time = time;
    }
    
    /**
     * Constructor for shops
     *
     * @param customerUid
     * @param customerPic
     * @param customerName
     * @param time
     */
    public ReservationFirestore(String customerUid, String customerPic, String customerName, long time)
    {
        this.customerUid = customerUid;
        this.customerPic = customerPic;
        this.customerName = customerName;
        this.time = time;
    }
    
    //region Getters
    public String timeFormatted() { return new SimpleDateFormat("EEE, d MMM HH:mm").format(time); }
    
    public String getUid() {return uid;}
    
    public String getShopUid() {return shopUid;}
    
    public String getShopName() {return shopName;}
    
    public String getShopPic() {return shopPic;}
    
    public String getCustomerUid() {return customerUid;}
    
    public String getCustomerPic() {return customerPic;}
    
    public String getCustomerName() {return customerName;}
    
    public String getWhere() {return where;}
    
    public long getTime() {return time;}
    //endregion Getters
}
