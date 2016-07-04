package com.karhades.tag_it.main.model;

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
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.karhades.tag_it.utils.PictureLoader;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * A wrapper class of NfcAdapter that can be bound to a given activity, to
 * enable NFC capabilities. It encapsulates the read/write tag and peer-to-peer
 * (Android Beam) operations of the NFC technology as well as more advanced
 * topics such as the foreground dispatch system.
 */
public class NfcHandler {

    private static final String TAG = "NfcHandler";

    /**
     * Constant describing the application's MIME type.
     */
    private static final String MIME_TYPE = "application/com.karhades.tag_it.nfchandler";

    /**
     * Constants needed for the PendingIntent.
     */
    private static final int REQUEST_TAG = 0;
    private static final int FLAGS = 0;

    /**
     * The PendingIntent that will be passed to
     * the Foreground Dispatch System.
     */
    private PendingIntent nfcPendingIntent;

    /**
     * An IntentFilter array containing one or more
     * tag discovery intent filters to be used for
     * the Foreground Dispatch System.
     */
    private IntentFilter[] discoveryIntentFilters;

    /**
     * A String array that contains all the tag technologies
     * that are supported by the ACTION_TECH_DISCOVERED intent
     * filter.
     */
    private String[][] techList;

    /**
     * Represents the NFC adapter of the device.
     */
    private NfcAdapter nfcAdapter;

    /**
     * Activity needed for the context based functions.
     */
    private Activity activity;

    /**
     * A boolean indicating the operation mode.
     */
    private static boolean writeMode = false;

    /**
     * Listener reference.
     */
    private OnTagWriteListener onTagWriteListener;

    /**
     * Enum reference.
     */
    private static Mode mode;

    /**
     * Interface definition for a callback to be invoked when
     * the tag had data written on it.
     */
    public interface OnTagWriteListener {
        int STATUS_OK = 0;
        int STATUS_ERROR = 1;

        /**
         * This method will be invoked when the discovered tag
         * had data written on it.
         *
         * @param status An int representing a positive outcome
         *               with STATUS_OK and a negative one with
         *               STATUS_ERROR.
         * @param tagId  A String representing the ID of the tag
         *               after the write operation.
         */
        void onTagWritten(int status, String tagId);
    }

    /**
     * Register a callback to be invoked when the tag had data written to it.
     *
     * @param onTagWriteListener The callback that will run.
     */
    public void setOnTagWriteListener(OnTagWriteListener onTagWriteListener) {
        this.onTagWriteListener = onTagWriteListener;
    }

    /**
     * Enum describing the write mode.
     */
    public enum Mode {
        OVERWRITE, CREATE_NEW
    }

    public static void setMode(Mode mode) {
        NfcHandler.mode = mode;
    }

    /**
     * Setup the NfcAdapter and the Foreground Dispatch System.
     *
     * @param activity The activity needed by the Android System.
     */
    public void setupNfcHandler(Activity activity) {
        this.activity = activity;

        // Get the phone's NFC adapter.
        nfcAdapter = NfcAdapter.getDefaultAdapter(activity);

        // If there is an NFC adapter.
        if (nfcAdapter != null) {
            // If NFC is not enabled on device.
            if (!nfcAdapter.isEnabled()) {
                startSettingsActivity("Turn on NFC.", Settings.ACTION_NFC_SETTINGS);
            }
            // If Android Beam feature is not enabled on device.
            else if (!nfcAdapter.isNdefPushEnabled()) {
                startSettingsActivity("Turn on Android Beam.", Settings.ACTION_NFCSHARING_SETTINGS);
            }

            setupForegroundDispatch();
        }
    }

    /**
     * Helper method for starting the Settings Activity.
     *
     * @param message A String representing the message that will
     *                be shown to the user.
     * @param action  A String representing the intent action.
     */
    private void startSettingsActivity(String message, String action) {
        // Inform user.
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show();

        // NFC is disabled, show the settings UI to enable NFC.
        Intent settingsIntent = new Intent();
        settingsIntent.setAction(action);
        activity.startActivity(settingsIntent);
    }

    /**
     * Intercept the intent and claim priority over other activities.
     * Block incoming intents that would launch due to the NFC tag Discovery,
     * including the base activity as well (Duplicate).
     */
    private void setupForegroundDispatch() {
        // Create an intent with tag data and deliver it
        // to the given activity.
        Intent intent = new Intent(activity, activity.getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        // Create a PendingIntent that will be passed to Foreground Dispatch System.
        nfcPendingIntent = PendingIntent.getActivity(activity, REQUEST_TAG, intent, FLAGS);

        // Register an NDEF_DISCOVERED intent filter.
        IntentFilter ndefDiscovered = new IntentFilter();
        ndefDiscovered.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndefDiscovered.addDataType(MIME_TYPE);
        } catch (Exception e) {
            Log.e(TAG, "Error dispatching intent. " + e.getMessage());
        }

        // Register a TECH_DISCOVERED intent filter.
        IntentFilter techDiscovered = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        // A custom technology list for the above TECH_DISCOVERED intent filter.
        techList = new String[][]{new String[]{MifareUltralight.class.getName(), NfcA.class.getName(), Ndef.class.getName()}};

        // Register a TAG_DISCOVERED intent filter.
        IntentFilter tagDiscovered = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);

        // Create an IntentFilter array with all discovery types.
        discoveryIntentFilters = new IntentFilter[]{ndefDiscovered, techDiscovered, tagDiscovered};
    }

    /**
     * Set whether the application is ready to write to an NFC tag.
     */
    public static void setWriteMode(boolean writeMode) {
        NfcHandler.writeMode = writeMode;
    }

    public static boolean getWriteMode() {
        return writeMode;
    }

    /**
     * Enable a foreground tag dispatch for this activity.
     */
    public void enableForegroundDispatch() {
        nfcAdapter.enableForegroundDispatch(activity, nfcPendingIntent, discoveryIntentFilters, techList);
    }

    /**
     * Disable the foreground tag dispatch for this activity.
     */
    public void disableForegroundDispatch() {
        nfcAdapter.disableForegroundDispatch(activity);
    }

    /**
     * Handle an NFC tag discovery and write data on it.
     *
     * @param intent The NFC intent to resolve the tag discovery type.
     * @return A boolean indicating whether the write operation is ready
     * to start.
     */
    public boolean handleNfcWriteTag(Intent intent) {
        // If any tag technology is discovered.
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())
                || NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            // Get the extra from the intent containing the tag.
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            // Get the discovered tag ID.
            String tagId = getTagId(tag);

            // Return the operation result.
            return writeTag(tag, tagId);
        }
        return false;
    }

    /**
     * Get the ID (NdefRecord ID) from the specified tag.
     *
     * @param tag The discovered NFC tag.
     * @return A String representing the ID of
     * the discovered tag or null if an error
     * occurred.
     */
    private String getTagId(Tag tag) {
        // Get the NDEF formatted tag.
        Ndef ndef = Ndef.get(tag);

        // The tag ID that will be returned.
        String newTagId = null;

        try {
            // Enable I/O operations with NFC tag.
            ndef.connect();

            // Read the NdefMessage from the NFC tag.
            NdefMessage ndefMessage = ndef.getNdefMessage();
            // If it isn't NDEF formatted.
            if (ndefMessage == null) {
                // Create a new tag ID.
                UUID uuid = UUID.randomUUID();
                newTagId = uuid.toString();

                // Return the new tag ID.
                return newTagId;
            }

            // Get the first NdefRecord from the NdefMessage.
            NdefRecord ndefRecord = ndefMessage.getRecords()[0];
            // Get the ID of NdefRecord.
            String oldTagId = new String(ndefRecord.getId());

            // If it's a new tag.
            if (mode == Mode.CREATE_NEW) {
                // Search if the tag exists.
                NfcTag nfcTag = MyTags.get(activity).getNfcTag(oldTagId);

                // If NFC tag exists, don't create another one.
                if (nfcTag != null) {
                    throw new TagExistsException("NFC tag already exists!");
                }

                // Create a new NdefRecord ID.
                UUID uuid = UUID.randomUUID();
                newTagId = uuid.toString();
            }
            // If it's a rewrite.
            else if (mode == Mode.OVERWRITE) {
                // Get the existing NdefRecord ID.
                newTagId = oldTagId;
            }

            // Return the new tag ID.
            return newTagId;
        } catch (TagExistsException e) {
            Log.e(TAG, "Error retrieving tag ID. " + e.getMessage(), e);

            // Inform user.
            Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (FormatException | IOException e) {
            Log.e(TAG, "Error reading tag " + e.getMessage(), e);
        } finally {
            if (ndef != null) {
                try {
                    ndef.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing tag " + e.getMessage(), e);
                }
            }
        }
        return null;
    }

    /**
     * Write the MIME type and payload to the specified tag
     * and return the operation result.
     *
     * @param tag   The Tag object representing the NFC tag.
     * @param tagId A String representing the ID that will
     *              be written to the specified tag.
     * @return A boolean indicating whether the write operation
     * succeeded.
     */
    private boolean writeTag(Tag tag, String tagId) {
        // Get the NDEF formatted tag.
        Ndef ndef = Ndef.get(tag);

        try {
            if (tagId == null) {
                throw new NullPointerException("Tag ID cannot be null.");
            }

            // Enable I/O operations with NFC tag.
            ndef.connect();

            // Get an NdefMessage that will be written on the tag.
            NdefMessage ndefMessage = createNdefMessage(tagId);
            // Write the NdefMessage to the tag.
            ndef.writeNdefMessage(ndefMessage);

            // Set STATUS_OK.
            onTagWriteListener.onTagWritten(OnTagWriteListener.STATUS_OK, tagId);

            Log.d(TAG, "Tag writing operation was successful!");

            // Write operation succeeded.
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error when writing NdefMessage to NfcTag. " + e.getMessage(), e);

            // Set STATUS_ERROR.
            onTagWriteListener.onTagWritten(OnTagWriteListener.STATUS_ERROR, null);
        } finally {
            if (ndef != null) {
                try {
                    ndef.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing tag " + e.getMessage(), e);
                }
            }
            if (onTagWriteListener != null) {
                onTagWriteListener = null;
            }

            // Set write mode to default value.
            writeMode = false;
        }
        // Write operation failed.
        return false;
    }

    /**
     * Create an NdefRecord with the specified ID, encapsulate it into an NdefMessage
     * and return it.
     *
     * @param ndefRecordId A String representing the ID of the NdefRecord.
     * @return The created NdefMessage.
     */
    private NdefMessage createNdefMessage(String ndefRecordId) {
        // TNF (Type Name Format).
        short tnf = NdefRecord.TNF_MIME_MEDIA;

        // MIME type.
        byte[] mimeType = MIME_TYPE.getBytes();

        // NdefRecord ID (Tag ID).
        byte[] id = ndefRecordId.getBytes();

        // Payload data.
        byte[] payload = "tag_it".getBytes();

        // Create an NdefRecord that will be encapsulated into an NdefMessage.
        NdefRecord ndefRecord = new NdefRecord(tnf, mimeType, id, payload);

        // Create an NdefMessage that contains the NdefRecord and
        // will be encapsulated into an intent.
        return new NdefMessage(ndefRecord);
    }

    /**
     * Handle an NFC tag discovery, read and return the underlying tag ID.
     *
     * @param intent The intent that is sent from the Android System at
     *               discovery time.
     * @return A String representing the discovered tag ID or null if
     * an error occurred.
     */
    public String handleNfcReadTag(Intent intent) {
        try {
            if (intent.getAction() == null) {
                return null;
            }

            // If the discovered tag maps to MIME type.
            if (intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
                // Get the extra from the intent containing the tag.
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

                // Get the ID of the discovered tag.
                String tagId = readTag(tag);

                // Get the NFC tag from the list that corresponds to
                // the discovered tag ID.
                NfcTag nfcTag = MyTags.get(activity).getNfcTag(tagId);
                // If there isn't a corresponding NfcTag.
                if (nfcTag == null) {
                    throw new TagNotRegisteredException("No corresponding NfcTag found for the given tag ID.");
                }

                // Return the discovered tag ID.
                return tagId;
            }
            // If the discovered tag cannot be mapped to MIME type.
            else if (intent.getAction().equals(NfcAdapter.ACTION_TECH_DISCOVERED) || intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
                throw new TagNotRegisteredException("NFC tag cannot be mapped to MIME type.");
            }
        } catch (TagNotRegisteredException e) {
            Log.e(TAG, "Error reading tag. " + e.getMessage(), e);

            // Inform user.
            Toast.makeText(activity, "NFC tag not registered!", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    /**
     * Read the NdefRecord of NdefMessage and return
     * the ID for the specified tag.
     *
     * @param tag The Tag object representing the NFC tag.
     * @return The tag ID or null if an error occurred.
     * @throws TagNotRegisteredException
     */
    private String readTag(Tag tag) throws TagNotRegisteredException {
        // Get the NDEF formatted tag.
        Ndef ndef = Ndef.get(tag);

        try {
            // Enable I/O operations.
            ndef.connect();

            // Read the NdefMessage from the NFC tag.
            NdefMessage ndefMessage = ndef.getNdefMessage();
            // Get the first NdefRecord from the NdefMessage.
            NdefRecord ndefRecord = ndefMessage.getRecords()[0];

            // Get the payload from the NdefRecord.
            String payload = new String(ndefRecord.getPayload());

            // If the discovered tag matches this application's signature.
            if (payload.equals("tag_it")) {
                // Return the ID of NdefRecord.
                return new String(ndefRecord.getId());
            }
            // The discovered tag is not registered in this application.
            else {
                throw new TagNotRegisteredException("NFC tag payload doesn't match.");
            }
        } catch (FormatException | IOException e) {
            Log.e(TAG, "Error reading tag " + e.getMessage(), e);
        } finally {
            if (ndef != null) {
                try {
                    ndef.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing tag " + e.getMessage(), e);
                }
            }
        }
        return null;
    }

    /**
     * Register an Android Beam callback and send the data when
     * the devices are in proximity.
     */
    public void registerAndroidBeamShareFiles() {
        nfcAdapter.setBeamPushUrisCallback(new NfcAdapter.CreateBeamUrisCallback() {
            @Override
            public Uri[] createBeamUris(NfcEvent event) {
                // Return the URIs array that Android Beam is going to send.
                return MyTags.get(activity).createFileUrisArray();
            }
        }, activity);
    }

    /**
     * Handle the Intent.ACTION_VIEW action and move the files
     * from the android beam folder to the external private storage.
     *
     * @param intent The intent to get the action.
     */
    public void handleAndroidBeamReceivedFiles(Intent intent) {
        // Get the intent action.
        String action = intent.getAction();

        // If the intent action isn't ACTION_VIEW.
        if (!action.equals(Intent.ACTION_VIEW)) {
            return;
        }

        // Get the URI from the Intent
        // that points to the first file.
        Uri firstFileUri = intent.getData();

        String parentPath = null;

        // If the URI has a scheme of file.
        if (firstFileUri.getScheme().equals("file")) {
            parentPath = getParentFromFileUri(firstFileUri);
        }
        // If the URI has a scheme of content.
        else if (firstFileUri.getScheme().equals("content")) {
            parentPath = getParentFromContentUri(firstFileUri);
        }

        moveFilesToPrivateExternalStorage(parentPath);
    }

    /**
     * Return the parent file directory of the given URI
     * that has a file scheme.
     *
     * @param uri A URI object to retrieve the path.
     * @return A String representing the parent file
     * directory.
     */
    private String getParentFromFileUri(Uri uri) {
        // Get the path of the URI.
        String fileName = uri.getPath();

        // Create a File object for this filename.
        File file = new File(fileName);

        // Get a string containing the file's parent directory.
        return file.getParent();
    }

    /**
     * Return the parent file directory of the given URI
     * that has a content scheme.
     *
     * @param uri A URI object needed by the cursor.
     * @return A String representing the parent file directory or
     * null if the authority is not from a media provider.
     */
    private String getParentFromContentUri(Uri uri) {
        // If it is not a media authority representing the Media Provider.
        if (!uri.getAuthority().equals(MediaStore.AUTHORITY)) {
            return null;
        }

        // Get the column that contains the filename.
        String[] projection = {MediaStore.MediaColumns.DATA};
        // Get the cursor that corresponds to the given query (SELECT _data FROM MediaStore).
        Cursor cursor = activity.getContentResolver().query(uri, projection, null, null, null);

        if (cursor == null) {
            return null;
        }
        // If the cursor is empty.
        if (!cursor.moveToFirst()) {
            return null;
        }

        // Get the index of DATA column that corresponds to the filename.
        int columnIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
        // Get the full file name, including path.
        String fileName = cursor.getString(columnIndex);
        // Close the cursor.
        cursor.close();

        // Create a File object for the filename to retrieve the parent directory.
        File copiedFile = new File(fileName);
        // Return the parent directory of the file.
        return new File(copiedFile.getParent()).toString();
    }

    /**
     * Move files to the private external storage of the device from the
     * specified file path.
     *
     * @param parentFilePath A String representing the parent file directory
     *                       where the files reside.
     */
    private void moveFilesToPrivateExternalStorage(String parentFilePath) {
        // Get tags.txt from Android Beam.
        File beamTagsJSONFile = new File(parentFilePath + File.separator + "tags.txt");
        // Existing file path.
        File existingTagsJSONFile = new File(activity.getExternalFilesDir(null) + File.separator + "tags.txt");
        // Overwrite the existing file with the beam file.
        renameFile(beamTagsJSONFile, existingTagsJSONFile);

        // Load the skeleton from the tags.txt file, received from Android Beam.
        MyTags.get(activity).loadTags();

        // Get pictures from Android Beam through the newly tags.txt file.
        // Get the size of the received tags.txt file.
        int size = MyTags.get(activity).getNfcTags().size();

        // For each NFC tag in the received tags.txt file.
        for (int i = 0; i < size; i++) {
            // Get the nfc tag.
            NfcTag nfcTag = MyTags.get(activity).getNfcTags().get(i);

            // Delete memory cache for the old image.
            PictureLoader.invalidateWithPicasso(activity, nfcTag.getPictureFilePath());

            // Get the Beam picture file path.
            File beamPictureFile = new File(parentFilePath + File.separator + "Tag" + nfcTag.getTagId() + ".jpg");
            // Get the existing picture file path.
            File existingPictureFile = new File(nfcTag.getPictureFilePath());
            // Overwrite the existing file with the beam file.
            renameFile(beamPictureFile, existingPictureFile);
        }
    }

    /**
     * Renames the given source file to the specified destination file.
     *
     * @param source      A File object representing the source file.
     * @param destination A File object representing the destination file.
     */
    private void renameFile(File source, File destination) {
        if (!source.renameTo(destination)) {
            Log.e(TAG, "Unable to rename " + source.getAbsolutePath());
        }
    }

    /**
     * Thrown when an NFC tag is not registered in the application.
     */
    public class TagNotRegisteredException extends RuntimeException {
        TagNotRegisteredException(String message) {
            super(message);
        }
    }

    /**
     * Thrown when an NFC tag already exists within the application.
     */
    public class TagExistsException extends RuntimeException {
        TagExistsException(String message) {
            super(message);
        }
    }
}
