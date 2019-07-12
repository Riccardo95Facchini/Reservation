package facchini.riccardo.reservation;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

public class ImageUploader
{
    private Context context;
    private ImageView imageView;
    private ProgressBar uploadBar;
    private StorageReference profilePics;
    private String uid;
    private String profilePicUrl;
    private boolean editing, isCustomer;
    private FirebaseStorage db;
    
    public ImageUploader(Context context, ImageView imageView, ProgressBar uploadBar, String uid, String profilePicUrl, boolean editing, boolean isCustomer)
    {
        this.context = context;
        this.imageView = imageView;
        this.uploadBar = uploadBar;
        this.uid = uid;
        this.profilePicUrl = profilePicUrl;
        this.editing = editing;
        this.isCustomer = isCustomer;
        db = FirebaseStorage.getInstance();
        profilePics = db.getReference("profile_pics").child(uid);
    }
    
    public StorageTask upload(Uri image)
    {
        imageView.setClickable(false);
        imageView.setColorFilter(Color.argb(128, 255, 255, 255));
        uploadBar.setProgress(0);
        uploadBar.setVisibility(View.VISIBLE);
        
        if (profilePicUrl != null && !profilePicUrl.isEmpty())
            db.getReferenceFromUrl(profilePicUrl).delete();
        
        profilePicUrl = "";
        
        return profilePics.putFile(image)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                    {
                        profilePics.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
                        {
                            @Override
                            public void onSuccess(Uri uri)
                            {
                                profilePicUrl = uri.toString();
                                
                                if (editing)
                                {
                                    if (isCustomer)
                                        FirebaseFirestore.getInstance().collection("customers").document(uid).update("profilePicUrl", profilePicUrl);
                                    else
                                        FirebaseFirestore.getInstance().collection("shops").document(uid).update("profilePicUrl", profilePicUrl);
                                }
    
                                Glide.with(context).load(profilePicUrl).placeholder(R.drawable.default_avatar).fitCenter().centerCrop().transform(new CircleCrop()).into(imageView);
                                uploadBar.setVisibility(View.GONE);
                                imageView.setClickable(true);
                                imageView.setColorFilter(Color.argb(0, 0, 0, 0));
                            }
                        });
                        
                    }
                }).addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        imageView.setClickable(true);
                        imageView.setImageResource(R.drawable.default_avatar);
                        imageView.setColorFilter(Color.argb(0, 0, 0, 0));
                        uploadBar.setVisibility(View.GONE);
                        Toast.makeText(context, context.getString(R.string.upload_error), Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot)
                    {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        uploadBar.setProgress((int) progress);
                    }
                });
    }
    
    public String getProfilePicUrl() {return profilePicUrl;}
    
    /**
     * Uploads the default avatar in order to fix the resource link
     */
    public void uploadDefaultAvatar()
    {
        Uri defaultAvatar = Uri.parse("android.resource://" + R.class.getPackage().getName() + "/" + R.drawable.default_avatar);
        profilePics.putFile(defaultAvatar).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
        {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
            {
                profilePics.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
                {
                    @Override
                    public void onSuccess(Uri uri)
                    {
                        profilePicUrl = uri.toString();
                    }
                });
            }
        });
    }
}