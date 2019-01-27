package facchini.riccardo.reservation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Shop
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
    
    public Shop()
    {
    
    }
    
    public String getInfo()
    {
        return String.format("%s\nCity: %s \tAddress: %s %s", name, city, address1, address2);
    }
    
    public String getHoursFormat()
    {
        StringBuilder h = new StringBuilder();
        
        for (Map.Entry<String, List<String>> entry : hours.entrySet())
        {
            if(!entry.getValue().get(0).equals("Closed") && !entry.getValue().get(2).equals("Closed"))
                h.append(String.format("%s: \t %s-%s \t %s-%s\n", entry.getKey(),
                    entry.getValue().get(0), entry.getValue().get(1), entry.getValue().get(2), entry.getValue().get(3)));
            else if(!entry.getValue().get(0).equals("Closed"))
                h.append(String.format("%s: \t %s-%s\n", entry.getKey(),entry.getValue().get(0), entry.getValue().get(1)));
            else if(!entry.getValue().get(3).equals("Closed"))
                h.append(String.format("%s: \t %s-%s\n", entry.getKey(),entry.getValue().get(2), entry.getValue().get(3)));
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
}
