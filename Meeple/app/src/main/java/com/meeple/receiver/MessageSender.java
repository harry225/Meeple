package com.meeple.receiver;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.meeple.utils.Config;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by harry on 10/5/15.
 */
public class MessageSender {
    private static final String TAG = "MessageSender";
    AsyncTask sendTask;
    AtomicInteger atomicInteger = new AtomicInteger();

    public void sendMessage(final Bundle data, final GoogleCloudMessaging gcm) {

        sendTask = new AsyncTask() {

            @Override
            protected String doInBackground(Object[] params) {
                String id = Integer.toString(atomicInteger.incrementAndGet());

                try {
                    gcm.send(Config.GOOGLE_PROJECT_ID + "@gcm.googleapis.com", id,
                            data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return "Message ID: " + id + " Sent.";
            }

            @Override
            protected void onPostExecute(Object o) {
                sendTask = null;
            }
        };

        sendTask.execute(null, null, null);
    }

}
