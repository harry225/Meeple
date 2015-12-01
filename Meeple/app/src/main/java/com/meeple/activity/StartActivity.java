package com.meeple.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.meeple.R;
import com.meeple.utils.Constant;
import com.meeple.utils.Debug;
import com.meeple.utils.Utils;

public class StartActivity extends AppCompatActivity {

    TextView tvLogin;
    TextView tvSignup;
    Toolbar toolbar;

    final int RESULT_LOGIN = 2;
    final int RESULT_REGISTER = 3;
    final int RESULT_DASHBOARD = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        if (!Utils.getFromUserDefaults(StartActivity.this, Constant.PARAMS_EMAIL).equals("")) {
            Intent i = new Intent(StartActivity.this, DashBoardActivity.class);
            startActivityForResult(i, RESULT_DASHBOARD);
        }
        initToolbar();
        initViews();
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

    }

    private void initViews() {
        tvLogin = (TextView) findViewById(R.id.tvLogin);
        tvSignup = (TextView) findViewById(R.id.tvSignup);

        tvLogin.setOnClickListener(OnClickListenertvLogin);
        tvSignup.setOnClickListener(OnClickListenertvSignup);
    }

    View.OnClickListener OnClickListenertvLogin = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent i = new Intent(StartActivity.this, LoginActivity.class);
            startActivityForResult(i, RESULT_LOGIN);
        }
    };

    View.OnClickListener OnClickListenertvSignup = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent i = new Intent(StartActivity.this, SignupActivity.class);
            startActivityForResult(i, RESULT_REGISTER);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Debug.e("StartActivity ", "reqcode" + requestCode + " resultCode" + resultCode);
            if (requestCode == RESULT_LOGIN) {
                Debug.e("StartActivity ", "reqcode" + requestCode + " resultCode" + resultCode);
                finish();
            } else if (requestCode == RESULT_REGISTER) {
                Debug.e("StartActivity", "reqcode" + requestCode + " resultCode" + resultCode);
                finish();
            } else if (requestCode == RESULT_DASHBOARD) {
                Debug.e("StartActivity", "reqcode" + requestCode + " resultCode" + resultCode);
                finish();
            }
        }
    }
}
