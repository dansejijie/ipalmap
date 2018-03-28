package com.backustech.huitumap.activity;

import android.app.Application;

import com.backustech.huitumap.NagrandApplication;


/**
 * Created by tygzx on 2018/3/9.
 */

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        NagrandApplication.init(this);
    }
}
