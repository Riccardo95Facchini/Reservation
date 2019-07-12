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

import java.util.List;

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
        holder.infoText.setText(content.getResourceText());
        
        if (content.getLatLng() != null)
            holder.initializeMap(content.getLatLng(), content.getName());
    }
    
    class CardInfo_ViewHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback
    {
        ImageView infoPic;
        TextView infoText;
        MapView map;
        String name;
        private LatLng latLng;
        
        public CardInfo_ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            infoPic = itemView.findViewById(R.id.infoPic);
            infoText = itemView.findViewById(R.id.infoText);
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
    }
    
    @Override
    public int getItemCount()
    {
        return contents.size();
    }
}
