package com.cashcuk.advertiser.makead;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.telephony.PhoneNumberUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.ad.detailview.ADDetailInfo;
import com.cashcuk.common.ImageLoader;
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
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 광고제작 - 프리뷰 (광고주 정보)
 * 나의 광고 상세보기 (광고주 정보)
 */
public class FrMakeADPreviewAdvertiserInfo3 extends Fragment implements OnMapReadyCallback {
    private LinearLayout llAdvertiserErr;
    private ScrollView svAdvertiserInfo;
    private FragmentActivity mActivity;

    private final String STR_TRADE_NM = "biz_nm"; //상호명
    private final String STR_REPRESENTATIVE_NM = "biz_rep"; //대표자
    private final String STR_REPRESENTATIVE_TEL = "biz_tel"; //대표전화
    private final String STR_HOME_PAGE= "biz_url"; //홈페이지
    private final String STR_ADDRESS = "biz_geo"; //주소
    private final String STR_IMG_URL = "biz_img"; //광고주 이미지 url
    private final String STR_REGI_MODE = "biz_ae"; //쓰기 모드 (A: 등록요청, E: 수정)

    private final int DEFAULT_NUM = -1;
    private final int PARSER_NUM_0 = 0;
    private final int PARSER_NUM_1 = 1;
    private final int PARSER_NUM_2 = 2;
    private final int PARSER_NUM_3 = 3;
    private final int PARSER_NUM_4 = 4;
    private final int PARSER_NUM_5 = 5;
    private final int PARSER_NUM_6 = 6;

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    /**
     * 결과 값 받는 handler
     */
    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case StaticDataInfo.RESULT_CODE_ERR:
                    llAdvertiserErr.setVisibility(View.VISIBLE);
                    svAdvertiserInfo.setVisibility(View.GONE);
                    break;
                case StaticDataInfo.RESULT_CODE_200:
                    displayView();
                    break;
            }
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


    private ImageView ivAdvertiserImg;
    private ProgressBar pbAdvertiser;
    private LinearLayout llAdvertiserImgErr;//광고주 사진
    private TextView txtAdvertiserImgErr; //통신오류 txt d/p
    private TextView txtTradeNm; //상호명
    private TextView txtRepresentativeNm; //대표자
    private TextView txtRepresentativeTel; //대표전화
    private TextView txtAddress; //주소
    private ImageView ivMapErr; //지도 표시 오류
    private LinearLayout llMap; //지도

    private ADDetailInfo mADDetailInfo;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments()!=null){
            mADDetailInfo = (ADDetailInfo)getArguments().getSerializable("AD_DATA");
        }else{
            dataRequest();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate( R.layout.ad_detail_advertiser_info_2, container, false );

        llAdvertiserErr = (LinearLayout) view.findViewById(R.id.ll_empty);
        svAdvertiserInfo = (ScrollView) view.findViewById(R.id.sv_info);
        ivAdvertiserImg = (ImageView) view.findViewById(R.id.iv_img);
        pbAdvertiser = (ProgressBar) view.findViewById(R.id.pb_advetiser);
        llAdvertiserImgErr = (LinearLayout) view.findViewById(R.id.ll_img);
        txtAdvertiserImgErr = (TextView) view.findViewById(R.id.txt_advertiser_err);
        txtTradeNm = (TextView) view.findViewById(R.id.txt_trade_nm);
        txtRepresentativeNm = (TextView) view.findViewById(R.id.txt_representative_nm);
        txtRepresentativeTel = (TextView) view.findViewById(R.id.txt_representative_tel);
        txtAddress = (TextView) view.findViewById(R.id.txt_address);
        ivMapErr = (ImageView) view.findViewById(R.id.iv_map_err);
        llMap = (LinearLayout) view.findViewById(R.id.ll_map);

        if(mADDetailInfo!=null){
            displayView();
        }else{
            llAdvertiserErr.setVisibility(View.VISIBLE);
            svAdvertiserInfo.setVisibility(View.GONE);
        }

        return view;
    }

    public void displayView(){
        llAdvertiserErr.setVisibility(View.GONE);
        svAdvertiserInfo.setVisibility(View.VISIBLE);

        if(mADDetailInfo!=null) {
            if(mADDetailInfo.getStrAdvertiserImgUrl()==null || mADDetailInfo.getStrAdvertiserImgUrl().equals("")){
                llAdvertiserImgErr.setVisibility(View.VISIBLE);
                txtAdvertiserImgErr.setVisibility(View.VISIBLE);
                ivAdvertiserImg.setVisibility(View.GONE);
                if(pbAdvertiser!=null && pbAdvertiser.isShown()) pbAdvertiser.setVisibility(View.GONE);
            }else{
                llAdvertiserImgErr.setVisibility(View.GONE);
                txtAdvertiserImgErr.setVisibility(View.GONE);
                ivAdvertiserImg.setVisibility(View.VISIBLE);
                String strImgPathTmp = mADDetailInfo.getStrAdvertiserImgUrl().toString().replace("\\", "//");
                if(pbAdvertiser!=null && !pbAdvertiser.isShown()) pbAdvertiser.setVisibility(View.VISIBLE);

                ImageLoader.loadImage(mActivity, strImgPathTmp, ivAdvertiserImg, pbAdvertiser);

            }

            txtTradeNm.setText(mADDetailInfo.getStrTradeName());
            txtRepresentativeNm.setText(mADDetailInfo.getStrRepresentativeName());
            txtRepresentativeTel.setText(PhoneNumberUtils.formatNumber(mADDetailInfo.getStrStrRepresentativeTel()));
            txtAddress.setText(mADDetailInfo.getStrAdvertiserAddres());
            txtAddress.setSelected(true);

            //new getLatLng().execute(mADDetailInfo.getStrAdvertiserAddres());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(pbAdvertiser!=null && pbAdvertiser.isShown()) pbAdvertiser.setVisibility(View.GONE);
    }

    public void requestData(String[] params) {
        executor.execute(() -> {
            String result = doInBackground(params);
            mainHandler.post(() -> onPostExecute(result));
        });
    }

    /**
     * 서버로 전송하는 값
     */
    public void dataRequest(){
        final String url=getResources().getString(R.string.str_new_url)+getResources().getString(R.string.str_member_bizdata);
        SharedPreferences pref = mActivity.getSharedPreferences("TokenInfo", mActivity.MODE_PRIVATE);
        final String token = pref.getString(mActivity.getResources().getString(R.string.str_token), "");

//        if(llProgress!=null && !llProgress.isShown()) llProgress.setVisibility(View.VISIBLE);
        HashMap<Integer, String> k_param = new HashMap<Integer, String>();
        k_param.put(StaticDataInfo.SEND_URL, url);
        k_param.put(StaticDataInfo.SEND_TOKEN, token);

        String[] strTask = new String[k_param.size()];
        for (int i = 0; i < strTask.length; i++) {
            strTask[i] = k_param.get(i);
        }

        requestData(strTask);
    }

    /**
     * 서버에 값 요청
     */

    private String doInBackground(String... params) {
        String retMsg = "";
        String url = params[StaticDataInfo.SEND_URL];
        String token = params[StaticDataInfo.SEND_TOKEN];

        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            // Set connection timeout
            con.setConnectTimeout(StaticDataInfo.TIME_OUT);
            con.setReadTimeout(StaticDataInfo.TIME_OUT);

            // Set request method
            con.setRequestMethod("POST");

            // Set request headers
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // Set request body
            String postData = getResources().getString(R.string.str_token) + "=" + token;
            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();
            os.write(postData.getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.close();

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream is = con.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                is.close();
                retMsg = sb.toString();
            } else {
                retMsg = "HTTP Error: " + responseCode;
            }
            con.disconnect();
        } catch (IOException e) {
            retMsg = e.toString();
        }
        return retMsg;
    }

    private void onPostExecute(String result) {
        if (result.startsWith(StaticDataInfo.TAG_LIST)) {
            resultAdvertiserInfo(result);
        } else {
            handler.sendEmptyMessage(StaticDataInfo.RESULT_CODE_ERR);
        }
    }


    public void resultAdvertiserInfo(String result){
        try {

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(result));

            int eventType = parser.getEventType();
            int k_data_num = 0;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.END_TAG:
                        break;
                    case XmlPullParser.END_DOCUMENT:
                        break;
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if (parser.getName().equals(StaticDataInfo.TAG_ITEM)) {
                            mADDetailInfo = new ADDetailInfo();
                        }

                        if (parser.getName().equals(STR_TRADE_NM)) {
                            k_data_num = PARSER_NUM_0;
                        } else if (parser.getName().equals(STR_REPRESENTATIVE_NM)) {
                            k_data_num = PARSER_NUM_1;
                        } else if (parser.getName().equals(STR_REPRESENTATIVE_TEL)) {
                            k_data_num = PARSER_NUM_2;
                        } else if (parser.getName().equals(STR_HOME_PAGE)) {
                            k_data_num = PARSER_NUM_4;
                        } else if (parser.getName().equals(STR_ADDRESS )) {
                            k_data_num = PARSER_NUM_5;
                        } else if (parser.getName().equals(STR_IMG_URL)) {
                            k_data_num = PARSER_NUM_6;
                        } else {
                            k_data_num = DEFAULT_NUM;
                        }
                        break;

                    case XmlPullParser.TEXT:
                        if (k_data_num > DEFAULT_NUM) {
                            switch (k_data_num) {
                                case PARSER_NUM_0:
                                    mADDetailInfo.setStrTradeName(parser.getText());
                                    break;
                                case PARSER_NUM_1:
                                    mADDetailInfo.setStrRepresentativeName(parser.getText());
                                    break;
                                case PARSER_NUM_2:
                                    mADDetailInfo.setStrStrRepresentativeTel(parser.getText());
                                    break;
                                case PARSER_NUM_4:
                                    mADDetailInfo.setStrHomepageUrl(parser.getText());
                                    break;
                                case PARSER_NUM_5:
                                    mADDetailInfo.setStrAdvertiserAddres(parser.getText());
                                    break;
                                case PARSER_NUM_6:
                                    mADDetailInfo.setStrAdvertiserImgUrl(parser.getText());
                                    break;
                            }
                            k_data_num = DEFAULT_NUM;
                        }
                        break;
                }
                eventType = parser.next();
            }

            Message msg = new Message();
            msg.what = StaticDataInfo.RESULT_CODE_200;
            handler.sendMessage(msg);
        } catch (Exception e) {
            e.printStackTrace();
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
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map_info);
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
}
