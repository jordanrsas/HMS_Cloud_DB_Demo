package com.dtse.hmsclouddbconnection;

import android.app.Application;

import com.dtse.hmsclouddbconnection.model.CloudDBZoneWrapper;

public class HmsCloudDbApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CloudDBZoneWrapper.initAGConnectCloudDB(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
