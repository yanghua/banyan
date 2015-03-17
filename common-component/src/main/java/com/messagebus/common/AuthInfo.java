package com.messagebus.common;

import java.io.Serializable;

public class AuthInfo implements Serializable {

    private String userName;
    private String password;

    public AuthInfo() {
    }

    public AuthInfo(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "AuthInfo{" +
            "userName='" + userName + '\'' +
            ", password='" + password + '\'' +
            '}';
    }
}
