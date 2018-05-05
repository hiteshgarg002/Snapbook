package com.hrrock.snapbook.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by hp-u on 2/20/2018.
 */

public class UserDetailsModel {
    @SerializedName("email")
    private String email;
    @SerializedName("password")
    private String password;
    @SerializedName("name")
    private String name;
    @SerializedName("mobileno")
    private String mobileno;
    @SerializedName("username")
    private String username;
    @SerializedName("profile")
    private String profile;
    @SerializedName("gender")
    private String gender;
    @SerializedName("about")
    private String about;

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getMobileno() {
        return mobileno;
    }

    public String getUsername() {
        return username;
    }

    public String getProfile() {
        return profile;
    }

    public String getGender() {
        return gender;
    }

    public String getAbout() {
        return about;
    }
}
