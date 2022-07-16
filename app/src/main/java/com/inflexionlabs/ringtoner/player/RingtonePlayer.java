package com.inflexionlabs.ringtoner.player;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RingtonePlayer {

    private MediaPlayer mediaPlayer;
    private OnPrepareListener onPrepareListener;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public RingtonePlayer() {
        this.mediaPlayer = new MediaPlayer();
    }

    public void playWithDataSource(String url, OnPrepareListener onPrepareListener){

        this.onPrepareListener = onPrepareListener;
        Handler handler = new Handler(Looper.getMainLooper());

        this.onPrepareListener.isPreparing(true);
        this.onPrepareListener.completed(false);

        executorService.execute(()->{
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(url);
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.prepareAsync();
            } catch (Exception e) {
                e.printStackTrace();
//                Log.d("AD", "playWithDataSource: ");
            }

            mediaPlayer.setOnCompletionListener(mediaPlayer1 -> {
                mediaPlayer1.reset();
                this.onPrepareListener.completed(true);
            });
        });

        handler.post(() -> mediaPlayer.setOnPreparedListener(mediaPlayer1 -> {
            mediaPlayer1.start();
            this.onPrepareListener.isPreparing(false);
        }));
    }


    public void stopPlay(){
        if (mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
        if (this.onPrepareListener != null) onPrepareListener.onNext();
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public interface OnPrepareListener{
        void isPreparing(boolean isPreparing);
        void completed(boolean completed);
        void onNext();
    }

    public void destroy(){
        mediaPlayer.release();
        mediaPlayer = null;
    }
}
