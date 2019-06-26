package cn.com.buildwin.gosky.activities;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnPageChange;
import cn.com.buildwin.gosky.R;
import cn.com.buildwin.gosky.fragments.HelpPageFragment;

// Note: 修改帮助页面图像请到HelpPageFragment.java

public class HelpActivity extends FragmentActivity {

    @BindView(R.id.help_viewPager)          ViewPager mViewPager;
    private PagerAdapter mPagerAdapter;
    
    @BindView(R.id.help_backButton)         ImageButton backButton;
    @BindView(R.id.help_prev_pageButton)    ImageButton prevPageButton;
    @BindView(R.id.help_next_pageButton)    ImageButton nextPageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_help);
        ButterKnife.bind(this);

        // ViewPager and PagerAdapter
        mPagerAdapter = new HelpPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
    }

    /**
     * 更新按键状态
     */
    private void updateButtonState() {
        HelpPagerAdapter adapter = (HelpPagerAdapter)mViewPager.getAdapter();
        HelpPageFragment fragment = (HelpPageFragment)adapter
                .instantiateItem(mViewPager, mViewPager.getCurrentItem());

        int pageNumber = fragment.getPageNumber();              // 当前页码
        int pageNumbers = HelpPageFragment.getPageNumbers();    // 总页数

        if (pageNumber == 0) {
            prevPageButton.setVisibility(View.INVISIBLE);
        } else {
            prevPageButton.setVisibility(View.VISIBLE);
        }
        if (pageNumber == pageNumbers - 1) {
            nextPageButton.setVisibility(View.INVISIBLE);
        } else {
            nextPageButton.setVisibility(View.VISIBLE);
        }
    }

    @OnPageChange(value = R.id.help_viewPager, callback = OnPageChange.Callback.PAGE_SCROLLED)
    public void onPageScrolled(int position, float positionOffset,
                               int positionOffsetPixels) {
        // 更新按键状态
        updateButtonState();
    }

    @OnClick({R.id.help_backButton, R.id.help_prev_pageButton, R.id.help_next_pageButton})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.help_backButton:
                finish();
                break;
            case R.id.help_prev_pageButton:
                mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
                break;
            case R.id.help_next_pageButton:
                mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
                break;
        }
    }

    /**
     * 帮助页面的PagerAdapter
     */
    private class HelpPagerAdapter extends FragmentStatePagerAdapter {
        public HelpPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return HelpPageFragment.create(position);   // 创建新页面
        }

        @Override
        public int getCount() {
            return HelpPageFragment.getPageNumbers();   // 总页数
        }
    }

}
