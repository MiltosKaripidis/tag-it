package com.example.karhades_pc.tag_it;

import android.support.v4.app.Fragment;

/**
 * Created by Karhades on 11-Sep-15.
 */
public class CreateTagActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new CreateTagFragment();
    }
}
