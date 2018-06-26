package com.brtbeacon.map3d.demo.map.event;

import com.brtbeacon.map.map3d.BRTMapView;
import com.brtbeacon.map.map3d.entity.BRTFloorInfo;
import com.brtbeacon.map3d.demo.activity.BaseMapActivity;

public class EventFloorMapActivity extends BaseMapActivity {
    @Override
    public void mapViewDidLoad(BRTMapView mapView, Error error) {
        super.mapViewDidLoad(mapView, error);
        if (error != null) {
            showToast("地图加载出错：" + error.getMessage());
        } else {
            mapView.setFloor(mapView.getFloorList().get(0));
        }
    }

    @Override
    public void onFinishLoadingFloor(BRTMapView mapView, BRTFloorInfo floorInfo) {
        super.onFinishLoadingFloor(mapView, floorInfo);
        showToast("楼层加载成功，当前楼层：" + floorInfo.getFloorNumber() + ", 名称：" + floorInfo.getFloorName());
    }

}
