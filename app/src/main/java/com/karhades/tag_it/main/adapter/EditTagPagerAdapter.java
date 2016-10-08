package com.karhades.tag_it.main.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.karhades.tag_it.main.controller.fragment.EditTagFragment;
import com.karhades.tag_it.main.model.NfcTag;

import java.util.List;

/**
 * FragmentStatePagerAdapter that handles fragment paging for a ViewPager.
 */
public class EditTagPagerAdapter extends FragmentStatePagerAdapter {

    private List<NfcTag> mNfcTags;
    private EditTagFragment currentFragment;

    public EditTagPagerAdapter(FragmentManager fragmentManager, List<NfcTag> nfcTags) {
        super(fragmentManager);
        mNfcTags = nfcTags;
    }

    @Override
    public Fragment getItem(int position) {
        NfcTag nfcTag = mNfcTags.get(position);

        return EditTagFragment.newInstance(nfcTag.getTagId());
    }

    @Override
    public int getCount() {
        return (mNfcTags == null) ? 0 : mNfcTags.size();
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);

        currentFragment = (EditTagFragment) object;
    }

    public EditTagFragment getCurrentFragment() {
        return currentFragment;
    }
}
