package facchini.riccardo.reservation.Customer_Package.Fragment_Customer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import facchini.riccardo.reservation.CurrentUserViewModel;
import facchini.riccardo.reservation.Customer_Package.Customer;
import facchini.riccardo.reservation.R;

public class Fragment_Customer_Profile extends Fragment
{
    
    private CurrentUserViewModel viewModel;
    
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
        viewModel = ViewModelProviders.of(getActivity()).get(CurrentUserViewModel.class);
        profileInfoText.setText(((Customer) viewModel.getCurrentUser().getValue()).displayProfile());
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        profileInfoText = view.findViewById(R.id.profileInfoText);
    }
}
