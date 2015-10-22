package com.meeple.json;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by harry on 9/28/15.
 */
public class UsersObject implements Serializable{

    public String id;
    public String name;
    public String userID;
    public String email;
    public int isBlock;

}
