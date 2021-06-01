package com.example.musicuniplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gauravk.audiovisualizer.visualizer.WaveVisualizer;

import java.io.File;
import java.util.ArrayList;

public class MusicPlayerActivity extends AppCompatActivity {
    private Button btnPlay, btnNext, btnPrev, btnFastForward, btnFastRewind;
    private TextView txtSongName, txtSongStart, txtSongStop;
    private SeekBar seekMusicBar;
    private WaveVisualizer visualizer;
    private ImageView imageView;

    private String songName;
    public static final String EXTRA_NAME = "song_name";
    private static MediaPlayer mediaPlayer;
    private int position;
    private ArrayList<File> mySongs;
    private Thread updateSeekBar;
    private String endTime;
    private int audiosessionId;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (visualizer != null) {
            visualizer.release();
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        getSupportActionBar().setTitle("Now Playing");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        btnPrev = findViewById(R.id.prevSongBtn);
        btnNext = findViewById(R.id.nextSongBtn);
        btnPlay = findViewById(R.id.playBtn);
        btnFastForward = findViewById(R.id.fastForwardBtn);
        btnFastRewind = findViewById(R.id.fastRewindBtn);
        txtSongName = findViewById(R.id.txtSongName);
        txtSongStart = findViewById(R.id.txtStartSong);
        txtSongStop = findViewById(R.id.txtStopSong);
        seekMusicBar = findViewById(R.id.seekBar);
        visualizer = findViewById(R.id.blast);
        imageView = findViewById(R.id.songImage);

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        Intent i = getIntent();
        Bundle bundle = i.getExtras();

        mySongs = (ArrayList) bundle.getParcelableArrayList("songs");
        songName = i.getStringExtra("songname");
        position = bundle.getInt("pos", 0);
        txtSongName.setSelected(true);
        Uri uri = Uri.parse(mySongs.get(position).toString());
        songName = mySongs.get(position).getName();
        txtSongName.setText(songName);

        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();

        updateSeekBar = new Thread() {
            @Override
            public void run() {
                int totalDuration = mediaPlayer.getDuration();
                int currentposition = 0;

                while (currentposition < totalDuration) {
                    try {
                        sleep(500);
                        currentposition = mediaPlayer.getCurrentPosition();
                        seekMusicBar.setProgress(currentposition);
                    } catch (InterruptedException | IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        seekMusicBar.setMax(mediaPlayer.getDuration());
        updateSeekBar.start();
        seekMusicBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.design_default_color_on_primary), PorterDuff.Mode.MULTIPLY);
        seekMusicBar.getThumb().setColorFilter(getResources().getColor(R.color.design_default_color_on_primary), PorterDuff.Mode.SRC_IN);

        seekMusicBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());

            }
        });

        endTime = createTime(mediaPlayer.getDuration());
        txtSongStop.setText(endTime);

        final Handler handler = new Handler();
        final int delay = 1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                endTime = createTime(mediaPlayer.getCurrentPosition());
                txtSongStart.setText(endTime);
                handler.postDelayed(this, delay);
            }
        }, delay);


        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()) {
                    btnPlay.setBackgroundResource(R.drawable.ic_play);
                    mediaPlayer.pause();
                } else {
                    btnPlay.setBackgroundResource(R.drawable.ic_pause);
                    mediaPlayer.start();
                }
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                btnNext.performClick();
            }
        });

        audiosessionId = mediaPlayer.getAudioSessionId();

        if (audiosessionId != -1) {
            visualizer.setAudioSessionId(audiosessionId);
        }

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();

                position = ((position + 1) % mySongs.size());
                startSongFromEvents();
            }
        });

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                mediaPlayer.release();

                position = ((position - 1) < 0) ? (mySongs.size() - 1) : (position - 1);
                startSongFromEvents();
            }
        });

        btnFastForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 10000);
                }
            }
        });

        btnFastRewind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 10000);
                }
            }
        });
    }

    public void startAnimation(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(imageView, "rotation", 0f, 360f);
        animator.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();
    }

    public String createTime(int duration) {
        String time = "";
        int min = duration / 1000 / 60;
        int sec = duration / 1000 % 60;

        time += min + ":";

        if (sec < 10) {
            time += "0";
        }
        time += sec;

        return time;
    }

    public void startSongFromEvents(){
        Uri u = Uri.parse(mySongs.get(position).toString());
        mediaPlayer = MediaPlayer.create(getApplicationContext(), u);
        endTime = createTime(mediaPlayer.getDuration());
        txtSongStop.setText(endTime);

        songName = mySongs.get(position).getName();
        txtSongName.setText(songName);

        btnPlay.setBackgroundResource(R.drawable.ic_pause);
        startAnimation(imageView);

        audiosessionId = mediaPlayer.getAudioSessionId();
        if (audiosessionId != -1) {
            visualizer.setAudioSessionId(audiosessionId);
        }

        mediaPlayer.start();
        mediaPlayer.seekTo(0);
    }
}