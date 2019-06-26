package cn.com.buildwin.gosky.activities;

import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import buildwin.common.Utilities;
import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import cn.com.buildwin.gosky.R;

public abstract class MediaListActivity extends AppCompatActivity {

    @BindView(R.id.media_back_button)
    ImageButton mBackButton;
    @BindView(R.id.media_select_button)
    ImageButton mSelectButton;
    @BindView(R.id.media_select_all_button)
    ImageButton mSelectAllButton;
    @BindView(R.id.media_delete_button)
    ImageButton mDeleteButton;

    @BindView(R.id.media_selection_textView)
    TextView mSelectionTextView;

    @BindView(R.id.media_list_listView)
    ListView mMediaListView;
    @BindView(R.id.media_list_empty_textView)
    TextView listEmptyTextView;

    @BindString(R.string.media_list_confirm_delete)   String confirmMessage;
    @BindString(R.string.media_list_delete_button)    String deleteText;
    @BindString(R.string.media_list_cancel_button)    String cancelText;

    @BindDrawable(R.mipmap.media_selectall)
    Drawable selectAllNormalDrawble;
    @BindDrawable(R.mipmap.media_selectall_h)
    Drawable selectAllHighlightDrawble;

    private LinearLayout parentLinearLayout;

    protected List<String> mMediaList;

    private boolean mCheckedMode;
    protected SparseBooleanArray mCheckedArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        initContentView(R.layout.activity_media_list);
        ButterKnife.bind(this);

        // Load media list
        mMediaList = new ArrayList<>();

        // Media ListView Settings
        mMediaListView.setAdapter(getListAdapter());
        mMediaListView.setEmptyView(listEmptyTextView);

        // Init checkedArray
        mCheckedArray = new SparseBooleanArray();
    }

    /**
     * 布局模板使用
     *
     * @param layoutResId 布局资源ID
     */
    private void initContentView(int layoutResId) {
        ViewGroup viewGroup = (ViewGroup) findViewById(android.R.id.content);
        viewGroup.removeAllViews();
        parentLinearLayout = new LinearLayout(this);
        parentLinearLayout.setOrientation(LinearLayout.VERTICAL);
        viewGroup.addView(parentLinearLayout);
        LayoutInflater.from(this).inflate(layoutResId, parentLinearLayout, true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Load media list
        mMediaList = reloadMediaList();
        ((BaseAdapter) mMediaListView.getAdapter()).notifyDataSetChanged();
        // Listview layout animation
        mMediaListView.startLayoutAnimation();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        // Activity slide from left
        overridePendingTransition(
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
        );
    }

    /**
     * 以下三个作为布局模板时使用
     */
    @Override
    public void setContentView(int layoutResId) {
        LayoutInflater.from(this).inflate(layoutResId, parentLinearLayout, true);
    }

    @Override
    public void setContentView(View view) {
        parentLinearLayout.addView(view);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        parentLinearLayout.addView(view, params);
    }

    // 防止短时间多次点击
    private static final int MIN_CLICK_DELAY_TIME = 1000;
    private long lastClickTime = 0;

    /**
     * ListView onItemClick
     */
    @OnItemClick(R.id.media_list_listView)
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (mCheckedMode) {
            mCheckedArray = mMediaListView.getCheckedItemPositions();
            mMediaListView.invalidateViews();

            updateSelectionText();
        } else {
            long currentTime = Calendar.getInstance().getTimeInMillis();
            if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {
                lastClickTime = currentTime;
                // Display media
                displayMediaAtIndex(i);
            }
        }
    }

    /**
     * Buttons OnClick
     */
    @OnClick({R.id.media_back_button, R.id.media_select_button, R.id.media_select_all_button, R.id.media_delete_button})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.media_back_button:
            {
                finish();
                // Activity slide from left
                overridePendingTransition(
                        android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right
                );
                break;
            }
            case R.id.media_select_button:
            {
                if (mCheckedMode) {
                    mSelectButton.setImageResource(R.mipmap.media_select);
                    mDeleteButton.setVisibility(View.INVISIBLE);
                    mSelectAllButton.setVisibility(View.INVISIBLE);

                    updateSelectionText();
                    mSelectionTextView.setVisibility(View.INVISIBLE);

                    // Reset listView's checkedItems
                    mMediaListView.clearChoices();
                    mCheckedArray = mMediaListView.getCheckedItemPositions();
                    mMediaListView.invalidateViews();

                    mMediaListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
                    mCheckedMode = false;
                } else {
                    mCheckedMode = true;
                    mMediaListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

                    mSelectButton.setImageResource(R.mipmap.media_cancel);
                    mDeleteButton.setVisibility(View.VISIBLE);
                    mSelectAllButton.setVisibility(View.VISIBLE);

                    updateSelectionText();
                    mSelectionTextView.setVisibility(View.VISIBLE);
                }
                break;
            }
            case R.id.media_select_all_button:
            {
                if (selectedAll()) {
                    setAllItemsChecked(false);
                } else {
                    setAllItemsChecked(true);
                }
                updateSelectionText();
                break;
            }
            case R.id.media_delete_button:
            {
                if (mMediaListView.getCheckedItemCount() > 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(confirmMessage)
                            .setPositiveButton(deleteText, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Delete files
                                    for (int i=0; i<mMediaListView.getCount(); i++) {
                                        if (mCheckedArray.get(i)) {
                                            String filePath = mMediaList.get(i);
                                            Utilities.deleteFile(filePath);
                                        }
                                    }

                                    // Reset listView's checkedItems
                                    mMediaListView.clearChoices();
                                    mCheckedArray = mMediaListView.getCheckedItemPositions();
                                    mMediaListView.invalidateViews();
                                    // Reload list
                                    mMediaList = reloadMediaList();
                                    ((BaseAdapter)mMediaListView.getAdapter()).notifyDataSetChanged();

                                    updateSelectionText();
                                }
                            })
                            .setNegativeButton(cancelText, null)
                            .setCancelable(false)
                            .create().show();
                }
                break;
            }
        }
    }

    /**
     * 是否已经选择所有文件
     */
    private boolean selectedAll() {
        int checkedNumber = 0;
        mCheckedArray = mMediaListView.getCheckedItemPositions();
        for (int i = 0; i < mMediaListView.getCount(); i++) {
            if (mCheckedArray.get(i))
                checkedNumber++;
        }
        return checkedNumber == mMediaList.size();
    }

    /**
     * 设置所有列表项全选或者取消全选
     */
    private void setAllItemsChecked(boolean checked) {
        for (int i = 0; i < mMediaListView.getCount(); i++) {
            mMediaListView.setItemChecked(i, checked);
        }
    }

    /**
     * 更新选择文件数的显示
     */
    private void updateSelectionText() {
        if (mCheckedMode) {
            int checkedItemCount = mMediaListView.getCheckedItemCount();
            int totalItemCount = mMediaList.size();
            String selectionText =
                    String.format(Locale.getDefault(), "%d/%d", checkedItemCount, totalItemCount);
            mSelectionTextView.setText(selectionText);
        }
        // Set button image
        mSelectAllButton.setImageDrawable(selectedAll() ? selectAllNormalDrawble : selectAllHighlightDrawble);
    }

    // Abstract method

    /**
     * 载入媒体文件列表
     *
     * @return 媒体文件列表
     */
    protected abstract List<String> reloadMediaList();

    /**
     * 获取ListAdapter
     *
     * @return ListAdapter
     */
    protected abstract ListAdapter getListAdapter();

    /**
     * 展示媒体文件
     *
     * @param index 媒体文件索引
     */
    protected abstract void displayMediaAtIndex(int index);
}
