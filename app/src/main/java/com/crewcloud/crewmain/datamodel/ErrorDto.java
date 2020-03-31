package com.crewcloud.crewmain.datamodel;

import com.google.gson.annotations.SerializedName;

public class ErrorDto {

    public boolean unAuthentication;

    @SerializedName("code")
    public int code = 1;

    @SerializedName("message")
    public String message = "";

    @Override
    public String toString() {
        return "ErrorDto{" +
                "unAuthentication=" + unAuthentication +
                ", code=" + code +
                ", message='" + message + '\'' +
                '}';
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
