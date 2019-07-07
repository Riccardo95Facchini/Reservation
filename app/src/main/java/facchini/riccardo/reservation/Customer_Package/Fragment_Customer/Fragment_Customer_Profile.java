package facchini.riccardo.reservation.Customer_Package.Fragment_Customer;

import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import facchini.riccardo.reservation.R;
import facchini.riccardo.reservation.SharedViewModel;

public class Fragment_Customer_Profile extends Fragment
{
    
    private SharedViewModel viewModel;
    
    private TextView profileInfoText;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        getActivity().setTitle(R.string.profile);
        return inflater.inflate(R.layout.fragment_customer_profile, container, false);
    }
    
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        profileInfoText.setText(viewModel.getCurrentCustomer().displayProfile());
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        profileInfoText = view.findViewById(R.id.profileInfoText);
    }
}
