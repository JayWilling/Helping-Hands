package com.example.studiomerge;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.example.studiomerge.lib.Constant;
import com.example.studiomerge.lib.FloatingActionButton_;
import com.example.studiomerge.lib.DialogueFragmentMakeBooking;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MakeBooking extends AppCompatActivity
                         implements DialogueFragmentMakeBooking.MakeBookingDialogueListener {

    private static final String TAG = "MAKE_BOOKING";

    private EditText editDonorID, editCharity;
    private TextView editDate, editTime;
    private Button btnBook;

    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private TimePickerDialog.OnTimeSetListener mTimeSetListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.make_booking);

        // Autofill email field
        EditText email = findViewById(R.id.emailEt);
        email.setText(getIntent().getExtras().getString(Constant.USER_EMAIL));

        // FloatingActionButton
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new FloatingActionButton_().onClick(view);
            }
        });

        // Date picker
        editDate = findViewById(R.id.dateEt);
        editDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        MakeBooking.this, android.R.style.Theme_DeviceDefault,
                        mDateSetListener, year, month, day
                );
                dialog.getWindow().setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.GRAY));
                dialog.show();
            }
        });

        // Time picker
        editTime = findViewById(R.id.timeEt);
        editTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
                int minute = cal.get(Calendar.MINUTE);

                TimePickerDialog dialog = new TimePickerDialog(
                        MakeBooking.this, android.R.style.Theme_DeviceDefault,
                        mTimeSetListener, hourOfDay, minute, true
                );
                dialog.getWindow().setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.GRAY));
                dialog.show();
            }
        });

        // Update date field with proper formatting
        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month++;
                editDate.setText(dayOfMonth + "/" + month + "/" + year);
            }
        };

        // Update time field with proper formatting
        mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                editTime.setText(String.format("%s:%s", hourOfDay, minute));
            }
        };

        addBooking();
    }

    /**
     * Create a new booking when the "Make Booking" button is clicked.
     *
     * NB: Currently does not perform any field validation.
     */
    public void addBooking() {
        editDonorID = findViewById(R.id.emailEt);
        editDate = findViewById(R.id.dateEt);
        editTime = findViewById(R.id.timeEt);
        editCharity = findViewById(R.id.charityEt);
        btnBook = findViewById(R.id.bookBtn);

        btnBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> data = new HashMap<>();
                data.put("donorEmail", editDonorID.getText().toString());
                data.put("charityName", editCharity.getText().toString());
                data.put("date", editDate.getText().toString());
                data.put("time", editTime.getText().toString());
                data.put("state", "active");

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("bookings")
                        .add(data)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d(TAG, "Added booking with ID: " + documentReference.getId());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error adding document", e);
                            }

                        });

                // Dialogue box for visual confirmation
                DialogueFragmentMakeBooking dialogueBox = new DialogueFragmentMakeBooking();
                dialogueBox.show(getSupportFragmentManager(), "Booking Made");
            }
        });
    }

    // Dialogue fragment
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's positive button
        finish();
    }
}
