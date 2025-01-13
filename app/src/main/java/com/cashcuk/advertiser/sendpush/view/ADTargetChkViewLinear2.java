package com.cashcuk.advertiser.sendpush.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.advertiser.sendpush.ADTargetSendActivity;

/**
 * 광고 대상 - 2. 발송대상 (세부 설정) 상세보기
 */
public class ADTargetChkViewLinear2 extends LinearLayout {
    private Context mContext;
    private LayoutInflater inflater;
    private String strRequestMode = "";

    public ADTargetChkViewLinear2(Context context) {
        super(context);
        mContext = context;
        init();
    }

    /**
     * 생성자
     *
     * @param context
     * @param attrs
     */
    public ADTargetChkViewLinear2(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    /**
     * layout 구성
     */
    public void init() {
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.advertiser_push_target_chk, this, true);

        ((EditText) findViewById(R.id.et_push_send_cnt)).setFocusable(false);
        ((LinearLayout) findViewById(R.id.ll_info_msg)).setVisibility(View.GONE);
        ((LinearLayout) findViewById(R.id.ll_btns)).setVisibility(View.GONE);
        ((LinearLayout) findViewById(R.id.ll_charge)).setVisibility(View.GONE);
    }

    public void setData(final PushSendViewInfo mInfo) {
        ((TextView) findViewById(R.id.txt_push_target_cnt)).setText(StaticDataInfo.makeStringComma(mInfo.getStrTargetNum()));
        EditText etSendCnt = (EditText) findViewById(R.id.et_push_send_cnt);
        etSendCnt.setText(StaticDataInfo.makeStringComma(mInfo.getStrPushSendNum()));
        etSendCnt.setFocusable(false);
        TextView txtSendCost = (TextView) findViewById(R.id.txt_push_send_amount);
        txtSendCost.setText(StaticDataInfo.makeStringComma(mInfo.getStrSendCost()));

        if (!etSendCnt.getText().toString().trim().equals("") && !etSendCnt.getText().toString().trim().startsWith("-")
                && !txtSendCost.getText().toString().trim().equals("") && !txtSendCost.getText().toString().trim().startsWith("-")) {
            long mSendNum = Long.valueOf(etSendCnt.getText().toString().trim().replaceAll(",", ""));
            long mSendCost = Long.valueOf(txtSendCost.getText().toString().trim().replaceAll(",", ""));

            ((TextView) findViewById(R.id.txt_push_send_total_cost)).setText(StaticDataInfo.makeStringComma(String.valueOf(mSendNum * mSendCost)));
        }

        TextView txtPushState = (TextView) findViewById(R.id.txt_push_state);
        if (mInfo.getStrPushState().equals(mContext.getResources().getString(R.string.str_ad_status_chk_q_push))) { //승인요청
            txtPushState.setText(mContext.getResources().getString(R.string.str_ad_status_q));
        } else if (mInfo.getStrPushState().equals(mContext.getResources().getString(R.string.str_ad_status_chk_a_push))) { //승인완료
            txtPushState.setText(mContext.getResources().getString(R.string.str_ad_status_a));
        } else if (mInfo.getStrPushState().equals(mContext.getResources().getString(R.string.str_ad_status_chk_r_push))) { //승인거부
            txtPushState.setText(mContext.getResources().getString(R.string.str_ad_status_r));
        } else if (mInfo.getStrPushState().equals(mContext.getResources().getString(R.string.str_ad_status_chk_p_push))) { //광고중지
            txtPushState.setText(mContext.getResources().getString(R.string.str_ad_status_p));
        } else if (mInfo.getStrPushState().equals(mContext.getResources().getString(R.string.str_ad_status_chk_i_push))) { //광고중
            txtPushState.setText(mContext.getResources().getString(R.string.str_ad_status_i));
        } else if (mInfo.getStrPushState().equals(mContext.getResources().getString(R.string.str_ad_status_chk_op))) { //광고죵료
            txtPushState.setText(mContext.getResources().getString(R.string.str_ad_status_op));
        } else if (mInfo.getStrPushState().equals(mContext.getResources().getString(R.string.str_ad_status_chk_eq))) { //승인재요청
            txtPushState.setText(mContext.getResources().getString(R.string.str_ad_status_eq));
        } else if (mInfo.getStrPushState().equals(mContext.getResources().getString(R.string.str_ad_status_chk_cl))) { //승인요청 취소
            txtPushState.setText(mContext.getResources().getString(R.string.str_ad_status_cl));
        }

        TextView tvPushDate = (TextView) findViewById(R.id.txt_push_date);
        TextView tvPushTime = (TextView) findViewById(R.id.txt_push_time);
        String strPushDate = mInfo.getStrPushDate();
        if (strPushDate != null && !strPushDate.equals("")) {
            String[] settingPushDate = strPushDate.split(">");
            tvPushDate.setText(settingPushDate[0]);
            tvPushTime.setText(settingPushDate[1]);
        }

        ImageView ivPushAdd = (ImageView) findViewById(R.id.iv_target_set_push_add);
        String strPushImgUrl = mInfo.getStrPushImgUrl();
        ivPushAdd.setVisibility(View.VISIBLE);
        Glide
                .with(mContext)
                .load(strPushImgUrl)
                .centerCrop()
                .placeholder(R.drawable.image_none)
                .into(ivPushAdd);

        ((ImageView) findViewById(R.id.iv_target_set_push)).setVisibility(View.GONE);

        Button btnReSend = (Button) findViewById(R.id.btn_push_resend);
        if (mInfo.getStrPushYN().equals(StaticDataInfo.STRING_Y) || mInfo.getStrPushState().equals(mContext.getResources().getString(R.string.str_ad_status_chk_r_push))) {
            btnReSend.setVisibility(View.VISIBLE);
            if(mInfo.getStrPushState().equals(mContext.getResources().getString(R.string.str_ad_status_chk_r_push))){
                btnReSend.setText(mContext.getResources().getString(R.string.str_push_again_request));
                strRequestMode = mContext.getResources().getString(R.string.str_ad_status_chk_r_push);
            }else{
                btnReSend.setText(mContext.getResources().getString(R.string.str_push_resend));
                strRequestMode = mContext.getResources().getString(R.string.str_ad_status_chk_a_push);
            }
        }else{
            btnReSend.setVisibility(View.GONE);
        }

        btnReSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ADTargetSendActivity.class);
                intent.putExtra("RequestMode", strRequestMode);
                intent.putExtra("DataInfo", mInfo);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mContext.startActivity(intent);
                ((Activity)mContext).finish();
            }
        });
    }
}
