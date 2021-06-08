package com.crewcloud.crewmain.datamodel;

import com.google.gson.annotations.SerializedName;

public class Community {
    @SerializedName("BoardNo")
    private int boardNo;

    @SerializedName("Title")
    private String title;

    @SerializedName("RegDateToString")
    private String regDateToString;

    public int getBoardNo() {
        return boardNo;
    }

    public String getTitle() {
        return title;
    }

    public String getRegDateToString() {
        return regDateToString;
    }
}
