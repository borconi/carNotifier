package uk.co.emil.borconi.carnotifier;

import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;

public class VoiceListener implements RecognitionListener {
    private static final String TAG = "VoiceRecognition";
    Long messageID;
    PlayBackQueListener playBackQueListener;
    SpeechRecognizer sr;
    private boolean respSent=false;

    public VoiceListener(Long s, PlayBackQueListener p, SpeechRecognizer sr) {
        this.messageID=s;
        this.playBackQueListener=p;
        this.sr=sr;
    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {
        Log.d(TAG,"Ready for speech");
    }

    @Override
    public void onBeginningOfSpeech(){

    }

    @Override
    public void onRmsChanged(float v){
      //  Log.d(TAG,"rmschanged");
    }

    @Override
    public void onBufferReceived(byte[] bytes) {
        Log.d(TAG,"buffer received");
    }

    @Override
    public void onEndOfSpeech() {
        Log.d(TAG,"Speech end");
       // sr.stopListening();
      //  sr.destroy();
    }

    @Override
    public void onError(int i) {
        Log.d(TAG,"Speech error: " +i);
        if (i==SpeechRecognizer.ERROR_NO_MATCH && !respSent)
        {
            respSent=true;
            playBackQueListener.listen(messageID,null,sr,false);
        }
        else if (i==SpeechRecognizer.ERROR_SPEECH_TIMEOUT && !respSent)
        {
            respSent=true;
            playBackQueListener.listen(messageID,null,sr,false);
        }
        else if (i==SpeechRecognizer.ERROR_RECOGNIZER_BUSY && !respSent)
        {
            respSent=true;
            playBackQueListener.listen(messageID,null,sr,true);
        }

       // if ((System.currentTimeMillis()-speechBegin)>400)
         //   onEndOfSpeech();
    }

    @Override
    public void onResults(Bundle results) {
        Log.d(TAG, "result " + results);
        ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        /* for (int i = 0; i < data.size(); i++)
        {
            Log.d(TAG, "result " + data.get(i));
        }*/
        if (!respSent)
        {
            playBackQueListener.listen(messageID,data,sr,false);
            respSent=true;
        }

    }

    @Override
    public void onPartialResults(Bundle bundle) {
        Log.d(TAG,"Partial result");
    }

    @Override
    public void onEvent(int i, Bundle bundle) {
        Log.d(TAG,"event: " + bundle.toString());
    }
}
