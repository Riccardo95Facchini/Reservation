package facchini.riccardo.reservation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Shop
{
    private String uid;
    private String name;
    private String address1;
    private String address2;
    private String city;
    private String zip;
    private ArrayList<String> tags;
    
    public Shop(String uid, String name, String address1, String address2, String city, String zip, ArrayList<String> tags)
    {
        this.uid = uid;
        this.name = name;
        this.address1 = address1;
        this.address2 = address2;
        this.city = city;
        this.zip = zip;
        this.tags = new ArrayList<String>(tags);
    }
    
    public String getUid() {return uid;}
    
    public String getName() {return name;}
    
    public String getAddress1() {return address1;}
    
    public String getAddress2() {return address2;}
    
    public String getCity() {return city;}
    
    public String getZip() {return zip;}
    
    public ArrayList<String> getTags() {return new ArrayList<String>(tags);}
}
