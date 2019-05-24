package com.example.coffepitstop;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.amazonaws.services.sns.model.UnsubscribeRequest;

public class Settings extends WearableActivity {

    private TextView textViewS;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Enables Always-on
        setAmbientEnabled();

        textViewS = (TextView) findViewById(R.id.TextViewS);

        textViewS.setText("Do you want\nto unsubscribe?");

        final Intent intent = new Intent(getApplicationContext(), MainActivity.class);

        final ImageButton accept = findViewById(R.id.AcceptS);
        accept.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                UnsubscribeRequest unsubscribeRequest = new UnsubscribeRequest(Util.getSharedPreferences("subscriptionArn",getApplicationContext()));
                MainActivity.getSnsClient().unsubscribe(unsubscribeRequest);
                Log.d("Unsubscribe","Unsibscribed");

                //Delete shared preferences

                Util.deleteSharedPreferences("subscriptionArn",getApplicationContext());
                Util.deleteSharedPreferences("topicName",getApplicationContext());

                //startActivity(intent);
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
