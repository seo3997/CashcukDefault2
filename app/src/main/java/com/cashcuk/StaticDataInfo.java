package com.cashcuk;

import android.app.Activity;
import android.net.Uri;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 공용 변수 및 함수
 */
public class StaticDataInfo {
    public static Activity currentActivity;

    public static final int TIME_OUT = 5000;
    public static final String STRING_Y = "Y";
    public static final String STRING_N = "N";

    public static final String STRING_M = "M";
    public static final String STRING_P = "P";

    public static final int FALSE = 0;
    public static final int TRUE = 1;
    public static final int CHARAGE_REJECT = 2;

    public static final int RESULT_CODE_MY_POINT = 1000;

    public static final int RESULT_NO_USER = 601; //사용자 정보 없음 (존재하지 않는 ID)
    public static final int RESULT_PWD_ERR = 602; //패스워드 틀림
    public static final int RESULT_CODE_200 = 200;
    public static final int RESULT_CODE_ADVERTISER = 201; //광고주
    public static final int RESULT_CODE_NO_ADVERTISER = 202; //비광고주
    public static final int RESULT_CODE_WAIT_ADVERTISER = 203; //광고주 승인대기
    public static final int RESULT_CODE_REJECT_ADVERTISER = 204; //승인거부
    public static final int RESULT_CODE_AUTHORIZATION_CODE_ERR = 503; //인증번호 실패
    public static final int RESULT_OVER_SAVE_POINT = 504; //적립 횟수 초과
    public static final int RESULT_NO_SAVE_POINT = 100; //적립 금액 없음
    public static final int RESULT_OVERLAP_ERR = 603; //ID 중복 된 값 있음
    public static final int RESULT_NO_DATA = 604; //데이터 없음
    public static final int RESULT_NO_RECOMMEND = 605; //추천인 없음
    public static final int RESULT_NO_DI = 606; //DI 값 없음.
    public static final int RESULT_OVERLAP_DI = 607; //DI 값 중복.
    public static final int RESULT_NO_SIGUN = 608; // 행정 구역(시/군/구)없음
    public static final int RESULT_NO_INPUT_RECOMMEND = 609; //재 가입 시 이전의 회원가입 시 입력한 추천인을 입력 할 수 없음.
    public static final int RESULT_CODE_ERR = 00; //오류

//    public static final int RESULT_CODE_ERR = 301; //통신 에러 (1. timeout 포함, 2.결과 값 못받음 등)
    public static final String STR_P = "p";
    public static final String TAG_ITEM = "item";
    public static final String TAG_LIST = "<list>";
    public static final String TAG_NO_LIST = "<list/>";
    public static final int SEND_URL = 0;
    public static final int SEND_TOKEN = 1;

    public static final String COMMON_CODE_TYPE_AD = "ad"; //공통코드 (광고 카테고리)
    public static final String COMMON_CODE_TYPE_DS = "ds"; //공통코드 (광고발송기본정보)
    public static final String COMMON_CODE_TYPE_BK = "bk"; //공통코드 (은행)
    public static final String COMMON_CODE_TYPE_JD = "jd"; //공통코드 (회원탈퇴 사유)
    public static final String COMMON_CODE_TYPE_CH = "ch"; //공통코드 (캐릭터 카테고리)

    public static final int RESULT_SI_DO = 5555; //시/도
    public static final int RESULT_SI_GUN_GU = 6666; //시/군/구

    public static final int RESULT_SEND_AD_INFO = 7777; //광고발송기본정보

    public static final int RESULT_AD_INFO = 8888; //광고기본정보

    public static final String MODE_POINT_LIST_ACCRUE = "Acc"; //누적포인트 모드
    public static final String MODE_POINT_LIST_USE = "Use"; //사용포인트 모드

    public static final String MODE_MY_AD = "My"; //관심광고 모드

    public static final int ADVER_INTEREST = 111; //관심광고 등록 or 해제

    public static final String SEX_MEN = "M";
    public static final String SEX_FEMALE = "F";

    public static final String BELL_AD_CODE_AGE_10 = "73";
    public static final String BELL_AD_CODE_AGE_20 = "74";
    public static final String BELL_AD_CODE_AGE_30 = "75";
    public static final String BELL_AD_CODE_AGE_40 = "95";
    public static final String BELL_AD_CODE_AGE_50 = "96";
    public static final String BELL_AD_CODE_AGE_60 = "97";
    public static final String BELL_AD_CODE_AGE_70 = "99";
    public static final String BELL_AD_CODE_SEX_MEN = "116";
    public static final String BELL_AD_CODE_SEX_FEMALE = "117";

    public static final String BELL_AD_NO_TIME = "NoTime";

    public static String STR_CHARATER_DOWN_OTHER = "DownOther"; //캐릭터 다운인지 다른 카테고리인지..
    public static String STR_CHARATER_DOWN = "Down"; //캐릭터 다운
    public static String STR_CHARATER_OTHER = "Other"; //다른 카테고리

    public static String STR_CHARGE_CODE_TAKE = "TAKE";     //충전금 마이너스 코드




    /**
     * 현재 시간 구하는 함수
     * @return 현재 시간
     */
    public static String NowTime(){
        // 현재 시간을 msec으로 구한다.
        long now = System.currentTimeMillis();
        // 현재 시간을 저장 한다.
        Date date = new Date(now);
        // 시간 포맷으로 만든다.
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        return sdfNow.format(date);
    }

    /**
     * 천단위 콤마
     * @param str
     * @return
     */
    public static String makeStringComma(String str) {
        boolean isPoint = false;
        String[] arrStr = new String[2];

        if (str==null || str.length() == 0) {
            return "";
        }else if(str.contains(",")){
            return str;
        }

        if(str.contains(".")){
            arrStr = str.split("[.]");
            str = arrStr[0];

            isPoint = true;
        }

        long value = Long.parseLong(str);
        DecimalFormat format = new DecimalFormat("###,###");

        String strComma = format.format(value);
        if(isPoint){
            strComma += ("."+arrStr[1]);
            isPoint = false;
        }
        return strComma;
    }

    public static void delDir(String path)
    {
        File file = new File(path);
        File[] childFileList = file.listFiles();
        
        if(childFileList!=null && childFileList.length>0) {
            for (File childFile : childFileList) {
                if (childFile.isDirectory()) {
                    delDir(childFile.getAbsolutePath());     //하위 디렉토리 루프
                } else {
                    childFile.delete();    //하위 파일삭제
                }
            }
        }
        file.delete();    //root 삭제
    }
}
