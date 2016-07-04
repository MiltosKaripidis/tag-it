package com.karhades.tag_it.main.controller.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.karhades.tag_it.R;


/**
 * Created by Karhades on 20-Sep-15.
 */
public class ShareGameFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_share_game, container, false);
    }
}
