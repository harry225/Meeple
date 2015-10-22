package com.meeple.json;

import com.google.gson.annotations.SerializedName;

/**
 * Created by harry on 10/2/15.
 */
public class BlockInfo {

    @SerializedName("status_code")
    public int status_code;

    @SerializedName("message")
    public String message;
}
