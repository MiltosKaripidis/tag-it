package com.example.karhades_pc.tag_it.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;

import com.example.karhades_pc.nfc.NfcHandler;
import com.example.karhades_pc.tag_it.fragment.CreateTagFragment;
import com.example.karhades_pc.tag_it.R;

/**
 * Created by Karhades on 11-Sep-15.
 */
public class CreateTagActivity extends SingleFragmentActivity {

    private NfcHandler nfcHandler;

    @Override
    protected Fragment createFragment() {
        return CreateTagFragment.newInstance(null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make content appear behind status bar.
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        setupNfcHandler();
    }

    private void setupNfcHandler() {
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

        FragmentManager fragmentManager = getSupportFragmentManager();
        CreateTagFragment currentFragment = (CreateTagFragment) fragmentManager.findFragmentById(R.id.fragmentContainer);

        boolean isWritten = nfcHandler.enableNfcWriteTag(intent);

        if (!isWritten) {
            currentFragment.makeSnackbar();
        }
    }
}
