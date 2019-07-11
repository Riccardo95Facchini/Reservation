package facchini.riccardo.reservation.Shop_Package.Activity_Shop;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import facchini.riccardo.reservation.CurrentUserViewModel;
import facchini.riccardo.reservation.R;
import facchini.riccardo.reservation.Shop_Package.Shop;

public class Fragment_Shop_Profile extends Fragment
{
    private Button buttonEdit;
    
    private TextView textShopName;
    private TextView textHours;
    private TextView textReviews;
    private TextView textPhoneMail;
    private TextView textAddress;
    //private ImageView profilePic;  TODO: profile pic
    private RatingBar ratingAvg;
    
    private CurrentUserViewModel viewModel;
    
    private Shop shop;
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        getActivity().setTitle(R.string.profile);
        textShopName = view.findViewById(R.id.textShopName);
        textHours = view.findViewById(R.id.textHours);
        textReviews = view.findViewById(R.id.textReviews);
        textPhoneMail = view.findViewById(R.id.textPhoneMail);
        textAddress = view.findViewById(R.id.textAddress);
        buttonEdit = view.findViewById(R.id.buttonEdit);
        //profilePic = view.findViewById(R.id.shopPic);  TODO: profile pic
        ratingAvg = view.findViewById(R.id.ratingAvg);
    }
    
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity()).get(CurrentUserViewModel.class);
        
        shop = (Shop) viewModel.getCurrentUser().getValue();
        fillProfile();
        
        buttonEdit.setOnClickListener(new View.OnClickListener()
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
    
    private void fillProfile()
    {
        textShopName.setText(shop.getName());
        textAddress.setText(String.format("%s %s %s", shop.getAddress(),
                shop.getCity(), shop.getZip()));
        textReviews.setText(String.format("(%.2f/5) %d %s", shop.getAverageReviews(), shop.getNumReviews(), getString(R.string.reviews)));
        textPhoneMail.setText(String.format("Phone: %s\nMail: %s", shop.getPhone(), shop.getMail()));
        textHours.setText(shop.displayHoursFormat());
        //shopPic.setImageResource();
        ratingAvg.setRating((float) shop.getAverageReviews());
    }
}
