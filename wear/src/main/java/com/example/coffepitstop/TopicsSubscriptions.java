package com.example.coffepitstop;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class TopicsSubscriptions extends WearableActivity {

    private EditText editTextTS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topics_subscriptions);

        editTextTS = (EditText) findViewById(R.id.editTextTS);

        // Enables Always-on
        setAmbientEnabled();

        final ImageButton accept = findViewById(R.id.acceptTS);
        accept.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Confirmation(v);
            }
        });

        final ImageButton deny = findViewById(R.id.denyTS);
        deny.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void Confirmation(View view) {
        Intent intent = new Intent(this, Confirmation.class);
        startActivity(intent);
    }
}
