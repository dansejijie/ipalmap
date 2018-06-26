package com.brtbeacon.map3d.demo.map.route;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.brtbeacon.map.map3d.BRTMapView;
import com.brtbeacon.map.map3d.entity.BRTPoint;
import com.brtbeacon.map.map3d.route.BRTDirectionalHint;
import com.brtbeacon.map.map3d.route.BRTMapRouteManager;
import com.brtbeacon.map.map3d.route.BRTRoutePart;
import com.brtbeacon.map.map3d.route.BRTRouteResult;
import com.brtbeacon.map.map3d.route.GeometryEngine;
import com.brtbeacon.map.map3d.utils.BRTConvert;
import com.brtbeacon.map3d.demo.R;
import com.brtbeacon.map3d.demo.activity.BaseMapActivity;

import java.util.List;

public class RouteNavHintActivity extends BaseMapActivity {

    private BRTPoint startPoint;
    private BRTPoint endPoint;
    private BRTMapRouteManager routeManager = null;
    private TextView tvHint;
    private Button btnReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tvHint = findViewById(R.id.tv_hint);
        btnReset = findViewById(R.id.btn_reset);
        btnReset.setOnClickListener(this);
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_route_nav_hint;
    }

    @Override
    public void onClickAtPoint(BRTMapView mapView, BRTPoint point) {
        super.onClickAtPoint(mapView, point);

        if (startPoint != null && endPoint != null) {
            // 如果已经选择了起点和终点，开始模拟定位点选择
            updateNavLocation(point);
            return;
        } else if (startPoint == null) {
            //  选择起点
            startPoint = point;
            mapView.setRouteStart(startPoint);
        } else if (endPoint == null) {
            //  选择终点
            endPoint = point;
            mapView.setRouteEnd(endPoint);
        }

        if (startPoint != null && endPoint != null) {
            routeManager.requestRoute(startPoint, endPoint);
            showToast("开始路径规划！");
        }
    }

    @Override
    public void mapViewDidLoad(BRTMapView mapView, Error error) {
        super.mapViewDidLoad(mapView, error);
        routeManager = new BRTMapRouteManager(mapView.getBuilding(), mapBundle.appkey, mapView.getFloorList(), true);
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

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.btn_reset: {
                if (mapView.getRouteResult() == null)
                    return;

                startPoint = null;
                endPoint = null;
                mapView.setRouteStart(null);
                mapView.setRouteEnd(null);
                mapView.setRouteResult(null);

                break;
            }
        }
    }

    private void updateNavLocation(BRTPoint point) {
        BRTPoint locationPoint = point;
        BRTRouteResult routeResult = mapView.getRouteResult();

        if (routeResult != null && point != null) {
            //  如果定位点距离终点小于0.5米，直接判断到达终点。
            if (mapView.getRouteResult().distanceToRouteEnd(locationPoint) < 0.5) {
                //已到达目的地
                showToast("已经到达终点！");
            } else {
                BRTPoint nearestPoint = routeResult.getNearestPointOnRoute(locationPoint);
                if (nearestPoint == null) {
                    showToast("定位点不在路径经过楼层，请重新规划路径！");
                } else if (GeometryEngine.distance(BRTConvert.toPoint(locationPoint), BRTConvert.toPoint(nearestPoint)) > 3) {
                    //  位置偏差大于3米，提示重新规划路径；
                    showToast("定位点和路径偏差过大，请重新规划路径！");
                } else {
                    //  将定位点设置为路径上最近点，达到路径吸附效果；
                    locationPoint = nearestPoint;
                    //  更新路径提示信息
                    updateRouteHint(locationPoint);
                }
            }
        }
        mapView.setLocation(locationPoint);
    }

    private void updateRouteHint(BRTPoint lp) {
        BRTRouteResult routeResult = mapView.getRouteResult();
        BRTRoutePart part = routeResult.getNearestRoutePart(lp);
        if (part != null) {
            List<BRTDirectionalHint> hints = part.getRouteDirectionalHint();
            BRTDirectionalHint hint = part.getDirectionalHintForLocationFromHints(lp, hints);
            if (hint != null) {
                tvHint.setText("方向：" + hint.getDirectionString() + hint.getRelativeDirection()
                        + "\n本段长度：" + String.format("%.2f", hint.getLength())
                        + "\n本段角度：" + String.format("%.2f", hint.getCurrentAngle())
                        + "\n剩余/全长：" + String.format("%.2f", routeResult.distanceToRouteEnd(lp))
                        + "/" + String.format("%.2f", routeResult.length));
            }
        }
    }

}
