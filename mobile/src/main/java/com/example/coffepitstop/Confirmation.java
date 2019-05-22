package com.example.coffepitstop;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.SubscribeRequest;

public class Confirmation extends AppCompatActivity {

    private TextView textViewC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        Intent intent = getIntent();
        final Intent returnBtn = new Intent(getApplicationContext(), MainActivity.class);

        final Boolean subscriptionResult = intent.getBooleanExtra("subscriptionResult",true);
        final String topicArn = intent.getStringExtra("topicArn");
        final String endpointArn = intent.getStringExtra("endpointArn");
        textViewC = (TextView) findViewById(R.id.TextViewC);

        if (subscriptionResult)
            textViewC.setText("Do you want\nto subscribe?");
        else
            textViewC.setText("Group not found.\nCreate Group?");


        final ImageButton accept = findViewById(R.id.AcceptC);
        accept.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //fai cose
                if (subscriptionResult) {
                    Toast.makeText(getBaseContext(), "Sub. Success.", Toast.LENGTH_LONG).show();
                    startActivity(returnBtn);
                }

                else{
                    AmazonSNSClient snsClient = MainActivity.getSnsClient();

                    try {

                        //Tries to subscribe to the topic with the given name
                        //If the topic does not exists the NotFoundException is thrown

                        final SubscribeRequest subscribeRequest = new SubscribeRequest(topicArn, "application", endpointArn);
                        snsClient.subscribe(subscribeRequest);
                        Log.d("SUBSCRIBE RESULT", "Subscribe done");
                        Toast.makeText(getBaseContext(),"Group created.",Toast.LENGTH_LONG).show();
                        startActivity(returnBtn);

                    } catch (com.amazonaws.services.sns.model.NotFoundException e){

                        Log.d("SUBSCRIBE RESULT", "CreateTopic done");
                        Toast.makeText(getBaseContext(),"Sub. Failed.",Toast.LENGTH_LONG).show();
                        finish();
                    }
                }

            }
        });

        final ImageButton deny = findViewById(R.id.DenyC);
        deny.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }

}
