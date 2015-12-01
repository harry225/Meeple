package com.meeple.json;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * Created by harry on 9/29/15.
 */
public class ConversationObject {

    public String id;
    public String message;
    public String fromUserId;
    public String toUserId;
    public String createdAt;
    public String isImage;

    public boolean islocally = false;
    public String local_media_path = "";
    public String server_media_path = "";


}
