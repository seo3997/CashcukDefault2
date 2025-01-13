package com.cashcuk.push;

/**
 * PUSH 보관함 data info
 */
public class PushDataInfo {
    private String strIdx;
    private String strADName;
    private String strDate;
    private String strPoint;
    private String strInfoMsg; //기본 이용방법 및 주의사항
    private String strPushIdx;

    public String getStrIdx() {
        return strIdx;
    }

    public void setStrIdx(String strIdx) {
        this.strIdx = strIdx;
    }

    public String getStrADName() {
        return strADName;
    }

    public void setStrADName(String strADName) {
        this.strADName = strADName;
    }

    public String getStrDate() {
        return strDate;
    }

    public void setStrDate(String strDate) {
        this.strDate = strDate;
    }

    public String getStrPoint() {
        return strPoint;
    }

    public void setStrPoint(String strPoint) {
        this.strPoint = strPoint;
    }

    public String getStrInfoMsg() {
        return strInfoMsg;
    }

    public void setStrInfoMsg(String strInfoMsg) {
        this.strInfoMsg = strInfoMsg;
    }

    public String getStrPushIdx() {
        return strPushIdx;
    }

    public void setStrPushIdx(String strPushIdx) {
        this.strPushIdx = strPushIdx;
    }
}
