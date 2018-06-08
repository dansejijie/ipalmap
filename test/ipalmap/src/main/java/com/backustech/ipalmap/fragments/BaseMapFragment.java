package com.backustech.ipalmap.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.backustech.ipalmap.R;
import com.backustech.ipalmap.utils.Constant;
import com.palmaplus.nagrand.easyapi.Map;
import com.palmaplus.nagrand.view.MapView;

/**
 * Created by jian.feng on 2017/6/2.
 */

public abstract class BaseMapFragment extends BaseFragment {

    protected MapView mapView;
    protected Map map;

    @Override
    public View provideView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return getView(inflater, container);
    }

    protected abstract View getView(LayoutInflater inflater, ViewGroup container);

    @Override
    public void onInitFragment(Bundle savedInstanceState) {
        super.onInitFragment(savedInstanceState);
        // 获取MapView
        mapView = (MapView) view.findViewById(R.id.ipalmap_map);


        map = mapView.getMap();
        // 通过MapView获取Map对象，并且根据MapID渲染地图
        map.startWithMapID(Constant.SINGLE_BUILDING_ID);

        // 获取放置Overlay的ViewGroup
        ViewGroup container = (ViewGroup) view.findViewById(R.id.ipalmap_map_overlay_container);
        // 设置这个ViewGroup用于放置Overlay
        map.setOverlayContainer(container);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 销毁地图
        if (mapView != null && !mapView.getPtr().isRelase()) {
            mapView.drop();
        }
    }
}
