package com.cashcuk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.cashcuk.ad.adlist.FrListAD;
import com.cashcuk.ad.admylist.FrMyListAD;
import com.cashcuk.common.CashcukArea;
import com.cashcuk.main.FrMain;
import com.cashcuk.setting.FrSetting;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapReverseGeoCoder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * 각 화면 관리하는 FragmentActivity
 */
public class MainActivity extends FragmentActivity implements View.OnClickListener {
    private BackPressClose backPressCloseHandler;

    private LinearLayout llThisCurrent2; //광고보기
    private LinearLayout llThisCurrent3; //관심광고
    private LinearLayout llThisCurrent4; // 설정
    public  LinearLayout ll_use_stop;    //중지 사용자는 아뭇거도 못함


    private LinearLayout llTab2; //광고보기
    private LinearLayout llTab3; //관심광고
    private LinearLayout llTab4; // 설정

    private final int THIS_PAGE_1 = 0; //광고보기
    private final int THIS_PAGE_2 = 1; //관심광고
    private final int THIS_PAGE_3 = 2; //설정

    private ViewPager mViewPager;
    private Fragment mFrMain;
    private LinearLayout llFrMain;


    private SectionsPagerAdapter mSectionsPagerAdapter;

    private String strSi = "";
    private boolean isFirst = true;
    private LinearLayout llDiv1;
    private LinearLayout llDiv2;

    private String TAG="MainActivity";
    private GoogleApiClient mClient;

    double mLatitude;
    double mLongitude;
    static public String mGpsAddress1="";
    static public String mGpsAddress2="";
    static public String mMyAddress="";
    static public String mMyAddress1="";
    static public String mMyAddress2="";
    static final LocationRequest mRequest=LocationRequest.create();
    private LocationManager mLocationManager;
    static public boolean mGPSOn = false;

    //static public String mAddr="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        CheckLoginService.mActivityList.add(this);
        backPressCloseHandler = new BackPressClose(this);

        llFrMain = (LinearLayout) findViewById(R.id.ll_frMain);
        mFrMain = new FrMain();
        mViewPager = (ViewPager) findViewById(R.id.pager);

        MainTitleBar mainTitleBar = (MainTitleBar) findViewById(R.id.main_title_bar);
        final ImageButton ibRefresh = (ImageButton) mainTitleBar.findViewById(R.id.ib_refresh);
        ibRefresh.setVisibility(View.VISIBLE);
        ibRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (llFrMain.isShown()) {
                    if (mFrMain != null) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.frMain, new FrMain()).commit();
                    }
                } else if (mViewPager.isShown()) {
                    mSectionsPagerAdapter.notifyDataSetChanged();
                }
            }
        });


        llThisCurrent2 = (LinearLayout) findViewById(R.id.ll_this_current2);
        llThisCurrent3 = (LinearLayout) findViewById(R.id.ll_this_current3);
        llThisCurrent4 = (LinearLayout) findViewById(R.id.ll_this_current4);

        llTab2 = (LinearLayout) findViewById(R.id.ll_tab2);
        llTab3 = (LinearLayout) findViewById(R.id.ll_tab3);
        llTab4 = (LinearLayout) findViewById(R.id.ll_tab4);
        llTab2.setOnClickListener(this);
        llTab3.setOnClickListener(this);
        llTab4.setOnClickListener(this);

        llDiv1 = (LinearLayout) findViewById(R.id.ll_div1);
        llDiv2 = (LinearLayout) findViewById(R.id.ll_div2);

        ll_use_stop=(LinearLayout) findViewById(R.id.ll_use_stop);

        //메인 화면에서 광고 보기 클릭시 2017-08-04
        Intent intent = getIntent() ;
        if(intent.getStringExtra("main")!=null) {
            llFrMain.setVisibility(View.GONE);
            mViewPager.setVisibility(View.VISIBLE);

            if (!llThisCurrent2.isShown()) llThisCurrent2.setVisibility(View.VISIBLE);
            llThisCurrent3.setVisibility(View.INVISIBLE);
            llThisCurrent4.setVisibility(View.INVISIBLE);
            llTab2.setBackgroundColor(getResources().getColor(R.color.color_main_friend_txt));
            llTab3.setBackgroundColor(getResources().getColor(R.color.color_white));
            llTab4.setBackgroundColor(getResources().getColor(R.color.color_white));

            llDiv1.setVisibility(View.GONE);
            llDiv2.setVisibility(View.VISIBLE);

        }

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mGPSOn=isLocationEnabled();


        mRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mRequest.setNumUpdates(1);
        mRequest.setInterval(0);

        if(mGPSOn) {
            /*
            mClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {

                            LocationServices.FusedLocationApi.requestLocationUpdates(mClient, mRequest, new LocationListener() {
                                @Override
                                public void onLocationChanged(Location location) {
                                    mLatitude = location.getLatitude();
                                    mLongitude = location.getLongitude();
                                    getAddress(mLatitude, mLongitude);
                                }
                            });

                        }

                        @Override
                        public void onConnectionSuspended(int i) {

                        }
                    })
                    .build();

             */

        }

        //GPS호출 후에 설정으로 변경   //2017-08-31 soohyun.Seo
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (llFrMain.isShown()) llFrMain.setVisibility(View.GONE);
                if (!mViewPager.isShown()) mViewPager.setVisibility(View.VISIBLE);
                ThisPage(position);
                mSectionsPagerAdapter.notifyDataSetChanged();

                if (position == THIS_PAGE_3) {
                    ibRefresh.setVisibility(View.GONE);
                } else {
                    ibRefresh.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });


        //String keyHash=getKeyHash(this);
        //Log.d("temp","keyhash["+keyHash+"]");
    }

    private boolean isLocationEnabled() {
        return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private void getAddress(double pLatitude, double pLongitude){
        String address="";


        MapPoint mapPoint=MapPoint.mapPointWithGeoCoord(pLatitude,pLongitude);
        String LOCAL_API_KEY=getResources().getString(R.string.str_kakao_key);
        MapReverseGeoCoder reverseGeoCoder = new MapReverseGeoCoder(LOCAL_API_KEY, mapPoint,
                new MapReverseGeoCoder.ReverseGeoCodingResultListener(){
                    // reverseGeoCodingResultListener
                    @Override
                    public void onReverseGeoCoderFoundAddress(MapReverseGeoCoder mapReverseGeoCoder, String addressString) {
                        // 주소를 찾은 경우.
                        Log.d("temp","**************************onReverseGeoCoderFoundAddress***************************");
                        Log.d("temp","**********************addressString["+addressString+"]*************************");
                        CashcukArea cashcukArea =new CashcukArea(addressString);

                        mGpsAddress1=cashcukArea.getSiDo();
                        mGpsAddress2=cashcukArea.getGuDong();
                        Log.d("temp","**********************area1["+cashcukArea.getSiDo()+" "+cashcukArea.getGuDong()+"]*************************");


                        //Toast.makeText(getApplicationContext(), "mGPSOn:"+mGPSOn+" Area:"+mGpsAddress1+" "+mGpsAddress2, Toast.LENGTH_LONG).show();
                        Log.d("temp","mGpsAddress["+mGpsAddress1+" "+mGpsAddress2+"]");
                        Log.d("temp","**************************onReverseGeoCoderFoundAddress***************************");
                    }

                    @Override
                    public void onReverseGeoCoderFailedToFindAddress(MapReverseGeoCoder mapReverseGeoCoder) {
                        // 호출에 실패한 경우.
                        Log.d("temp","**********onReverseGeoCoderFailedToFindAddress*******************");
                        Log.d("temp","*********mapReverseGeoCoder["+mapReverseGeoCoder+"]****************");
                    }
                }
                ,this);
        reverseGeoCoder.startFindingAddress();
    }

    @Override
    protected void onResume() {
        super.onResume();

        /*
        if(mClient!=null && mClient.isConnected()){
            LocationServices.FusedLocationApi.requestLocationUpdates(mClient, mRequest, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    mLatitude=location.getLatitude();
                    mLongitude=location.getLongitude();
                    getAddress(mLatitude,mLongitude);
                }
            });
        }
         */


        //if(this.checkPlayServices(this)){
        //
        //}


        //Log.d("temp","*****************checkPlayServices ["+this.checkPlayServices(this)+"]*********************");

        // int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable()
        //위도 경도를 요청
        /*
        MapPoint mapPoint=MapPoint.mapPointWithGeoCoord(37.496684,127.070695);
        String LOCAL_API_KEY=getResources().getString(R.string.str_kakao_key);
        MapReverseGeoCoder reverseGeoCoder = new MapReverseGeoCoder(LOCAL_API_KEY, mapPoint,
                new MapReverseGeoCoder.ReverseGeoCodingResultListener(){
                    // reverseGeoCodingResultListener
                    @Override
                    public void onReverseGeoCoderFoundAddress(MapReverseGeoCoder mapReverseGeoCoder, String addressString) {
                        // 주소를 찾은 경우.
                        Log.d("temp","**************************onReverseGeoCoderFoundAddress***************************");
                        Log.d("temp","addressString["+addressString+"]");
                        Log.d("temp","**************************onReverseGeoCoderFoundAddress***************************");
                    }

                    @Override
                    public void onReverseGeoCoderFailedToFindAddress(MapReverseGeoCoder mapReverseGeoCoder) {
                        // 호출에 실패한 경우.
                        Log.d("temp","**********onReverseGeoCoderFailedToFindAddress*******************");
                        Log.d("temp","**********onReverseGeoCoderFailedToFindAddress*******************");
                        Log.d("temp","**********onReverseGeoCoderFailedToFindAddress*******************");
                        Log.d("temp","**********onReverseGeoCoderFailedToFindAddress*******************");
                    }
                }
                ,this);
        reverseGeoCoder.startFindingAddress();
        */

    }


    @Override
    protected void onStart() {
        super.onStart();
        if(mGPSOn && mClient!=null)  mClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mGPSOn && mClient!=null) mClient.disconnect();
    }

    public static boolean checkPlayServices(Activity activity) {
        final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(activity, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        int thisPosition = 0;
        int viewId = v.getId();
        if (viewId == R.id.ll_tab2) {
            thisPosition = THIS_PAGE_1;
        } else if (viewId == R.id.ll_tab3) {
            thisPosition = THIS_PAGE_2;
        } else if (viewId == R.id.ll_tab4) {
            thisPosition = THIS_PAGE_3;
        }

        if (mViewPager != null) mViewPager.setCurrentItem(thisPosition);
        if (isFirst || thisPosition == THIS_PAGE_1) {
            if (llFrMain.isShown()) llFrMain.setVisibility(View.GONE);
            if (!mViewPager.isShown()) mViewPager.setVisibility(View.VISIBLE);

            ThisPage(thisPosition);
        }
    }

    private int frViewPage = 0;
    public void ThisPage(int page) {
        frViewPage = page;
        switch (page) {
            case THIS_PAGE_1:
                if (!llThisCurrent2.isShown()) llThisCurrent2.setVisibility(View.VISIBLE);
                llThisCurrent3.setVisibility(View.INVISIBLE);
                llThisCurrent4.setVisibility(View.INVISIBLE);
                llTab2.setBackgroundColor(getResources().getColor(R.color.color_main_friend_txt));
                llTab3.setBackgroundColor(getResources().getColor(R.color.color_white));
                llTab4.setBackgroundColor(getResources().getColor(R.color.color_white));

                llDiv1.setVisibility(View.GONE);
                llDiv2.setVisibility(View.VISIBLE);
                break;
            case THIS_PAGE_2:
                if (!llThisCurrent3.isShown()) llThisCurrent3.setVisibility(View.VISIBLE);
                llThisCurrent2.setVisibility(View.INVISIBLE);
                llThisCurrent4.setVisibility(View.INVISIBLE);
                llTab2.setBackgroundColor(getResources().getColor(R.color.color_white));
                llTab3.setBackgroundColor(getResources().getColor(R.color.color_main_friend_txt));
                llTab4.setBackgroundColor(getResources().getColor(R.color.color_white));

                llDiv1.setVisibility(View.GONE);
                llDiv2.setVisibility(View.GONE);
                break;
            case THIS_PAGE_3:
                if (!llThisCurrent4.isShown()) llThisCurrent4.setVisibility(View.VISIBLE);
                llThisCurrent2.setVisibility(View.INVISIBLE);
                llThisCurrent3.setVisibility(View.INVISIBLE);
                llTab2.setBackgroundColor(getResources().getColor(R.color.color_white));
                llTab3.setBackgroundColor(getResources().getColor(R.color.color_white));
                llTab4.setBackgroundColor(getResources().getColor(R.color.color_main_friend_txt));

                llDiv1.setVisibility(View.VISIBLE);
                llDiv2.setVisibility(View.GONE);
                break;
        }
    }

    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {
        private int TOTAL_PAGE = 3;
        private Fragment fr;
        private int mThisPage = THIS_PAGE_1;

        SharedPreferences prefs = getSharedPreferences("MyArea", MODE_PRIVATE);
        final String MyArea = prefs.getString("MyArea", "");



        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            mThisPage = position;
            switch (position) {
                case THIS_PAGE_1:
                    fragment = Fragment.instantiate(MainActivity.this, FrListAD.class.getName());
                    break;
                case THIS_PAGE_2:
                    fragment = Fragment.instantiate(MainActivity.this, FrMyListAD.class.getName());
                    break;
                case THIS_PAGE_3:
                    fragment = Fragment.instantiate(MainActivity.this, FrSetting.class.getName());
                    break;
            }
            Bundle bundle = new Bundle();
            bundle.putInt("Page", frViewPage);


            //2초지연
            /*
            if(mGPSOn ==true && mGpsAddress1.equals("")){
                Log.d("temp", "**************Thread1*********************");
                try {
                    Log.d("temp", "**************Thread2*********************");
                    Thread.sleep(1000);
                    Log.d("temp", "**************Thread3*********************");
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            */

            Log.d("temp","**************Thread4*********************");
            Log.d("temp","**************SectionsPagerAdapter mGPSOn["+mGPSOn+"]*********************");
            Log.d("temp","**************SectionsPagerAdapter mGpsAddress1["+mGpsAddress1+"]mGpsAddress1["+mGpsAddress2+"]*********************");
            //Toast.makeText(getApplicationContext(), "mGPSOn:"+mGPSOn+" Area:"+mGpsAddress, Toast.LENGTH_LONG).show();


            if(!mGpsAddress1.equals("")) {
                bundle.putString("Addr1", mGpsAddress1);
                bundle.putString("Addr2", mGpsAddress2);
            }else{
                String[] sAddrArr=null;
                if(!mMyAddress.equals("")){
                    sAddrArr = mMyAddress.split("\\s");
                }
                if(sAddrArr!=null && sAddrArr.length>1){
                    mMyAddress1=sAddrArr[0];
                    mMyAddress2=sAddrArr[1];
                }

                bundle.putString("Addr1", mMyAddress1);
                bundle.putString("Addr2", mMyAddress2);
            }

            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public int getCount() {
            return TOTAL_PAGE;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    /**
     * back key 두번 press 시 앱 종료
     */
    @Override
    public void onBackPressed() {
        if(llFrMain.isShown()) {
            backPressCloseHandler.onBackPressed();
        }else{
            CheckLoginService.Close_All();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    public static String getKeyHash(final Context context) {
        /*
        PackageInfo packageInfo = getPackageInfo(context, PackageManager.GET_SIGNATURES);
        if (packageInfo == null)
            return null;

        for (Signature signature : packageInfo.signatures) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                return Base64.encodeToString(md.digest(), Base64.NO_WRAP);
            } catch (NoSuchAlgorithmException e) {
                Log.w("temp", "Unable to get MessageDigest. signature=" + signature, e);
            }
        }
         */
        return null;
    }



}
