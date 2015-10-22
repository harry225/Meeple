package com.meeple.activity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.meeple.R;
import com.meeple.utils.Constant;
import com.meeple.utils.GPSTracker;
import com.meeple.utils.Utils;

/**
 * Created by harry on 10/9/15.
 */
public class SplashActivity extends AppCompatActivity {

    TextView ivStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

//        gpsTracker = new GPSTracker(this);
//        isGPSEnable = gpsTracker.isGPSEnabled;
//
//        if (isGPSEnable) {
//
//            if (Utils.isNetworkAvailable(this)) {
//
//                if (!Utils.getFromUserDefaults(SplashActivity.this, Constant.PARAMS_EMAIL).equals("")) {
//                    Intent i = new Intent(SplashActivity.this, DashBoardActivity.class);
//                    startActivityForResult(i, RESULT_DASHBOARD);
//                } else {
//
//                    Intent intent = new Intent(TakeMeActivity.this, FeedActivity.class);
//                    startActivity(intent);
//                    finish();
//
//                }
//
//            } else {
//
//                Log.e("newwork", "" + false);
//
//            }
//
//        } else {
//
//            Log.e("location enable", "" + false);
//
//        }
//
//        ivStart = (TextView) findViewById(R.id.ivTakeMe);
//        ivStart.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View view) {
//
//                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                startActivityForResult(intent, 1);
//
//            }
//
//        });
//
    }
//
//    GPSTracker gpsTracker;
//    boolean isGPSEnable;

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        gpsTracker = new GPSTracker(this);
//        isGPSEnable = gpsTracker.isGPSEnabled;
//
//        if (!isGPSEnable) {
//
//        } else {
//
//            getLogin();
//
//        }
//
//    }

}

