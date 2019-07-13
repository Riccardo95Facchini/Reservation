package facchini.riccardo.reservation;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import facchini.riccardo.reservation.Reservation_Package.ReservationFirestore;

public class ReservationViewModel extends ViewModel
{
    private MutableLiveData<List<ReservationFirestore>> nextReservations, pastReservations;
    private MutableLiveData<Boolean> isNextEmpty, isPastEmpty;
    private CollectionReference customersCollection, shopsCollection, reservationsCollection, updatesCollection;
    private String thisUid;
    FirebaseFirestore db;
    
    int tag;
    
    public static final int CUSTOMER = 0, SHOP = 1;
    
    public ReservationViewModel()
    {
        tag = -1;
        db = FirebaseFirestore.getInstance();
        shopsCollection = db.collection("shops");
        customersCollection = db.collection("customers");
        reservationsCollection = db.collection("reservations");
        updatesCollection = db.collection("reservationsUpdate");
        thisUid = FirebaseAuth.getInstance().getUid();
        nextReservations = new MutableLiveData<>();
        pastReservations = new MutableLiveData<>();
        isNextEmpty = new MutableLiveData<>();
        isPastEmpty = new MutableLiveData<>();
        addFirestoreListener();
    }
    
    private void addFirestoreListener()
    {
        DocumentReference docRef = updatesCollection.document(thisUid);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>()
        {
            @Override
            public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException e)
            {
                if (e != null)
                    return;
                
                if (snapshot != null && snapshot.exists())
                    updateViewModel();
            }
        });
    }
    
    public void updateViewModel()
    {
        queryNextReservations();
        queryPastReservations();
        //fix();
    }
    
    private void fix()
    {
        reservationsCollection.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
        {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots)
            {
                for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments())
                {
                    final String uid = doc.getId();
                    reservationsCollection.document(uid).delete();
                }
            }
        });
    }
    
    //region ReservationViewModel.Query
    private void queryNextReservations()
    {
        String search = "";
        switch (tag)
        {
            case CUSTOMER:
                search = "customerUid";
                break;
            case SHOP:
                search = "shopUid";
                break;
        }
        
        reservationsCollection.whereEqualTo(search, thisUid).whereGreaterThan("time", Calendar.getInstance().getTime().getTime()).orderBy("time", Query.Direction.ASCENDING)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
        {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots)
            {
                fillReservations(queryDocumentSnapshots, nextReservations);
                
                if (queryDocumentSnapshots.isEmpty())
                    isNextEmpty.setValue(true);
                else
                    isNextEmpty.setValue(false);
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                e.printStackTrace();
            }
        });
    }
    
    private void queryPastReservations()
    {
        String search = "";
        switch (tag)
        {
            case CUSTOMER:
                search = "customerUid";
                break;
            case SHOP:
                search = "shopUid";
                break;
        }
        
        reservationsCollection.whereEqualTo(search, thisUid).whereLessThan("time", Calendar.getInstance().getTime().getTime()).orderBy("time", Query.Direction.DESCENDING)
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
        {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots)
            {
                fillReservations(queryDocumentSnapshots, pastReservations);
                
                if (queryDocumentSnapshots.isEmpty())
                    isPastEmpty.setValue(true);
                else
                    isPastEmpty.setValue(false);
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                e.printStackTrace();
            }
        });
    }
    
    private void fillReservations(QuerySnapshot snap, MutableLiveData<List<ReservationFirestore>> liveData)
    {
        if (liveData.getValue() != null)
            liveData.getValue().clear();
        
        switch (tag)
        {
            case CUSTOMER:
                fillWithShops(snap, liveData);
                break;
            case SHOP:
                fillWithCustomers(snap, liveData);
                break;
        }
        
        
    }
    
    private void fillWithShops(final QuerySnapshot snap, final MutableLiveData<List<ReservationFirestore>> liveData)
    {
        final List<ReservationFirestore> res = new ArrayList<>();
        
        for (final QueryDocumentSnapshot doc : snap)
        {
            Log.d("RES: ", String.valueOf(doc.getLong("time")));
            Log.d("RES: ", doc.getString("shopName"));
            res.add(new ReservationFirestore(doc.getId(), doc.getString("shopUid"), doc.getString("shopName"),
                    doc.getString("shopPic"), doc.getString("where"), doc.getLong("time")));
            
            if (res.size() == snap.size())
                liveData.setValue(res);
        }
    }
    
    private void fillWithCustomers(final QuerySnapshot snap, final MutableLiveData<List<ReservationFirestore>> liveData)
    {
        final List<ReservationFirestore> res = new ArrayList<>();
        
        for (final QueryDocumentSnapshot doc : snap)
        {
            res.add(new ReservationFirestore(doc.getString("customerUid"), doc.getString("customerPic"),
                    doc.getString("customerName"), doc.getLong("time")));
            
            if (res.size() == snap.size())
                liveData.setValue(res);
        }
    }
    
    public void delete(String resUid, String otherUid)
    {
        reservationsCollection.document(resUid).delete();
        updatesCollection.document(thisUid).update("reservations", FieldValue.increment(-1));
        updatesCollection.document(otherUid).update("reservations", FieldValue.increment(-1));
    }
    
    //endregion ReservationViewModel.Query
    
    //region ReservationViewModel.Getters
    
    public MutableLiveData<List<ReservationFirestore>> getNextReservations() { return nextReservations; }
    
    public MutableLiveData<List<ReservationFirestore>> getPastReservations() { return pastReservations; }
    
    public MutableLiveData<Boolean> getIsNextEmpty() {return isNextEmpty;}
    
    public MutableLiveData<Boolean> getIsPastEmpty() {return isPastEmpty;}
    
    //endregion ReservationViewModel.Getters
    
    
    public void setTag(int tag)
    {
        this.tag = tag;
    }
}
