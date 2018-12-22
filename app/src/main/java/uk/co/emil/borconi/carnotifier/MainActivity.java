package uk.co.emil.borconi.carnotifier;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.car.Car;
import android.support.car.CarConnectionCallback;
import android.support.car.CarNotConnectedException;
import android.support.car.media.CarAudioManager;
import android.support.car.media.CarAudioRecord;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;


import com.google.android.apps.auto.sdk.nav.NavigationStateManager;
import com.google.android.apps.auto.sdk.nav.NavigationSuggestionManager;
import com.google.android.apps.auto.sdk.nav.suggestion.NavigationSuggestion;
import com.google.android.apps.auto.sdk.service.CarFirstPartyManager;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

public class MainActivity extends AppCompatActivity {


    private Set<String> myset=new HashSet<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> enabledapps = NotificationManagerCompat.getEnabledListenerPackages(this);
        boolean havepermission = false;
        for (String currapp : enabledapps) {
            Log.d("OBD2AA", "package:" + currapp);
            if (currapp.equalsIgnoreCase("uk.co.emil.borconi.carnotifier"))
                havepermission = true;
        }

        if (!havepermission) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(getResources().getString(R.string.perm_req));
            builder.setMessage(getResources().getString(R.string.perm_desc));
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                }
            });
            builder.setNegativeButton(getString(R.string.ignore), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            AlertDialog notification_dialog = builder.show();
        }
        else
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


                // If the user previously denied this permission then show a message explaining why
                // this permission is needed
                if (checkCallingOrSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED || checkCallingOrSelfPermission(Manifest.permission.PROCESS_OUTGOING_CALLS ) == PackageManager.PERMISSION_DENIED || checkCallingOrSelfPermission(Manifest.permission.READ_PHONE_STATE ) == PackageManager.PERMISSION_DENIED) {
                    requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.PROCESS_OUTGOING_CALLS,Manifest.permission.READ_PHONE_STATE}, 101);
                }
                else
                    startService(new Intent(this,CarnotificationListener.class));
            }
            else
            {
                startService(new Intent(this,CarnotificationListener.class));
            }


           /* Car car = Car.createCar(this, new CarConnectionCallback() {
                @SuppressLint("MissingPermission")
                @Override
                public void onConnected(Car car) {
                    android.util.Log.d("CAR", "Car connected");


                    try {

                        //NavigationSuggestionManager mgr = car.getCarManager(NavigationSuggestionManager.class);
                        CarFirstPartyManager mgr = (CarFirstPartyManager) car.getCarManager(CarFirstPartyManager.SERVICE_NAME);

                        car.
                        //NavigationSuggestionManager mgr=new NavigationSuggestionManager();

                        Intent navintent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("google.navigation:q=45.994286,26.1270052&mode=b"));
                        navintent.setPackage("com.google.android.apps.maps");


                        NavigationSuggestion[] navlist=new NavigationSuggestion[1];

                        NavigationSuggestion xxxxx=new NavigationSuggestion.Builder()
                                .setAddress("Strada Libertatii nr 3, Targu Secuiesc")
                                .setFormattedTimeToDestination("36:00")
                                .setLatLng(45.994286,26.1270052)
                                .setSecondsToDestination(129000)
                                .setTraffic(NavigationSuggestion.Traffic.UNKNOWN)
                                .setNavigationIntent(navintent)
                                .build();

                        navlist[0]=xxxxx;
                        mgr.sendNavigationSuggestions(navlist);


                        //car.disconnect();


                    }  catch (RemoteException e) {
                        e.printStackTrace();
                    } catch (CarNotConnectedException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onDisconnected(Car car) {
                    android.util.Log.d("CAR", "Car disconnected");
                }
            });
            car.connect();*/

           /* new Thread(new Runnable() {
                @Override
                public void run() {
                    Translator.googleTranslateApi("Mit irjak?".substring(0,Math.min(10,100)),"auto","en");
                }
            }).start();*/

            /*List<LanguageProfile> languageProfiles = null;
            try {
                languageProfiles = new LanguageProfileReader().readAllBuiltIn();


//build language detector:
                LanguageDetector languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
                        .withProfiles(languageProfiles)
                        .build();

//create a text object factory
                TextObjectFactory textObjectFactory = CommonTextObjectFactories.forDetectingOnLargeText();

//query:
                TextObject textObject = textObjectFactory.forText("ez egy tesztszöveg");
                final com.google.common.base.Optional<LdLocale> lang = languageDetector.detect(textObject);
                if (lang.isPresent())
                {
                    Log.d("CarNotif","Detected language: " + languageDetector.detect(textObject).get().getLanguage());

                }


            } catch (IOException e) {
                e.printStackTrace();
            }*/


           /* new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            Log.d("carNotif", "Translation 1: hu; " + Translator.googleTranslateApi("Got it, here is your message:", "en", "hu"));
                            Log.d("carNotif", "Translation 1: hu; " + Translator.googleTranslateApi("Sorry I don't understand, aborting.", "en", "hu"));
                        }
                    }).start();*/
           /* Iterator it = GenerateLanguageTag.languageMap.entrySet().iterator();
            while (it.hasNext()) {
                final Map.Entry pair = (Map.Entry)it.next();
                Log.d("CanNotif",pair.getKey() + " = " + pair.getValue());
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {

                                Log.d("carNotif", "Translation 2: "+pair.getKey()+"; " + Translator.googleTranslateApi("Got it, here is your message:", "en", String.valueOf(pair.getKey())));
                                Log.d("carNotif", "Translation 3: "+pair.getKey()+"; " + Translator.googleTranslateApi("Sorry I don't understand, aborting", "en", String.valueOf(pair.getKey())));



                            }
                        }).start();
            }*/
            /* it = GenerateLanguageTag.languageMap.entrySet().iterator();
            while (it.hasNext()) {
                final Map.Entry pair = (Map.Entry)it.next();
                Log.d("CanNotif",pair.getKey() + " = " + pair.getValue());
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                Log.d("carNotif", "Translation 2: "+pair.getKey()+"; " + Translator.googleTranslateApi("I didn't understand.", "en", String.valueOf(pair.getKey())));
                            }
                        }).start();
            }*/
           // Log.d("Language","Country code: " + (GenerateLanguageTag.languageMap.get("hu")).getReplyQuestion());

           /* SpeechRecognizer sr = SpeechRecognizer.createSpeechRecognizer(this);
            sr.setRecognitionListener(new VoiceListener(0l,null,sr));
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1);
            //if (new Locale("ro").getCountry().equalsIgnoreCase(""))
              //  Log.d("Language","No country");
            //Log.d("CarNotif","Lanugage: " + GenerateLanguageTag.forLanguage("ro"));
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hu-HU");
            sr.startListening(intent);*/


            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<PackageInfo> apps = getPackageManager().getInstalledPackages(0);

            Set<String> set=null;
            if (prefs.getStringSet("List", null)!=null)
             set = prefs.getStringSet("List", null);

            if (set!=null)
            {
                myset.addAll(set);
                Log.d("TEST",set.toString());
            }



            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout parent = (LinearLayout) (LinearLayout) findViewById(R.id.parrent_container);
           ArrayList<PackageClass> list=new ArrayList<PackageClass>();



            PackageManager packageManager= getApplicationContext().getPackageManager();
            for (PackageInfo currappp: apps) {
                try {
                    String appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(currappp.packageName, PackageManager.GET_META_DATA));

                    boolean toadd=false;
                    if (myset!=null)
                        toadd=myset.contains(currappp.packageName);

                    list.add(new PackageClass(appName,currappp.packageName,toadd));
                   //list.add(appName,currappp.packageName);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }

           /* Collections.sort(list, new Comparator() {
                @Override
                public int compare(Object o1, Object o2) {
                    PackageClass p1 = (PackageClass) o1;
                    PackageClass p2 = (PackageClass) o2;
                    return p1.getAppName().compareToIgnoreCase(p2.getAppName());
                }
            });*/

          //  for(String key : list.keySet()) {

            Collections.sort(list);

            for (PackageClass curr:list) {
               // PackageClass curr=(PackageClass)list.get(key);

                final View custom = inflater.inflate(R.layout.app_selector, null);
                TextView tv = (TextView) custom.findViewById(R.id.textView);
                Switch sw= (Switch) custom.findViewById(R.id.switch1);
                tv.setText(curr.appName);
                sw.setTag(curr.packageName);
               sw.setChecked(curr.isEnabled);
                parent.addView(custom);
                sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                       //Log.d("TAG","State changed: " + buttonView.getTag().toString() + ", state: " + isChecked);
                        if (myset!=null && myset.contains(buttonView.getTag().toString()) && !isChecked)
                            myset.remove(buttonView.getTag().toString());
                        else if (myset!=null && !myset.contains(buttonView.getTag().toString()) && isChecked)
                            myset.add(buttonView.getTag().toString());
                        else if (myset==null && isChecked)
                            myset.add(buttonView.getTag().toString());
                        Log.d("carNotifier","Set: " + myset.toString());
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putStringSet("List",myset);
                        editor.apply();
                        editor.commit();

                        CarnotificationListener.updateSet(myset);

                    }
                });
            }
            }
        }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mymenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onPause() {

       // Log.d("carNotif","OnPause");
        super.onPause();
    }
    public void openpref(MenuItem item) {
        Log.d("OBD2AA","Menu Clicked");

        Intent i=null;
        if (item.getItemId()==R.id.pref_menu)
            i = new Intent(getBaseContext(),AppPreferences.class);

        startActivity(i);

    }

    }

