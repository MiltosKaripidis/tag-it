package com.example.karhades_pc.tag_it;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.karhades_pc.floating_action_button.ActionButton;
import com.example.karhades_pc.nfc.NfcHandler;
import com.example.karhades_pc.utils.FontCache;

import java.util.ArrayList;

/**
 * Created by Karhades - PC on 4/15/2015.
 */
public class TrackingGameFragment extends Fragment {

    private ArrayList<Tag> tags;

    private ActionButton actionButton;
    private RecyclerView recyclerView;
    private Dialog alertDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tags = MyTags.get(getActivity()).getTags();

        // Tell the FragmentManager that this fragment should receive
        // a call to onCreateOptionsMenu.
        setHasOptionsMenu(true);
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
                Toast.makeText(getActivity(), "Add menu button was pressed!", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tracking_game, container, false);

        setupRecyclerView(view);
        setupFloatingActionButton(view);

        return view;
    }

    private void setupRecyclerView(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
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
    }

    private void setupFloatingActionButton(View view) {
        actionButton = (ActionButton) view.findViewById(R.id.floating_action_button);
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionButton.setHideAnimation(ActionButton.Animations.SCALE_DOWN);
                actionButton.hide();
                actionButton.setHideAnimation(ActionButton.Animations.ROLL_TO_DOWN);

                NfcHandler.get().enableTagWriteMode();
                NfcHandler.get().setOnTagWriteListener(new NfcHandler.OnTagWriteListener() {
                    @Override
                    public void onTagWritten(int status) {
                        Log.d("TrackingGameFragment", "onTagWritten!");
                        alertDialog.dismiss();

                        if (status == NfcHandler.OnTagWriteListener.STATUS_OK) {
                            Toast.makeText(getActivity(), "Tag was successfully written!", Toast.LENGTH_SHORT).show();
                        } else if (status == NfcHandler.OnTagWriteListener.STATUS_ERROR) {
                            Toast.makeText(getActivity(), "Could not write to tag!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                alertDialog = onCreateDialog();
                alertDialog.show();
            }
        });
        actionButton.setShowAnimation(ActionButton.Animations.ROLL_FROM_DOWN);
        actionButton.setHideAnimation(ActionButton.Animations.ROLL_TO_DOWN);
    }

    private Dialog onCreateDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle("Nfc Write Mode")
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        NfcHandler.get().disableTagWriteMode();
                    }
                })
                .setMessage("Touch the tag to write the information inserted.")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        NfcHandler.get().disableTagWriteMode();
                    }
                });

        return alertDialog.create();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Refresh the Tag list.
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

    private class RiddleHolder extends RecyclerView.ViewHolder {
        private Tag tag;
        private ImageView imageView;
        private TextView titleTextView;
        private TextView difficultyLabelTextView;
        private TextView difficultyTextView;
        private CheckBox solvedCheckBox;

        public RiddleHolder(View view) {
            super(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), TagPagerActivity.class);
                    intent.putExtra(TagFragment.EXTRA_TAG_ID, tag.getTagId());
                    startActivity(intent);
                }
            });

            imageView = (ImageView) view.findViewById(R.id.image_view);
            titleTextView = (TextView) view.findViewById(R.id.list_item_title_text_view);
            difficultyLabelTextView = (TextView) view.findViewById(R.id.list_item_difficulty_label_text_view);
            difficultyTextView = (TextView) view.findViewById(R.id.list_item_difficulty_text_view);
            solvedCheckBox = (CheckBox) view.findViewById(R.id.list_item_solved_check_box);
        }

        public void bindRiddle(Tag tag) {
            // Custom Fonts.
            Typeface typefaceBold = FontCache.get("fonts/Capture_it.ttf", getActivity());
            Typeface typefaceNormal = FontCache.get("fonts/amatic_bold.ttf", getActivity());

            this.tag = tag;


            titleTextView.setText(tag.getTitle());
            titleTextView.setTypeface(typefaceBold);

            //difficultyLabelTextView.setTypeface(typefaceNormal);

            difficultyTextView.setText(tag.getDifficulty());
            difficultyTextView.setTypeface(typefaceNormal);
            difficultyTextView.setTextColor(getResources().getColor(R.color.colorAccent));

            solvedCheckBox.setChecked(tag.isSolved());
        }
    }

    private class RiddleAdapter extends RecyclerView.Adapter<RiddleHolder> {
        @Override
        public RiddleHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_tag2, viewGroup, false);

            return new RiddleHolder(view);
        }

        @Override
        public void onBindViewHolder(RiddleHolder riddleHolder, int i) {
            Tag tag = tags.get(i);
            riddleHolder.bindRiddle(tag);
        }

        @Override
        public int getItemCount() {
            return tags.size();
        }
    }
}
