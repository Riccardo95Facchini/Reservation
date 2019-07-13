package facchini.riccardo.reservation.Shop_Package.Fragment_Shop;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

import java.util.ArrayList;
import java.util.List;

import facchini.riccardo.reservation.Adapter_CardInfo;
import facchini.riccardo.reservation.CurrentUserViewModel;
import facchini.riccardo.reservation.Info_Content;
import facchini.riccardo.reservation.R;
import facchini.riccardo.reservation.Shop_Package.Activity_Shop.Activity_Shop_Create;
import facchini.riccardo.reservation.Shop_Package.Shop;

public class Fragment_Shop_Profile extends Fragment
{
    private ImageView profilePic;
    private RatingBar ratingAvg;
    private RecyclerView recyclerView;
    private TextView textReviews;
    
    private Adapter_CardInfo adapterCardInfo;
    private List<Info_Content> contents;
    
    private CurrentUserViewModel viewModel;
    
    private Shop shop;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        getActivity().setTitle(R.string.profile);
        return inflater.inflate(R.layout.fragment_shop_profile, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        profilePic = view.findViewById(R.id.profilePic);
        textReviews = view.findViewById(R.id.textReviews);
        ratingAvg = view.findViewById(R.id.ratingAvg);
        recyclerView = view.findViewById(R.id.info);
        ImageButton buttonAction = view.findViewById(R.id.buttonAction);
        
        contents = new ArrayList<>();
        adapterCardInfo = new Adapter_CardInfo(getContext(), contents);
        recyclerView.setAdapter(adapterCardInfo);
    
    
        buttonAction.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent();
                Bundle b = new Bundle();
                b.putParcelable("CurrentShop", shop);
                intent.putExtras(b);
                intent.setClass(getContext(), Activity_Shop_Create.class);
                startActivity(intent);
            }
        });
    }
    
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity()).get(CurrentUserViewModel.class);
        
        shop = (Shop) viewModel.getCurrentUser().getValue();
        
        Glide.with(this).load(shop.getProfilePicUrl()).placeholder(R.drawable.default_avatar).fitCenter().centerCrop().transform(new CircleCrop()).into(profilePic);
        
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        textReviews.setText(String.format("(%.2f/5) %d %s", shop.getAverageReviews(), shop.getNumReviews(), getString(R.string.reviews)));
        ratingAvg.setRating((float) shop.getAverageReviews());
        contents.addAll(viewModel.getCurrentUser().getValue().createInfoContentList());
        
    }
}
