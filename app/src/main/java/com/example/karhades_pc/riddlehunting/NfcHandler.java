package com.example.karhades_pc.riddlehunting;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;

/**
 * Created by Karhades on 18-Aug-15.
 */
class NfcHandler
{
    private final NfcAdapter nfcAdapter;
    private PendingIntent nfcPendingIntent;
    private IntentFilter[] filters;

    private final Activity activity;
    private final Context context;

    public NfcHandler(Context context, Activity activity)
    {
        this.activity = activity;
        this.context = context;

        nfcAdapter = NfcAdapter.getDefaultAdapter(context);

        setupForegroundDispatch();
    }

    // Blocks incoming intents that would launch due to
    // the NFC Tag Discovery, including the base activity
    // as well (Duplicate).
    private void setupForegroundDispatch()
    {
        // Creates an intent with tag data and delivers it
        // to this activity.
        Intent intent = new Intent(context, RiddleListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        nfcPendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        // Listens for the TAG_DISCOVERED intent.
        IntentFilter tagDetected = new IntentFilter();
        tagDetected.addAction(NfcAdapter.ACTION_TAG_DISCOVERED);

        filters = new IntentFilter[]{tagDetected};
    }

    public void enableForegroundDispatch()
    {
        nfcAdapter.enableForegroundDispatch(activity, nfcPendingIntent, filters, null);
    }

    public void disableForegroundDispatch()
    {
        nfcAdapter.disableForegroundDispatch(activity);
    }

    // Gets the Tag ID and updates the appropriate riddle.
    public void handleIntent(Intent intent)
    {
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction()) || NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            String tagId = ByteArrayToHexString(tag.getId());

            //Creates an Intent and sends the extra discovered Tag ID and
            //another extra to indicate that it's from the NFC discovery.
            Intent intentTagId = new Intent(context, RiddleActivity.class);
            intentTagId.putExtra(RiddleFragment.EXTRA_TAG_ID, tagId);
            intentTagId.putExtra(RiddleFragment.EXTRA_NFC_TAG_DISCOVERED, true);
            activity.startActivity(intentTagId);
        }
    }

    // Converts the byte array into hex.
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
