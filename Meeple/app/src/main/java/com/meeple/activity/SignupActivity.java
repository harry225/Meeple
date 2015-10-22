package com.meeple.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.provider.Settings.Secure;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.meeple.R;
import com.meeple.json.SignupInfo;
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


/**
 * Created by harry on 9/25/15.
 */
public class SignupActivity extends AppCompatActivity {

    AlertMessages alertMessages;
    Toolbar toolbar;

    TextInputLayout nameLayout, userIDLayout, emailLayout, passwordLayout, confirmPasswordLayout;

    EditText etName, etUserID, etEmail, etPassword, etConfirmPassword;
    TextView tvSignup;

    ProgressDialog pd;

    SignupInfo signupInfo;
    final int RESULT_DASHBOARD = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        initToolbar();
        initViews();

        alertMessages = new AlertMessages(this);

    }

    View.OnClickListener OnClickListenertvSignup = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (etName.getText().toString().length() == 0) {
                etName.setError("Required");
            } else if (etName.getText().toString().length() < 3) {
                etName.setError("Minimum length 3 charcters");
            } else if (etUserID.getText().toString().length() == 0) {
                etUserID.setError("Required");
            } else if (etUserID.getText().toString().length() < 4 || etUserID.getText().toString().length() > 16) {
                etUserID.setError("length between 4 to 16 characters");
            } else if (!Utils.isValidEmail(etEmail.getText().toString())) {
                etEmail.setError("Please enter valid Email");
            } else if (etPassword.getText().toString().length() == 0) {
                etPassword.setError("Required");
            } else if (etPassword.getText().toString().length() < 4 || etPassword.getText().toString().length() > 12) {
                etPassword.setError("length between 4 to 12 characters");
            } else if (etConfirmPassword.getText().toString().length() == 0) {
                etConfirmPassword.setError("Please Re-Enter Password");
            } else if (!etPassword.getText().toString().matches(etConfirmPassword.getText().toString())) {
                etConfirmPassword.setError("Please Enter Correct Password");
            } else {
                if (Utils.isNetworkAvailable(SignupActivity.this)) {
                    signup();
                } else {
                    alertMessages.showErrornInConnection();
                }
            }
        }
    };

    public void signup() {
        pd = null;
        pd = ProgressDialog.show(SignupActivity.this, "", "Loading...", true,
                false);

        try {

            String deviceID = Secure.getString(this.getContentResolver(),
                    Secure.ANDROID_ID);

            JSONObject json = new JSONObject();

            json.put("email", etEmail.getText().toString());
            json.put("password", etPassword.getText().toString());
            json.put("userId", etUserID.getText().toString());
            json.put("name", etName.getText().toString());

            StringEntity stringEntity = new StringEntity(json.toString());
            stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

            AsyncHttpClient client = new AsyncHttpClient();
            client.setTimeout(Constant.TIMEOUT);
//            client.addHeader("Token", "4hxH53OWFu0c1WOlwHKV9WmwQ8QMaXbb");

            client.post(SignupActivity.this, URLs.SIGNUP, stringEntity, "application/json", new RegisterNewUserResponseHandler());

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class RegisterNewUserResponseHandler extends AsyncHttpResponseHandler {

        @Override
        public void onSuccess(int i, Header[] headers, byte[] responseBody) {

            pd.dismiss();
            String content = new String(responseBody);
            Debug.e("SignUp Resp", "-" + content);

            Gson gson = new Gson();
            Type type = new TypeToken<SignupInfo>() {
            }.getType();

            signupInfo = gson.fromJson(content, type);

            try {
                if (signupInfo.status_code == 1) {

                    Utils.saveToUserDefaults(SignupActivity.this, Constant.PARAMS_PASS, etPassword.getText().toString());
                    Utils.saveToUserDefaults(SignupActivity.this, Constant.PARAMS_TOKEN, signupInfo.token);
                    Utils.saveToUserDefaults(SignupActivity.this, Constant.PARAMS_ID, signupInfo.user.id);
                    Utils.saveToUserDefaults(SignupActivity.this, Constant.PARAMS_USERID, signupInfo.user.userID);
                    Utils.saveToUserDefaults(SignupActivity.this, Constant.PARAMS_EMAIL, signupInfo.user.email);
                    Utils.saveToUserDefaults(SignupActivity.this, Constant.PARAMS_NAME, signupInfo.user.name);

                    Intent intent = new Intent(SignupActivity.this, DashBoardActivity.class);
                    startActivityForResult(intent, RESULT_DASHBOARD);
                } else {
                    Toast.makeText(SignupActivity.this, signupInfo.message, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
            pd.dismiss();
            Debug.e("SignUp Resp", "-*** Failure ***");
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
            if (requestCode == RESULT_DASHBOARD) {
                if (resultCode == Activity.RESULT_OK) {
                    Intent returnIntent = new Intent();
                    setResult(RESULT_OK, returnIntent);
                    finish();
                }
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
    }

    private void initViews() {
        nameLayout = (TextInputLayout) findViewById(R.id.nameLayout);
        userIDLayout = (TextInputLayout) findViewById(R.id.userIDLayout);
        emailLayout = (TextInputLayout) findViewById(R.id.emailLayout);
        passwordLayout = (TextInputLayout) findViewById(R.id.passwordLayout);
        confirmPasswordLayout = (TextInputLayout) findViewById(R.id.confirmPasswordLayout);

        etName = (EditText) findViewById(R.id.etName);
        etUserID = (EditText) findViewById(R.id.etUserID);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etConfirmPassword = (EditText) findViewById(R.id.etConfirmPassword);

        tvSignup = (TextView) findViewById(R.id.tvSignup);
        tvSignup.setOnClickListener(OnClickListenertvSignup);

    }
}
