package com.backsutech.map.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.backsutech.map.R;
import com.brtbeacon.map.map3d.entity.BRTPoiEntity;
import java.util.List;


/**
 * Created by tygzx on 2018/3/23.
 */

public class IpalmapSearchAdapter extends BaseAdapter {

    private Context mContent;
    private List<BRTPoiEntity> mData;

    public IpalmapSearchAdapter(Context context, List<BRTPoiEntity> data) {
        mContent = context;
        mData = data;
    }

    public void setData(List<BRTPoiEntity> data) {
        mData = data;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContent).inflate(R.layout.ipalmap_search_list_item, null);
            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.ipalmap_nav_tv_list_item_name);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        BRTPoiEntity entity = mData.get(position);
        viewHolder.tvTitle.setText(entity.getName());
        return convertView;
    }


    class ViewHolder {
        TextView tvTitle;
    }
}
