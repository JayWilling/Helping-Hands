package com.example.studiomerge;

import android.content.Intent;
import android.os.Bundle;

import com.example.studiomerge.lib.Constant;
import com.example.studiomerge.lib.MultiObservable;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class SearchCharity extends AppCompatActivity {

    private static final String TAG = "SEARCHING";

    ArrayAdapter<String> dataAdapter;
    Map<String, String> organisation_email = new HashMap<>();  // {organisation : email}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_charity);

        // Setup database query
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference charityRef = db.collection("users");
        Query query = charityRef.whereEqualTo("type", "charity");

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                ArrayList<String> charities = new ArrayList<>();

                if (task.isSuccessful()) {
                    for(QueryDocumentSnapshot document: task.getResult()){
                        charities.add(document.get("organisation").toString());
                        organisation_email.put(
                                document.get("organisation").toString(),
                                document.get("email").toString()
                        );

                        Log.d(TAG, "Added charity with ID: " + document.getId());
                    }

                    // Link the Adapter to the ListView
                    ListView charitiesLv = findViewById(R.id.charityLv);

                    dataAdapter = new ArrayAdapter<String>(
                            getApplicationContext(),
                            R.layout.simple_textview, charities
                    );
                    charitiesLv.setAdapter(dataAdapter);
                    charitiesLv.setTextFilterEnabled(true);
                }
            }
        });

        // Open the appropriate profile when clicked
        ListView charitiesLv = findViewById(R.id.charityLv);
        charitiesLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), Profile.class);
                intent.putExtra(
                        Constant.PROFILE_EMAIL,
                        organisation_email.get(dataAdapter.getItem(position))
                );

                startActivity(intent);
            }
        });

        // Filter charities based on the contents of the search field
        EditText charityEt = findViewById(R.id.charityEt);
        charityEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                dataAdapter.getFilter().filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
}
