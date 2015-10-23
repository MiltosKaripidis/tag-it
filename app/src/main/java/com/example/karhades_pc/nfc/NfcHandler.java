package com.example.karhades_pc.nfc;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
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
import com.example.karhades_pc.tag_it.TrackingTagFragment;
import com.example.karhades_pc.tag_it.TrackingTagPagerActivity;

import java.io.IOException;

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

    public enum Mode {
        OVERWRITE, CREATE_NEW
    }

    private static Mode mode;

    public static void setMode(Mode newMode) {
        mode = newMode;
    }

    private static OnTagWriteListener onTagWriteListener;

    /**
     * Interface definition for a callback to be invoked when
     * the tag had data written to it.
     */
    public interface OnTagWriteListener {
        int STATUS_OK = 0;
        int STATUS_ERROR = 1;

        void onTagWritten(int status, String tagId);
    }

    /**
     * Register a callback to be invoked when the tag had data written to it.
     *
     * @param newOnTagWriteListener The callback that will run.
     */
    public static void setOnTagWriteListener(OnTagWriteListener newOnTagWriteListener) {
        onTagWriteListener = newOnTagWriteListener;
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
    public static void toggleTagWriteMode(boolean value) {
        writeMode = value;
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
     * Listen for a NDEF tag discovery that needs to be read.
     *
     * @param intent The NFC intent to resolve the tag discovery type.
     */
    public void enableNfcReadTag(Intent intent) {
        try {
            if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                // NFC tag.
                readFromTag(tag);
            } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction()) || NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
                throw new TagIdNotRegisteredException("Nfc tag not registered!");
            }
        } catch (TagIdNotRegisteredException e) {
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error reading from tag. " + e.getMessage());
        }
    }

    /**
     * Read the NdefRecord of NdefMessage for the specified tag.
     *
     * @param tag The tag that will be read.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void readFromTag(Tag tag) throws IOException, FormatException {
        Ndef ndef = Ndef.get(tag);
        ndef.connect();
        NdefMessage ndefMessage = ndef.getNdefMessage();
        NdefRecord ndefRecord = ndefMessage.getRecords()[0];

        String mimeType = ndefRecord.toMimeType();
        //Log.d(TAG, "MIME TYPE: " + mimeType);

        String payload = new String(ndefRecord.getPayload());
        //Log.d(TAG, "Payload: " + payload);

        if (mimeType.equals(MIME_TYPE) && payload.equals("tag")) {
            String tagId = ByteArrayToHexString(tag.getId());
            //Log.d(TAG, "NfcTag ID: " + tagId);

            // Start the solved activity.
            startActivityFromNFC(tagId);
        } else {
            throw new TagIdNotRegisteredException("Nfc tag not registered!");
        }
    }

    /**
     * Start the TrackingTagPagerActivity that will pass the tag id to
     * the TrackingTagFragment.
     *
     * @param tagId The tag id to open the appropriate NfcTag.
     */
    private void startActivityFromNFC(String tagId) {
        NfcTag nfcTag = MyTags.get(activity).getNfcTag(tagId);

        if (nfcTag != null) {
            // Create an Intent and send the extra discovered NfcTag ID and
            // another extra to indicate that it's from the NFC discovery.
            Intent intent = new Intent(activity, TrackingTagPagerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra(TrackingTagFragment.EXTRA_TAG_ID, tagId);
            intent.putExtra(TrackingTagFragment.EXTRA_TAG_DISCOVERED, true);
            activity.startActivity(intent);
        } else {
            throw new TagIdNotRegisteredException("Nfc tag not registered!");
        }
    }

    /**
     * TODO: Pending documentation.
     *
     * @param intent The NFC intent to resolve the tag discovery type.
     */
    public void enableNfcAndroidBeam(Intent intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            // Android beam.
            if (tag.getTechList()[0].equals("android.nfc.tech.Ndef")) {
                readFromBeam(intent);
            }
        }
    }

    /**
     * TODO: Pending documentation.
     *
     * @param intent The intent to resolve the raw data.
     */
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
     * Listen for a tag discovery that needs to be written.
     *
     * @param intent The NFC intent to resolve the tag discovery type.
     */
    public boolean enableNfcWriteTag(Intent intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())
                || NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            if (writeMode) {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                writeToTag(tag);
                return true;
            }
        }
        return false;
    }

    /**
     * Write to the specified tag the MIME type and a payload.
     *
     * @param tag The tag that will be written.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void writeToTag(Tag tag) {
        try {
            // Get discovered tag id.
            String tagId = ByteArrayToHexString(tag.getId());

            // If it's a new tag.
            if (mode == Mode.CREATE_NEW) {
                // Search if the tag exists.
                NfcTag nfcTag = MyTags.get(activity).getNfcTag(tagId);

                // If NFC tag exists, don't create another one.
                if (nfcTag != null) {
                    throw new TagIdExistsException("Nfc tag already exists!");
                }
            }
            // If it exists.
            else if (mode == Mode.OVERWRITE) {
                // DO NOTHING.
            }

            /* START WRITING */
            byte[] payload = "tag".getBytes();

            // Encapsulate a NdefRecord inside a NdefMessage.
            NdefRecord ndefRecord = NdefRecord.createMime(MIME_TYPE, payload);
            NdefMessage ndefMessage = new NdefMessage(ndefRecord);

            // Get an interface to connect with NFC tag.
            Ndef ndef = Ndef.get(tag);

            // Connect and write data to NFC tag.
            ndef.connect();
            ndef.writeNdefMessage(ndefMessage);

            Log.d(TAG, "Write to tag was successful!");

            // Set STATUS_OK.
            onTagWriteListener.onTagWritten(OnTagWriteListener.STATUS_OK, tagId);
        } catch (TagIdExistsException e) {
            Log.e(TAG, "Error when writing NdefMessage to NfcTag. " + e.getMessage());

            // Inform user.
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();

            // Set STATUS_ERROR.
            onTagWriteListener.onTagWritten(OnTagWriteListener.STATUS_ERROR, null);
        } catch (Exception e) {
            Log.e(TAG, "Error when writing NdefMessage to NfcTag. " + e.getMessage());

            // Inform user.
            Toast.makeText(activity, "Could not write to nfc tag!", Toast.LENGTH_SHORT).show();

            // Set STATUS_ERROR.
            onTagWriteListener.onTagWritten(OnTagWriteListener.STATUS_ERROR, null);
        } finally {
            writeMode = false;
            if (onTagWriteListener != null) {
                onTagWriteListener = null;
            }
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

    public class TagIdExistsException extends RuntimeException {
        TagIdExistsException(String message) {
            super(message);
        }
    }

    public class TagIdNotRegisteredException extends RuntimeException {
        TagIdNotRegisteredException(String message) {
            super(message);
        }
    }
}
