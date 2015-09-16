package com.example.karhades_pc.tag_it;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.karhades_pc.floating_action_button.ActionButton;
import com.example.karhades_pc.utils.FontCache;

import java.util.ArrayList;

/**
 * Created by Karhades - PC on 4/15/2015.
 */
public class CreateGameFragment extends Fragment {

    private ArrayList<NfcTag> nfcTags;

    private ActionButton actionButton;
    private RecyclerView recyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        nfcTags = MyTags.get(getActivity()).getNfcTags();

        // Tell the FragmentManager that this fragment should receive
        // a call to onCreateOptionsMenu.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.fragment_create_game, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_track:
                Toast.makeText(getActivity(), "Track menu button was pressed!", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_game, container, false);

        setupRecyclerView(view);
        setupFloatingActionButton(view);

        return view;
    }

    private void setupRecyclerView(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        recyclerView.setAdapter(new RiddleAdapter());
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // If scrolling down (dy > 0). The faster the scrolling
                // the bigger the dy.
                if (dy > 0) {
                    actionButton.hide();
                } else if (dy < -15) {
                    actionButton.show();
                }
            }
        });
    }

    private void setupFloatingActionButton(View view) {
        actionButton = (ActionButton) view.findViewById(R.id.floating_action_button);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                actionButton.setHideAnimation(ActionButton.Animations.SCALE_DOWN);
//                actionButton.hide();
//                actionButton.setHideAnimation(ActionButton.Animations.ROLL_TO_DOWN);

                Intent intent = new Intent(getActivity(), CreateTagActivity.class);
                startActivity(intent);
            }
        });
        actionButton.setShowAnimation(ActionButton.Animations.ROLL_FROM_DOWN);
        actionButton.setHideAnimation(ActionButton.Animations.ROLL_TO_DOWN);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Refresh the NfcTag list.
        recyclerView.getAdapter().notifyDataSetChanged();

        startupAnimation();
    }

    private void startupAnimation()
    {
        // Floating Action Button animation on show after a period of time.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (actionButton.isHidden()) {
                    actionButton.setShowAnimation(ActionButton.Animations.SCALE_UP);
                    actionButton.show();
                    actionButton.setShowAnimation(ActionButton.Animations.ROLL_FROM_DOWN);
                }
            }
        }, 750);
    }

    private class RiddleHolder extends RecyclerView.ViewHolder {
        private NfcTag nfcTag;
        private ImageView imageView;
        private TextView titleTextView;
        private TextView difficultyTextView;

        public RiddleHolder(View view) {
            super(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), CreateTagPagerActivity.class);
                    intent.putExtra(CreateTagFragment.EXTRA_TAG_ID, nfcTag.getTagId());
                    startActivity(intent);
                }
            });

            imageView = (ImageView) view.findViewById(R.id.image_view);
            titleTextView = (TextView) view.findViewById(R.id.list_item_title_text_view);
            difficultyTextView = (TextView) view.findViewById(R.id.list_item_difficulty_text_view);
        }

        public void bindRiddle(NfcTag nfcTag) {
            // Custom Fonts.
            Typeface typefaceBold = FontCache.get("fonts/Capture_it.ttf", getActivity());
            Typeface typefaceNormal = FontCache.get("fonts/amatic_bold.ttf", getActivity());

            this.nfcTag = nfcTag;


            titleTextView.setText(nfcTag.getTitle());
            titleTextView.setTypeface(typefaceBold);

            difficultyTextView.setText(nfcTag.getDifficulty());
            difficultyTextView.setTypeface(typefaceNormal);
            difficultyTextView.setTextColor(getResources().getColor(R.color.accent));
        }
    }

    private class RiddleAdapter extends RecyclerView.Adapter<RiddleHolder> {
        @Override
        public RiddleHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view;
            if(Build.VERSION.SDK_INT < 21)
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_pre_lollipop, viewGroup, false);
            else
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_create_game_fragment, viewGroup, false);

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
