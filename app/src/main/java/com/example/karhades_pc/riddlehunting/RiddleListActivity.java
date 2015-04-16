package com.example.karhades_pc.riddlehunting;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by Karhades - PC on 4/15/2015.
 */
public class RiddleListActivity extends SingleFragmentActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent nfcPendingIntent;
    private IntentFilter[] filters;

    @Override
    protected Fragment createFragment() {
        return new RiddleListFragment();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        onNewIntent(getIntent());
        setupForegroundDispatch();
    }

    //Blocks incoming intents that would launch due to
    //the NFC Tag Discovery, including the base activity
    //as well (Duplicate)
    private void setupForegroundDispatch() {
        //Creates an intent with tag data and delivers it
        //to this activity
        Intent intent = new Intent(RiddleListActivity.this, RiddleListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        nfcPendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        //Listens for the TAG_DISCOVERED intent
        IntentFilter tagDetected = new IntentFilter();
        tagDetected.addAction(NfcAdapter.ACTION_TAG_DISCOVERED);

        filters = new IntentFilter[]{tagDetected};
    }

    @Override
    protected void onResume() {
        super.onResume();

        nfcAdapter.enableForegroundDispatch(this, nfcPendingIntent, filters, null);
    }

    //Called when a tag is discovered
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        handleIntent(intent);
    }

    //Stops the Foreground Dispatch
    @Override
    protected void onPause() {
        nfcAdapter.disableForegroundDispatch(this);

        super.onPause();
    }

    //Gets the Tag ID and updates the appropriate riddle
    private void handleIntent(Intent intent) {
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction()) || NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            String tagId = ByteArrayToHexString(tag.getId());

            //Creates an Intent and sends the extra discovered Tag ID and
            //another extra to indicate that it's from the NFC discovery.
            Intent intentTagId = new Intent(this, RiddleActivity.class);
            intentTagId.putExtra(RiddleFragment.EXTRA_TAG_ID, tagId);
            intentTagId.putExtra(RiddleFragment.EXTRA_NFC_TAG_DISCOVERED, true);
            startActivity(intentTagId);
        }
    }

    //Converts the byte array into hex
    private String ByteArrayToHexString(byte[] inarray) {
        int i, j, in;
        String[] hex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
        String out = "";

        for (j = 0; j < inarray.length; ++j) {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }
}
