/*
 * Copyright (C) 2016 Karipidis Miltiadis
 */

package com.karhades.tag_it.main.controller.fragment;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.karhades.tag_it.R;
import com.karhades.tag_it.main.controller.activity.CreateTagActivity;
import com.karhades.tag_it.main.controller.activity.EditTagPagerActivity;
import com.karhades.tag_it.main.model.MyTags;
import com.karhades.tag_it.main.model.NfcTag;
import com.karhades.tag_it.utils.PictureLoader;
import com.karhades.tag_it.utils.TransitionHelper;

import java.util.List;

/**
 * Controller Fragment class that binds the create tab with the data set.
 */
public class CreateGameFragment extends Fragment {

    /**
     * Request codes, used for startActivityForResult().
     */
    private static final int REQUEST_INSERT = 0;
    private static final int REQUEST_EDIT = 1;
    private static final int REQUEST_DELETE = 2;

    /**
     * Extra key, used for the position value returned from startActivityForResult().
     */
    private static final String EXTRA_POSITION = "com.karhades.tag_it.position";

    /**
     * Widget variables.
     */
    private RecyclerView mRecyclerView;
    private NfcTagAdapter mNfcTagAdapter;
    private FloatingActionButton mAddActionButton;
    private LinearLayout mEmptyLinearLayout;

    /**
     * Instance variables.
     */
    private List<NfcTag> mNfcTags;

    /**
     * Transition variables.
     */
    private ViewGroup mSceneRoot;
    private View mRevealContent;
    private ViewGroup.LayoutParams mOriginalLayoutParams;

    /**
     * Interface variable.
     */
    private Callbacks mCallbacks;

    /**
     * Interface definition for a callback to be invoked when
     * the fragment enters contextual mode.
     */
    public interface Callbacks {
        void onItemLongClicked();

        void onItemClicked(int tagsSelected);

        void onFragmentAttached(CreateGameFragment fragment);

        void onItemDeleted(String title);
    }

    public static CreateGameFragment newInstance() {
        return new CreateGameFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mNfcTags = MyTags.get(getActivity()).getNfcTags();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallbacks = (Callbacks) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement CreateGameFragment.Callbacks interface.");
        }
        mCallbacks.onFragmentAttached(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mCallbacks = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO: Doesn't get called.
        if (requestCode == REQUEST_INSERT) {
            if (resultCode == Activity.RESULT_OK) {
                mNfcTagAdapter.notifyItemInserted(mNfcTags.size());
            }
        } else if (requestCode == REQUEST_EDIT) {
            if (resultCode == Activity.RESULT_OK) {
                mNfcTagAdapter.notifyItemChanged(data.getIntExtra(EXTRA_POSITION, -1));
            }
        } else if (requestCode == REQUEST_DELETE) {
            if (resultCode == Activity.RESULT_OK) {
                // Gets the NFC tag from the intent extra.
                String tagId = (String) data.getSerializableExtra(DeleteDialogFragment.EXTRA_TAG_ID);
                NfcTag nfcTag = MyTags.get(getActivity()).getNfcTag(tagId);

                // Gets the adapter position from the intent extra.
                int adapterPosition = (int) data.getSerializableExtra(DeleteDialogFragment.EXTRA_ADAPTER_POSITION);

                // Deletes the selected NFC tag.
                mNfcTagAdapter.deleteSelectedItem(nfcTag, adapterPosition);

                // Invokes MainActivity's callback method.
                mCallbacks.onItemDeleted(nfcTag.getTitle());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_game, container, false);

        setupRecyclerView(view);
        setupEmptyView(view);

        return view;
    }

    private void setupRecyclerView(View view) {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.create_game_recycler_view);
        // Forces the recycling of items (Default=2).
        mRecyclerView.setItemViewCacheSize(0);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
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
        mRecyclerView.setAdapter(mNfcTagAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // If scrolling down (dy > 0).
                // The faster the scrolling the bigger the dy.
                if (dy > 0) {
                    mAddActionButton.hide();
                }
                // If scrolling up.
                else if (dy < 0) {
                    mAddActionButton.show();
                }
            }
        });
    }

    private void setupEmptyView(View view) {
        mEmptyLinearLayout = (LinearLayout) view.findViewById(R.id.create_game_empty_linear_layout);
        hideRecyclerViewIfEmpty();
    }

    private void hideRecyclerViewIfEmpty() {
        if (mNfcTags == null || mNfcTags.size() == 0) {
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

        restoreLayoutAfterTransition();

        // Updates UI.
        mNfcTagAdapter.notifyDataSetChanged();
        hideRecyclerViewIfEmpty();
    }

    public int contextGetSelectionSize() {
        return mNfcTagAdapter.getSelectionSize();
    }

    public void contextDeleteSelectedItems() {
        mNfcTagAdapter.deleteSelectedItems();

        reorderNfcTags();
    }

    public void contextSelectAll() {
        mNfcTagAdapter.selectAll();
    }

    public void contextClearSelection() {
        mNfcTagAdapter.clearSelection();
    }

    public void contextFinish() {
        mNfcTagAdapter.setSelectionMode(false);
        mNfcTagAdapter.clearSelection();
    }

    /**
     * Wraps the data set and creates views for individual items. It's the
     * intermediate that sits between the RecyclerView and the data set.
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
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_create_game_fragment, viewGroup, false);

            return new NfcTagHolder(view);
        }

        // Replace the contents of a view (invoked by the layout manager).
        @Override
        public void onBindViewHolder(NfcTagHolder nfcTagHolder, int position) {
            NfcTag nfcTag = mNfcTags.get(position);

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
            return (mNfcTags == null) ? 0 : mNfcTags.size();
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

        public void selectAll() {
            for (int i = 0; i < mNfcTags.size(); i++) {
                View view = mRecyclerView.getChildAt(i);
                if (view instanceof CardView) {
                    view.setActivated(true);
                }
                selectedItems.put(i, true);
            }
            mCallbacks.onItemClicked(getSelectionSize());
        }

        public void clearSelection() {
            selectedItems.clear();

            for (int i = 0; i < mNfcTags.size(); i++) {
                View view = mRecyclerView.getChildAt(i);
                if (view instanceof CardView) {
                    view.setActivated(false);
                }
            }
            mCallbacks.onItemClicked(getSelectionSize());
        }

        public int getSelectionSize() {
            return selectedItems.size();
        }

        public void deleteSelectedItems() {
            for (int i = mNfcTags.size(); i >= 0; i--) {
                if (isSelected(i)) {
                    // Gets the selected NFC tag.
                    NfcTag nfcTag = mNfcTags.get(i);

                    // Deletes the selected NFC tag.
                    MyTags.get(getActivity()).deleteNfcTag(nfcTag);

                    // Notifies the adapter.
                    notifyItemRemoved(i);
                }
            }
        }

        public void deleteSelectedItem(NfcTag nfcTag, int adapterPosition) {
            // Deletes the given NFC tag.
            MyTags.get(getActivity()).deleteNfcTag(nfcTag);

            // Notifies the adapter.
            notifyItemRemoved(adapterPosition);

            reorderNfcTags();
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
        private NfcTagAdapter nfcTagAdapter;

        @SuppressWarnings("deprecation")
        public NfcTagHolder(View view) {
            super(view);

            nfcTagAdapter = (NfcTagAdapter) mRecyclerView.getAdapter();

            setupTouchListener(view);
            setupClickListener(view);
            setupLongClickListener(view);

            imageView = (ImageView) view.findViewById(R.id.row_create_image_view);

            titleTextView = (TextView) view.findViewById(R.id.row_create_title_text_view);

            difficultyTextView = (TextView) view.findViewById(R.id.row_create_difficulty_text_view);
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
            if (nfcTagAdapter.isSelected(getAdapterPosition())) {
                view.setActivated(false);
            } else {
                view.setActivated(true);
            }

            nfcTagAdapter.toggleSelection(getAdapterPosition());
            mCallbacks.onItemClicked(nfcTagAdapter.getSelectionSize());
        }

        @SuppressLint("InflateParams")
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
                    // Creates and shows the dialog fragment.
                    DeleteDialogFragment dialog = DeleteDialogFragment.newInstance(nfcTag.getTagId(), getAdapterPosition());
                    dialog.setTargetFragment(CreateGameFragment.this, REQUEST_DELETE);
                    dialog.show(getActivity().getSupportFragmentManager(), "delete");

                    popupWindow.dismiss();
                }
            });

            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            if (Build.VERSION.SDK_INT >= 21) {
                popupWindow.setElevation(24);
            }
            popupWindow.showAsDropDown(view, 25, -265);
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

        private void setupClickListener(final View view) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // If Contextual Action Bar is disabled.
                    if (!nfcTagAdapter.isSelectionMode()) {
                        // Starts EditTagPagerActivity.
                        Intent intent = EditTagPagerActivity.newIntent(getActivity(), nfcTag.getTagId());
                        startActivityForResult(intent, REQUEST_EDIT);
                    }
                    // If Contextual Action Bar is enabled.
                    else {
                        // Toggle the selected view.
                        selectItem(view);
                    }
                }
            });
        }

        private void setupLongClickListener(final View view) {
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mCallbacks.onItemLongClicked();

                    // Enable selection mode.
                    nfcTagAdapter.setSelectionMode(true);

                    // Toggle selected view.
                    selectItem(view);

                    return true;
                }
            });
        }

        public void bindRiddle(NfcTag nfcTag) {
            this.nfcTag = nfcTag;

            PictureLoader.loadBitmapWithPicasso(getActivity(), nfcTag.getPictureFilePath(), imageView);

            titleTextView.setText(nfcTag.getTitle());
            difficultyTextView.setText(nfcTag.getDifficulty());
        }
    }

    private void reorderNfcTags() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MyTags.get(getActivity()).reorderNfcTags();
                mNfcTagAdapter.notifyItemRangeChanged(0, MyTags.get(getActivity()).getNfcTags().size());
            }
        }, 1000);
    }

    public void setupFloatingActionButton(View view) {
        mAddActionButton = (FloatingActionButton) view;
        mAddActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TransitionHelper.isTransitionSupportedAndEnabled()) {
                    startCreateTagActivityWithTransition();
                }
                // No transitions.
                else {
                    startCreateTagActivity();
                }
            }
        });
    }

    public static class DeleteDialogFragment extends DialogFragment {

        private static final String EXTRA_TAG_ID = "com.karhades.tag_it.tag_id";
        private static final String EXTRA_ADAPTER_POSITION = "com.karhades.tag_it.adapter_position";

        private String tagId;
        private int adapterPosition;

        public static DeleteDialogFragment newInstance(String tagId, int adapterPosition) {
            Bundle bundle = new Bundle();
            bundle.putString(EXTRA_TAG_ID, tagId);
            bundle.putInt(EXTRA_ADAPTER_POSITION, adapterPosition);

            DeleteDialogFragment fragment = new DeleteDialogFragment();
            fragment.setArguments(bundle);

            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            tagId = getArguments().getString(EXTRA_TAG_ID);
            adapterPosition = getArguments().getInt(EXTRA_ADAPTER_POSITION);
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setMessage(getResources().getQuantityString(R.plurals.dialog_deleted_plural, 1))
                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // DO NOTHING.
                        }
                    })
                    .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            tagId = (String) getArguments().getSerializable(EXTRA_TAG_ID);
                            adapterPosition = (int) getArguments().getSerializable(EXTRA_ADAPTER_POSITION);

                            Intent intent = new Intent();
                            intent.putExtra(EXTRA_TAG_ID, tagId);
                            intent.putExtra(EXTRA_ADAPTER_POSITION, adapterPosition);

                            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
                        }
                    })
                    .create();
        }
    }

    public void setupTransitionViews(ViewGroup sceneRoot, ViewGroup revealContent) {
        mSceneRoot = sceneRoot;
        mRevealContent = revealContent;
        mOriginalLayoutParams = mAddActionButton.getLayoutParams();
    }

    @TargetApi(21)
    private void startCreateTagActivityWithTransition() {
        // Gets the transition from the XML file.
        Transition transition = TransitionInflater.from(getActivity()).inflateTransition(R.transition.changebounds_with_arcmotion);
        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {
                // DO NOTHING.
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                TransitionHelper.circularShow(mAddActionButton, mRevealContent, new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(getActivity(), CreateTagActivity.class);
                        Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(getActivity(), null).toBundle();
                        getActivity().startActivityForResult(intent, REQUEST_INSERT, bundle);
                    }
                });
            }

            @Override
            public void onTransitionCancel(Transition transition) {
                // DO NOTHING.
            }

            @Override
            public void onTransitionPause(Transition transition) {
                // DO NOTHING.
            }

            @Override
            public void onTransitionResume(Transition transition) {
                // DO NOTHING.
            }
        });

        // Curved motion transition.
        TransitionManager.beginDelayedTransition(mSceneRoot, transition);

        // Changes the action button's gravity from bottom|right to center.
        CoordinatorLayout.LayoutParams newLayoutParams = new CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.WRAP_CONTENT, CoordinatorLayout.LayoutParams.WRAP_CONTENT);
        newLayoutParams.gravity = Gravity.CENTER;
        mAddActionButton.setLayoutParams(newLayoutParams);
    }

    private void startCreateTagActivity() {
        Intent intent = new Intent(getActivity(), CreateTagActivity.class);
        startActivityForResult(intent, REQUEST_INSERT);
    }

    @TargetApi(21)
    private void restoreLayoutAfterTransition() {
        if (!TransitionHelper.isTransitionSupportedAndEnabled()) {
            return;
        }

        if (mRevealContent == null || mRevealContent.getVisibility() == View.INVISIBLE) {
            return;
        }

        TransitionHelper.circularHide(mAddActionButton, mRevealContent, new Runnable() {
            @Override
            public void run() {
                // Gets the transition from the XML file.
                Transition transition = TransitionInflater.from(getActivity()).inflateTransition(R.transition.changebounds_with_arcmotion);

                // Curved motion transition.
                TransitionManager.beginDelayedTransition(mSceneRoot, transition);

                // Revert action button to it's original position.
                mAddActionButton.setLayoutParams(mOriginalLayoutParams);
            }
        });
    }
}
