package com.backustech.ipalmap.other;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.palmap.gl.cache.TextOption;
import com.palmap.gl.plugin.IBitmapLoader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 王天明 on 2018/3/2.
 * 简单示范如何使用Picasso替代图片加载
 * 该demo仅供参考.
 */
public class PicassoImageLoader implements IBitmapLoader {

    private static final String TAG = "PicassoImageLoader";

    private static class MyTarget implements Target {

        private final WeakReference<PicassoImageLoader> ref;

        public MyTarget(PicassoImageLoader instance) {
            this.ref = new WeakReference<>(instance);
            instance.requestTarget.add(this);
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            if (this.ref.get() == null) {
                return;
            }
            this.ref.get().requestTarget.remove(this);
            Log.d(TAG, "onBitmapLoaded: " + this.ref.get().requestTarget.size());
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            if (this.ref.get() == null) {
                return;
            }
            this.ref.get().requestTarget.remove(this);
            Log.d(TAG, "onBitmapFailed: " + this.ref.get().requestTarget.size());
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    }

    private Context context;

    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private Object tag = new Object();

    private final Picasso picasso;

    private List<Target> requestTarget = new ArrayList<>();

    public PicassoImageLoader(Context context) {
        Log.d(TAG, "PicassoImageLoader <init>!!!");
        this.context = context;
        picasso = Picasso.with(this.context);
    }

    @Override
    public void loadWithUrl(final String s, final int i, final int i1, final CallBack callBack) {
        final Target myTarget = new MyTarget(PicassoImageLoader.this) {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                super.onBitmapLoaded(bitmap, from);
                callBack.onLoadSuccess(bitmap);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                super.onBitmapFailed(errorDrawable);
                callBack.onLoadError(new RuntimeException("load error"));
            }
        };
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                picasso.load(s)
                        .tag(tag)
                        .resize(i, i1)
                        .into(myTarget);
            }
        });
    }

    @Override
    public void loadIconText(final TextOption textOption, final String url, final boolean iconEnable, final int i, final int i1, final CallBack callBack) {
        final Target myTarget = new MyTarget(PicassoImageLoader.this) {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                super.onBitmapLoaded(bitmap, from);
                callBack.onLoadSuccess(bitmap);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                super.onBitmapFailed(errorDrawable);
                callBack.onLoadError(new RuntimeException("load error"));
            }
        };
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                picasso.load(url)
                        .tag(tag)
                        .resize(i, i1)
                        .transform(new PicassoTextTransformation(textOption, iconEnable))
                        .into(myTarget);
            }
        });
    }

    /**
     * 加载单纯由文字构成的图片 暂不示范
     * @param textOption
     * @param callBack
     */
    @Override
    public void loadWithText(TextOption textOption, CallBack callBack) {

    }

    /**
     * 从id加载图片
     * @param i
     * @param callBack
     */
    @Override
    public void loadWithResource(final int i,final CallBack callBack) {
        final Target target = new MyTarget(PicassoImageLoader.this) {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                super.onBitmapLoaded(bitmap, from);
                callBack.onLoadSuccess(bitmap);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                super.onBitmapFailed(errorDrawable);
                callBack.onLoadError(new RuntimeException("load error"));
            }
        };
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                picasso.load(i)
                        .tag(tag)
                        .into(target);
            }
        });
    }

    /**
     * 这个path 是style文件配置的,根据自己的需求加载即可
     * @param path
     * @param callBack
     */
    @Override
    public void loadWithPath(final String path, final CallBack callBack) {
        final Target target = new MyTarget(PicassoImageLoader.this) {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                super.onBitmapLoaded(bitmap, from);
                callBack.onLoadSuccess(bitmap);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                super.onBitmapFailed(errorDrawable);
                callBack.onLoadError(new RuntimeException("load error"));
            }
        };
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                picasso.load(path)
                        .tag(tag)
                        .into(target);
            }
        });
    }

    /**
     * 加载图文 （从路径中加载）
     * @param textOption
     * @param path
     * @param b
     * @param width
     * @param height
     * @param callBack
     */
    @Override
    public void loadIconTextFromFile(TextOption textOption, String path, boolean b, int width, int height, CallBack callBack) {

    }

    @Override
    public void onDestroy() {
        cancelAll();
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onLowMemory() {
    }

    @Override
    public void cancelAll() {
        mainHandler.removeCallbacksAndMessages(null);
        for (Target t:requestTarget) {
            picasso.cancelRequest(t);
        }
    }
}
