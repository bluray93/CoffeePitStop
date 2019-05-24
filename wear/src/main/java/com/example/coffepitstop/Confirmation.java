package com.example.coffepitstop;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.SubscribeResult;
import com.amazonaws.services.sns.model.UnsubscribeRequest;

public class Confirmation extends WearableActivity {

    private TextView textViewC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        // Enables Always-on
        setAmbientEnabled();

        Intent intent = getIntent();
        final Intent returnBtn = new Intent(getApplicationContext(), MainActivity.class);

        final Boolean subscriptionResult = intent.getBooleanExtra("subscriptionResult",true);
        final String topicArnPrefix = intent.getStringExtra("topicArnPrefix");
        final String topicName = intent.getStringExtra("topicName");
        final String endpointArn = Util.getSharedPreferences("endpointArn",getApplicationContext());

        textViewC = (TextView) findViewById(R.id.TextViewC);

        if (subscriptionResult)
            textViewC.setText("Do you want\nto subscribe?");
        else
            textViewC.setText("Group not found.\nCreate Group?");


        final ImageButton accept = findViewById(R.id.acceptC);
        accept.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //fai cose
                if (subscriptionResult) {

                    Toast.makeText(getBaseContext(), "Sub. Success.", Toast.LENGTH_LONG).show();
                    Util.storeSharedPreferences("topicName",topicName,getApplicationContext());
                    startActivity(returnBtn);
                }

                else{
                    AmazonSNSClient snsClient = MainActivity.getSnsClient();

                    try {

                        //Tries to subscribe to the topic with the given name
                        //If the topic does not exists the NotFoundException is thrown
                        final CreateTopicRequest createTopicRequest = new CreateTopicRequest(topicName);
                        final CreateTopicResult createTopicResult = snsClient.createTopic(createTopicRequest);

                        final SubscribeRequest subscribeRequest = new SubscribeRequest(topicArnPrefix+topicName, "application", endpointArn);
                        SubscribeResult subscribeResult = snsClient.subscribe(subscribeRequest);
                        Log.d("SUBSCRIBE RESULT", "Subscribe done");
                        Toast.makeText(getBaseContext(),"Group created.",Toast.LENGTH_LONG).show();
                        Util.storeSharedPreferences("topicName",topicName,getApplicationContext());
                        Util.storeSharedPreferences("subscriptionArn",subscribeResult.getSubscriptionArn(),getApplicationContext());
                        startActivity(returnBtn);

                    } catch (com.amazonaws.services.sns.model.NotFoundException e){

                        Log.d("SUBSCRIBE RESULT", "CreateTopic done");
                        Toast.makeText(getBaseContext(),"Sub. Failed.",Toast.LENGTH_LONG).show();
                        finish();
                    }
                }

            }
        });

        final ImageButton deny = findViewById(R.id.denyC);
        deny.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AmazonSNSClient snsClient = MainActivity.getSnsClient();

                if (snsClient == null)
                    Log.d("CLIENT SNS", "CLIENT NULLO");
                if (subscriptionResult){

                    UnsubscribeRequest unsubscribeRequest = new UnsubscribeRequest(Util.getSharedPreferences("subscriptionArn",getApplicationContext()));
                    snsClient.unsubscribe(unsubscribeRequest);
                    Log.d("Unsibscribe","Unsibscribed");

                    //Delete shared preferences

                    Util.deleteSharedPreferences("subscriptionArn",getApplicationContext());
                }

                finish();
            }
        });
    }
}
