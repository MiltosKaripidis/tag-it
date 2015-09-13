package com.example.karhades_pc.nfc;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.os.Build;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.example.karhades_pc.tag_it.MyTags;
import com.example.karhades_pc.tag_it.NfcTag;
import com.example.karhades_pc.tag_it.TagPagerActivity;
import com.example.karhades_pc.tag_it.TrackingTagFragment;

/**
 * Created by Karhades on 18-Aug-15.
 */
public class NfcHandler {
    private static final String TAG = "NfcHandler";
    private static final String MIME_TYPE = "application/com.example.karhades_pc.nfchandler";

    private NfcAdapter nfcAdapter;
    private PendingIntent nfcPendingIntent;
    private IntentFilter[] filters;
    private String[][] techList;
    private Activity activity;

    private static boolean writeMode = false;
    private OnTagWriteListener onTagWriteListener;

    /**
     * interface for the onTagWritten callback method
     * which is called when the tag had data written to it.
     */
    public interface OnTagWriteListener {
        int STATUS_OK = 0;
        int STATUS_ERROR = 1;

        void onTagWritten(int status);
    }

    /**
     * Setter method for the callback.
     *
     * @param onTagWriteListener The listener for the callback.
     */
    public void setOnTagWriteListener(OnTagWriteListener onTagWriteListener) {
        this.onTagWriteListener = onTagWriteListener;
    }

    /**
     * Setup the NfcAdapter, the foreground tag dispatch system and
     * the Android Beam.
     *
     * @param activity The activity needed for the android system.
     */
    public void setupNfcHandler(Activity activity) {
        this.activity = activity;

        nfcAdapter = NfcAdapter.getDefaultAdapter(activity);

        if (nfcAdapter == null)
            return;

        setupForegroundDispatch();
        setupAndroidBeam();
        resolveIntent(activity.getIntent());
    }

    @TargetApi(16)
    private void setupAndroidBeam() {
        nfcAdapter.setNdefPushMessageCallback(new NfcAdapter.CreateNdefMessageCallback() {
            @Override
            public NdefMessage createNdefMessage(NfcEvent event) {
                Log.d(TAG, "createNdefMessage called!");

                byte[] payload = "Karhades".getBytes();

                NdefRecord ndefRecord = NdefRecord.createMime(MIME_TYPE, payload);
                NdefMessage ndefMessage = new NdefMessage(ndefRecord);

                return ndefMessage;
            }
        }, activity);
    }

    /**
     * Intercept the intent and claim priority over other activities.
     * Block incoming intents that would launch due to the NFC NfcTag Discovery,
     * including the base activity as well (Duplicate).
     */
    private void setupForegroundDispatch() {
        // Creates an intent with tag data and delivers it
        // to this activity.
        Intent intent = new Intent(activity, activity.getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        nfcPendingIntent = PendingIntent.getActivity(activity, 0, intent, 0);

        // Listens for the NDEF_DISCOVERED intent.
        IntentFilter ndefDiscovered = new IntentFilter();
        ndefDiscovered.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndefDiscovered.addDataType(MIME_TYPE);
        } catch (Exception e) {
            Log.e(TAG, "Error at dispatching the intent. " + e.getMessage());
        }

        // Listens for the TECH_DISCOVERED intent.
        IntentFilter techDiscovered = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        // A custom technology list for the TECH_DISCOVERED filter.
        techList = new String[][]{
                new String[]{MifareUltralight.class.getName(), NfcA.class.getName(), Ndef.class.getName()},
                new String[]{NfcA.class.getName(), Ndef.class.getName()}};

        // Listens for the TAG_DISCOVERED intent.
        IntentFilter tagDiscovered = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);

        filters = new IntentFilter[]{ndefDiscovered, techDiscovered, tagDiscovered};
    }

    /**
     * Indicate that the application is ready to write to a nfc tag.
     */
    public static void enableTagWriteMode() {
        writeMode = true;
    }

    public static void disableTagWriteMode() {
        writeMode = false;
    }

    /**
     * Enable a foreground tag dispatch for this activity.
     */
    public void enableForegroundDispatch() {
        if (nfcAdapter != null)
            nfcAdapter.enableForegroundDispatch(activity, nfcPendingIntent, filters, techList);
    }

    /**
     * Disable the foreground tag dispatch for this activity.
     */
    public void disableForegroundDispatch() {
        if (nfcAdapter != null)
            nfcAdapter.disableForegroundDispatch(activity);
    }

    /**
     * Handle the newly discovered tag to either read from it or write to it.
     *
     * @param intent The intent to specify the tag discovery method.
     */
    public void resolveIntent(Intent intent) {
        Log.d(TAG, "Intent action: " + intent.getAction());

        if (writeMode && (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())
                || NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction()))) {
            Log.d(TAG, "Write Mode");

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            writeToTag(tag);
        } else if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Log.d(TAG, "Read Mode");

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            // Android beam.
            if (tag.getTechList()[0].equals("android.nfc.tech.Ndef")) {
                readFromBeam(intent);
            }
            // NFC tag.
            else {
                readFromTag(tag);
            }
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction()) || NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            Toast.makeText(activity, "Nfc tag not registered!", Toast.LENGTH_SHORT).show();
        }
    }

    private void readFromBeam(Intent intent) {
        Log.d(TAG, "readFromBeam called!");
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];

        String payload = new String(msg.getRecords()[0].getPayload());
        Toast.makeText(activity, "Payload: " + payload, Toast.LENGTH_SHORT).show();
    }

    /**
     * Read the NdefRecord of NdefMessage for the specified tag.
     *
     * @param tag The tag that will be read.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void readFromTag(android.nfc.Tag tag) {
        Log.d(TAG, "readFromTag called!");
        try {
            Ndef ndef = Ndef.get(tag);
            ndef.connect();
            NdefMessage ndefMessage = ndef.getNdefMessage();
            NdefRecord ndefRecord = ndefMessage.getRecords()[0];

            String mimeType = ndefRecord.toMimeType();
            Log.d(TAG, "MIME TYPE: " + mimeType);

            String payload = new String(ndefRecord.getPayload());
            Log.d(TAG, "Payload: " + payload);

            if (!mimeType.equals(MIME_TYPE)) {
                Toast.makeText(activity, "Nfc NfcTag not registered!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (payload.equals("tag")) {
                String tagId = ByteArrayToHexString(tag.getId());

                Log.d(TAG, "NfcTag ID: " + tagId);

                startActivityFromNFC(tagId);
                // Solve the Nfc Tag.
                MyTags.get(activity).solveNfcTag(tagId);
            } else {
                Toast.makeText(activity, "Nfc NfcTag not registered!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading from tag. " + e.getMessage());
        }
    }

    /**
     * Start the TagPagerActivity that will pass the tag id to
     * the TrackingTagFragment.
     *
     * @param tagId The tag id to open the appropriate NfcTag.
     */
    private void startActivityFromNFC(String tagId) {
        NfcTag nfcTag = MyTags.get(activity).getTag(tagId);

        if (nfcTag != null) {
            // Create an Intent and send the extra discovered NfcTag ID and
            // another extra to indicate that it's from the NFC discovery.
            Intent intentTagId = new Intent(activity, TagPagerActivity.class);
            intentTagId.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intentTagId.putExtra(TrackingTagFragment.EXTRA_TAG_ID, tagId);
            activity.startActivity(intentTagId);
        } else {
            Toast.makeText(activity, "Nfc NfcTag not registered!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Write to the specified tag the MIME type and a payload.
     *
     * @param tag The tag that will be written.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void writeToTag(android.nfc.Tag tag) {
        byte[] payload = "tag".getBytes();

        NdefRecord ndefRecord = NdefRecord.createMime(MIME_TYPE, payload);
        NdefMessage ndefMessage = new NdefMessage(ndefRecord);

        Ndef ndef = Ndef.get(tag);

        try {
            ndef.connect();
            ndef.writeNdefMessage(ndefMessage);

            Log.d(TAG, "Write to tag was successful!");
            onTagWriteListener.onTagWritten(OnTagWriteListener.STATUS_OK);
        } catch (Exception e) {
            Log.e(TAG, "Error when writing NdefMessage to NfcTag. " + e.getMessage());
            onTagWriteListener.onTagWritten(OnTagWriteListener.STATUS_ERROR);
        } finally {
            writeMode = false;
        }
    }

    /**
     * Convert the byte array into hex.
     *
     * @param byteArray The bytes array to convert.
     * @return The converted String array.
     */
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
