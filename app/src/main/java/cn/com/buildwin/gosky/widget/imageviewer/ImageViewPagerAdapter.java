package cn.com.buildwin.gosky.widget.imageviewer;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

import cn.com.buildwin.gosky.fragments.ImageFragment;

public class ImageViewPagerAdapter extends FragmentStatePagerAdapter {

    private List<String> mImageList;

    public ImageViewPagerAdapter(FragmentManager fm, List<String> list) {
        super(fm);
        mImageList = list;
    }

    @Override
    public Fragment getItem(int position) {
        String imagePath = mImageList.get(position);
        Fragment fragment = ImageFragment.newInstance(imagePath);
        return fragment;
    }

    @Override
    public int getCount() {
        return mImageList.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public void removeItem(int position) {
        mImageList.remove(position);
        notifyDataSetChanged();
    }
}
