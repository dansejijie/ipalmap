package com.backustech.huitumap.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
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

import com.backustech.huitumap.R;
import com.backustech.huitumap.adapter.IpalmapSearchAdapter;
import com.backustech.huitumap.constants.Constant;
import com.backustech.huitumap.utils.ToastUtils;
import com.backustech.huitumap.view.Mark;
import com.palmap.widget.Compass;
import com.palmaplus.nagrand.core.Types;
import com.palmaplus.nagrand.data.BasicElement;
import com.palmaplus.nagrand.data.DataSource;
import com.palmaplus.nagrand.data.Feature;
import com.palmaplus.nagrand.data.FeatureCollection;
import com.palmaplus.nagrand.data.LocationModel;
import com.palmaplus.nagrand.data.LocationPagingList;
import com.palmaplus.nagrand.data.MapElement;
import com.palmaplus.nagrand.data.PlanarGraph;
import com.palmaplus.nagrand.easyapi.LBSManager;
import com.palmaplus.nagrand.easyapi.Map;
import com.palmaplus.nagrand.easyapi.MockPositionManager;
import com.palmaplus.nagrand.geos.Coordinate;
import com.palmaplus.nagrand.geos.GeometryFactory;
import com.palmaplus.nagrand.geos.Point;
import com.palmaplus.nagrand.navigate.DynamicNavigateWrapper;
import com.palmaplus.nagrand.navigate.DynamicNavigationMode;
import com.palmaplus.nagrand.navigate.NavigateManager;
import com.palmaplus.nagrand.position.Location;
import com.palmaplus.nagrand.position.PositioningManager;
import com.palmaplus.nagrand.position.ble.BeaconPositioningManager;
import com.palmaplus.nagrand.position.util.PositioningUtil;
import com.palmaplus.nagrand.view.MapView;
import com.palmaplus.nagrand.view.gestures.OnLongPressListener;
import com.palmaplus.nagrand.view.gestures.OnZoomListener;
import com.palmaplus.nagrand.view.layer.FeatureLayer;
import com.palmaplus.nagrand.view.overlay.LocationOverlay;
/**
 * Created by tygzx on 2018/3/9.
 */

public class IpalmapNavigationActivity extends IpalmapActivity implements SensorEventListener {


    /**
     * 指北针
     */
    Compass mCompass;

    //定位点
    LocationOverlay locationOverlay;
    RelativeLayout rl_positionContainer;

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
    TextView tv_start;
    TextView tv_end;
    TextView tv_floor;

    //顶部 导航正在行进中的起始末尾组件
    LinearLayout ll_naving_container;
    TextView tv_naving_start;
    TextView tv_naving_end;
    TextView tv_naving_middle;

    //底部导航按钮
    Button btnNav;

    //底部 导航显示的按钮
    LinearLayout ll_nav_button_container;
    Button btn_nav_moni;
    Button btn_nav_start;

    //放大缩小
    View v_zoom_out;
    View v_zoom_in;

    //定位按钮
    ImageButton ib_location;

    IpalmapSearchAdapter mAdapter;
    LocationPagingList mData;

    boolean isTapNaving=false;//处于点选状态 导航的时候选用startPoint 和finalPoint
    boolean isSearchNaving=false;//处于搜索状态，导航的时候选用searchPoint 和finalPoint;
    double [] finalPoint=new double[]{13373618.795863323,3537448.919769641};
    double [] startPoint=new double[]{0,0};
    double [] searchPoint=new double[]{0,0};
    double [] positionPoint=new double[]{0,0};
    Mark lastStartMark=null;
    int markNums=0;

    /**
     * 定位图层
     */
    FeatureLayer mPositioningLayer;

    /**
     * 蓝牙定位接口
     */
    PositioningManager mBlePositioningManager;

    //构造一个蓝牙定位导航管理器
    LBSManager lbsManager ;
    int state=0;
    boolean isResetNavigation=false;

    MockPositionManager positionManager;
    /**
     * 导航图层
     */
    private FeatureLayer mNavFeatureLayer;

    private boolean mIsNavigating=false;
    private boolean mIsReadyNavigating=false;


    @Override
    protected void initLayout() {
        setContentView(R.layout.activity_ipalmap_nav);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.ipalmap_header_bar_nav);
    }

    @Override
    protected void initView() {

        mCompass= (Compass) findViewById(R.id.ipalmap_compass);
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
        tv_start= (TextView) findViewById(R.id.ipalmap_nav_tv_start);
        tv_end= (TextView) findViewById(R.id.ipalmap_nav_tv_end);
        tv_floor= (TextView) findViewById(R.id.ipalmap_nav_tv_floor);
        ll_nav_button_container= (LinearLayout) findViewById(R.id.ipalmap_nav_nav_button_container);
        btn_nav_moni= (Button) findViewById(R.id.ipalmap_nav_btn_moni);
        btn_nav_start= (Button) findViewById(R.id.ipalmap_nav_btn_start);
        v_zoom_out=findViewById(R.id.ipalmap_zoom_out);
        v_zoom_in=findViewById(R.id.ipalmap_zoom_in);
        ib_location= (ImageButton) findViewById(R.id.ipalmap_nav_position);

        btnNav= (Button) findViewById(R.id.ipalmap_btn_nav);
        btnNav.setOnClickListener(this);

        ll_naving_container= (LinearLayout) findViewById(R.id.ipalmap_nav_naving_container);
        tv_naving_start= (TextView) findViewById(R.id.ipalmap_nav_tv_naving_start);
        tv_naving_end= (TextView) findViewById(R.id.ipalmap_nav_tv_naving_end);
        tv_naving_middle= (TextView) findViewById(R.id.ipalmap_nav_tv_naving);

        rl_positionContainer= (RelativeLayout) findViewById(R.id.ipalmap_nav_position_container);

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
                LocationModel locationModel=mData.getPOI(position);

                Feature feature = mMapView.selectFeature(LocationModel.id.get(locationModel));

                if(lastStartMark!=null){
                    mMapView.removeOverlay(lastStartMark);
                }

                Types.Point point=mMapView.converToWorldCoordinate(feature.getCentroid().getX(),feature.getCentroid().getY());
                startPoint[0]=point.x;
                startPoint[1]=point.y;
                markNums++;
                lastStartMark = new Mark(IpalmapNavigationActivity.this,markNums,R.drawable.ipalmap_start);
                lastStartMark.init(new double[]{startPoint[0],startPoint[1]});
                lastStartMark.setFloorId(mCurrentFloorId);
                mMapView.addOverlay(lastStartMark);

                showLoadingDialog();
                //mNavigateManager.navigation(startPoint[0], startPoint[1], mCurrentFloorId, finalPoint[0], finalPoint[1], mCurrentFloorId); //请求导航线

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
        ib_location.setOnClickListener(this);
    }

    @Override
    protected void initAfaterData() {

    }

    @Override
    protected void initAfaterMap() {
        //放置定位点
        final Map map =mMapView.getMap();

        locationOverlay = new LocationOverlay(IpalmapNavigationActivity.this, map.mapView(), 1000);
        // 初始化Overlay的位置
        locationOverlay.init(new double[] { 0, 0 });
        locationOverlay.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.btn_nav));
        // 添加Overlay
        map.addOverlay(locationOverlay);

        SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sm.registerListener(this,
                sm.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void initData() {

        lbsManager=new LBSManager(mMapView.getMap(), this,Constant.SINGLE_BUILDING_ID,Constant.APP_KEY);
        lbsManager.setNavigateMode(DynamicNavigationMode.NORTH);

        // 创建一个根据导航线模拟导航的管理器
        positionManager = new MockPositionManager();
        // 设置给导航定位管理器
        lbsManager.switchPostionType(positionManager);
        // 设置定位点的图标
        lbsManager.locationOverlay().setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.btn_nav));
        // 初始化一些必要的参数
        positionManager.attchLBSManager(lbsManager);

        lbsManager.addOnNavigationListener(new LBSManager.OnNavigationListener() {
            @Override
            public void onNavigateComplete(NavigateManager.NavigateState navigateState, FeatureCollection featureCollection) {
                Log.d("Navigate", "onNavigateComplete->ResourceState.ok");
                mNavFeatureLayer.clearFeatures();
                mNavFeatureLayer.addFeatures(featureCollection);
                state++;
                isResetNavigation=true;
            }

            @Override
            public void onNavigateError(NavigateManager.NavigateState navigateState) {
                hideLoadingDialog();
                ToastUtils.showLongToast(R.string.no_navigation_data);
            }
        });

        // 增加动态导航和离开导航一段距离的回调
        lbsManager.addOnDynamicListener(new LBSManager.OnDynamicListener() {
            // 动态导航的回调
            @Override
            public void onDynamicChange(DynamicNavigateWrapper dynamicNavigateWrapper) {
                if(dynamicNavigateWrapper.mSuccess){
                    String text=dynamicNavigateWrapper.mDynamicNavigateOutput.mDynamicNaviExplain;
                }
            }

            // 定位点离开导航线一定距离回调
            @Override
            public void onLeaveNaviLine(Location location, float v) {
                if (isResetNavigation) {
//                    lbsManager.stopUpdatingLocation();
                    // 重新设置定位点
                    positionManager.reset();
                    isResetNavigation = false;
                    // 获取定位点的坐标
                    Point point = location.getPoint();
                    // 获取定位点所在的楼层
                    Long aLong = Location.floorId.get(location.getProperties());
                    // 重新导航
                    lbsManager.navigateFromPoint(point.getCoordinate(), aLong, new Coordinate(finalPoint[0], finalPoint[0]),mCurrentFloorId, mCurrentFloorId);
                }
            }
        });
        // 设置定位点离导航线5米外就触发离开导航线的回调
        lbsManager.setCorrectionRadius(5);



        /**
         * 设置更换楼层监听器,每次切换楼层需要重新添加导航层
         */
        mMapView.setOnChangePlanarGraph(new MapView.OnChangePlanarGraph() {
            @Override
            public void onChangePlanarGraph(PlanarGraph oldPlanarGraph, PlanarGraph newPlanarGraph, long oldPlanarGraphId, long newPlanarGraphId) {
                Log.d("Navigate", "oldPlanarGraphId = " + oldPlanarGraphId + "; " +
                        "newPlanarGraphId = " + newPlanarGraphId);
                mCompass.invalidate();

                mCurrentFloorId = newPlanarGraphId;
                mNavFeatureLayer = new FeatureLayer("navigate");
                mMapView.addLayer(mNavFeatureLayer);
                mMapView.setLayerOffset(mNavFeatureLayer);
                if (lbsManager != null) {
                    lbsManager.switchPlanarGraph(newPlanarGraphId);
                }
            }
        });



        mMapView.setBackgroundColor(Color.parseColor("#c4e1ff"));
        mMapView.setOnZoomListener(new OnZoomListener() {
            @Override
            public void preZoom(MapView mapView, float v, float v1) {

            }

            @Override
            public void onZoom(MapView mapView, boolean b) {

            }

            @Override
            public void postZoom(MapView mapView, float v, float v1) {
                hideNavView();
                hideListViewContainer();
                showSearchView();

            }
        });

        mMapView.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                hideNavView();
                hideListViewContainer();
                showSearchView();
                return true;
            }
        });

        mMapView.setOnLongPressListener(new OnLongPressListener() {
            @Override
            public void onLongPress(final MapView mapView,final float x,final float y) {

                //先确定是否点击到标记物
                //获取该起点的名称
                Feature selectFeature = mMapView.selectFeature(x, y);
                //通过LocationModel来拿到Feature的name
                final String name = LocationModel.name.get(selectFeature);
                if(name==null){
                    return;
                }

                hideSearchView();
                hideListViewContainer();
                showNavView();

                if (startDialog == null) {
                    startDialog = new AlertDialog.Builder(IpalmapNavigationActivity.this)
                            .setTitle("是否将该点设为起点")
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    hideNavView();
                                    showSearchView();
                                }
                            })
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(lastStartMark!=null){
                                        mMapView.removeOverlay(lastStartMark);
                                    }

                                    tv_start.setText(name);

                                    tv_end.setText("F");


                                    Types.Point point=mapView.converToWorldCoordinate(x,y);
                                    startPoint[0]=point.x;
                                    startPoint[1]=point.y;
                                    markNums++;
                                    lastStartMark = new Mark(IpalmapNavigationActivity.this,markNums,R.drawable.ipalmap_start);
                                    lastStartMark.init(new double[]{startPoint[0],startPoint[1]});
                                    lastStartMark.setFloorId(mCurrentFloorId);
                                    mMapView.addOverlay(lastStartMark);

                                    showLoadingDialog();
                                    lbsManager.navigateFromPoint(new Coordinate(startPoint[0],startPoint[1]),mCurrentFloorId,new Coordinate(finalPoint[0],finalPoint[1]),mCurrentFloorId,mCurrentFloorId);
                                }
                            }).show();
                } else {
                    startDialog.show();
                }
            }
        });

        mMapView.setOnZoomListener(new OnZoomListener() {
            @Override
            public void preZoom(MapView mapView, float x, float y) {

            }

            @Override
            public void onZoom(MapView mapView, boolean b) {

            }

            @Override
            public void postZoom(MapView mapView, float x, float y) {
                mCompass.invalidate();
            }
        });

        mCompass.setMapView(mMapView);
        mMapOptions.setRotateEnabled(true);
        mMapView.setMapOptions(mMapOptions);

        // 蓝牙定位管理对象
        mBlePositioningManager = new BeaconPositioningManager(this, Constant.APP_KEY);
        // 定位监听的事件，如果得到了新的位置数据，就会调用这个方法
        mBlePositioningManager.setOnLocationChangeListener(
                new PositioningManager.OnLocationChangeListener<Location>() {
                    @Override
                    public void onLocationChange(PositioningManager.LocationStatus status, final Location oldLocation,
                                                 final Location newLocation) {  // 分别代表着上一个位置点和新位置点
                        Log.d("TAG", "onLocationChange");
                        switch (status) {
                            case MOVE:
                                Coordinate coordinate = newLocation.getPoint().getCoordinate();
                                Log.d("onLocationChange", "x = " + coordinate.getX() + ", y =" +
                                        " " + coordinate.getY());

                                positionPoint[0]=coordinate.x;
                                positionPoint[1]=coordinate.y;

                                locationOverlay.init(new double[]{coordinate.x, coordinate.y});
                                // 设置Overlay附属的楼层
                                locationOverlay.setFloorId(mCurrentFloorId);

                                if(mIsNavigating){
                                    // 移动地图到指指定位置
                                    mMapView.moveToPoint(coordinate);
                                }

                                // 需要重新刷新Overlay
                                mMapView.getOverlayController().refresh();
                                //PositioningUtil.positionLocation(1L, mPositioningLayer, newLocation); //
                                // 当第二次返回点位点时，我们就可以让这个定位点开始移动了
                                break;
                            default:
                                break;
                        }

                    }
                });
    }

    @Override
    protected void afterDrawPlanarGraph() {
        markNums++;
        Mark mark = new Mark(IpalmapNavigationActivity.this,markNums,R.drawable.ipalmap_end);
        mark.init(finalPoint);
        mark.setFloorId(mCurrentFloorId);
        mMapView.addOverlay(mark);

        mPositioningLayer = new FeatureLayer("positioning"); //新建一个放置定位点的图层
        mMapView.addLayer(mPositioningLayer);  // 把这个图层添加至MapView中
        mMapView.setLayerOffset(mPositioningLayer); // 让这个图层获取到当前地图的坐标偏移

        // 在PositionLayer上添加一个特征点，用于显示定位点
        Point point = GeometryFactory.createPoint(new Coordinate(0, 0));
        MapElement mapElement = new MapElement();
        mapElement.addElement("id", new BasicElement(1L)); // 1L为特征点ID，下面需要
        Feature feature = new Feature(point, mapElement);
        mPositioningLayer.addFeature(feature);

        //开始定位
        if (mMapView.checkInitializing()) {
            return;
        }
        mBlePositioningManager.start();


    }

    @Override
    public boolean handleMessage(Message msg) {
        return false;
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
            mMapView.zoomIn();
        }else if(id==R.id.ipalmap_zoom_in){
            //地图缩小
            mMapView.zoomOut();
        }else if(id==R.id.ipalmap_nav_position){
            if (mMapView.checkInitializing()) {
                return;
            }
            mBlePositioningManager.start(); // 开始定位
        }else if(id==R.id.ipalmap_nav_btn_start){

        }else if(id==R.id.ipalmap_btn_nav){

            if(mIsNavigating){
                return;
            }

            if(!isTapNaving&&!isSearchNaving){

                if(positionPoint[0]>0&&positionPoint[1]>0){
                    lbsManager.navigateFromPoint(new Coordinate(positionPoint[0],positionPoint[1]),mCurrentFloorId,new Coordinate(finalPoint[0],finalPoint[1]),mCurrentFloorId,mCurrentFloorId);
                    mIsReadyNavigating=true;
                }
            }

            if(mIsNavigating){

            }else{

            }
            //开始导航
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            float value = event.values[0];
            locationOverlay.setRotation(value - (float) mMapView.getRotate());
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(lbsManager!=null){
            lbsManager.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(lbsManager!=null){
            lbsManager.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBlePositioningManager != null) {
            mBlePositioningManager.drop(); //销毁蓝牙定位管理对象
        }
        if(lbsManager!=null){
            lbsManager.close();
        }

        SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sm.unregisterListener(this);
    }

    private void showSearchView(){
        ll_search_container.setVisibility(View.VISIBLE);
    }

    private void hideSearchView(){
        ll_search_container.setVisibility(View.GONE);
    }

    private void showNavView(){
        ll_nav_container.setVisibility(View.VISIBLE);
        //ll_nav_button_container.setVisibility(View.VISIBLE);
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
        showLoadingDialog();
        mDataSource.search(content, 1, 10, null, null, new DataSource.OnRequestDataEventListener<LocationPagingList>() {
            @Override
            public void onRequestDataEvent(DataSource.ResourceState resourceState, LocationPagingList locationPagingList) {
                hideLoadingDialog();
                if (resourceState == DataSource.ResourceState.OK) {

                    if(locationPagingList.getSize()==0) {
                        ToastUtils.showToast("暂无搜索结果");
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
            }
        });
    }



}
