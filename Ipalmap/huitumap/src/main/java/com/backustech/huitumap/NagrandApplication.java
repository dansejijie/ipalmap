package com.backustech.huitumap;

import android.app.Application;
import android.provider.SyncStateContract;

import com.backustech.huitumap.constants.Constant;
import com.backustech.huitumap.utils.FileUtils;
import com.backustech.huitumap.utils.FileUtilsTools;
import com.palmaplus.nagrand.core.Engine;



/**
 * Created by lchad on 2016/11/1.
 * Github: https://github.com/lchad
 */
public class NagrandApplication  {

    public static Application instance=null;

    public static void init(Application application) {


        instance=application;

        FileUtilsTools.copyDirToSDCardFromAsserts(application, "Nagrand/lua", "font");
        FileUtilsTools.copyDirToSDCardFromAsserts(application, "Nagrand/lua", "Nagrand/lua");

        // init Engine
        Engine instance = Engine.getInstance();
        instance.startWithLicense(Constant.APP_KEY, application);
    }
}
