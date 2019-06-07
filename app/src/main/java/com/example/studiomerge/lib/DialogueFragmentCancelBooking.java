package com.example.studiomerge.lib;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class DialogueFragmentCancelBooking extends DialogFragment {

    private static final String TAG = "CANCEL_BOOKING_FRAGMENT";

    CancelBookingDialogueListener listener;

    // Used to pass events to the calling class
    public interface CancelBookingDialogueListener {
        void onDialogPositiveClick(DialogFragment dialog);
        void onDialogNegativeClick(DialogFragment dialog);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Are you sure you want to cancel this booking?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDialogPositiveClick(DialogueFragmentCancelBooking.this);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        listener.onDialogNegativeClick(DialogueFragmentCancelBooking.this);
                    }
                });
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            // Instantiate the DialogListener so events can be sent to the host
            listener = (CancelBookingDialogueListener) context;
        } catch (ClassCastException e) {
            Log.e(TAG, "Class does not support dialogue.", e);
        }
    }
}
