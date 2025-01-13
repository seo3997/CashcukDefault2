package com.cashcuk.common;

/**
 * Created by jun on 2017-08-29.
 */

public class CommonUtil {

    static public  String getAreaGu(KoreaCity pKoreaCity) {
        String sReturn="";
        switch (pKoreaCity) {
            case SEOUL :
                sReturn = "서울시";
                break;
            case BUSAN :
                sReturn = "부산광역시";
                break;
            case INCHEON :
                sReturn = "인천광역시";
                break;
            case DAEGU :
                sReturn = "대구광역시";
                break;
            case DAEJEON :
                sReturn = "대전광역시";
                break;
            case JEJU :
                sReturn = "제주특별자치도";
                break;
            case SEJONG :
                sReturn = "세종특별자치시";
                break;
            case GYEONGGI :
                sReturn = "경기도";
                break;
            case GANGWON :
                sReturn = "강원도";
                break;
            case GYEONGBUK :
                sReturn = "경상북도";
                break;
            case GYEONGNAM :
                sReturn = "경상남도";
                break;
            case JEOLLABBUK :
                sReturn = "전라북도";
                break;
            case JEOLLABNAM :
                sReturn = "전라남도";
                break;
            case CHUNGCHEONGBUK :
                sReturn = "충청북도";
                break;
            case CHUNGCHEONGNAM :
                sReturn = "충청남도";
                break;
            case GWANGUN :
                sReturn = "광주광역시";
                break;
            case ULSAN :
                sReturn = "울산광역시";
                break;
            default :
                sReturn = "서울시";
                break;
        }
        return sReturn;
    }

    static public  String getGudong(KoreaGuDong pKoreaGuDong) {
        String sReturn="";
        switch (pKoreaGuDong) {
            case GuDong1 :
                sReturn = "달서구";
                break;
            case GuDong2 :
                sReturn = "옹진구";
                break;
            default :
                sReturn = "강남구";
                break;
        }
        return sReturn;
    }


}
