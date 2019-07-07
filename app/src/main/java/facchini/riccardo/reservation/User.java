package facchini.riccardo.reservation;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Map;

/**
 * Base class for users
 */
public abstract class User implements Parcelable
{
    protected String uid;
    protected String name;
    protected String phone;
    protected String mail;
    
    //region User.Constructors
    
    /**
     * Empty constructor for internal use
     */
    protected User() { }
    
    /**
     * Default constructor
     *
     * @param uid   User uid
     * @param name  Name to be displayed (name & surname for customers)
     * @param phone Phone number
     * @param mail  E-mail address
     */
    public User(String uid, String name, String phone, String mail)
    {
        this.uid = uid;
        this.name = name;
        this.phone = phone;
        this.mail = mail;
    }
    
    /**
     * Shorter constructor
     *
     * @param name Name to be displayed (name & surname for customers)
     */
    public User(String uid, String name)
    {
        this.uid = uid;
        this.name = name;
        this.phone = "";
        this.mail = "";
    }
    
    public User(Map<String, Object> c)
    {
        this.uid = (String) c.get("uid");
        this.name = (String) c.get("name");
        this.phone = (String) c.get("phone");
        this.mail = (String) c.get("mail");
    }
    
    public User(User u)
    {
        this.uid = u.uid;
        this.name = u.name;
        this.phone = u.phone;
        this.mail = u.mail;
    }
    
    //endregion User.Constructors
    
    // region User.Getters
    
    public String getUid() {return uid;}
    
    public String getName() {return name;}
    
    public String getPhone() {return phone;}
    
    public String getMail() {return mail;}
    
    // endregion User.Getters
    
    //region User.ParcelableMethods
    
    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(uid);
        dest.writeString(name);
        dest.writeString(mail);
        dest.writeString(phone);
    }
    
    protected User(Parcel in)
    {
        this.uid = in.readString();
        this.name = in.readString();
        this.mail = in.readString();
        this.phone = in.readString();
    }
    
    @Override
    public int describeContents()
    {
        return 0;
    }
    
    //endregion User.ParcelableMethods
    
    //region User.FromToArrayList
    
    protected ArrayList<String> toArrayList()
    {
        ArrayList<String> list = new ArrayList<>();
        list.add(uid);
        list.add(name);
        list.add(phone);
        list.add(mail);
        return list;
    }
    
    //endregion User.FromToArrayList
    
}