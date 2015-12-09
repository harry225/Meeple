package com.meeple.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.util.Log;

import com.meeple.json.ConversationObject;

import java.io.ByteArrayOutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by harry on 11/23/15.
 */

public class DBAdapter {

    //Table List
    public static final String DATABASE_NAME = "MyDBName.db";
    public static final String NEW_CONVERSATION_TABLE_NAME = "New_Conversation";
    public static final String CONVERSATION_TABLE_NAME = "Conversation";
    public static final String CHAT_TABLE_NAME = "Chat";
    public static final String PROFILE_TABLE_NAME = "Profile";

    private static final int DATABASE_VERSION = 1;
    public static final String COLUM_ID = "id";

    //    All Conversation List Screen
    public static final String COLUMN_ID_CONV = "id_conv";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_USERID = "userid";
    public static final String COLUMN_ISBLOCK = "isblock";

    //    Chat Screen
    public static final String COLUMN_ID_MESSAGE = "id";
    public static final String COLUMN_MESSAGE = "message";
    public static final String COLUMN_FROMUSER = "fromuser";
    public static final String COLUMN_TOUSER = "touser";
    public static final String COLUMN_MESSAGE_TIME = "createdAT";
    public static final String COLUMN_IS_IMAGE = "isImage";
    public static final String COLUMN_IMAGE = "image";

    //    Profile Screen
    public static final String COLUMN_GENDER = "gender";
    public static final String COLUMN_AGE = "age";

    Context context;
    DatabaseHelper dbhelper;
    private SQLiteDatabase db;

    private static final String DATABASE_NEW_CONV_CREATE = "create table New_Conversation (id integer primary key, id_conv text, name text,email text,userid text, isblock text);";
    private static final String DATABASE_CREATE = "create table Conversation (id integer primary key, id_conv text, name text,email text,userid text, isblock text);";
    private static final String DATABASE_CHAT_CREATE = "create table Chat (id integer primary key, userid text, message text,fromuser text, touser text, createdAT text, isImage text, image blob);";
    private static final String DATABASE_PROFILE_CREATE = "create table Profile (id integer primary key, gender text,age text);";

    public DBAdapter(Context ctx) {

        this.context = ctx;
        dbhelper = new DatabaseHelper(context);

    }

    private class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {

            super(context, DATABASE_NAME, null, DATABASE_VERSION);

        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_NEW_CONV_CREATE);
            db.execSQL(DATABASE_CREATE);
            db.execSQL(DATABASE_CHAT_CREATE);
            db.execSQL(DATABASE_PROFILE_CREATE);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            db.execSQL("DROP TABLE IF EXISTS usersinfo"); //$NON-NLS-1$
            onCreate(db);

        }

    }

    public DBAdapter open() {

        this.db = this.dbhelper.getWritableDatabase();
        return this;

    }

    /**
     * close return type: void
     */

    public void close() {

        this.dbhelper.close();

    }

    public boolean insertConversation(String id, String name, String email, String userid, String isblock) {

        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_ID_CONV, id);
        contentValues.put(COLUMN_NAME, name);
        contentValues.put(COLUMN_EMAIL, email);
        contentValues.put(COLUMN_USERID, userid);
        contentValues.put(COLUMN_ISBLOCK, isblock);

        int result = updateConversation(contentValues, id);

        if (result == 0) {

            db.insert(CONVERSATION_TABLE_NAME, null, contentValues);

        } else {

            Log.e("data update", "successfully");

        }
        return true;

    }

    public int updateConversation(ContentValues contentValues, String id) {

        return db.update(CONVERSATION_TABLE_NAME, contentValues, "id_conv = ? ", new String[]{id});

    }

    public Cursor getConversationList() {

        Cursor cursor = db.rawQuery("select * from Conversation", null);
        return cursor;

    }

    public Cursor getNewConversationList() {

        Cursor cursor = db.rawQuery("select * from New_Conversation", null);
        return cursor;

    }

    public boolean addMessages(String messagid, String message, String fromUserID, String toUserID, String createdAt, String isImage) {

        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_ID_MESSAGE, messagid);
        contentValues.put(COLUMN_MESSAGE, message);
        contentValues.put(COLUMN_FROMUSER, fromUserID);
        contentValues.put(COLUMN_TOUSER, toUserID);
        contentValues.put(COLUMN_MESSAGE_TIME, createdAt);
        contentValues.put(COLUMN_IS_IMAGE, isImage);

        int result = updateMessage(contentValues, message);

        if (result == 0) {

            db.insert(CHAT_TABLE_NAME, null, contentValues);

        } else {

            Log.e("data update", "successfully");

        }
        return true;

    }

    public int updateMessage(ContentValues contentValues, String id) {

        return db.update(CHAT_TABLE_NAME, contentValues, COLUMN_ID_MESSAGE + " = ? ", new String[]{id});

    }

    public boolean insertNewConversation(String id, String name, String email, String userid, String isblock) {

        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_ID_CONV, id);
        contentValues.put(COLUMN_NAME, name);
        contentValues.put(COLUMN_EMAIL, email);
        contentValues.put(COLUMN_USERID, userid);
        contentValues.put(COLUMN_ISBLOCK, isblock);

        db.insert(NEW_CONVERSATION_TABLE_NAME, null, contentValues);

        return true;

    }

    public Cursor getMessages(String userID) {
        Cursor c = db.query(CHAT_TABLE_NAME, null, "userid = ?", new String[]{userID}, null, null, null);
        return c;
    }

    public Cursor getChat(String userid) {

        Cursor cursor = db.rawQuery("select * from " + CHAT_TABLE_NAME + " where " + COLUMN_FROMUSER + "=" + userid + " or " + COLUMN_TOUSER + "=" + userid, null);

        return cursor;

    }

    public void deleteConversationListRecord() {
        db.execSQL("delete from " + CONVERSATION_TABLE_NAME);
    }

    public void deleteNewConversationListRecord() {
        db.execSQL("delete from " + NEW_CONVERSATION_TABLE_NAME);
    }

}
