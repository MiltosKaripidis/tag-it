package com.example.karhades_pc.tag_it;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.example.karhades_pc.nfc.NfcHandler;

/**
 * Created by Karhades on 11-Sep-15.
 */
public class CreateTagActivity extends SingleFragmentActivity {
    NfcHandler nfcHandler;

    @Override
    protected Fragment createFragment() {
        return CreateTagFragment.newInstance(null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupNfcHandler();
    }

    private void setupNfcHandler()
    {
        nfcHandler = new NfcHandler();
        nfcHandler.setupNfcHandler(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        nfcHandler.enableForegroundDispatch();
    }

    @Override
    protected void onPause() {
        super.onPause();

        nfcHandler.disableForegroundDispatch();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        nfcHandler.enableNfcWriteTag(intent);
    }
}
