package com.example.coffepitstop;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.amazonaws.auth.AWSSessionCredentials;
import com.amazonaws.auth.CognitoCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreatePlatformEndpointRequest;
import com.amazonaws.services.sns.model.CreatePlatformEndpointResult;
import com.amazonaws.services.sns.model.DeleteEndpointRequest;
import com.amazonaws.services.sns.model.GetEndpointAttributesRequest;
import com.amazonaws.services.sns.model.GetEndpointAttributesResult;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.SetEndpointAttributesRequest;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {


    private static final String PUSH_SETTINGS = "PushController.PUSH_SETTINGS";
    private static final String PUSH_SETTINGS_KEY = "PushController.PUSH_SETTINGS_KEY";
    private CognitoCredentialsProvider credentialsProvider;
    private AWSSessionCredentials awsSessionCredentials;
    private AmazonSNSClient snsClient;
    private String token;
    private CreatePlatformEndpointRequest platformEndpointRequest;
    private CreatePlatformEndpointResult platformEndpointResult;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if( Build.VERSION.SDK_INT >= 9){
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

            StrictMode.setThreadPolicy(policy);
        }

        //String token = FirebaseInstanceId.getInstance().getToken();


        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                token = instanceIdResult.getToken();
                // Do whatever you want with your token now
                // i.e. store it on SharedPreferences or DB
                // or directly send it to server

                if(token == null)
                    Log.d("TOKEN","NULL");
                else
                    Log.d("TOKEN",token);

                //initialSetup();

                setSnsClient();
                snsSetup();
            }
        });


        final ImageButton button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("BUTTON", "Premuto");
                // Code here executes on main thread after user presses button

                //TODO: topic arn is hardcoded for the moment. Needs to be replaced
                String topicArn = "arn:aws:sns:us-east-1:341434091225:Arg1";
                // Publish a message to an Amazon SNS topic.
                final String msg = "If you receive this message, publishing a message to an Amazon SNS topic works.";

                //PublishRequest creates the request that is sent with the next method publish()
                final PublishRequest publishRequest = new PublishRequest(topicArn, msg);
                final PublishResult publishResponse = snsClient.publish(publishRequest);

                // Print the MessageId of the message.
                System.out.println("MessageId: " + publishResponse.getMessageId());
            }
        });

        final ImageButton button2 = findViewById(R.id.settings);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                topicsSubscriptions(v);
            }
        });

    }

    private String retrieveEndpointArn() {
        final SharedPreferences sharedPreferences =
                this.getSharedPreferences(PUSH_SETTINGS, Context.MODE_PRIVATE);
        return sharedPreferences.getString(PUSH_SETTINGS_KEY, null);
    }

    private void storeEndpointArn(String endpointArn) {
        final SharedPreferences.Editor sharedPreferencesEditor =
                this.getSharedPreferences(PUSH_SETTINGS, Context.MODE_PRIVATE).edit();
        sharedPreferencesEditor.putString(PUSH_SETTINGS_KEY, endpointArn).apply();
    }


    private void createEndpoint(){
        platformEndpointRequest = new CreatePlatformEndpointRequest();
        platformEndpointRequest.setCustomUserData("CoffeePitStopUser");
        platformEndpointRequest.setToken(token);
        platformEndpointRequest.setPlatformApplicationArn("arn:aws:sns:us-east-1:341434091225:app/GCM/CoffeePitStop");
        platformEndpointResult = snsClient.createPlatformEndpoint(platformEndpointRequest);

        //We store the newly created endpoint
        storeEndpointArn(platformEndpointResult.getEndpointArn());


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

        String endpointARN = retrieveEndpointArn();

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
        startActivity(intent);
    }

}
