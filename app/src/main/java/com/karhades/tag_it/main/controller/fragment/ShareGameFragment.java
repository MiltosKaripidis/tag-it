/*
 * Copyright (C) 2016 Karipidis Miltiadis
 */

package com.karhades.tag_it.main.controller.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.karhades.tag_it.R;


/**
 * Controller Fragment class that binds the share tab with the data set.
 */
public class ShareGameFragment extends Fragment {

    public static ShareGameFragment newInstance() {
        return new ShareGameFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_share_game, container, false);
    }
}
