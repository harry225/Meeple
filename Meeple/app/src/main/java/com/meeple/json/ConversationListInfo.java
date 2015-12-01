package com.meeple.json;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by harry on 9/29/15.
 */
public class ConversationListInfo {

    @SerializedName("conversations")
    public List<Conversations> conversations;

    public class Conversations {

        @SerializedName("id")
        public String id;

        @SerializedName("message")
        public String message;

        @SerializedName("fromUserId")
        public String fromUserId;

        @SerializedName("toUserId")
        public String toUserId;

        @SerializedName("createdAt")
        public String createdAt;

        @SerializedName("isImage")
        public String isImage;

    }
}
