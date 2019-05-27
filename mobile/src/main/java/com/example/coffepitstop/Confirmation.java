package com.example.coffepitstop;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;

public class Confirmation extends AppCompatActivity {

    private Boolean subscriptionResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        Intent intent = getIntent();

        subscriptionResult = intent.getBooleanExtra("subscriptionResult",false);
        final String topicName = intent.getStringExtra("topicName");
        final Boolean nfc = intent.getBooleanExtra("nfc",false);

        TextView textViewC = findViewById(R.id.TextViewC);

        if (subscriptionResult) {
            if (nfc)
                textViewC.setText("Already subscribed to a different group\ndo you want overwrite?");
            else
                textViewC.setText("Do you want\nto subscribe?");
        }
        else
            textViewC.setText("Group not found.\nCreate Group?");


        final ImageButton accept = findViewById(R.id.AcceptC);
        accept.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (subscriptionResult) {
                    Toast.makeText(getBaseContext(), "Sub. Success.", Toast.LENGTH_LONG).show();
                    Util.storeSharedPreferences("topicName",topicName,getApplicationContext());
                    finish();
                }

                else if(nfc){
                    Util.unsubscribe(subscriptionResult,getApplicationContext());
                    Util.subscribeTopic(topicName, MainActivity.getSnsClient(),getApplicationContext());
                    Toast.makeText(getBaseContext(), "Sub. Success.", Toast.LENGTH_LONG).show();
                    Util.storeSharedPreferences("topicName",topicName,getApplicationContext());
                }

                else{
                    AmazonSNSClient snsClient = MainActivity.getSnsClient();

                    try {
                        //Tries to subscribe to the topic with the given name
                        //If the topic does not exists the NotFoundException is thrown
                        final CreateTopicRequest createTopicRequest = new CreateTopicRequest(topicName);
                        final CreateTopicResult createTopicResult = snsClient.createTopic(createTopicRequest);

                        Util.subscribeTopic(topicName, snsClient,getApplicationContext());

                        Toast.makeText(getBaseContext(),"Group created.",Toast.LENGTH_LONG).show();
                        Util.storeSharedPreferences("topicName",topicName,getApplicationContext());
                        finish();

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
                Util.unsubscribe(subscriptionResult,getApplicationContext());
                finish();
            }
        });
    }



}
