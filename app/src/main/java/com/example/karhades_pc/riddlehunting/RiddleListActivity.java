package com.example.karhades_pc.riddlehunting;

import android.support.v4.app.Fragment;

/**
 * Created by Karhades - PC on 4/15/2015.
 */
public class RiddleListActivity extends SingleFragmentActivity
{
    @Override
    protected Fragment createFragment()
    {
        return new RiddleListFragment();
    }
}
