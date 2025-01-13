package com.cashcuk.pointlist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;

import java.util.ArrayList;

/**
 * 누적 or 사용 포인트 adppter
 */
public class PointLIstAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private ViewHolder vh = null;
    private ArrayList<PointListDataInfo> arrPointInfo = new ArrayList<PointListDataInfo>();

    public PointLIstAdapter(Context context, ArrayList<PointListDataInfo> arrPointInfo){
        mContext = context;
        this.arrPointInfo = arrPointInfo;

        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return arrPointInfo.size();
    }

    @Override
    public Object getItem(int position) {
        return arrPointInfo.get(position);
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
            v = mInflater.inflate(R.layout.point_list_item, null);

            vh.txtDate = (TextView) v.findViewById(R.id.txt_use_date);
            vh.txtContents = (TextView) v.findViewById(R.id.txt_use_nm);
            vh.txtVal = (TextView) v.findViewById(R.id.txt_use_money);
            vh.txtAccount = (TextView) v.findViewById(R.id.txt_account);
            vh.llRequestAccountInfo = (LinearLayout) v.findViewById(R.id.ll_request_account_info);
            vh.txtAccountHolder = (TextView) v.findViewById(R.id.txt_request_nm);
            vh.txtRequestState = (TextView) v.findViewById(R.id.txt_request_state);
            vh.txtEtc = (TextView) v.findViewById(R.id.txt_input_info);
            v.setTag(vh);
        }else{
            v = convertView;
            vh = (ViewHolder) v.getTag();
        }

        vh.txtDate.setText(arrPointInfo.get(position).getStrDate());
        vh.txtContents.setText(arrPointInfo.get(position).getStrContent());
        vh.txtVal.setText(StaticDataInfo.makeStringComma(arrPointInfo.get(position).getStrPoint()));
        vh.txtAccount.setText(arrPointInfo.get(position).getStrAccount());
        vh.txtAccountHolder.setText(arrPointInfo.get(position).getStrAccountHolder());

        if(!arrPointInfo.get(position).getStrAccountHolder().equals("")){
            vh.llRequestAccountInfo.setVisibility(View.VISIBLE);
        }else{
            vh.llRequestAccountInfo.setVisibility(View.GONE);
        }

        if(!arrPointInfo.get(position).getStrRequestState().equals("")) {
            vh.txtRequestState.setVisibility(View.VISIBLE);
            if (arrPointInfo.get(position).getStrRequestState().equals(StaticDataInfo.STRING_Y)) {
                vh.txtRequestState.setText(mContext.getResources().getString(R.string.str_complete));
                vh.txtRequestState.setTextColor(mContext.getResources().getColor(R.color.color_process_complete));
                vh.txtEtc.setVisibility(View.GONE);
            } else if (arrPointInfo.get(position).getStrRequestState().equals(StaticDataInfo.STRING_N)) {
                vh.txtEtc.setVisibility(View.VISIBLE);
                vh.txtEtc.setText(arrPointInfo.get(position).getStrEtc());
                vh.txtRequestState.setText(mContext.getResources().getString(R.string.str_waiting));
                vh.txtRequestState.setTextColor(mContext.getResources().getColor(R.color.color_process_waiting));
            }else if(arrPointInfo.get(position).getStrRequestState().equals("R")){
                vh.txtEtc.setVisibility(View.GONE);
                vh.txtRequestState.setText(mContext.getResources().getString(R.string.str_reject));
                vh.txtRequestState.setTextColor(mContext.getResources().getColor(R.color.color_ad_status_r));
            }
        }else{
            vh.txtRequestState.setVisibility(View.GONE);
        }
        return v;
    }

    class ViewHolder{
        public TextView txtDate; //일자
        public TextView txtContents; //내용 or 사용처
        public TextView txtVal; //누적 or 사용 포인트

        public TextView txtRequestState; //신청상태
        public TextView txtAccount; //계좌번호
        public LinearLayout llRequestAccountInfo; //예금주 정보 layout
        public TextView txtAccountHolder; //예금주
        public TextView txtEtc; //입금처리 설명
    }
}
