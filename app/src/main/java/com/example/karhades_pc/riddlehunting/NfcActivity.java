package com.example.karhades_pc.riddlehunting;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Karhades on 18-Aug-15.
 */
public class NfcActivity extends AppCompatActivity
{
    private NfcAdapter nfcAdapter;
    private PendingIntent nfcPendingIntent;
    private IntentFilter[] filters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        handleIntent(getIntent());

        setupForegroundDispatch();
    }

    @Override
    protected void onResume() {
        super.onResume();

        enableForegroundDispatch();
    }

    @Override
    protected void onPause() {
        super.onPause();

        disableForegroundDispatch();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        handleIntent(intent);
    }

    // Blocks incoming intents that would launch due to
    // the NFC Tag Discovery, including the base activity
    // as well (Duplicate).
    private void setupForegroundDispatch()
    {
        // Creates an intent with tag data and delivers it
        // to this activity.
        Intent intent = new Intent(this, MenuActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        nfcPendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // Listens for the TAG_DISCOVERED intent.
        IntentFilter tagDiscovered = new IntentFilter();
        tagDiscovered.addAction(NfcAdapter.ACTION_TAG_DISCOVERED);

        // Listens for the TECH_DISCOVERED intent.
        IntentFilter techDiscovered = new IntentFilter();
        techDiscovered.addAction(NfcAdapter.ACTION_TECH_DISCOVERED);
        // TODO Fill the TECH_DISCOVERED list and pass it onto the enableForegroundDispatch method.

        filters = new IntentFilter[]{tagDiscovered, techDiscovered};
    }

    public void enableForegroundDispatch()
    {
        if(nfcAdapter != null)
            nfcAdapter.enableForegroundDispatch(this, nfcPendingIntent, filters, null);
    }

    public void disableForegroundDispatch()
    {
        if(nfcAdapter != null)
            nfcAdapter.disableForegroundDispatch(this);
    }

    // Gets the Tag ID and updates the appropriate riddle.
    public void handleIntent(Intent intent)
    {
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction()) || NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            String tagId = ByteArrayToHexString(tag.getId());

            // Creates an Intent and sends the extra discovered Tag ID and
            // another extra to indicate that it's from the NFC discovery.
            Intent intentTagId = new Intent(this, RiddleActivity.class);
            intentTagId.putExtra(RiddleFragment.EXTRA_TAG_ID, tagId);
            intentTagId.putExtra(RiddleFragment.EXTRA_NFC_TAG_DISCOVERED, true);
            startActivity(intentTagId);
        }
    }

    // Converts the byte array into hex.
    private String ByteArrayToHexString(byte[] byteArray) {
        int i, j, in;
        String[] hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
        String hexArray = "";

        for (j = 0; j < byteArray.length; ++j) {
            in = (int) byteArray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            hexArray += hex[i];
            i = in & 0x0f;
            hexArray += hex[i];
        }
        return hexArray;
    }
}
