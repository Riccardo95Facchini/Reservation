package facchini.riccardo.reservation;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class Fragment_TimePicker extends DialogFragment
{
    @NonNull
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
        return new TimePickerDialog(getActivity(), (TimePickerDialog.OnTimeSetListener) getActivity(), 0, 0, true);
    }
}
