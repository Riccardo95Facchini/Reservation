package facchini.riccardo.reservation.Customer_Package.Fragment_Customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

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
import facchini.riccardo.reservation.Customer_Package.Activity_Customer.Activity_Customer_Create;
import facchini.riccardo.reservation.Customer_Package.Customer;
import facchini.riccardo.reservation.Info_Content;
import facchini.riccardo.reservation.R;

public class Fragment_Customer_Profile extends Fragment
{
    
    private CurrentUserViewModel viewModel;
    
    private ImageView profilePic;
    private RecyclerView recyclerView;
    
    private Adapter_CardInfo adapterCardInfo;
    private List<Info_Content> contents;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        getActivity().setTitle(R.string.profile);
        return inflater.inflate(R.layout.fragment_customer_profile, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        profilePic = view.findViewById(R.id.profilePic);
        recyclerView = view.findViewById(R.id.info);
        ImageButton buttonEdit = view.findViewById(R.id.buttonEdit);
    
        contents = new ArrayList<>();
        adapterCardInfo = new Adapter_CardInfo(getContext(), contents);
        recyclerView.setAdapter(adapterCardInfo);
        
        buttonEdit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                
                Customer current = (Customer) viewModel.getCurrentUser().getValue();
                
                Intent intent = new Intent(getContext(), Activity_Customer_Create.class);
                intent.putExtra("uid", current.getUid());
                intent.putExtra("name", current.getName());
                intent.putExtra("mail", current.getMail());
                intent.putExtra("phone", current.getPhone());
                intent.putExtra("profilePicUrl", current.getProfilePicUrl());
                intent.putExtra("editing", true);
                startActivity(intent);
            }
        });
    }
    
    
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity()).get(CurrentUserViewModel.class);
        Glide.with(this).load(viewModel.getCurrentUser().getValue().getProfilePicUrl()).placeholder(R.drawable.default_avatar).fitCenter().centerCrop().transform(new CircleCrop()).into(profilePic);
        
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        
        contents.addAll(viewModel.getCurrentUser().getValue().createInfoContentList());
    }
}
