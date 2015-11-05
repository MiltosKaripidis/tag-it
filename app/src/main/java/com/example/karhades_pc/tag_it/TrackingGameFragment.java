package com.example.karhades_pc.tag_it;

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
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.karhades_pc.utils.FontCache;
import com.example.karhades_pc.utils.PictureLoader;
import com.example.karhades_pc.utils.Utils;

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

        if (Utils.itSupportsTransitions()) {
            enableTransitions();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tracking_game, container, false);

        emptyLinearLayout = (LinearLayout) view.findViewById(R.id.tracking_empty_linear_layout);

        setupRecyclerView(view);

        // Listen for list changes to hide or show widgets.
        MyTags.get(getActivity()).setOnListChangeListener(new MyTags.onListChangeListener() {
            @Override
            public void onListChanged() {
                hideRecyclerViewIfEmpty();
            }
        });
        hideRecyclerViewIfEmpty();

        return view;
    }

    private void setupRecyclerView(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.tracking_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        recyclerView.setAdapter(new RiddleAdapter());
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

    private class RiddleHolder extends RecyclerView.ViewHolder {
        private NfcTag nfcTag;
        private ImageView imageView;
        private TextView titleTextView;
        private TextView difficultyTextView;
        private CheckBox solvedCheckBox;

        public RiddleHolder(View view) {
            super(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), TrackingTagPagerActivity.class);
                    intent.putExtra(TrackingTagFragment.EXTRA_TAG_ID, nfcTag.getTagId());
                    intent.putExtra(TrackingTagPagerActivity.EXTRA_CURRENT_TAG_POSITION, getAdapterPosition());

                    if (Utils.itSupportsTransitions()) {
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

            imageView = (ImageView) view.findViewById(R.id.row_tracking_image_view);

            titleTextView = (TextView) view.findViewById(R.id.row_tracking_title_text_view);
            difficultyTextView = (TextView) view.findViewById(R.id.row_tracking_difficulty_text_view);
            solvedCheckBox = (CheckBox) view.findViewById(R.id.row_tracking_solved_check_box);

            // Custom Fonts.
            Typeface typefaceTitle = FontCache.get("fonts/capture_it.ttf", getActivity());
            Typeface typefaceBold = FontCache.get("fonts/amatic_bold.ttf", getActivity());

            titleTextView.setTypeface(typefaceTitle);
            difficultyTextView.setTypeface(typefaceBold);
            difficultyTextView.setTextColor(getResources().getColor(R.color.accent));
        }

        public void bindRiddle(NfcTag nfcTag) {
            this.nfcTag = nfcTag;

            if (Utils.itSupportsTransitions()) {
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
