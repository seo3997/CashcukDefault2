package com.cashcuk.ad.charactercall.receive;

/**
 * 전화 수신 시 return data info
 */
public class ReceiveInfo {
    private String strIncomingEmail;
    private String strCharImgIdx;
    private String strCharImgUrl;
    private String strCharSendMsg;
    private String strCharTitle;

    public String getStrIncomingEmail() {
        return strIncomingEmail;
    }

    public void setStrIncomingEmail(String strIncomingEmail) {
        this.strIncomingEmail = strIncomingEmail;
    }

    public String getStrCharImgIdx() {
        return strCharImgIdx;
    }

    public void setStrCharImgIdx(String strCharImgIdx) {
        this.strCharImgIdx = strCharImgIdx;
    }

    public String getStrCharImgUrl() {
        return strCharImgUrl;
    }

    public void setStrCharImgUrl(String strCharImgUrl) {
        this.strCharImgUrl = strCharImgUrl;
    }

    public String getStrCharSendMsg() {
        return strCharSendMsg;
    }

    public void setStrCharSendMsg(String strCharSendMsg) {
        this.strCharSendMsg = strCharSendMsg;
    }

    public String getStrCharTitle() {
        return strCharTitle;
    }

    public void setStrCharTitle(String strCharTitle) {
        this.strCharTitle = strCharTitle;
    }
}
