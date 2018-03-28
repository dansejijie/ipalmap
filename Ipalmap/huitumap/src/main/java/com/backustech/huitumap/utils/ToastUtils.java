package com.backustech.huitumap.utils;

import android.os.Handler;
import android.widget.Toast;

import com.backustech.huitumap.NagrandApplication;


/**
 * Created by lchad on 2016/11/10.
 * Github: https://github.com/lchad
 */

public class ToastUtils {

    public static void showToast(String msg) {
        Toast.makeText(NagrandApplication.instance, msg, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(int msgId) {
        Toast.makeText(NagrandApplication.instance, msgId, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(Handler handler, final String msg) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(NagrandApplication.instance, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void showToast(Handler handler, final int msgId) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(NagrandApplication.instance, msgId, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void showLongToast(String msg) {
        Toast.makeText(NagrandApplication.instance, msg, Toast.LENGTH_LONG).show();
    }


    public static void showLongToast(int msgId) {
        Toast.makeText(NagrandApplication.instance, NagrandApplication.instance.getResources().getString(msgId), Toast.LENGTH_LONG).show();
    }

}
