package com.example.coffepitstop;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.amazonaws.auth.AWSSessionCredentials;
import com.amazonaws.auth.CognitoCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreatePlatformEndpointRequest;
import com.amazonaws.services.sns.model.CreatePlatformEndpointResult;
import com.amazonaws.services.sns.model.GetEndpointAttributesRequest;
import com.amazonaws.services.sns.model.GetEndpointAttributesResult;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.SetEndpointAttributesRequest;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends WearableActivity{

    private CognitoCredentialsProvider credentialsProvider;
    private AWSSessionCredentials awsSessionCredentials;
    private static AmazonSNSClient snsClient;
    private String token;
    private CreatePlatformEndpointRequest platformEndpointRequest;
    private CreatePlatformEndpointResult platformEndpointResult;
    private String topicName;
    private Boolean snsSet = false;
    private Boolean pushed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                token = instanceIdResult.getToken();
                // Do whatever you want with your token now
                // i.e. store it on SharedPreferences or DB
                // or directly send it to server

                TestAsyncTask task = new TestAsyncTask(MainActivity.this);
                task.execute(10);
            }
        });

        final ImageButton button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(sharedPreferences.contains("topicName")) {
                    if(snsSet){
                        sendNotification();
                    }
                    else{
                        pushed=true;
                    }
                }
                else{
                    Toast.makeText(getBaseContext(),"Subscribe to a group first",Toast.LENGTH_LONG).show();
                }
            }
        });
        final ImageButton settings = findViewById(R.id.settings);
        settings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (sharedPreferences.contains("topicName"))
                    settings(v);
                else
                    topicsSubscriptions(v);
            }
        });

    }

    private void sendNotification(){
        topicName = Util.getSharedPreferences("topicName",getApplicationContext());
        // Code here executes on main thread after user presses button

        String topicArn = "arn:aws:sns:us-east-1:341434091225:" + topicName;
        // Publish a message to an Amazon SNS topic.
        final String msg = "If you receive this message, publishing a message to an Amazon SNS topic works.";

        //PublishRequest creates the request that is sent with the next method publish()
        final PublishRequest publishRequest = new PublishRequest(topicArn, msg);
        final PublishResult publishResponse = snsClient.publish(publishRequest);

        // Print the MessageId of the message.
        System.out.println("MessageId: " + publishResponse.getMessageId());
    }

    private void createEndpoint(){
        platformEndpointRequest = new CreatePlatformEndpointRequest();
        platformEndpointRequest.setCustomUserData("CoffeePitStopUser");
        platformEndpointRequest.setToken(token);
        platformEndpointRequest.setPlatformApplicationArn("arn:aws:sns:us-east-1:341434091225:app/GCM/CoffeePitStop");
        platformEndpointResult = snsClient.createPlatformEndpoint(platformEndpointRequest);

        //We store the newly created endpoint

        Util.storeSharedPreferences("endpointArn",platformEndpointResult.getEndpointArn(),getApplicationContext());

        Log.d("ENDPOINT", "Endpoint created " + platformEndpointResult.getEndpointArn());

    }


    private void setSnsClient(){
        credentialsProvider = new CognitoCredentialsProvider(
                "us-east-1:5223d77f-b704-4027-97e2-d6c837276cdb", // ID pool di identità
                Regions.US_EAST_1 // Regione
        );

        if (credentialsProvider == null)
            Log.d("SET_CLIENT","Credential Provider is null");

        awsSessionCredentials = credentialsProvider.getCredentials();

        if (awsSessionCredentials == null)
            Log.d("SET_CLIENT","Session Credentials are null");

        snsClient = new AmazonSNSClient(awsSessionCredentials);
        snsClient.setRegion(Region.getRegion(Regions.US_EAST_1));

        if (snsClient == null)
            Log.d("SET_CLIENT","SNS client is null");
    }


    private void snsSetup(){

        String endpointARN = Util.getSharedPreferences("endpointArn",getApplicationContext());
        Boolean toUpdate = false;

        Log.d("ENDPOINT","is "+ endpointARN);

        if (endpointARN == null){

            Log.d("ENDPOINT","SONO IN NULL");
            createEndpoint();

        } else{

            try {

                final GetEndpointAttributesRequest attrReq = new GetEndpointAttributesRequest().withEndpointArn(endpointARN);
                final GetEndpointAttributesResult attrRes = snsClient.getEndpointAttributes(attrReq);

                toUpdate = !attrRes.getAttributes().get("Token").equals(token)
                        || !attrRes.getAttributes().get("Enabled").equalsIgnoreCase("true");

            } catch (Resources.NotFoundException nfe) {
                // We had a stored ARN, but the platform endpoint associated with it
                // disappeared. Recreate it.
            }
        }

        if(toUpdate){
            final Map<String, String> attribs = new HashMap<>();
            attribs.put("Token", token);
            attribs.put("Enabled", "true");

            final SetEndpointAttributesRequest saeReq = new SetEndpointAttributesRequest()
                    .withEndpointArn(endpointARN)
                    .withAttributes(attribs);

            snsClient.setEndpointAttributes(saeReq);
        }

    }

    /** Called when the user taps the Settings button */
    public void topicsSubscriptions(View view) {
        Intent intent = new Intent(this, TopicsSubscriptions.class);

        intent.putExtra("endpointArn", Util.getSharedPreferences("endpointArn",getApplicationContext()));
        Log.d("CLIENT MAIN",snsClient.getEndpoint());

        startActivity(intent);
    }

    public void settings(View view) {
        Intent intent = new Intent(this, Settings.class);

        //intent.putExtra("endpointArn", retrieveEndpointArn());
        //Log.d("CLIENT MAIN",snsClient.getEndpoint());

        startActivity(intent);
    }

    public static AmazonSNSClient getSnsClient(){
        return snsClient;
    }

    private static class TestAsyncTask extends AsyncTask<Integer, Integer, AmazonSNSClient> {
        private WeakReference<MainActivity> activityWeakReference;

        TestAsyncTask(MainActivity activity) {
            activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected AmazonSNSClient doInBackground(Integer... strings) {
            MainActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()) {
                return null;
            }
            activity.setSnsClient();
            activity.snsSetup();
            return null;
        }

        @Override
        protected void onPostExecute(AmazonSNSClient amazonSNSClient) {
            super.onPostExecute(amazonSNSClient);
            MainActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            final ImageButton settings = activity.findViewById(R.id.settings);
            settings.setVisibility(View.VISIBLE);
            if(activity.pushed) {
                activity.sendNotification();
                activity.snsSet = true;
            }
            else
                activity.snsSet=true;
        }

    }
}

