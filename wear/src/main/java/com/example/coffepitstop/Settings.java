package com.example.coffepitstop;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.services.sns.model.UnsubscribeRequest;

import static android.nfc.NdefRecord.createMime;

public class Settings extends WearableActivity implements NfcAdapter.CreateNdefMessageCallback {

    private TextView textViewS;
    private NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        textViewS = findViewById(R.id.TextViewS);

        textViewS.setText("Do you want\nto unsubscribe?");

        final ImageButton accept = findViewById(R.id.AcceptS);
        accept.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Util.unsubscribe(true,getApplicationContext());

                Util.deleteSharedPreferences("topicName",getApplicationContext());

                finish();
            }

        });

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        final ImageButton deny = findViewById(R.id.DenyS);
        deny.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (nfcAdapter == null) {
                    Toast.makeText(getBaseContext(), "NFC is not available", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                // Register callback
                nfcAdapter.setNdefPushMessageCallback(Settings.this, Settings.this);

                finish();
            }
        });





    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {

        Log.d("NFC", "Message created.");
        String msgToBeam = Util.getSharedPreferences("topicName", getApplicationContext());

        return new NdefMessage( new NdefRecord[] { createMime( "application/vnd.com.example.android.beam", msgToBeam.getBytes())});
    }
}
