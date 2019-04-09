package uk.co.emil.borconi.carnotifier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.car.CarNotConnectedException;
import android.support.car.media.CarAudioManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PlayBackQue implements PlayBackQueListener{
    private static final String TAG ="PlayBackQue" ;
    public SortedMapWithCallBack<Long,PlayBackQueMessage> messages;
    TextToSpeech tts;
    CarAudioManager carAudioManager;
    AudioAttributes audioAttributes;
    boolean isPlaying=false;
    public Context appContext;
    boolean phoneInUse;
    CarnotificationListener carnotificationListener;

    public PlayBackQue() {

    }

    public PlayBackQue(CarnotificationListener carnotificationListener) {
        this.carnotificationListener=carnotificationListener;
        messages= new TreeMapCalllback<>(this);
        appContext=carnotificationListener.getApplicationContext();
    }
    synchronized public void add(String text, String language, Action action)
    {
        Log.d("CarNotif", "Message added to playback que");
        messages.put(System.currentTimeMillis(),new PlayBackQueMessage(text,language,action,appContext));
        //playQuedMessages();
    }

    void playQuedMessages()
    {
        Log.d("CarNotif", "PlayQuedMessages: tts: " + tts + ", and isPlaying: " + isPlaying);
        if (tts!=null && !isPlaying)
            if (!messages.isEmpty()) //While we have messaged in the que we should play them, one by one.
            {
                isPlaying=true;
                Long curr = messages.firstKey();
                PlayBackQueMessage currMessage=messages.get(curr);
                readTTS(currMessage,curr);

            }
    }


    void onCall() {
        Log.d("TEST","On call");
        if (tts!=null && tts.isSpeaking())
            tts.stop();
        phoneInUse=true;
        isPlaying=false;
    }

    @Override
    public void update(final Long msg) {
        Log.d(TAG,"Update started.... " +msg);
    if (phoneInUse)
        return;

        final PlayBackQueMessage message = messages.get(msg);
        if (message.needsAnswer()) {
               // message.changeState(1);
                Runnable listen = new Runnable() {
                    @Override
                    public void run() {

                        SpeechRecognizer sr = SpeechRecognizer.createSpeechRecognizer(appContext);
                        sr.setRecognitionListener(new VoiceListener(msg,PlayBackQue.this,sr));
                        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5);
                        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS,5000);
                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, message.language+"-"+GenerateLanguageTag.getStringByLocal(appContext,R.string.countryCode,new Locale(message.language).toString()));
                        sr.startListening(intent);
                    }
                };
                new Handler(Looper.getMainLooper()).post(listen);

        }
        else
            messages.removenadupdate(msg);

    }

    @Override
    public void listen(Long msg, ArrayList response, SpeechRecognizer sr,boolean retry) {
        Log.d(TAG,"Listen started.... " +msg);
        sr.stopListening();
        sr.destroy();
        if (retry)
        {
            Log.d(TAG,"Listener service was busy, give it half a second and retry " +msg);
            update(msg);
        return;
        }


        if (phoneInUse)
            return;

        PlayBackQueMessage message = messages.get(msg);
        if (message==null)  //Prevent delayed callbacks from VoiceInputRecognition Listener.
            return;
        HashMap<String, String> answers = message.getAnswers();
        String undersand=null;
        if (answers==null)
        {
            if (response!=null)
                message.response=response.get(0).toString();
            else
                message.response="";
            Log.d(TAG,"Message is: " + message.response);
            message.changeState(1);
            message.wrongCount=0;
            readTTS(message,msg);

        }
        else {
            if (response!=null) {
                for (Map.Entry<?, ?> e : answers.entrySet()) {
                    for (int i = 0; i < response.size(); i++) {
                        if (e.getKey().toString().equalsIgnoreCase(response.get(i).toString().trim())) {
                            undersand = e.getValue().toString();
                            break;
                        }
                    }
                    if (undersand != null)
                        break;
                }
            }

            if (undersand == null) {
                message.wrongCount++;
                readTTS(message, msg);
            }
            else if (undersand.equalsIgnoreCase("no") || undersand.equalsIgnoreCase("cancel"))
            {
                Log.d(TAG,"Answer is no, removing notification from que");
                messages.removenadupdate(msg);
            }
            else if (undersand.equalsIgnoreCase("yes")) {
                message.changeState(1);
                message.wrongCount = 0;
                readTTS(message, msg);
            } else if (undersand.equalsIgnoreCase("change")) {
                message.wrongCount = 0;
                message.changeState(-1);
                readTTS(message, msg);
            } else if (undersand.equalsIgnoreCase("send")) {
                message.sendReply(appContext);
                message.changeState(1);
                readTTS(message,msg);
                //messages.removenadupdate(msg);
            }
        }
    }

    void readTTS(final PlayBackQueMessage currMessage, final Long curr)
    {
        Log.d(TAG,"ReadTTS called.... " + curr);
        if (phoneInUse)
            return;

           /* if (currMessage.wrongCount>=3)
            {
                messages.removenadupdate(curr);
                return;
            }*/


        int ret = 0;
        try {
            if (carAudioManager != null)
            ret = carAudioManager.requestAudioFocus(null, audioAttributes, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE);
            else
            {
                Log.d("PlayBackQue","CarAudio not available using local");
                AudioManager audioManager=(AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
                if (audioManager!=null)
                ret=audioManager.requestAudioFocus(null,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            }
        } catch (CarNotConnectedException e) {
            Log.e("carNotifier","not connected to the car");
        }
        if (ret == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {

            int result = tts.setLanguage(new Locale(currMessage.language));
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("error", "This Language is not supported");

            }
            Runnable speak=new Runnable() {
                @Override
                public void run() {
                    if (audioAttributes!=null)
                        tts.setAudioAttributes(audioAttributes);
                    tts.speak(currMessage.getMessage(), TextToSpeech.QUEUE_ADD,null,String.valueOf(curr));
                }
            };
            new Handler(Looper.getMainLooper()).post(speak);

//                    tts.speak(GenerateLanguageTag.languageMap.get(currMessage.language).getReplyQuestion(), TextToSpeech.QUEUE_ADD,params,String.valueOf(curr));


        }
        else {
            Log.w(TAG, "Failed to obtain audio focus, playing anyway.");
        }
    }
}
