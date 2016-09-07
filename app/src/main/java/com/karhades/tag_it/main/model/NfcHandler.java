/*
 * Copyright (C) 2016 Karipidis Miltiadis
 */

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
    private PendingIntent mNfcPendingIntent;

    /**
     * An IntentFilter array containing one or more
     * tag discovery intent filters to be used for
     * the Foreground Dispatch System.
     */
    private IntentFilter[] mDiscoveryIntentFilters;

    /**
     * A String array that contains all the tag technologies
     * that are supported by the ACTION_TECH_DISCOVERED intent
     * filter.
     */
    private String[][] mTechList;

    /**
     * Represents the NFC adapter of the device.
     */
    private NfcAdapter mNfcAdapter;

    /**
     * Activity needed for the context based functions.
     */
    private Activity mActivity;

    /**
     * A boolean indicating the operation mode.
     */
    private static boolean sWriteMode = false;

    /**
     * Listener reference.
     */
    private OnTagWriteListener mOnTagWriteListener;

    /**
     * Enum reference.
     */
    private static Mode sMode;

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
     * Registers a callback to be invoked when the tag had data written to it.
     *
     * @param onTagWriteListener The callback that will run.
     */
    public void setOnTagWriteListener(OnTagWriteListener onTagWriteListener) {
        mOnTagWriteListener = onTagWriteListener;
    }

    /**
     * Enum describing the write mode.
     */
    public enum Mode {
        CREATE, OVERWRITE
    }

    public static void setMode(Mode mode) {
        NfcHandler.sMode = mode;
    }

    /**
     * Setups the NfcAdapter and the Foreground Dispatch System.
     *
     * @param activity The activity needed by the Android System.
     */
    public void setupNfcHandler(Activity activity) {
        mActivity = activity;

        // Get the phone's NFC adapter.
        mNfcAdapter = NfcAdapter.getDefaultAdapter(activity);

        // If there isn't an NFC adapter.
        if (mNfcAdapter == null) {
            return;
        }

        // If NFC is not enabled on device.
        if (!mNfcAdapter.isEnabled()) {
            startSettingsActivity("Turn on NFC.", Settings.ACTION_NFC_SETTINGS);
        }
        // If Android Beam feature is not enabled on device.
        else if (!mNfcAdapter.isNdefPushEnabled()) {
            startSettingsActivity("Turn on Android Beam.", Settings.ACTION_NFCSHARING_SETTINGS);
        }

        setupForegroundDispatch();
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
        Toast.makeText(mActivity, message, Toast.LENGTH_LONG).show();

        // NFC is disabled, show the settings UI to enable NFC.
        Intent settingsIntent = new Intent();
        settingsIntent.setAction(action);
        mActivity.startActivity(settingsIntent);
        mActivity.finish();
    }

    /**
     * Intercepts the intent and claim priority over other activities.
     * Blocks incoming intents that would launch due to the NFC tag Discovery,
     * including the base activity as well (Duplicate).
     */
    private void setupForegroundDispatch() {
        // Create an intent with tag data and deliver it
        // to the given activity.
        Intent intent = new Intent(mActivity, mActivity.getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        // Create a PendingIntent that will be passed to Foreground Dispatch System.
        mNfcPendingIntent = PendingIntent.getActivity(mActivity, REQUEST_TAG, intent, FLAGS);

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
        mTechList = new String[][]{new String[]{MifareUltralight.class.getName(), NfcA.class.getName(), Ndef.class.getName()}};

        // Register a TAG_DISCOVERED intent filter.
        IntentFilter tagDiscovered = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);

        // Create an IntentFilter array with all discovery types.
        mDiscoveryIntentFilters = new IntentFilter[]{ndefDiscovered, techDiscovered, tagDiscovered};
    }

    /**
     * Sets whether the application is ready to write to an NFC tag.
     */
    public static void setWriteMode(boolean writeMode) {
        NfcHandler.sWriteMode = writeMode;
    }

    public static boolean getWriteMode() {
        return sWriteMode;
    }

    /**
     * Enables a foreground tag dispatch for this activity.
     */
    public void enableForegroundDispatch() {
        mNfcAdapter.enableForegroundDispatch(mActivity, mNfcPendingIntent, mDiscoveryIntentFilters, mTechList);
    }

    /**
     * Disables the foreground tag dispatch for this activity.
     */
    public void disableForegroundDispatch() {
        mNfcAdapter.disableForegroundDispatch(mActivity);
    }

    /**
     * Handles an NFC tag discovery and writes data on it.
     *
     * @param intent The NFC intent to resolve the tag discovery type.
     * @return A boolean indicating whether the write operation succeeded.
     */
    public boolean handleNfcTagWrite(Intent intent) {
        // If any tag technology is discovered.
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())
                || NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {

            // Gets the extra from the intent containing the tag.
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            // A String representing the ID that will be written on the tag.
            String tagId = null;

            // If it's a new tag.
            if (sMode.equals(Mode.CREATE)) {
                tagId = createTagId(tag);
            }
            // If it's an existing tag.
            else if (sMode.equals(Mode.OVERWRITE)) {
                tagId = getTagId(tag);
            }

            // Returns the operation result.
            return writeTag(tag, tagId);
        }

        return false;
    }

    /**
     * Creates a new tag ID for the specified tag. Returns null if the tag exists within the
     * application.
     *
     * @param tag The discovered NFC tag.
     * @return A string representing the new tag ID or null if the tag already exists.
     */
    private String createTagId(Tag tag) {
        try {
            String tagId = getTagId(tag);

            // Searches if the tag exists.
            NfcTag nfcTag = MyTags.get(mActivity).getNfcTag(tagId);

            // If NFC tag exists, don't create another one.
            if (nfcTag != null) {
                throw new TagExistsException("NFC tag already exists!");
            }

            // Creates and returns the new tag ID.
            UUID uuid = UUID.randomUUID();
            return uuid.toString();
        } catch (TagExistsException e) {
            Log.e(TAG, "Error retrieving tag ID. " + e.getMessage(), e);
            Toast.makeText(mActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    /**
     * Gets the ID (NdefRecord ID) from the specified tag.
     *
     * @param tag The discovered NFC tag.
     * @return A String representing the ID of
     * the discovered tag or null if an error
     * occurred.
     */
    private String getTagId(Tag tag) {
        // Gets the NDEF formatted tag.
        Ndef ndef = Ndef.get(tag);

        try {
            // Reads the cached NdefMessage from the NFC tag at discovery time.
            NdefMessage ndefMessage = ndef.getCachedNdefMessage();

            // Gets the first NdefRecord from the NdefMessage.
            NdefRecord ndefRecord = ndefMessage.getRecords()[0];

            // Gets the old tag ID of the NdefRecord.
            return new String(ndefRecord.getId());
        } catch (NullPointerException e) {
            Log.e(TAG, e.getMessage(), e);
            Toast.makeText(mActivity, "Write operation was interrupted", Toast.LENGTH_LONG).show();
            return null;
        }
    }

    /**
     * Writes the NdefMessage to the specified tag and returns the operation result. Causes RF
     * activity.
     *
     * @param tag   The Tag object representing the NFC tag.
     * @param tagId A String representing the ID that will
     *              be written to the specified tag.
     * @return A boolean indicating whether the write operation
     * succeeded.
     */
    private boolean writeTag(Tag tag, String tagId) {
        if (tagId == null) {
            throw new NullPointerException("Tag ID cannot be null.");
        }

        // Gets the NDEF formatted tag.
        Ndef ndef = Ndef.get(tag);

        try {
            // Enables I/O operations with NFC tag.
            ndef.connect();

            // Gets an NdefMessage that will be written on the tag.
            NdefMessage ndefMessage = createNdefMessage(tagId);

            // Writes the NdefMessage to the tag.
            ndef.writeNdefMessage(ndefMessage);

            // Sets STATUS_OK.
            mOnTagWriteListener.onTagWritten(OnTagWriteListener.STATUS_OK, tagId);

            // Write operation succeeded.
            return true;
        } catch (NullPointerException | IOException | FormatException e) {
            Log.e(TAG, "Write operation was interrupted. " + e.getMessage(), e);

            // Sets STATUS_ERROR.
            mOnTagWriteListener.onTagWritten(OnTagWriteListener.STATUS_ERROR, null);

            // Write operation failed.
            return false;
        } finally {
            if (ndef != null) {
                try {
                    ndef.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing tag. " + e.getMessage(), e);
                }
            }

            if (mOnTagWriteListener != null) {
                mOnTagWriteListener = null;
            }

            // Sets write mode to default value.
            sWriteMode = false;
        }
    }

    /**
     * Creates an NdefRecord with the specified ID, encapsulates it into an NdefMessage
     * and returns it.
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

        // Creates an NdefRecord that will be encapsulated into an NdefMessage.
        NdefRecord ndefRecord = new NdefRecord(tnf, mimeType, id, payload);

        // Creates an NdefMessage that contains the NdefRecord and
        // will be encapsulated into an intent.
        return new NdefMessage(ndefRecord);
    }

    /**
     * Handles an NFC tag discovery, reads and returns the underlying tag ID.
     *
     * @param intent The intent that is sent from the Android System at
     *               discovery time.
     * @return A String representing the discovered tag ID or null if
     * an error occurred.
     */
    public String handleNfcTagRead(Intent intent) {
        try {
            if (!intent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
                throw new TagNotRegisteredException("NFC tag isn't NDEF formatted.");
            }

            // Gets the extra from the intent containing the tag.
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            // Gets the ID of the discovered tag.
            String tagId = readTag(tag);

            // Gets the NFC tag from the list that corresponds to
            // the discovered tag ID.
            NfcTag nfcTag = MyTags.get(mActivity).getNfcTag(tagId);

            // If there isn't a corresponding NfcTag.
            if (nfcTag == null) {
                throw new TagNotRegisteredException("No corresponding NfcTag found for the given tag ID.");
            }

            // Returns the discovered tag ID.
            return tagId;

        } catch (TagNotRegisteredException e) {
            Log.e(TAG, "Error reading tag. " + e.getMessage(), e);
            Toast.makeText(mActivity, "NFC tag not registered", Toast.LENGTH_LONG).show();
            return null;
        } catch (NullPointerException e) {
            Log.e(TAG, e.getMessage(), e);
            Toast.makeText(mActivity, "Read operation was interrupted", Toast.LENGTH_LONG).show();
            return null;
        }
    }

    /**
     * Reads the NdefRecord of NdefMessage and returns the ID for the specified tag. Doesn't cause
     * any RF activity.
     *
     * @param tag The Tag object representing the NFC tag.
     * @return The tag ID or null if an error occurred.
     * @throws NullPointerException
     * @throws TagNotRegisteredException
     */
    private String readTag(Tag tag) throws NullPointerException, TagNotRegisteredException {
        // Gets the NDEF formatted tag.
        Ndef ndef = Ndef.get(tag);

        // Reads the cached NdefMessage from the NFC tag at discovery time.
        NdefMessage ndefMessage = ndef.getCachedNdefMessage();
        // Gets the first NdefRecord from the NdefMessage.
        NdefRecord ndefRecord = ndefMessage.getRecords()[0];

        // Gets the payload from the NdefRecord.
        String payload = new String(ndefRecord.getPayload());

        if (!payload.equals("tag_it")) {
            throw new TagNotRegisteredException("NFC tag payload doesn't match.");
        }

        // Returns the ID of NdefRecord.
        return new String(ndefRecord.getId());
    }

    /**
     * Registers an Android Beam callback and sends the data when
     * the devices are in proximity.
     */
    public void registerAndroidBeamShareFiles() {
        mNfcAdapter.setBeamPushUrisCallback(new NfcAdapter.CreateBeamUrisCallback() {
            @Override
            public Uri[] createBeamUris(NfcEvent event) {
                // Return the URIs array that Android Beam is going to send.
                return MyTags.get(mActivity).createFileUrisArray();
            }
        }, mActivity);
    }

    /**
     * Handles the Intent.ACTION_VIEW action and moves the files
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
     * Returns the parent file directory of the given URI
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
     * Returns the parent file directory of the given URI
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
        Cursor cursor = mActivity.getContentResolver().query(uri, projection, null, null, null);

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
     * Moves files to the private external storage of the device from the
     * specified file path.
     *
     * @param parentFilePath A String representing the parent file directory
     *                       where the files reside.
     */
    private void moveFilesToPrivateExternalStorage(String parentFilePath) {
        // Get tags.txt from Android Beam.
        File beamTagsJSONFile = new File(parentFilePath + File.separator + "tags.txt");
        // Existing file path.
        File existingTagsJSONFile = new File(mActivity.getExternalFilesDir(null) + File.separator + "tags.txt");
        // Overwrite the existing file with the beam file.
        renameFile(beamTagsJSONFile, existingTagsJSONFile);

        // Load the skeleton from the tags.txt file, received from Android Beam.
        MyTags.get(mActivity).loadTags();

        // Get pictures from Android Beam through the newly tags.txt file.
        // Get the size of the received tags.txt file.
        int size = MyTags.get(mActivity).getNfcTags().size();

        // For each NFC tag in the received tags.txt file.
        for (int i = 0; i < size; i++) {
            // Get the nfc tag.
            NfcTag nfcTag = MyTags.get(mActivity).getNfcTags().get(i);

            // Delete memory cache for the old image.
            PictureLoader.invalidateWithPicasso(mActivity, nfcTag.getPictureFilePath());

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
