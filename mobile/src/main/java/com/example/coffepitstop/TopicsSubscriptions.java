package com.example.coffepitstop;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.SubscribeResult;

public class TopicsSubscriptions extends AppCompatActivity {

    private EditText editTextTS;
    private AmazonSNSClient snsClient;
    private String topicName;

    private String topicArnPrefix = "arn:aws:sns:us-east-1:341434091225:";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topics_subscriptions);

        //TODO: retrieve client from intent

        snsClient = MainActivity.getSnsClient();

        Log.d("CLIENT TOPIC",snsClient.getEndpoint());

        editTextTS = (EditText) findViewById(R.id.editTextTS);

        final ImageButton accept = findViewById(R.id.AcceptTS);
        accept.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                topicName = editTextTS.getText().toString();

                if (topicName.equals(""))
                    Log.d("TOPIC CREATION", "Empty topic name");
                else {
                    Boolean result = subscribeTopic(topicName);
                    Confirmation(v,result);
                }
            }
        });

        final ImageButton deny = findViewById(R.id.DenyTS);
        deny.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

    }

    public void Confirmation(View view,Boolean result) {

        Intent intent = new Intent(this, Confirmation.class);
        intent.putExtra("subscriptionResult",result);
        intent.putExtra("topicArnPrefix",topicArnPrefix);
        intent.putExtra("topicName", topicName);

        startActivity(intent);
    }

    private void createTopic(String topicName){
        final CreateTopicRequest createTopicRequest = new CreateTopicRequest(topicName);
        final CreateTopicResult createTopicResult = snsClient.createTopic(createTopicRequest);

        Log.d("CREATE TOPIC", createTopicResult.getTopicArn());
    }

    private boolean subscribeTopic(String topicName){

        try {

            //Tries to subscribe to the topic with the given name
            //If the topic does not exists the NotFoundException is thrown

            final SubscribeRequest subscribeRequest = new SubscribeRequest(topicArnPrefix + topicName, "application", Util.getSharedPreferences("endpointArn",getApplicationContext()));

            SubscribeResult subscribeResult = snsClient.subscribe(subscribeRequest);
            Log.d("SubARN",subscribeResult.getSubscriptionArn());
            Util.storeSharedPreferences("subscriptionArn",subscribeResult.getSubscriptionArn(),getApplicationContext());
            Log.d("SUBSCRIBE RESULT", "Subscribe done");
            return true;


        } catch (com.amazonaws.services.sns.model.NotFoundException e){

            //In this case, the topic with the initial name is created
            //createTopic(topicName);
            Log.d("SUBSCRIBE RESULT", "CreateTopic done");

            return false;
        }


    }


}
