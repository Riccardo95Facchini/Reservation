package facchini.riccardo.reservation;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import facchini.riccardo.reservation.Customer_Package.Customer;
import facchini.riccardo.reservation.Shop_Package.Shop;

public class CurrentUserViewModel extends ViewModel
{
    private MutableLiveData<User> currentUser;
    private MutableLiveData<Shop> selectedShop;
    private CollectionReference userCollection;
    
    private String thisUid;
    
    private int tag;
    
    public static final int CUSTOMER = 0, SHOP = 1;
    
    public CurrentUserViewModel()
    {
        tag = -1;
        currentUser = new MutableLiveData<>();
        selectedShop = new MutableLiveData<>();
        thisUid = FirebaseAuth.getInstance().getUid();
    }
    
    private void addFirestoreListener()
    {
        DocumentReference docRef = userCollection.document(thisUid);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>()
        {
            @Override
            public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException e)
            {
                if (e != null)
                    return;
                
                if (snapshot != null && snapshot.exists())
                    queryUser();
            }
        });
    }
    
    private void queryUser()
    {
        userCollection.document(thisUid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
        {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot)
            {
                User user;
                if (documentSnapshot.exists())
                {
                    user = tag == SHOP ? new Shop(documentSnapshot.getData()) : new Customer(documentSnapshot.getData());
                    currentUser.setValue(user);
                }
            }
        });
    }
    
    
    //region ReservationViewModel.Getters
    
    public MutableLiveData<User> getCurrentUser() {return currentUser;}
    
    public MutableLiveData<Shop> getSelectedShop() {return selectedShop;}
    
    public int getTag() {return tag;}
    
    //endregion ReservationViewModel.Getters
    
    //region ReservationViewModel.Setters
    
    public void setSelectedShop(Shop selectedShop) {
        this.selectedShop.setValue(selectedShop); }
    
    public void setTag(int tag)
    {
        this.tag = tag;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        switch (tag)
        {
            case CUSTOMER:
                userCollection = db.collection("customers");
                break;
            case SHOP:
                userCollection = db.collection("shops");
                break;
        }
        addFirestoreListener();
    }
    
    //endregionReservationViewModel.Setters
}
