package com.example.studiomerge.lib;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class DialogueFragmentMakeBooking extends DialogFragment {

    private static final String TAG = "MAKE_BOOKING_FRAGMENT";

    MakeBookingDialogueListener listener;

    // Used to pass events to the calling class
    public interface MakeBookingDialogueListener {
        void onDialogPositiveClick(DialogFragment dialog);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Your booking has been made.")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDialogPositiveClick(DialogueFragmentMakeBooking.this);
                    }
                });
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            // Instantiate the DialogListener so events can be sent to the host
            listener = (MakeBookingDialogueListener) context;
        } catch (ClassCastException e) {
            Log.e(TAG, "Class does not support dialogue.", e);
        }
    }
}
