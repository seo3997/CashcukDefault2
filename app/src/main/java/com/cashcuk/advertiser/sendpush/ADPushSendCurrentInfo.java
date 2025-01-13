package com.cashcuk.advertiser.sendpush;

/**
 * 전체 PUSH 현황 (PUSH 발송 현황) data info
 */
public class ADPushSendCurrentInfo {
    private String strPushIdx;
    private String strADName;
    private String strSendNum;
    private String strPushState;
    private String strRejectCuz;
    private String strSendDate;
    private String strSendPushCost;
    private String strSendTotalCost="";

    public String getStrPushIdx() {
        return strPushIdx;
    }

    public void setStrPushIdx(String strPushIdx) {
        this.strPushIdx = strPushIdx;
    }

    public String getStrADName() {
        return strADName;
    }

    public void setStrADName(String strADName) {
        this.strADName = strADName;
    }

    public String getStrSendNum() {
        return strSendNum;
    }

    public void setStrSendNum(String strSendNum) {
        this.strSendNum = strSendNum;
    }

    public String getStrPushState() {
        return strPushState;
    }

    public void setStrPushState(String strPushState) {
        this.strPushState = strPushState;
    }

    public String getStrRejectCuz() {
        return strRejectCuz;
    }

    public void setStrRejectCuz(String strRejectCuz) {
        this.strRejectCuz = strRejectCuz;
    }

    public String getStrSendDate() {
        return strSendDate;
    }

    public void setStrSendDate(String strSendDate) {
        this.strSendDate = strSendDate;
    }

    public String getStrSendPushCost() {
        return strSendPushCost;
    }

    public void setStrSendPushCost(String strSendPushCost) {
        this.strSendPushCost = strSendPushCost;
    }

    public String getStrSendTotalCost() {
        return strSendTotalCost;
    }

    public void setStrSendTotalCost(String strSendTotalCost) {
        this.strSendTotalCost = strSendTotalCost;
    }
}
