package com.cashcuk.advertiser.sendpush;

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
 * 전체 PUSH 현황 (PUSH 발송 현황) adapter
 */
public class ADPushSendCurrentStateAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<ADPushSendCurrentInfo> arrData;
    private LayoutInflater mInflater;
    private ViewHolder vh;

    public ADPushSendCurrentStateAdapter(Context context, ArrayList<ADPushSendCurrentInfo> arrData){
        mContext = context;
        this.arrData = arrData;

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
            v = mInflater.inflate(R.layout.push_send_current_state_item, null);

            vh.txtADTitle = (TextView) v.findViewById(R.id.txt_ad_name);
            vh.txtSendCnt = (TextView) v.findViewById(R.id.txt_send_cnt);
            vh.txtSendDate = (TextView) v.findViewById(R.id.txt_send_date);
            vh.txtSendPushCost = (TextView) v.findViewById(R.id.txt_send_push_cost);
            vh.txtState = (TextView) v.findViewById(R.id.txt_state);

            v.setTag(vh);
        }else{
            v = convertView;
            vh = (ViewHolder) v.getTag();
        }

        vh.txtADTitle.setText(arrData.get(position).getStrADName());
        vh.txtSendCnt.setText(arrData.get(position).getStrSendNum());
        vh.txtSendDate.setText(arrData.get(position).getStrSendDate());
        vh.txtSendPushCost.setText(StaticDataInfo.makeStringComma(arrData.get(position).getStrSendPushCost()));

        if(arrData.get(position).getStrPushState().equals(mContext.getResources().getString(R.string.str_ad_status_chk_q_push))){
            vh.txtState.setText(mContext.getResources().getString(R.string.str_ad_status_q));
            vh.txtState.setTextColor(mContext.getResources().getColor(R.color.color_ad_status_q));
        } else if(arrData.get(position).getStrPushState().equals(mContext.getResources().getString(R.string.str_ad_status_chk_a_push))){
            vh.txtState.setText(mContext.getResources().getString(R.string.str_ad_status_a));
            vh.txtState.setTextColor(mContext.getResources().getColor(R.color.color_ad_status_a));
        } else if(arrData.get(position).getStrPushState().equals(mContext.getResources().getString(R.string.str_ad_status_chk_r_push))){
            vh.txtState.setText(mContext.getResources().getString(R.string.str_ad_status_r));
            vh.txtState.setTextColor(mContext.getResources().getColor(R.color.color_ad_status_r));
        }

        return v;
    }

    public class ViewHolder{
        private TextView txtADTitle;
        private TextView txtSendCnt;
        private TextView txtSendDate;
        private TextView txtSendPushCost;
        private TextView txtState;
    }
}
