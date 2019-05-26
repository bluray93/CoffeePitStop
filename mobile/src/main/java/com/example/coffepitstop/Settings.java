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
    private String beamTopicArn;



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
        String msgToBeam = Util.getSharedPreferences("topicArn", getApplicationContext());

        NdefMessage msg = new NdefMessage( new NdefRecord[] { createMime( "application/vnd.com.example.android.beam", msgToBeam.getBytes())});

        return msg;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }
    }

    void processIntent(Intent intent) {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        // record 0 contains the MIME type, record 1 is the AAR, if present

        beamTopicArn = new String(msg.getRecords()[0].getPayload());

        Toast.makeText(getApplicationContext(),beamTopicArn,Toast.LENGTH_SHORT);
    }

    @Override
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        setIntent(intent);
    }



}
