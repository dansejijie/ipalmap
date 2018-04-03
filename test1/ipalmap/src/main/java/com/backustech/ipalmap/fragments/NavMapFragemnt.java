package com.backustech.ipalmap.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.backustech.ipalmap.R;
import com.backustech.ipalmap.activity.IpalmapActivity;
import com.backustech.ipalmap.adapter.IpalmapSearchAdapter;
import com.backustech.ipalmap.utils.Constant;
import com.palmaplus.nagrand.core.Types;
import com.palmaplus.nagrand.data.DataSource;
import com.palmaplus.nagrand.data.Feature;
import com.palmaplus.nagrand.data.FeatureCollection;
import com.palmaplus.nagrand.data.LocationModel;
import com.palmaplus.nagrand.data.LocationPagingList;
import com.palmaplus.nagrand.data.PlanarGraph;
import com.palmaplus.nagrand.easyapi.LBSManager;
import com.palmaplus.nagrand.easyapi.MockPositionManager;
import com.palmaplus.nagrand.geos.Coordinate;
import com.palmaplus.nagrand.geos.Point;
import com.palmaplus.nagrand.navigate.DynamicNavigateWrapper;
import com.palmaplus.nagrand.navigate.DynamicNavigationMode;
import com.palmaplus.nagrand.navigate.NavigateManager;
import com.palmaplus.nagrand.position.Location;
import com.palmaplus.nagrand.position.PositioningManager;
import com.palmaplus.nagrand.position.ble.BeaconPositioningManager;
import com.palmaplus.nagrand.tools.ViewUtils;
import com.palmaplus.nagrand.view.MapView;
import com.palmaplus.nagrand.view.gestures.OnSingleTapListener;
import com.palmaplus.nagrand.view.overlay.ImageOverlay;
import com.palmaplus.nagrand.view.overlay.LocationOverlay;
import com.palmaplus.nagrand.view.overlay.OverlayCell;
import com.palmaplus.nagrand.view.widgets.Compass;
import com.palmaplus.nagrand.view.widgets.Switch;

import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 * Created by tygzx on 2018/3/29.
 */

public class NavMapFragemnt extends BaseMapFragment implements View.OnClickListener{

    private LBSManager lbsManager;

    private RelativeLayout rl_control_container;

    private OverlayCell startCell;
    private ImageOverlay startOverlay;
    private ImageOverlay endOverlay;
    private String startName;
    private String endName;//终点名字

    private BeaconPositioningManager positionManager;
    private MockPositionManager mockPositionManager;

    //点击地图 弹出是否设置为起点的dialog
    private Dialog startDialog;
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

    //放大缩小
    View v_zoom_out;
    View v_zoom_in;

    //导航按钮
    ImageButton ib_naving;

    IpalmapSearchAdapter mAdapter;
    LocationPagingList mData;

    private boolean once;

    @Override
    protected View getView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.activity_ipalmap_nav, container, false);
    }

    @Override
    public void onInitFragment(Bundle savedInstanceState) {
        super.onInitFragment(savedInstanceState);

        ll_search_container = (LinearLayout) view.findViewById(R.id.ipalmap_ll_search_container);
        et_search = (EditText) view.findViewById(R.id.ipalmap_nav_et_search);
        btn_search = (Button) view.findViewById(R.id.ipalmap_nav_btn_search);
        fl_bottom_container = (FrameLayout) view.findViewById(R.id.ipalmap_nav_bottom_container);
        ll_listview_container = (LinearLayout) view.findViewById(R.id.ipalmap_nav_listview_container);
        ll_floordown_container = (LinearLayout) view.findViewById(R.id.ipalmap_nav_ll_floordown_container);
        ll_floorup_container = (LinearLayout) view.findViewById(R.id.ipalmap_nav_ll_floorup_container);
        lv_search = (ListView) view.findViewById(R.id.ipalmap_nav_listview);
        ll_nav_container = (LinearLayout) view.findViewById(R.id.ipalmap_nav_nav_container);
        ll_nav_back = (LinearLayout) view.findViewById(R.id.ipalmap_nav_address_choose_back);
        ll_nav_back.setOnClickListener(this);
        tv_start = (TextView) view.findViewById(R.id.ipalmap_nav_tv_start);
        tv_end = (TextView) view.findViewById(R.id.ipalmap_nav_tv_end);
        tv_floor = (TextView) view.findViewById(R.id.ipalmap_nav_tv_floor);
        ll_nav_button_container = (LinearLayout) view.findViewById(R.id.ipalmap_nav_nav_button_container);
        btn_nav_moni = (Button) view.findViewById(R.id.ipalmap_nav_btn_moni);
        btn_nav_start = (Button) view.findViewById(R.id.ipalmap_nav_btn_start);
        v_zoom_out = view.findViewById(R.id.ipalmap_zoom_out);
        v_zoom_in = view.findViewById(R.id.ipalmap_zoom_in);
        ib_naving = (ImageButton) view.findViewById(R.id.ipalmap_nav_ib_naving);

        ll_naving_container = (LinearLayout) view.findViewById(R.id.ipalmap_nav_naving_container);
        ib_naving_back = (ImageButton) view.findViewById(R.id.ipalmap_naving_back);
        ib_naving_back.setOnClickListener(this);
        tv_naving_start = (TextView) view.findViewById(R.id.ipalmap_nav_tv_naving_start);
        tv_naving_end = (TextView) view.findViewById(R.id.ipalmap_nav_tv_naving_end);
        tv_naving_middle = (TextView) view.findViewById(R.id.ipalmap_nav_tv_naving);

        rl_control_container= (RelativeLayout) view.findViewById(R.id.ipalmap_map_control_container);
        //rl_positionContainer= (RelativeLayout) findViewById(R.id.ipalmap_nav_position_container);

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

                LocationModel locationModel = mData.getPOI(position);
                Feature feature = map.mapView().selectFeature(LocationModel.id.get(locationModel));
                startName = LocationModel.name.get(feature);
                startOverlay.init(new double[]{feature.getCentroid().getX(), feature.getCentroid().getY()});
                startOverlay.mFloorId = map.getFloorId();
                startCell=startOverlay;
                showStartEndView();
                mapView.getOverlayController().refresh();
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
    }


    protected void initData() {

        //添加指南针
        map.setDefaultWidgetContrainer(rl_control_container);
        // 隐藏比例尺
        map.getScale().setVisibility(View.GONE);
        // 隐藏楼层切换控件
        map.getFloorLayout().setVisibility(View.GONE);

        //指南针
        Compass compass=map.getCompass();
        Bitmap compassBitmap=BitmapFactory.decodeResource(getResources(),R.drawable.icon_compass_north);
        compass.setCompassImage(compassBitmap);
        compass.setVisibility(View.VISIBLE);
        //切换按钮
        Switch swicth=map.getSwitch();
        swicth.setTextSize(12);
        swicth.setTextColor(Color.BLACK);
        swicth.setBackgroundColor(Color.WHITE);
        swicth.setBackground(getResources().getDrawable(R.drawable.ipalmap_border));
        rl_control_container.removeView(swicth);
        final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewUtils.dip2px(getContext(), 30.0F), ViewUtils.dip2px(getContext(), 30.0F));
        layoutParams.setMargins(0,ViewUtils.dip2px(getContext(), 12.0F), ViewUtils.dip2px(getContext(), 12.0F) , 0);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        rl_control_container.addView(swicth, layoutParams);


        final Bundle bundle = getArguments();
        mapView.setBackgroundColor(Color.parseColor("#c4e1ff"));

        map.addOnChangePlanarGraph(new MapView.OnChangePlanarGraph() {
            @Override
            public void onChangePlanarGraph(PlanarGraph planarGraph, PlanarGraph planarGraph1, long l, long l1) {
                //设置楼层
                endOverlay.mFloorId = map.getFloorId();
                Types.Point point=mapView.converToScreenCoordinate((float)bundle.getDouble("map_x_value"),(float)bundle.getDouble("map_y_value"));
                Feature feature= mapView.selectFeature((float) point.x,(float) point.y);
                endName=LocationModel.name.get(feature);
                if(feature!=null){
                    long featureId = LocationModel.id.get(feature);
                    mapView.setRenderableColor("Area",featureId, Color.BLUE);
                }
                // 设置给导航定位管理器
                lbsManager.switchPostionType(positionManager);
                lbsManager.startUpdatingLocation();

                mapView.getOverlayController().refresh();
            }
        });
        mapView.setOnSingleTapListener(new OnSingleTapListener() {
            @Override
            public void onSingleTap(final MapView mapView, final float x, final float y) {

                Feature selectFeature = mapView.selectFeature(x, y);
                //通过LocationModel来拿到Feature的name
                if (selectFeature == null || selectFeature.getPtr() == null) {
                    return;
                }
                final String name = LocationModel.name.get(selectFeature);
                if (name == null) {
                    return;
                }

                startName=name;

                if (startDialog == null) {
                    startDialog = new android.app.AlertDialog.Builder(getContext())
                            .setTitle("是否将该点设为起点")
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    Types.Point point = mapView.converToWorldCoordinate(x, y);
                                    // 设置点击的区域为起点
                                    startOverlay.init(new double[]{point.x, point.y});
                                    // 设置点击所在的楼层为终点
                                    startOverlay.mFloorId = map.getFloorId();
                                    startCell=startOverlay;
                                    showStartEndView();

                                    mapView.getOverlayController().refresh();

                                }
                            }).show();
                } else {
                    startDialog.show();
                }

            }
        });
        lbsManager = new LBSManager(map, getActivity(), Constant.SINGLE_BUILDING_ID, Constant.APP_KEY);
        // 添加导航的事件回调
        lbsManager.addOnNavigationListener(new LBSManager.OnNavigationListener() {
            @Override
            public void onNavigateComplete(NavigateManager.NavigateState navigateState, FeatureCollection featureCollection) {
                hasNavRoad=true;
                once = true;
            }

            @Override
            public void onNavigateError(NavigateManager.NavigateState navigateState) {
            }
        });

        // 增加动态导航和离开导航一段距离的回调
        lbsManager.addOnDynamicListener(new LBSManager.OnDynamicListener() {
            // 动态导航的回调
            @Override
            public void onDynamicChange(DynamicNavigateWrapper dynamicNavigateWrapper) {
                if (dynamicNavigateWrapper.mSuccess) {
                    final String text = dynamicNavigateWrapper.mDynamicNavigateOutput.mDynamicNaviExplain;
                    if (text != null) {
                        tv_naving_middle.postOnAnimation(new Runnable() {
                            @Override
                            public void run() {
                                tv_naving_middle.setText(text);
                                if (text.equals("到达")) {
                                    Toast.makeText(getContext(), "您已到达目的地", Toast.LENGTH_LONG).show();
                                    stopNaving();
                                    ((IpalmapActivity)getActivity()).directShowBookShelf();
                                }
                            }
                        });
                    }
                }
            }

            // 定位点离开导航线一定距离回调
            @Override
            public void onLeaveNaviLine(Location location, float v) {
                if (once) {
                    lbsManager.stopUpdatingLocation();
                    once = false;
                    // 获取定位点的坐标
                    Point point = location.getPoint();
                    // 获取定位点所在的楼层
                    Long aLong = Location.floorId.get(location.getProperties());
                    // 获取终点的坐标
                    double[] geoCoordinate = endOverlay.getGeoCoordinate();
                    // 重新导航
                    lbsManager.navigateFromPoint(point.getCoordinate(), aLong, new Coordinate(geoCoordinate[0], geoCoordinate[1]), endOverlay.getFloorId(), aLong);
                    lbsManager.startUpdatingLocation();
                }
            }
        });
        // 设置定位点离导航线5米外就触发离开导航线的回调
        lbsManager.setCorrectionRadius(5);

        startOverlay = new ImageOverlay(getContext());
        startOverlay.setBackgroundResource(R.drawable.ipalmap_start);
        startOverlay.init(new double[]{0, 0});
        map.addOverlay(startOverlay);
        // 添加终点的定位图标
        endOverlay = new ImageOverlay(getContext());
        endOverlay.setBackgroundResource(R.drawable.ipalmap_end);
        endOverlay.init(new double[]{bundle.getDouble("map_x_value"), bundle.getDouble("map_y_value")});
        //endOverlay.init(new double[]{0,0});
        map.addOverlay(endOverlay);

        // 蓝牙定位管理对象
        positionManager = new BeaconPositioningManager(getContext(), Constant.APP_KEY);
        // 创建一个根据导航线模拟导航的管理器
        mockPositionManager = new MockPositionManager();

        // 设置定位点的图标
        lbsManager.locationOverlay().setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.btn_nav));

        // 朝北模式
        lbsManager.setNavigateMode(DynamicNavigationMode.NORTH);
        mapView.getOverlayController().refresh();
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

        lbsManager.getNaviLayer().clearFeatures();
        lbsManager.navigateFromPoint(
                new Coordinate(startCell.getGeoCoordinate()[0], startCell.getGeoCoordinate()[1]),
                startCell.getFloorId(),
                new Coordinate(endOverlay.getGeoCoordinate()[0], endOverlay.getGeoCoordinate()[1]),
                endOverlay.getFloorId(),
                endOverlay.getFloorId()
        );

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

        tv_naving_start.setText("起点:"+startName);
        tv_naving_end.setText("目的地:"+endName);
    }


    private void search() {

        String content = et_search.getText().toString();
        if (TextUtils.isEmpty(content)) {
            return;
        }
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        et_search.setText("");
        et_search.clearFocus();

        // 根据关键字搜索
        map.dataSource().search(content, 0, 10, new long[]{map.getFloorId()}, null, new DataSource.OnRequestDataEventListener<LocationPagingList>() {
            @Override
            public void onRequestDataEvent(DataSource.ResourceState resourceState, LocationPagingList locationPagingList) {
                if (resourceState != DataSource.ResourceState.OK && resourceState != DataSource.ResourceState.CACHE) {
                    return;
                }
                if (locationPagingList.getSize() == 0) {
                    Toast.makeText(getActivity(), "暂无搜索结果", Toast.LENGTH_SHORT);
                    return;
                }

                mData = locationPagingList;
                if (mAdapter == null) {
                    mAdapter = new IpalmapSearchAdapter(getActivity(), locationPagingList);
                    lv_search.setAdapter(mAdapter);
                } else {
                    mAdapter.setData(locationPagingList);
                }
                showSearchResultView();
            }
        });
    }

    @Override
    public void onDestroyView() {
        lbsManager.stopUpdatingLocation();
        lbsManager.close();
        super.onDestroyView();
    }


    @Override
    public void onResume() {
        super.onResume();
        lbsManager.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        lbsManager.pause();
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        if (id == R.id.ipalmap_nav_btn_search) {
            search();
        } else if (id == R.id.ipalmap_nav_ll_floordown_container) {
            showSearchHideResultView();
        } else if (id == R.id.ipalmap_nav_ll_floorup_container) {
            showSearchResultView();
        } else if (id == R.id.ipalmap_zoom_out) {
            //地图放大
            mapView.zoomIn();
        } else if (id == R.id.ipalmap_zoom_in) {
            //地图缩小
            mapView.zoomOut();
        } else if (id == R.id.ipalmap_nav_ib_naving) {
            LocationOverlay locationOverlay=lbsManager.locationOverlay();
            //表示定位点确认可以导航了
            if(locationOverlay.getGeoCoordinate()[0]>0){
                startCell=locationOverlay;
                startName="定位点";
                showStartEndView();
            }

        }else if(id==R.id.ipalmap_nav_btn_moni){

            lbsManager.stopUpdatingLocation();
            mockPositionManager.reset();
            lbsManager.switchPostionType(mockPositionManager);
            mockPositionManager.attchLBSManager(lbsManager);

            lbsManager.getNaviLayer().clearFeatures();
            lbsManager.navigateFromPoint(
                    new Coordinate(startCell.getGeoCoordinate()[0], startCell.getGeoCoordinate()[1]),
                    startCell.getFloorId(),
                    new Coordinate(endOverlay.getGeoCoordinate()[0], endOverlay.getGeoCoordinate()[1]),
                    endOverlay.getFloorId(),
                    endOverlay.getFloorId()
            );

            // 开始动态导航
            lbsManager.startDynamic();
            // 开始跟新定位点
            lbsManager.startUpdatingLocation();
            showNavingView();
        } else if (id == R.id.ipalmap_nav_btn_start) {

            lbsManager.switchPostionType(positionManager);
            // 开始跟新定位点
            lbsManager.startUpdatingLocation();

            // 开始动态导航
            lbsManager.startDynamic();
            showNavingView();

        } else if (id == R.id.ipalmap_nav_address_choose_back) {
            if (hasNavRoad) {
                // 清除地图上的导航线
                startOverlay.init(new double[]{0, 0});
                lbsManager.getNaviLayer().clearFeatures();
                mapView.getOverlayController().refresh();
                hasNavRoad = false;
            } else {
                showInitView();
            }
        } else if (id == R.id.ipalmap_naving_back) {
            stopNaving();
        }
    }

    private void stopNaving(){
        lbsManager.stopUpdatingLocation();
        lbsManager.stopDynamic();
        lbsManager.getNaviLayer().clearFeatures();
        startOverlay.init(new double[]{0, 0});
        lbsManager.switchPostionType(positionManager);
        lbsManager.startUpdatingLocation();
        tv_naving_middle.setText("");
        mapView.getOverlayController().refresh();
        showInitView();
    }

}
