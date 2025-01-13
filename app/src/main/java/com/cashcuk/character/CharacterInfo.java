package com.cashcuk.character;

import java.io.Serializable;

/**
 * 캐릭터 data info (캬테고리 별)
 */
public class CharacterInfo implements Serializable {
    private String strIdx=""; //idx
    private String strRep=""; //대표이미지 여부(1:대표, 0: 대표아님)
    private String strImgUrl=""; //이미지 url
    private String strTxt=""; //전하는 글
    private boolean isChk = false;
    private String strMiddleImgUrl = ""; //캐릭터 thumbnail

    public boolean getIsChk() {
        return isChk;
    }

    public void setIsChk(boolean isChk) {
        this.isChk = isChk;
    }

    public String getStrIdx() {
        return strIdx;
    }

    public void setStrIdx(String strIdx) {
        this.strIdx = strIdx;
    }

    public String getStrRep() {
        return strRep;
    }

    public void setStrRep(String strRep) {
        this.strRep = strRep;
    }

    public String getStrImgUrl() {
        return strImgUrl;
    }

    public void setStrImgUrl(String strImgUrl) {
        this.strImgUrl = strImgUrl;
    }

    public String getStrTxt() {
        return strTxt;
    }

    public void setStrTxt(String strTxt) {
        this.strTxt = strTxt;
    }

    public String getStrMiddleImgUrl() {
        return strMiddleImgUrl;
    }

    public void setStrMiddleImgUrl(String strMiddleImgUrl) {
        this.strMiddleImgUrl = strMiddleImgUrl;
    }
}
