package com.backustech.ipalmap.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.backustech.ipalmap.R;
import com.backustech.ipalmap.utils.Constant;
import com.palmaplus.nagrand.easyapi.LBSManager;
import com.palmaplus.nagrand.easyapi.Map;
import com.palmaplus.nagrand.view.MapView;
import com.palmaplus.nagrand.view.overlay.ImageOverlay;

/**
 * Created by tygzx on 2018/3/28.
 */

public abstract class IpalmapActivity extends AppCompatActivity {

    protected ProgressDialog mProgressDialog;

    protected LBSManager lbsManager;
    protected MapView mapView;
    protected Map map;

    protected ImageOverlay startOverlay;
    protected ImageOverlay endOverlay;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        initLayout();
        initMap();
        initView();
        initData();
    }

    protected void initMap(){
        // 获取MapView
        mapView = (MapView)findViewById(R.id.ipalmap_map);
        // 通过MapView获取Map对象，并且根据MapID渲染地图
        map=mapView.getMap();
        mapView.getMap().startWithMapID(Constant.SINGLE_BUILDING_ID);

        // 获取放置Overlay的ViewGroup
        ViewGroup container = (ViewGroup)findViewById(R.id.ipalmap_map_overlay_container);
        // 设置这个ViewGroup用于放置Overlay
        map.setOverlayContainer(container);
        startOverlay = new ImageOverlay(IpalmapActivity.this);
        startOverlay.setBackgroundResource(R.drawable.ipalmap_start);
        startOverlay.init(new double[] { 0, 0 });
        map.addOverlay(startOverlay);
        // 添加终点的定位图标
        endOverlay = new ImageOverlay(IpalmapActivity.this);
        endOverlay.setBackgroundResource(R.drawable.ipalmap_end);
        endOverlay.init(new double[] { 0, 0 });
        map.addOverlay(endOverlay);

        lbsManager = new LBSManager(map, IpalmapActivity.this,Constant.SINGLE_BUILDING_ID,Constant.APP_KEY);

    }

    protected abstract void initLayout();

    protected abstract void initView();

    protected abstract void initData();


    @Override
    protected void onResume() {
        super.onResume();
        lbsManager.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        lbsManager.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        lbsManager.close();
        mapView.drop();
    }

    public void showLoadingDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }

    public void hideLoadingDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    protected void backgroundAlpha(float alpha){
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = alpha; //0.0-1.0
        getWindow().setAttributes(lp);
    }
}
