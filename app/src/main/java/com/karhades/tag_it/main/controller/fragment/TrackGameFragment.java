/*
 * Copyright (C) 2016 Karipidis Miltiadis
 */

package com.karhades.tag_it.main.controller.fragment;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.karhades.tag_it.R;
import com.karhades.tag_it.main.adapter.TrackGameAdapter;
import com.karhades.tag_it.main.controller.activity.TrackTagPagerActivity;
import com.karhades.tag_it.main.model.MyTags;
import com.karhades.tag_it.main.model.NfcTag;
import com.karhades.tag_it.utils.TransitionHelper;

import java.util.List;

/**
 * Controller Fragment class that binds the tracking tab with the data set.
 */
public class TrackGameFragment extends Fragment {

    /**
     * Widget references.
     */
    private RecyclerView mRecyclerView;
    private LinearLayout mEmptyLinearLayout;

    /**
     * Instance variables.
     */
    private List<NfcTag> mNfcTags;
    private TrackGameAdapter mTrackGameAdapter;
    private Callbacks mCallbacks;

    public interface Callbacks {
        void onFragmentResumed(TrackGameFragment fragment);
    }

    public static TrackGameFragment newInstance() {
        return new TrackGameFragment();
    }

    public void updateUi() {
        mNfcTags = MyTags.get(getActivity()).getNfcTags();
        mTrackGameAdapter.setNfcTags(mNfcTags);
        mTrackGameAdapter.notifyDataSetChanged();

        hideRecyclerViewIfEmpty();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallbacks = (Callbacks) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement TrackGameFragment.Callbacks interface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mCallbacks = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_track_game, container, false);

        setupAdapter();
        setupRecyclerView(view);
        setupEmptyView(view);

        return view;
    }

    private void setupAdapter() {
        mTrackGameAdapter = new TrackGameAdapter(getActivity(), mNfcTags);
        mTrackGameAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                hideRecyclerViewIfEmpty();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                hideRecyclerViewIfEmpty();
            }
        });
        mTrackGameAdapter.setOnItemClickListener(new TrackGameAdapter.OnItemClickListener() {
            @Override
            public void onTouch(View view, MotionEvent motionEvent) {
                setupItemTouchListener(view, motionEvent);
            }

            @Override
            public void onClick(View view, int position) {
                setupItemClickListener(view, position);
            }
        });
    }

    private void setupItemTouchListener(View view, MotionEvent motionEvent) {
        Animator startAnimator = AnimatorInflater.loadAnimator(getActivity(), R.animator.card_view_elevate);
        Animator endAnimator = AnimatorInflater.loadAnimator(getActivity(), R.animator.card_view_rest);

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startAnimator.setTarget(view);
                startAnimator.start();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                endAnimator.setTarget(view);
                endAnimator.start();
                break;
        }
    }

    private void setupItemClickListener(View view, int position) {
        if (TransitionHelper.isTransitionSupportedAndEnabled()) {
            startTrackingTagPagerActivityWithTransition(view, position);
        }
        // No transitions.
        else {
            startTrackingTagPagerActivity(position);
        }
    }

    @TargetApi(21)
    private void startTrackingTagPagerActivityWithTransition(View view, int position) {
        NfcTag nfcTag = mTrackGameAdapter.getNfcTag(position);

        Intent intent = TrackTagPagerActivity.newIntent(getActivity(), nfcTag.getTagId(), position);
        Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(getActivity(), view, view.getTransitionName()).toBundle();
        getActivity().startActivity(intent, bundle);
    }

    private void startTrackingTagPagerActivity(int position) {
        NfcTag nfcTag = mTrackGameAdapter.getNfcTag(position);

        Intent intent = TrackTagPagerActivity.newIntent(getActivity(), nfcTag.getTagId(), position);
        startActivity(intent);
    }

    private void setupRecyclerView(View view) {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.track_game_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mTrackGameAdapter);
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    private void setupEmptyView(View view) {
        mEmptyLinearLayout = (LinearLayout) view.findViewById(R.id.track_game_empty_linear_layout);
    }

    private void hideRecyclerViewIfEmpty() {
        boolean shouldHide = mNfcTags == null || mNfcTags.size() == 0;
        if (shouldHide) {
            mRecyclerView.setVisibility(View.INVISIBLE);
            mEmptyLinearLayout.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mEmptyLinearLayout.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        mCallbacks.onFragmentResumed(this);
    }
}
