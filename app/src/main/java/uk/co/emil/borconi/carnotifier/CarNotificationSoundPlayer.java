package uk.co.emil.borconi.carnotifier;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.support.annotation.RawRes;
import android.support.car.Car;
import android.support.car.CarConnectionCallback;
import android.support.car.CarNotConnectedException;
import android.support.car.media.CarAudioManager;
import android.util.Log;

import com.google.android.apps.auto.sdk.service.CarFirstPartyManager;

import java.util.Locale;
import java.util.Set;

public class CarNotificationSoundPlayer {
    private final static String TAG = "CarNotifSoundPlayer";

    final static int PLAYBACK_START_DELAY_MS = 300;
    private final Car car;

    private @RawRes
    int mSoundResource;
    Context mContext;
    private  Handler mHandler;
    CarAudioManager carAudioManager;
    private boolean isCarReady=false;
    AudioAttributes audioAttributes;
    static boolean isTTSReady;
    AudioManager manager;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    PlayBackQue playBackQue;

    //boolean isTTSReady;
    //private PlayBackQue playbackQue;


    public CarNotificationSoundPlayer(Context context,PlayBackQue playback) {
        this.mContext = context;
        this.mHandler = new Handler(context.getMainLooper());
        car = Car.createCar(mContext, carReady);
        manager=(AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
        car.connect();
        playBackQue=playback;
        playback.tts = new TextToSpeech(mContext, new TTSCallBack(this, playback),"com.google.android.tts");
    }

    public void play(@RawRes int soundResource) {
        mSoundResource=soundResource;
        Log.d(TAG, "Starting");
        if (!isCarReady)  //if car is not ready and connected intrerupt;
        {
            Log.e("carNotif","Car is not ready");
            return;
        }

            if(manager.getMode()==AudioManager.MODE_IN_CALL)
                return; //Don't play sound during call's!
        try {
        final AssetFileDescriptor fd = mContext.getResources().openRawResourceFd(mSoundResource);

        mediaPlayer.setAudioAttributes(audioAttributes);
        mediaPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
        //fd.close();
        mediaPlayer.prepare();

        int ret = carAudioManager.requestAudioFocus(null, audioAttributes,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
        if (ret == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    Log.d(TAG, "Playback completed.");
                    try {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        carAudioManager.abandonAudioFocus(null, audioAttributes);

                    } catch (Exception e) {
                        Log.w(TAG, "Error finalizing playback", e);
                    }
                }
            });
        } else {
            Log.w(TAG, "Failed to obtain audio focus, playing anyway.");
        }

        // Allow some time for the ducking to take effect.
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mediaPlayer.start();
            }
        }, PLAYBACK_START_DELAY_MS);
    } catch (Exception e) {
        Log.w(TAG, "Error initiating playback: "+ e.getMessage());
        //car.disconnect();
    }

    }

    /*public void playTTS(final String language, final String text, Action action) {
        if (!isCarReady || !isTTSReady)  //if car or TTS Engine is not ready and connected intrerupt;
            return;

        playbackQue.add(text, language, true, action);
        playbackQue.playQuedMessages();
    }*/





    private CarConnectionCallback carReady = new CarConnectionCallback() {

        @Override
        public void onConnected(Car car) {
            isCarReady=true;
            try {
                Log.d("carNotif","Car Connected");
              //  CarFirstPartyManager mgr = (CarFirstPartyManager) car.getCarManager(CarFirstPartyManager.SERVICE_NAME);
                //mgr.
                carAudioManager = car.getCarManager(CarAudioManager.class);
                audioAttributes = carAudioManager.getAudioAttributesForCarUsage(CarAudioManager.CAR_AUDIO_USAGE_NOTIFICATION);
                playBackQue.audioAttributes=audioAttributes;
                playBackQue.carAudioManager=carAudioManager;
              //  playbackQue=new PlayBackQue(carAudioManager,audioAttributes);
            } catch (CarNotConnectedException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onDisconnected(Car car) {
            isCarReady=false;
        }
    };







}
