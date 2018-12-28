package facchini.riccardo.reservation;

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
    
    public String getUid() {return uid;}
    
    public String getName() {return name;}
    
    public String getSurname() {return surname;}
    
    public String getPhone() {return phone;}
    
    public String getMail() {return mail;}
}
