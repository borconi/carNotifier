package uk.co.emil.borconi.carnotifier;

import android.app.PendingIntent;
import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

class PlayBackQueMessage {
    String message;
    String language;
    Action action;
    String response;
    int wrongCount,state;
    Context appContext;
    String locale;



    public PlayBackQueMessage(String message, String language, Action action, Context appContext) {
        this.message = message;
        this.language = language;
        this.locale=new Locale(language).toString();
        this.action=action;
        this.appContext=appContext;
        wrongCount=0;
        if (message.length()>500)
            state=-1;
        else
            state=0;
    }

    public String getMessage() {
        if (wrongCount>=3)
            return GenerateLanguageTag.getStringByLocal(appContext,R.string.aborting,locale);
        if (state<0 && wrongCount==0)
            return GenerateLanguageTag.getStringByLocal(appContext,R.string.longmessage,locale);
        else if (state<0 && wrongCount>0)
            return GenerateLanguageTag.getStringByLocal(appContext,R.string.notUnderstand,locale)+". "+GenerateLanguageTag.getStringByLocal(appContext,R.string.yesorno,locale);
        else if (state==0)
        {
            if (action !=null && action.isQuickReply())
                if (wrongCount==0)
                   // return message+". "+GenerateLanguageTag.getStringByLocal(appContext,R.string.replyQuestion;
                    return message+". "+GenerateLanguageTag.getStringByLocal(appContext,R.string.replyQuestion,locale);
                else
                    return GenerateLanguageTag.getStringByLocal(appContext,R.string.notUnderstand,locale)+". "+GenerateLanguageTag.getStringByLocal(appContext,R.string.yesorno,locale);
            else
                return message;
        }

        else if (state==1)
            return GenerateLanguageTag.getStringByLocal(appContext,R.string.whatsthemessage,locale);

        else if (state==2)
            if (wrongCount==0)
                return GenerateLanguageTag.getStringByLocal(appContext,R.string.gotit,locale)+" "+response+". " + GenerateLanguageTag.getStringByLocal(appContext,R.string.sendOrChange,locale);
            else
                return GenerateLanguageTag.getStringByLocal(appContext,R.string.notUnderstand,locale)+". "+GenerateLanguageTag.getStringByLocal(appContext,R.string.scc,locale);
        else
            return GenerateLanguageTag.getStringByLocal(appContext,R.string.sent,locale);


    }


    public boolean needsAnswer(){
         if (wrongCount>=3)
             return false;
         if (state==0 && (action==null || !action.isQuickReply()))
            return false;
         else if (state>2)
             return false;
        else
            return true;
    }


    public void sendReply(Context context)
    {
        try {
            action.sendReply(context,response);
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, String> getAnswers() {
        HashMap<String,String> answers=new HashMap<>();
        if (state<=0)
        {
            answers.put(GenerateLanguageTag.getStringByLocal(appContext,R.string.yes,locale),"yes");
            answers.put(GenerateLanguageTag.getStringByLocal(appContext,R.string.no,locale),"no");
        }
        else if (state==1)
            return null;
        else
        {
            answers.put(GenerateLanguageTag.getStringByLocal(appContext,R.string.send,locale),"send");
            answers.put(GenerateLanguageTag.getStringByLocal(appContext,R.string.change,locale),"change");
            answers.put(GenerateLanguageTag.getStringByLocal(appContext,R.string.cancel,locale),"cancel");
        }

        return answers;
    }

    public void changeState(int i) {
        Log.d("StateMonitor","Current state: " +i);
        state=state+i;
    }

    public void reset(){
        if (this.message.length()>500)
            state=-1;
        else
            state=0;
        wrongCount=0;
    }
}
