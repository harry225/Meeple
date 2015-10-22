package com.meeple.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.meeple.R;
import com.meeple.json.LoginInfo;
import com.meeple.utils.AlertMessages;
import com.meeple.utils.Constant;
import com.meeple.utils.Debug;
import com.meeple.utils.URLs;
import com.meeple.utils.Utils;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by harry on 9/25/15.
 */
public class LoginActivity extends AppCompatActivity {


    AlertMessages alertMessages;
    Toolbar toolbar;
    TextInputLayout emailLayout, passwordLayout;
    EditText etEmail;
    EditText etPassword;
    TextView tvLogin;
    ProgressDialog pd;
    LoginInfo loginInfo;
    final int RESULT_DASHBOARD = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        initToolbar();
        initViews();

        etEmail.setText(Utils.getFromUserDefaults(this, Constant.PARAMS_EMAIL));
        alertMessages = new AlertMessages(this);


    }

    View.OnClickListener OnClickListenertvLogin = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            if (etEmail.getText().toString().length() == 0) {

                etEmail.setError("Please enter Email or UserID");
            }

            if (isValidEmail(etEmail.getText().toString())) {

                if (etPassword.getText().toString().length() == 0) {

                    etPassword.setError("Please enter Password");
                } else {
                    if (Utils.isNetworkAvailable(LoginActivity.this)) {
                        login();
                    } else {
                        alertMessages.showErrornInConnection();
                    }
                }

            } else {

                if (etEmail.getText().toString().contains("@") || etEmail.getText().toString().contains("_")) {

                    etEmail.setError("Please enter valid userid");

                } else {

                    if (etPassword.getText().toString().length() == 0) {

                        etPassword.setError("Please enter Password");

                    } else {
                        if (Utils.isNetworkAvailable(LoginActivity.this)) {
                            login();
                        } else {
                            alertMessages.showErrornInConnection();
                        }
                    }
                }

            }

        }

    };

    public final static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    public void login() {

        pd = null;
        pd = ProgressDialog.show(LoginActivity.this, "", "Loading...", true,
                false);

        try {

            JSONObject json = new JSONObject();

            json.put("email", etEmail.getText().toString());
            json.put("password", etPassword.getText().toString());

            StringEntity stringEntity = new StringEntity(json.toString());
            stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

            AsyncHttpClient client = new AsyncHttpClient();
            client.setTimeout(Constant.TIMEOUT);

            client.post(LoginActivity.this, URLs.LOGIN, stringEntity, "application/json", new LoginUserResponseHandler());

        } catch (UnsupportedEncodingException e) {

            e.printStackTrace();

        } catch (JSONException e) {

            e.printStackTrace();

        }

    }


    public class LoginUserResponseHandler extends AsyncHttpResponseHandler {

        @Override
        public void onSuccess(int i, Header[] headers, byte[] responseBody) {


            pd.dismiss();

            String content = new String(responseBody);

            Debug.e("Login Resp", "-" + content);

            Gson gson = new Gson();
            Type type = new TypeToken<LoginInfo>() {
            }.getType();

            loginInfo = gson.fromJson(content, type);

            try {

                if (loginInfo.status_code == 1) {

                    Utils.saveToUserDefaults(LoginActivity.this, Constant.PARAMS_PASS, etPassword.getText().toString());
                    Utils.saveToUserDefaults(LoginActivity.this, Constant.PARAMS_TOKEN, loginInfo.token);
                    Utils.saveToUserDefaults(LoginActivity.this, Constant.PARAMS_ID, loginInfo.user.id);
                    Utils.saveToUserDefaults(LoginActivity.this, Constant.PARAMS_USERID, loginInfo.user.userID);
                    Utils.saveToUserDefaults(LoginActivity.this, Constant.PARAMS_EMAIL, loginInfo.user.email);
                    Utils.saveToUserDefaults(LoginActivity.this, Constant.PARAMS_NAME, loginInfo.user.name);

                    Debug.e("Token Login*****", "" + Utils.getFromUserDefaults(LoginActivity.this, Constant.PARAMS_TOKEN));

                    Intent intent = new Intent(LoginActivity.this, DashBoardActivity.class);
                    startActivityForResult(intent, RESULT_DASHBOARD);

                } else {

                    alertMessages.showMessageWithTitle("Invalid", "UserID or Password mismatch");

                }

            } catch (Exception e) {

                e.printStackTrace();

            }

        }


        @Override
        public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {

            pd.dismiss();
            Debug.e("Login", "-" + "***Failure ");

        }


        @Override
        public void onFinish() {
            super.onFinish();

            pd.dismiss();

        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_DASHBOARD) {
            if (resultCode == Activity.RESULT_OK) {
                Intent returnIntent = new Intent();
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initToolbar() {

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

    }

    private void initViews() {

        emailLayout = (TextInputLayout) findViewById(R.id.emailLayout);
        passwordLayout = (TextInputLayout) findViewById(R.id.passwordLayout);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        tvLogin = (TextView) findViewById(R.id.tvLogin);
        tvLogin.setOnClickListener(OnClickListenertvLogin);


    }

}