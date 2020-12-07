package com.codewithhamad.musicplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.Random;
import pl.droidsonroids.gif.GifImageView;
import static com.codewithhamad.musicplayer.ApplicationClass.CHANNEL_ID_2;

public class PlayerActivity extends AppCompatActivity implements ActionPlaying, ServiceConnection {

    GifImageView gifImageView, secondGif;
    TextView songName, artistName, durationPlayed, durationTotal;
    ImageView coverArt, staticArt, nextBtn, prevBtn, shuffleBtn, repeatBtn, pausedImg, pausedBack, nowPlayingImg;
    FloatingActionButton playPauseBtn;
    SeekBar seekBar;
    int position=-1;

    static Uri uri;
//    static MediaPlayer mediaPlayer;
    static ArrayList<MusicFiles> listSongs=new ArrayList<>();

    private Handler handler= new Handler();
    private Thread playThread, nextThread, prevThread;
    MusicService musicService;
    MediaSessionCompat mediaSessionCompat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        mediaSessionCompat= new MediaSessionCompat(getBaseContext(), "My Audio");
        initViews();
        getIntentMethod();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(musicService != null && b){
                    musicService.seekTo(i*1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(musicService != null){
                    int mCurrentPosition= musicService.getCurrentPosition()/1000;
                    seekBar.setProgress(mCurrentPosition);
                    durationPlayed.setText(formattedTime(mCurrentPosition));
                }
                handler.postDelayed(this, 1000);
            }
        });

        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MainActivity.shuffleBoolean){
                    MainActivity.shuffleBoolean= false;
                    shuffleBtn.setImageResource(R.drawable.ic_shuffle_off);
                }
                else{
                    MainActivity.shuffleBoolean= true;
                    shuffleBtn.setImageResource(R.drawable.ic_shuffle_on);
                }
            }
        });

        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MainActivity.repeatBoolean){
                    MainActivity.repeatBoolean= false;
                    repeatBtn.setImageResource(R.drawable.ic_repeat_off);
                }
                else{
                    MainActivity.repeatBoolean= true;
                    repeatBtn.setImageResource(R.drawable.ic_repeat_on);
                }
            }
        });
    }

    @Override
    protected void onPostResume() {
        Intent intent= new Intent(this, MusicService.class);
        bindService(intent, this, BIND_AUTO_CREATE);

        playThreadbtn();
        nextThreadbtn();
        prevThreadbtn();
        super.onPostResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);
    }

    private void prevThreadbtn() {
        prevThread= new Thread(new Runnable() {
            @Override
            public void run() {
                prevBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        prevBtnClicked();
                    }
                });
            }
        });
        prevThread.start();
    }

    public void prevBtnClicked() {
        if(musicService.isPlaying()){
            musicService.stop();
            musicService.release();

            if(MainActivity.shuffleBoolean && !MainActivity.repeatBoolean){
                position= getRandom(listSongs.size() - 1);
            }
            else if(!MainActivity.shuffleBoolean && !MainActivity.repeatBoolean){
                position= ((position - 1) < 0 ? (listSongs.size()-1) : (position -1));
            }

            uri= Uri.parse(listSongs.get(position).getPath());
            musicService.createMediaPlater(position);
            metaData(uri);

            songName.setText(listSongs.get(position).getTitle());
            artistName.setText(listSongs.get(position).getArtist());

            seekBar.setMax(musicService.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService != null){
                        int mCurrentPosition= musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            musicService.onCompleted();
            showNotification(R.drawable.ic_pause);
            playPauseBtn.setBackgroundResource(R.drawable.ic_pause);
            if(coverArt.getVisibility() == View.GONE){
                gifImageView.setVisibility(View.VISIBLE);
                staticArt.setVisibility(View.GONE);

                secondGif.setVisibility(View.VISIBLE);
                nowPlayingImg.setVisibility(View.VISIBLE);
                pausedBack.setVisibility(View.GONE);
                pausedImg.setVisibility(View.GONE);
            }
            musicService.start();
        }
        else {
            musicService.stop();
            musicService.release();


            if(MainActivity.shuffleBoolean && !MainActivity.repeatBoolean){
                position= getRandom(listSongs.size() - 1);
            }
            else if(!MainActivity.shuffleBoolean && !MainActivity.repeatBoolean){
                position= ((position - 1) < 0 ? (listSongs.size()-1) : (position -1));
            }

            uri = Uri.parse(listSongs.get(position).getPath());
            musicService.createMediaPlater(position);
            metaData(uri);

            songName.setText(listSongs.get(position).getTitle());
            artistName.setText(listSongs.get(position).getArtist());

            seekBar.setMax(musicService.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            musicService.onCompleted();
            showNotification(R.drawable.ic_play);
            playPauseBtn.setBackgroundResource(R.drawable.ic_play);

            if (coverArt.getVisibility() == View.GONE) {
                gifImageView.setVisibility(View.GONE);
                staticArt.setVisibility(View.VISIBLE);

                secondGif.setVisibility(View.GONE);
                nowPlayingImg.setVisibility(View.GONE);
                pausedBack.setVisibility(View.VISIBLE);
                pausedImg.setVisibility(View.VISIBLE);
            }
        }
    }

    private void nextThreadbtn() {
        nextThread= new Thread(new Runnable() {
            @Override
            public void run() {
                nextBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        nextBtnClicked("none");
                    }
                });
            }
        });
        nextThread.start();
    }

    public void nextBtnClicked(String called) {
        if(musicService.isPlaying()){
            musicService.stop();
            musicService.release();

            if(MainActivity.shuffleBoolean && !MainActivity.repeatBoolean){
                position= getRandom(listSongs.size() - 1);
            }
            else if(!MainActivity.shuffleBoolean && !MainActivity.repeatBoolean){
                position= ((position + 1) % listSongs.size());
            }

            // else if repaeat button is on:  position will be same as it was...
            uri= Uri.parse(listSongs.get(position).getPath());
            musicService.createMediaPlater(position);
            metaData(uri);

            songName.setText(listSongs.get(position).getTitle());
            artistName.setText(listSongs.get(position).getArtist());

            seekBar.setMax(musicService.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService != null){
                        int mCurrentPosition= musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            musicService.onCompleted();
            showNotification(R.drawable.ic_pause);
            playPauseBtn.setBackgroundResource(R.drawable.ic_pause);

            if(coverArt.getVisibility() == View.GONE){
                gifImageView.setVisibility(View.VISIBLE);
                staticArt.setVisibility(View.GONE);

                secondGif.setVisibility(View.VISIBLE);
                nowPlayingImg.setVisibility(View.VISIBLE);
                pausedBack.setVisibility(View.GONE);
                pausedImg.setVisibility(View.GONE);
            }
            musicService.start();
        }
        else {
            musicService.stop();
            musicService.release();

            if(MainActivity.shuffleBoolean && !MainActivity.repeatBoolean){
                position= getRandom(listSongs.size() - 1);
            }
            else if(!MainActivity.shuffleBoolean && !MainActivity.repeatBoolean){
                position= ((position + 1) % listSongs.size());
            }

            uri = Uri.parse(listSongs.get(position).getPath());
            musicService.createMediaPlater(position);
            metaData(uri);

            songName.setText(listSongs.get(position).getTitle());
            artistName.setText(listSongs.get(position).getArtist());

            seekBar.setMax(musicService.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            musicService.onCompleted();
            showNotification(R.drawable.ic_play);
            playPauseBtn.setBackgroundResource(R.drawable.ic_play);

            if (coverArt.getVisibility() == View.GONE) {
                gifImageView.setVisibility(View.GONE);
                staticArt.setVisibility(View.VISIBLE);

                secondGif.setVisibility(View.GONE);
                nowPlayingImg.setVisibility(View.GONE);
                pausedBack.setVisibility(View.VISIBLE);
                pausedImg.setVisibility(View.VISIBLE);
            }
        }
        if(called.equals("onCompletion")){
            Log.d("called", "nextBtnClicked: called.equals(onCompletion)");
            if(coverArt.getVisibility() == View.GONE){
                gifImageView.setVisibility(View.VISIBLE);
                staticArt.setVisibility(View.GONE);

                secondGif.setVisibility(View.VISIBLE);
                nowPlayingImg.setVisibility(View.VISIBLE);
                pausedBack.setVisibility(View.GONE);
                pausedImg.setVisibility(View.GONE);
            }
        }
    }

    private int getRandom(int i) {

        Random random= new Random();
        return random.nextInt(i +1);
    }

    private void playThreadbtn() {
        playThread= new Thread(new Runnable() {
            @Override
            public void run() {
                playPauseBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        playPauseBtnClicked();
                    }
                });
            }
        });
        playThread.start();
    }

    public void playPauseBtnClicked() {
        Log.d("tag", "playPauseBtnClicked: started");
        if(musicService.isPlaying()){
            playPauseBtn.setImageResource(R.drawable.ic_play);
            showNotification(R.drawable.ic_play);
            musicService.pause();
//            MusicAdapter.MyViewHolder.file_name.setTextColor(Color.WHITE);

            // for showing static_image on pausing the audio
            if(gifImageView.getVisibility() == View.VISIBLE && staticArt.getVisibility() == View.GONE){
                gifImageView.setVisibility(View.GONE);
                staticArt.setVisibility(View.VISIBLE);

                secondGif.setVisibility(View.GONE);
                nowPlayingImg.setVisibility(View.GONE);
                pausedBack.setVisibility(View.VISIBLE);
                pausedImg.setVisibility(View.VISIBLE);
            }

            if (coverArt.getVisibility() == View.VISIBLE){
                secondGif.setVisibility(View.GONE);
                nowPlayingImg.setVisibility(View.GONE);
                pausedBack.setVisibility(View.VISIBLE);
                pausedImg.setVisibility(View.VISIBLE);
            }

            seekBar.setMax(musicService.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService != null){
                        int mCurrentPosition= musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
        }
        else{
            showNotification(R.drawable.ic_pause);
            playPauseBtn.setImageResource(R.drawable.ic_pause);
            musicService.start();
            seekBar.setMax(musicService.getDuration() / 1000);
//            MusicAdapter.MyViewHolder.file_name.setTextColor(Color.GREEN);

            // for showing gif on resuming the video
            if(gifImageView.getVisibility() == View.GONE && staticArt.getVisibility() == View.VISIBLE){
                gifImageView.setVisibility(View.VISIBLE);
                staticArt.setVisibility(View.GONE);

                secondGif.setVisibility(View.VISIBLE);
                nowPlayingImg.setVisibility(View.VISIBLE);
                pausedBack.setVisibility(View.GONE);
                pausedImg.setVisibility(View.GONE);
            }

            if(coverArt.getVisibility() == View.VISIBLE){
                secondGif.setVisibility(View.VISIBLE);
                nowPlayingImg.setVisibility(View.VISIBLE);
                pausedBack.setVisibility(View.GONE);
                pausedImg.setVisibility(View.GONE);
            }


            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(musicService != null){
                        int mCurrentPosition= musicService.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });

        }
    }

    private String formattedTime(int mCurrentPosition) {

        String totalOut="";
        String totalNew="";
        String minutes= String.valueOf(mCurrentPosition/60);
        String seconds= String.valueOf(mCurrentPosition%60);

        totalOut= minutes + ":" + seconds;
        totalNew= minutes + ":" + "0" + seconds;

        if(seconds.length() == 1){
            return totalNew;
        }
        else{
            return totalOut;
        }
    }

    private void getIntentMethod() {
        position= getIntent().getIntExtra("position", -1);
        String sender= getIntent().getStringExtra("sender");
        if(sender != null && sender.equals("albumDetails")){
            listSongs= AlbumDetailsAdapter.albumFiles;
        }
        else{
            listSongs= MusicAdapter.mFiles;
        }
        if(listSongs != null){
            playPauseBtn.setImageResource(R.drawable.ic_pause);
            uri= Uri.parse(listSongs.get(position).getPath());
        }
        showNotification(R.drawable.ic_pause);
        Intent intent= new Intent(this, MusicService.class);
        intent.putExtra("servicePosition", position);
        startService(intent);

    }

    private void initViews() {
        gifImageView=findViewById(R.id.gifImageView);
        secondGif= findViewById(R.id.secondGif);

        songName=findViewById(R.id.song_name);
        artistName=findViewById(R.id.artist_name);
        durationPlayed=findViewById(R.id.durationPlayed);
        durationTotal=findViewById(R.id.durationTotal);

        coverArt=findViewById(R.id.cover_art);
        staticArt=findViewById(R.id.static_art);
        nextBtn=findViewById(R.id.id_skip_next);
        prevBtn=findViewById(R.id.id_skip_previous);
//        backBtn=findViewById(R.id.back_btn);
        shuffleBtn=findViewById(R.id.id_shuffle);
        repeatBtn=findViewById(R.id.id_repeat);
        nowPlayingImg= findViewById(R.id.nowPlayingImg);
        pausedImg= findViewById(R.id.pausedImg);
        pausedBack= findViewById(R.id.pausedBack);

        playPauseBtn=findViewById(R.id.play_pause);
        seekBar=findViewById(R.id.seekBar);

    }

    private void metaData(Uri uri){

        MediaMetadataRetriever retriever= new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        int duration_Total= Integer.parseInt(listSongs.get(position).getDuration()) / 1000;
        durationTotal.setText(formattedTime(duration_Total));
        byte[] art= retriever.getEmbeddedPicture();

        if(art != null){
            coverArt.setVisibility(View.VISIBLE);
            gifImageView.setVisibility(View.GONE);
            staticArt.setVisibility(View.GONE);

            Glide.with(this)
                    .asBitmap()
                    .load(art)
                    .into(coverArt);
        }
        else{
            coverArt.setVisibility(View.GONE);
            staticArt.setVisibility(View.GONE);
            gifImageView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        MusicService.MyBinder myBinder=(MusicService.MyBinder) iBinder;
        musicService= myBinder.getService();
        musicService.setCallBack(this);
        Toast.makeText(this, "Connected" + musicService, Toast.LENGTH_SHORT).show();
        seekBar.setMax(musicService.getDuration()/1000);
        metaData(uri);
        songName.setText(listSongs.get(position).getTitle());
        artistName.setText(listSongs.get(position).getArtist());
        musicService.onCompleted();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        musicService= null;
    }

    void showNotification(int playPauseBtn){
        Intent intent= new Intent(this, PlayerActivity.class);
        PendingIntent contentIntent= PendingIntent.getActivity(this, 0, intent, 0);

        Intent prevIntent= new Intent(this, NotificationReceiver.class).setAction(ApplicationClass.ACTION_PREVIOUS);
        PendingIntent prevPending= PendingIntent.getBroadcast(this, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pauseIntent= new Intent(this, NotificationReceiver.class).setAction(ApplicationClass.ACTION_PLAY);
        PendingIntent pausePending= PendingIntent.getBroadcast(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent= new Intent(this, NotificationReceiver.class).setAction(ApplicationClass.ACTION_NEXT);
        PendingIntent nextPending= PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        byte[] picture= null;
        picture= getAlbumArt(listSongs.get(position).getPath());
        Bitmap thumb= null;
        if(picture != null){
            thumb= BitmapFactory.decodeByteArray(picture, 0, picture.length);
        }
        else{
            thumb= BitmapFactory.decodeResource(getResources(), R.drawable.note);
        }
        Notification notification= new NotificationCompat.Builder(this, CHANNEL_ID_2)
                .setSmallIcon(playPauseBtn)
                .setLargeIcon(thumb)
                .setContentTitle(listSongs.get(position).getTitle())
                .setContentText(listSongs.get(position).getArtist())
                .addAction(R.drawable.ic_skip_previous, "Previous", prevPending)
                .addAction(playPauseBtn, "Pause", pausePending)
                .addAction(R.drawable.ic_skip_next, "Next", nextPending)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .build();

        NotificationManager notificationManager= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);

    }
    private byte[] getAlbumArt(String uri){

        MediaMetadataRetriever retriever= new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art= retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }
}
