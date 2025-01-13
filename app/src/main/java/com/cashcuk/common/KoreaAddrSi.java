package com.cashcuk.common;

/**
 * Created by jun on 2017-08-30.
 */

public enum KoreaAddrSi {
     GuDong1("고양시 덕양구")
    ,GuDong2("고양시 일산동구")
    ,GuDong3("고양시 일산서구")
    ,GuDong4("부천시 소사구")
    ,GuDong5("부천시 오정구")
    ,GuDong6("부천시 원미구")
    ,GuDong7("성남시 분당구")
    ,GuDong8("성남시 수정구")
    ,GuDong9("성남시 중원구")
    ,GuDong10("수원시 권선구")
    ,GuDong11("수원시 영통구")
    ,GuDong12("수원시 장안구")
    ,GuDong13("수원시 팔달구")
    ,GuDong14("안산시 단원구")
    ,GuDong15("안산시 상록구")
    ,GuDong16("안양시 동안구")
    ,GuDong17("안양시 만안구")
    ,GuDong18("용인시 기흥구")
    ,GuDong19("용인시 수지구")
    ,GuDong20("용인시 처인구")
    ,GuDong21("포항시 남구")
    ,GuDong22("포항시 북구")
    ,GuDong23("창원시 마산합포구")
    ,GuDong24("창원시 마산회원구")
    ,GuDong25("창원시 성산구")
    ,GuDong26("창원시 의창구")
    ,GuDong27("창원시 진해구")
    ,GuDong28("전주시 덕진구")
    ,GuDong29("전주시 완산구")
    ,GuDong30("청주시 상당구")
    ,GuDong31("청주시 서원구")
    ,GuDong32("청주시 흥덕구")
    ,GuDong33("청주시 청원구")
    ,GuDong34("천안시 동남구")
    ,GuDong35("천안시 서북구")
    ;


    private String guDongName;
    KoreaAddrSi(String guDongName)
    {
        this.guDongName = guDongName;
    }

    public String getGuDongName() {
        return guDongName;
    }

    public void setGuDongName(String guDongName) {
        this.guDongName = guDongName;
    }

}
