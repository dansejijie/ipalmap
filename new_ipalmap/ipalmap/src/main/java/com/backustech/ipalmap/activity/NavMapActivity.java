package com.backustech.ipalmap.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
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
import com.backustech.ipalmap.R;
import com.backustech.ipalmap.adapter.IpalmapSearchAdapter;
import com.backustech.ipalmap.utils.Constants;
import com.palmap.gl.geometry.Coordinate;
import com.palmap.gl.gestures.event.OnLongPressListener;
import com.palmap.gl.gestures.event.OnSingleTapListener;
import com.palmap.gl.maps.Palmap;
import com.palmap.gl.maps.listener.OnMapReadyListener;
import com.palmap.gl.maps.listener.OnPalmapErrorListener;
import com.palmap.gl.maps.listener.OnRouteRequestListener;
import com.palmap.gl.model.Feature;
import com.palmap.gl.navigation.entity.ActionState;
import com.palmap.gl.navigation.entity.NaviInfo;
import com.palmap.gl.navigation.entity.NodeInfo;
import com.palmap.gl.navigation.exception.NavigateException;
import com.palmap.gl.navigation.listener.OnNavigateUpdateListener;
import com.palmap.gl.overlay.impl.SimpleImageMarker;
import com.palmap.gl.view.MapView;
import com.palmap.gl.view.event.OnMapRotateChangedListener;
import com.palmap.gl.widget.impl.MapUIController;
import com.palmap.positionsdk.positioning.PalmapPositioning;
import com.palmap.positionsdk.positioning.Position;
import com.palmap.positionsdk.positioning.options.PalmapPositioningOptions;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by tygzx on 2018/6/1.
 */
//// TODO: 2018/6/5
// 1、导航结束后的一个回调，
// 2、设置地图俯视角的函数


public class NavMapActivity extends Activity implements View.OnClickListener, SensorEventListener {


    private static final String TAG = NavMapActivity.class.getSimpleName();

    Palmap palmap;
    MapView mapView;

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
    boolean hasNavRoad;//在该状态下，有规划路线的话，back第一次按时取消路线，第二次按时隐藏起始末尾组件

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

    IpalmapSearchAdapter mAdapter;

    Boolean mapLoaded;

    RelativeLayout rl_control_container;
    ProgressBar pb_Bar;

    String startName;
    String endName;//终点名字

    Bundle mData;
    Timer timer;
    TimerTask timerTask;

    boolean isMoniNaving;
    boolean isFirstArrived;//用来做导航结束后的回调标志
    boolean isRestartNaving;//当偏离导航时 ，重新请求导航路线

    SimpleImageMarker startMarker, endMarker;
    Position locationPosition;

    List<Feature> featureList;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_ipalmap_nav);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.ipalmap_header_bar);
        mData = getIntent().getBundleExtra("map_info");
        initPositioning();
        initView();
        initSensor();
    }

    private void initSensor() {

        SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void initPositioning() {

        PalmapPositioning.get().init(this);
        PalmapPositioningOptions options = new PalmapPositioningOptions();

        options.setPort(Integer.valueOf(mData.getString("port")));
        PalmapPositioning.get().addOpitions(options);
        PalmapPositioning.get().startBlePosition();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (isMoniNaving) {
                    return;
                }
                final Position position = PalmapPositioning.get().getResult();
                if (position.getX() <= 0) {
                    return;
                }
                locationPosition = position;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isMoniNaving) {
                            return;
                        }
                        if (mapView.checkInitCompleted()) {
                            palmap.updateLocation(position.getFloorId(), false, position.getX(), position.getY());
                        }
                    }
                });
            }
        };
        timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 0, 500);
    }

    private void initView() {
        initHeaderView();
        initControlView();
        initMapView();
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
//        v_zoom_out = findViewById(R.id.ipalmap_zoom_out);
//        v_zoom_in = findViewById(R.id.ipalmap_zoom_in);
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
                Feature feature = featureList.get(position);
                startName = feature.getDisplay();
                markStartLocation(new Coordinate(feature.getCenter().x, feature.getCenter().y));
            }
        });

        btn_search.setOnClickListener(this);
        fl_bottom_container.setOnClickListener(this);
        ll_listview_container.setOnClickListener(this);
        ll_floordown_container.setOnClickListener(this);
        ll_floorup_container.setOnClickListener(this);

        btn_nav_moni.setOnClickListener(this);
        btn_nav_start.setOnClickListener(this);

//        v_zoom_out.setOnClickListener(this);
//        v_zoom_in.setOnClickListener(this);
        ib_naving.setOnClickListener(this);
    }

    private void initHeaderView() {
        fl_header_back = (LinearLayout) findViewById(R.id.ipalmap_header_bar_back);
        ll_header_nav = (LinearLayout) findViewById(R.id.ipalmap_header_bar_nav);
        ll_header_nav.setVisibility(View.GONE);
        fl_header_back.setOnClickListener(this);
    }

    private void initMapView() {
        palmap = findViewById(R.id.ipalmap_map);
        //设置地图引擎启动监听(MapEngine.start(Context var0, String var1)的监听)
        palmap.setOnMapReadyListener(new OnMapReadyListener() {
            @Override
            public void onMapReady(MapView mapView) {
                mapLoaded = true;
                palmap.loadMap(mData.getString("mapid"));
                palmap.postOnAnimationDelayed(new Runnable() {
                    @Override
                    public void run() {
                        renderMark();
                    }
                }, 1500);
            }
        });
        //设置地图加载错误回调
        palmap.setOnPalmapErrorListener(new OnPalmapErrorListener() {
            @Override
            public void onRequestMapDataFailure(Exception e) {
                Toast.makeText(NavMapActivity.this, "Load map failure : " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        mapView = palmap.getMapView();

        palmap.setOnRouteRequestListener(new OnRouteRequestListener() {
            @Override
            public void onRequestSucceed() {
                pb_Bar.setVisibility(View.GONE);
                hasNavRoad = true;
                if(isRestartNaving){
                    isRestartNaving=false;
                    //偏离只出现在实际导航上
                    palmap.getNavigateManager().start(false, 1, 1000);
                }
            }

            @Override
            public void onRequestFailed(NavigateException e) {
                Toast.makeText(NavMapActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                hasNavRoad = false;
            }
        });


        palmap.getNavigateManager().addNavigateUpdateListener(new OnNavigateUpdateListener() {
            @Override
            public void onNavigateUpdate(NaviInfo naviInfo) {
                //此处可以获取导航回调信息
                if (naviInfo != null) {
                    tv_naving_middle.setText(naviInfo.getNaviTip());

                    if(true){
                        if(naviInfo.getDistance()>5){

                            palmap.getNavigateManager().stop();
                            //清除导航线
                            palmap.clearNavigateRoute();
                            //清除导航数据
                            palmap.getNavigateManager().clear();

                            isRestartNaving=true ;

                            Coordinate coord=new Coordinate(locationPosition.getX(), locationPosition.getY());
                            if (startMarker == null) {
                                startMarker = new SimpleImageMarker(NavMapActivity.this, coord, mapView.getCurrentFloorId());
                                startMarker.setImageResource(R.drawable.ipalmap_start);
                                mapView.addOverlay(startMarker);
                            } else {
                                startMarker.coordinate(new Coordinate(coord.x, coord.y));
                                mapView.refreshOverlay();
                            }
                            pb_Bar.setVisibility(View.VISIBLE);
                            palmap.requestRoute(
                                    startMarker.getGeoCoordinate().x, startMarker.getGeoCoordinate().y, startMarker.getFloorId(),
                                    endMarker.getGeoCoordinate().x, endMarker.getGeoCoordinate().y, endMarker.getFloorId());
                        }
                    }
                    else if (naviInfo.getNextAction() == ActionState.ACTION_ARRIVE && naviInfo.getTotalRemainLength() < 0.3 && !isFirstArrived) {
                        isFirstArrived = true;
                        Toast.makeText(NavMapActivity.this, "您已到达目的地", Toast.LENGTH_LONG).show();
                        if (!isMoniNaving) {
                            tv_naving_middle.postOnAnimationDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    finish();
                                }
                            }, 2000);
                        }
                        stopNaving();
                    }
                }


            }

            @Override
            public void onMockPosition(NodeInfo nodeInfo) {
                //此处可以获取模拟定位数据，不需要传入给NavigateManager
            }

            @Override
            public void onPauseNavi() {
                Toast.makeText(NavMapActivity.this, "导航暂停", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onResumeNavi() {
                Toast.makeText(NavMapActivity.this, "导航恢复", Toast.LENGTH_LONG).show();
            }


        });

        mapView.setOnSingleTapListener(new OnSingleTapListener() {
            @Override
            public void onSingleTap(final MapView mapView, float v, float v1) {

                if (palmap.getNavigateManager().isNavigating()) {
                    return;
                }
                final Coordinate coord = mapView.screenCoordinate2WorldCoordinate(v, v1);
                List<Feature> selectFeatures = mapView.searchFeaturesByWorldCoordinate(coord);
                if (selectFeatures == null || selectFeatures.isEmpty()) {
                    return;
                }

                final String name = selectFeatures.get(0).getDisplay();
                if (name == null) {
                    return;
                }
                startName = name;

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
                                        markStartLocation(coord);
                                    }
                                });
                            }
                        }).show();

            }


        });

        mapView.setOnLongPressListener(new OnLongPressListener() {
            @Override
            public void onLongPress(MapView mapView, float v, float v1) {

            }
        });

        MapUIController mapUIController = new MapUIController();
        mapUIController.enableCompass(false);

        mapView.setMapUIController(mapUIController);

        mapView.setOnMapRotateChangedListener(new OnMapRotateChangedListener() {
            @Override
            public void onMapRotateChanged(double v) {
                iv_compass.setRotation((float) v);
            }
        });
    }

    private void renderMark() {

        Coordinate coord = new Coordinate(mData.getDouble("map_x_value"), mData.getDouble("map_y_value"));
        endMarker = new SimpleImageMarker(NavMapActivity.this, coord, mapView.getCurrentFloorId());
        endMarker.setImageResource(R.drawable.ipalmap_end);
        mapView.addOverlay(endMarker);

        List<Feature> features = mapView.searchFeaturesByWorldCoordinate(coord);
        if (features == null || features.isEmpty()) {
            Toast.makeText(this, "当前层没有找到符合条件的POI", Toast.LENGTH_SHORT).show();
            return;
        }
        mapView.resetAllRendererColor();
        //修改目标Poi背景颜色
        mapView.updateRendererColor(features.get(0).getId(), Color.BLUE);

        endName = features.get(0).getDisplay();
    }

    private void markStartLocation(Coordinate coord) {
        if (startMarker == null) {
            startMarker = new SimpleImageMarker(NavMapActivity.this, coord, mapView.getCurrentFloorId());
            startMarker.setImageResource(R.drawable.ipalmap_start);
            mapView.addOverlay(startMarker);
        } else {
            startMarker.coordinate(new Coordinate(coord.x, coord.y));
            mapView.refreshOverlay();
        }
//        //清除导航线
        palmap.clearNavigateRoute();
        //清除导航数据
        palmap.getNavigateManager().clear();

        pb_Bar.setVisibility(View.VISIBLE);
        palmap.requestRoute(
                startMarker.getGeoCoordinate().x, startMarker.getGeoCoordinate().y, startMarker.getFloorId(),
                endMarker.getGeoCoordinate().x, endMarker.getGeoCoordinate().y, endMarker.getFloorId());
        showStartEndView();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (palmap != null) {
            palmap.onResume();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (palmap != null) {
            palmap.onPause();
        }

        stopNaving();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (palmap != null) {
            palmap.onDestroy();
        }
        timer.cancel();
        PalmapPositioning.get().stopBlePositioning();
        PalmapPositioning.get().release();

        SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sm.unregisterListener(this);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (palmap != null) {
            palmap.onLowMemory();
        }
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

        String content = et_search.getText().toString();
        if (TextUtils.isEmpty(content)) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        et_search.setText("");
        et_search.clearFocus();

        featureList = mapView.searchFeature("display", content);
        if (featureList == null || featureList.isEmpty()) {
            Toast.makeText(this, "当前层没有找到符合条件的POI", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mAdapter == null) {
            mAdapter = new IpalmapSearchAdapter(NavMapActivity.this, featureList);
            lv_search.setAdapter(mAdapter);
        } else {
            mAdapter.setData(featureList);
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
//        else if (id == R.id.ipalmap_zoom_out) {
//            //地图放大
//            mapView.zoomIn();
//        } else if (id == R.id.ipalmap_zoom_in) {
//            //地图缩小
//            mapView.zoomOut();
//        }
        else if (id == R.id.ipalmap_nav_ib_naving) {
            if (locationPosition != null) {
                markStartLocation(new Coordinate(locationPosition.getX(), locationPosition.getY()));
                startName = "定位点";
            } else {
                Toast.makeText(NavMapActivity.this, "获取定位点失败", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.ipalmap_nav_btn_moni) {
            if(!hasNavRoad){
                return;
            }
            isMoniNaving = true;
            isFirstArrived = false;
            palmap.postOnAnimationDelayed(new Runnable() {
                @Override
                public void run() {
                    palmap.getNavigateManager().start(true, 1, 1000);
                    showNavingView();
                }
            }, 500);

        } else if (id == R.id.ipalmap_nav_btn_start) {
            if(!hasNavRoad){
                return;
            }
            if (locationPosition != null) {
                isFirstArrived = false;
                palmap.getNavigateManager().start(false, 1, 1000);
                showNavingView();
            } else {
                Toast.makeText(NavMapActivity.this, "获取定位点失败", Toast.LENGTH_SHORT).show();
            }

        } else if (id == R.id.ipalmap_nav_address_choose_back) {
            if (hasNavRoad) {
                stopNaving();
            } else {
                showInitView();
            }
        } else if (id == R.id.ipalmap_naving_back) {
            stopNaving();
        }
    }

    private void stopNaving() {

        palmap.getNavigateManager().stop();
        //清除导航线
        palmap.clearNavigateRoute();
        //清除导航数据
        palmap.getNavigateManager().clear();

        hasNavRoad = false;
        isMoniNaving = false;
        if (startMarker != null) {
            startMarker.coordinate(new Coordinate(0, 0));
            mapView.refreshOverlay();
        }

        showInitView();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!mapView.checkInitCompleted()) {
            return;
        }
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            float value = event.values[0];
            palmap.updatemAzimuth(value - (float) mapView.getRotate()+180);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
