package com.brtbeacon.map3d.demo.map;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;

import com.brtbeacon.map3d.demo.R;
import com.brtbeacon.map3d.demo.activity.BaseActivity;
import com.brtbeacon.map3d.demo.activity.BaseMapActivity;
import com.brtbeacon.map3d.demo.adapter.GuideListAdapter;
import com.brtbeacon.map3d.demo.entity.GuideItem;
import com.brtbeacon.map3d.demo.entity.MapBundle;
import com.brtbeacon.map3d.demo.map.event.EventClickMapActivity;
import com.brtbeacon.map3d.demo.map.event.EventFloorMapActivity;
import com.brtbeacon.map3d.demo.map.event.EventLoadMapActivity;
import com.brtbeacon.map3d.demo.map.event.EventMapActivity;
import com.brtbeacon.map3d.demo.map.event.PoiMapDiffColorActivity;


import java.util.LinkedList;
import java.util.List;

public class MapGuideActivity extends BaseActivity {

    public static final String ARG_BUILDING_ID = "arg_building_id";
    public static final String ARG_APPKEY = "arg_appkey";
    private ExpandableListView listView;
    private GuideListAdapter guideListAdapter = null;

    private String buildingId = "00230029";
    private String appkey = "d4a6d090d3b04542bcf91e80f933c8e1";

    public static void startActivity(Context context, String buildingId, String appkey) {
        Intent intent = new Intent(context, MapGuideActivity.class);
        intent.putExtra(ARG_BUILDING_ID, buildingId);
        intent.putExtra(ARG_APPKEY, appkey);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_guide);
        listView = findViewById(R.id.listView);
        guideListAdapter = new GuideListAdapter(this, guideList);
        listView.setAdapter(guideListAdapter);
        listView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
                GuideItem item = (GuideItem) guideListAdapter.getChild(i, i1);
                Intent intent = new Intent(MapGuideActivity.this, item.cls);
                MapBundle mapBundle = new MapBundle();
                mapBundle.buildingId = buildingId;
                mapBundle.appkey = appkey;
                intent.putExtra(BaseMapActivity.ARG_MAP_BUNDLE, mapBundle);
                startActivity(intent);
                return true;
            }
        });
        listView.expandGroup(0);
    }

    private static final List<GuideItem> guideList;

    static {
        guideList = new LinkedList<>();

        {
            GuideItem groupItem = new GuideItem("地图事件");
            groupItem.add(new GuideItem("* 地图初始化加载完成", EventLoadMapActivity.class));
            groupItem.add(new GuideItem("* 楼层切换事件", EventFloorMapActivity.class));
            groupItem.add(new GuideItem("* 地图操作事件", EventMapActivity.class));
            groupItem.add(new GuideItem("* 地图点击事件", EventClickMapActivity.class));
            groupItem.add(new GuideItem("* 拾取事件", PoiMapDiffColorActivity.class));

            guideList.add(groupItem);
        }
    }


}
