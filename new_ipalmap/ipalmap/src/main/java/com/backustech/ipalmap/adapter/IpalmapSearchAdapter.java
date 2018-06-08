package com.backustech.ipalmap.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.backustech.ipalmap.R;
import com.palmap.gl.model.Feature;
import java.util.List;


/**
 * Created by tygzx on 2018/3/23.
 */

public class IpalmapSearchAdapter extends BaseAdapter {

    private Context mContent;
    private List<Feature> mData;

    public IpalmapSearchAdapter(Context context, List<Feature> data) {
        mContent = context;
        mData = data;
    }

    public void setData(List<Feature> data) {
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

        Feature feature = mData.get(position);
        viewHolder.tvTitle.setText(feature.getDisplay());
        return convertView;
    }


    class ViewHolder {
        TextView tvTitle;
    }
}
