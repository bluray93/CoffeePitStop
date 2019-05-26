package com.example.coffepitstop;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.amazonaws.services.sns.AmazonSNSClient;
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

        editTextTS = findViewById(R.id.editTextTS);

        final ImageButton accept = findViewById(R.id.AcceptTS);
        accept.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                topicName = editTextTS.getText().toString();

                if (topicName.equals(""))
                    Log.d("TOPIC CREATION", "Empty topic name");
                else {
                    Boolean result = subscribeTopic(topicName);
                    Confirmation(result);
                    finish();
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

    public void Confirmation(Boolean result) {

        Intent intent = new Intent(this, Confirmation.class);
        intent.putExtra("subscriptionResult",result);
        intent.putExtra("topicArnPrefix",topicArnPrefix);
        intent.putExtra("topicName", topicName);

        startActivity(intent);
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

    @Override
    public void onResume() {
        super.onResume();
        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            Log.d("AOOO", "Connection received.");
            processIntent(getIntent());
        }
    }

    void processIntent(Intent intent) {
        Log.d("AOOO", "Process intent received.");

        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        // record 0 contains the MIME type, record 1 is the AAR, if present

        topicName = new String(msg.getRecords()[0].getPayload());
        Log.d("AOOO",topicName + " è il messaggio");

        Boolean result = subscribeTopic(topicName);
        Confirmation(result);
        
        Intent i=new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);


    }

    @Override
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        setIntent(intent);
    }
}
