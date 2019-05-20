package com.example.coffepitstop;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

public class TopicsSubscriptions extends AppCompatActivity {

    private EditText editTextTS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topics_subscriptions);

        editTextTS = (EditText) findViewById(R.id.editTextTS);

        final ImageButton accept = findViewById(R.id.AcceptTS);
        accept.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Confirmation(v);
            }
        });

        final ImageButton deny = findViewById(R.id.DenyTS);
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
