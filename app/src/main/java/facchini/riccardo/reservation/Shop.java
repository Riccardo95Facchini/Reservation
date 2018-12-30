package facchini.riccardo.reservation;

import java.util.ArrayList;

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
    
    public Shop(String uid, String name, String mail, String address1, String address2, String city, String zip, String phone, ArrayList<String> tags)
    {
        this.uid = uid;
        this.name = name;
        this.mail = mail;
        this.address1 = address1;
        this.address2 = address2;
        this.city = city;
        this.zip = zip;
        this.phone = phone;
        this.tags = new ArrayList<String>(tags);
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
}
