package com.meeple.json;

import com.google.gson.annotations.SerializedName;

/**
 * Created by harry on 9/25/15.
 */
public class SignupInfo {

    @SerializedName("status_code")
    public int status_code;

    @SerializedName("token")
    public String token;

    @SerializedName("message")
    public String message;

    @SerializedName("user")
    public LoginInfo.User user;

}