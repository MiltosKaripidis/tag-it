package com.example.karhades_pc.riddlehunting;

import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * Created by Karhades - PC on 4/15/2015.
 */
public class RiddleListActivity extends NfcActivity
{
    @Override
    protected Fragment createFragment()
    {
        return new RiddleListFragment();
    }
}
