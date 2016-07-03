package com.karhades.tag_it.main.controller.fragment;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.app.SharedElementCallback;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.karhades.tag_it.R;
import com.karhades.tag_it.main.controller.activity.TrackingTagPagerActivity;
import com.karhades.tag_it.main.model.MyTags;
import com.karhades.tag_it.main.model.NfcTag;
import com.karhades.tag_it.utils.FontCache;
import com.karhades.tag_it.utils.PictureLoader;
import com.karhades.tag_it.utils.TransitionHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Karhades - PC on 4/15/2015.
 */
public class TrackingGameFragment extends Fragment {

    /**
     * Widget references.
     */
    private RecyclerView recyclerView;
    private LinearLayout emptyLinearLayout;

    /**
     * Instance variables.
     */
    private ArrayList<NfcTag> nfcTags;
    private NfcTagAdapter adapter;

    /**
     * Transition variables.
     */
    private Bundle bundle;
    private boolean isReentering;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the list of NFC tags.
        nfcTags = MyTags.get(getActivity()).getNfcTags();

        if (TransitionHelper.isTransitionSupported() && TransitionHelper.isTransitionEnabled) {
            enableTransitions();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tracking_game, container, false);

        setupRecyclerView(view);
        setupEmptyView(view);

        return view;
    }

    private void setupRecyclerView(View view) {
        adapter = new NfcTagAdapter();
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
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

        recyclerView = (RecyclerView) view.findViewById(R.id.tracking_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
    }

    private void setupEmptyView(View view) {
        emptyLinearLayout = (LinearLayout) view.findViewById(R.id.tracking_empty_linear_layout);
        hideRecyclerViewIfEmpty();
    }

    private void hideRecyclerViewIfEmpty() {
        if (nfcTags.size() == 0) {
            recyclerView.setVisibility(View.INVISIBLE);
            emptyLinearLayout.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyLinearLayout.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Refresh the NfcTag list.
        adapter.notifyDataSetChanged();
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
        private CheckBox solvedCheckBox;

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

            // Custom Fonts.
            Typeface typefaceTitle = FontCache.get("fonts/capture_it.ttf", getActivity());
            Typeface typefaceBold = FontCache.get("fonts/amatic_bold.ttf", getActivity());

            imageView = (ImageView) view.findViewById(R.id.row_tracking_image_view);

            titleTextView = (TextView) view.findViewById(R.id.row_tracking_title_text_view);
            titleTextView.setTypeface(typefaceTitle);

            difficultyTextView = (TextView) view.findViewById(R.id.row_tracking_difficulty_text_view);
            difficultyTextView.setTypeface(typefaceBold);
            difficultyTextView.setTextColor(getResources().getColor(R.color.accent));

            solvedCheckBox = (CheckBox) view.findViewById(R.id.row_tracking_solved_check_box);
        }

        /**
         * Card resting elevation is 2dp and Card raised elevation is 8dp. Animate the changes between them.
         *
         * @param view The CardView to animate.
         */
        private void setupTouchListener(View view) {
            view.setOnTouchListener(new View.OnTouchListener() {
                Animator startAnimator = AnimatorInflater.loadAnimator(getActivity(), R.animator.start);
                Animator endAnimator = AnimatorInflater.loadAnimator(getActivity(), R.animator.end);

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
                    if (TransitionHelper.isTransitionSupported() && TransitionHelper.isTransitionEnabled) {
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
            Intent intent = new Intent(getActivity(), TrackingTagPagerActivity.class);
            intent.putExtra(TrackingTagFragment.EXTRA_TAG_ID, nfcTag.getTagId());
            intent.putExtra(TrackingTagPagerActivity.EXTRA_CURRENT_TAG_POSITION, getAdapterPosition());

            isReentering = false;

            Pair<View, String>[] pairs = createPairs(Pair.create(imageView, imageView.getTransitionName()));
            Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(getActivity(), pairs).toBundle();
            getActivity().startActivity(intent, bundle);
        }

        private void startTrackingTagPagerActivity() {
            Intent intent = new Intent(getActivity(), TrackingTagPagerActivity.class);
            intent.putExtra(TrackingTagFragment.EXTRA_TAG_ID, nfcTag.getTagId());
            intent.putExtra(TrackingTagPagerActivity.EXTRA_CURRENT_TAG_POSITION, getAdapterPosition());
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

            if (TransitionHelper.isTransitionSupported()) {
                imageView.setTransitionName("image" + nfcTag.getTagId());
                imageView.setTag("image" + nfcTag.getTagId());
            }

            PictureLoader.loadBitmapWithPicasso(getActivity(), nfcTag.getPictureFilePath(), imageView);

            titleTextView.setText(nfcTag.getTitle());
            difficultyTextView.setText(nfcTag.getDifficulty());
            solvedCheckBox.setChecked(nfcTag.isSolved());
        }
    }

    /**
     * Wraps the data set and creates views for individual items. It's the
     * intermediate that sits between the RecyclerView and the data set.
     */
    private class NfcTagAdapter extends RecyclerView.Adapter<NfcTagHolder> {

        @Override
        public NfcTagHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_tracking_game_fragment, viewGroup, false);

            return new NfcTagHolder(view);
        }

        @Override
        public void onBindViewHolder(NfcTagHolder nfcTagHolder, int position) {
            NfcTag nfcTag = nfcTags.get(position);
            nfcTagHolder.bindNfcTag(nfcTag);
        }

        @Override
        public int getItemCount() {
            return nfcTags.size();
        }
    }

    @SuppressWarnings("unchecked")
    @TargetApi(21)
    private Pair<View, String>[] createPairs(Pair... sharedViews) {
        ArrayList<Pair> pairs = new ArrayList<>();

        View navigationBar = getActivity().findViewById(android.R.id.navigationBarBackground);

        if (navigationBar != null) {
            pairs.add(Pair.create(navigationBar, navigationBar.getTransitionName()));
        }

        Collections.addAll(pairs, sharedViews);

        return pairs.toArray(new Pair[pairs.size()]);
    }

    @TargetApi(21)
    public void prepareReenterTransition(Intent data) {
        isReentering = true;
        bundle = new Bundle(data.getExtras());

        int oldTagPosition = data.getIntExtra(TrackingTagPagerActivity.EXTRA_OLD_TAG_POSITION, -1);
        int currentTagPosition = data.getIntExtra(TrackingTagPagerActivity.EXTRA_CURRENT_TAG_POSITION, -1);

        // If user swiped to another tag.
        if (oldTagPosition != currentTagPosition) {
            recyclerView.scrollToPosition(currentTagPosition);

            // Wait for RecyclerView to load it's layout.
            getActivity().postponeEnterTransition();
            recyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    recyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                    getActivity().startPostponedEnterTransition();
                    return true;
                }
            });
        }
    }

    @TargetApi(21)
    private void enableTransitions() {
        getActivity().setExitSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {

                // If TrackingTagPagerActivity returns to MainActivity.
                if (isReentering) {
                    int oldTagPosition = bundle.getInt(TrackingTagPagerActivity.EXTRA_OLD_TAG_POSITION);
                    int currentTagPosition = bundle.getInt(TrackingTagPagerActivity.EXTRA_CURRENT_TAG_POSITION);

                    // If currentPosition != oldPosition the user must have swiped to a different
                    // page in the ViewPager. We must update the shared element so that the
                    // correct one falls into place.
                    if (currentTagPosition != oldTagPosition) {

                        // Get the transition name of the current tag.
                        NfcTag nfcTag = MyTags.get(getActivity()).getNfcTags().get(currentTagPosition);
                        String currentTransitionName = "image" + nfcTag.getTagId();

                        // Get the ImageView from the RecyclerView.
                        View currentSharedImageView = recyclerView.findViewWithTag(currentTransitionName);
                        // If it exists.
                        if (currentSharedImageView != null) {
                            // Clear the previous (original) ImageView registrations.
                            names.clear();
                            sharedElements.clear();

                            // Add the current ImageView.
                            names.add(currentTransitionName);
                            sharedElements.put(currentTransitionName, currentSharedImageView);
                        }
                    }
                    // Delete the previous positions.
                    bundle = null;
                }
            }
        });
    }
}
