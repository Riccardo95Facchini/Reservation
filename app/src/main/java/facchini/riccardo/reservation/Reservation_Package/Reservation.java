package facchini.riccardo.reservation.Reservation_Package;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

import facchini.riccardo.reservation.User;

public class Reservation
{
    
    private String resUid;
    
    private Date date;
    private User otherUser;
    
    //private static final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM HH:mm");
    
    public Reservation(String resUid, Date date, User otherUser)
    {
        this.resUid = resUid;
        this.date = date;
        this.otherUser = otherUser;
    }
    
    public static Comparator<Reservation> reservationComparator = new Comparator<Reservation>()
    {
        @Override
        public int compare(Reservation o1, Reservation o2)
        {
            return o1.getDate().compareTo(o2.getDate());
        }
    };
    
    //region Reservation.Getters
    
    public String getResUid() {return resUid;}
    
    public Date getDate() {return date;}
    
    public User getOtherUser() {return otherUser;}
    
    public String getDateFormatted() { return new SimpleDateFormat("EEE, d MMM HH:mm").format(date); }
    
    //endregion Reservation.Getters
}
