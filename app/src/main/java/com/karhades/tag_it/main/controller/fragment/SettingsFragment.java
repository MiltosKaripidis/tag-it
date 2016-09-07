package com.karhades.tag_it.main.controller.fragment;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.karhades.tag_it.R;
import com.karhades.tag_it.main.model.MyTags;

/**
 * Controller fragment that manages the application settings.
 */
public class SettingsFragment extends PreferenceFragment {

    /**
     * Shared preference key.
     */
    private static final String PREFS_TRANSITION = "transition";

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onPause() {
        super.onPause();

        MyTags.setTransitionDisabled(getStoredTransition(getActivity()));
    }

    public static boolean getStoredTransition(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREFS_TRANSITION, false);
    }
}
