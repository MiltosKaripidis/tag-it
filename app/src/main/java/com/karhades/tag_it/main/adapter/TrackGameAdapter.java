package com.karhades.tag_it.main.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.karhades.tag_it.R;
import com.karhades.tag_it.main.model.NfcTag;
import com.karhades.tag_it.utils.PictureLoader;
import com.karhades.tag_it.utils.TransitionHelper;

import java.util.List;

/**
 * Wraps the data set and creates views for individual items. It's the
 * intermediate that sits between the RecyclerView and the data set.
 */
public class TrackGameAdapter extends RecyclerView.Adapter<TrackGameAdapter.NfcTagHolder> {
    /**
     * The context needed for various resource related operations.
     */
    private Context mContext;

    /**
     * A list that holds all the NfcTag objects.
     */
    private List<NfcTag> mNfcTags;

    /**
     * Interface definition for a callback to be invoked when
     * the item has been clicked.
     */
    public interface OnItemClickListener {
        void onTouch(View view, MotionEvent motionEvent);

        void onClick(View view, int position);
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

    public TrackGameAdapter(Context context, List<NfcTag> nfcTags) {
        mContext = context;
        mNfcTags = nfcTags;
    }

    public void setNfcTags(List<NfcTag> nfcTags) {
        mNfcTags = nfcTags;
    }

    public NfcTag getNfcTag(int position) {
        return mNfcTags.get(position);
    }

    @Override
    public NfcTagHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_track_game_fragment, viewGroup, false);

        return new NfcTagHolder(view);
    }

    @Override
    public void onBindViewHolder(NfcTagHolder nfcTagHolder, int position) {
        setupTouchListener(nfcTagHolder.itemView);
        setupClickListener(nfcTagHolder.itemView, position);

        bindNfcTag(nfcTagHolder);
    }

    /**
     * Helper method for binding data on the adapter's
     * onBindViewHolder() method.
     *
     * @param nfcTagHolder The NfcTagHolder object to bind data to views.
     */
    private void bindNfcTag(NfcTagHolder nfcTagHolder) {
        NfcTag nfcTag = mNfcTags.get(nfcTagHolder.getAdapterPosition());

        if (TransitionHelper.isTransitionSupportedAndEnabled()) {
            nfcTagHolder.imageView.setTransitionName("image" + nfcTag.getTagId());
            nfcTagHolder.imageView.setTag("image" + nfcTag.getTagId());
        }

        PictureLoader.loadBitmap(mContext, nfcTag.getPictureFilePath(), nfcTagHolder.imageView);
        nfcTagHolder.titleTextView.setText(nfcTag.getTitle());
        nfcTagHolder.difficultyTextView.setText(nfcTag.getDifficulty());
        nfcTagHolder.discoveredCheckBox.setChecked(nfcTag.isDiscovered());
        if (nfcTagHolder.discoveredCheckBox.isChecked()) {
            nfcTagHolder.discoveredCheckBox.setVisibility(View.VISIBLE);
        } else {
            nfcTagHolder.discoveredCheckBox.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Card resting elevation is 2dp and Card raised elevation is 8dp. Animate the changes between them.
     *
     * @param view The CardView to animate.
     */
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

    @Override
    public int getItemCount() {
        return (mNfcTags == null) ? 0 : mNfcTags.size();
    }

    /**
     * Describes the view and it's child views that
     * will bind the data for an adapter item.
     */
    static class NfcTagHolder extends RecyclerView.ViewHolder {
        /**
         * Widget references.
         */
        private ImageView imageView;
        private TextView titleTextView;
        private TextView difficultyTextView;
        private CheckBox discoveredCheckBox;

        /**
         * Constructor that registers any listeners and make calls
         * to findViewById() for each adapter item.
         *
         * @param view The view describing an adapter item (CardView).
         */
        NfcTagHolder(View view) {
            super(view);

            imageView = (ImageView) view.findViewById(R.id.row_track_image_view);
            titleTextView = (TextView) view.findViewById(R.id.row_track_title_text_view);
            difficultyTextView = (TextView) view.findViewById(R.id.row_track_difficulty_text_view);
            discoveredCheckBox = (CheckBox) view.findViewById(R.id.row_track_discovered_check_box);
        }
    }
}
