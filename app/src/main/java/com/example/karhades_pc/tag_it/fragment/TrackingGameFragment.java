package com.example.karhades_pc.tag_it.fragment;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.app.SharedElementCallback;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
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

import com.example.karhades_pc.tag_it.R;
import com.example.karhades_pc.tag_it.activity.TrackingTagPagerActivity;
import com.example.karhades_pc.tag_it.model.MyTags;
import com.example.karhades_pc.tag_it.model.NfcTag;
import com.example.karhades_pc.utils.FontCache;
import com.example.karhades_pc.utils.PictureLoader;
import com.example.karhades_pc.utils.TransitionHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Karhades - PC on 4/15/2015.
 */
public class TrackingGameFragment extends Fragment {

    private ArrayList<NfcTag> nfcTags;
    private RecyclerView recyclerView;
    private LinearLayout emptyLinearLayout;

    private Bundle bundle;
    // Used for transitions.
    private boolean isReentering;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        nfcTags = MyTags.get(getActivity()).getNfcTags();

        if (TransitionHelper.itSupportsTransitions()) {
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
        recyclerView = (RecyclerView) view.findViewById(R.id.tracking_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        RiddleAdapter adapter = new RiddleAdapter();
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
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    @SuppressWarnings("deprecation")
    private class RiddleHolder extends RecyclerView.ViewHolder {
        private NfcTag nfcTag;
        private ImageView imageView;
        private TextView titleTextView;
        private TextView difficultyTextView;
        private CheckBox solvedCheckBox;

        public RiddleHolder(View view) {
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

        @SuppressWarnings("unchecked")
        private void setupClickListener(View view) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), TrackingTagPagerActivity.class);
                    intent.putExtra(TrackingTagFragment.EXTRA_TAG_ID, nfcTag.getTagId());
                    intent.putExtra(TrackingTagPagerActivity.EXTRA_CURRENT_TAG_POSITION, getAdapterPosition());

                    if (TransitionHelper.itSupportsTransitions()) {
                        isReentering = false;

                        Pair<View, String>[] pairs = createPairs(Pair.create(imageView, imageView.getTransitionName()));
                        Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(getActivity(), pairs).toBundle();
                        getActivity().startActivity(intent, bundle);
                    }
                    // No transitions.
                    else {
                        startActivity(intent);
                    }
                }
            });
        }

        public void bindRiddle(NfcTag nfcTag) {
            this.nfcTag = nfcTag;

            if (TransitionHelper.itSupportsTransitions()) {
                imageView.setTransitionName("image" + nfcTag.getTagId());
                imageView.setTag("image" + nfcTag.getTagId());
            }

            PictureLoader.loadBitmapWithPicasso(getActivity(), nfcTag.getPictureFilePath(), imageView, null);

            titleTextView.setText(nfcTag.getTitle());
            difficultyTextView.setText(nfcTag.getDifficulty());
            solvedCheckBox.setChecked(nfcTag.isSolved());
        }
    }

    private class RiddleAdapter extends RecyclerView.Adapter<RiddleHolder> {
        @Override
        public RiddleHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_tracking_game_fragment, viewGroup, false);

            return new RiddleHolder(view);
        }

        @Override
        public void onBindViewHolder(RiddleHolder riddleHolder, int position) {
            NfcTag nfcTag = nfcTags.get(position);
            riddleHolder.bindRiddle(nfcTag);
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
