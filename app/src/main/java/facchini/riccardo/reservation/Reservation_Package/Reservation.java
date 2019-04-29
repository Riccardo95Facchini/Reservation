package facchini.riccardo.reservation.Reservation_Package;

import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class Reservation
{
    protected Date date;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM HH:mm");
    
    public Reservation(Date date) {this.date = date;}
    
    public String getDateFormatted() { return dateFormat.format(date); }
    
    public Date getDate() {return date;}
}
