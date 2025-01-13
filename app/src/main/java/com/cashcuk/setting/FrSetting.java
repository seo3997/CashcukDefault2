package com.cashcuk.setting;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.cashcuk.R;
import com.cashcuk.dialog.RadioListAdapter;

import java.util.ArrayList;

/**
 * 설정
 */
public class FrSetting extends Fragment implements View.OnTouchListener {
    private Activity mActivity;
    private TextView txtAccount; //계정
    private CheckBox chkSound; //소리
    private CheckBox chkVibrate; //진동
    private CheckBox chkAlrimPopup; //알림팝업

    private Dialog mDlg = null;
    private RadioListAdapter mRadioListAdapter;
    private RingtoneManager rm;
    private Cursor cursor;
    private int selSoundPosition;
    private int saveSoundPosition;

    // 알림음 설정
    private Ringtone ringtoneTemp;
    private ArrayList<String> arrSound;
    private TextView txtSetSound;
    private LinearLayout llSound;

    // 알립 팝업 설정
    private int saveRbPosition;
    private int selRbPosition;
    private String strSelNoti;
    private ArrayList<String> arrNotiType;

    private int mDlgMode=-1;
    private final int DIALOG_MODE_NOTI_SOUND = 0; //알림은 dlg
    private final int DIALOG_MODE_NOTI_TYPE = 1; //알림팝업 type dlg

    private LinearLayout layoutBG;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        try {
        } catch (ClassCastException e) {
            throw new ClassCastException(mActivity.toString() + " must implement OnArticleSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fr_setting, null);

        layoutBG = (LinearLayout) view.findViewById(R.id.ll_bg);
        layoutBG.setBackgroundDrawable(new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.display_bg)));

        // 계정
        ((LinearLayout) view.findViewById(R.id.ll_account)).setOnClickListener(mNewActivity);
        ((ImageButton) view.findViewById(R.id.ib_account)).setOnClickListener(mNewActivity);
        txtAccount = (TextView) view.findViewById(R.id.txt_account);
        SharedPreferences prefs = mActivity.getSharedPreferences("SaveLoginInfo", mActivity.MODE_PRIVATE);
        txtAccount.setText(prefs.getString("LogIn_ID", ""));

        //일반 - 공지사항
        ((LinearLayout) view.findViewById(R.id.ll_notice)).setOnClickListener(mNewActivity);
        ((ImageButton) view.findViewById(R.id.ib_notice)).setOnClickListener(mNewActivity);

        //알림 - 소리
        ((LinearLayout) view.findViewById(R.id.ll_sound)).setOnClickListener(mNewActivity);
        chkSound = (CheckBox) view.findViewById(R.id.chk_sound);
        //알림음
        txtSetSound = (TextView) view.findViewById(R.id.txt_setting_sound);
        llSound = (LinearLayout) view.findViewById(R.id.ll_setting_sound);
        llSound.setOnClickListener(mNewActivity);

        //알림 - 진동
        ((LinearLayout) view.findViewById(R.id.ll_vibrate)).setOnClickListener(mNewActivity);
        chkVibrate = (CheckBox) view.findViewById(R.id.chk_vibrate);

        //알림 - 알림팝업
        ((LinearLayout) view.findViewById(R.id.ll_alrim_popup)).setOnClickListener(mNewActivity);
        ((ImageButton) view.findViewById(R.id.ib_alrim_popup)).setOnClickListener(mNewActivity);

        arrNotiType = new ArrayList<String>();
        arrNotiType.clear();
        arrNotiType.add(getString(R.string.str_set_nofi_dig_always));
        arrNotiType.add(getString(R.string.str_set_nofi_dig_on_dp));
        arrNotiType.add(getString(R.string.str_set_nofi_dig_off));

        //회원가이드
        ((LinearLayout) view.findViewById(R.id.ll_tutorial)).setOnClickListener(mNewActivity);
        ((ImageButton) view.findViewById(R.id.ib_tutorial)).setOnClickListener(mNewActivity);

        //광고주가이드
        ((LinearLayout) view.findViewById(R.id.ll_guide_ad)).setOnClickListener(mNewActivity);
        ((ImageButton) view.findViewById(R.id.ib_guide_ad)).setOnClickListener(mNewActivity);

        //캐릭터가이드
        ((LinearLayout) view.findViewById(R.id.ll_guide_character)).setOnClickListener(mNewActivity);
        ((ImageButton) view.findViewById(R.id.ib_guide_character)).setOnClickListener(mNewActivity);

        //고객센터
        ((LinearLayout) view.findViewById(R.id.ll_customer_service)).setOnClickListener(mNewActivity);
        ((ImageButton) view.findViewById(R.id.ib_customer_service)).setOnClickListener(mNewActivity);

        //FAQ
        ((LinearLayout) view.findViewById(R.id.ll_faq)).setOnClickListener(mNewActivity);
        ((ImageButton) view.findViewById(R.id.ib_faq)).setOnClickListener(mNewActivity);

        //서비스 이용약관
        ((LinearLayout) view.findViewById(R.id.ll_service_agreement)).setOnClickListener(mNewActivity);
        ((ImageButton) view.findViewById(R.id.ib_service_agreement)).setOnClickListener(mNewActivity);

        //개인정보 취급방침
        ((LinearLayout) view.findViewById(R.id.ll_privacy_policy)).setOnClickListener(mNewActivity);
        ((ImageButton) view.findViewById(R.id.ib_privacy_policy)).setOnClickListener(mNewActivity);

        ((LinearLayout) view.findViewById(R.id.ll_company_introduction)).setOnClickListener(mNewActivity);
        ((ImageButton) view.findViewById(R.id.ib_company_introduction)).setOnClickListener(mNewActivity);

        TextView txtAppVersion = (TextView) view.findViewById(R.id.txt_setting_app_version);
        try {
            PackageInfo i = mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), 0);
            txtAppVersion.setText("v "+i.versionName);
        } catch(PackageManager.NameNotFoundException e) { }

        return view;
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


    public View.OnClickListener mNewActivity = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent mNewintent = null;
            int viewId = v.getId();
//계정
            if (viewId == R.id.ll_account || viewId == R.id.ib_account) {
                mNewintent = new Intent(mActivity, AccountActivity.class);
                mNewintent.putExtra("Account", txtAccount.getText().toString());
                //공지사항
            } else if (viewId == R.id.ll_notice || viewId == R.id.ib_notice) {
                mNewintent = new Intent(mActivity, SettingWebViewActivity.class);
                mNewintent.putExtra("DisplayMode", "Notice");
                //소리
            } else if (viewId == R.id.ll_sound) {
                chkSound.setChecked(!chkSound.isChecked());
                //선택 음
            } else if (viewId == R.id.ll_setting_sound) {
                SettingDlg(DIALOG_MODE_NOTI_SOUND);
                //진동
            } else if (viewId == R.id.ll_vibrate) {
                chkVibrate.setChecked(!chkVibrate.isChecked());
                //알림팝업
            } else if (viewId == R.id.ll_alrim_popup || viewId == R.id.ib_alrim_popup) {
                SettingDlg(DIALOG_MODE_NOTI_TYPE);
                //회원 가이드
            } else if (viewId == R.id.ll_tutorial || viewId == R.id.ib_tutorial) {
                mNewintent = new Intent(mActivity, GuideActivity.class);
                ArrayList<Integer> mUserImgList = new ArrayList<Integer>();
                mUserImgList.add(R.drawable.user1);
                mUserImgList.add(R.drawable.user2);
                mUserImgList.add(R.drawable.user3);
                mUserImgList.add(R.drawable.user4);
                mUserImgList.add(R.drawable.user5);
                mNewintent.putExtra("GuideAD", mUserImgList);
                //광고주가이드
            } else if (viewId == R.id.ll_guide_ad || viewId == R.id.ib_guide_ad) {
                mNewintent = new Intent(mActivity, GuideActivity.class);
                ArrayList<Integer> mAdvertiserImgList = new ArrayList<Integer>();
                mAdvertiserImgList.add(R.drawable.advertiser1);
                mAdvertiserImgList.add(R.drawable.advertiser2);
                mAdvertiserImgList.add(R.drawable.advertiser3);
                mAdvertiserImgList.add(R.drawable.advertiser4);
                mAdvertiserImgList.add(R.drawable.advertiser5);
                mAdvertiserImgList.add(R.drawable.advertiser6);
                mAdvertiserImgList.add(R.drawable.advertiser7);
                mAdvertiserImgList.add(R.drawable.advertiser8);
                mAdvertiserImgList.add(R.drawable.advertiser9);
                mNewintent.putExtra("GuideAD", mAdvertiserImgList);
                //캐릭터가이드
            } else if (viewId == R.id.ll_guide_character || viewId == R.id.ib_guide_character) {
                mNewintent = new Intent(mActivity, GuideActivity.class);
                ArrayList<Integer> mCharacterImgList = new ArrayList<Integer>();
                mCharacterImgList.add(R.drawable.character1);
                mCharacterImgList.add(R.drawable.character2);
                mCharacterImgList.add(R.drawable.character3);
                mCharacterImgList.add(R.drawable.character4);
                mNewintent.putExtra("GuideAD", mCharacterImgList);
                //고객센터
            } else if (viewId == R.id.ll_customer_service || viewId == R.id.ib_customer_service) {
                mNewintent = new Intent(mActivity, CustomerServiceActivity.class);
                //FAQ
            } else if (viewId == R.id.ll_faq || viewId == R.id.ib_faq) {
                mNewintent = new Intent(mActivity, SettingWebViewActivity.class);
                mNewintent.putExtra("DisplayMode", "FAQ");
                //서비스 이용약관
            } else if (viewId == R.id.ll_service_agreement || viewId == R.id.ib_service_agreement) {
                mNewintent = new Intent(mActivity, SettingWebViewActivity.class);
                mNewintent.putExtra("DisplayMode", "Service_Agreement");
                //개인정보 취급방침
            } else if (viewId == R.id.ll_privacy_policy || viewId == R.id.ib_privacy_policy) {
                mNewintent = new Intent(mActivity, SettingWebViewActivity.class);
                mNewintent.putExtra("DisplayMode", "PrivacyPolicy");
                //회사소개
            } else if (viewId == R.id.ll_company_introduction || viewId == R.id.ib_company_introduction) {
                mNewintent = new Intent(mActivity, CompanyIntroductionActivity.class);
            }

            if(mNewintent!=null){
                startActivity(mNewintent);
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        getNotiSysSound(); // 시스템 알림음 list
        getNotiSound(); // 사용자가 지정한 알림 음
        setNotiSound(); // 저장된 알림음

        getNotiType();
        getSaveData();
    }

    /**
     * 설정 값 가져옴.
     */
    public void getSaveData(){
        SharedPreferences prefs = mActivity.getSharedPreferences("SaveSetting", mActivity.MODE_PRIVATE);
        chkSound.setChecked(prefs.getBoolean("setSound", true));
        chkVibrate.setChecked(prefs.getBoolean("setVibrate", true));
    }

    private Button btnOK;
    private Button btnCancel;
    /**
     * 알림음, 알림팝업 타입 선택 시 popup 사용
     * @param dlgMode
     */
    public void SettingDlg(int dlgMode){
        mDlgMode = dlgMode;
        mDlg = new Dialog(mActivity);
        mDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDlg.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        mDlg.setContentView(R.layout.dlg_radio_list);

        TextView txtTitle = (TextView) mDlg.findViewById(R.id.txt_title);
        ListView lvData = (ListView) mDlg.findViewById(R.id.lv_radio_data);

        ((LinearLayout) mDlg.findViewById(R.id.ll_list_dig_ok)).setOnTouchListener(this);
        ((LinearLayout) mDlg.findViewById(R.id.ll_list_dig_cancel)).setOnTouchListener(this);
        btnOK = (Button) mDlg.findViewById(R.id.btn_list_dig_ok);
        btnCancel = (Button) mDlg.findViewById(R.id.btn_list_dig_cancel);
        btnOK.setOnTouchListener(this);
        btnCancel.setOnTouchListener(this);

        switch (dlgMode){
            case DIALOG_MODE_NOTI_SOUND: //알림음
                txtTitle.setText(getResources().getString(R.string.str_set_nofi_sound_dig));
                mRadioListAdapter = new RadioListAdapter(mActivity, arrSound);
                mRadioListAdapter.setSelectedIndex(saveSoundPosition);
                selSoundPosition = saveSoundPosition;
                lvData.setAdapter(mRadioListAdapter);

                // 소리 볼륨 얻어오기
                AudioManager am = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);
                int curVol = am.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
                // 알람 볼륨 설정
                am.setStreamVolume(AudioManager.STREAM_NOTIFICATION, curVol, AudioManager.FLAG_PLAY_SOUND);

                lvData.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        selSoundPosition = position;
                        Uri ringtoneUri = rm.getRingtoneUri(selSoundPosition);
                        Ringtone ringtone = RingtoneManager.getRingtone(mActivity, ringtoneUri);
                        ringtone.setStreamType(AudioManager.STREAM_NOTIFICATION);

                        if(ringtoneTemp!=null) ringtoneTemp.stop();
                        ringtoneTemp = ringtone;

                        ringtone.play();

                        mRadioListAdapter.setSelectedIndex(selSoundPosition);
                        mRadioListAdapter.notifyDataSetChanged();
                    }
                });
                break;
            case DIALOG_MODE_NOTI_TYPE: //알림팝업
                txtTitle.setText(getResources().getString(R.string.str_setting_alrim_popup));
                mRadioListAdapter = new RadioListAdapter(mActivity, arrNotiType);
                mRadioListAdapter.setSelectedIndex(saveRbPosition);
                selRbPosition = saveRbPosition;
                lvData.setAdapter(mRadioListAdapter);

                lvData.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        selRbPosition = position;

                        strSelNoti = arrNotiType.get(selRbPosition);
                        mRadioListAdapter.setSelectedIndex(selRbPosition);
                        mRadioListAdapter.notifyDataSetChanged();
                    }
                });
                break;
        }
        if (mDlg != null && !mDlg.isShowing()) mDlg.show();
    }

    public View.OnClickListener mRBClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            int viewId = v.getId();
            if (viewId == R.id.btn_list_dig_cancel) {
                if(mDlgMode==DIALOG_MODE_NOTI_SOUND){
                    mRadioListAdapter.setSelectedIndex(saveSoundPosition);
                }else if(mDlgMode==DIALOG_MODE_NOTI_TYPE){
                    mRadioListAdapter.setSelectedIndex(saveRbPosition);
                }
                mDlgMode=-1;
            } else if (viewId == R.id.btn_list_dig_ok) {
                if(mDlgMode==DIALOG_MODE_NOTI_SOUND){
                    saveSoundPosition = selSoundPosition;
                    String selSound = rm.getRingtone(selSoundPosition).getTitle(mActivity);
                    txtSetSound.setText(selSound);

                    SaveNotiSound();
                }else if(mDlgMode==DIALOG_MODE_NOTI_TYPE){
                    saveRbPosition = selRbPosition;

                    SaveNoti();
                }
                mDlgMode=-1;
            }
            if (mDlg!=null && mDlg.isShowing()) mDlg.dismiss();
        }
    };

    /**
     * 시스템 알림음 list
     */
    public void getNotiSysSound() {
        // 알림 사운드 리스트 가져옴
        rm = new RingtoneManager(mActivity);
        rm.setType(RingtoneManager.TYPE_NOTIFICATION);
        cursor = rm.getCursor();

        arrSound = new ArrayList<String>();
        arrSound.clear();

        cursor.moveToFirst();
        if (cursor != null) {

            do {
                String strSound = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
                arrSound.add(strSound);
            } while (cursor.moveToNext());
        }
    }

    /**
     * 알림팝업 타입 가져오기
     */
    public void getNotiType() {
        SharedPreferences prefs = mActivity.getSharedPreferences("SaveNoti", mActivity.MODE_PRIVATE);
        saveRbPosition = prefs.getInt("svaeRbPosition", 0);
    }

    /**
     * 선택한 알림 음 가져오기
     */
    public void getNotiSound() {
        SharedPreferences prefs = mActivity.getSharedPreferences("SaveNotiSound", mActivity.MODE_PRIVATE);
        saveSoundPosition = prefs.getInt("svaeSoundPosition", 0);
    }

    /**
     * 알림 음 저장
     */
    public void SaveNotiSound() {
        SharedPreferences prefs = mActivity.getSharedPreferences("SaveNotiSound", mActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("rmUri", rm.getRingtone(selSoundPosition).toString());
        editor.putString("rmPath", rm.getRingtoneUri(selSoundPosition).toString());
        editor.putInt("svaeSoundPosition", selSoundPosition);
        editor.commit();
    }

    /**
     * 설정한 알림음 가져옴.
     */
    public void setNotiSound(){
        SharedPreferences prefs = mActivity.getSharedPreferences("SaveNotiSound", mActivity.MODE_PRIVATE);
        saveSoundPosition = prefs.getInt("svaeSoundPosition", 0);
        txtSetSound.setText(rm.getRingtone(saveSoundPosition).getTitle(mActivity));
    }

    /**
     * 알림팝업 타입 저장
     */
    public void SaveNoti() {
        SharedPreferences prefs = mActivity.getSharedPreferences("SaveNoti", mActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("setNoti", strSelNoti);
        editor.putInt("svaeRbPosition", selRbPosition);
        editor.commit();
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences prefs = mActivity.getSharedPreferences("SaveSetting", mActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("setSound", chkSound.isChecked()); //소리
        editor.putBoolean("setVibrate", chkVibrate.isChecked()); //진동
        editor.commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mDlg!=null && mDlg.isShowing()){
            mDlg.dismiss();
        }

        recycleView(layoutBG);
    }

    public View.OnClickListener mCheckClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(v.getId() == R.id.ll_list_dig_ok || v.getId() == R.id.btn_list_dig_ok){
            if(event.getAction()==MotionEvent.ACTION_DOWN) btnOK.setBackgroundDrawable(getResources().getDrawable(R.drawable.dlg_chk_press));
            if(event.getAction()==MotionEvent.ACTION_UP){
                if(mDlgMode==DIALOG_MODE_NOTI_SOUND){
                    saveSoundPosition = selSoundPosition;
                    String selSound = rm.getRingtone(selSoundPosition).getTitle(mActivity);
                    txtSetSound.setText(selSound);

                    SaveNotiSound();
                }else if(mDlgMode==DIALOG_MODE_NOTI_TYPE){
                    saveRbPosition = selRbPosition;

                    SaveNoti();
                }
                mDlgMode=-1;

                if(mDlg!=null && mDlg.isShowing()) mDlg.dismiss();
            }
            return true;
        }else if(v.getId() == R.id.ll_list_dig_cancel || v.getId() == R.id.btn_list_dig_cancel){
            if(event.getAction()==MotionEvent.ACTION_DOWN) btnCancel.setBackgroundDrawable(getResources().getDrawable(R.drawable.dlg_chk_press));
            if(event.getAction()==MotionEvent.ACTION_UP){
                if(mDlgMode==DIALOG_MODE_NOTI_SOUND){
                    mRadioListAdapter.setSelectedIndex(saveSoundPosition);
                }else if(mDlgMode==DIALOG_MODE_NOTI_TYPE){
                    mRadioListAdapter.setSelectedIndex(saveRbPosition);
                }
                mDlgMode=-1;

                if(mDlg!=null && mDlg.isShowing()) mDlg.dismiss();
            }
            return true;
        }
        return false;
    }
}
