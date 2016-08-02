package com.karhades.tag_it.main.controller.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.karhades.tag_it.R;
import com.karhades.tag_it.main.controller.fragment.SettingsFragment;

/**
 * Controller activity that hosts a SettingsFragment.
 */
public class SettingsActivity extends AppCompatActivity {

    public static Intent newIntent(Context context) {
        return new Intent(context, SettingsActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_fragment);

        setupToolbar();
        setupFragment();
    }

    private void setupToolbar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.settings_tool_bar);
        // Substitute the action bar for this toolbar.
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Settings");
        }
    }

    private void setupFragment() {
        getFragmentManager().beginTransaction()
                .add(R.id.fragment_container, SettingsFragment.newInstance())
                .commit();
    }
}
