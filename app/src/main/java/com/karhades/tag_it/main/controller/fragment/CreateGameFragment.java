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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.karhades.tag_it.R;
import com.karhades.tag_it.main.adapter.CreateGameAdapter;
import com.karhades.tag_it.main.controller.activity.CreateTagActivity;
import com.karhades.tag_it.main.controller.activity.EditTagPagerActivity;
import com.karhades.tag_it.main.model.MyTags;
import com.karhades.tag_it.main.model.NfcTag;
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
    private CreateGameAdapter mCreateGameAdapter;
    private FloatingActionButton mAddActionButton;
    private LinearLayout mEmptyLinearLayout;

    /**
     * Instance variables.
     */
    private List<NfcTag> mNfcTags;

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
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_INSERT) {
            if (resultCode == Activity.RESULT_OK) {
                int position = mNfcTags.size();
                mCreateGameAdapter.notifyItemInserted(position);
            }
        } else if (requestCode == REQUEST_EDIT) {
            if (resultCode == Activity.RESULT_OK) {
                int position = data.getExtras().getInt(EXTRA_POSITION);
                mCreateGameAdapter.notifyItemChanged(position);
            }
        } else if (requestCode == REQUEST_DELETE) {
            if (resultCode == Activity.RESULT_OK) {
                // Gets the NFC tag from the intent extra.
                String tagId = (String) data.getSerializableExtra(DeleteDialogFragment.EXTRA_TAG_ID);
                // Gets the NfcTag from the tag ID.
                NfcTag nfcTag = MyTags.get(getActivity()).getNfcTag(tagId);
                // Deletes the given NFC tag.
                MyTags.get(getActivity()).deleteNfcTag(nfcTag);

                // Gets the adapter position from the intent extra.
                int adapterPosition = (int) data.getSerializableExtra(DeleteDialogFragment.EXTRA_ADAPTER_POSITION);
                // Notifies the adapter.
                mCreateGameAdapter.notifyItemRemoved(adapterPosition);

                // Invokes MainActivity's callback method.
                mCallbacks.onItemDeleted(nfcTag.getTitle());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_game, container, false);

        setupAdapter();
        setupRecyclerView(view);
        setupEmptyView(view);

        return view;
    }

    private void setupAdapter() {
        mCreateGameAdapter = new CreateGameAdapter(getActivity(), mNfcTags);
        mCreateGameAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                updateUI();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                updateUI();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                updateUI();
            }
        });
        // Custom adapter callbacks.
        mCreateGameAdapter.setOnItemClickListener(new CreateGameAdapter.OnItemClickListener() {
            @Override
            public void onTouch(View view, MotionEvent motionEvent) {
                setupItemTouchListener(view, motionEvent);
            }

            @Override
            public void onClick(View view, int position) {
                setupItemClickListener(view, position);
            }

            @Override
            public void onLongClick(View view, int position) {
                setupItemLongClickListener(view, position);
            }

            @Override
            public void onMoreClick(View view, int position) {
                setupPopupWindow(view, position);
            }
        });
    }

    /**
     * Card resting elevation is 2dp and Card raised elevation is 8dp. Animate the changes between them.
     *
     * @param view The CardView to animate.
     */
    private void setupItemTouchListener(View view, MotionEvent motionEvent) {
        Animator startAnimator = AnimatorInflater.loadAnimator(getActivity(), R.animator.card_view_elevate);
        Animator endAnimator = AnimatorInflater.loadAnimator(getActivity(), R.animator.card_view_rest);

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startAnimator.setTarget(view);
                startAnimator.start();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                endAnimator.setTarget(view);
                endAnimator.start();
                break;
        }
    }

    private void setupItemClickListener(View view, int position) {
        // If Contextual Action Bar is disabled.
        if (!mCreateGameAdapter.isSelectionMode()) {
            // Starts EditTagPagerActivity.
            Intent intent = EditTagPagerActivity.newIntent(getActivity(), mCreateGameAdapter.getNfcTag(position).getTagId());
            startActivityForResult(intent, REQUEST_EDIT);
        }
        // If Contextual Action Bar is enabled.
        else {
            // Toggle the selected view.
            selectItem(view, position);
        }
    }

    private void setupItemLongClickListener(View view, int position) {
        mCallbacks.onItemLongClicked();

        // Enable selection mode.
        mCreateGameAdapter.setSelectionMode(true);

        // Toggle selected view.
        selectItem(view, position);
    }

    @SuppressLint("InflateParams")
    private void setupPopupWindow(View view, final int position) {
        final NfcTag nfcTag = mCreateGameAdapter.getNfcTag(position);

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
                DeleteDialogFragment dialog = DeleteDialogFragment.newInstance(nfcTag.getTagId(), position);
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

    private void selectItem(View view, int position) {
        // Highlights the selected view.
        if (mCreateGameAdapter.isItemSelected(position)) {
            view.setActivated(false);
        } else {
            view.setActivated(true);
        }

        mCreateGameAdapter.toggleItemSelection(position);
        mCallbacks.onItemClicked(mCreateGameAdapter.getSelectionSize());
    }

    private void setupRecyclerView(View view) {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.create_game_recycler_view);
        // Forces the recycling of items (Default=2).
        mRecyclerView.setItemViewCacheSize(0);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mCreateGameAdapter);
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

        updateUI();
    }

    private void updateUI() {
        mNfcTags = MyTags.get(getActivity()).getNfcTags();
        mCreateGameAdapter.setNfcTags(mNfcTags);
        hideRecyclerViewIfEmpty();
    }

    public int contextGetSelectionSize() {
        return mCreateGameAdapter.getSelectionSize();
    }

    public void contextDeleteSelectedItems() {
        mCreateGameAdapter.deleteSelectedItems();
    }

    public void contextSelectAll() {
        mCreateGameAdapter.selectAll(mRecyclerView);
        int size = mCreateGameAdapter.getSelectionSize();
        mCallbacks.onItemClicked(size);
    }

    public void contextClearSelection() {
        mCreateGameAdapter.clearSelection(mRecyclerView);
    }

    public void contextFinish() {
        mCreateGameAdapter.setSelectionMode(false);
        mCreateGameAdapter.clearSelection(mRecyclerView);
        int size = mCreateGameAdapter.getSelectionSize();
        mCallbacks.onItemClicked(size);
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

    @TargetApi(21)
    private void startCreateTagActivityWithTransition() {
        Intent intent = new Intent(getActivity(), CreateTagActivity.class);
        Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(getActivity(), mAddActionButton, mAddActionButton.getTransitionName()).toBundle();
        getActivity().startActivityForResult(intent, REQUEST_INSERT, bundle);
    }

    private void startCreateTagActivity() {
        Intent intent = new Intent(getActivity(), CreateTagActivity.class);
        startActivityForResult(intent, REQUEST_INSERT);
    }
}
