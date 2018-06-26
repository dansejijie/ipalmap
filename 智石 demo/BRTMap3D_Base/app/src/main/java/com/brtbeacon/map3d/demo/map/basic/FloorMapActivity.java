package com.brtbeacon.map3d.demo.map.basic;

import android.os.Bundle;
import android.widget.Toast;

import com.brtbeacon.map.map3d.BRTMapView;
import com.brtbeacon.map.map3d.entity.BRTFloorInfo;
import com.brtbeacon.map3d.demo.activity.BaseMapActivity;

public class FloorMapActivity extends BaseMapActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFloorControlVisible(true);
    }

    @Override
    public void onFinishLoadingFloor(BRTMapView mapView, BRTFloorInfo floorInfo) {
        super.onFinishLoadingFloor(mapView, floorInfo);
        Toast.makeText(this, "已经切换楼层至：" + floorInfo.getFloorNumber(), Toast.LENGTH_SHORT).show();
    }
}
