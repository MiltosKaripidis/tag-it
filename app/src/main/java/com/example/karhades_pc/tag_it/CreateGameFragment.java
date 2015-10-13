package com.example.karhades_pc.tag_it;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.karhades_pc.floating_action_button.ActionButton;
import com.example.karhades_pc.picture_utils.PictureUtils;
import com.example.karhades_pc.utils.FontCache;

import java.util.ArrayList;

/**
 * Created by Karhades - PC on 4/15/2015.
 */
public class CreateGameFragment extends Fragment {

    /**
     * Request codes, used for startActivityForResult().
     */
    private static final int REQUEST_NEW = 0;
    private static final int REQUEST_EDIT = 1;

    /**
     * Extra key, used for the position value returned from startActivityForResult().
     */
    public static final String EXTRA_POSITION = "com.example.karhades_pc.tag_it.position";

    private ArrayList<NfcTag> nfcTags;

    private RecyclerView recyclerView;
    private ActionButton actionButton;
    private LinearLayout emptyLinearLayout;

    private static OnContextFragmentListener onContextFragmentListener;

    public static void setOnContextFragmentListener(OnContextFragmentListener newOnContextFragmentListener) {
        onContextFragmentListener = newOnContextFragmentListener;
    }

    /**
     * TODO
     */
    public interface OnContextFragmentListener {
        void onItemLongClicked();

        void onItemClicked(int tagsSelected);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        nfcTags = MyTags.get(getActivity()).getNfcTags();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_NEW) {
            if (resultCode == Activity.RESULT_OK) {
                recyclerView.getAdapter().notifyItemInserted(nfcTags.size());
            }
        } else if (requestCode == REQUEST_EDIT) {
            if (resultCode == Activity.RESULT_OK) {
                recyclerView.getAdapter().notifyItemChanged(data.getIntExtra(EXTRA_POSITION, -1));
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_game, container, false);

        setupRecyclerView(view);
        setupFloatingActionButton(view);
        setupEmptyView(view);

        return view;
    }

    private void setupRecyclerView(final View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.create_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(new NfcTagAdapter());
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
                // Start CreateTagActivity.
                Intent intent = new Intent(getActivity(), CreateTagActivity.class);
                startActivityForResult(intent, 0);
            }
        });
        actionButton.setShowAnimation(ActionButton.Animations.ROLL_FROM_DOWN);
        actionButton.setHideAnimation(ActionButton.Animations.ROLL_TO_DOWN);
    }

    private void setupEmptyView(View view) {
        emptyLinearLayout = (LinearLayout) view.findViewById(R.id.create_empty_linear_layout);

        // Listen for list changes to hide or show widgets.
        MyTags.get(getActivity()).setOnListChangeListener(new MyTags.onListChangeListener() {
            @Override
            public void onListChanged() {
                hideRecyclerViewIfEmpty();
            }
        });
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

        startupAnimation();
    }

    private void startupAnimation() {
        // Floating Action Button animation onShow after a period of time.
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

    /**
     * Wraps the data set and creates views for individual items.
     */
    private class NfcTagAdapter extends RecyclerView.Adapter<NfcTagHolder> {

        private SparseBooleanArray selectedItems;
        private boolean isSelectionMode = false;

        public NfcTagAdapter() {
            selectedItems = new SparseBooleanArray();
        }

        // Create new views (invoked by the layout manager).
        @Override
        public NfcTagHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            //Log.d("CreateGameFragment", "onCreateViewHolder called");
            View view;
            if (Build.VERSION.SDK_INT < 21)
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_create_pre_lollipop, viewGroup, false);
            else
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_create_game_fragment, viewGroup, false);

            return new NfcTagHolder(view);
        }

        // Replace the contents of a view (invoked by the layout manager).
        @Override
        public void onBindViewHolder(NfcTagHolder nfcTagHolder, int position) {
            NfcTag nfcTag = nfcTags.get(position);

            // Fix the recycling of the holders.
            if (isSelected(position)) {
                nfcTagHolder.itemView.setActivated(true);
            } else {
                nfcTagHolder.itemView.setActivated(false);
            }

            nfcTagHolder.bindRiddle(nfcTag);
        }

        @Override
        public int getItemCount() {
            return nfcTags.size();
        }

        public void setSelectionMode(boolean isSelectableMode) {
            this.isSelectionMode = isSelectableMode;
        }

        public boolean isSelectionMode() {
            return isSelectionMode;
        }

        public void toggleSelection(int position) {
            if (selectedItems.get(position)) {
                selectedItems.delete(position);
            } else {
                selectedItems.put(position, true);
            }
        }

        public boolean isSelected(int position) {
            return selectedItems.get(position);
        }

        public void clearSelection() {
            selectedItems.clear();

            for (int i = 0; i < nfcTags.size(); i++) {
                View view = recyclerView.getChildAt(i);
                if (view instanceof CardView) {
                    view.setActivated(false);
                }
            }
        }

        public int getSelectionSize() {
            return selectedItems.size();
        }

        public void deleteSelectedItems() {
            for (int i = nfcTags.size(); i >= 0; i--) {
                if (isSelected(i)) {
                    NfcTag nfcTag = nfcTags.get(i);
                    MyTags.get(getActivity()).deleteNfcTag(nfcTag);
                    notifyItemRemoved(i);
                }
            }
        }
    }

    /**
     * Holds all sub views that depend on the current item’s data.
     */
    private class NfcTagHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView titleTextView;
        private TextView difficultyTextView;
        private ImageButton moreImageButton;

        private NfcTag nfcTag;
        private NfcTagAdapter adapter;

        public NfcTagHolder(final View view) {
            super(view);

            adapter = (NfcTagAdapter) recyclerView.getAdapter();

            // CardView OnClickListener.
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Contextual Action Bar is disabled.
                    if (!adapter.isSelectionMode()) {
                        // Start CreateTagPagerActivity.
                        Intent intent = new Intent(getActivity(), CreateTagPagerActivity.class);
                        intent.putExtra(CreateTagFragment.EXTRA_TAG_ID, nfcTag.getTagId());
                        startActivityForResult(intent, REQUEST_EDIT);
                    }
                    // Contextual Action Bar is enabled.
                    else {
                        // Toggle the selected view.
                        selectItem(view);
                    }
                }
            });
            // CardView OnLongClickListener.
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onContextFragmentListener.onItemLongClicked();

                    // Enable selection mode.
                    adapter.setSelectionMode(true);

                    // Toggle selected view.
                    selectItem(view);

                    // Listen for MainActivity's events.
                    MainActivity.setOnContextActivityListener(new MainActivity.OnContextActivityListener() {
                        @Override
                        public void onDeleteIconPressed() {
                            adapter.deleteSelectedItems();

                            reorderNfcTags();

                            // Inform user.
                            Toast.makeText(getActivity(), "Items deleted", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onContextExited() {
                            adapter.setSelectionMode(false);
                            adapter.clearSelection();
                        }
                    });

                    return true;
                }
            });

            // Custom Fonts.
            Typeface typefaceBold = FontCache.get("fonts/Capture_it.ttf", getActivity());
            Typeface typefaceNormal = FontCache.get("fonts/amatic_bold.ttf", getActivity());

            imageView = (ImageView) view.findViewById(R.id.row_create_image_view);

            titleTextView = (TextView) view.findViewById(R.id.row_create_title_text_view);
            titleTextView.setTypeface(typefaceBold);

            difficultyTextView = (TextView) view.findViewById(R.id.row_create_difficulty_text_view);
            difficultyTextView.setTypeface(typefaceNormal);
            difficultyTextView.setTextColor(getResources().getColor(R.color.accent));

            moreImageButton = (ImageButton) view.findViewById(R.id.row_create_more_image_button);
            moreImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setupPopupWindow(moreImageButton);
                }
            });
        }

        private void selectItem(View view) {
            // Highlight selected view.
            if (adapter.isSelected(getAdapterPosition())) {
                view.setActivated(false);
            } else {
                view.setActivated(true);
            }

            adapter.toggleSelection(getAdapterPosition());
            onContextFragmentListener.onItemClicked(adapter.getSelectionSize());
        }

        private void setupPopupWindow(View view) {
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.popup_window, null);

            final PopupWindow popupWindow = new PopupWindow();
            popupWindow.setContentView(layout);
            popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
            popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            popupWindow.setFocusable(true);

            TextView deleteTextView = (TextView) layout.findViewById(R.id.popup_delete_text_view);
            deleteTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Delete the selected NfcTag.
                    MyTags.get(getActivity()).deleteNfcTag(nfcTag);

                    // Refresh the list.
                    recyclerView.getAdapter().notifyItemRemoved(getAdapterPosition());

                    reorderNfcTags();

                    popupWindow.dismiss();
                }
            });

            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            if (Build.VERSION.SDK_INT >= 21) {
                popupWindow.setElevation(24);
            }
            popupWindow.showAsDropDown(view, 25, -265);
        }

        public void bindRiddle(NfcTag nfcTag) {
            this.nfcTag = nfcTag;

            PictureUtils.loadRecyclerViewBitmap(nfcTag.getPictureFilePath(), imageView);
            //PictureUtils.loadBitmapWithPicasso(getActivity(), nfcTag.getPictureFilePath(), imageView);
            titleTextView.setText(nfcTag.getTitle());
            difficultyTextView.setText(nfcTag.getDifficulty());
        }
    }

    private void reorderNfcTags() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MyTags.get(getActivity()).reorderNfcTags();
                recyclerView.getAdapter().notifyItemRangeChanged(0, MyTags.get(getActivity()).getNfcTags().size());
            }
        }, 1000);
    }
}
