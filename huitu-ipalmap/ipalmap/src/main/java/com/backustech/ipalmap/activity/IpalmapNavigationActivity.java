package com.backustech.ipalmap.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
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
import com.backustech.ipalmap.adapter.IpalmapSearchAdapter;
import com.palmaplus.nagrand.core.Types;
import com.palmaplus.nagrand.data.DataSource;
import com.palmaplus.nagrand.data.Feature;
import com.palmaplus.nagrand.data.FeatureCollection;
import com.palmaplus.nagrand.data.LocationModel;
import com.palmaplus.nagrand.data.LocationPagingList;
import com.palmaplus.nagrand.easyapi.LBSManager;
import com.palmaplus.nagrand.easyapi.MockPositionManager;
import com.palmaplus.nagrand.geos.Coordinate;
import com.palmaplus.nagrand.geos.Point;
import com.palmaplus.nagrand.navigate.DynamicNavigateWrapper;
import com.palmaplus.nagrand.navigate.DynamicNavigationMode;
import com.palmaplus.nagrand.navigate.NavigateManager;
import com.palmaplus.nagrand.position.Location;
import com.palmaplus.nagrand.view.MapView;
import com.palmaplus.nagrand.view.gestures.OnSingleTapListener;

/**
 * Created by tygzx on 2018/3/28.
 */

public class IpalmapNavigationActivity extends IpalmapActivity implements View.OnClickListener {

    protected MockPositionManager positionManager;

    //点击地图 弹出是否设置为起点的dialog
    private Dialog startDialog;

    ImageButton ib_close;
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
    protected void initLayout() {
        setContentView(R.layout.activity_ipalmap_nav);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.ipalmap_header_bar_nav);
    }

    @Override
    protected void initView() {
        ib_close= (ImageButton) findViewById(R.id.ipalmap_header_bar_back);
        ll_search_container= (LinearLayout) findViewById(R.id.ipalmap_ll_search_container);
        et_search= (EditText) findViewById(R.id.ipalmap_nav_et_search);
        btn_search= (Button) findViewById(R.id.ipalmap_nav_btn_search);
        fl_bottom_container= (FrameLayout) findViewById(R.id.ipalmap_nav_bottom_container);
        ll_listview_container= (LinearLayout) findViewById(R.id.ipalmap_nav_listview_container);
        ll_floordown_container= (LinearLayout) findViewById(R.id.ipalmap_nav_ll_floordown_container);
        ll_floorup_container= (LinearLayout) findViewById(R.id.ipalmap_nav_ll_floorup_container);
        lv_search= (ListView) findViewById(R.id.ipalmap_nav_listview);
        ll_nav_container= (LinearLayout) findViewById(R.id.ipalmap_nav_nav_container);
        ll_nav_back= (LinearLayout) findViewById(R.id.ipalmap_nav_address_choose_back);
        ll_nav_back.setOnClickListener(this);
        tv_start= (TextView) findViewById(R.id.ipalmap_nav_tv_start);
        tv_end= (TextView) findViewById(R.id.ipalmap_nav_tv_end);
        tv_floor= (TextView) findViewById(R.id.ipalmap_nav_tv_floor);
        ll_nav_button_container= (LinearLayout) findViewById(R.id.ipalmap_nav_nav_button_container);
        btn_nav_moni= (Button) findViewById(R.id.ipalmap_nav_btn_moni);
        btn_nav_start= (Button) findViewById(R.id.ipalmap_nav_btn_start);
        v_zoom_out=findViewById(R.id.ipalmap_zoom_out);
        v_zoom_in=findViewById(R.id.ipalmap_zoom_in);
        ib_naving= (ImageButton) findViewById(R.id.ipalmap_nav_ib_naving);

        ll_naving_container= (LinearLayout) findViewById(R.id.ipalmap_nav_naving_container);
        tv_naving_start= (TextView) findViewById(R.id.ipalmap_nav_tv_naving_start);
        tv_naving_end= (TextView) findViewById(R.id.ipalmap_nav_tv_naving_end);
        tv_naving_middle= (TextView) findViewById(R.id.ipalmap_nav_tv_naving);

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
                    hideBottomView();
                }
            }
        });

        lv_search.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                hideListViewContainer();
                hideSearchView();
                showNavView();
                LocationModel locationModel=mData.getPOI(position);
                Feature feature = map.mapView().selectFeature(LocationModel.id.get(locationModel));
                String name=LocationModel.name.get(feature);
                tv_start.setText(name);
                tv_floor.setText("F2");
                map.mapView().moveToFeature(feature, true, 1000);

                startOverlay.init(new double[]{feature.getCentroid().getX(),feature.getCentroid().getY()});

                // 设置点击所在的楼层为终点
                startOverlay.mFloorId = map.getFloorId();

                lbsManager.navigateFromPoint(
                        new Coordinate(startOverlay.getGeoCoordinate()[0], startOverlay.getGeoCoordinate()[1]),
                        startOverlay.mFloorId,
                        new Coordinate(endOverlay.getGeoCoordinate()[0], endOverlay.getGeoCoordinate()[1]),
                        endOverlay.mFloorId,
                        endOverlay.mFloorId
                );
                hasNavRoad=true;
                mapView.getOverlayController().refresh();
            }
        });

        ib_close.setOnClickListener(this);

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
    protected void initData() {

        mapView.setBackgroundColor(Color.parseColor("#c4e1ff"));
        mapView.setOnSingleTapListener(new OnSingleTapListener() {
            @Override
            public void onSingleTap(final MapView mapView, final float x, final float y) {

                Feature selectFeature = mapView.selectFeature(x, y);
                //通过LocationModel来拿到Feature的name
                final String name = LocationModel.name.get(selectFeature);
                if(name==null){
                    return;
                }

                if (startDialog == null) {
                    startDialog = new AlertDialog.Builder(IpalmapNavigationActivity.this)
                            .setTitle("是否将该点设为起点")
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    showNavView();
                                    tv_start.setText(name);

                                    tv_end.setText("F");
                                    tv_floor.setText("F2");

                                    Types.Point point = mapView.converToWorldCoordinate(x, y);
                                    // 清除地图上的导航线
                                    lbsManager.getNaviLayer().clearFeatures();
                                    // 设置点击的区域为起点
                                    startOverlay.init(new double[] { point.x, point.y });
                                    // 设置点击所在的楼层为终点
                                    startOverlay.mFloorId = map.getFloorId();
                                    //todo
                                    endOverlay.mFloorId=map.getFloorId();

                                    lbsManager.navigateFromPoint(
                                            new Coordinate(startOverlay.getGeoCoordinate()[0], startOverlay.getGeoCoordinate()[1]),
                                            startOverlay.mFloorId,
                                            new Coordinate(endOverlay.getGeoCoordinate()[0], endOverlay.getGeoCoordinate()[1]),
                                            endOverlay.mFloorId,
                                            endOverlay.mFloorId
                                    );
                                    hasNavRoad=true;
                                    mapView.getOverlayController().refresh();
                                }
                            }).show();
                } else {
                    startDialog.show();
                }

            }
        });

        // 添加导航的事件回调
        lbsManager.addOnNavigationListener(new LBSManager.OnNavigationListener() {
            @Override
            public void onNavigateComplete(NavigateManager.NavigateState navigateState, FeatureCollection featureCollection) {
                once = true;
//                lbsManager.startUpdatingLocation();
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
                if(dynamicNavigateWrapper.mSuccess){
                    String text=dynamicNavigateWrapper.mDynamicNavigateOutput.mDynamicNaviExplain;
                    tv_naving_middle.setText(text);
                }
            }

            // 定位点离开导航线一定距离回调
            @Override
            public void onLeaveNaviLine(Location location, float v) {
                if (once) {
//                    lbsManager.stopUpdatingLocation();
                    // 重新设置定位点
                    positionManager.reset();
                    once = false;
                    // 获取定位点的坐标
                    Point point = location.getPoint();
                    // 获取定位点所在的楼层
                    Long aLong = Location.floorId.get(location.getProperties());
                    // 获取终点的坐标
                    double[] geoCoordinate = endOverlay.getGeoCoordinate();
                    // 重新导航
                    lbsManager.navigateFromPoint(point.getCoordinate(), aLong, new Coordinate(geoCoordinate[0], geoCoordinate[1]), endOverlay.getFloorId(), aLong);
                }
            }
        });
        // 设置定位点离导航线5米外就触发离开导航线的回调
        lbsManager.setCorrectionRadius(5);


        //设置终点
        // 设置终点图标的可见性
        endOverlay.setVisibility(View.VISIBLE);
        // 设置点击区域的终点
        endOverlay.init(new double[] { 13373630.426723048E7, 3537421.0997897387 });
        // 设置点击所在的楼层为终点
        endOverlay.mFloorId = map.getFloorId();



        // 创建一个根据导航线模拟导航的管理器
        positionManager = new MockPositionManager();
        // 设置给导航定位管理器
        lbsManager.switchPostionType(positionManager);
        // 设置定位点的图标
        lbsManager.locationOverlay().setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.btn_nav));
        // 初始化一些必要的参数
        positionManager.attchLBSManager(lbsManager);

        // 朝北模式
        lbsManager.setNavigateMode(DynamicNavigationMode.NORTH);
        lbsManager.startDynamic();
        lbsManager.startUpdatingLocation();

        mapView.getOverlayController().refresh();
    }

    private void showSearchView(){
        ll_search_container.setVisibility(View.VISIBLE);
    }

    private void hideSearchView(){
        ll_search_container.setVisibility(View.GONE);
    }

    private void showNavView(){
        ll_nav_container.setVisibility(View.VISIBLE);
        ll_nav_button_container.setVisibility(View.VISIBLE);
    }

    private void hideNavView(){
        ll_nav_container.setVisibility(View.GONE);
        ll_nav_button_container.setVisibility(View.GONE);
    }

    private void showBottomView(){
        fl_bottom_container.setVisibility(View.VISIBLE);
    }

    private void hideBottomView(){
        fl_bottom_container.setVisibility(View.GONE);
    }

    private void showListViewContainer(){
        ll_listview_container.setVisibility(View.VISIBLE);
        showListView();
    }

    private void hideListViewContainer(){
        ll_listview_container.setVisibility(View.GONE);
        hideListView();
    }

    private void showListView(){

        lv_search.setVisibility(View.VISIBLE);
        ll_floordown_container.setVisibility(View.VISIBLE);
        ll_floorup_container.setVisibility(View.GONE);
    }

    private void hideListView(){
        ll_floordown_container.setVisibility(View.GONE);
        lv_search.setVisibility(View.GONE);
        ll_floorup_container.setVisibility(View.VISIBLE);
    }

    private void showNavingContainer(){
        ll_naving_container.setVisibility(View.VISIBLE);
    }

    private void hideNavingContainer(){
        ll_naving_container.setVisibility(View.GONE);
    }



    private void search(){
        String content=et_search.getText().toString();
        if (TextUtils.isEmpty(content)) {
            return;
        }

        // 根据关键字搜索
        map.dataSource().search(content, 0, 10, new long[] { map.getFloorId() }, null, new DataSource.OnRequestDataEventListener<LocationPagingList>() {
            @Override
            public void onRequestDataEvent(DataSource.ResourceState resourceState, LocationPagingList locationPagingList) {
                if (resourceState != DataSource.ResourceState.OK && resourceState != DataSource.ResourceState.CACHE) {
                    return;
                }
                if(locationPagingList.getSize()==0) {
                    Toast.makeText(IpalmapNavigationActivity.this,"暂无搜索结果",Toast.LENGTH_SHORT);
                        return;
                }

                mData=locationPagingList;
                if(mAdapter==null){
                    mAdapter=new IpalmapSearchAdapter(IpalmapNavigationActivity.this,locationPagingList);
                    lv_search.setAdapter(mAdapter);
                }else {
                    mAdapter.setData(locationPagingList);
                }
                showListViewContainer();
            }
        });
    }


    @Override
    public void onClick(View v) {
        int id=v.getId();
        if(id==R.id.ipalmap_header_bar_back){
            finish();
        }else if(id==R.id.ipalmap_nav_btn_search){
            search();
        }else if(id==R.id.ipalmap_nav_ll_floordown_container){
            hideListView();
        }else if(id==R.id.ipalmap_nav_ll_floorup_container){
            showListView();
        }else if(id==R.id.ipalmap_zoom_out){
            //地图放大
            mapView.zoomIn();
        }else if(id==R.id.ipalmap_zoom_in){
            //地图缩小
            mapView.zoomOut();
        }else if(id==R.id.ipalmap_nav_ib_naving){
            // 开始动态导航
            lbsManager.startDynamic();
            // 开始跟新定位点
            lbsManager.startUpdatingLocation();



        }else if(id==R.id.ipalmap_nav_btn_start){

        }else if(id==R.id.ipalmap_nav_address_choose_back){
            if(hasNavRoad){
                tv_start.setText("请选择起点");
                // 清除地图上的导航线
                startOverlay.init(new double[]{0,0});
                lbsManager.getNaviLayer().clearFeatures();
                mapView.getOverlayController().refresh();
                hasNavRoad=false;
            }else{
                hideNavView();
                showSearchView();
            }
        }
    }
}
