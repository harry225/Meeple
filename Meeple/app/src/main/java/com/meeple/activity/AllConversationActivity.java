package com.meeple.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import com.meeple.utils.URLs;
import com.meeple.utils.Utils;

import org.apache.http.Header;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by harry on 9/26/15.
 */
public class AllConversationActivity extends AppCompatActivity {

    AlertMessages alertMessages;

    Toolbar toolbar;
    ListView lvChats;
    TextView tvNoUser;

    ProgressDialog pd;
    UsersListInfo usersListInfo;
    ArrayList<UsersObject> usersList;
    UsersListAdapter usersListAdapter;

    final int RESULT_CHAT = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_allconversation);

        initToolbar();
        initViews();

        alertMessages = new AlertMessages(this);

        if (Utils.isNetworkAvailable(AllConversationActivity.this)) {
            getUserList();
        } else {
            alertMessages.showErrornInConnection();
        }

        lvChats.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                UsersObject usersObject = usersListAdapter.getItem(position);
                Intent i = new Intent(AllConversationActivity.this, ChatActivity.class);
                Bundle b = new Bundle();
                b.putSerializable("USEROBJECT", (Serializable) usersObject);
                i.putExtras(b);
                startActivityForResult(i, RESULT_CHAT);
            }
        });
    }

    private void getUserList() {
        pd = null;
        pd = ProgressDialog.show(AllConversationActivity.this, "", "Loading...", true,
                false);

        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(Constant.TIMEOUT);
        client.addHeader("Token", Utils.getFromUserDefaults(AllConversationActivity.this, Constant.PARAMS_TOKEN));
        Debug.e("TOKEN*****", Utils.getFromUserDefaults(AllConversationActivity.this, Constant.PARAMS_TOKEN));
        Debug.e("RECENTCONVERS", "" + URLs.RECENTCONVERSATION + Utils.getFromUserDefaults(AllConversationActivity.this, Constant.PARAMS_ID));

        client.get(AllConversationActivity.this, URLs.RECENTCONVERSATION + Utils.getFromUserDefaults(AllConversationActivity.this, Constant.PARAMS_ID), new ListUserResponseHandler());
    }

    public class ListUserResponseHandler extends AsyncHttpResponseHandler {

        @Override
        public void onSuccess(int i, Header[] headers, byte[] responseBody) {

            pd.dismiss();

            String content = new String(responseBody);

            Debug.e("AllRecentConversation-Resp", "-" + content);

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
            usersListAdapter = new UsersListAdapter(AllConversationActivity.this, false);
            usersListAdapter.addAll(usersList);
            if (usersList.size() == 0) {
                tvNoUser.setVisibility(View.VISIBLE);
                lvChats.setVisibility(View.GONE);
            } else {
                tvNoUser.setVisibility(View.GONE);
                lvChats.setVisibility(View.VISIBLE);
            }
            lvChats.setAdapter(usersListAdapter);
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            pd.dismiss();
            Debug.e("AllRecentConversation-Resp", "-*** Failure");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == RESULT_OK) {
            Debug.e("StartActivity ","reqcode" + requestCode + " resultCode" + resultCode);
            if (requestCode == RESULT_CHAT) {
                Debug.e("StartActivity ", "reqcode" + requestCode + " resultCode" + resultCode);
                if (Utils.isNetworkAvailable(AllConversationActivity.this)) {
                    getUserList();
                } else {
                    alertMessages.showErrornInConnection();
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

    public void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void initViews() {
        lvChats = (ListView) findViewById(R.id.lvChats);
        tvNoUser = (TextView) findViewById(R.id.tvNoUser);
    }
}