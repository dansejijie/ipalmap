package com.brtbeacon.map3d.demo.map.route;

import android.os.Bundle;
import android.os.Handler;
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

import com.brtbeacon.map3d.demo.R;
import com.brtbeacon.map3d.demo.activity.BaseMapActivity;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

import java.util.List;

public class RouteHintActivity extends BaseMapActivity {

    private BRTPoint startPoint;
    private BRTPoint endPoint;

    private BRTMapRouteManager routeManager = null;
    private TextView tvHint;
    private Button btnNavSim;
    private int pointIndex = 0;
    private Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tvHint = findViewById(R.id.tv_hint);
        btnNavSim = findViewById(R.id.btn_nav_sim);
        btnNavSim.setOnClickListener(this);
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_route_hint;
    }

    @Override
    public void onClickAtPoint(BRTMapView mapView, BRTPoint point) {
        super.onClickAtPoint(mapView, point);

        if (startPoint != null && endPoint != null) {
            startPoint = null;
            endPoint = null;
            //mapView.setRouteResult(null);
        } else if (startPoint == null) {
            startPoint = point;
        } else if (endPoint == null) {
            endPoint = point;
        }
        mapView.setRouteStart(startPoint);
        mapView.setRouteEnd(endPoint);

        if (startPoint != null && endPoint != null) {
            routeManager.requestRoute(startPoint, endPoint);
            showToast("开始路径规划！");
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

    @Override
    public void onClick(View view) {
        super.onClick(view);
        switch (view.getId()) {
            case R.id.btn_nav_sim: {
                if (mapView.getRouteResult() == null)
                    return;

                BRTRoutePart part = mapView.getRouteResult().getAllRouteParts().get(0);
                Point firstPoint = part.getFirstPoint();
                BRTPoint point = new BRTPoint(part.getMapInfo().getFloorNumber(), firstPoint.latitude(), firstPoint.longitude());
                showHints(point);

                btnNavSim.setEnabled(false);
                break;
            }
        }
    }

    private void showHints(BRTPoint lp) {

        BRTRouteResult routeResult = mapView.getRouteResult();

        //新建、更新指示图标位置
        mapView.setLocation(lp);

        if (mapView.getRouteResult().distanceToRouteEnd(lp) < 0.5) {
            //已到达目的地
            btnNavSim.setEnabled(true);
            showToast("已经到达终点！");
            return;
        }

        //以下代码仅用于模拟整条线路点位移动，实际场景可直接使用定位回调
        //取出本段路线上各个点
        BRTRoutePart part = routeResult.getNearestRoutePart(lp);
        if (part == null) {
            return;
        }
        LineString line = part.getRoute();
        Point pt = line.coordinates().get(pointIndex);
        pointIndex++;
        int floor = part.getMapInfo().getFloorNumber();
        //是否为本段结束点
        if (pt.equals(part.getLastPoint())) {
            pointIndex = 0;
            //是否为终段
            if (part.isLastPart()) {
                btnNavSim.setEnabled(true);
            } else {
                //取下一段路线起点
                BRTRoutePart nextPart = part.getNextPart();
                pt = nextPart.getFirstPoint();
                floor = nextPart.getMapInfo().getFloorNumber();
                if (floor != mapView.getCurrentFloor().getFloorNumber()) {
                    mapView.setFloor(nextPart.getMapInfo());
                }
            }
        }
        BRTPoint localPoint = new BRTPoint(floor, pt.latitude(), pt.longitude());
        animateUpdateGraphic(0, lp, localPoint);

        List<BRTDirectionalHint> hints = part.getRouteDirectionalHint();
        BRTDirectionalHint hint = part.getDirectionalHintForLocationFromHints(localPoint, hints);
        if (hint != null) {
            mapView.setRotation((float)hint.getCurrentAngle());
        }
    }

    private void showCurrentHint(BRTPoint lp) {
        mapView.setLocation(lp);
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


    private void animateUpdateGraphic(final double offset, final BRTPoint lp1, final BRTPoint lp2) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isFinishing())
                    return;
                double distance = GeometryEngine.distance(lp1.getPoint(), lp2.getPoint());
                if (distance > 0 && offset<distance) {
                    Point tmp = getPointWithLengthAndOffset(lp1, lp2, offset / distance);
                    //mapView.centerAt(tmp,false);
                    showCurrentHint(new BRTPoint(lp1.getFloorNumber(), tmp.latitude(), tmp.longitude()));
                    animateUpdateGraphic(offset + 10.0, lp1, lp2);
                }else {
                    showCurrentHint(lp2);
                    showHints(lp2);
                }

            }
        }, 250);
    }

    private Point getPointWithLengthAndOffset(BRTPoint start, BRTPoint end, double per) {
        double scale = per;

        double x = start.getLatitude() * (1 - scale) + end.getLatitude() * scale;
        double y = start.getLongitude() * (1 - scale) + end.getLongitude() * scale;

        return Point.fromLngLat(y, x);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
