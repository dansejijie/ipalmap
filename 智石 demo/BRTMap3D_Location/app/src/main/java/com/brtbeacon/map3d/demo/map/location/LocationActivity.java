
package com.brtbeacon.map3d.demo.map.location;

import android.bluetooth.BluetoothAdapter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;

import com.brtbeacon.locationengine.ble.BRTBeacon;
import com.brtbeacon.locationengine.ble.BRTLocationManager;
import com.brtbeacon.locationengine.ble.BRTPublicBeacon;
import com.brtbeacon.map.map3d.BRTMapView;
import com.brtbeacon.map.map3d.entity.BRTPoint;
import com.brtbeacon.map.map3d.utils.BRTConvert;
import com.brtbeacon.map3d.demo.R;
import com.brtbeacon.map3d.demo.activity.BaseMapActivity;
import com.brtbeacon.mapdata.BRTLocalPoint;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.List;

public class LocationActivity extends BaseMapActivity {

    private BRTLocationManager locationManager;
    private View btnLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        btnLocation = findViewById(R.id.btn_location);
        btnLocation.setOnClickListener(this);
        btnLocation.setVisibility(View.GONE);
        checkBluetooth();
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_location;
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.btn_location: {
                view.setSelected(!view.isSelected());
                if (view.isSelected()){
                    startLocation();
                    view.setBackground(getResources().getDrawable(R.drawable.btn_locate_on));
                }else {
                    stopLocation();
                    mapView.setLocation(null);
                    view.setBackground(getResources().getDrawable(R.drawable.btn_locate_off));
                }
                break;
            }
        }
    }

    @Override
    public void onClickAtPoint(BRTMapView mapView, BRTPoint point) {
        super.onClickAtPoint(mapView, point);
    }

    @Override
    public void mapViewDidLoad(BRTMapView mapView, Error error) {
        super.mapViewDidLoad(mapView, error);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.location_arrow);
        mapView.setLocationImage(bitmap);
        locationManager = new BRTLocationManager(this, mapBundle.buildingId, mapBundle.appkey);
        locationManager.addLocationEngineListener(locationManagerListener);
        locationManager.setLimitBeaconNumber(true);
        locationManager.setMaxBeaconNumberForProcessing(5);
        locationManager.setRssiThreshold(-75);
        btnLocation.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocation();
    }

    private void checkBluetooth() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            showToast("当前设备没有蓝牙，无法进行定位操作！");
            return;
        }

        if (!adapter.isEnabled()) {
            adapter.enable();
        }

    }

    private void startLocation() {
        if (locationManager != null) {
            locationManager.startUpdateLocation();
        }
    }

    private void stopLocation() {
        if (locationManager != null) {
            locationManager.stopUpdateLocation();
        }
    }

    private BRTLocationManager.BRTLocationManagerListener locationManagerListener = new BRTLocationManager.BRTLocationManagerListener() {

        @Override
        public void didRangedBeacons(BRTLocationManager BRTLocationManager, List<BRTBeacon> list) {
            System.out.println("didRangedBeacons");
        }

        @Override
        public void didRangedLocationBeacons(BRTLocationManager BRTLocationManager, List<BRTPublicBeacon> list) {
            System.out.println("didRangedLocationBeacons");
        }

        @Override
        public void didFailUpdateLocation(BRTLocationManager BRTLocationManager, final Error error) {
            System.out.println("didFailUpdateLocation");
        }

        @Override
        public void didUpdateDeviceHeading(BRTLocationManager BRTLocationManager, double v) {
            System.out.println("didUpdateDeviceHeading");
            mapView.processDeviceRotation(v);
        }

        @Override
        public void didUpdateImmediateLocation(BRTLocationManager BRTLocationManager, final BRTLocalPoint tyLocalPoint) {
            System.out.println("didUpdateImmediateLocation");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LatLng pointLL = BRTConvert.toLatLng(tyLocalPoint.getX(), tyLocalPoint.getY());
                    BRTPoint point = new BRTPoint(tyLocalPoint.getFloor(), pointLL.getLatitude(), pointLL.getLongitude());
                    mapView.setLocation(point);
                    if (mapView.getCurrentFloor().getFloorNumber() != tyLocalPoint.getFloor()) {
                        mapView.setFloorByNumber(tyLocalPoint.getFloor());
                    }
                }
            });
        }

        @Override
        public void didUpdateLocation(BRTLocationManager BRTLocationManager, BRTLocalPoint tyLocalPoint) {
            System.out.println("didUpdateLocation");
        }
    };

    static {
        System.loadLibrary("BRTLocationEngine");
    }
}

