package uk.co.emil.borconi.carnotifier;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;

class Translator {
    public static String googleTranslateApi(String text, String from, String to) {
        String returnString = "";

        try {
            String textEncoded= URLEncoder.encode(text, "utf-8");
            String url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl="+from+"&tl="+to+"&dt=t&q=" + textEncoded;
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response = httpclient.execute(new HttpGet(url));
            StatusLine statusLine = response.getStatusLine();
            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                String responseString = out.toString();
                out.close();

                String aJsonString = responseString;
                Log.d("GoogleLanguage",responseString);
                //aJsonString = aJsonString.replace("[", "");
                //aJsonString = aJsonString.replace("]", "");
                aJsonString = aJsonString.substring(aJsonString.indexOf("]")+3).replace("\"","").split(",")[1];
                Log.d("GoogleLanguage",aJsonString);
               /* Log.d("GoogleLanguage",aJsonString);
                int plusIndex = aJsonString.indexOf('"');
                aJsonString = aJsonString.substring(0, plusIndex);*/

                returnString = aJsonString;
            } else{
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch(Exception e) {
            returnString = e.getMessage();
        }

        return returnString;
    }
}
