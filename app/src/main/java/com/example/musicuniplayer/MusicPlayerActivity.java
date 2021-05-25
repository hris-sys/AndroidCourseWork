package com.example.musicuniplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;
import com.gauravk.audiovisualizer.visualizer.WaveVisualizer;

import java.io.File;
import java.util.ArrayList;

public class MusicPlayerActivity extends AppCompatActivity {
    Button btnPlay, btnNextSong, btnPreviousSong, btnFastForward, btnFastRewind;
    TextView txtSongName, txtStart, txtStop;
    SeekBar seekBar;
    WaveVisualizer barVisualizer;

    String songName;
    public static final String EXTRA_NAME = "song_name";
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<File> mySongs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        btnPreviousSong = findViewById(R.id.prevSongBtn);
        btnNextSong = findViewById(R.id.nextSongBtn);
        btnPlay = findViewById(R.id.playBtn);
        btnFastForward = findViewById(R.id.fastForwardBtn);
        btnFastRewind = findViewById(R.id.fastRewindBtn);
        txtSongName = findViewById(R.id.txtSongName);
        txtStart = findViewById(R.id.txtStartSong);
        txtStop = findViewById(R.id.txtStopSong);
        seekBar = findViewById(R.id.seekBar);
        barVisualizer = findViewById(R.id.blast);

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        mySongs = (ArrayList) bundle.getParcelableArrayList("songs");
        String songName = intent.getStringExtra("songName");
        position = bundle.getInt("pos", 0);
        txtSongName.setSelected(true);
        Uri uri = Uri.parse(mySongs.get(position).toString());

        songName = mySongs.get(position).getName();
        txtSongName.setText(songName);

        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    btnPlay.setBackgroundResource(R.drawable.ic_play);
                    mediaPlayer.pause();
                }
                else {
                    btnPlay.setBackgroundResource(R.drawable.ic_pause);
                    mediaPlayer.start();
                }
            }
        });
    }
}