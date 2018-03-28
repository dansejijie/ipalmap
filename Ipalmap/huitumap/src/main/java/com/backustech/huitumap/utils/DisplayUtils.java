package com.backustech.huitumap.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * Created by lchad on 2016/11/2.
 * Github: https://github.com/lchad
 */

public class DisplayUtils {
    private static final String TAG = DisplayUtils.class.getSimpleName();

    /**
     * 获取屏幕宽度
     */
    public static int getScreenWidthPixel(Context context) {
        return getDisplayMetrics(context).widthPixels;
    }

    /**
     * 获取屏幕高度
     */
    public static int getScreenHeightPixel(Context context) {
        return getDisplayMetrics(context).heightPixels;
    }

    /**
     * 获取 显示信息
     */
    public static DisplayMetrics getDisplayMetrics(Context context) {
        return context.getResources().getDisplayMetrics();
    }

    /**
     * px转dp
     */
    public static int px2Dp(Context context, int px) {
        return (int) (px / getDisplayMetrics(context).density);
    }

    /**
     * px转dp
     */
    public static int dp2Px(Context context, float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue,
                getDisplayMetrics(context));
    }

    /**
     * sx转dp
     */
    public static int sp2Px(Context context, float spValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue,
                getDisplayMetrics(context));
    }

    /**
     * 获取状态栏高度
     */
    public static int getStatusBarHeight(Context context) {
        int statusBarHeight = 0;
        try {
            int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
            }
        } catch (Resources.NotFoundException exception) {
        }
        return statusBarHeight;
    }

    /**
     * 获取导航栏高度
     */
    public static int getNavigationBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }
}
