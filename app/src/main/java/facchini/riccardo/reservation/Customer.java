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
    private List<Map<String, String>> customerReservations;
    
    
    public Customer(String uid, String name, String surname, String phone, String mail, List<Map<String, String>> customerReservations)
    {
        this.uid = uid;
        this.name = name;
        this.surname = surname;
        this.phone = phone;
        this.mail = mail;
        this.customerReservations = customerReservations;
    }
    
    public String getUid() {return uid;}
    
    public String getName() {return name;}
    
    public String getSurname() {return surname;}
    
    public String getPhone() {return phone;}
    
    public String getMail() {return mail;}
    
    public List<Map<String, String>> getCustomerReservations() {return customerReservations;}
}
