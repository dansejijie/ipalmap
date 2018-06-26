package com.brtbeacon.map3d.demo.map.event;

import com.brtbeacon.map.map3d.BRTMapView;
import com.brtbeacon.map3d.demo.activity.BaseMapActivity;

public class EventLoadMapActivity extends BaseMapActivity {

    @Override
    public void mapViewDidLoad(BRTMapView mapView, Error error) {
        super.mapViewDidLoad(mapView, error);
        if (error != null) {
            showToast("地图加载出错：" + error.getMessage());
        } else {
            showToast("地图加载成功，共有楼层：" + mapView.getFloorList().size());
            mapView.setFloor(mapView.getFloorList().get(0));
        }
    }
}
