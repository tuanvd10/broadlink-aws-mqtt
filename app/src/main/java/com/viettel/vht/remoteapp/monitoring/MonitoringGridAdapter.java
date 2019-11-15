package com.viettel.vht.remoteapp.monitoring;

import android.content.Context;
import android.graphics.drawable.VectorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.viettel.vht.remoteapp.R;

import java.util.List;

public class MonitoringGridAdapter extends BaseAdapter {
    private List<MonitoringStatus> listStatus;
    private LayoutInflater layoutInflater;
    private Context context;

    public MonitoringGridAdapter(Context ctx, List<MonitoringStatus> listData) {
        this.context = ctx;
        this.listStatus = listData;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listStatus.size();
    }

    @Override
    public Object getItem(int position) {
        return listStatus.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.grid_item_layout, null);
            holder = new ViewHolder();
            holder.txtParameter = (TextView) convertView.findViewById(R.id.txtParameter);
            holder.txtValue = (TextView) convertView.findViewById(R.id.txtValue);
            holder.txtUnit = (TextView) convertView.findViewById(R.id.txtUnit);
            holder.flagView = (ImageView) convertView.findViewById(R.id.imageView_flag);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        MonitoringStatus status = this.listStatus.get(position);
        // set value
        holder.txtParameter.setText(status.getName());
        holder.txtValue.setText(status.getValue());
        holder.txtUnit.setText(status.getUnit());
        VectorDrawable vectorDrawable = (VectorDrawable) ResourcesCompat.getDrawable(context.getResources(), getDrawableResIdByName(status.getIconName()), null);
        holder.flagView.setImageDrawable(vectorDrawable);
        // set color
//        holder.txtParameter.setBackgroundColor(context.getResources().getColor(getColorIdByName(status.getQualityLevel().toColor()), null));
        holder.txtValue.setTextColor(context.getResources().getColor(getColorIdByName(status.getQualityLevel().toColor()), null));
        holder.txtUnit.setTextColor(context.getResources().getColor(getColorIdByName(status.getQualityLevel().toColor()), null));
        holder.flagView.setColorFilter(context.getResources().getColor(getColorIdByName(status.getQualityLevel().toColor()), null));

        return convertView;
    }

    public int getDrawableResIdByName(String resName) {
        int resID = context.getResources().getIdentifier(resName, "drawable", context.getPackageName());
        return resID;
    }

    public int getColorIdByName(String resName) {
        int resID = context.getResources().getIdentifier(resName, "color", context.getPackageName());
        return resID;
    }

    static class ViewHolder {
        ImageView flagView;
        TextView txtParameter;
        TextView txtValue;
        TextView txtUnit;
    }
}
