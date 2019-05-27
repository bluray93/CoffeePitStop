package com.example.coffepitstop;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.SubscribeResult;

public class TopicsSubscriptions extends WearableActivity {

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

        editTextTS = findViewById(R.id.editTextTS);

        final ImageButton accept = findViewById(R.id.acceptTS);
        accept.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                topicName = editTextTS.getText().toString();

                if (topicName.equals(""))
                    Log.d("TOPIC CREATION", "Empty topic name");
                else {
                    Boolean result = Util.subscribeTopic(topicName, snsClient,getApplicationContext());
                    Confirmation(result);
                    finish();
                }
            }
        });

        final ImageButton deny = findViewById(R.id.denyTS);
        deny.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

    }

    public void Confirmation(Boolean result) {

        Intent intent = new Intent(this, Confirmation.class);
        intent.putExtra("subscriptionResult",result);
        intent.putExtra("topicArnPrefix",topicArnPrefix);
        intent.putExtra("topicName", topicName);

        startActivity(intent);
    }
}
