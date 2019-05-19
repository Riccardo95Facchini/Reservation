package facchini.riccardo.reservation.Chat;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatEntry implements Comparable<ChatEntry>
{
    private String otherUid_Name;
    private Date lastMsg;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM HH:mm");
    
    public ChatEntry(String otherUid, Date lastMsg)
    {
        this.otherUid_Name = otherUid;
        this.lastMsg = lastMsg;
    }
    
    public String getOtherUid_Name() {return otherUid_Name;}
    
    public String getOtherUid() {return otherUid_Name.substring(0, otherUid_Name.indexOf('_'));}
    
    public String getOtherName(){return otherUid_Name.substring(otherUid_Name.indexOf('_'));}
    
    public Date getLastMsg() {return lastMsg;}
    
    public String getDateFormatted() { return dateFormat.format(lastMsg); }
    
    public void setLastMsg(Date lastMsg)
    {
        this.lastMsg = lastMsg;
    }
    
    @Override
    public int compareTo(ChatEntry o)
    {
        return this.lastMsg.compareTo(o.lastMsg);
    }
    
    /*
    *  //1. Employee ids in ascending order
        Collections.sort(employees);
        
        System.out.println(employees);
        
        //2. Employee ids in reverse order
        Collections.sort(employees, Collections.reverseOrder());
    *
    * */
}
