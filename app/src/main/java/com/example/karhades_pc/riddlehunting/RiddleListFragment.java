package com.example.karhades_pc.riddlehunting;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.example.karhades_pc.floating_action_button.ActionButton;
import com.example.karhades_pc.utils.FontCache;

import java.util.ArrayList;

/**
 * Created by Karhades - PC on 4/15/2015.
 */
public class RiddleListFragment extends Fragment {

    private ArrayList<Riddle> riddles;

    private ActionButton actionButton;
    private RecyclerView recyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Tell the FragmentManager that this fragment should receive
        // a call to onCreateOptionsMenu.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_riddle_list, container, false);

        riddles = MyRiddles.get(getActivity()).getRiddles();

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new RiddleAdapter());
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // If scrolling down (dy > 0). The faster the scrolling
                // the bigger the dy.
                if (dy > 0)
                    actionButton.hide();
                else if (dy < -15)
                    actionButton.show();
            }
        });

        setupFloatingActionButton(view);

        return view;
    }

    private void setupFloatingActionButton(View view) {
        actionButton = (ActionButton) view.findViewById(R.id.floating_action_button);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Floating action button pressed!", Toast.LENGTH_SHORT).show();
                actionButton.setHideAnimation(ActionButton.Animations.SCALE_DOWN);
                actionButton.hide();
                actionButton.setHideAnimation(ActionButton.Animations.ROLL_TO_DOWN);
            }
        });
        actionButton.setShowAnimation(ActionButton.Animations.ROLL_FROM_DOWN);
        actionButton.setHideAnimation(ActionButton.Animations.ROLL_TO_DOWN);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Refresh the Riddle list.
        recyclerView.getAdapter().notifyDataSetChanged();

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.fragment_riddle_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_new_riddle:
                if (actionButton.isHidden())
                    actionButton.show();
                else
                    actionButton.hide();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class RiddleHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Riddle riddle;
        private TextView titleTextView;
        private TextView difficultyLabelTextView;
        private TextView difficultyTextView;
        private CheckBox solvedCheckBox;

        public RiddleHolder(View view) {
            super(view);
            view.setOnClickListener(this);

            titleTextView = (TextView) view.findViewById(R.id.list_item_title_text_view);
            difficultyLabelTextView = (TextView) view.findViewById(R.id.list_item_difficulty_label_text_view);
            difficultyTextView = (TextView) view.findViewById(R.id.list_item_difficulty_text_view);
            solvedCheckBox = (CheckBox) view.findViewById(R.id.list_item_solved_check_box);
        }

        public void bindRiddle(Riddle riddle) {
            // Custom Fonts.
            Typeface typefaceBold = FontCache.get("fonts/Capture_it.ttf", getActivity());
            Typeface typefaceNormal = FontCache.get("fonts/amatic_bold.ttf", getActivity());

            this.riddle = riddle;
            titleTextView.setText(riddle.getTitle());
            titleTextView.setTypeface(typefaceBold);

            difficultyLabelTextView.setTypeface(typefaceNormal);

            difficultyTextView.setText(riddle.getDifficulty());
            difficultyTextView.setTypeface(typefaceNormal);
            difficultyTextView.setTextColor(getResources().getColor(R.color.colorAccent));

            solvedCheckBox.setChecked(riddle.isSolved());
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getActivity(), RiddlePagerActivity.class);
            intent.putExtra(RiddleFragment.EXTRA_TAG_ID, riddle.getTagId());
            startActivity(intent);
        }
    }

    private class RiddleAdapter extends RecyclerView.Adapter<RiddleHolder> {
        @Override
        public RiddleHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_riddle, viewGroup, false);

            return new RiddleHolder(view);
        }

        @Override
        public void onBindViewHolder(RiddleHolder riddleHolder, int i) {
            Riddle riddle = riddles.get(i);
            riddleHolder.bindRiddle(riddle);
        }

        @Override
        public int getItemCount() {
            return riddles.size();
        }
    }
}
