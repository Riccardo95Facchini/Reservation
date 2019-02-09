package facchini.riccardo.reservation;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Shop implements Parcelable
{
    private String uid;
    private String name;
    private String mail;
    private String address1;
    private String address2;
    private String city;
    private String zip;
    private String phone;
    private ArrayList<String> tags;
    private Map<String, List<String>> hours;
    
    public Shop(String uid, String name, String mail, String address1, String address2, String city, String zip, String phone, ArrayList<String> tags, Map<String, List<String>> hours)
    {
        this.uid = uid;
        this.name = name;
        this.mail = mail;
        this.address1 = address1;
        this.address2 = address2;
        this.city = city;
        this.zip = zip;
        this.phone = phone;
        this.tags = new ArrayList<>(tags);
        this.hours = new HashMap<>(hours);
    }
    
    public Shop(Shop s)
    {
        this.uid = s.uid;
        this.name = s.name;
        this.mail = s.mail;
        this.address1 = s.address1;
        this.address2 = s.address2;
        this.city = s.city;
        this.zip = s.zip;
        this.phone = s.phone;
        this.tags = new ArrayList<>(s.tags);
        this.hours = new HashMap<>(s.hours);
    }
    
    public Shop(Map<String, Object> m)
    {
        this.uid = (String) m.get("uid");
        this.name = (String) m.get("name");
        this.mail = (String) m.get("mail");
        this.address1 = (String) m.get("address1");
        this.address2 = (String) m.get("address2");
        this.city = (String) m.get("city");
        this.zip = (String) m.get("zip");
        this.phone = (String) m.get("phone");
        this.tags = new ArrayList<>((ArrayList<String>) m.get("tags"));
        this.hours = new HashMap<>((HashMap<String, List<String>>) m.get("hours"));
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(uid);
        dest.writeString(name);
        dest.writeString(mail);
        dest.writeString(address1);
        dest.writeString(address2);
        dest.writeString(city);
        dest.writeString(zip);
        dest.writeString(phone);
        dest.writeList(new ArrayList<>(tags));
        dest.writeMap(new HashMap<>(hours));
    }
    
    private Shop(Parcel in)
    {
        this.uid = in.readString();
        this.name = in.readString();
        this.mail = in.readString();
        this.address1 = in.readString();
        this.address2 = in.readString();
        this.city = in.readString();
        this.zip = in.readString();
        this.phone = in.readString();
        this.tags = in.readArrayList(Shop.class.getClassLoader());
        this.hours = in.readHashMap(Shop.class.getClassLoader());
    }
    
    public static final Parcelable.Creator<Shop> CREATOR = new Parcelable.Creator<Shop>()
    {
        @Override
        public Shop createFromParcel(Parcel source)
        {
            return new Shop(source);
        }
        
        @Override
        public Shop[] newArray(int size)
        {
            return new Shop[size];
        }
    };
    
    public Shop()
    {
    
    }
    
    public String getInfo()
    {
        return String.format("%s\nCity: %s \tAddress: %s %s", name, city, address1, address2);
    }
    
    public String displayProfile()
    {
        return getInfo().concat("\n\nHours:\n").concat(getHoursFormat());
    }
    
    public String getHoursFormat()
    {
        StringBuilder h = new StringBuilder();
        
        for (Map.Entry<String, List<String>> entry : hours.entrySet())
        {
            if (!entry.getValue().get(0).equalsIgnoreCase("closed") && !entry.getValue().get(2).toLowerCase().equals("closed"))
                h.append(String.format("%s: \t %s-%s \t %s-%s\n", entry.getKey(),
                        entry.getValue().get(0), entry.getValue().get(1), entry.getValue().get(2), entry.getValue().get(3)));
            else if (!entry.getValue().get(0).equalsIgnoreCase("closed"))
                h.append(String.format("%s: \t %s-%s\n", entry.getKey(), entry.getValue().get(0), entry.getValue().get(1)));
            else if (!entry.getValue().get(3).equalsIgnoreCase("closed"))
                h.append(String.format("%s: \t %s-%s\n", entry.getKey(), entry.getValue().get(2), entry.getValue().get(3)));
        }
        return h.toString();
    }
    
    public String getPhone() {return phone;}
    
    public String getMail() {return mail;}
    
    public String getUid() {return uid;}
    
    public String getName() {return name;}
    
    public String getAddress1() {return address1;}
    
    public String getAddress2() {return address2;}
    
    public String getCity() {return city;}
    
    public String getZip() {return zip;}
    
    public ArrayList<String> getTags() {return new ArrayList<String>(tags);}
    
    public Map<String, List<String>> getHours() {return new HashMap<>(hours);}
    
    @Override
    public int describeContents()
    {
        return 0;
    }
}
