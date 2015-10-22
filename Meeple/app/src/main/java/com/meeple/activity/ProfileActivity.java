package com.meeple.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.meeple.R;
import com.meeple.json.GetProfileInfo;
import com.meeple.json.LoginInfo;
import com.meeple.utils.AlertMessages;
import com.meeple.utils.Constant;
import com.meeple.utils.Debug;
import com.meeple.utils.URLs;
import com.meeple.utils.Utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;

/**
 * Created by harry on 9/26/15.
 */
public class ProfileActivity extends AppCompatActivity {

    AlertMessages alertMessages;

    Toolbar toolbar;
    RadioGroup rgGender;
    TextInputLayout ageLayout;
    EditText etAge;
    TextView tvUserId;

    TextView tvUpdateProfile;
    ProgressDialog pd;

    GetProfileInfo getProfileInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initToolbar();
        initViews();

        alertMessages = new AlertMessages(this);

        tvUserId.setText("Hello, " + Utils.getFromUserDefaults(this, Constant.PARAMS_USERID));

        if (Utils.isNetworkAvailable(ProfileActivity.this)) {
            getProfileData();
        } else {
            alertMessages.showErrornInConnection();
        }

    }

    View.OnClickListener OnClickListenertvUpdateProfile = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (etAge.getText().toString().length() == 0) {
                etAge.setError("Please Enter Your Age");
            } else if (etAge.getText().toString().equals("0") || etAge.getText().toString().equals("00")) {
                etAge.setError("Age is not valid");
            } else {
                if (Utils.isNetworkAvailable(ProfileActivity.this)) {
                    updateProfile();
                } else {
                    alertMessages.showErrornInConnection();
                }
            }
        }
    };

    private void getProfileData() {
        pd = null;
        pd = ProgressDialog.show(ProfileActivity.this, "", "Loading...", true,
                false);

        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(Constant.TIMEOUT);
        client.addHeader("Token", Utils.getFromUserDefaults(ProfileActivity.this, Constant.PARAMS_TOKEN));

        Debug.e("TOKENProfile--*****", "" + Utils.getFromUserDefaults(ProfileActivity.this, Constant.PARAMS_TOKEN));

        Debug.e("GETPROFILE***", URLs.GETPROFILE + Utils.getFromUserDefaults(this, Constant.PARAMS_ID));
        client.get(ProfileActivity.this, URLs.GETPROFILE + Utils.getFromUserDefaults(this, Constant.PARAMS_ID), new getProfileResponseHandler());
    }

    public class getProfileResponseHandler extends AsyncHttpResponseHandler {

        @Override
        public void onSuccess(int i, Header[] headers, byte[] responseBody) {

            pd.dismiss();

            String content = new String(responseBody);

            Debug.e("getProfile-Resp", "-" + content);

            Gson gson = new Gson();
            Type type = new TypeToken<GetProfileInfo>() {
            }.getType();

            getProfileInfo = gson.fromJson(content, type);
            if (getProfileInfo.status_code != 0) {
                etAge.setText(getProfileInfo.user.age);
                if (getProfileInfo.user.gender.equalsIgnoreCase("Male")) {
                    rgGender.check(R.id.radioMale);
                } else {
                    rgGender.check(R.id.radioFemale);
                }
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            pd.dismiss();
            Debug.e("getProfile-Resp", "-*** Failure");
        }
    }

    public void updateProfile() {
        pd = null;
        pd = ProgressDialog.show(ProfileActivity.this, "", "Loading...", true,
                false);

        String selection = null;
        if (rgGender.getCheckedRadioButtonId() != -1) {
            int id = rgGender.getCheckedRadioButtonId();
            View radioButton = rgGender.findViewById(id);
            int radioId = rgGender.indexOfChild(radioButton);
            RadioButton btn = (RadioButton) rgGender.getChildAt(radioId);
            selection = (String) btn.getText();
            Debug.e("GENDER****", selection);
        }

        try {

            JSONObject json = new JSONObject();

            json.put("user_id", Utils.getFromUserDefaults(this, Constant.PARAMS_ID));
            json.put("age", etAge.getText().toString());
            json.put("gender", selection);

            StringEntity stringEntity = new StringEntity(json.toString());
            stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

            AsyncHttpClient client = new AsyncHttpClient();
            client.setTimeout(Constant.TIMEOUT);
            client.addHeader("Token", "4hxH53OWFu0c1WOlwHKV9WmwQ8QMaXbb");

            client.post(ProfileActivity.this, URLs.SETPROFILE, stringEntity, "application/json", new updateProfileResponseHandler());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class updateProfileResponseHandler extends AsyncHttpResponseHandler {

        @Override
        public void onSuccess(int i, Header[] headers, byte[] responseBody) {

            pd.dismiss();
            String content = new String(responseBody);
            Debug.e("setProfile-Resp", "-" + content);
            Toast.makeText(ProfileActivity.this, "Profile Successfully Updated", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            setResult(6, intent);
            finish();
        }

        @Override
        public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
            pd.dismiss();
            Debug.e("setProfile-Resp", "-" + "***Failure ");
        }

        @Override
        public void onFinish() {
            super.onFinish();
            pd.dismiss();
        }
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void initViews() {
        rgGender = (RadioGroup) findViewById(R.id.rgGender);
        ageLayout = (TextInputLayout) findViewById(R.id.ageLayout);
        etAge = (EditText) findViewById(R.id.etAge);
        tvUpdateProfile = (TextView) findViewById(R.id.tvUpdateProfile);
        tvUserId = (TextView) findViewById(R.id.tvUserId);

        tvUpdateProfile.setOnClickListener(OnClickListenertvUpdateProfile);

    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }
}
