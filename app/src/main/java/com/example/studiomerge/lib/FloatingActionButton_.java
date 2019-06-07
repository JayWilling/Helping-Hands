package com.example.studiomerge.lib;

import android.view.View;

import com.google.android.material.snackbar.Snackbar;

/**
 * Convenience class. Handles actions related to the
 * FloatingActionButton widget.
 */
public class FloatingActionButton_ {

    public FloatingActionButton_() {}

    public void onClick(View view) {
        Snackbar.make(
                view, "Got a question? Send an email to studio1a.g4@gmail.com",
                Snackbar.LENGTH_LONG
        ).setAction("Action", null).show();
    }

}
