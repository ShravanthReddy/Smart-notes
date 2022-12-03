package com.shravanth.smartnotes.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CalculateActivity extends AppCompatActivity {
    String result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //getting the selected character
        CharSequence text = getIntent()
                .getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);

        //converting it to a string
        String givenText = text.toString();

        //creating a new handler and executor
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {

            Calculation calculation = new Calculation(givenText);
            result = calculation.doCalculation();

            if (result == "W") {
                result = "Wrong equation";

            }

            handler.post(() -> {
                if (result == "Wrong equation") {
                    Toast.makeText(this, result + ", please correct the equation and try again.", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(this, "Calculated value: " + result, Toast.LENGTH_SHORT).show();

                }
            });

        });

        finish();

    }
}