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
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.karhades.tag_it.R;
import com.karhades.tag_it.main.controller.activity.TrackTagPagerActivity;
import com.karhades.tag_it.main.model.MyTags;
import com.karhades.tag_it.main.model.NfcTag;
import com.karhades.tag_it.utils.PictureLoader;
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
    private NfcTagAdapter mNfcTagAdapter;
    private Callbacks mCallbacks;

    public interface Callbacks {
        void onFragmentResumed(TrackGameFragment fragment);
    }

    public static TrackGameFragment newInstance() {
        return new TrackGameFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void updateUi() {
        // Updates the UI.
        mNfcTags = MyTags.get(getActivity()).getNfcTags();

        // Refreshes the NfcTag list.
        mNfcTagAdapter.notifyDataSetChanged();

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

        setupRecyclerView(view);
        setupEmptyView(view);

        return view;
    }

    private void setupRecyclerView(View view) {
        mNfcTagAdapter = new NfcTagAdapter();
        mNfcTagAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);

                hideRecyclerViewIfEmpty();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);

                hideRecyclerViewIfEmpty();
            }
        });

        mRecyclerView = (RecyclerView) view.findViewById(R.id.track_game_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mNfcTagAdapter);
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

    /**
     * Describes the view and it's child views that
     * will bind the data for an adapter item.
     */
    @SuppressWarnings("deprecation")
    private class NfcTagHolder extends RecyclerView.ViewHolder {

        /**
         * Instance variable.
         */
        private NfcTag nfcTag;

        /**
         * Widget references.
         */
        private ImageView imageView;
        private TextView titleTextView;
        private TextView difficultyTextView;
        private CheckBox discoveredCheckBox;

        /**
         * Constructor that registers any listeners and make calls
         * to findViewById() for each adapter item.
         *
         * @param view The view describing an adapter item (CardView).
         */
        public NfcTagHolder(View view) {
            super(view);

            setupTouchListener(view);
            setupClickListener(view);

            imageView = (ImageView) view.findViewById(R.id.row_track_image_view);

            titleTextView = (TextView) view.findViewById(R.id.row_track_title_text_view);

            difficultyTextView = (TextView) view.findViewById(R.id.row_track_difficulty_text_view);
            difficultyTextView.setTextColor(getResources().getColor(R.color.accent));

            discoveredCheckBox = (CheckBox) view.findViewById(R.id.row_track_discovered_check_box);
        }

        /**
         * Card resting elevation is 2dp and Card raised elevation is 8dp. Animate the changes between them.
         *
         * @param view The CardView to animate.
         */
        private void setupTouchListener(View view) {
            view.setOnTouchListener(new View.OnTouchListener() {
                Animator startAnimator = AnimatorInflater.loadAnimator(getActivity(), R.animator.card_view_elevate);
                Animator endAnimator = AnimatorInflater.loadAnimator(getActivity(), R.animator.card_view_rest);

                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            startAnimator.setTarget(v);
                            startAnimator.start();
                            break;
                        case MotionEvent.ACTION_CANCEL:
                        case MotionEvent.ACTION_UP:
                            endAnimator.setTarget(v);
                            endAnimator.start();
                            break;
                    }
                    return false;
                }
            });
        }

        private void setupClickListener(View view) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (TransitionHelper.isTransitionSupportedAndEnabled()) {
                        startTrackingTagPagerActivityWithTransition();
                    }
                    // No transitions.
                    else {
                        startTrackingTagPagerActivity();
                    }
                }
            });
        }

        @TargetApi(21)
        @SuppressWarnings("unchecked")
        private void startTrackingTagPagerActivityWithTransition() {
            Intent intent = TrackTagPagerActivity.newIntent(getActivity(), nfcTag.getTagId(), getAdapterPosition());
            Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(getActivity(), imageView, imageView.getTransitionName()).toBundle();
            getActivity().startActivity(intent, bundle);
        }

        private void startTrackingTagPagerActivity() {
            Intent intent = TrackTagPagerActivity.newIntent(getActivity(), nfcTag.getTagId(), getAdapterPosition());
            startActivity(intent);
        }

        /**
         * Helper method for binding data on the adapter's
         * onBindViewHolder() method.
         *
         * @param nfcTag The NfcTag object to bind data to views.
         */
        public void bindNfcTag(NfcTag nfcTag) {
            this.nfcTag = nfcTag;

            if (TransitionHelper.isTransitionSupportedAndEnabled()) {
                imageView.setTransitionName("image" + nfcTag.getTagId());
                imageView.setTag("image" + nfcTag.getTagId());
            }

            PictureLoader.loadBitmapWithPicasso(getActivity(), nfcTag.getPictureFilePath(), imageView);
            titleTextView.setText(nfcTag.getTitle());
            difficultyTextView.setText(nfcTag.getDifficulty());
            discoveredCheckBox.setChecked(nfcTag.isDiscovered());
            if (discoveredCheckBox.isChecked()) {
                discoveredCheckBox.setVisibility(View.VISIBLE);
            } else {
                discoveredCheckBox.setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * Wraps the data set and creates views for individual items. It's the
     * intermediate that sits between the RecyclerView and the data set.
     */
    private class NfcTagAdapter extends RecyclerView.Adapter<NfcTagHolder> {

        @Override
        public NfcTagHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_track_game_fragment, viewGroup, false);

            return new NfcTagHolder(view);
        }

        @Override
        public void onBindViewHolder(NfcTagHolder nfcTagHolder, int position) {
            NfcTag nfcTag = mNfcTags.get(position);
            nfcTagHolder.bindNfcTag(nfcTag);
        }

        @Override
        public int getItemCount() {
            return (mNfcTags == null) ? 0 : mNfcTags.size();
        }
    }
}
