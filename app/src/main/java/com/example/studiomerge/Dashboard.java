package com.example.studiomerge;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studiomerge.lib.Constant;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Dashboard extends AppCompatActivity {

    // Navigation bar
    private BottomNavigationView.OnNavigationItemSelectedListener navigationBarItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.navigation_dashboard:
                    return true;

                // Move to the calendar screen
                case R.id.navigation_calendar:
                    Intent intent = new Intent(
                            getApplicationContext(), ViewCalendar.class);
                    intent.putExtra(
                            Constant.USER_EMAIL,
                            getIntent().getExtras().getString(Constant.USER_EMAIL)
                    );

                    startActivity(intent);
                    return false;  // Keep dashboard as the selected menu option
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        updateUI(getIntent().getExtras().getString(Constant.USER_TYPE));

        // Navigation bar
        BottomNavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(
                navigationBarItemSelectedListener);
    }

    /**
     * Update the UI if the current user is a charity. The UI is not
     * updated if the current user is a donor because the default UI is
     * designed for donors.
     *
     * @param userType user type (donor or charity)
     */
    private void updateUI(String userType) {
        if (userType.equals("charity")) {
            Button btnBooking = findViewById(R.id.btnBooking);
            btnBooking.setText(R.string.event);
        }
    }

    public void onBooking (View v) {
        Intent intent = new Intent(this, MakeBooking.class);
        intent.putExtra(
                Constant.USER_TYPE,
                getIntent().getExtras().getString(Constant.USER_TYPE)
        );
        intent.putExtra(
                Constant.USER_EMAIL,
                getIntent().getExtras().getString(Constant.USER_EMAIL)
        );

        startActivity(intent);
    }

    public void openCalendar(View v) {
        Intent intent = new Intent(this, ViewCalendar.class);
        intent.putExtra(
                Constant.USER_EMAIL,
                getIntent().getExtras().getString(Constant.USER_EMAIL)
        );

        startActivity(intent);
    }

    public void viewBookings(View v) {
        Intent intent = new Intent(this, ViewBookings.class);
        intent.putExtra(
                Constant.USER_EMAIL,
                getIntent().getExtras().getString(Constant.USER_EMAIL)
        );

        startActivity(intent);
    }

    public void onSearchCharity(View v) {
        Intent intent = new Intent(this, SearchCharity.class);
        startActivity(intent);
    }
}
