package com.cashcuk.push;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;

import java.util.ArrayList;

/**
 * PUSH 보관함 adapter
 */
public class PushAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private ViewHolder vh;
    private ArrayList<PushDataInfo> arrData;

    public PushAdapter(Context context, ArrayList<PushDataInfo> arrInfo){
        mContext = context;
        arrData = arrInfo;
        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return arrData.size();
    }

    @Override
    public Object getItem(int position) {
        return arrData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if(v==null){
            vh = new ViewHolder();
            v = mInflater.inflate(R.layout.push_storage_item, null);
            vh.txtADName = (TextView) v.findViewById(R.id.txt_push_name);
            vh.txtDate = (TextView) v.findViewById(R.id.txt_push_date);
            vh.txtPoint = (TextView) v.findViewById(R.id.txt_push_point);

            v.setTag(vh);
        }else{
            v = convertView;
            vh = (ViewHolder) v.getTag();
        }

        vh.txtADName.setText(arrData.get(position).getStrADName());
        vh.txtDate.setText(arrData.get(position).getStrDate());
        vh.txtPoint.setText(StaticDataInfo.makeStringComma(arrData.get(position).getStrPoint()));

        return v;
    }

    public class ViewHolder{
        private TextView txtADName;
        private TextView txtDate;
        private TextView txtPoint;
    }
}
