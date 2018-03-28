package com.backustech.huitumap.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.backustech.huitumap.R;
import com.backustech.huitumap.constants.Constant;
import com.backustech.huitumap.utils.ToastUtils;
import com.palmaplus.nagrand.data.DataSource;
import com.palmaplus.nagrand.data.FloorModel;
import com.palmaplus.nagrand.data.LocationList;
import com.palmaplus.nagrand.data.LocationModel;
import com.palmaplus.nagrand.data.MapModel;
import com.palmaplus.nagrand.data.PlanarGraph;
import com.palmaplus.nagrand.view.MapOptions;
import com.palmaplus.nagrand.view.MapView;
import com.palmaplus.nagrand.view.adapter.DataListAdapter;


/**
 * Created by tygzx on 2018/3/22.
 */

public abstract class IpalmapActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener,Handler.Callback{

    protected Handler mHandler;

    private ProgressDialog mProgressDialog;

    protected DataSource mDataSource;

    protected MapOptions mMapOptions;

    protected MapView mMapView;

    protected RelativeLayout mMapOverlayContainer;

    protected long mCurrentFloorId;

    protected Spinner mSpinner;

    //地图加载状态
    protected boolean mapViewLoaded=false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        initLayout();
        mDataSource = new DataSource(Constant.SERVER_MAP_URL);
        mHandler = new Handler(this);
        /**
         * 修改它，可以改变mapView交互的一些属性
         */
        mMapOptions = new MapOptions();
        initView();
        initMap();
        initAfaterMap();
        initData();
        initAfaterData();
    }

    private void initMap(){
        mSpinner= (Spinner) findViewById(R.id.ipalmap_spinner);
        mSpinner.setOnItemSelectedListener(this);
        mMapView= (MapView) findViewById(R.id.ipalmap_map);
        mMapOverlayContainer= (RelativeLayout) findViewById(R.id.ipalmap_map_overlay_container);

        mMapView.start();
        mMapView.setMapOptions(mMapOptions);
        mMapView.setOverlayContainer(mMapOverlayContainer);


        showLoadingDialog();

        mDataSource.requestMap(Constant.SINGLE_BUILDING_ID, new DataSource.OnRequestDataEventListener<MapModel>() {
            @Override
            public void onRequestDataEvent(DataSource.ResourceState resourceState, MapModel mapModel) {
                if (resourceState != DataSource.ResourceState.OK) {
                    ToastUtils.showToast(R.string.map_load_fail);
                    hideLoadingDialog();
                    mapViewLoaded=false;
                    return;
                }
                mDataSource.requestPOI(MapModel.POI.get(mapModel), new DataSource.OnRequestDataEventListener<LocationModel>() {
                    @Override
                    public void onRequestDataEvent(DataSource.ResourceState resourceState, final LocationModel locationModel) {
                        if (resourceState != DataSource.ResourceState.OK) {
                            ToastUtils.showToast(R.string.map_load_fail);
                            hideLoadingDialog();
                            mapViewLoaded=false;
                            return;
                        }

                        switch (LocationModel.type.get(locationModel)) {
                            case LocationModel.PLANARGRAPH://平面图
                            case LocationModel.FLOOR://楼层
                                mSpinner.setVisibility(View.GONE);
                                mCurrentFloorId = LocationModel.id.get
                                        (locationModel);
                                mDataSource.requestPlanarGraph(LocationModel.id.get
                                        (locationModel), new DataSource.OnRequestDataEventListener<PlanarGraph>() {
                                    @Override
                                    public void onRequestDataEvent(DataSource.ResourceState resourceState, PlanarGraph planarGraph) {
                                        mMapView.drawPlanarGraph(planarGraph);
                                        hideLoadingDialog();
                                        mapViewLoaded=true;
                                        afterDrawPlanarGraph();
                                    }
                                });
                                break;
                            case LocationModel.BUILDING://建筑物
                                mDataSource.requestPOIChildren(LocationModel.id.get
                                        (locationModel), new DataSource.OnRequestDataEventListener<LocationList>() {
                                    @Override
                                    public void onRequestDataEvent(DataSource.ResourceState resourceState, LocationList locationList) {
                                        if (resourceState != DataSource.ResourceState.OK) {
                                            ToastUtils.showToast(R.string.map_load_fail);
                                            hideLoadingDialog();
                                            mapViewLoaded=false;
                                            return;
                                        }
                                        //楼层切换控件adapter
                                        DataListAdapter<LocationModel> floorAdapter = new DataListAdapter<>(
                                                IpalmapActivity.this,
                                                android.R.layout.simple_spinner_item, locationList,
                                                Constant.FLOOR_SHOW_FIELD);

                                        floorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        mSpinner.setAdapter(floorAdapter);

                                        //设置默认楼层
                                        for (int i = 0; i < floorAdapter.getCount(); i++) {
                                            LocationModel model = floorAdapter.getItem(i);
                                            if (model != null) {
                                                if (FloorModel.default_.get(model)) {
                                                    mSpinner.setSelection(i);
                                                    break;
                                                }
                                            }
                                        }

                                    }
                                });
                                break;
                            default:
                                break;
                        }

                    }
                });

            }
        });
    }


    protected abstract void initLayout();

    protected abstract void initView();

    protected abstract void initAfaterMap();

    protected abstract void initData();

    protected abstract void initAfaterData();

    protected abstract void afterDrawPlanarGraph();


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mMapView!=null){
            mMapView.drop();
        }
        if (mDataSource != null) {
            mDataSource.drop();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        final LocationModel item = (LocationModel) parent.getAdapter().getItem(position);
        //根据floorId，加载地图数据
        mCurrentFloorId = LocationModel.id.get(item);
        mDataSource.requestPlanarGraph(LocationModel.id.get(item),
                new DataSource.OnRequestDataEventListener<PlanarGraph>() {
                    @Override
                    public void onRequestDataEvent(DataSource.ResourceState state, PlanarGraph planarGraph) {
                        hideLoadingDialog();
                        if (state != DataSource.ResourceState.OK) {
                            ToastUtils.showToast(mHandler, R.string.map_load_fail);
                            mapViewLoaded=false;
                            return;
                        }
                        mapViewLoaded=true;
                        mMapView.drawPlanarGraph(planarGraph);
                        afterDrawPlanarGraph();
                    }
                });
    }

    public void showLoadingDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }

    public void hideLoadingDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    protected void backgroundAlpha(float alpha){
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = alpha; //0.0-1.0
        getWindow().setAttributes(lp);
    }

}
