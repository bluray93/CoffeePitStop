package com.example.coffepitstop;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class Confirmation extends WearableActivity {

    private TextView textViewC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        textViewC = (TextView) findViewById(R.id.TextViewC);

        // Enables Always-on
        setAmbientEnabled();

        final ImageButton accept = findViewById(R.id.acceptC);
        accept.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //fai cose
            }
        });

        final ImageButton deny = findViewById(R.id.denyC);
        deny.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }
}
