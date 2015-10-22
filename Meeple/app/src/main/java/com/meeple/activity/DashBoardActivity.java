package com.meeple.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.meeple.R;
import com.meeple.adapter.UsersListAdapter;
import com.meeple.json.UsersListInfo;
import com.meeple.json.UsersObject;
import com.meeple.utils.AlertMessages;
import com.meeple.utils.Constant;
import com.meeple.utils.Debug;
import com.meeple.utils.GPSTracker;
import com.meeple.utils.URLs;
import com.meeple.utils.Utils;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by harry on 9/25/15.
 */

public class DashBoardActivity extends AppCompatActivity {

    AlertMessages alertMessages;

    Toolbar toolbar;

    TextView tvUserId;

    TextView tvNewConversation, tvConversations;
    TextView tvProfile;
    TextView tvLogout;

    ListView lvChats;
    TextView tvNoUser;

    final int RESULT_PROFILE = 6;

    ProgressDialog pd;
    UsersListInfo usersListInfo;
    ArrayList<UsersObject> usersList;
    UsersListAdapter usersListAdapter;

    GPSTracker gpsTracker;
    boolean isGPSEnable;

    double latitude;
    double longitude;

    final int RESULT_LOCATION = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Log.e("onCreate******", "");

        initToolbar();
        initViews();

        gpsTracker = new GPSTracker(this);
        isGPSEnable = gpsTracker.isGPSEnabled;

        alertMessages = new AlertMessages(this);

        tvUserId.setText("Hello, " + Utils.getFromUserDefaults(this, Constant.PARAMS_USERID));

        if (!isGPSEnable) {
            showSettingsAlert();
        } else {
            latitude = gpsTracker.getLatitude();
            longitude = gpsTracker.getLongitude();
            updateLocation(latitude, longitude);

//            Geocoder gcd = new Geocoder(this, Locale.getDefault());
//            try {
//                List<Address> addresses = gcd.getFromLocation(latitude, longitude, 1);
//            if (addresses.size() > 0)
//               Log.e("ADDRESS",""+addresses.get(0).getLocality());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            getUserList();
        }
    }

    View.OnClickListener OnClickListenertvProfile = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(DashBoardActivity.this, ProfileActivity.class);
//            startActivity(intent);
            startActivityForResult(intent, RESULT_PROFILE);
        }
    };

    View.OnClickListener OnClickListenertvLogout = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Utils.clearUserDefaults(DashBoardActivity.this);
            Intent intent = new Intent(DashBoardActivity.this, StartActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    };

    View.OnClickListener OnClickListenertvNewConversation = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (Utils.isNetworkAvailable(DashBoardActivity.this)) {
                gpsTracker = new GPSTracker(DashBoardActivity.this);
                isGPSEnable = gpsTracker.isGPSEnabled;

                if (!isGPSEnable) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(intent, 1);
                } else {
                    latitude = gpsTracker.getLatitude();
                    longitude = gpsTracker.getLongitude();
                    getUserList(latitude, longitude);
                }
            } else {
                alertMessages.showErrornInConnection();
            }
        }
    };

    View.OnClickListener OnClickListenertvConversations = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent i = new Intent(DashBoardActivity.this, AllConversationActivity.class);
            startActivityForResult(i, RESULT_PROFILE);
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_PROFILE) {

        }
        if (requestCode == RESULT_LOCATION) {
            gpsTracker = new GPSTracker(this);
            isGPSEnable = gpsTracker.isGPSEnabled;

            if (!isGPSEnable) {
                showSettingsAlert();
            } else {
                latitude = gpsTracker.getLatitude();
                longitude = gpsTracker.getLongitude();
                Log.e("LatLng", "" + latitude + "," + longitude);
                updateLocation(latitude, longitude);
            }
        }
    }

    public void showSettingsAlert() {
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(this);

        alertDialog.setTitle("GPS Settings");

        alertDialog.setMessage("GPS is not enabled. Do you want to go to GPS Settings ?");

        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent, RESULT_LOCATION);
            }
        });

        alertDialog.setNegativeButton("Retry", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        gpsTracker = new GPSTracker(DashBoardActivity.this);
                        isGPSEnable = gpsTracker.isGPSEnabled;

                        if (!isGPSEnable) {
                            showSettingsAlert();
                        } else {
                            latitude = gpsTracker.getLatitude();
                            longitude = gpsTracker.getLongitude();
                            updateLocation(latitude, longitude);
                        }
                    }
                }

        );
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    private void getUserList(double latitude, double longitude) {

        pd = null;
        pd = ProgressDialog.show(DashBoardActivity.this, "", "Loading...", true,
                false);

        try {
            JSONObject json = new JSONObject();
            json.put("userId", Utils.getFromUserDefaults(DashBoardActivity.this, Constant.PARAMS_ID));
            json.put("latitude", String.valueOf(latitude));
            json.put("longitude", String.valueOf(longitude));

            StringEntity stringEntity = new StringEntity(json.toString());
            stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            AsyncHttpClient client = new AsyncHttpClient();
            client.setTimeout(Constant.TIMEOUT);
            client.addHeader("Token", Utils.getFromUserDefaults(DashBoardActivity.this, Constant.PARAMS_TOKEN));

            client.post(DashBoardActivity.this, URLs.USER_LIST, stringEntity, "application/json", new ListUserResponseHandler());

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class ListUserResponseHandler extends AsyncHttpResponseHandler {

        @Override
        public void onSuccess(int i, Header[] headers, byte[] responseBody) {

            pd.dismiss();

            String content = new String(responseBody);

            Debug.e("Users-Resp", "-" + content);

            Gson gson = new Gson();
            Type type = new TypeToken<UsersListInfo>() {
            }.getType();

            usersListInfo = gson.fromJson(content, type);
            usersList = new ArrayList<UsersObject>();

            for (int j = 0; j < usersListInfo.users.size(); j++) {
                UsersObject usersObject = new UsersObject();
                usersObject.id = usersListInfo.users.get(j).id;
                usersObject.name = usersListInfo.users.get(j).name;
                usersObject.email = usersListInfo.users.get(j).email;
                usersObject.userID = usersListInfo.users.get(j).userID;
                usersObject.isBlock = usersListInfo.users.get(j).isBlock;
                usersList.add(usersObject);
            }
            Debug.e("USERSLIST", "" + usersList.size());

            showUserList();
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            pd.dismiss();
            Debug.e("Users-Resp", "-*** Failure");
        }
    }

    private void updateLocation(double latitude, double longitude) {

        pd = ProgressDialog.show(DashBoardActivity.this, "", "Loading...", true,
                false);
        try {
            JSONObject json = new JSONObject();
            json.put("userId", Utils.getFromUserDefaults(DashBoardActivity.this, Constant.PARAMS_ID));
            json.put("latitude", String.valueOf(latitude));
            json.put("longitude", String.valueOf(longitude));

            StringEntity stringEntity = new StringEntity(json.toString());
            stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            AsyncHttpClient client = new AsyncHttpClient();
            client.setTimeout(Constant.TIMEOUT);
            client.addHeader("Token", Utils.getFromUserDefaults(DashBoardActivity.this, Constant.PARAMS_TOKEN));

            client.post(DashBoardActivity.this, URLs.LOCATION_UPDATE, stringEntity, "application/json", new UpdateLocationResponseHandler());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class UpdateLocationResponseHandler extends AsyncHttpResponseHandler {

        @Override
        public void onSuccess(int i, Header[] headers, byte[] responseBody) {

            pd.dismiss();

            String content = new String(responseBody);

            Debug.e("Location-Resp", "-" + content);

        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            pd.dismiss();
            Debug.e("Location-Resp", "-*** Failure");
        }

    }

    public void showUserList() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(DashBoardActivity.this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_allconversation, null);
        dialogBuilder.setTitle("Select User...");
        dialogBuilder.setView(dialogView);

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        Toolbar toolbar = (Toolbar) dialogView.findViewById(R.id.toolbar);
        toolbar.setVisibility(View.GONE);
        lvChats = (ListView) dialogView.findViewById(R.id.lvChats);
        tvNoUser = (TextView) dialogView.findViewById(R.id.tvNoUser);

        usersListAdapter = new UsersListAdapter(DashBoardActivity.this, true);
        usersListAdapter.addAll(usersList);
        if (usersList.size() == 0) {

            tvNoUser.setVisibility(View.VISIBLE);
            lvChats.setVisibility(View.GONE);

        } else {

            tvNoUser.setVisibility(View.GONE);
            lvChats.setVisibility(View.VISIBLE);

        }

        lvChats.setAdapter(usersListAdapter);

        lvChats.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UsersObject usersObject = usersListAdapter.getItem(position);
                Intent i = new Intent(DashBoardActivity.this, ChatActivity.class);
                Bundle b = new Bundle();
                b.putSerializable("USEROBJECT", (Serializable) usersObject);
                i.putExtras(b);
                startActivity(i);
                alertDialog.dismiss();
            }
        });
    }

    public void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public void initViews() {
        tvUserId = (TextView) findViewById(R.id.tvUserId);
        tvNewConversation = (TextView) findViewById(R.id.tvNewConversation);
        tvConversations = (TextView) findViewById(R.id.tvConversations);
        tvProfile = (TextView) findViewById(R.id.tvProfile);
        tvLogout = (TextView) findViewById(R.id.tvLogout);

        tvProfile.setOnClickListener(OnClickListenertvProfile);
        tvLogout.setOnClickListener(OnClickListenertvLogout);

        tvNewConversation.setOnClickListener(OnClickListenertvNewConversation);
        tvConversations.setOnClickListener(OnClickListenertvConversations);
    }

    @Override
    public void onBackPressed() {
        Log.e("onBackPressed", "***********");
        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("onResume******", "");
    }
}