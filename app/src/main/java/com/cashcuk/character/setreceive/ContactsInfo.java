package com.cashcuk.character.setreceive;

/**
 * 전화번호 정보
 */
public class ContactsInfo {
    private String strEmail;
    private String strPhoneNum;
    private String strName;
    private boolean isChk;

    public boolean getIsChk() {
        return isChk;
    }

    public void setIsChk(boolean isChk) {
        this.isChk = isChk;
    }

    public String getStrEmail() {
        return strEmail;
    }

    public void setStrEmail(String strEmail) {
        this.strEmail = strEmail;
    }

    public String getStrPhoneNum() {
        return strPhoneNum;
    }

    public void setStrPhoneNum(String strPhoneNum) {
        this.strPhoneNum = strPhoneNum;
    }

    public String getStrName() {
        return strName;
    }

    public void setStrName(String strName) {
        this.strName = strName;
    }
}
