package facchini.riccardo.reservation.Shop_Package.Fragment_Shop;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import facchini.riccardo.reservation.R;
import facchini.riccardo.reservation.SharedViewModel;
import facchini.riccardo.reservation.Shop_Package.Shop;
import facchini.riccardo.reservation.Shop_Package.Activity_Shop.Activity_Shop_Create;

public class Fragment_Shop_Profile extends Fragment
{
    
    private SharedViewModel viewModel;
    
    private TextView profileInfoText;
    private Button buttonEdit;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        getActivity().setTitle(R.string.profile);
        return inflater.inflate(R.layout.fragment_shop_profile, container, false);
    }
    
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        profileInfoText.setText(viewModel.getCurrentShop().displayProfile());
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        profileInfoText = view.findViewById(R.id.profileInfoText);
        buttonEdit = view.findViewById(R.id.buttonEdit);
        
        buttonEdit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Shop currentShop = viewModel.getCurrentShop();
                getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(null);
                Intent intent = new Intent();
                Bundle b = new Bundle();
                b.putParcelable("CurrentShop", currentShop);
                intent.putExtras(b);
                intent.setClass(getContext(), Activity_Shop_Create.class);
                startActivity(intent);
            }
        });
    }
}
