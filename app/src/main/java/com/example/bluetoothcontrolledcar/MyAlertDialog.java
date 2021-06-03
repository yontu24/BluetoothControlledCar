package com.example.bluetoothcontrolledcar;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class MyAlertDialog extends AppCompatDialogFragment {
    private final Activity activity;

    public MyAlertDialog(Activity activity) {
        this.activity = activity;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

        alertDialogBuilder.setTitle("Controller App");

        alertDialogBuilder
                .setMessage("Do you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> {
                    activity.finishAffinity();
                    System.exit(0);
                })
                .setNeutralButton("Cancel", (dialog, id) -> {
                    dialog.cancel();
                })
                .setNegativeButton("No", (dialog, id) -> {
                    dialog.cancel();
                });

        return alertDialogBuilder.create();
    }
}
