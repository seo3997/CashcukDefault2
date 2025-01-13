package com.cashcuk.character.updown;

/**
 * 캐릭터 공유,받기 list data info
 */
public class CharacterUpDownInfo {
    private String strIdx; //item idx
    private String strCharImg; //이미지 url
    private String strCharThumbnail;
    private String strCharName; //명칭
    private String strCompayName; //회사명
    private String strSellerId; //공유인 id
    private String strCategoryIdx; //카테고리 idx (upload 일때 사용)
    private String strDownState; //다운 받은것인지 아닌지 chk

    public String getStrSellerId() {
        return strSellerId;
    }

    public void setStrSellerId(String strSellerId) {
        this.strSellerId = strSellerId;
    }

    public String getStrCompayName() {
        return strCompayName;
    }

    public void setStrCompayName(String strCompayName) {
        this.strCompayName = strCompayName;
    }

    public String getStrCharName() {
        return strCharName;
    }

    public void setStrCharName(String strCharName) {
        this.strCharName = strCharName;
    }

    public String getStrCharImg() {
        return strCharImg;
    }

    public void setStrCharImg(String strCharImg) {
        this.strCharImg = strCharImg;
    }

    public String getStrIdx() {
        return strIdx;
    }

    public void setStrIdx(String strIdx) {
        this.strIdx = strIdx;
    }

    public String getStrCategoryIdx() {
        return strCategoryIdx;
    }

    public void setStrCategoryIdx(String strCategoryIdx) {
        this.strCategoryIdx = strCategoryIdx;
    }

    public String getStrDownState() {
        return strDownState;
    }

    public void setStrDownState(String strDownState) {
        this.strDownState = strDownState;
    }

    public String getStrCharThumbnail() {
        return strCharThumbnail;
    }

    public void setStrCharThumbnail(String strCharThumbnail) {
        this.strCharThumbnail = strCharThumbnail;
    }
}
