package com.example.rama.model.login;
import com.google.gson.annotations.SerializedName;
public class LoginData {
    @SerializedName("userid")
    private String userid;

    @SerializedName("name")
    private String name;

    @SerializedName("username")
    private String username;

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUserid() {
        return userid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}

