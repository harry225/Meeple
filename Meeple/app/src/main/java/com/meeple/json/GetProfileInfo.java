package com.meeple.json;

import com.google.gson.annotations.SerializedName;

/**
 * Created by harry on 9/28/15.
 */
public class GetProfileInfo {

    @SerializedName("status_code")
    public int status_code;

    @SerializedName("message")
    public String message;

    @SerializedName("user")
    public User user;

    public class User {

        @SerializedName("id")
        public String id;

        @SerializedName("age")
        public String age;

        @SerializedName("gender")
        public String gender;

    }
}
