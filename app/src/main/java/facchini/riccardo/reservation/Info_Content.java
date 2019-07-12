package facchini.riccardo.reservation;

import com.google.android.gms.maps.model.LatLng;

public class Info_Content
{
    private int resourceId;
    private String resourceText;
    private String name;
    private LatLng latLng;
    
    public Info_Content(int resourceId, String resourceText)
    {
        this.resourceId = resourceId;
        this.resourceText = resourceText;
        this.latLng = null;
    }
    
    public Info_Content(int resourceId, String resourceText, String name, LatLng latLng)
    {
        this.resourceId = resourceId;
        this.resourceText = resourceText;
        this.name = name;
        this.latLng = latLng;
    }
    
    public int getResourceId() {return resourceId;}
    
    public String getResourceText() {return resourceText;}
    
    public LatLng getLatLng() {return latLng;}
    
    public String getName() {return name;}
}
