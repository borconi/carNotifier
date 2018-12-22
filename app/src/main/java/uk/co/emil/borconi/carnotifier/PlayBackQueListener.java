package uk.co.emil.borconi.carnotifier;

import android.speech.SpeechRecognizer;

import java.util.ArrayList;

public interface PlayBackQueListener {
    public void update(Long msg);
    public void listen(Long msg, ArrayList data, SpeechRecognizer sr,boolean retry);

}
