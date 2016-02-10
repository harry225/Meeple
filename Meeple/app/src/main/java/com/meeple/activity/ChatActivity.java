package com.meeple.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.meeple.R;
import com.meeple.adapter.ChatArrayAdapter;
import com.meeple.emojicon.EmojiconEditText;
import com.meeple.emojicon.EmojiconGridView;
import com.meeple.emojicon.EmojiconsPopup;
import com.meeple.emojicon.emoji.Emojicon;
import com.meeple.json.BlockInfo;
import com.meeple.json.ConversationListInfo;
import com.meeple.json.ConversationObject;
import com.meeple.json.UsersObject;
import com.meeple.utils.AlertMessages;
import com.meeple.utils.Constant;
import com.meeple.utils.DBAdapter;
import com.meeple.utils.Debug;
import com.meeple.utils.URLs;
import com.meeple.utils.Utils;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity implements ImageChooserListener {

    AlertMessages alertMessages;

    Toolbar toolbar;
    LinearLayout llRootView;
    ImageLoader imageLoader;

    EmojiconEditText etWriteMessage;
    EmojiconsPopup emojiconsPopup;
    ImageView ivEmoji;
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
    DBAdapter dba;

    private ImageChooserManager imageChooserManager;
    private String filePath;
    private int chooserType;
    private boolean isActivityResultOver = false;
    private String originalFilePath;
    private String thumbnailFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        dba = new DBAdapter(ChatActivity.this);
        initImageLoader();

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

            getOfflineConversation();

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
                if (Utils.isNetworkAvailable(ChatActivity.this)) {
                    attemptSend(etWriteMessage.getText().toString(), 0, null);
                    etWriteMessage.setText("");
                } else {
                    alertMessages.showErrornInConnection();
                }
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

    private void attemptSend(String message, int type, String localpath) {

        Object[] obj = new Object[2];
        obj[0] = message;
        obj[1] = type;

        etWriteMessage.setText("");
        ConversationObject conversationObject = new ConversationObject();
        conversationObject.message = message;
        conversationObject.fromUserId = Utils.getFromUserDefaults(ChatActivity.this, Constant.PARAMS_ID);
        conversationObject.toUserId = usersObject.id;
        conversationObject.createdAt = Utils.getDeviceTime();
        conversationObject.islocally = true;
        conversationObject.local_media_path = localpath;

        //Message is Text
        if (type == 0) {

            conversationObject.isImage = "0";

        }
        //Message is Base64 String of Image
        else {

            conversationObject.isImage = "1";

        }

//        addToDatabase(conversationObject.message, conversationObject.fromUserId, conversationObject.toUserId, conversationObject.createdAt, conversationObject.isImage, conversationObject.bmp);

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
                    conversationObject.isImage = String.valueOf(args[4]);

                    Log.e("ISIMAGECHECK", conversationObject.isImage);

//                    if (conversationObject.isImage.equalsIgnoreCase("1")) {
//                        Log.e("ImageURL", "" + URLs.AMAZON_IMG_BASE + conversationObject.message);
//                        conversationObject.server_media_path = imageLoader.loadImageSync(URLs.AMAZON_IMG_BASE + conversationObject.message);
//                    }

                    chatArrayAdapter.add(conversationObject);
//                    addToDatabase(conversationObject.message, conversationObject.fromUserId, conversationObject.toUserId, conversationObject.createdAt, conversationObject.isImage, conversationObject.bmp);

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

        Log.e("GET-Conv", "GET-CONV1");
        try {
            pd = null;
            pd = ProgressDialog.show(ChatActivity.this, "", "Loading...", true,
                    false);

            JSONObject json = new JSONObject();

            json.put("fromUserId", Utils.getFromUserDefaults(this, Constant.PARAMS_ID));
            json.put("toUserId", usersObject.id);
            json.put("pageSize", "20000");
            json.put("startIndex", "0");

            Debug.e("Request parameter", "-" + json.toString());

            StringEntity stringEntity = new StringEntity(json.toString());
            stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));


            AsyncHttpClient client = new AsyncHttpClient();
            client.setTimeout(Constant.TIMEOUT);
            client.addHeader("Token", Utils.getFromUserDefaults(ChatActivity.this, Constant.PARAMS_TOKEN));

            client.post(ChatActivity.this, URLs.GETCONVERSATION, stringEntity, "application/json", new getConversationResponseHandler());

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    public class getConversationResponseHandler extends AsyncHttpResponseHandler {

        @Override
        public void onSuccess(int i, Header[] headers, byte[] responseBody) {

            pd.dismiss();
            String content = new String(responseBody);

            Log.e("getConversation-Resp", "-" + content);

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
                conversationObject.isImage = conversationListInfo.conversations.get(j).isImage;

                conversation.add(conversationObject);

//                addToDatabase(conversationObject.message, conversationObject.fromUserId, conversationObject.toUserId, conversationObject.createdAt, conversationObject.isImage);

                DBAdapter dba = new DBAdapter(ChatActivity.this);
                dba.open();
                dba.addMessages(conversationObject.id, conversationObject.message, conversationObject.fromUserId, conversationObject.toUserId, conversationObject.createdAt, conversationObject.isImage);
                dba.close();


            }
            Log.e("GET-Conv", "GET-CONV2");
            chatArrayAdapter.addAll(conversation);
            lvChats.setAdapter(chatArrayAdapter);
            Log.e("GET-Conv", "GET-CONV3");
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            pd.dismiss();

            error.printStackTrace();

            Log.e("error", "-" + new String(responseBody));

            Toast.makeText(ChatActivity.this, "Error" + new String(responseBody), Toast.LENGTH_SHORT).show();


        }

    }


    @Override
    public void onImageChosen(final ChosenImage chosenImage) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                isActivityResultOver = true;
//                originalFilePath = chosenImage.getFilePathOriginal();
//                Log.e("OriginalFilePath", originalFilePath);
                thumbnailFilePath = chosenImage.getFileThumbnail();

                if (chosenImage != null) {

                    Bitmap bmp = imageLoader.loadImageSync("file://" + thumbnailFilePath);

                    Log.e("Base64", "" + encodeTobase64(bmp).length());

                    if (Utils.isNetworkAvailable(ChatActivity.this)) {

                        attemptSend(encodeTobase64(bmp), 1, "file://" + thumbnailFilePath);

                    } else {

                        alertMessages.showErrornInConnection();

                    }


                } else {



                }

            }
        });
    }

    @Override
    public void onError(final String s) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                Log.i("onError", "OnError: " + s);
                Toast.makeText(ChatActivity.this, s,
                        Toast.LENGTH_LONG).show();

            }
        });

    }

    private void getOfflineConversation() {

        dba.open();
        conversation = new ArrayList<ConversationObject>();

        Cursor cursor = dba.getChat(usersObject.id);

        Log.e("Cursor**", "" + cursor.getCount());

        if (cursor.getCount() == 0) {


        } else {

            if (cursor.moveToFirst()) {

                while (cursor.isAfterLast() == false) {

                    ConversationObject conversationObject = new ConversationObject();
                    conversationObject.id = cursor.getString(cursor.getColumnIndex("id"));
                    conversationObject.message = cursor.getString(cursor.getColumnIndex("message"));
                    conversationObject.fromUserId = cursor.getString(cursor.getColumnIndex("fromuser"));
                    conversationObject.toUserId = cursor.getString(cursor.getColumnIndex("touser"));
                    conversationObject.createdAt = cursor.getString(cursor.getColumnIndex("createdAT"));
                    conversationObject.isImage = cursor.getString(cursor.getColumnIndex("isImage"));

                    conversation.add(conversationObject);
                    cursor.moveToNext();

                }

            }

        }

        dba.close();
        chatArrayAdapter.addAll(conversation);
        lvChats.setAdapter(chatArrayAdapter);

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
            case R.id.action_add_image:
                showChooserDialog();
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

        ivEmoji = (ImageView) findViewById(R.id.ivEmoji);

        etWriteMessage = (EmojiconEditText) findViewById(R.id.etWriteMessage);

        tvSend = (TextView) findViewById(R.id.tvSend);

        lvChats = (ListView) findViewById(R.id.lvChats);

        tvSend.setOnClickListener(OnClickListenertvSend);

        llRootView = (LinearLayout) findViewById(R.id.llRootView);

        emojiconsPopup = new EmojiconsPopup(llRootView, this);
        emojiconsPopup.setSizeForSoftKeyboard();

        emojiconsPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                changeEmojiKeyboardIcon(ivEmoji, R.drawable.smiley);
            }

        });

        //If the text keyboard closes, also dismiss the emoji popup
        emojiconsPopup.setOnSoftKeyboardOpenCloseListener(new EmojiconsPopup.OnSoftKeyboardOpenCloseListener() {

            @Override
            public void onKeyboardOpen(int keyBoardHeight) {

            }

            @Override
            public void onKeyboardClose() {
                if (emojiconsPopup.isShowing())
                    emojiconsPopup.dismiss();
            }

        });

        //On emoji clicked, add it to edittext
        emojiconsPopup.setOnEmojiconClickedListener(new EmojiconGridView.OnEmojiconClickedListener() {

            @Override

            public void onEmojiconClicked(Emojicon emojicon) {

                if (etWriteMessage == null || emojicon == null) {
                    return;
                }

                int start = etWriteMessage.getSelectionStart();
                int end = etWriteMessage.getSelectionEnd();
                if (start < 0) {
                    etWriteMessage.append(emojicon.getEmoji());
                } else {
                    etWriteMessage.getText().replace(Math.min(start, end),
                            Math.max(start, end), emojicon.getEmoji(), 0,
                            emojicon.getEmoji().length());
                }
            }
        });

        //On backspace clicked, emulate the KEYCODE_DEL key event
        emojiconsPopup.setOnEmojiconBackspaceClickedListener(new EmojiconsPopup.OnEmojiconBackspaceClickedListener() {

            @Override
            public void onEmojiconBackspaceClicked(View v) {
                KeyEvent event = new KeyEvent(
                        0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                etWriteMessage.dispatchKeyEvent(event);
            }

        });

        ivEmoji.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //If popup is not showing => emoji keyboard is not visible, we need to show it
                if (!emojiconsPopup.isShowing()) {

                    //If keyboard is visible, simply show the emoji popup
                    if (emojiconsPopup.isKeyBoardOpen()) {
                        emojiconsPopup.showAtBottom();
                        changeEmojiKeyboardIcon(ivEmoji, R.drawable.ic_action_keyboard);
                    }

                    //else, open the text keyboard first and immediately after that show the emoji popup
                    else {
                        etWriteMessage.setFocusableInTouchMode(true);
                        etWriteMessage.requestFocus();
                        emojiconsPopup.showAtBottomPending();
                        final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.showSoftInput(etWriteMessage, InputMethodManager.SHOW_IMPLICIT);
                        changeEmojiKeyboardIcon(ivEmoji, R.drawable.ic_action_keyboard);
                    }
                }

                //If popup is showing, simply dismiss it to show the undelying text keyboard
                else {
                    emojiconsPopup.dismiss();
                }
            }
        });

    }

    private void changeEmojiKeyboardIcon(ImageView iconToBeChanged, int drawableResourceId) {

        iconToBeChanged.setImageResource(drawableResourceId);

    }

    public void showChooserDialog() {

        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.dialog_image_picker, null);

        dialog.setView(view);

        final AlertDialog alertDialog = dialog.show();

        TextView tvCamera = (TextView) view.findViewById(R.id.tvCamera);
        TextView tvGallery = (TextView) view.findViewById(R.id.tvGallery);

        tvCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                takePicture();
            }
        });

        tvGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                chooseImage();
            }
        });

    }

    private void chooseImage() {

        chooserType = ChooserType.REQUEST_PICK_PICTURE;
        imageChooserManager = new ImageChooserManager(this,
                ChooserType.REQUEST_PICK_PICTURE, true);
        imageChooserManager.setImageChooserListener(this);
        imageChooserManager.clearOldFiles();
        try {
            filePath = imageChooserManager.choose();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void takePicture() {

        chooserType = ChooserType.REQUEST_CAPTURE_PICTURE;
        imageChooserManager = new ImageChooserManager(this,
                ChooserType.REQUEST_CAPTURE_PICTURE, true);
        imageChooserManager.setImageChooserListener(this);

        try {
            filePath = imageChooserManager.choose();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.e("OnActivityResult", "OnActivityResult");
        Log.e("FilePath**", "File Path : " + filePath);
        Log.e("Chooser**", "Chooser Type: " + chooserType);
        if (resultCode == RESULT_OK
                && (requestCode == ChooserType.REQUEST_PICK_PICTURE || requestCode == ChooserType.REQUEST_CAPTURE_PICTURE)) {
            if (imageChooserManager == null) {
                reinitializeImageChooser();
            }
            imageChooserManager.submit(requestCode, data);
        } else {


        }

    }

    private void reinitializeImageChooser() {

        imageChooserManager = new ImageChooserManager(this, chooserType, true);
        imageChooserManager.setImageChooserListener(this);
        imageChooserManager.reinitialize(filePath);

    }

    @SuppressWarnings("deprecation")
    private void initImageLoader() {

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true).cacheOnDisc(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new FadeInBitmapDisplayer(400)).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(ChatActivity.this).memoryCache(new WeakMemoryCache())
                .defaultDisplayImageOptions(defaultOptions).build();

        imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);

    }

    public static String encodeTobase64(Bitmap image) {

        Bitmap immagex = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immagex.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

        return imageEncoded;

    }

//    public void addToDatabase(String message, String fromUserId, String toUserId, String createdAt, String isImage) {
//
//        dba.open();
//        dba.addMessages(usersObject.userID, message, fromUserId, toUserId, createdAt, isImage);
//        dba.close();
//
//    }

}