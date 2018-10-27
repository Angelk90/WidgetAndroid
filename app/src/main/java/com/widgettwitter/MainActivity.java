package com.widgettwitter;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

/*
*<activity
            android:name=".MainActivity"
            android:label="@string/title_activity_main"
            android:theme="@style/AppTheme.NoActionBar">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>*/

public class MainActivity extends AppCompatActivity {

    private String url = "https://twitter.com/search?f=tweets&q=%23gfvip&lang=it";
    private JSONArray array = new JSONArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //new Html().execute();
    }

    private class Html extends AsyncTask<Void, Void, Void> {
        String desc;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                // Connect to the web site
                Document doc = Jsoup.connect(url).get();
                // Using Elements to get the Meta data
                Elements mElementDataSize = doc.select("div[class=content]");
                // Locate the content attribute
                int mElementSize = mElementDataSize.size();

                for (int i = 0; i < mElementSize; i++) {
                    JSONObject obj = new JSONObject();

                    Elements mElementAvatar = doc.select("img[class=avatar js-action-profile-avatar]").eq(i);
                    String mAvatar = mElementAvatar.attr("src");

                    Elements mElementFullName = doc.select("strong[class=fullname show-popup-with-id u-textTruncate]").eq(i);
                    String mFullName = mElementFullName.text();

                    Elements mElementUsername = doc.select("span[class=username u-dir u-textTruncate]").eq(i);
                    String mUsername = mElementUsername.text();

                    Elements mElementTimestamp = doc.select("span[class=_timestamp js-short-timestamp js-relative-timestamp]").eq(i);
                    String mTimestamp = mElementTimestamp.text();

                    Elements mElementText = doc.select("p[class=TweetTextSize  js-tweet-text tweet-text]").eq(i);
                    String mText = mElementText.text();

                    try {
                        obj.put("avatar", mAvatar);
                        obj.put("fullname", mFullName);
                        obj.put("username", mUsername);
                        obj.put("timestamp ", mTimestamp);
                        obj.put("text ", mText);
                        array.put(obj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            for (int i = 0; i < array.length(); i++) {
                try {
                    Log.v("Ok:", String.valueOf(array.getJSONObject(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
