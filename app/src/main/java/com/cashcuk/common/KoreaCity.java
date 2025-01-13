package com.cashcuk.common;

/**
 * Created by jun on 2017-08-29.
 */

public enum KoreaCity {
    SEOUL("서울")
    , BUSAN("부산")
    , INCHEON("인천")
    , DAEGU("대구")
    , DAEJEON("대전")
    , JEJU("제주특별자치도")
    , SEJONG("세종특별자치시")
    , GYEONGGI("경기")
    , GANGWON("강원")
    , GYEONGBUK("경북")
    , GYEONGNAM("경남")
    , JEOLLABBUK("전북")
    , JEOLLABNAM("전남")
    , CHUNGCHEONGBUK("충북")
    , CHUNGCHEONGNAM("충남")
    , GWANGUN("광주")
    , ULSAN("울산")
    ;

    private String cityName;
    KoreaCity(String cityName) {
        this.cityName = cityName;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

}
