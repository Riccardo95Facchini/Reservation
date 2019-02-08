package facchini.riccardo.reservation;

import java.util.List;
import java.util.Map;

public class Customer
{
    private String uid;
    private String name;
    private String surname;
    private String phone;
    private String mail;
    
    
    public Customer(String uid, String name, String surname, String phone, String mail)
    {
        this.uid = uid;
        this.name = name;
        this.surname = surname;
        this.phone = phone;
        this.mail = mail;
    }
    
    public Customer(Map<String, Object> c)
    {
        this.uid = (String) c.get("uid");
        this.name = (String) c.get("name");
        this.surname = (String) c.get("surname");
        this.phone = (String) c.get("phone");
        this.mail = (String) c.get("mail");
    }
    
    public String displayProfile()
    {
        return String.format("%s %s\nPhone: %s\nMail: %s", name, surname, phone, mail);
    }
    
    public String getUid() {return uid;}
    
    public String getName() {return name;}
    
    public String getSurname() {return surname;}
    
    public String getPhone() {return phone;}
    
    public String getMail() {return mail;}
}
