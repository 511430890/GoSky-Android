package cn.com.buildwin.gosky.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.com.buildwin.gosky.R;

public class ReviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_review);
        ButterKnife.bind(this);
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

    @OnClick({R.id.review_back_button, R.id.review_photo_button, R.id.review_video_button, R.id.review_card_media_button})
    public void onClick(View v) {
        switch (v.getId()) {
            // Back Button
            case R.id.review_back_button: {
                finish();
                break;
            }
            // Photo Button
            case R.id.review_photo_button: {
                Intent i = new Intent(ReviewActivity.this, PhotoListActivity.class);
                startActivity(i);
                break;
            }
            // Video Button
            case R.id.review_video_button: {
                Intent i = new Intent(ReviewActivity.this, VideoListActivity.class);
                startActivity(i);
                break;
            }
            // Card Media Button
            case R.id.review_card_media_button: {
                Intent intent = new Intent(ReviewActivity.this, CardMediaListActivity.class);
                startActivity(intent);
                break;
            }
        }
        // Activity slide from left
        overridePendingTransition(
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
        );
    }
}
