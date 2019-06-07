package com.example.studiomerge;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studiomerge.lib.Constant;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class CharityEvents extends AppCompatActivity {

    private static final String TAG = "CHARITY_EVENTS";

    private ArrayAdapter<String> dataAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.events);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference eventsRef = db.collection("events");
        Query query = eventsRef
                .whereEqualTo("email", getIntent().getExtras().getString(Constant.PROFILE_EMAIL));

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    ArrayList<String> events = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        events.add(String.format("Date: %s\n"
                                                 + "Time: %s",
                                doc.get("date").toString(),
                                doc.get("time").toString()
                        ));
                        Log.d(TAG, "Added event with ID: " + doc.getId());
                    }

                    // Link the Adapter to the ListView
                    ListView eventsLv = findViewById(R.id.eventsLv);

                    dataAdapter = new ArrayAdapter<String>(
                            getApplicationContext(),
                            R.layout.simple_textview, events
                    );
                    eventsLv.setAdapter(dataAdapter);
                    eventsLv.setTextFilterEnabled(true);
                }
            }
        });
    }
}
