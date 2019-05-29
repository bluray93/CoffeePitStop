package com.example.coffepitstop;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class Settings extends AppCompatActivity {
    private TextView textViewS;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        textViewS = findViewById(R.id.TextViewS);

        textViewS.setText("Do you want\nto unsubscribe?");

        final ImageButton accept = findViewById(R.id.AcceptS);
        accept.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Util.unsubscribe(true,getApplicationContext());

                Util.deleteSharedPreferences("topicName",getApplicationContext());

                finish();
            }

        });

        final ImageButton deny = findViewById(R.id.DenyS);
        deny.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }






}
