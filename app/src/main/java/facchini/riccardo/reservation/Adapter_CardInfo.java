package facchini.riccardo.reservation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import facchini.riccardo.reservation.Shop_Package.Shop;

public class Adapter_CardInfo extends RecyclerView.Adapter<Adapter_CardInfo.CardInfo_ViewHolder>
{
    private Context context;
    private List<Info_Content> contents;
    
    public Adapter_CardInfo(Context context, List<Info_Content> contents)
    {
        this.context = context;
        this.contents = contents;
    }
    
    @NonNull
    @Override
    public CardInfo_ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.card_info, null);
        return new CardInfo_ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CardInfo_ViewHolder holder, int pos)
    {
        Info_Content content = contents.get(pos);
        
        holder.infoPic.setImageResource(content.getResourceId());
        
        if (content.getHours() == null)
            holder.infoText.setText(content.getResourceText());
        else
            Shop.displayHoursInTextViews(holder.infoText, holder.infoText2, content.getHours());
            //holder.setHours(content.getHours());
        
        
        if (content.getLatLng() != null)
            holder.initializeMap(content.getLatLng(), content.getName());
    }
    
    class CardInfo_ViewHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback
    {
        ImageView infoPic;
        TextView infoText, infoText2;
        MapView map;
        String name;
        private LatLng latLng;
        
        CardInfo_ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            infoPic = itemView.findViewById(R.id.infoPic);
            infoText = itemView.findViewById(R.id.infoText);
            infoText2 = itemView.findViewById(R.id.infoText2);
            map = itemView.findViewById(R.id.map);
        }
        
        void initializeMap(LatLng latLng, String name)
        {
            this.latLng = latLng;
            this.name = name;
            map.onCreate(null);
            map.onResume();
            map.getMapAsync(this);
        }
        
        @Override
        public void onMapReady(GoogleMap googleMap)
        {
            map.setVisibility(View.VISIBLE);
            MapsInitializer.initialize(context);
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
            MarkerOptions options = new MarkerOptions().position(latLng).draggable(false).title(name);
            googleMap.addMarker(options).showInfoWindow();
        }
        
        void setHours(Map<String, ArrayList<String>> hours)
        {
            infoText.setText("");
            infoText2.setText("");
            infoText2.setVisibility(View.VISIBLE);
            
            for (Map.Entry<String, ArrayList<String>> entry : hours.entrySet())
            {
                infoText.append(entry.getKey() + ":\n");
                try
                {
                    if (!entry.getValue().get(0).equalsIgnoreCase("closed") && !entry.getValue().get(2).equalsIgnoreCase("closed"))
                    {
                        infoText2.append(String.format("%s-%s\n", entry.getValue().get(0), entry.getValue().get(1)));
                        infoText.append("\n");
                        infoText2.append(String.format("%s-%s\n", entry.getValue().get(2), entry.getValue().get(3)));
                    } else if (!entry.getValue().get(0).equalsIgnoreCase("closed"))
                        infoText2.append(String.format("%s-%s\n", entry.getValue().get(0), entry.getValue().get(1)));
                    else if (!entry.getValue().get(3).equalsIgnoreCase("closed"))
                        infoText2.append(String.format("%s-%s\n", entry.getValue().get(2), entry.getValue().get(3)));
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
    }
    
    @Override
    public int getItemCount()
    {
        return contents.size();
    }
}
