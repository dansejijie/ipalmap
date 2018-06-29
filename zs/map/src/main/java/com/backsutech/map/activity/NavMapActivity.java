package com.backsutech.map.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.backsutech.map.R;
import com.backsutech.map.adapter.IpalmapSearchAdapter;
import com.brtbeacon.locationengine.ble.BRTBeacon;
import com.brtbeacon.locationengine.ble.BRTLocationManager;
import com.brtbeacon.locationengine.ble.BRTPublicBeacon;
import com.brtbeacon.map.map3d.BRTMapEnvironment;
import com.brtbeacon.map.map3d.BRTMapView;
import com.brtbeacon.map.map3d.entity.BRTFloorInfo;
import com.brtbeacon.map.map3d.entity.BRTPoi;
import com.brtbeacon.map.map3d.entity.BRTPoiEntity;
import com.brtbeacon.map.map3d.entity.BRTPoint;
import com.brtbeacon.map.map3d.route.BRTDirectionalHint;
import com.brtbeacon.map.map3d.route.BRTMapRouteManager;
import com.brtbeacon.map.map3d.route.BRTRoutePart;
import com.brtbeacon.map.map3d.route.BRTRouteResult;
import com.brtbeacon.map.map3d.route.GeometryEngine;
import com.brtbeacon.map.map3d.utils.BRTConvert;
import com.brtbeacon.map.map3d.utils.BRTSearchAdapter;
import com.brtbeacon.mapdata.BRTLocalPoint;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.widgets.CompassView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NavMapActivity extends Activity implements View.OnClickListener, BRTMapView.BRTMapViewListener{


    private static final String TAG = NavMapActivity.class.getSimpleName();

    BRTMapView mapView;
    BRTMapRouteManager routeManager = null;
    BRTPoint startPoint,endPoint=null;
    BRTSearchAdapter searchAdapter;
    List<BRTPoiEntity> entityList=new ArrayList<>();
    IpalmapSearchAdapter mAdapter;
    private BRTLocationManager locationManager;
    private int pointIndex = 0;
    //模拟导航定时更新
    Handler mockHandler = new Handler();

    //标题栏
    LinearLayout fl_header_back;
    LinearLayout ll_header_nav;

    //搜索相关组件
    LinearLayout ll_search_container;
    EditText et_search;
    Button btn_search;

    //底部搜索内容相关组件
    FrameLayout fl_bottom_container;
    LinearLayout ll_listview_container;
    LinearLayout ll_floordown_container;
    LinearLayout ll_floorup_container;
    ListView lv_search;

    //顶部 导航显示的起始末尾组件
    LinearLayout ll_nav_container;
    LinearLayout ll_nav_back;
    TextView tv_start;
    TextView tv_end;
    TextView tv_floor;

    //顶部 导航正在行进中的起始末尾组件
    LinearLayout ll_naving_container;
    ImageButton ib_naving_back;
    TextView tv_naving_start;
    TextView tv_naving_end;
    TextView tv_naving_middle;

    //底部 导航显示的按钮
    LinearLayout ll_nav_button_container;
    Button btn_nav_moni;
    Button btn_nav_start;

    ImageView iv_compass;

    //放大缩小
    View v_zoom_out;
    View v_zoom_in;

    //导航按钮
    ImageButton ib_naving;



    Boolean mapLoaded;

    RelativeLayout rl_control_container;
    ProgressBar pb_Bar;

    String startName;
    String endName;//终点名字

    Bundle mData;
    TimerTask timerTask;

    boolean isMoniNaving;
    boolean isFirstArrived;//用来做导航结束后的回调标志
    boolean isRestartNaving;//当偏离导航时 ，重新请求导航路线

    BRTPoint locationPosition;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BRTMapEnvironment.initMapEnvironment(this);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_map_nav);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.ipalmap_header_bar);
        mData = getIntent().getBundleExtra("map_info");
        initMapView(savedInstanceState);
        initView();
        checkBluetooth();
    }

    private void initView() {
        initHeaderView();
        initControlView();
    }

    private void initControlView() {

        ll_search_container = (LinearLayout) findViewById(R.id.ipalmap_ll_search_container);
        et_search = (EditText) findViewById(R.id.ipalmap_nav_et_search);
        btn_search = (Button) findViewById(R.id.ipalmap_nav_btn_search);
        fl_bottom_container = (FrameLayout) findViewById(R.id.ipalmap_nav_bottom_container);
        ll_listview_container = (LinearLayout) findViewById(R.id.ipalmap_nav_listview_container);
        ll_floordown_container = (LinearLayout) findViewById(R.id.ipalmap_nav_ll_floordown_container);
        ll_floorup_container = (LinearLayout) findViewById(R.id.ipalmap_nav_ll_floorup_container);
        lv_search = (ListView) findViewById(R.id.ipalmap_nav_listview);
        ll_nav_container = (LinearLayout) findViewById(R.id.ipalmap_nav_nav_container);
        ll_nav_back = (LinearLayout) findViewById(R.id.ipalmap_nav_address_choose_back);
        ll_nav_back.setOnClickListener(this);
        tv_start = (TextView) findViewById(R.id.ipalmap_nav_tv_start);
        tv_end = (TextView) findViewById(R.id.ipalmap_nav_tv_end);
        tv_floor = (TextView) findViewById(R.id.ipalmap_nav_tv_floor);
        ll_nav_button_container = (LinearLayout) findViewById(R.id.ipalmap_nav_nav_button_container);
        btn_nav_moni = (Button) findViewById(R.id.ipalmap_nav_btn_moni);
        btn_nav_start = (Button) findViewById(R.id.ipalmap_nav_btn_start);
        v_zoom_out = findViewById(R.id.ipalmap_zoom_out);
        v_zoom_in = findViewById(R.id.ipalmap_zoom_in);
        ib_naving = (ImageButton) findViewById(R.id.ipalmap_nav_ib_naving);
        ib_naving.setOnClickListener(this);
        ll_naving_container = (LinearLayout) findViewById(R.id.ipalmap_nav_naving_container);
        ib_naving_back = (ImageButton) findViewById(R.id.ipalmap_naving_back);
        ib_naving_back.setOnClickListener(this);
        tv_naving_start = (TextView) findViewById(R.id.ipalmap_nav_tv_naving_start);
        tv_naving_end = (TextView) findViewById(R.id.ipalmap_nav_tv_naving_end);
        tv_naving_middle = (TextView) findViewById(R.id.ipalmap_nav_tv_naving);

        iv_compass = findViewById(R.id.ipalmap_nav_iv_compass);

        rl_control_container = (RelativeLayout) findViewById(R.id.ipalmap_map_control_container);

        pb_Bar=findViewById(R.id.ipalmap_nav_progress);

        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 0) {
                    showInitView();
                }
            }
        });
        lv_search.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BRTPoiEntity entity = entityList.get(position);
                markStartLocation(new BRTPoint(entity.getFloorNumber(),entity.getLatLng().getLatitude(),entity.getLatLng().getLongitude()),"定位点");

            }
        });

        btn_search.setOnClickListener(this);
        fl_bottom_container.setOnClickListener(this);
        ll_listview_container.setOnClickListener(this);
        ll_floordown_container.setOnClickListener(this);
        ll_floorup_container.setOnClickListener(this);

        btn_nav_moni.setOnClickListener(this);
        btn_nav_start.setOnClickListener(this);

        v_zoom_out.setOnClickListener(this);
        v_zoom_in.setOnClickListener(this);
        ib_naving.setOnClickListener(this);
    }

    private void initHeaderView() {
        fl_header_back = (LinearLayout) findViewById(R.id.ipalmap_header_bar_back);
        ll_header_nav = (LinearLayout) findViewById(R.id.ipalmap_header_bar_nav);
        ll_header_nav.setVisibility(View.GONE);
        fl_header_back.setOnClickListener(this);
    }

    private void initMapView(Bundle savedInstanceState) {

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.addMapListener(this);

        mapView.init(mData.getString("mapid"), mData.getString("appkey"), BRTMapView.MAP_LOAD_MODE_OFFLINE);
        searchAdapter = new BRTSearchAdapter(mData.getString("mapid"));
    }


    private void markStartLocation(BRTPoint point,String name) {
        startPoint=point;
        startName=name;
        mapView.setRouteStart(point);
        routeManager.requestRoute(startPoint, endPoint);
        showStartEndView();
    }


    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();

        stopNaving();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        mockHandler.removeCallbacksAndMessages(null);
        locationManager.stopUpdateLocation();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    private void showInitView() {

        ll_naving_container.setVisibility(View.GONE);
        ll_nav_container.setVisibility(View.GONE);
        fl_bottom_container.setVisibility(View.GONE);
        ll_search_container.setVisibility(View.VISIBLE);
        ib_naving.setVisibility(View.VISIBLE);
    }

    private void showSearchResultView() {

        ll_naving_container.setVisibility(View.GONE);
        ll_nav_container.setVisibility(View.GONE);
        ll_nav_button_container.setVisibility(View.GONE);

        ll_search_container.setVisibility(View.VISIBLE);
        fl_bottom_container.setVisibility(View.VISIBLE);
        ll_listview_container.setVisibility(View.VISIBLE);
        lv_search.setVisibility(View.VISIBLE);
        ll_floordown_container.setVisibility(View.VISIBLE);
        ib_naving.setVisibility(View.VISIBLE);
        ll_floorup_container.setVisibility(View.GONE);

    }

    private void showSearchHideResultView() {
        ll_naving_container.setVisibility(View.GONE);
        ll_nav_container.setVisibility(View.GONE);

        ll_search_container.setVisibility(View.VISIBLE);
        fl_bottom_container.setVisibility(View.VISIBLE);
        ll_listview_container.setVisibility(View.VISIBLE);
        ib_naving.setVisibility(View.VISIBLE);
        lv_search.setVisibility(View.GONE);
        ll_floordown_container.setVisibility(View.GONE);
        ll_floorup_container.setVisibility(View.VISIBLE);
    }

    private void showStartEndView() {

        ll_search_container.setVisibility(View.GONE);
        ll_listview_container.setVisibility(View.GONE);
        ll_naving_container.setVisibility(View.GONE);
        ib_naving.setVisibility(View.GONE);

        ll_nav_container.setVisibility(View.VISIBLE);
        fl_bottom_container.setVisibility(View.VISIBLE);
        ll_nav_button_container.setVisibility(View.VISIBLE);

        tv_start.setText(startName);
        tv_end.setText(endName);

    }

    private void showNavingView() {

        ll_search_container.setVisibility(View.GONE);
        ll_nav_container.setVisibility(View.GONE);
        fl_bottom_container.setVisibility(View.GONE);
        ll_listview_container.setVisibility(View.GONE);
        lv_search.setVisibility(View.GONE);
        ll_floordown_container.setVisibility(View.GONE);
        ll_floorup_container.setVisibility(View.GONE);

        ll_naving_container.setVisibility(View.VISIBLE);
        ib_naving.setVisibility(View.GONE);

        tv_naving_start.setText("起点:" + startName);
        tv_naving_end.setText("目的地:" + endName);
    }


    private void search() {
        if(!mapLoaded){
            return;
        }
        String content = et_search.getText().toString();
        if (TextUtils.isEmpty(content)) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        et_search.setText("");
        et_search.clearFocus();
        entityList = searchAdapter.queryPoi(content);
        if (entityList == null || entityList.isEmpty()) {
            Toast.makeText(this, "当前层没有找到符合条件的POI", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mAdapter == null) {
            mAdapter = new IpalmapSearchAdapter(NavMapActivity.this, entityList);
            lv_search.setAdapter(mAdapter);
        } else {
            mAdapter.setData(entityList);
        }
        showSearchResultView();
    }


    @Override
    public void onClick(View v) {

        int id = v.getId();
        if (id == R.id.ipalmap_header_bar_back) {
            finish();
        } else if (id == R.id.ipalmap_nav_btn_search) {
            search();
        } else if (id == R.id.ipalmap_nav_ll_floordown_container) {
            showSearchHideResultView();
        } else if (id == R.id.ipalmap_nav_ll_floorup_container) {
            showSearchResultView();
        }
        else if (id == R.id.ipalmap_zoom_out) {
            //地图放大
            mapView.setZoomIn();
        } else if (id == R.id.ipalmap_zoom_in) {
            //地图缩小
            mapView.setZoomOut();
        }
        else if (id == R.id.ipalmap_nav_ib_naving) {
            checkBluetooth();
            if (locationPosition != null) {
                markStartLocation(locationPosition,"定位点");
            } else {
                Toast.makeText(NavMapActivity.this, "获取定位点失败", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.ipalmap_nav_btn_moni) {
            if (mapView.getRouteResult() == null)
                return;
            locationManager.stopUpdateLocation();
            showNavingView();
            isFirstArrived = false;
            isMoniNaving = true;
            BRTRoutePart part = mapView.getRouteResult().getAllRouteParts().get(0);
            Point firstPoint = part.getFirstPoint();
            BRTPoint point = new BRTPoint(part.getMapInfo().getFloorNumber(), firstPoint.latitude(), firstPoint.longitude());
            handleNavLocation(point);
        } else if (id == R.id.ipalmap_nav_btn_start) {
            if (mapView.getRouteResult() == null)
                return;
            checkBluetooth();
            if(locationPosition==null){
                Toast.makeText(NavMapActivity.this, "获取定位点失败", Toast.LENGTH_SHORT).show();
            }else{
                isMoniNaving = false;
                isFirstArrived = false;
                showNavingView();
                handleNavLocation(locationPosition);
            }
        } else if (id == R.id.ipalmap_nav_address_choose_back) {
            if (mapView.getRouteResult()!=null) {
                stopNaving();
            } else {
                showInitView();
            }
        } else if (id == R.id.ipalmap_naving_back) {
            stopNaving();
        }
    }

    private void stopNaving() {
        startPoint=null;
        mapView.setLocation(null);
        mapView.setRouteStart(null);
        mapView.setRouteResult(null);
        pointIndex=0;
        isMoniNaving = false;
        showInitView();
    }


    @Override
    public void mapViewDidLoad(BRTMapView brtMapView, Error error) {
        if(error!=null){
            Toast.makeText(NavMapActivity.this, "Load map failure : " + error.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }
        mapView.setFloor(mapView.getFloorList().get(0));
        routeManager = new BRTMapRouteManager(mapView.getBuilding(), mapView.getFloorList());
        routeManager.addRouteManagerListener(routeManagerListener);

        locationManager = new BRTLocationManager(this, mData.getString("mapid"), mData.getString("appkey"));
        locationManager.addLocationEngineListener(locationManagerListener);
        locationManager.setLimitBeaconNumber(true);
        locationManager.setMaxBeaconNumberForProcessing(5);
        locationManager.setRssiThreshold(-75);
        locationManager.startUpdateLocation();
    }

    @Override
    public void onFinishLoadingFloor(BRTMapView brtMapView, BRTFloorInfo brtFloorInfo) {
        mapLoaded = true;
        endPoint=new BRTPoint(brtFloorInfo.getFloorNumber(),mData.getDouble("map_x_value"),mData.getDouble("map_y_value"));
        mapView.setRouteEnd(endPoint);

        CompassView compassView = mapView.getCompassView();
        FrameLayout.LayoutParams layoutParams=new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin=300;
        layoutParams.leftMargin=20;
        compassView.setLayoutParams(layoutParams);


    }

    @Override
    public void onClickAtPoint(BRTMapView brtMapView, BRTPoint brtPoint) {

    }

    @Override
    public void onPoiSelected(BRTMapView brtMapView, final List<BRTPoi> points) {
        if (points.isEmpty())
            return;
        new android.app.AlertDialog.Builder(NavMapActivity.this)
                .setTitle("是否将该点设为起点")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                markStartLocation(points.get(0).getPoint(),points.get(0).getName());
                            }
                        });
                    }
                }).show();

    }

    private BRTMapRouteManager.BRTRouteManagerListener routeManagerListener = new BRTMapRouteManager.BRTRouteManagerListener() {

        @Override
        public void didSolveRouteWithResult(BRTMapRouteManager routeManager, final BRTRouteResult routeResult) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mapView.setRouteResult(routeResult);
                }
            });
        }

        @Override
        public void didFailSolveRouteWithError(BRTMapRouteManager routeManager, final BRTMapRouteManager.BRTRouteException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(NavMapActivity.this, "Load map failure : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    mapView.setRouteResult(null);
                }
            });
        }
    };



    private Point getPointWithLengthAndOffset(BRTPoint start, BRTPoint end, double per) {
        double scale = per;

        double x = start.getLatitude() * (1 - scale) + end.getLatitude() * scale;
        double y = start.getLongitude() * (1 - scale) + end.getLongitude() * scale;

        return Point.fromLngLat(y, x);
    }

    private void checkBluetooth() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            Toast.makeText(NavMapActivity.this,"当前设备没有蓝牙，无法进行定位操作！",Toast.LENGTH_SHORT).show();
            return;
        }

        if (!adapter.isEnabled()) {
            adapter.enable();
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
                    locationPosition = new BRTPoint(tyLocalPoint.getFloor(), pointLL.getLatitude(), pointLL.getLongitude());
                    handleNavLocation(locationPosition);
                }
            });
        }

        @Override
        public void didUpdateLocation(BRTLocationManager BRTLocationManager, BRTLocalPoint tyLocalPoint) {
            System.out.println("didUpdateLocation");
        }
    };


    private void handleNavLocation(final BRTPoint point){
        //新建、更新指示图标位置
        mapView.setLocation(point);
        if (mapView.getCurrentFloor().getFloorNumber() != point.getFloorNumber()) {
            mapView.setFloorByNumber(point.getFloorNumber());
        }

        //以下是导航时处理的函数
        BRTRouteResult brtRouteResult=mapView.getRouteResult();
        if(brtRouteResult!=null){

            //判断是否到终点
            if (mapView.getRouteResult().distanceToRouteEnd(point) < 0.5) {
                Toast.makeText(NavMapActivity.this, "您已到达目的地", Toast.LENGTH_LONG).show();
                if (!isMoniNaving) {
                    tv_naving_middle.postOnAnimationDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 2000);
                }else {

                    locationManager.startUpdateLocation();
                }
                stopNaving();
                return;
            }

            BRTRoutePart part = brtRouteResult.getNearestRoutePart(point);
            if (part != null) {
                List<BRTDirectionalHint> hints = part.getRouteDirectionalHint();
                BRTDirectionalHint hint = part.getDirectionalHintForLocationFromHints(point, hints);
                if (hint != null) {
                    tv_naving_middle.setText(hint.getLandmarkString());
                }
            }

        }


        //以下代码仅用于模拟整条线路点位移动，实际场景可直接使用定位回调
        //取出本段路线上各个点
        if(isMoniNaving){
            BRTRoutePart part = brtRouteResult.getNearestRoutePart(point);
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
                if (!part.isLastPart()) {
                    //取下一段路线起点
                    BRTRoutePart nextPart = part.getNextPart();
                    pt = nextPart.getFirstPoint();
                    floor = nextPart.getMapInfo().getFloorNumber();
                    if (floor != mapView.getCurrentFloor().getFloorNumber()) {
                        mapView.setFloor(nextPart.getMapInfo());
                    }
                }
            }
            final BRTPoint localPoint = new BRTPoint(floor, pt.latitude(), pt.longitude());
            animateUpdateGraphic(0, point, localPoint);
        }
    }

    private void animateUpdateGraphic(final double offset, final BRTPoint lp1, final BRTPoint lp2) {
        mockHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isFinishing())
                    return;
                double distance = GeometryEngine.distance(lp1.getPoint(), lp2.getPoint());
                if (distance > 0 && offset<distance) {
                    Point tmp = getPointWithLengthAndOffset(lp1, lp2, offset / distance);
                    //mapView.centerAt(tmp,false);
                    animateUpdateGraphic(offset + 10.0, lp1, lp2);
                }else {
                    if(mapView.getRouteResult()!=null){
                        handleNavLocation(lp2);
                    }
                }

            }
        }, 250);
    }

    static {
        System.loadLibrary("BRTLocationEngine");
    }
}
