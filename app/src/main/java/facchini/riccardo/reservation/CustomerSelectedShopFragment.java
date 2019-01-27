package facchini.riccardo.reservation;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.os.Debug;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class CustomerSelectedShopFragment extends Fragment
{
    private SharedViewModel viewModel;
    private Shop selectedShop;
    
    private TextView shopNameText, shopInfoText, shopHoursText;
    private Button selectDateButton;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        getActivity().setTitle("");
        return inflater.inflate(R.layout.fragment_customer_selected_shop, container, false);
    }
    
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        try
        {
            
            viewModel = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
            selectedShop = viewModel.getSelectedShop();
            shopNameText = view.findViewById(R.id.shopNameText);
            shopInfoText = view.findViewById(R.id.shopInfoText);
            shopHoursText = view.findViewById(R.id.shopHoursText);
            selectDateButton = view.findViewById(R.id.selectDateButton);
            
            shopNameText.setText(selectedShop.getName());
            shopInfoText.setText(String.format("City: %s \tAddress: %s %s", selectedShop.getCity(),
                    selectedShop.getAddress1(), selectedShop.getAddress2()));
            shopHoursText.setText(selectedShop.getHoursFormat());
        } catch (Exception e)
        {
            Log.d("ECCEZIONE", e.getMessage());
        }
    }
    
}
