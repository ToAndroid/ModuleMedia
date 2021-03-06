package com.chestnut.media.v.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.chestnut.media.R;
import com.chestnut.media.contract.MediaManager;
import com.chestnut.media.contract.MusicBuilder;
import com.chestnut.media.contract.MusicContract;
import com.chestnut.media.p.MusicPresenter;
import com.chestnut.media.utils.TimeUtils;

import java.io.File;

public class MusicDialogActivity extends Activity implements MusicContract.V, View.OnClickListener, SeekBar.OnSeekBarChangeListener{

    private MusicContract.P p;
    private SeekBar seekBar;
    private ImageView imgPausePlay;
    private TextView tvProgress;
    private boolean isDragByUser = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //隐去标题栏（应用程序的名字）
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.media_activity_music);
        //设置宽度
        WindowManager m = getWindowManager();
        Display d = m.getDefaultDisplay();
        android.view.WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.width = (int) (d.getWidth() * 0.90f);
        getWindow().setAttributes(layoutParams);
        //view
        imgPausePlay = findViewById(R.id.img_pause_play);
        imgPausePlay.setOnClickListener(this);
        seekBar = findViewById(R.id.seekBar_progress);
        tvProgress = findViewById(R.id.tv_progress);
        seekBar.setOnSeekBarChangeListener(this);
        //init
        p = new MusicPresenter();
        Intent intent = getIntent();
        if (intent!=null && intent.getExtras()!=null) {
            long longKey = intent.getLongExtra(MediaManager.Key_Builder, -1);
            MusicBuilder builder = (MusicBuilder) MediaManager.getInstance().popAndClean(longKey);
            //view title
            TextView textView = findViewById(R.id.tv_title);
            if (!TextUtils.isEmpty(builder.title)) {
                textView.setText(builder.title);
            }
            else {
                File file = new File(builder.url);
                textView.setText(file.getName());
            }
            //listener
            builder.setCallback(new MusicBuilder.Callback() {
                @Override
                public void onBuffering(int percent) {
                    seekBar.setMax(p.getTotalSecond());
                    seekBar.setSecondaryProgress((int) (p.getTotalSecond()*percent*0.01f));
                }

                @Override
                public void onSeekTo(int seconds) {

                }

                @Override
                public void onStart() {
                    imgPausePlay.setImageResource(R.drawable.media_pause_black);
                }

                @Override
                public void onStop() {
                    imgPausePlay.setImageResource(R.drawable.media_play_black);
                }

                @Override
                public void onCompletion() {
                    imgPausePlay.setImageResource(R.drawable.media_play_black);
                    seekBar.setProgress(0);
                }

                @Override
                public void onPause() {
                    imgPausePlay.setImageResource(R.drawable.media_play_black);
                }

                @Override
                public void onErr(int code, String msg) {
                    imgPausePlay.setImageResource(R.drawable.media_play_black);
                    seekBar.setProgress(0);
                }

                @Override
                public void onProgressChange(int currentSecond, int totalSecond) {
                    if (!isDragByUser) {
                        seekBar.setMax(totalSecond);
                        seekBar.setProgress(currentSecond);
                        tvProgress.setText(TimeUtils.toMediaTime(currentSecond)+" / "+TimeUtils.toMediaTime(totalSecond));
                    }
                }
            });
            p.setBuilder(builder);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        p.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        p.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        p.release();
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.img_pause_play) {
            if (p.isPlaying())
                p.pause();
            else
                p.start();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        tvProgress.setText(TimeUtils.toMediaTime(seekBar.getProgress())+" / "+TimeUtils.toMediaTime(p.getTotalSecond()));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isDragByUser = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        isDragByUser = false;
        p.seekToSecond(seekBar.getProgress());
        p.start();
    }
}
