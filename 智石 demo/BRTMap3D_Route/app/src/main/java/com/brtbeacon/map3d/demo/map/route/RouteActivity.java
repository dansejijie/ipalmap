package com.brtbeacon.map3d.demo.map.route;

import android.os.Bundle;

import com.brtbeacon.map.map3d.BRTMapView;
import com.brtbeacon.map.map3d.entity.BRTPoint;
import com.brtbeacon.map.map3d.route.BRTMapRouteManager;
import com.brtbeacon.map.map3d.route.BRTRouteResult;

import com.brtbeacon.map3d.demo.R;
import com.brtbeacon.map3d.demo.activity.BaseMapActivity;

public class RouteActivity extends BaseMapActivity {

    private BRTPoint startPoint;
    private BRTPoint endPoint;

    private BRTMapRouteManager routeManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_route;
    }

    @Override
    public void onClickAtPoint(BRTMapView mapView, BRTPoint point) {
        super.onClickAtPoint(mapView, point);

        if (startPoint != null && endPoint != null) {
            startPoint = null;
            endPoint = null;
            mapView.setRouteResult(null);
        } else if (startPoint == null) {
            startPoint = point;
        } else if (endPoint == null) {
            endPoint = point;
        }
        mapView.setRouteStart(startPoint);
        mapView.setRouteEnd(endPoint);

        if (startPoint != null && endPoint != null) {
            showToast("开始路径规划！");
            routeManager.requestRoute(startPoint, endPoint);
        }
    }

    @Override
    public void mapViewDidLoad(BRTMapView mapView, Error error) {
        super.mapViewDidLoad(mapView, error);
        routeManager = new BRTMapRouteManager(mapView.getBuilding(), mapView.getFloorList());
        routeManager.addRouteManagerListener(routeManagerListener);
    }


    private BRTMapRouteManager.BRTRouteManagerListener routeManagerListener = new BRTMapRouteManager.BRTRouteManagerListener() {

        @Override
        public void didSolveRouteWithResult(BRTMapRouteManager routeManager, BRTRouteResult routeResult) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showToast("路径规划成功执行完成！");
                    mapView.setRouteResult(routeResult);
                }
            });
        }

        @Override
        public void didFailSolveRouteWithError(BRTMapRouteManager routeManager, BRTMapRouteManager.BRTRouteException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showToast(e.getMessage());
                }
            });
        }
    };


}
