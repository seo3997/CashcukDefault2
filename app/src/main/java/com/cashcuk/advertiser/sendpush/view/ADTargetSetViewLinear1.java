package com.cashcuk.advertiser.sendpush.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cashcuk.R;
import com.cashcuk.membership.txtlistdata.TxtListDataInfo;

import java.util.ArrayList;

/**
 * 광고 대상 - 대상 설정 View
 */
public class ADTargetSetViewLinear1 extends LinearLayout {
    private Context mContext;
    private LayoutInflater inflater;

    private LinearLayout llCategory; // 지역 view(1, 2, 3차)
    private LinearLayout llADSendInfo;      // 광고발송 기본 정보


    public ADTargetSetViewLinear1(Context context) {
        super(context);

        mContext = context;
        Init();
    }

    /**
     * 생성자
     * @param context
     * @param attrs
     */
    public ADTargetSetViewLinear1(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        Init();
    }

    /**
     * layout 구성
     */
    private void Init() {
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.advertiser_ad_target_set_view, this, true);

        llCategory = (LinearLayout) findViewById(R.id.ll_category);
        llADSendInfo = (LinearLayout) findViewById(R.id.ll_ad_send_info);
    }

    public void setData(PushSendViewInfo mInfo){
        TxtListDataInfo txtInfo;
        String[] arrTmp = null;
        arrTmp = mInfo.getStrAddr().split(":");
        ArrayList<TxtListDataInfo> alAddr = new ArrayList<TxtListDataInfo>();
        for(int i=0; i<arrTmp.length; i++){
            txtInfo = new TxtListDataInfo();
            String[] tmp = arrTmp[i].split(">");
            txtInfo.setStrIdx(tmp[0]);
            txtInfo.setStrMsg(tmp[1]);
            alAddr.add(txtInfo);
        }

        arrTmp = null;
        if (mInfo.getStrAddrSub() == null) {
            mInfo.setStrAddrSub("0>전체");
        }

        arrTmp = mInfo.getStrAddrSub().split(":");
        ArrayList<TxtListDataInfo> alAddrSub = new ArrayList<TxtListDataInfo>();
        for(int i=0; i<arrTmp.length; i++){
            txtInfo = new TxtListDataInfo();
            String[] tmp = arrTmp[i].split(">");
            txtInfo.setStrIdx(tmp[0]);
            txtInfo.setStrMsg(tmp[1]);
            alAddrSub.add(txtInfo);
        }
        CategoryAdd(alAddr, alAddrSub);


        arrTmp = null;
        arrTmp = mInfo.getStrInfo().split(":");
        ArrayList<TxtListDataInfo> alInfo = new ArrayList<TxtListDataInfo>();
        for(int i=0; i<arrTmp.length; i++){
            txtInfo = new TxtListDataInfo();
            String[] tmp = arrTmp[i].split(">");
            txtInfo.setStrIdx(tmp[0]);
            txtInfo.setStrMsg(tmp[1]);
            alInfo.add(txtInfo);
        }

        arrTmp = null;
        arrTmp = mInfo.getStrInfoSub().split(":");
        ArrayList<TxtListDataInfo> alInfoSub = new ArrayList<TxtListDataInfo>();
        for(int i=0; i<arrTmp.length; i++){
            txtInfo = new TxtListDataInfo();
            String[] tmp = arrTmp[i].split(">");
            txtInfo.setStrIdx(tmp[0]);
            txtInfo.setStrMsg(tmp[1]);
            alInfoSub.add(txtInfo);
        }
        sendInfoAdd(alInfo, alInfoSub);
    }

    public void CategoryAdd(ArrayList<TxtListDataInfo> addr, ArrayList<TxtListDataInfo> addrSub) {
//        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View view = inflater.inflate(R.layout.low_category_item, null);

        for(int i=0; i<addr.size(); i++) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.low_category_item, null);

            ((ImageButton) view.findViewById(R.id.ib_category2)).setVisibility(View.GONE);
            ((ImageButton) view.findViewById(R.id.ib_category3)).setVisibility(View.GONE);
            TextView txtCategory2 = (TextView) view.findViewById(R.id.txt_category2);
            txtCategory2.setText(addr.get(i).getStrMsg());
            TextView txtCategory3 = (TextView) view.findViewById(R.id.txt_category3);
            txtCategory3.setText(addrSub.get(i).getStrMsg());

            llCategory.addView(view);
        }
    }

    public void sendInfoAdd(ArrayList<TxtListDataInfo> info, ArrayList<TxtListDataInfo> infoSub){
        for(int i=0; i<info.size(); i++) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.low_common_set_item, null);

            ((ImageButton) view.findViewById(R.id.ib_common_set_value)).setVisibility(View.GONE);
            TextView txt2 = (TextView) view.findViewById(R.id.txt_common_set_name);
            txt2.setText(info.get(i).getStrMsg());
            TextView txt3 = (TextView) view.findViewById(R.id.txt_common_set_value);
            txt3.setText(infoSub.get(i).getStrMsg());

            llADSendInfo.addView(view);
        }

    }
}
