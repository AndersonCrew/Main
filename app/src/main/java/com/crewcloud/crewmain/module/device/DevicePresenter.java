package com.crewcloud.crewmain.module.device;


import com.crewcloud.crewmain.base.BaseView;

/**
 * Created by Dazone on 7/25/2017.
 */

public interface DevicePresenter {

    interface view extends BaseView {
        void onSucess();
        void onError();
    }
    interface presenter {
        void insertDevice(String regId);
        void deleteDevice(String regId);
    }
}
