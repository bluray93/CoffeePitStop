package com.example.coffepitstop;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;

import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import android.nfc.NfcAdapter.CreateNdefMessageCallback;

import com.amazonaws.services.sns.model.UnsubscribeRequest;

import static android.nfc.NdefRecord.createMime;

public class Settings extends AppCompatActivity implements CreateNdefMessageCallback {
    private TextView textViewS;
    private NfcAdapter nfcAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

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


        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        // Register callback
        nfcAdapter.setNdefPushMessageCallback(this, this);

    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {

        Log.d("NFC", "Message created.");
        String msgToBeam = Util.getSharedPreferences("topicName", getApplicationContext());

        NdefMessage msg = new NdefMessage( new NdefRecord[] { createMime( "application/vnd.com.example.android.beam", msgToBeam.getBytes())});

        return msg;
    }




}
