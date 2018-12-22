package uk.co.emil.borconi.carnotifier;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;


public class TTSCallBack extends UtteranceProgressListener implements TextToSpeech.OnInitListener {
    CarNotificationSoundPlayer carNotificationSoundPlayer;
    PlayBackQue playBackQueListener;
    public TTSCallBack(CarNotificationSoundPlayer carNotificationSoundPlayer, PlayBackQue playBackQueListener) {
        this.carNotificationSoundPlayer=carNotificationSoundPlayer;
        this.playBackQueListener=playBackQueListener;
    }

    @Override
    public void onStart(String s) {

    }

    @Override
    public void onDone(final String s) {
        Log.d("CarNotifier","Completed TTS");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        playBackQueListener.update(Long.valueOf(s));
        if (carNotificationSoundPlayer.carAudioManager!=null)
            carNotificationSoundPlayer.carAudioManager.abandonAudioFocus(null, carNotificationSoundPlayer.audioAttributes);
        else
            carNotificationSoundPlayer.manager.abandonAudioFocus(null);

        }


    @Override
    public void onError(String s) {
        Log.d("CarNotifier","TTS Error");
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS)
        {
            playBackQueListener.tts.setOnUtteranceProgressListener(TTSCallBack.this);
            carNotificationSoundPlayer.isTTSReady=true;
        }
    }
}
