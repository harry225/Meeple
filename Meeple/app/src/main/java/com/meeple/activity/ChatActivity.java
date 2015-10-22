package com.meeple.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.meeple.R;
import com.meeple.adapter.ChatArrayAdapter;
import com.meeple.json.BlockInfo;
import com.meeple.json.ConversationListInfo;
import com.meeple.json.ConversationObject;
import com.meeple.json.UsersObject;
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
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * Created by harry on 9/29/15.
 */
public class ChatActivity extends AppCompatActivity {

    AlertMessages alertMessages;

    Toolbar toolbar;
    EditText etWriteMessage;
    TextView tvSend;
    String username;
    int isBlock;
    ListView lvChats;
    Menu menu;

    ChatArrayAdapter chatArrayAdapter;
    ProgressDialog pd;
    UsersObject usersObject;
    MenuItem menuItem_block;

    ConversationListInfo conversationListInfo;
    ArrayList<ConversationObject> conversation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        Intent i = getIntent();
        Bundle b = i.getExtras();
        usersObject = (UsersObject) b.getSerializable("USEROBJECT");
        username = usersObject.name;
        isBlock = usersObject.isBlock;
        Debug.e("BLOCKED", "" + isBlock);

        initToolbar();
        initViews();

        if (isBlock == 0) {
            initSocket();
            listenSocket();
        }

        alertMessages = new AlertMessages(this);
        chatArrayAdapter = new ChatArrayAdapter(this);

        if (Utils.isNetworkAvailable(ChatActivity.this)) {
            getConversation();
        } else {
            alertMessages.showErrornInConnection();
        }
        lvChats.setAdapter(chatArrayAdapter);


    }

    private void listenSocket() {

        Object[] obj = new Object[2];
        obj[0] = usersObject.id;
        obj[1] = Utils.getFromUserDefaults(ChatActivity.this, Constant.PARAMS_TOKEN);
        socket.emit("listen", obj);


    }

    View.OnClickListener OnClickListenertvSend = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (isBlock == 1) {
                dialogforBlockorUnblock("unblock");
            } else if (etWriteMessage.getText().toString().length() != 0) {
                attemptSend(etWriteMessage.getText().toString());
                etWriteMessage.setText("");
            }
        }


    };

    private void dialogforBlockorUnblock(String BlockorUnblock) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        builder.setMessage("Are you sure to " + BlockorUnblock + " " + usersObject.name + " ?");
        builder.setPositiveButton(BlockorUnblock, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                if (Utils.isNetworkAvailable(ChatActivity.this)) {
                    if (isBlock == 1) {
                        unblockUser();
                    } else {
                        blockUser();
                    }
                } else {
                    alertMessages.showErrornInConnection();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
        alert.setCancelable(false);

    }

    private void attemptSend(String message) {

        if (TextUtils.isEmpty(message)) {
            return;
        }

        Object[] obj = new Object[1];
        obj[0] = message;

        etWriteMessage.setText("");

        ConversationObject conversationObject = new ConversationObject();
        conversationObject.message = message;
        conversationObject.fromUserId = Utils.getFromUserDefaults(ChatActivity.this, Constant.PARAMS_ID);
        conversationObject.toUserId = usersObject.id;
        conversationObject.createdAt = Utils.getDeviceTime();
        conversationObject.islocally = true;
        chatArrayAdapter.add(conversationObject);

        socket.emit("message", obj);


    }

    private Emitter.Listener onListening = new Emitter.Listener() {

        @Override
        public void call(final Object... args) {

            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                }
            });
        }

    };

    private Emitter.Listener onNewMessage = new Emitter.Listener() {

        @Override
        public void call(final Object... args) {

            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    String Message = (String) args[0];
                    ConversationObject conversationObject = new ConversationObject();
                    conversationObject.message = Message;
                    conversationObject.fromUserId = (String) args[1];
                    conversationObject.toUserId = (String) args[2];
                    conversationObject.createdAt = (String) args[3];
                    chatArrayAdapter.add(conversationObject);

                }


            });
        }

    };

    public void initSocket() {

        socket.on("listening", onListening);
        socket.on("message", onNewMessage);
//        socket.on("getNewMessage", onNewMessage);
        socket.connect();

    }

    private Socket socket;

    {
        try {
            socket = IO.socket("http://52.20.93.93:8081/");
        } catch (URISyntaxException e) {
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        socket.disconnect();
        socket.off("newmessage", onNewMessage);
    }

    private void getConversation() {
        pd = null;
        pd = ProgressDialog.show(ChatActivity.this, "", "Loading...", true,
                false);

        try {

            JSONObject json = new JSONObject();

            json.put("fromUserId", Utils.getFromUserDefaults(this, Constant.PARAMS_ID));
            json.put("toUserId", usersObject.id);
            json.put("pageSize", "20000");
            json.put("startIndex", "0");

            StringEntity stringEntity = new StringEntity(json.toString());
            stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

            AsyncHttpClient client = new AsyncHttpClient();
            client.setTimeout(Constant.TIMEOUT);
            client.addHeader("Token", Utils.getFromUserDefaults(ChatActivity.this, Constant.PARAMS_TOKEN));

            client.post(ChatActivity.this, URLs.GETCONVERSATION, stringEntity, "application/json", new getConversationResponseHandler());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class getConversationResponseHandler extends AsyncHttpResponseHandler {

        @Override
        public void onSuccess(int i, Header[] headers, byte[] responseBody) {

            pd.dismiss();
            String content = new String(responseBody);

            Debug.e("getConversation-Resp", "-" + content);

            Gson gson = new Gson();
            Type type = new TypeToken<ConversationListInfo>() {
            }.getType();

            conversationListInfo = gson.fromJson(content, type);
            conversation = new ArrayList<ConversationObject>();

            for (int j = conversationListInfo.conversations.size() - 1; j >= 0; j--) {
                ConversationObject conversationObject = new ConversationObject();
                conversationObject.id = conversationListInfo.conversations.get(j).id;
                conversationObject.message = conversationListInfo.conversations.get(j).message;
                conversationObject.fromUserId = conversationListInfo.conversations.get(j).fromUserId;
                conversationObject.toUserId = conversationListInfo.conversations.get(j).toUserId;
                conversationObject.createdAt = conversationListInfo.conversations.get(j).createdAt;
                conversation.add(conversationObject);
            }
            chatArrayAdapter.addAll(conversation);
            lvChats.setAdapter(chatArrayAdapter);
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            pd.dismiss();
            Debug.e("getConversation-Resp", "-*** Failure");
        }
    }

    public void unblockUser() {

        pd = null;
        pd = ProgressDialog.show(ChatActivity.this, "", "Loading...", true,
                false);

        try {

            JSONObject json = new JSONObject();

            json.put("fromUserId", Utils.getFromUserDefaults(ChatActivity.this, Constant.PARAMS_ID));
            json.put("toUserId", usersObject.id);

            StringEntity stringEntity = new StringEntity(json.toString());
            stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

            AsyncHttpClient client = new AsyncHttpClient();
            client.setTimeout(Constant.TIMEOUT);
            client.addHeader("Token", Utils.getFromUserDefaults(ChatActivity.this, Constant.PARAMS_TOKEN));

            client.post(ChatActivity.this, URLs.UNBLOCKUSER, stringEntity, "application/json", new unBlockUserResponseHandler());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public class unBlockUserResponseHandler extends AsyncHttpResponseHandler {

        @Override
        public void onSuccess(int i, Header[] headers, byte[] responseBody) {

            pd.dismiss();

            String content = new String(responseBody);

            Debug.e("unBlock Resp", "-" + content);

            Gson gson = new Gson();
            Type type = new TypeToken<BlockInfo>() {
            }.getType();

            BlockInfo blockInfo = gson.fromJson(content, type);
            try {
                if (blockInfo.status_code == 1) {
                    initSocket();
                    listenSocket();
                    isBlock = 0;
                    etWriteMessage.setText("");
                    menuItem_block.setTitle("Block");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
            pd.dismiss();
            Debug.e("unBlock", "-" + "***Failure ");
        }

        @Override
        public void onFinish() {
            super.onFinish();
            pd.dismiss();
        }
    }

    public void blockUser() {
        pd = null;
        pd = ProgressDialog.show(ChatActivity.this, "", "Loading...", true,
                false);
        try {

            JSONObject json = new JSONObject();

            json.put("fromUserId", Utils.getFromUserDefaults(ChatActivity.this, Constant.PARAMS_ID));
            json.put("toUserId", usersObject.id);

            StringEntity stringEntity = new StringEntity(json.toString());
            stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

            AsyncHttpClient client = new AsyncHttpClient();
            client.setTimeout(Constant.TIMEOUT);
            client.addHeader("Token", Utils.getFromUserDefaults(ChatActivity.this, Constant.PARAMS_TOKEN));

            client.post(ChatActivity.this, URLs.BLOCKUSER, stringEntity, "application/json", new BlockUserResponseHandler());

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public class BlockUserResponseHandler extends AsyncHttpResponseHandler {


        @Override
        public void onSuccess(int i, Header[] headers, byte[] responseBody) {

            pd.dismiss();

            String content = new String(responseBody);

            Debug.e("Block Resp", "-" + content);

            Gson gson = new Gson();
            Type type = new TypeToken<BlockInfo>() {
            }.getType();

            BlockInfo blockInfo = gson.fromJson(content, type);
            try {
                if (blockInfo.status_code == 1) {
                    isBlock = 1;
                    menuItem_block.setTitle("Unblock");
                    socket.disconnect();
                    socket.off("newmessage", onNewMessage);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
            pd.dismiss();
            Debug.e("Block", "-" + "***Failure ");
        }

        @Override
        public void onFinish() {
            super.onFinish();
            pd.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu customMenu) {
        this.menu = customMenu;
        getMenuInflater().inflate(R.menu.menu_chat, customMenu);
        menuItem_block = menu.findItem(R.id.action_block);
        if (isBlock == 0) {
            menuItem_block.setTitle("Block");
        } else {
            menuItem_block.setTitle("Unblock");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                Intent returnIntent = new Intent();
                setResult(RESULT_OK, returnIntent);
                finish();
                return true;
            case R.id.action_block:
                if (isBlock == 0) {
                    dialogforBlockorUnblock("Block");
                } else {
                    dialogforBlockorUnblock("unblock");
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    public void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(username);
    }

    public void initViews() {

        etWriteMessage = (EditText) findViewById(R.id.etWriteMessage);

        tvSend = (TextView) findViewById(R.id.tvSend);

        lvChats = (ListView) findViewById(R.id.lvChats);

        tvSend.setOnClickListener(OnClickListenertvSend);

    }
}
