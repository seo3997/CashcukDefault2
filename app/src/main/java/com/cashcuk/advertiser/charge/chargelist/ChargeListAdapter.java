package com.cashcuk.advertiser.charge.chargelist;

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
 * 누적 or 사용충전금 adapter
 */
public class ChargeListAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private ViewHolder vh = null;
    private ArrayList<ChargeListDataInfo> arrChargeInfo = new ArrayList<ChargeListDataInfo>();

    public ChargeListAdapter(Context context, ArrayList<ChargeListDataInfo> arrChargeInfo){
        mContext = context;
        this.arrChargeInfo = arrChargeInfo;

        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return arrChargeInfo.size();
    }

    @Override
    public Object getItem(int position) {
        return arrChargeInfo.get(position);
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
            v = mInflater.inflate(R.layout.charge_list_item, null);

            vh.txtDate = (TextView) v.findViewById(R.id.txt_use_date);
            vh.txtText = (TextView) v.findViewById(R.id.txt_use_nm);
            vh.txtCharge = (TextView) v.findViewById(R.id.txt_use_money);
            vh.txtInputNm = (TextView) v.findViewById(R.id.txt_input_nm);
            vh.txtBank = (TextView) v.findViewById(R.id.txt_bank);
            vh.txtAccount = (TextView) v.findViewById(R.id.txt_account);
            vh.llRequestAccountInfo = (LinearLayout) v.findViewById(R.id.ll_request_account_info);
            vh.txtAccountHolder = (TextView) v.findViewById(R.id.txt_request_nm);
            vh.txtRequestState = (TextView) v.findViewById(R.id.txt_request_state);
            v.setTag(vh);
        }else{
            v = convertView;
            vh = (ViewHolder) v.getTag();
        }

        vh.txtDate.setText(arrChargeInfo.get(position).getStrDate());
        vh.txtText.setText(arrChargeInfo.get(position).getStrText());
        vh.txtCharge.setText(StaticDataInfo.makeStringComma(arrChargeInfo.get(position).getStrCharge()));

        if(!arrChargeInfo.get(position).getStrAccountHolder().equals("")){
            vh.txtBank.setText(arrChargeInfo.get(position).getStrBank());
            vh.txtAccount.setText(arrChargeInfo.get(position).getStrAccount());
            vh.txtAccountHolder.setText(arrChargeInfo.get(position).getStrAccountHolder());
            vh.llRequestAccountInfo.setVisibility(View.VISIBLE);
        }else{
            vh.llRequestAccountInfo.setVisibility(View.GONE);
        }

        if(!arrChargeInfo.get(position).getStrInputName().equals("")){
            vh.txtInputNm.setText(arrChargeInfo.get(position).getStrInputName());
            vh.txtInputNm.setVisibility(View.VISIBLE);
        }

        if(!arrChargeInfo.get(position).getStrRequestState().equals("")) {
            vh.txtRequestState.setVisibility(View.VISIBLE);
            if (arrChargeInfo.get(position).getStrRequestState().equals(String.valueOf(StaticDataInfo.TRUE))) {
                vh.txtRequestState.setText(mContext.getResources().getString(R.string.str_complete));
                vh.txtRequestState.setTextColor(mContext.getResources().getColor(R.color.color_process_complete));
            } else if (arrChargeInfo.get(position).getStrRequestState().equals(String.valueOf(StaticDataInfo.FALSE))) {
                vh.txtRequestState.setText(mContext.getResources().getString(R.string.str_waiting));
                vh.txtRequestState.setTextColor(mContext.getResources().getColor(R.color.color_process_waiting));
            } else if (arrChargeInfo.get(position).getStrRequestState().equals(String.valueOf(StaticDataInfo.CHARAGE_REJECT))) {
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
        public TextView txtText; //내용 or 사용처
        public TextView txtCharge; //누적 or 사용 충전금

        public TextView txtRequestState; //신청상태
        public TextView txtInputNm;
        public TextView txtBank; //은행
        public TextView txtAccount; //계좌번호
        public LinearLayout llRequestAccountInfo; //예금주 정보 layout
        public TextView txtAccountHolder; //예금주
    }
}
