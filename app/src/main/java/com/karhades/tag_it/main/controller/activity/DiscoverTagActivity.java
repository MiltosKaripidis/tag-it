package com.karhades.tag_it.main.controller.activity;

import android.support.v4.app.Fragment;

import com.karhades.tag_it.main.controller.fragment.DiscoverTagFragment;

/**
 * Controller Activity class that hosts a DiscoverTagFragment.
 */
public class DiscoverTagActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return DiscoverTagFragment.newInstance();
    }
}
