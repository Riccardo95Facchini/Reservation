package facchini.riccardo.reservation.Shop_Package;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import facchini.riccardo.reservation.Info_Content;
import facchini.riccardo.reservation.R;
import facchini.riccardo.reservation.User;

/**
 * Shop class
 */
public class Shop extends User
{
    private String address;
    private String city;
    private String zip;
    private double latitude;
    private double longitude;
    private double averageReviews;
    private int numReviews;
    private int intLongitude;
    private ArrayList<String> tags;
    private Map<String, ArrayList<String>> hours;
    
    private final static SimpleDateFormat df = new SimpleDateFormat("HH:mm");
    
    //region Shop.Constructors
    
    public Shop()
    {
        super();
    }
    
    public Shop(String uid, String name, String phone, String mail, String pic, String address, String city, String zip,
                double latitude, double longitude, double averageReviews, int numReviews, int intLongitude,
                ArrayList<String> tags, Map<String, ArrayList<String>> hours)
    {
        super(uid, name, phone, mail, pic);
        this.address = address;
        this.city = city;
        this.zip = zip;
        this.latitude = latitude;
        this.longitude = longitude;
        this.averageReviews = averageReviews;
        this.numReviews = numReviews;
        this.intLongitude = intLongitude;
        this.tags = tags;
        this.hours = hours;
    }
    
    public Shop(Map<String, Object> m)
    {
        super(m);
        this.address = (String) m.get("address");
        this.city = (String) m.get("city");
        this.zip = (String) m.get("zip");
        this.latitude = (double) m.get("latitude");
        this.longitude = (double) m.get("longitude");
        
        try
        {
            this.averageReviews = (double) m.get("averageReviews");
        } catch (Exception e)
        {
            this.averageReviews = (long) m.get("averageReviews");
        }
        this.numReviews = (int) ((long) m.get("numReviews"));
        this.intLongitude = (int) ((long) m.get("intLongitude"));
        this.tags = new ArrayList<>((ArrayList<String>) m.get("tags"));
        this.hours = new HashMap<>((HashMap<String, ArrayList<String>>) m.get("hours"));
    }
    
    //endregion Shop.Constructors
    
    @Override
    public List<Info_Content> createInfoContentList()
    {
        List<Info_Content> contents = super.createInfoContentList();
        //contents.add(new Info_Content(R.drawable.ic_hours_color_32dp, displayHoursFormat()));
        contents.add(new Info_Content(R.drawable.ic_hours_color_32dp, hours));
        contents.add(new Info_Content(R.drawable.ic_location_on_primary_32dp, displayFullAddress(), name, new LatLng(latitude, longitude)));
        return contents;
    }
    
    //region Shop.ParcelableMethods
    
    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        super.writeToParcel(dest, flags);
        dest.writeString(address);
        dest.writeString(city);
        dest.writeString(zip);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeDouble(averageReviews);
        dest.writeInt(numReviews);
        dest.writeInt(intLongitude);
        dest.writeList(new ArrayList<>(tags));
        dest.writeMap(new HashMap<>(hours));
    }
    
    private Shop(Parcel in)
    {
        super(in);
        this.address = in.readString();
        this.city = in.readString();
        this.zip = in.readString();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.averageReviews = in.readDouble();
        this.numReviews = in.readInt();
        this.intLongitude = in.readInt();
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
    
    @Override
    public int describeContents() { return 0; }
    
    //endregion Shop.ParcelableMethods
    
    //region Shop.Display
    
    public String displayFullAddress()
    {
        return String.format("%s %s %s", address, city, zip);
    }
    
    public static void displayHoursInTextViews(TextView infoText, TextView infoText2, Map<String, ArrayList<String>> hours)
    {
        ArrayList<String> days = new ArrayList<>();
        days.add("Sunday");
        days.add("Monday");
        days.add("Tuesday");
        days.add("Wednesday");
        days.add("Thursday");
        days.add("Friday");
        days.add("Saturday");
        
        infoText.setText("");
        infoText2.setText("");
        infoText2.setVisibility(View.VISIBLE);
        
        for (String entry : days)
        {
            infoText.append(entry + ":\n");
            try
            {
                if (!hours.get(entry).get(0).equalsIgnoreCase("closed") && !hours.get(entry).get(2).equalsIgnoreCase("closed"))
                {
                    infoText2.append(String.format("%s-%s\n", hours.get(entry).get(0), hours.get(entry).get(1)));
                    infoText.append("\n");
                    infoText2.append(String.format("%s-%s\n", hours.get(entry).get(2), hours.get(entry).get(3)));
                } else if (!hours.get(entry).get(0).equalsIgnoreCase("closed"))
                    infoText2.append(String.format("%s-%s\n", hours.get(entry).get(0), hours.get(entry).get(1)));
                else if (!hours.get(entry).get(3).equalsIgnoreCase("closed"))
                    infoText2.append(String.format("%s-%s\n", hours.get(entry).get(2), hours.get(entry).get(3)));
                else
                    infoText2.append(String.format("%s\n", "Closed"));
            } catch (Exception e)
            {
                infoText2.append(String.format("%s\n", "Closed"));
            }
            infoText.append("\n");
            infoText2.append("\n");
        }
    }
    
    public String displayHoursDay(String day)
    {
        try
        {
            return String.format("%s-%s \t %s-%s", hours.get(day).get(0), hours.get(day).get(1), hours.get(day).get(2), hours.get(day).get(3));
        } catch (Exception e)
        {
            return "Closed-Closed \t Closed-Closed";
        }
    }
    
    //endregion Shop.Display
    
    //region Shop.Getters
    
    public String getAddress() {return address;}
    
    public String getCity() {return city;}
    
    public String getZip() {return zip;}
    
    public ArrayList<String> getTags() {return new ArrayList<>(tags);}
    
    public Map<String, ArrayList<String>> getHours() {return hours;}
    
    public double getLatitude() {return latitude;}
    
    public double getLongitude() {return longitude;}
    
    public double getAverageReviews() {return averageReviews;}
    
    public int getNumReviews() {return numReviews;}
    
    public int getIntLongitude() {return intLongitude;}
    
    //endregion Shop.Getters
    
    //region Shop.FromToArrayList
    
    @Override
    public ArrayList<String> toArrayList()
    {
        ArrayList<String> list = new ArrayList<>();
        list.add("SHOP");
        list.addAll(super.toArrayList());
        list.add(address);
        list.add(city);
        list.add(zip);
        list.add(String.valueOf(latitude));
        list.add(String.valueOf(longitude));
        list.add(String.valueOf(averageReviews));
        list.add(String.valueOf(numReviews));
        list.add(String.valueOf(intLongitude));
        list.add(new Gson().toJson(tags));
        list.add(new Gson().toJson(hours));
        return list;
    }
    
    public Shop(ArrayList<String> list)
    {
        super(list.get(0), list.get(1), list.get(2), list.get(3), list.get(4));
        this.address = list.get(4);
        this.city = list.get(5);
        this.zip = list.get(6);
        this.latitude = Double.parseDouble(list.get(7));
        this.longitude = Double.parseDouble(list.get(8));
        this.averageReviews = Double.parseDouble(list.get(9));
        this.numReviews = Integer.parseInt(list.get(10));
        this.intLongitude = Integer.parseInt(list.get(11));
        
        Type tagsListType = new TypeToken<ArrayList<String>>() {}.getType();
        Type hoursListType = new TypeToken<ArrayList<Long>>() {}.getType();
        this.tags = new Gson().fromJson(list.get(12), tagsListType);
        this.hours = new Gson().fromJson(list.get(13), hoursListType);
    }
    
    //endregion Shop.FromToArrayList
}
