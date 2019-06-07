package com.example.studiomerge;

import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.studiomerge.lib.Constant;
import com.example.studiomerge.lib.FloatingActionButton_;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class ViewCalendar extends AppCompatActivity {

    private static final String TAG = "VIEW_CALENDAR";

    private ArrayAdapter<String> dataAdapter;
    private ArrayList<String> bookings;
    private String date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar);

        // CalendarView
        CalendarView calendar = findViewById(R.id.calendarCV);
        calendar.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year,
                                            int month, int dayOfMonth) {
                // Offset month by 1 since January is month 0
                date = String.format("%d/%d/%d", dayOfMonth, month + 1, year);
                Log.d(TAG, "Changed to date: " + date);

                getBookings();
            }
        });

        // FloatingActionButton
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new FloatingActionButton_().onClick(view);
            }
        });
    }

    /**
     * Update the bookingsLv with the bookings for the current date.
     * Uses the private class variable date.
     */
    private void getBookings() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference bookingsRef = db.collection("bookings");
        Query query = bookingsRef
                .whereEqualTo("date", date)
                .whereEqualTo("donorEmail",
                              getIntent().getExtras().getString(Constant.USER_EMAIL))
                .whereEqualTo("state", "active");

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    bookings = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        bookings.add(String.format("Time: %s\n"
                                                   + "Charity: %s",
                                doc.get("time").toString(),
                                doc.get("charityName").toString()
                        ));
                        Log.d(TAG, "Added booking with ID: " + doc.getId());
                    }

                    // Link the Adapter to the ListView
                    ListView bookingsLv = findViewById(R.id.bookingsLv);

                    dataAdapter = new ArrayAdapter<String>(
                            ViewCalendar.this,
                            android.R.layout.simple_list_item_1, bookings
                    );
                    bookingsLv.setAdapter(dataAdapter);
                    bookingsLv.setTextFilterEnabled(true);
                }
            }
        });
    }

}