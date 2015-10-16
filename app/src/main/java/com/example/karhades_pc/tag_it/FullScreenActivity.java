package com.example.karhades_pc.tag_it;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.example.karhades_pc.utils.PictureLoader;

/**
 * Created by Karhades on 15-10-15.
 */
public class FullScreenActivity extends Activity {

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        toggleSystemUI();

        setContentView(R.layout.activity_fullscreen);

        // Get file path from intent extra.
        String filePath = getIntent().getStringExtra(TrackingTagFragment.EXTRA_FILE_PATH);

        imageView = (ImageView) findViewById(R.id.full_screen_image_view);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSystemUI();
            }
        });

        // Load image.
        PictureLoader.loadBitmapWithPicasso(this, filePath, imageView);
    }

    private void toggleSystemUI() {
        if (Build.VERSION.SDK_INT >= 16) {
            // Full screen | Hide navigation bar.
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }
}
