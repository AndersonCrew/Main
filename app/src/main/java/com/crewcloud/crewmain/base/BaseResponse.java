package com.crewcloud.crewmain.base;

import com.google.gson.annotations.SerializedName;

/**
 * Created by tunglam on 12/23/16.
 */

public class BaseResponse<D> {

    @SerializedName("d")
    public D data;

    public D getData() {
        return data;
    }

    public void setData(D data) {
        this.data = data;
    }
}
