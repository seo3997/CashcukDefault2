package com.cashcuk.advertiser.adcost;

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
 * 총 광고비용 내역 adapter
 */
public class TotalADCostAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<TotalADCostInfo> arrTotalADCostInfo=new ArrayList<TotalADCostInfo>();
    private LayoutInflater mInflater;
    private ViewHolder vh = null;

    public TotalADCostAdapter(Context context, ArrayList<TotalADCostInfo> arrTotalADCostInfo){
        mContext = context;
        this.arrTotalADCostInfo.addAll(arrTotalADCostInfo);
//        this.arrTotalADCostInfo = arrTotalADCostInfo;

        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void updateTotalCostList(ArrayList<TotalADCostInfo> newlist) {
        arrTotalADCostInfo.clear();
        arrTotalADCostInfo.addAll(newlist);
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return arrTotalADCostInfo.size();
    }

    @Override
    public Object getItem(int position) {
        return arrTotalADCostInfo.get(position);
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
            v = mInflater.inflate(R.layout.advertiser_total_ad_cost_item, null);
            vh.txtADName = (TextView) v.findViewById(R.id.txt_ad_name);
            vh.txtTotalCost = (TextView) v.findViewById(R.id.txt_total_ad_cost);
            vh.txtUseCost = (TextView) v.findViewById(R.id.txt_use_cost);
            vh.txtReturnCost = (TextView) v.findViewById(R.id.txt_retun_cost);
            vh.txtViewCnt = (TextView) v.findViewById(R.id.txt_view_cnt);

            v.setTag(vh);
        }else{
            v = convertView;
            vh = (ViewHolder) v.getTag();
        }

        vh.txtADName.setText(arrTotalADCostInfo.get(position).getStrADName());
        vh.txtTotalCost.setText(StaticDataInfo.makeStringComma(arrTotalADCostInfo.get(position).getStrADMomey()));
        vh.txtUseCost.setText(StaticDataInfo.makeStringComma(arrTotalADCostInfo.get(position).getStrUseADCost()));
        if(arrTotalADCostInfo.get(position).getStrReturn()!=null && !arrTotalADCostInfo.get(position).getStrReturn().equals("")) {
            vh.txtReturnCost.setText(StaticDataInfo.makeStringComma(arrTotalADCostInfo.get(position).getStrReturn()));
        }
        vh.txtViewCnt.setText(StaticDataInfo.makeStringComma(arrTotalADCostInfo.get(position).getStrViewCnt()));

        return v;
    }

    class ViewHolder{
        public TextView txtADName; //광고명
        public TextView txtTotalCost; //광고 할 금액
        public TextView txtUseCost; //광고 소비액
        public TextView txtReturnCost; //광고비 반환
        public TextView txtViewCnt; //클릭 수
    }
}
