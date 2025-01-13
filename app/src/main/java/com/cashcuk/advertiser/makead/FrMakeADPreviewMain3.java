package com.cashcuk.advertiser.makead;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.TitleBar;
import com.cashcuk.common.ImageLoader;
import com.cashcuk.dialog.DlgBtnActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 광고제작 - 프리뷰 (광고정보)
 */
public class FrMakeADPreviewMain3 extends Fragment implements View.OnClickListener, OnMapReadyCallback {
    private FragmentActivity mActivity;
    private LinearLayout llPreviewDetailImg; //광고 상세 이미지
    private ImageView ivPreviewTitle; //광고 타이틀 img view
    private String strTitleImgPath; //광고 타이틀 이미지
    private boolean[] arrIsChangeDetaillmg;
    private boolean isChangeTitleIlmg;
    private boolean isRejudged = false; //재심사 여부
    private ArrayList<String> arrDetailImg = new ArrayList<String>();
    private LinearLayout llProgress;
    private ProgressBar pbTitleImg;

    private String strADIdx; //광고 idx
    private String strADName; //광고명
    private String strADDetail; //상세정보
    private String strADCategory; //카테고리
    private String strADAmount; //광고 할 금액
    private String strADSavePoint; //적립 포인트
    private String strADStartDate; //광고기간
    private String strADEndDate; //광고기간
    private String strADAddr; //주소
    private String strHomepage; //홈페이지
    private String strEvent; //이벤트 여부
    private String strADRecommend; //광고주 추천
    private String strADIsRejudged; //재심사 여부
    private String strADStatus = ""; //광고상태
    private String strMyCharge = ""; //충전금
    private String strUpCost = ""; //증액 금액
    private boolean isUpCost = false; //증액 금액 존재 여부, 있으면 수정 금액 변경 url 호출 후 광고 수정 url 호출, 없으면 광고 수정 url 호출

    public static final String STR_TITLE_IMG = "TITLE_IMG";
    public static final String STR_CHANGE_TITLE_IMG = "CHANGE_TITLE_IMG";
    public static final String STR_DETAIL_IMG = "DTAIL_IMG";
    public static final String STR_CHANGE_DETAIL_IMG = "CHANGE_DTAIL_IMG";

    private ImageView ivMapErr; //지도 표시 오류
    private LinearLayout llMap; //지도

    private final String STR_AD_NM = "ad_nm";
    private final String STR_AD_INFO_MSG = "ad_txt";
    private final String STR_AD_CATEGORY = "ad_ctg";
    private final String STR_AD_AMOUNT = "ad_amnt";
    private final String STR_AD_SAVE_POINT = "ad_pnt";
    private final String STR_AD_DATE_S = "ad_str";
    private final String STR_AD_DATE_E = "ad_end";
    private final String STR_AD_ADDR = "ad_geo";
    private final String STR_AD_HOMEPAGE = "ad_url";
    private final String STR_AD_EVENT = "ad_event";
    private final String STR_AD_STATUS = "ad_status";
    private final String STR_AD_MY_CHARGE = "chrg_amnt";
    private final String STR_AD_RECOMMENDR = "ad_seller";
    private final String STR_AD_THUMBNAIL = "ad_thumbnail";
    private final String STR_AD_TITLE_IMG = "ad_titleimg";
    private final String STR_AD_DETAIL_IMG = "ad_dtlimg";

    private final String STR_AD_IDX = "ad_idx";
    private final String STR_AD_REJUDGED = "rejudged";

    private final int SEND_AD_CHANGE_IDX = 2; //광고 금액 변경 (광고 idx)
    private final int SEND_AD_CHANGE_COST= 3; //광고 금액 변경 (증액 금액)
    private final String STR_AD_CHANGE_IDX = "ad_idx"; //광고 금액 변경 (광고 idx)
    private final String STR_AD_CHANGE_COST= "up_cost"; //광고 금액 변경 (증액 금액)

    private final int SEND_AD_NM = 2;
    private final int SEND_AD_INFO_MSG = 3;
    private final int SEND_AD_CATEGORY = 4;
    private final int SEND_AD_AMOUNT = 5;
    private final int SEND_AD_SAVE_POINT = 6;
    private final int SEND_AD_DATE_S = 7;
    private final int SEND_AD_DATE_E = 8;
    private final int SEND_AD_ADDR = 9;
    private final int SEND_AD_HOMEPAGE = 10;
    private final int SEND_AD_EVENT = 11;
    private final int SEND_AD_RECOMMENDR = 12;
    private final int SEND_AD_CHARGE = 13;
    private final int SEND_AD_STATUS = 14;
    private final int SEND_AD_REJUDGED = 15;
    private final int SEND_AD_IDX = 16;

    /**
     * 결과 값 받는 handler
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case StaticDataInfo.RESULT_CODE_ERR:
                    Toast.makeText(mActivity, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
                    break;
            }

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
                }
            },500);
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (FragmentActivity)activity;
        try {
        } catch (ClassCastException e) {
            throw new ClassCastException(mActivity.toString() + " must implement OnArticleSelectedListener");
        }
    }

    public static FrMakeADPreviewMain3 newInstance(boolean isChangeTitleImg, String strTitle, ArrayList<String> arrBitmapData, boolean[] isChangeDetailImg) {
        FrMakeADPreviewMain3 fragment = new FrMakeADPreviewMain3();
        Bundle args = new Bundle();
        args.putString(STR_TITLE_IMG, strTitle);
        args.putBoolean(STR_CHANGE_TITLE_IMG, isChangeTitleImg);
        args.putStringArrayList(STR_DETAIL_IMG, arrBitmapData);
        args.putBooleanArray(STR_CHANGE_DETAIL_IMG, isChangeDetailImg);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            strADIdx = (String) getArguments().getString(MakeADDetail1.STR_PUT_AD_IDX);
            strADName = (String) getArguments().getString(MakeADDetail1.STR_PUT_AD_NAME);
            strADDetail = (String) getArguments().getString(MakeADDetail1.STR_PUT_AD_DETAIL);
            strADCategory = (String)getArguments().getString(MakeADDetail1.STR_PUT_AD_CATEGORY);
            strADAmount = (String) getArguments().getString(MakeADDetail1.STR_PUT_AD_AMOUNT);
            strADSavePoint = (String) getArguments().getString(MakeADDetail1.STR_PUT_AD_SAVE_POINT);
            strADStartDate = (String) getArguments().getString(MakeADDetail1.STR_PUT_AD_DATE_S);
            strADEndDate = (String) getArguments().getString(MakeADDetail1.STR_PUT_AD_DATE_E);
            strADAddr = (String) getArguments().getString(MakeADDetail1.STR_PUT_AD_ADDR);
            strHomepage = (String) getArguments().getString(MakeADDetail1.STR_PUT_AD_HOMEPAGE);
            strEvent = (String) getArguments().getString(MakeADDetail1.STR_PUT_AD_EVENT);
            strADRecommend = (String) getArguments().getString(MakeADDetail1.STR_PUT_AD_RECOMMEND);
            strADIsRejudged = (String) getArguments().getString(MakeADDetail1.STR_PUT_AD_REJUDGED);
            strADStatus = (String) getArguments().getString(MakeADDetail1.STR_PUT_AD_STATUS);
            strMyCharge = (String) getArguments().getString(MakeADDetail1.STR_PUT_AD_MY_CHARGE);
            strUpCost = (String) getArguments().getString(MakeADDetail1.STR_PUT_AD_UP_COST);

            if(strUpCost!=null && !strUpCost.trim().equals("") && !strUpCost.equals("0") && !strADStatus.equals(getResources().getString(R.string.str_ad_status_chk_r))) {
                isUpCost = true;
            }else{
                isUpCost = false;
            }
            strTitleImgPath = (String) getArguments().getString(STR_TITLE_IMG);
            arrIsChangeDetaillmg = getArguments().getBooleanArray(STR_CHANGE_DETAIL_IMG);
            isChangeTitleIlmg = getArguments().getBoolean(STR_CHANGE_TITLE_IMG);

            ArrayList<String> arrDetailImgTemp = (ArrayList<String>) getArguments().getStringArrayList(STR_DETAIL_IMG);
            if(arrDetailImgTemp!=null && arrDetailImgTemp.size()>0) {
                arrDetailImg.addAll((ArrayList<String>) getArguments().getStringArrayList(STR_DETAIL_IMG));
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate( R.layout.ad_detail_main_1, container, false );
        llProgress = (LinearLayout) view.findViewById(R.id.ll_progress_circle);
        ((TitleBar) view.findViewById(R.id.title_bar)).setTitle(mActivity.getResources().getString(R.string.str_make_ad));
        ((TextView) view.findViewById(R.id.txt_ad_name)).setText(strADName);
        ((TextView) view.findViewById(R.id.txt_ad_detail)).setText(strADDetail);
        ((TextView) view.findViewById(R.id.txt_grade)).setText("3.0");
        ((ImageView) view.findViewById(R.id.iv_grade1)).setImageDrawable(mActivity.getResources().getDrawable(R.drawable.star_press));
        ((ImageView) view.findViewById(R.id.iv_grade2)).setImageDrawable(mActivity.getResources().getDrawable(R.drawable.star_press));
        ((ImageView) view.findViewById(R.id.iv_grade3)).setImageDrawable(mActivity.getResources().getDrawable(R.drawable.star_press));

        Button btnRegiRequest = (Button) view.findViewById(R.id.btn_one);
        btnRegiRequest.setVisibility(View.VISIBLE);
        btnRegiRequest.setText(mActivity.getResources().getString(R.string.str_registration_request));
        btnRegiRequest.setOnClickListener(this);

        ivMapErr = (ImageView) view.findViewById(R.id.iv_map_err);
        llMap = (LinearLayout) view.findViewById(R.id.ll_map);
        llPreviewDetailImg = (LinearLayout) view.findViewById(R.id.ll_ad_detail_img);
        ivPreviewTitle = (ImageView) view.findViewById(R.id.iv_ad_title);
        pbTitleImg = (ProgressBar) view.findViewById(R.id.pb_title_img);

        SetDetailImg();
        return view;
    }

    /**
     * 상세 이미지 d/p
     */
    public void SetDetailImg() {
        new getLatLng().execute(strADAddr);

        strTitleImgPath = strTitleImgPath.replace("\\", "//");
        if (isChangeTitleIlmg) {
            Bitmap bitTitle = decodeSampledPreviewBitmapFromPath(strTitleImgPath, Integer.parseInt(getResources().getString(R.string.str_ad_char_w)), Integer.parseInt(getResources().getString(R.string.str_ad_h)));
            ivPreviewTitle.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ivPreviewTitle.setBackground(new BitmapDrawable(bitTitle));
            }else{
                ivPreviewTitle.setBackgroundDrawable(new BitmapDrawable(bitTitle));
            }
        } else {
            if (pbTitleImg != null && !pbTitleImg.isShown()) pbTitleImg.setVisibility(View.VISIBLE);

            ImageLoader.loadImage(mActivity, strTitleImgPath, ivPreviewTitle, pbTitleImg);

        }

        if (arrDetailImg != null) {
            for(int i=0; i<arrDetailImg.size(); i++) {
                LayoutInflater inflaterDetailImg = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = inflaterDetailImg.inflate(R.layout.advertiser_make_ad_detail_img_add, null);

                ((ImageView) view.findViewById(R.id.iv_img_ad_detail)).setVisibility(View.GONE);
                ((TextView) view.findViewById(R.id.txt_detail)).setVisibility(View.GONE);
                llPreviewDetailImg.addView(view);

                if(arrIsChangeDetaillmg[i]) {
                    Bitmap bImgDetail = decodeSampledPreviewBitmapFromPath(arrDetailImg.get(i), Integer.parseInt(getResources().getString(R.string.str_ad_char_w)), Integer.parseInt(getResources().getString(R.string.str_ad_h)));

                    LinearLayout llDetailImg = (LinearLayout) llPreviewDetailImg.getChildAt(i).findViewById(R.id.ll_detail_img);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        llDetailImg.setBackground(new BitmapDrawable(bImgDetail));
                    } else {
                        llDetailImg.setBackgroundDrawable(new BitmapDrawable(bImgDetail));
                    }
                }else{
                    LinearLayout llDetailImg = (LinearLayout) llPreviewDetailImg.getChildAt(i).findViewById(R.id.ll_detail_img);
                    llDetailImg.setVisibility(View.GONE);

                    ImageView ivDetailImg = (ImageView) view.findViewById(R.id.iv_detail_img);
                    ivDetailImg.setVisibility(View.VISIBLE);

                    final ProgressBar pbDetailImg = (ProgressBar) view.findViewById(R.id.pb_detail_img);
                    if (pbDetailImg != null && pbDetailImg.isShown()) pbDetailImg.setVisibility(View.GONE);
                    ImageLoader.loadImage(mActivity, arrDetailImg.get(i).replace("\\", ""), ivDetailImg, pbDetailImg);

                }
            }
        }

        resizeImg();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_one){
            if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);
            DataRequest(isUpCost);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
    }

    /**
     * 서버로 전송하는 값
     */
    public void DataRequest(boolean isCostChange){

        String url = "";
        if(strADIsRejudged.equals(MakeADDetail1.STR_NOT_MODIFY)) {
            url = getResources().getString(R.string.str_new_url) + getResources().getString(R.string.str_advertise_advertise);
        }else{
            if(isCostChange) {
                url = getResources().getString(R.string.str_new_url) + getResources().getString(R.string.str_advertise_ad_change_amount);
            }else{
                url = getResources().getString(R.string.str_new_url) + getResources().getString(R.string.str_advertise_advertise_edit);
            }
        }
        SharedPreferences pref = mActivity.getSharedPreferences("TokenInfo", mActivity.MODE_PRIVATE);
        final String token = pref.getString(getResources().getString(R.string.str_token), "");

        HashMap<Integer, String> k_param = new HashMap<Integer, String>();
        k_param.put(StaticDataInfo.SEND_URL, url);
        k_param.put(StaticDataInfo.SEND_TOKEN, token);
        if(isCostChange){
            k_param.put(SEND_AD_CHANGE_IDX, strADIdx);
            k_param.put(SEND_AD_CHANGE_COST, strUpCost);
        }else {
            k_param.put(SEND_AD_NM, strADName);
            k_param.put(SEND_AD_INFO_MSG, strADDetail);
            k_param.put(SEND_AD_CATEGORY, strADCategory);
            k_param.put(SEND_AD_AMOUNT, strADAmount);
            k_param.put(SEND_AD_SAVE_POINT, strADSavePoint);
            k_param.put(SEND_AD_DATE_S, strADStartDate);
            k_param.put(SEND_AD_DATE_E, strADEndDate);
            k_param.put(SEND_AD_ADDR, strADAddr);
            k_param.put(SEND_AD_HOMEPAGE, strHomepage);
            k_param.put(SEND_AD_EVENT, strEvent);
            k_param.put(SEND_AD_RECOMMENDR, strADRecommend);
            k_param.put(SEND_AD_CHARGE, strMyCharge.replace(",", ""));
            k_param.put(SEND_AD_STATUS, strADStatus);
            k_param.put(SEND_AD_REJUDGED, strADIsRejudged);
            if (strADIdx != null && !strADIdx.equals("")) {
                k_param.put(SEND_AD_IDX, strADIdx);
            }
        }

        String[] strTask = new String[k_param.size()];
        for (int i = 0; i < strTask.length; i++) {
            strTask[i] = k_param.get(i);
        }

        if(isCostChange) {
            if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);
            new ChangeAmountTask().execute(strTask);
        }else{
            new DataTask().execute(strTask);
        }
    }

    private class DataTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String retMsg = "";
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            builder.addTextBody(getResources().getString(R.string.str_token), params[StaticDataInfo.SEND_TOKEN], ContentType.create("Multipart/related", "UTF-8"));
            builder.addTextBody(STR_AD_NM, params[SEND_AD_NM], ContentType.create("Multipart/related", "UTF-8"));
            builder.addTextBody(STR_AD_INFO_MSG, params[SEND_AD_INFO_MSG], ContentType.create("Multipart/related", "UTF-8"));
            builder.addTextBody(STR_AD_CATEGORY, params[SEND_AD_CATEGORY], ContentType.create("Multipart/related", "UTF-8"));
            builder.addTextBody(STR_AD_AMOUNT, params[SEND_AD_AMOUNT], ContentType.create("Multipart/related", "UTF-8"));
            builder.addTextBody(STR_AD_SAVE_POINT, params[SEND_AD_SAVE_POINT], ContentType.create("Multipart/related", "UTF-8"));
            builder.addTextBody(STR_AD_DATE_S, params[SEND_AD_DATE_S], ContentType.create("Multipart/related", "UTF-8"));
            builder.addTextBody(STR_AD_DATE_E, params[SEND_AD_DATE_E], ContentType.create("Multipart/related", "UTF-8"));
            builder.addTextBody(STR_AD_ADDR, params[SEND_AD_ADDR], ContentType.create("Multipart/related", "UTF-8"));
            builder.addTextBody(STR_AD_HOMEPAGE, params[SEND_AD_HOMEPAGE], ContentType.create("Multipart/related", "UTF-8"));
            builder.addTextBody(STR_AD_EVENT, params[SEND_AD_EVENT], ContentType.create("Multipart/related", "UTF-8"));
            builder.addTextBody(STR_AD_RECOMMENDR, params[SEND_AD_RECOMMENDR], ContentType.create("Multipart/related", "UTF-8"));
            builder.addTextBody(STR_AD_MY_CHARGE, params[SEND_AD_CHARGE], ContentType.create("Multipart/related", "UTF-8"));
            if(strADStatus!=null && !strADStatus.equals("")) {
                builder.addTextBody(STR_AD_STATUS, params[SEND_AD_STATUS], ContentType.create("Multipart/related", "UTF-8"));
            }

            builder.addTextBody(STR_AD_REJUDGED, params[SEND_AD_REJUDGED], ContentType.create("Multipart/related", "UTF-8"));
            if (strADIdx != null && !strADIdx.equals("")) {
                builder.addTextBody(STR_AD_IDX, params[SEND_AD_IDX], ContentType.create("Multipart/related", "UTF-8"));
            }

            if (isChangeTitleIlmg) {
                builder.addPart(STR_AD_TITLE_IMG, contentTitle);
                builder.addPart(STR_AD_THUMBNAIL, contentThumbnail);
            } else {
                builder.addTextBody(STR_AD_TITLE_IMG, strTitleImgPath, ContentType.create("Multipart/related", "UTF-8"));
            }

            int mContentDetail = 0;
            for (int i = 0; i < arrDetailImg.size(); i++) {
                String strDetailImgTag = STR_AD_DETAIL_IMG + (i + 1);
                if (arrIsChangeDetaillmg[i]) {
                    builder.addPart(strDetailImgTag, contentDetail.get(mContentDetail++));
                } else {
                    builder.addTextBody(strDetailImgTag, arrDetailImg.get(i), ContentType.create("Multipart/related", "UTF-8"));
                }
            }


            // Send Request
            InputStream inputStream = null;
            HttpParams httpParams = new BasicHttpParams();
            httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
            HttpClient client = new DefaultHttpClient(httpParams);
            HttpPost post = new HttpPost(params[StaticDataInfo.SEND_URL]);

            httpParams = client.getParams();
//            HttpConnectionParams.setConnectionTimeout(httpParams, StaticDataInfo.TIME_OUT);
//            HttpConnectionParams.setSoTimeout(httpParams, StaticDataInfo.TIME_OUT);
            post.setEntity(builder.build());
            HttpResponse responsePOST = null;
            try {
                responsePOST = client.execute(post);
                HttpEntity resEntity = responsePOST.getEntity();

                if (resEntity != null) {
                    retMsg = EntityUtils.toString(resEntity);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return retMsg;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (llProgress != null && llProgress.isShown())
                        llProgress.setVisibility(View.GONE);
                }
            }, 500);

            if(result.trim().equals("")){
                Toast.makeText(mActivity, mActivity.getResources().getString(R.string.str_ad_regi_err), Toast.LENGTH_SHORT).show();
                return;
            }

            Message msg = new Message();
            if(result.equals(String.valueOf(StaticDataInfo.RESULT_CODE_200))) {
                Toast.makeText(mActivity, getResources().getString(R.string.str_ad_regi_success), Toast.LENGTH_SHORT).show();
                if(llProgress!=null && llProgress.isShown()) llProgress.setVisibility(View.GONE);
                ((MakeADPreviewActivity) getActivity()).finishAdd();
            }else{
                Toast.makeText(mActivity, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private ContentBody contentTitle;
    private ContentBody contentThumbnail;
    private ArrayList<ContentBody> contentDetail;
    private void resizeImg(){
        if(isChangeTitleIlmg) {
            ByteArrayBody bTitleImg = decodeSampledBitmapFromPath(strTitleImgPath, Integer.parseInt(mActivity.getResources().getString(R.string.str_ad_char_w)), Integer.parseInt(mActivity.getResources().getString(R.string.str_ad_h))); //광고 타이틀
            //타이틀 썸네일
            ByteArrayBody bImg = decodeSampledBitmapFromPath(strTitleImgPath, Integer.parseInt(getResources().getString(R.string.str_char_thumbnail_w)), Integer.parseInt(getResources().getString(R.string.str_char_thumbnail_h)));

            if (bTitleImg != null && bImg != null) {
                contentTitle = bTitleImg;
                contentThumbnail = bImg;
            } else {
                Intent intent = new Intent(mActivity, DlgBtnActivity.class);
                intent.putExtra("BtnDlgMsg", mActivity.getResources().getString(R.string.str_ad_img_err));
                startActivity(intent);

                return;
            }
        }

        if(arrDetailImg.size()>0){
            contentDetail = new ArrayList<ContentBody>();
            for (int i = 0; i < arrDetailImg.size(); i++) {
                File oFile = new File(arrDetailImg.get(i));
                if(oFile.exists()) {
                    ByteArrayBody bDetailImg = decodeSampledBitmapFromPath(arrDetailImg.get(i), Integer.parseInt(mActivity.getResources().getString(R.string.str_ad_char_w)), Integer.parseInt(mActivity.getResources().getString(R.string.str_ad_h))); //광고 detail
                    contentDetail.add(bDetailImg);
                }
            }
        }

    }

    /** @Author : pppdw
     * @Description : 구글 URL을 이용해 간단하게 lng.lat를 뽑는다. new HttpGet 생성자에 사용된 URL로 뽑고자하는 지역의 네임값만 날리면된다.
     *                  단 네임값에 공백이 있으면 안되며, 공백이 존재 할 시 공백을 +로 변경하여 리퀘스트 요청을 해야한다.
     * @Param : strPlaceName --> 지오코딩 하고자 하는 지역의 이름 (예시 : "서울특별시+강남구+개포동+3421번지")
     **/

    private class getLatLng extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String strPlaceNameClone = null;
            strPlaceNameClone = params[0].replace(" ", "+");
            JSONObject jsonObject = new JSONObject();

            HttpGet httpGet = new HttpGet("http://maps.google.com/maps/api/geocode/json?address=" +strPlaceNameClone+"&ka&sensor=false");
            HttpParams httpParams = new BasicHttpParams();
            httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
            HttpClient client = new DefaultHttpClient(httpParams);
            HttpResponse response;
            StringBuilder stringBuilder = new StringBuilder();

            try {
                httpParams = client.getParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, StaticDataInfo.TIME_OUT);
                HttpConnectionParams.setSoTimeout(httpParams, StaticDataInfo.TIME_OUT);
                response = client.execute(httpGet);
                HttpEntity entity = response.getEntity();
                InputStream stream = entity.getContent();
                int b;
                while ((b = stream.read()) != -1) {
                    stringBuilder.append((char) b);
                }
            } catch (ClientProtocolException e) {
            } catch (IOException e) {
            }

            try {
                jsonObject = new JSONObject(stringBuilder.toString());
            } catch (JSONException e) {

                e.printStackTrace();
            }

            Double lng = new Double(0);
            Double lat = new Double(0);
            String strLocation = "";

            try {
                lng = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                        .getJSONObject("geometry").getJSONObject("location")
                        .getDouble("lng");

                lat = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                        .getJSONObject("geometry").getJSONObject("location")
                        .getDouble("lat");

                strLocation = String.valueOf(lat)+"/"+String.valueOf(lng);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return strLocation;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(result==null || result.equals("")){
                ivMapErr.setVisibility(View.VISIBLE);
                llMap.setVisibility(View.GONE);
            }else {
                ivMapErr.setVisibility(View.GONE);
                llMap.setVisibility(View.VISIBLE);
                displayMap(result);
            }
        }
    }

    /**
     * map에 위치 표시
     * @param location
     */
    private LatLng position;
    private String[] strLocation;
    public void displayMap(String location) {
        strLocation = location.split("/");
        position = new LatLng(Double.parseDouble(strLocation[0]), Double.parseDouble(strLocation[1]));

        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map_ad);
        if (mapFragment == null) {
            ivMapErr.setVisibility(View.VISIBLE);
            llMap.setVisibility(View.GONE);
            return;
        }

        mapFragment.getMapAsync(this);
    }

    private GoogleMap mMap;
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //맵 위치이동
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));

        //마커설정
        MarkerOptions opt = new MarkerOptions();
        opt.position(position);
        mMap.addMarker(opt).showInfoWindow();

        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.setOnMapClickListener(mMapClick);
    }

    private GoogleMap.OnMapClickListener mMapClick = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {
            if(strLocation!=null) {
                Uri gmmIntentUri = Uri.parse("geo:" + strLocation[0] + "," + strLocation[1]+"?q="+strLocation[0] + "," + strLocation[1]);

                // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                // Make the Intent explicit by setting the Google Maps package
                mapIntent.setPackage("com.google.android.apps.maps");
                // Attempt to start an activity that can handle the Intent
                startActivity(mapIntent);
            }
        }
    };

    /**
     * 서버에 값 요청
     */
    private class ChangeAmountTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String retMsg = "";

            try {
                HttpParams httpParams = new BasicHttpParams();
                httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
                HttpClient client = new DefaultHttpClient(httpParams);
                HttpPost post = new HttpPost(params[StaticDataInfo.SEND_URL]);

                List<NameValuePair> listParams = new ArrayList<NameValuePair>();
                listParams.add(new BasicNameValuePair(getResources().getString(R.string.str_token), params[StaticDataInfo.SEND_TOKEN]));
                listParams.add(new BasicNameValuePair(STR_AD_CHANGE_IDX, params[SEND_AD_CHANGE_IDX]));
                listParams.add(new BasicNameValuePair(STR_AD_CHANGE_COST, params[SEND_AD_CHANGE_COST]));
                
                httpParams = client.getParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, StaticDataInfo.TIME_OUT);
                HttpConnectionParams.setSoTimeout(httpParams, StaticDataInfo.TIME_OUT);
                UrlEncodedFormEntity ent = new UrlEncodedFormEntity(listParams, HTTP.UTF_8);
                post.setEntity(ent);
                HttpResponse responsePOST = client.execute(post);
                HttpEntity resEntity = responsePOST.getEntity();

                if (resEntity != null) {
                    retMsg = EntityUtils.toString(resEntity);
                }
            } catch (Exception e) {
                retMsg = e.toString();
            }

            return retMsg;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(result.equals(String.valueOf(StaticDataInfo.RESULT_CODE_200))){

                DataRequest(false);
            }else{
                handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_ERR);
            }
        }
    }

    private Bitmap decodeSampledPreviewBitmapFromPath(String path, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        Bitmap src = BitmapFactory.decodeFile(path, options);
        src = src.createScaledBitmap(src, reqWidth, reqHeight, false);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        src.compress(Bitmap.CompressFormat.PNG, 100, bos);
        return src;
    }

    private ByteArrayBody decodeSampledBitmapFromPath(String path, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        Bitmap src = BitmapFactory.decodeFile(path, options);
        src = src.createScaledBitmap(src, reqWidth, reqHeight, false);

        ByteArrayBody bab = null;
        if (src != null) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            src.compress(Bitmap.CompressFormat.PNG, 100, bos);
            byte[] data = bos.toByteArray();
            File oFile = new File(path);
            bab = new ByteArrayBody(data, oFile.getName());
            src.recycle();
            src = null;
        }

        return bab;
    }
}
