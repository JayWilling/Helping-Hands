package com.example.studiomerge;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studiomerge.lib.Constant;
import com.example.studiomerge.lib.MultiObservable;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class Profile extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "PROFILE";

    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        populateFields();

        // Initialise MapView widget
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.googleMap);
        SupportMapFragment mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.googleMap);
        mapFragment.getMapAsync(this);

        // Disable ScrollView scrolling when using the MapView
        final ScrollView scrollView = findViewById(R.id.scrollView);
        ImageView transparentImage = findViewById(R.id.transparent_image);
        transparentImage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView, capture touch events
                        scrollView.requestDisallowInterceptTouchEvent(true);
                        return false;  // Disable touch on transparent image

                    case MotionEvent.ACTION_UP:
                        scrollView.requestDisallowInterceptTouchEvent(false);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        scrollView.requestDisallowInterceptTouchEvent(true);
                        return false;

                    default:
                        return true;
                }
            }
        });
    }

    /**
     * Extract the email of the charity from the calling Intent and
     * search the database for its physical address (combination of
     * address, state and postcode fields).
     *
     * Fails if there is no charity with the extracted email in the
     * database.
     *
     * The email is extracted using the Constant.PROFILE_EMAIL String.
     *
     * @param googleMap instance of GoogleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        final String email = getIntent().getStringExtra(Constant.PROFILE_EMAIL);

        /**
         * Setup an observable to handle the results of the search
         * query.
         *
         * If a charity was found with the extracted email, attempt to
         * convert their address to a specific latitude and longitude. */
        final MultiObservable<String> charityAddress = new MultiObservable<>();
        charityAddress.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                getLocationFromAddress(getApplicationContext(), (String) arg);
            }
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int count = 0;
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                if (
                                        doc.get("type").equals("charity")
                                        && doc.get("email").equals(email)
                                    ) {
                                    String address = String.format(
                                            "%s, %s, %s",
                                            doc.get("address"), doc.get("state"),
                                            doc.get("postcode")
                                    );
                                    Log.d(TAG, "Found charity with email: " + email);
                                    Log.d(TAG, "Charity address: " + address);

                                    charityAddress.setValue(address);
                                } else if (++count == task.getResult().size()) {
                                    Log.d(TAG, "Failed to find charity with email: " + email);
                                }
                            }
                        } else {
                            Log.w(
                                    TAG, "Error getting documents.",
                                    task.getException());
                        }
                    }
                });
    }

    /**
     * Convert the given address to a specific latitude and longitude
     * using Geocoder.
     *
     * If conversion is successful, call initialiseMap().
     *
     * @param context current application context
     * @param address address to convert
     */
    private void getLocationFromAddress(Context context, String address) {
        Geocoder coder = new Geocoder(context);
        List<Address> coderResults;

        LatLng result = null;
        try {
            coderResults = coder.getFromLocationName(address, 2);
            if (coderResults == null) {
                Log.d(TAG, "Failed to convert address to LatLng.");
                Log.d(TAG, "Address: " + address);
                return;
            }

            Address location = coderResults.get(0);
            initialiseMap(
                    new LatLng(location.getLatitude(), location.getLongitude()),
                    address
            );
        } catch (IOException e) {
            //
        }
    }

    /**
     * Pan the camera to and set a marker at the given location, then
     * zoom in to an appropriate field of view.
     *
     * @param location latitude and longitude of the given address
     * @param address  address description
     */
    private void initialiseMap(LatLng location, String address) {
        try {
            this.googleMap.addMarker(
                    new MarkerOptions().position(location).title(address));
            this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(location));
            this.googleMap.moveCamera(CameraUpdateFactory.zoomTo(16));

            Log.d(TAG, "Map initialised successfully.");
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Cannot initialise map on null location.", e);
        }
    }

    /**
     * Extract the email of the charity from the calling Intent and
     * search the database for its other details, then populate the
     * relevant fields.
     *
     * Fails if there is no charity with the extracted email in the
     * database.
     *
     * The email is extracted using the Constant.PROFILE_EMAIL String.
     */
    private void populateFields() {
        final String email = getIntent().getStringExtra(Constant.PROFILE_EMAIL);

        /**
         * Setup an observable to handle the results of the search
         * query.
         *
         * If a charity was found with the extracted email, populate
         * the relevant fields.
         */
        final MultiObservable<String[]> charityDoc = new MultiObservable<>();
        charityDoc.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                TextView tvName = findViewById(R.id.tvName);
                TextView tvEmail = findViewById(R.id.tvEmail);
                TextView tvPhone = findViewById(R.id.tvPhone);

                tvName.setText(((String[]) arg)[0]);
                tvEmail.setText(((String[]) arg)[1]);
                tvPhone.setText(((String[]) arg)[2]);
            }
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int count = 0;
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                if (
                                        doc.get("type").equals("charity")
                                                && doc.get("email").equals(email)
                                ) {
                                    Log.d(TAG, "Found charity with email: " + email);

                                    String[] details = {
                                            doc.get("organisation").toString(),
                                            doc.get("email").toString(),
                                            doc.get("phone").toString()
                                    };
                                    charityDoc.setValue(details);
                                } else if (++count == task.getResult().size()) {
                                    Log.d(TAG, "Failed to find charity with email: " + email);
                                }
                            }
                        } else {
                            Log.w(
                                    TAG, "Error getting documents.",
                                    task.getException());
                        }
                    }
                });
    }

    /**
     * Go to the charity events screen.
     *
     * @param v the View that received the onClick event
     */
    public void onEvents(View v) {
        Intent intent = new Intent(this, CharityEvents.class);
        intent.putExtra(
                Constant.PROFILE_EMAIL,
                getIntent().getExtras().getString(Constant.PROFILE_EMAIL)
        );

        startActivity(intent);
    }
}
