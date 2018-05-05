package com.hrrock.snapbook.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.hrrock.snapbook.fragments.HomeCameraViewFragment;
import com.hrrock.snapbook.fragments.MomentCameraViewFragment;
import com.hrrock.snapbook.fragments.NewsFeedFragment;

/**
 * Created by hp-u on 2/18/2018.
 */

public class HomePagerAdapter extends FragmentPagerAdapter {
    private Fragment fragment = null;
    private static final int POSITION_CAMERA_VIEW = 0;
    private static final int POSITION_NEWS_FEED = 1;

    public HomePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if (position == POSITION_CAMERA_VIEW) {
            fragment = new HomeCameraViewFragment();
        }

        if (position == POSITION_NEWS_FEED) {
            fragment = new NewsFeedFragment();
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
