package cn.com.buildwin.gosky.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import buildwin.common.Utilities;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnPageChange;
import cn.com.buildwin.gosky.R;
import cn.com.buildwin.gosky.widget.imageviewer.ImageViewPager;
import cn.com.buildwin.gosky.widget.imageviewer.ImageViewPagerAdapter;

public class PhotoActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String PHOTO_LIST = "photoList";
    private static final String PHOTO_INDEX = "photoIndex";

    private List<String> mPhotoList;
    private int mPhotoIndex;

    @BindView(R.id.image_view_pager) ImageViewPager mViewPager;
    private ImageViewPagerAdapter mPagerAdapter;

    @BindView(R.id.file_name_textView) TextView mFileNameTextView;
    @BindView(R.id.delete_imageButton) ImageButton mDeleteButton;

    @BindString(R.string.image_viewer_confirm_delete)   String confirmMessage;
    @BindString(R.string.image_viewer_delete_button)    String deleteText;
    @BindString(R.string.image_viewer_cancel_button)    String cancelText;
    @BindString(R.string.image_viewer_delete_fail)      String failText;
    @BindString(R.string.image_viewer_alert_deleted)    String alertDeletedText;

    public static Intent newIntent(Context context, ArrayList<String> photoList, int photoIndex) {
        Intent intent = new Intent(context, PhotoActivity.class);
        intent.putStringArrayListExtra(PHOTO_LIST, photoList);
        intent.putExtra(PHOTO_INDEX, photoIndex);
        return intent;
    }

    public static void intentTo(Context context, ArrayList<String> photoList) {
        context.startActivity(newIntent(context, photoList, 0));
    }

    public static void intentTo(Context context, ArrayList<String> photoList, int photoIndex) {
        context.startActivity(newIntent(context, photoList, photoIndex));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_photo);
        ButterKnife.bind(this);

        // handle arguments
        mPhotoList = getIntent().getStringArrayListExtra(PHOTO_LIST);
        mPhotoIndex = getIntent().getIntExtra(PHOTO_INDEX, 0);

        // 设置ViewPager
        mPagerAdapter = new ImageViewPagerAdapter(getSupportFragmentManager(), mPhotoList);
        mViewPager.setAdapter(mPagerAdapter);

        // 页面改变后，显示对应文件名称
        setFileNameText(mPhotoIndex); // 如果mPhotoIndex是0，不会调用onPageSelected，这里手动调用一次显示
        mViewPager.setCurrentItem(mPhotoIndex);
    }

    /**
     * 显示对应文件的名称
     * @param index 文件名索引
     */
    private void setFileNameText(int index) {
        String filePath = mPhotoList.get(index);
        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        mFileNameTextView.setText(fileName);
    }

    @OnClick(R.id.delete_imageButton)
    public void onClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(confirmMessage)
                .setPositiveButton(deleteText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int index = mViewPager.getCurrentItem();
                        String filePath = mPhotoList.get(index);
                        if (Utilities.deleteFile(filePath)) {
                            mPagerAdapter.removeItem(index);
                            Toast.makeText(PhotoActivity.this, alertDeletedText, Toast.LENGTH_SHORT).show();
                            // 如果文件列表已经全部删除，则退出
                            if (mPagerAdapter.getCount() == 0) {
                                finish();
                            }
                        } else {
                            showAlertDialog(failText);
                        }
                    }
                })
                .setNegativeButton(cancelText, null)
                .setCancelable(false)
                .create().show();
    }

    // 显示提示框
    private void showAlertDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setPositiveButton("OK", null)
                .setCancelable(false)
                .create().show();
    }

    /* ViewPager.OnPageChangeListener */

    @OnPageChange(value = R.id.image_view_pager, callback = OnPageChange.Callback.PAGE_SCROLLED)
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        // 动画显示文件名
        if (positionOffset < 0.5) {
            setFileNameText(position);
            mFileNameTextView.setAlpha(1 - positionOffset * 2);
        } else {
            setFileNameText(position + 1);
            mFileNameTextView.setAlpha(positionOffset * 2 - 1);
        }
    }

    @OnPageChange(value = R.id.image_view_pager, callback = OnPageChange.Callback.PAGE_SELECTED)
    public void onPageSelected(int position) {
        setFileNameText(position);
    }

    @OnPageChange(value = R.id.image_view_pager, callback = OnPageChange.Callback.PAGE_SCROLL_STATE_CHANGED)
    public void onPageScrollStateChanged(int state) {
        // 动画显示删除按钮
        if (state == ViewPager.SCROLL_STATE_IDLE) {
            mDeleteButton.animate()
                    .alpha(1)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            super.onAnimationStart(animation);
                            mDeleteButton.setVisibility(View.VISIBLE);
                        }
                    });
        } else {
            mDeleteButton.animate()
                    .alpha(0)
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            mDeleteButton.setVisibility(View.INVISIBLE);
                        }
                    });
        }
    }
}
