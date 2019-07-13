package facchini.riccardo.reservation;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RatingBar;

import androidx.annotation.NonNull;

public class RatingDialog extends Dialog
{
    private RatingBar rating;
    
    public RatingDialog(@NonNull Context context, int themeResId, float pastRating)
    {
        super(context, themeResId);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_rating, null);
        this.setContentView(view);
        this.setTitle(context.getString(R.string.rating));
        rating = this.findViewById(R.id.rating);
    
        if (pastRating > 0)
            rating.setRating(pastRating);
        
        this.setCancelable(false);
    }
    
    public RatingBar getRating() {return rating;}
    
    @Override
    public void onBackPressed()
    {
        this.dismiss();
    }
}
