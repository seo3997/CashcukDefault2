package com.cashcuk.findaddr;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.fragment.app.FragmentActivity;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cashcuk.CheckLoginService;
import com.cashcuk.R;
import com.cashcuk.StaticDataInfo;
import com.cashcuk.TitleBar;
import com.cashcuk.common.AddrUser;
import com.cashcuk.dialog.DlgBtnActivity;
import com.cashcuk.membership.txtlistdata.TxtListAdapter;
import com.cashcuk.membership.txtlistdata.TxtListDataInfo;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * 주소 검색
 */
public class FindAddressActivity extends FragmentActivity implements View.OnClickListener {
    private EditText etDetailAddr; //상세 주소
    private RelativeLayout rlSiDo; //시/도 layout
    private RelativeLayout rlSiGun; //시/군/구 layout
    private TextView txtSiDo;
    private TextView txtSiGun;
    private ListView lvHangOutList; // 시도, 시군구 list

    private final int DIALOG_MODE_SI_DO = 1;
    private final int DIALOG_MODE_SI_GUN = 2;
    private boolean isSelSiGun = false;

    private final String STR_SI_MODE = "S"; //시,도 선택
    private final String STR_GUN_MODE = "G"; //시,군,구 선택
    private String strSiDoMode=STR_SI_MODE;
    private String selSiDoIdx="";
    private String selSiGunGuIdx="";

    //시도, 시군구
    private ArrayList<TxtListDataInfo> arrHangOutCitiesData = new ArrayList<TxtListDataInfo>();
    private ArrayList<TxtListDataInfo> arrHangOutTownData = new ArrayList<TxtListDataInfo>();

    private Button btnAddrSearch;

    /**
     * 결과 값 받는 handler
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case StaticDataInfo.RESULT_CODE_ERR:
                    Toast.makeText(FindAddressActivity.this, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
                    break;
                case StaticDataInfo.RESULT_SI_DO:
                    if(arrHangOutCitiesData==null){
                        arrHangOutCitiesData = new ArrayList<TxtListDataInfo>();
                    }
                    arrHangOutCitiesData.addAll((ArrayList<TxtListDataInfo>)msg.obj);
                    break;
                case StaticDataInfo.RESULT_SI_GUN_GU:
                    if(arrHangOutTownData==null){
                        arrHangOutTownData = new ArrayList<TxtListDataInfo>();
                    }
                    arrHangOutTownData.clear();
                    arrHangOutTownData.addAll((ArrayList<TxtListDataInfo>) msg.obj);
                    break;
                case StaticDataInfo.RESULT_NO_DATA:
                case StaticDataInfo.RESULT_NO_SIGUN:
                    isSelSiGun = true;
                    if(arrHangOutTownData != null) arrHangOutTownData.clear();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_address);
        ((TitleBar) findViewById(R.id.title_bar)).setTitle(getResources().getString(R.string.str_find_address_num));
        CheckLoginService.mActivityList.add(this);

        LinearLayout layoutBG = (LinearLayout) findViewById(R.id.ll_bg);
        layoutBG.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.display_bg)));

        rlSiDo = (RelativeLayout) findViewById(R.id.rl_category2);
        rlSiGun = (RelativeLayout) findViewById(R.id.rl_category3);
        txtSiDo = (TextView) findViewById(R.id.txt_category2);
        txtSiGun = (TextView) findViewById(R.id.txt_category3);

        rlSiDo.setOnClickListener(this);
        rlSiGun.setOnClickListener(this);

        etDetailAddr = (EditText) findViewById(R.id.et_detail_addr);
        btnAddrSearch = (Button) findViewById(R.id.btn_addr_search);
        btnAddrSearch.setOnClickListener(this);

        ((Button) findViewById(R.id.btn_find_addr_cancel)).setOnClickListener(this);
        ((Button) findViewById(R.id.btn_find_addr_ok)).setOnClickListener(this);

        new AddrUser(FindAddressActivity.this, handler, STR_SI_MODE, selSiDoIdx);

        displayMap(String.valueOf(37.466179) + "/" + String.valueOf(126.886911), false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recycleView(findViewById(R.id.ll_bg));
    }

    private void recycleView(View view) {
        if(view != null) {
            Drawable bg = view.getBackground();
            if(bg != null) {
                bg.setCallback(null);
                ((BitmapDrawable)bg).getBitmap().recycle();
                view.setBackgroundDrawable(null);
            }
        }
    }


    @Override
    public void onClick(View v) {
        Intent intent = null;
        int viewId = v.getId();
        if (viewId == R.id.btn_addr_search) {
            if (txtSiDo.getText().toString().trim().equals("")) {
                intent = new Intent(FindAddressActivity.this, DlgBtnActivity.class);
                intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_selector_cities));
            } else if (txtSiGun.getText().toString().trim().equals("") && !isSelSiGun) {
                intent = new Intent(FindAddressActivity.this, DlgBtnActivity.class);
                intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_selector_town));
            } else if (etDetailAddr.getText().toString().trim().equals("")) {
                intent = new Intent(FindAddressActivity.this, DlgBtnActivity.class);
                intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_detail_addr_empty));
            } else {
                InputMethodManager imm= (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etDetailAddr.getWindowToken(), 0);

                String addr = txtSiDo.getText().toString() + " " + txtSiGun.getText().toString() + " " + etDetailAddr.getText().toString();
                new GetLatLng().execute(addr);
            }
        } else if (viewId == R.id.rl_category2) {
            OpenDialog(DIALOG_MODE_SI_DO);
            return;
        } else if (viewId == R.id.rl_category3) {
            OpenDialog(DIALOG_MODE_SI_GUN);
            return;
        } else if (viewId == R.id.btn_find_addr_ok) {
            if (txtSiDo.getText().toString().trim().equals("")) {
                intent = new Intent(FindAddressActivity.this, DlgBtnActivity.class);
                intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_selector_cities));
            } else if (txtSiGun.getText().toString().trim().equals("") && !isSelSiGun) {
                intent = new Intent(FindAddressActivity.this, DlgBtnActivity.class);
                intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_selector_town));
            } else if (etDetailAddr.getText().toString().trim().equals("")) {
                intent = new Intent(FindAddressActivity.this, DlgBtnActivity.class);
                intent.putExtra("BtnDlgMsg", getResources().getString(R.string.str_detail_addr_empty));
            } else {
                Intent intent1 = new Intent();
                String strAddr="";

                if(txtSiGun.getText().toString().trim().equals("")){
                    strAddr = txtSiDo.getText() + " "+etDetailAddr.getText();
                }else{
                    strAddr = txtSiDo.getText() + " " + txtSiGun.getText() + " " + etDetailAddr.getText();
                }

                if(!strAddr.equals("")) {
                    intent1.putExtra("Addr", strAddr);
                    setResult(RESULT_OK, intent1);
                    finish();
                }
            }
        } else if (viewId == R.id.btn_find_addr_cancel) {
            finish();
            return;
        }

        if(intent!=null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    /** @Author : pppdw
     * @Description : 구글 URL을 이용해 간단하게 lng.lat를 뽑는다. new HttpGet 생성자에 사용된 URL로 뽑고자하는 지역의 네임값만 날리면된다.
     *                  단 네임값에 공백이 있으면 안되며, 공백이 존재 할 시 공백을 +로 변경하여 리퀘스트 요청을 해야한다.
     * @Param : strPlaceName --> 지오코딩 하고자 하는 지역의 이름 (예시 : "서울특별시+강남구+개포동+3421번지")
     **/

    private class GetLatLng extends AsyncTask<String, Void, String> {
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
//                LatLng position = new LatLng(lat, lng);
//                GoogleMap mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
//                //맵 위치이동
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
//
//                //마커설정
//                MarkerOptions opt = new MarkerOptions();
//                opt.position(position);
//                mMap.addMarker(opt).showInfoWindow();

            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return strLocation;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(result.equals("")){
                Toast.makeText(FindAddressActivity.this, getResources().getString(R.string.str_find_address_err), Toast.LENGTH_SHORT).show();
            }else {
                displayMap(result, true);
            }
        }
    }

    /**
     * map에 위치 표시
     * @param location
     */
    public void displayMap(String location, boolean isMarker) {
        //2020.01.01 위치기간 나중에 반영 sooHyun.Seo
        /*
        String[] strLocation = location.split("/");
        LatLng position = new LatLng(Double.parseDouble(strLocation[0]), Double.parseDouble(strLocation[1]));
        GoogleMap mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        //맵 위치이동
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 13));

        if(isMarker) {
            //마커설정
            MarkerOptions opt = new MarkerOptions();
            opt.position(position);
            mMap.addMarker(opt).showInfoWindow();
        }
         */
    }

    private Dialog mDialog;
    public void OpenDialog(final int mDlgMode){
        mDialog = new Dialog(FindAddressActivity.this);

        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Dialog 사이즈 조절 하기
        WindowManager.LayoutParams params = mDialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;
        mDialog.getWindow().setAttributes((WindowManager.LayoutParams) params);
        mDialog.setContentView(R.layout.dlg_txt_list);

        lvHangOutList = (ListView) mDialog.findViewById(R.id.lv_txt);
        TextView txtTitle = (TextView) mDialog.findViewById(R.id.txt_dlg_title);

        switch(mDlgMode){
            case DIALOG_MODE_SI_DO:
                isSelSiGun = false;
                if(arrHangOutCitiesData!=null){
                    txtTitle.setText(getResources().getString(R.string.str_cities_hint));
                    lvHangOutList.setAdapter(new TxtListAdapter(this, arrHangOutCitiesData));
                }
                break;
            case DIALOG_MODE_SI_GUN:
                if(arrHangOutTownData!=null && arrHangOutTownData.size()>0){
                    txtTitle.setText(getResources().getString(R.string.str_town_hint));
                    lvHangOutList.setAdapter(new TxtListAdapter(this, arrHangOutTownData));
                }else{
                    Toast.makeText(FindAddressActivity.this, getResources().getString(R.string.str_no_sigun), Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
        }

        lvHangOutList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (mDlgMode) {
                    case DIALOG_MODE_SI_DO:
                        txtSiDo.setText(arrHangOutCitiesData.get(position).getStrMsg());

                        txtSiGun.setText("");
                        selSiDoIdx = arrHangOutCitiesData.get(position).getStrIdx();
                        strSiDoMode = STR_GUN_MODE;

                        new AddrUser(FindAddressActivity.this, handler, strSiDoMode, selSiDoIdx);
                        break;
                    case DIALOG_MODE_SI_GUN:
                        txtSiGun.setText(arrHangOutTownData.get(position).getStrMsg());
                        selSiGunGuIdx = arrHangOutTownData.get(position).getStrIdx();
                        break;
                }
                if (mDialog != null && mDialog.isShowing()) mDialog.dismiss();
            }
        });

        mDialog.show();
    }
}
