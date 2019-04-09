package uk.co.emil.borconi.carnotifier;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

class Translator {
    public Translator() {
    }

    public String bingTranslate(String text) throws IOException {

       String response;
        String textEncoded= URLEncoder.encode(text, "utf-8");

        String url = "https://www.bing.com/tdetect";
        URL obj = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

        con.setRequestMethod("POST");
        //con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "UTF-8");

        con.setDoOutput(true);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(con.getOutputStream());
        outputStreamWriter.write("text="+textEncoded);
        outputStreamWriter.flush();


        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        response=in.readLine();
        in.close();
        con.disconnect();

        return response;

    }

    public void bingTranslate(final CarnotificationListener notifier, final String text, final Action xxx) {
        RequestQueue queue = Volley.newRequestQueue(notifier);
        String url ="https://www.bing.com/tdetect";

// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        notifier.onResponse(response,text,xxx);
                    }
                },
                new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    notifier.onResponse(Locale.getDefault().getLanguage(),text,xxx);
                }
                })
        {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                try {
                    params.put("text", URLEncoder.encode(text.substring(0, Math.min(text.length(), 100)), "utf-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return params;
            }
        };
        queue.add(stringRequest);
    }
}
