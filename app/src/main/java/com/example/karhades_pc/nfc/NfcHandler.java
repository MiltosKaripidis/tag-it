package com.example.karhades_pc.nfc;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
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
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.example.karhades_pc.tag_it.MyTags;
import com.example.karhades_pc.tag_it.NfcTag;
import com.example.karhades_pc.tag_it.TrackingTagFragment;
import com.example.karhades_pc.tag_it.TrackingTagPagerActivity;
import com.example.karhades_pc.utils.PictureLoader;
import com.example.karhades_pc.utils.TagJSONSerializer;

import java.io.File;
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

        // Check whether NFC is enabled on device.
        if (!nfcAdapter.isEnabled()) {
            // NFC is disabled, show the settings UI
            // to enable NFC
            Toast.makeText(activity, "Please enable NFC.", Toast.LENGTH_SHORT).show();
            activity.startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
        }
        // Check whether Android Beam feature is enabled on device.
        else if (!nfcAdapter.isNdefPushEnabled()) {
            // Android Beam is disabled, show the settings UI
            // to enable Android Beam
            Toast.makeText(activity, "Please enable Android Beam.", Toast.LENGTH_SHORT).show();
            activity.startActivity(new Intent(Settings.ACTION_NFCSHARING_SETTINGS));
        }

        setupForegroundDispatch();
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
     * Send small amount of data like URL, text, etc.
     */
    public void setupAndroidBeam() {
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
     * Listen for an android beam intent call.
     *
     * @param intent The NFC intent to resolve the tag discovery type.
     */
    public void enableNfcAndroidBeam(Intent intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            // If it's from Android beam.
            if (tag.getTechList()[0].equals("android.nfc.tech.Ndef")) {
                readFromBeam(intent);
            }
        }
    }

    /**
     * Read the data from the intent sent of android beam.
     *
     * @param intent The intent to resolve the raw data.
     */
    private void readFromBeam(Intent intent) {

        // Get raw data from intent.
        Parcelable[] rawData = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        // Message sent during the beam.
        NdefMessage ndefMessage = (NdefMessage) rawData[0];

        // Create String from raw data.
        String payload = new String(ndefMessage.getRecords()[0].getPayload());

        Toast.makeText(activity, "Payload: " + payload, Toast.LENGTH_SHORT).show();
    }

    /**
     * Listen for an android beam event and send the data.
     */
    public void enableAndroidBeamShareFiles() {
        nfcAdapter.setBeamPushUrisCallback(new NfcAdapter.CreateBeamUrisCallback() {
            @Override
            public Uri[] createBeamUris(NfcEvent event) {
                Uri[] filesUris = MyTags.get(activity).createFileUrisArray();

                String mimeType = getMimeType(filesUris[0].getPath());
                Log.d("NfcHandler", "MIME_TYPE: " + mimeType);

                return filesUris;
            }
        }, activity);
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    /**
     * Handle the Intent.ACTION_VIEW action and move the files
     * from the android beam folder to the external private storage.
     *
     * @param intent The intent to get the cation.
     */
    public void handleAndroidBeamReceivedFiles(Intent intent) {
        String action = intent.getAction();

        // For ACTION_VIEW, the Activity is being asked to display data.
        // Get the URI.
        if (TextUtils.equals(action, Intent.ACTION_VIEW)) {
            // Get the URI from the Intent.
            Uri beamUri = intent.getData();
            Log.d("NfcHandler", "SCHEME: " + beamUri.getScheme());

            String parentFilePath = null;
            // Test for the type of URI, by getting its scheme value.
            if (TextUtils.equals(beamUri.getScheme(), "file")) {
                parentFilePath = handleFileUri(beamUri);
            } else if (TextUtils.equals(beamUri.getScheme(), "content")) {
                parentFilePath = handleContentUri(beamUri);
            }

            // Get tags.txt from android beam.
            File beamTagsJSONFile = new File(parentFilePath + File.separator + "tags.txt");
            // Existing file path.
            File existingTagsJSONFile = new File(activity.getExternalFilesDir(null) + File.separator + "tags.txt");
            // Overwrite the existing file with the beam file.
            beamTagsJSONFile.renameTo(existingTagsJSONFile);

            TagJSONSerializer tagJSONSerializer = new TagJSONSerializer(activity, null);
            try {
                // Load the skeleton from the tags.txt file, received from Android beam.
                MyTags.get(activity).setNfcTags(tagJSONSerializer.loadTagsExternal());
            } catch (Exception e) {
                Log.e(TAG, "Error reading from tags.txt");
            }

            // Get pictures from android beam through the newly tags.txt file.
            int size = MyTags.get(activity).getNfcTags().size();
            for (int i = 0; i < size; i++) {
                NfcTag nfcTag = MyTags.get(activity).getNfcTags().get(i);

                // Delete memory cache.
                PictureLoader.invalidateWithPicasso(activity, nfcTag.getPictureFilePath());

                // Beam picture file path.
                File beamPictureFile = new File(parentFilePath + File.separator + "Tag" + nfcTag.getTagId() + ".jpg");
                // Existing picture file path.
                File existingPictureFile = new File(nfcTag.getPictureFilePath());
                // Overwrite the existing file with the beam file.
                beamPictureFile.renameTo(existingPictureFile);
            }
        }
    }

    private String handleFileUri(Uri beamUri) {
        // Get the path part of the URI.
        String fileName = beamUri.getPath();
        // Create a File object for this filename.
        File copiedFile = new File(fileName);
        // Get a string containing the file's parent directory.
        return copiedFile.getParent();
    }

    public String handleContentUri(Uri beamUri) {
        // Position of the filename in the query Cursor.
        int filenameIndex;
        // File object for the filename.
        File copiedFile;
        // The filename stored in MediaStore.
        String fileName;
        // Test the authority of the URI.
        if (!TextUtils.equals(beamUri.getAuthority(), MediaStore.AUTHORITY)) {
            /*
             * Handle content URIs for other content providers
             */
            // For a MediaStore content URI
        } else {
            // Get the column that contains the file name
            String[] projection = {MediaStore.MediaColumns.DATA};
            Cursor pathCursor = activity.getContentResolver().query(beamUri, projection, null, null, null);
            // Check for a valid cursor
            if (pathCursor != null && pathCursor.moveToFirst()) {
                // Get the column index in the Cursor
                filenameIndex = pathCursor.getColumnIndex(MediaStore.MediaColumns.DATA);
                // Get the full file name including path
                fileName = pathCursor.getString(filenameIndex);
                // Create a File object for the filename
                copiedFile = new File(fileName);
                // Return the parent directory of the file
                return copiedFile.getParent();
            } else {
                // The query didn't work; return null
                return null;
            }
        }
        return null;
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
