package com.brtbeacon.map3d.demo.map.annotations;

import com.brtbeacon.map.map3d.BRTMapView;
import com.brtbeacon.map.map3d.entity.BRTPoint;
import com.brtbeacon.map3d.demo.activity.BaseMapActivity;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

public class FollowMapActivity extends BaseMapActivity {

    @Override
    public void onClickAtPoint(BRTMapView mapView, BRTPoint point) {
        super.onClickAtPoint(mapView, point);
        LatLng latLng = new LatLng(point.getLatitude(), point.getLongitude());
        mapView.setLocation(point);

        MapboxMap map = mapView.getMap();

        CameraPosition currentPosition = map.getCameraPosition();
        CameraPosition newPosition = new CameraPosition.Builder()
                .target(latLng)
                .bearing(currentPosition.bearing)
                .tilt(currentPosition.tilt)
                .zoom(currentPosition.zoom)
                .build();

        map.easeCamera(CameraUpdateFactory.newCameraPosition(newPosition), 500);
    }
}
