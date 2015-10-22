package com.meeple.json;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by harry on 9/25/15.
 */
public class LoginInfo {

    @SerializedName("status_code")
    public int status_code;

    @SerializedName("token")
    public String token;

    @SerializedName("message")
    public String message;

    @SerializedName("user")
    public User user;

    public class User {

        @SerializedName("id")
        public String id;

        @SerializedName("name")
        public String name;

        @SerializedName("user_name")
        public String userID;

        @SerializedName("email")
        public String email;

    }

}
