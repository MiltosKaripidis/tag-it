package com.karhades.tag_it.main.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.karhades.tag_it.R;
import com.karhades.tag_it.main.model.MyTags;
import com.karhades.tag_it.main.model.NfcTag;
import com.karhades.tag_it.utils.PictureLoader;

import java.util.List;

/**
 * Wraps the data set and creates views for individual items. It's the
 * intermediate that sits between the RecyclerView and the data set.
 */
public class CreateGameAdapter extends RecyclerView.Adapter<CreateGameAdapter.NfcTagHolder> {
    /**
     * The context needed for various resource related operations.
     */
    private Context mContext;

    /**
     * A list that holds all the NfcTag objects.
     */
    private List<NfcTag> mNfcTags;

    /**
     * An array that holds all the selected items from the contextual action mode.
     */
    private SparseBooleanArray mSelectedItems;

    /**
     * A boolean value indicating whether the contextual action mode has been enabled.
     */
    private boolean mIsSelectionMode = false;

    /**
     * Interface definition for a callback to be invoked when
     * the item has been clicked.
     */
    public interface OnItemClickListener {
        void onTouch(View view, MotionEvent motionEvent);

        void onClick(View view, int position);

        void onLongClick(View view, int position);

        void onMoreClick(View view, int position);
    }

    /**
     * Listener reference.
     */
    private OnItemClickListener mOnItemClickListener;

    /**
     * Registers a callback to be invoked when the item has been clicked.
     *
     * @param onItemClickListener The callback that will run.
     */
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public CreateGameAdapter(Context context, List<NfcTag> nfcTags) {
        mContext = context;
        mNfcTags = nfcTags;
        mSelectedItems = new SparseBooleanArray();
    }

    public void setNfcTags(List<NfcTag> nfcTags) {
        mNfcTags = nfcTags;
    }

    public NfcTag getNfcTag(int position) {
        return mNfcTags.get(position);
    }

    @Override
    public NfcTagHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_create_game_fragment, viewGroup, false);

        return new NfcTagHolder(view);
    }

    @Override
    public void onBindViewHolder(final NfcTagHolder nfcTagHolder, int position) {
        setupTouchListener(nfcTagHolder.itemView);
        setupClickListener(nfcTagHolder.itemView, position);
        setupLongClickListener(nfcTagHolder.itemView, position);

        bindNfcTag(nfcTagHolder);
    }

    private void setupTouchListener(View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mOnItemClickListener.onTouch(v, event);
                return false;
            }
        });
    }

    private void setupClickListener(View view, final int position) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onClick(v, position);
            }
        });
    }

    private void setupLongClickListener(final View view, final int position) {
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mOnItemClickListener.onLongClick(v, position);
                return true;
            }
        });
    }

    private void bindNfcTag(final NfcTagHolder nfcTagHolder) {
        NfcTag nfcTag = mNfcTags.get(nfcTagHolder.getAdapterPosition());

        // Fixes the recycling of the holders.
        if (isItemSelected(nfcTagHolder.getAdapterPosition())) {
            nfcTagHolder.itemView.setActivated(true);
        } else {
            nfcTagHolder.itemView.setActivated(false);
        }

        PictureLoader.loadBitmap(mContext, nfcTag.getPictureFilePath(), nfcTagHolder.imageView);
        nfcTagHolder.titleTextView.setText(nfcTag.getTitle());
        nfcTagHolder.difficultyTextView.setText(nfcTag.getDifficulty());
        nfcTagHolder.difficultyTextView.setTextColor(mContext.getResources().getColor(R.color.accent));
        nfcTagHolder.moreImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnItemClickListener.onMoreClick(nfcTagHolder.moreImageButton, nfcTagHolder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return (mNfcTags == null) ? 0 : mNfcTags.size();
    }

    public void setSelectionMode(boolean isSelectableMode) {
        mIsSelectionMode = isSelectableMode;
    }

    public boolean isSelectionMode() {
        return mIsSelectionMode;
    }

    public void toggleItemSelection(int position) {
        if (mSelectedItems.get(position)) {
            mSelectedItems.delete(position);
        } else {
            mSelectedItems.put(position, true);
        }
    }

    public boolean isItemSelected(int position) {
        return mSelectedItems.get(position);
    }

    public void selectAll(RecyclerView recyclerView) {
        for (int i = 0; i < mNfcTags.size(); i++) {
            View view = recyclerView.getChildAt(i);
            if (view instanceof CardView) {
                view.setActivated(true);
            }
            mSelectedItems.put(i, true);
        }
    }

    public void clearSelection(RecyclerView recyclerView) {
        mSelectedItems.clear();

        for (int i = 0; i < mNfcTags.size(); i++) {
            View view = recyclerView.getChildAt(i);
            if (view instanceof CardView) {
                view.setActivated(false);
            }
        }
    }

    public int getSelectionSize() {
        return mSelectedItems.size();
    }

    public void deleteSelectedItems() {
        for (int i = mNfcTags.size(); i >= 0; i--) {
            if (isItemSelected(i)) {
                // Gets the selected NFC tag.
                NfcTag nfcTag = mNfcTags.get(i);

                // Deletes the selected NFC tag.
                MyTags.get(mContext).deleteNfcTag(nfcTag);

                // Notifies the adapter.
                notifyItemRemoved(i);
            }
        }
    }

    /**
     * Holds all sub views that depend on the current item’s data.
     */
    static class NfcTagHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private TextView titleTextView;
        private TextView difficultyTextView;
        private ImageButton moreImageButton;

        NfcTagHolder(View view) {
            super(view);

            imageView = (ImageView) view.findViewById(R.id.row_create_image_view);
            titleTextView = (TextView) view.findViewById(R.id.row_create_title_text_view);
            difficultyTextView = (TextView) view.findViewById(R.id.row_create_difficulty_text_view);
            moreImageButton = (ImageButton) view.findViewById(R.id.row_create_more_image_button);
        }
    }
}
