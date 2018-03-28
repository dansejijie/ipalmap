package com.backustech.ipalmap;

import android.app.Application;

import com.backustech.ipalmap.utils.Constant;
import com.backustech.ipalmap.utils.FileUtilsTools;
import com.palmaplus.nagrand.core.Engine;

/**
 * Created by jian.feng on 2017/5/31.
 */

public class IpalmapApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

//        CrashReport.initCrashReport(this, "c53b8df309", false);

        // copy assets Nagrand/lua to SD Card Nagrand/lua folder
        FileUtilsTools.copyDirToSDCardFromAsserts(this, "Nagrand/lua", "font");
        FileUtilsTools.copyDirToSDCardFromAsserts(this, "Nagrand/lua", "Nagrand/lua");

        // init Engine
        Engine instance = Engine.getInstance();
        instance.startWithLicense(Constant.APP_KEY, this);

    }
}
