package com.meeple.utils;

/**
 * Created by harry on 9/25/15.
 */

public class URLs {

    public static String BASE_URL = "http://52.20.93.93:8080/";

//    public static String BASE_URL = "http://192.168.0.112:8080/";

    public static String LOGIN = BASE_URL + "login";
    public static String SIGNUP = BASE_URL + "user";
    public static String USER_LIST = BASE_URL + "user/list/";
    public static String GETPROFILE = BASE_URL + "user/getProfile/";
    public static String SETPROFILE = BASE_URL + "user/setProfile/";
    public static String GETCONVERSATION = BASE_URL + "message/getConversation";
    public static String RECENTCONVERSATION = BASE_URL + "user/list/recentConversations/";
    public static String BLOCKUSER = BASE_URL + "user/blockUser";
    public static String UNBLOCKUSER = BASE_URL + "user/unblockUser";
    public static String LOCATION_UPDATE = BASE_URL + "user/location";

}