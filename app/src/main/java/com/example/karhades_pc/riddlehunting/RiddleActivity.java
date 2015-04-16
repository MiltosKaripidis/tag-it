package com.example.karhades_pc.riddlehunting;


import android.support.v4.app.Fragment;

/**
 * Created by Karhades - PC on 4/14/2015.
 */
public class RiddleActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        //Gets the Tag ID either from the onListClick() of RiddleListFragment
        //or the startActivity() of RiddleListActivity.
        String tagId = getIntent().getStringExtra(RiddleFragment.EXTRA_TAG_ID);

        //Get a boolean value whether the intent was from handleIntent().
        boolean nfcDiscovered = getIntent().getBooleanExtra(RiddleFragment.EXTRA_NFC_TAG_DISCOVERED, false);

        return RiddleFragment.newInstance(tagId, nfcDiscovered);
    }
}
