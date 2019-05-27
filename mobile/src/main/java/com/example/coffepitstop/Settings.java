package com.example.coffepitstop;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import static android.nfc.NdefRecord.createMime;

public class Settings extends AppCompatActivity implements CreateNdefMessageCallback {
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
                finish();
            }
        });



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

        return new NdefMessage( new NdefRecord[] { createMime( "application/vnd.com.example.android.beam", msgToBeam.getBytes())});
    }




}
