package com.meeple.json;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by harry on 9/28/15.
 */
public class UsersListInfo {

        @SerializedName("users")
        public List<UsersList> users;

        public class UsersList {

                @SerializedName("id")
                public String id;

                @SerializedName("name")
                public String name;

                @SerializedName("user_name")
                public String userID;

                @SerializedName("email")
                public String email;

                @SerializedName("isBlock")
                public int isBlock;

        }
}