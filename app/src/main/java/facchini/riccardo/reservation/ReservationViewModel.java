package facchini.riccardo.reservation;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import facchini.riccardo.reservation.Reservation_Package.Reservation;
import facchini.riccardo.reservation.Shop_Package.Shop;

public class ReservationViewModel extends ViewModel
{
    private MutableLiveData<List<Reservation>> nextReservations, pastReservations;
    private MutableLiveData<Boolean> isNextEmpty, isPastEmpty;
    private CollectionReference shopsCollection, reservationsCollection, updatesCollection;
    private String thisUid;
    FirebaseFirestore db;
    
    public ReservationViewModel()
    {
        db = FirebaseFirestore.getInstance();
        shopsCollection = db.collection("shops");
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
    }
    
    //region ReservationViewModel.Query
    private void queryNextReservations()
    {
        reservationsCollection.whereEqualTo("customerUid", thisUid).whereGreaterThan("time", Calendar.getInstance().getTime())/*.orderBy("time")*/
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
        reservationsCollection.whereEqualTo("customerUid", thisUid).whereLessThan("time", Calendar.getInstance().getTime()).orderBy("time")
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
    
    private void fillReservations(final QuerySnapshot snap, final MutableLiveData<List<Reservation>> liveData)
    {
        if (liveData.getValue() != null)
            liveData.getValue().clear();
        
        final List<Reservation> res = new ArrayList<>();
        
        for (final QueryDocumentSnapshot doc : snap)
        {
            shopsCollection.document((String) doc.get("shopUid")).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
            {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot)
                {
                    if (documentSnapshot.exists())
                        res.add(new Reservation(doc.getId(), ((Timestamp) doc.get("time")).toDate(),
                                documentSnapshot.toObject(Shop.class)));
                    
                    if (res.size() == snap.size())
                    {
                        liveData.setValue(res);
                    }
                }
            });
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
    
    public MutableLiveData<List<Reservation>> getNextReservations() {return nextReservations;}
    
    public MutableLiveData<List<Reservation>> getPastReservations() {return pastReservations;}
    
    public MutableLiveData<Boolean> getIsNextEmpty() {return isNextEmpty;}
    
    public MutableLiveData<Boolean> getIsPastEmpty() {return isPastEmpty;}
    
    //endregion ReservationViewModel.Getters
    
}
