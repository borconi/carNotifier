package uk.co.emil.borconi.carnotifier;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;

import android.app.UiModeManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.speech.tts.TextToSpeech;
import android.support.car.Car;
import android.support.car.CarConnectionCallback;
import android.support.car.CarNotConnectedException;
import android.support.car.media.CarAudioManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.apps.auto.sdk.notification.CarNotificationExtender;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static android.app.Notification.EXTRA_LARGE_ICON;


public class CarnotificationListener extends NotificationListenerService  {
    Context context;
    private static Set<String> set=null;
    NotificationManager mNotifyMgr;
    private PackageManager packageManager;
    private static List setList;
    private ScheduledExecutorService pollexecutor= Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> countdown=null;
    CarNotificationSoundPlayer soundPlayer;
    PlayBackQue playbackQue;
    private String mPreviousNotificationKey="";
    private boolean playSound,dismissOriginal,shouldRead;
    private int clearTimeout;
    static HashMap<String,Long> pastNot=null;
    private Car car;


    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onCreate() {

        super.onCreate();
        context = getApplicationContext();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getStringSet("List", null)!=null)
            set = prefs.getStringSet("List", null);
        if (set==null)
            return;
        playSound=prefs.getBoolean("playsound",true);
        dismissOriginal=prefs.getBoolean("dismissorignal",true);
        clearTimeout=Integer.parseInt(prefs.getString("clearown","15"));
        shouldRead=prefs.getBoolean("autoread",true);
        setList = Arrays.asList(set.toArray(new String[0]));
        mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        packageManager= getApplicationContext().getPackageManager();
        IntentFilter filter = new IntentFilter ();
        filter.addAction ("android.app.action.ENTER_CAR_MODE");
        filter.addAction ("android.app.action.EXIT_CAR_MODE");
        filter.addAction ("android.intent.action.NEW_OUTGOING_CALL");
        filter.addAction ("android.intent.action.PHONE_STATE");
        registerReceiver(new carEventListener(),filter);


    }
    @Override
    public void onDestroy(){
        Log.d("carNotifier","Service destroyed");
    }

    static public void updateSet(Set<String> set)
    {
        setList = Arrays.asList(set.toArray(new String[0]));
    }
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn){

    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        Log.e("test","onListenerConnected");
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        Log.e("test","onListenerDisconnected");
    }

    @Override
    public void onNotificationPosted(final StatusBarNotification sbn) {

        if (((UiModeManager) getSystemService(Context.UI_MODE_SERVICE)).getCurrentModeType() != Configuration.UI_MODE_TYPE_CAR)
            return;

        if (pastNot==null) {         //Ups we are in car mode but the
            carConnect();
        }
        if (pastNot!=null && pastNot.containsKey(sbn.getKey()+sbn.getNotification().when))
            return;

        else
            pastNot.put(sbn.getKey()+sbn.getNotification().when,sbn.getPostTime());

        final String pack = sbn.getPackageName();
        if (pack.equalsIgnoreCase("com.google.android.gm") && sbn.getId() == 0) //Ignore gmail duplicate notification
            return;


        final Action xxx = NotificationHelper.getQuickReplyAction(sbn.getNotification(), pack);


        final Bundle bundle = sbn.getNotification().extras;

        if (set == null)
            return;

        if (!set.contains(pack))
            return;


        new Thread(new Runnable() {


            @Override
            public void run() {
        String title = "";
        try {
            title = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(pack, PackageManager.GET_META_DATA));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Log.d("NotReceiver", bundle.toString());
        final String text = bundle.getCharSequence("android.title", "").toString() + ": " + (bundle.containsKey("android.bigText") ? bundle.getCharSequence("android.bigText", "").toString() : bundle.getCharSequence("android.text", "").toString());

        if (text.toLowerCase().contains("syncing mail..."))
            return;
        if (bundle.getCharSequence("android.title", "").toString().equalsIgnoreCase("Chat heads active")) //Ignore chat had active in Facebook
            return;
        if (bundle.getCharSequence("android.title", "").toString().matches("\\d* new messages")) //Ignore "X new messages in hangouts"
            return;

        Bitmap bmp = (Bitmap) bundle.get(EXTRA_LARGE_ICON);
        if (bmp==null)
            bmp=BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_notif);

        //PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_ONE_SHOT);
        //load all languages:


                if (shouldRead) {
                    Log.d("CarNotif","Should proceed with TTS readout");
                  /*  List<LanguageProfile> languageProfiles = null;
                    try {
                        if (playSound)
                            Thread.sleep(500+PLAYBACK_START_DELAY_MS);
                        Log.d("CarNotif","After notification sound playback");
                        languageProfiles = new LanguageProfileReader().readAllBuiltIn();
                        //build language detector:
                        LanguageDetector languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
                                .withProfiles(languageProfiles)
                                .build();
                        //create a text object factory
                        TextObjectFactory textObjectFactory = CommonTextObjectFactories.forDetectingOnLargeText();
                        //query:
                        TextObject textObject = textObjectFactory.forText(text);
                        final com.google.common.base.Optional<LdLocale> lang = languageDetector.detect(textObject);
                        if (lang.isPresent()) {
                            Log.d("CarNotif", "Detected language: " + lang.get().toString());
                            playbackQue.add(text, lang.get().getLanguage(), xxx);
                        } else {
                            Log.d("CarNotif", "Language not detected, setting to default and trying Google detection");
                            String detected = Locale.getDefault().getLanguage();
                            try {
                                detected = Translator.googleTranslateApi(text.substring(0, Math.min(text.length(), 100)), "auto", "en");
                            } catch (Exception e) {
                                Log.e("CarNotif", "Google detection error: " +e.getMessage());
                            } finally {
                                playbackQue.add(text, detected, xxx);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e("Notifier", "Error detecting language.");*/
                       /* String detected = Locale.getDefault().getLanguage();
                        try {
                            detected = new Translator().bingTranslate(text.substring(0, Math.min(text.length(), 100)));
                            Log.d("carNotiff","Detected language: " + detected);
                        } catch (Exception e1) {
                            Log.e("carNottif","Problem detecting language: " + e1.getMessage());
                        } finally {
                            if (playSound)
                                soundPlayer.play(R.raw.note);
                            playbackQue.add(text, detected, xxx);
                        }
                    /*} catch (InterruptedException e) {
                        e.printStackTrace();
                    }*/
                    new Translator().bingTranslate(CarnotificationListener.this,text,xxx);
                }
                else
                if (playSound)
                    soundPlayer.play(R.raw.note);



                CarNotificationExtender paramString2 = new CarNotificationExtender.Builder()
                        .setTitle(title)
                        .setSubtitle(text)
                        .setShouldShowAsHeadsUp(true)
                        .setActionIconResId(R.drawable.icon_notif)
                        .setBackgroundColor(Color.WHITE)
                        .setNightBackgroundColor(Color.DKGRAY)
                        .setThumbnail(bmp)
                       // .setActionIntent(intent2)
                        .build();


                NotificationCompat.Builder mynot = new NotificationCompat.Builder(context)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setLargeIcon(bmp)
                        .setSmallIcon(R.drawable.icon_notif)
                        .setColor(Color.GRAY)
                        .setOngoing(false)

                        .extend(paramString2);

                if (dismissOriginal)
                    cancelNotification(sbn.getKey());



                mNotifyMgr.notify(setList.indexOf(pack), mynot.build());

                if (countdown != null)
                    countdown.cancel(true);

        countdown = pollexecutor.schedule(new Runnable() {
            @Override
            public void run() {
               // Log.d("TAG","running delayed clear");
                mNotifyMgr.cancelAll();
            }}, clearTimeout, TimeUnit.SECONDS);


            }
        }).start();

    }


    public void onResponse(String detected,String text,Action xxx) {
        if (playSound && soundPlayer!=null)
            soundPlayer.play(R.raw.note);
        if (playbackQue!=null)
            playbackQue.add(text, detected, xxx);
    }


    public class carEventListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction()!=null && intent.getAction().equalsIgnoreCase("android.app.action.ENTER_CAR_MODE"))
                carConnect();
            else if (intent.getAction().equalsIgnoreCase("android.app.action.EXIT_CAR_MODE"))
            {
                Log.d("CarNotif","Exit car mode");
                if (car!=null){
                car.disconnect();
                car=null;
                }


                soundPlayer=null;
                pastNot.clear();
                pastNot=null;
                if (playbackQue!=null) {
                    playbackQue.tts.stop();
                    playbackQue.tts.shutdown();
                    playbackQue.tts = null;
                    playbackQue.messages.clear();
                    playbackQue = null;
                }
            }
            else if (intent.getAction().equalsIgnoreCase("android.intent.action.NEW_OUTGOING_CALL"))
            {
                if (playbackQue!=null)
                    playbackQue.onCall();
            }
            else if(intent.getAction().equals("android.intent.action.ACTION_PHONE_STATE_CHANGED") || intent.getAction().equals("android.intent.action.PHONE_STATE")){

                String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

                if(state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)){
                    if (playbackQue!=null)
                        playbackQue.onCall();
                }

                else if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)){
                    if (playbackQue!=null)
                        playbackQue.onCall();
                }
                else if(state.equals(TelephonyManager.EXTRA_STATE_IDLE)){
                    if (playbackQue!=null && playbackQue.tts!=null)
                    {
                        Log.d("TEST","Off call");
                        playbackQue.phoneInUse=false;
                        playbackQue.playQuedMessages();
                    }
                }
            }
        }
    }

    private void carConnect() {
        playbackQue=new PlayBackQue(CarnotificationListener.this);
        Log.d("CarNotif","Enterng car mode");
        soundPlayer=new CarNotificationSoundPlayer(context.getApplicationContext(),playbackQue);
        playbackQue.isPlaying=false;
        pastNot=new HashMap<>();
        car = Car.createCar(getApplicationContext(), carReady);
        car.connect();
    }
    private CarConnectionCallback carReady = new CarConnectionCallback() {

        @Override
        public void onConnected(Car car) {
            while (soundPlayer==null) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            soundPlayer.isCarReady=true;
            try {
                Log.d("carNotif","Car Connected");
                //  CarFirstPartyManager mgr = (CarFirstPartyManager) car.getCarManager(CarFirstPartyManager.SERVICE_NAME);
                //mgr.
                soundPlayer.carAudioManager = car.getCarManager(CarAudioManager.class);
                soundPlayer.audioAttributes = soundPlayer.carAudioManager.getAudioAttributesForCarUsage(CarAudioManager.CAR_AUDIO_USAGE_NOTIFICATION);
                soundPlayer.playBackQue.audioAttributes=soundPlayer.audioAttributes;
                soundPlayer.playBackQue.carAudioManager=soundPlayer.carAudioManager;
                //  playbackQue=new PlayBackQue(carAudioManager,audioAttributes);
            } catch (CarNotConnectedException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onDisconnected(Car car) {
            Log.d("carNotif","Car Disconnected");
            if (soundPlayer!=null)
                soundPlayer.isCarReady=false;
        }
    };
}
