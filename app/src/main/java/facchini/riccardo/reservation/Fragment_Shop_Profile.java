package facchini.riccardo.reservation;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class Fragment_Shop_Profile extends Fragment
{
    
    private SharedViewModel viewModel;
    
    private TextView profileInfoText;
    
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
    }
}
