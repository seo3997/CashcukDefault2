package com.cashcuk.ad.adlist;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;

import java.util.ArrayList;

/**
 * 광고 보기 list dapter
 */
public class ListADAdapter extends BaseAdapter{
    private Context mContext;
    private String strMode = ""; //광고 list 인지 관심광고 인지
    private ArrayList<ListADInfo> arrADList;
    private LayoutInflater mInflater;
    private ViewHolder vh = null;

    private Handler handler;
    private Message msg;

    private SparseArray<View> views = new SparseArray<View>();

    private static boolean isCheck = false;

    public ListADAdapter(Context context, ArrayList<ListADInfo> arrADInfo, String strMode, Handler handler){
        mContext = context;
        arrADList = arrADInfo;
        this.strMode = strMode;
        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        this.handler = handler;
        msg = this.handler.obtainMessage();
    }

    @Override
    public int getCount() {
        return arrADList.size();
    }

    @Override
    public Object getItem(int position) {
        return arrADList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = convertView;

        String strTitleImgPath = arrADList.get(position).getStrTitleImgUrl().replace("\\","//");

        if(v==null){
            vh = new ViewHolder();
            v = mInflater.inflate(R.layout.ad_list_item, null);
            vh.llItemBG = (LinearLayout) v.findViewById(R.id.ll_item_bg);
            vh.ivADTitleImg = (ImageView) v.findViewById(R.id.iv_ad);
            vh.txtADName = (TextView) v.findViewById(R.id.txt_ad_nm);
            vh.txtADInfo = (TextView) v.findViewById(R.id.txt_ad_info);
            vh.txtPoint = (TextView) v.findViewById(R.id.txt_point);
            vh.txtStatus = (TextView) v.findViewById(R.id.txt_ad_status);
            vh.ivEvent = (ImageView) v.findViewById(R.id.iv_event);


            v.setTag(vh);
        }else{
            v = convertView;
            vh = (ViewHolder) v.getTag();
        }

        /*
        if(arrADList.get(position).getStrIsRead().equals(String.valueOf(StaticDataInfo.TRUE))){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                vh.llItemBG.setBackground(mContext.getResources().getDrawable(R.drawable.ad_list_read_item_selector));
            }else{
                vh.llItemBG.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.ad_list_read_item_selector));
            }
            vh.llItemBG.setVisibility(View.VISIBLE);
        }else{
            vh.llItemBG.setVisibility(View.GONE);
        }
         */

        Glide
                .with(mContext)
                .load(strTitleImgPath)
                .centerCrop()
                .placeholder(R.drawable.image_none)
                .into(vh.ivADTitleImg);

        vh.txtADName.setText(arrADList.get(position).getStrName());
        vh.txtADInfo.setText(arrADList.get(position).getStrDetail());
        vh.txtPoint.setText(StaticDataInfo.makeStringComma(arrADList.get(position).getStrPoint()));

        vh.txtStatus.setTypeface(null, Typeface.BOLD);
        float scale = mContext.getResources().getDisplayMetrics().density;
        int txtSize = (int) (15*scale);
        vh.txtStatus.setTextSize(TypedValue.COMPLEX_UNIT_PX, txtSize);

        if(arrADList.get(position).getStrStatus().equals(mContext.getResources().getString(R.string.str_ad_status_chk_q))){ //승인요청
            vh.txtStatus.setText(mContext.getResources().getString(R.string.str_ad_status_q));
            vh.txtStatus.setTextColor(mContext.getResources().getColor(R.color.color_ad_status_q));
        }else if(arrADList.get(position).getStrStatus().equals(mContext.getResources().getString(R.string.str_ad_status_chk_a))) { //승인완료
            vh.txtStatus.setText(mContext.getResources().getString(R.string.str_ad_status_a));
            vh.txtStatus.setTextColor(mContext.getResources().getColor(R.color.color_ad_status_a));
        }else if(arrADList.get(position).getStrStatus().equals(mContext.getResources().getString(R.string.str_ad_status_chk_r))) { //승인거부
            vh.txtStatus.setText(mContext.getResources().getString(R.string.str_ad_status_r));
            vh.txtStatus.setTextColor(mContext.getResources().getColor(R.color.color_ad_status_r));
        }else if(arrADList.get(position).getStrStatus().equals(mContext.getResources().getString(R.string.str_ad_status_chk_p))) { //광고중지
            vh.txtStatus.setText(mContext.getResources().getString(R.string.str_ad_status_p));
            vh.txtStatus.setTextColor(mContext.getResources().getColor(R.color.color_ad_status_p));
        }else if(arrADList.get(position).getStrStatus().equals(mContext.getResources().getString(R.string.str_ad_status_chk_i))) { //광고중
            vh.txtStatus.setText(mContext.getResources().getString(R.string.str_ad_status_i));
            vh.txtStatus.setTextColor(mContext.getResources().getColor(R.color.color_ad_status_i));
        }else if(arrADList.get(position).getStrStatus().equals(mContext.getResources().getString(R.string.str_ad_status_chk_op))) { //광고죵료
            vh.txtStatus.setText(mContext.getResources().getString(R.string.str_ad_status_op));
            vh.txtStatus.setTextColor(mContext.getResources().getColor(R.color.color_ad_status_op));
        }else if(arrADList.get(position).getStrStatus().equals(mContext.getResources().getString(R.string.str_ad_status_chk_eq))) { //심사재요청
            vh.txtStatus.setText(mContext.getResources().getString(R.string.str_ad_status_eq));
            vh.txtStatus.setTextColor(mContext.getResources().getColor(R.color.color_ad_status_eq));
        }else if(arrADList.get(position).getStrStatus().equals(mContext.getResources().getString(R.string.str_ad_status_chk_cl))) { //승인요청 취소
            vh.txtStatus.setText(mContext.getResources().getString(R.string.str_ad_status_cl));
            vh.txtStatus.setTextColor(mContext.getResources().getColor(R.color.color_ad_status_cl));
        }else{
            txtSize = (int) (10*scale);
            vh.txtStatus.setTextSize(TypedValue.COMPLEX_UNIT_PX, txtSize);
            vh.txtStatus.setTypeface(null, Typeface.NORMAL);
            vh.txtStatus.setTextColor(mContext.getResources().getColor(R.color.color_main_friend_txt));
            if(arrADList.get(position).getStrGrade().equals("")){
                vh.txtStatus.setText(mContext.getResources().getString(R.string.str_ad_average) + " "+"0.0");
            }else {
                vh.txtStatus.setText(mContext.getResources().getString(R.string.str_ad_average) + " " + arrADList.get(position).getStrGrade());
            }
        }

        if(arrADList.get(position).getStrEventYN().equals(String.valueOf(StaticDataInfo.TRUE))) { //1: 행사중
            vh.ivEvent.setVisibility(View.VISIBLE);
        }else{
            vh.ivEvent.setVisibility(View.GONE);
        }

        views.put(position, v);
        return v;
    }

    class ViewHolder{
        public ImageView ivADTitleImg;
        public TextView txtADName; //광고명
        public TextView txtADInfo; //광고설명
        public TextView txtPoint; // 적립포인트
        public TextView txtStatus; //내 광고: 광고 상태, 적립하기: 평점
        public ImageView ivEvent;
        public LinearLayout llItemBG;
    }

}
