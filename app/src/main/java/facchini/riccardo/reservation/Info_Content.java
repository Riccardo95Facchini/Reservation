package facchini.riccardo.reservation;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Map;

public class Info_Content
{
    private int resourceId;
    private String resourceText;
    private String name;
    private LatLng latLng;
    private Map<String, ArrayList<String>> hours;
    
    public Info_Content(int resourceId, String resourceText)
    {
        this.resourceId = resourceId;
        this.resourceText = resourceText;
        this.latLng = null;
        this.hours = null;
    }
    
    public Info_Content(int resourceId, String resourceText, String name, LatLng latLng)
    {
        this.resourceId = resourceId;
        this.resourceText = resourceText;
        this.name = name;
        this.latLng = latLng;
        this.hours = null;
    }
    
    public Info_Content(int resourceId, Map<String, ArrayList<String>> hours)
    {
        this.resourceId = resourceId;
        this.hours = hours;
        this.latLng = null;
    }
    
    public int getResourceId() {return resourceId;}
    
    public String getResourceText() {return resourceText;}
    
    public LatLng getLatLng() {return latLng;}
    
    public String getName() {return name;}
    
    public Map<String, ArrayList<String>> getHours() {return hours;}
}
