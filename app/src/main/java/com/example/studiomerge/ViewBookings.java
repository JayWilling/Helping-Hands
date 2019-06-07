package com.example.studiomerge;

import android.os.Bundle;

import com.example.studiomerge.lib.DialogueFragmentCancelBooking;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import android.view.View;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import com.example.studiomerge.lib.Constant;
import com.google.firebase.firestore.SetOptions;

public class ViewBookings extends AppCompatActivity
                          implements DialogueFragmentCancelBooking.CancelBookingDialogueListener {

    private static final String TAG = "VIEW_BOOKING";

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ArrayAdapter<String> dataAdapter;

    // Used to identify which booking in the ListView was clicked
    final List<String> bookingIDs = new ArrayList<>();
    final List<String> bookingStates = new ArrayList<>();
    int positionOfClickedBooking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_bookings);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference charityRef = db.collection("bookings");
        Query query = charityRef.whereEqualTo(
                "donorEmail",
                getIntent().getExtras().getString(Constant.USER_EMAIL)
        );

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    ArrayList<String> userBookings = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        userBookings.add(String.format("%s (%s)\n"
                                                       + "Charity: %s\n"
                                                       + "Time: %s",
                                document.get("date").toString(),
                                (
                                        document.get("state").toString().substring(0, 1).toUpperCase()
                                        + document.get("state").toString().substring(1)
                                ),
                                document.get("charityName").toString(),
                                document.get("time").toString()
                        ));
                        Log.d(TAG, "Added charity with ID: " + document.getId());

                        bookingIDs.add(document.getId());
                        bookingStates.add(document.get("state").toString());
                    }

                    // Link the Adapter to the ListView
                    ListView bookingsLv = (ListView)findViewById(R.id.bookingsLv);

                    dataAdapter = new ArrayAdapter<String>(
                            getApplicationContext(),
                            R.layout.simple_textview, userBookings
                    );
                    bookingsLv.setAdapter(dataAdapter);
                    bookingsLv.setTextFilterEnabled(true);
                }
            }
        });

        // Open the dialogue for the right booking when clicked
        ListView bookingsLv = findViewById(R.id.bookingsLv);
        bookingsLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (bookingStates.get(position).equals("active")) {
                    positionOfClickedBooking = position;

                    DialogueFragmentCancelBooking dialogueBox = new DialogueFragmentCancelBooking();
                    dialogueBox.show(getSupportFragmentManager(), "Cancel Booking");
                }
            }
        });
    }

    /**
     * Update the state of the given booking to cancelled.
     *
     * @param bookingID ID of the booking to be cancelled
     */
    public void cancelBooking(String bookingID) {
        Map<String, Object> data = new HashMap<>();
        data.put("state", "cancelled");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("bookings").document(bookingID)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Cancelled booking with ID: " + bookingIDs.get(positionOfClickedBooking));

                        // Reload the current screen
                        ViewBookings.this.recreate();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

    // Dialogue fragment
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's positive button
        Log.d(TAG, "bookingID: " + bookingIDs.get(positionOfClickedBooking));
        Log.d(TAG, "Position: " + positionOfClickedBooking);
        cancelBooking(bookingIDs.get(positionOfClickedBooking));
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button
    }
}
