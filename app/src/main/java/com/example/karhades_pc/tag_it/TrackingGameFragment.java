package com.example.karhades_pc.tag_it;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.karhades_pc.picture_utils.PictureLoader;
import com.example.karhades_pc.utils.FontCache;

import java.util.ArrayList;

/**
 * Created by Karhades - PC on 4/15/2015.
 */
public class TrackingGameFragment extends Fragment {

    private ArrayList<NfcTag> nfcTags;
    private RecyclerView recyclerView;
    private LinearLayout emptyLinearLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        nfcTags = MyTags.get(getActivity()).getNfcTags();
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
                    startActivity(intent);
                }
            });

            imageView = (ImageView) view.findViewById(R.id.row_tracking_image_view);

            titleTextView = (TextView) view.findViewById(R.id.row_tracking_title_text_view);
            difficultyTextView = (TextView) view.findViewById(R.id.row_tracking_difficulty_text_view);
            solvedCheckBox = (CheckBox) view.findViewById(R.id.row_tracking_solved_check_box);

            // Custom Fonts.
            Typeface typefaceBold = FontCache.get("fonts/Capture_it.ttf", getActivity());
            Typeface typefaceNormal = FontCache.get("fonts/amatic_bold.ttf", getActivity());

            titleTextView.setTypeface(typefaceBold);
            difficultyTextView.setTypeface(typefaceNormal);
            difficultyTextView.setTextColor(getResources().getColor(R.color.accent));
        }

        public void bindRiddle(NfcTag nfcTag) {
            this.nfcTag = nfcTag;

            PictureLoader.loadBitmapWithPicasso(getActivity(), nfcTag.getPictureFilePath(), imageView);

            titleTextView.setText(nfcTag.getTitle());
            difficultyTextView.setText(nfcTag.getDifficulty());
            solvedCheckBox.setChecked(nfcTag.isSolved());
        }
    }

    private class RiddleAdapter extends RecyclerView.Adapter<RiddleHolder> {
        @Override
        public RiddleHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view;
            if (Build.VERSION.SDK_INT < 21)
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_tracking_pre_lollipop, viewGroup, false);
            else
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_tracking_game_fragment, viewGroup, false);

            return new RiddleHolder(view);
        }

        @Override
        public void onBindViewHolder(RiddleHolder riddleHolder, int i) {
            NfcTag nfcTag = nfcTags.get(i);
            riddleHolder.bindRiddle(nfcTag);
        }

        @Override
        public int getItemCount() {
            return nfcTags.size();
        }
    }
}
