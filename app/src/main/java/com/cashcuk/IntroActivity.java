package com.cashcuk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.cashcuk.common.DefaultData;
import com.cashcuk.dialog.DlgBtnActivity;
import com.cashcuk.loginout.LoginActivity;
import com.cashcuk.loginout.LoginInfo;
import com.cashcuk.market.MarketVersionChecker;
import com.cashcuk.network.NetworkCheck;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class IntroActivity extends Activity {
    public static final String ACTION_GCM_REGISTRATION = "com.cashcuk.intent.GCM_REGISTRATION";
    private final int REQUEST_ERR = 999;

    private String mThisAppVersion;
    private String regstrationId;


    private Handler mHandler;
    private Runnable mRunnable;

    private Handler mRegHandler = null;
    private Runnable mRegRunnable = null;

    private AlertDialog.Builder ad;
    private boolean isVersionMatch;

    /** 로딩뷰 */
    private ImageView mIntroLoadingView;

    /** 로딩애니메이션 */
    private AnimationDrawable mLoadingAnimation;

    /**
     * 로그인 결과 값 받는 handler
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case StaticDataInfo.RESULT_NO_USER:
//                    Toast.makeText(IntroActivity.this, getResources().getString(R.string.str_find_email_err), Toast.LENGTH_SHORT).show();
                    break;
                case StaticDataInfo.RESULT_CODE_ERR:
                    Toast.makeText(IntroActivity.this, getResources().getString(R.string.str_http_error), Toast.LENGTH_SHORT).show();
                    break;
                case StaticDataInfo.RESULT_PWD_ERR:
//                    Toast.makeText(IntroActivity.this, getResources().getString(R.string.str_pwd_err), Toast.LENGTH_SHORT).show();
                    break;
                case StaticDataInfo.RESULT_CODE_200:
                    nextPage(true);
                    break;
            }

            if(msg.what != StaticDataInfo.RESULT_CODE_200){
                Intent intent = new Intent(IntroActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_intro);
        CheckLoginService.mActivityList.add(this);

        Bundle aBungle = getIntent().getExtras();
        if(aBungle!=null){
            Log.d("temp","**********IntroActvity***********");
            Log.d("temp","**********IntroActvity["+aBungle.getString("AD")+"]***************");
        }

        mHandler = new Handler();
        ad = new AlertDialog.Builder(this);
        isVersionMatch = true;

        mIntroLoadingView = (ImageView)findViewById(R.id.iv_intro_loading);
        mLoadingAnimation = (AnimationDrawable)mIntroLoadingView.getBackground();

        mIntroLoadingView.setVisibility(View.VISIBLE);
        mLoadingAnimation.start();


        new DefaultData(IntroActivity.this);




        if (NetworkCheck.getConnectivityStatus(IntroActivity.this) == NetworkCheck.TYPE_NOT_CONNECTED) {
//            Toast.makeText(IntroActivity.this, R.string.str_network_error, Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, DlgBtnActivity.class);
            i.putExtra("BtnDlgMsg", getResources().getString(R.string.str_network_error));
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(i, REQUEST_ERR);
            return;
        } else {

            SharedPreferences prefsAppVer = getSharedPreferences("SaveAppVersion", MODE_PRIVATE);
            String mAppVersion = prefsAppVer.getString("AppVersion", "");
            mThisAppVersion = getAppVersion(this);

            if (mAppVersion.equals("") || mAppVersion == null || !(mAppVersion.equals(mThisAppVersion))) {
                saveAppVersion(mThisAppVersion);
                isVersionMatch = false;
                //unregisterToken();
            }

            checkMarketVersion();
        }
    }


    private void checkMarketVersion() {
        AppVersionCheckThread appVersionCheckThread = new AppVersionCheckThread();
        appVersionCheckThread.start();
    }

    private void checkRegId() {
        SharedPreferences prefs = getSharedPreferences("SaveRegId", MODE_PRIVATE);
        String regId = prefs.getString("setRegId", "1");


        if (regId.equals("") || regId == null || !isVersionMatch) {
            // 등록
            registerToken();
        } else {
            //StaticDataInfo.REGISTRATION_ID = regId;

            mRunnable = new Runnable() {
                @Override
                public void run() {
                    AutoLoginCheck();
                }
            };

            mHandler.postDelayed(mRunnable, 1000);
        }
    }

    /**
     * AutoLogin Check
     */
    private void AutoLoginCheck() {
        if (mHandler != null) mHandler.removeCallbacks(mRunnable);

        SharedPreferences prefs = getSharedPreferences("SaveLoginInfo", MODE_PRIVATE);
        final String sUID = prefs.getString("LogIn_ID", "");
        final String sPWD = prefs.getString("LogIn_PWD", "");

        if (sUID!=null && !sUID.trim().equals("")
                && sPWD!=null && !sPWD.trim().equals("")
                && !mThisAppVersion.equals("")) {
            new LoginInfo(IntroActivity.this, sUID, sPWD, mThisAppVersion, handler);
        } else {
            nextPage(false);
        }
    }

    /**
     * 로그인 check
     * @param isLogin true: Login 상태 o, false: Login 상태 x
     */
    private void nextPage(boolean isLogin) {
        Intent intent = null;
        if(isLogin) {
            intent = new Intent(this, MainActivity.class);
        }else{
            intent = new Intent(this, LoginActivity.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

        finish();
    }

    /**
     * GCM에 디바이스토큰 삭제
     */
    /*
    private void unregisterToken() {
        if (GCMRegistrar.isRegistered(this)) {
            GCMRegistrar.unregister(this);
        }
    }
    */
    /**
     * GCM에 디바이스 토큰 등록
     */
    /*
    private void registerToken() {
        // registration ID（디바이스 토큰) 취득하고 등록되지 않은 경우 GCM에 등록
        String regId = GCMRegistrar.getRegistrationId(this);

        if ("".equals(regId)) {
            GCMRegistrar.register(this, GCMIntentService.SENDER_ID);
        }

        mRegRunnable = new Runnable() {
            @Override
            public void run() {
                if(regstrationId==null || regstrationId.equals("") ){
                    //GCMRegistrar.register(IntroActivity.this, GCMIntentService.SENDER_ID);
                    registerToken();
                }else{
                    if(mRegHandler!=null && mRegRunnable!=null){
                        mRegHandler.removeCallbacks(mRegRunnable);
                    }
                }
            }
        };

        mRegHandler = new Handler(Looper.getMainLooper());
        mRegHandler.postDelayed(mRegRunnable, 5000);
    }
    */
    private void registerToken() {
        // registration ID（디바이스 토큰) 취득하고 등록되지 않은 경우 GCM에 등록
        /*
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("temp", "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        Log.d("temp", "token"+token+"]");

                        // Log and toast

                        regstrationId = token;
                        Log.d("temp", "token"+regstrationId+"]");

                        SharedPreferences prefs = getSharedPreferences("SaveRegId", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("setRegId", regstrationId);
                        editor.commit();


                        AutoLoginCheck();

                    }
                });

           */

    }




    /**
     * 현재 앱 버전 저장
     * @param appVer
     */
    private void saveAppVersion(String appVer){
        SharedPreferences prefs = getSharedPreferences("SaveAppVersion", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("AppVersion", appVer);
        editor.commit();
    }

    /**
     * @return {@code PackageManager}의 애플리케이션의 버전 코드.
     */
    private static String getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // 일어나지 않아야 합니다
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode == REQUEST_ERR){
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mRegHandler!=null && mRegRunnable!=null){
            mRegHandler.removeCallbacks(mRegRunnable);
        }

        if(mHandler!=null) mHandler.removeCallbacks(mRunnable);

        mLoadingAnimation.stop();

    }

    /**
     * 앱 버전을 체크하는 Thread
     */
    public class AppVersionCheckThread extends Thread {
        @Override
        public void run() {
            checkRegId();
            /*
            String storeVersion = MarketVersionChecker.getMarketVersion(getPackageName());

            if ((storeVersion != null) && (storeVersion.compareTo(mThisAppVersion) > 0)) {
                // 업데이트 필요
                // -> 앱 스토어 이동
                showUpdateAlert();
            }
            else {
                // 업데이트 불필요
                // -> checkRegId() 체크
                checkRegId();
            }
             */
        }
    }

    /**
     * 업데이트 팝업창 표시 메소드
     */
    private void showUpdateAlert() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ad.setMessage(R.string.str_update_message)
                        .setCancelable(false)
                        .setPositiveButton(R.string.str_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
                                marketLaunch.setData(Uri.parse("market://details?id="+getPackageName()));
                                startActivity(marketLaunch);

                                finish();
                            }
                        })
                        .setNegativeButton(R.string.str_no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });

                AlertDialog alert = ad.create();
                alert.setTitle(R.string.str_update_title);
                alert.show();
            }
        }, 1000);
    }
}
