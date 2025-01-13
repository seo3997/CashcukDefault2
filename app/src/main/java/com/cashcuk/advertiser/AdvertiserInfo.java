package com.cashcuk.advertiser;

import java.io.Serializable;

/**
 * 광고주 정보
 */
public class AdvertiserInfo implements Serializable {
    private String strTradeName; //상호명
    private String strRepresentativeName; //대표자명
    private String strRepresentativeTel; //대표전화
    private String strBusinessmanNum; //사업자 번호
    private String strHomePage; //홈페이지
    private String strAddr; //주소
    private String strImgUrl; //이미지 url
    private String strBizIdx=""; //광고주 idx

    public String getStrTradeName() {
        return strTradeName;
    }

    public void setStrTradeName(String strTradeName) {
        this.strTradeName = strTradeName;
    }

    public String getStrRepresentativeName() {
        return strRepresentativeName;
    }

    public void setStrRepresentativeName(String strRepresentativeName) {
        this.strRepresentativeName = strRepresentativeName;
    }

    public String getStrRepresentativeTel() {
        return strRepresentativeTel;
    }

    public void setStrRepresentativeTel(String strRepresentativeTel) {
        this.strRepresentativeTel = strRepresentativeTel;
    }

    public String getStrBusinessmanNum() {
        return strBusinessmanNum;
    }

    public void setStrBusinessmanNum(String strBusinessmanNum) {
        this.strBusinessmanNum = strBusinessmanNum;
    }

    public String getStrHomePage() {
        return strHomePage;
    }

    public void setStrHomePage(String strHomePage) {
        this.strHomePage = strHomePage;
    }

    public String getStrAddr() {
        return strAddr;
    }

    public void setStrAddr(String strAddr) {
        this.strAddr = strAddr;
    }

    public String getStrImgUrl() {
        return strImgUrl;
    }

    public void setStrImgUrl(String strImgUrl) {
        this.strImgUrl = strImgUrl;
    }

    public String getStrBizIdx() {
        return strBizIdx;
    }

    public void setStrBizIdx(String strBizIdx) {
        this.strBizIdx = strBizIdx;
    }
}
