package com.cashcuk.membership.txtlistdata;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cashcuk.R;

import java.util.ArrayList;

/**
 * 사는 곳 adapter
 */
public class TxtListAdapter extends BaseAdapter {
    private ArrayList<TxtListDataInfo> arrHangOutData;
    private Context mContext;
    private LayoutInflater mInflater;
    private ViewHolder vh = null;

    public TxtListAdapter(Context context, ArrayList<TxtListDataInfo> arrData){
        mContext = context;
        this.arrHangOutData = arrData;

        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return arrHangOutData.size();
    }

    @Override
    public Object getItem(int position) {
        return arrHangOutData.get(position);
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
            v = mInflater.inflate(R.layout.list_txt_item, null);
            vh.txtData = (TextView) v.findViewById(R.id.txt_item);

            v.setTag(vh);
        }else{
            v = convertView;
            vh = (ViewHolder) v.getTag();
        }

        vh.txtData.setText(arrHangOutData.get(position).getStrMsg());

        return v;
    }

    class ViewHolder{
        public TextView txtData;
    }
}
