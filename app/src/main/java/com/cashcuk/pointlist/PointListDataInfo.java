package com.cashcuk.pointlist;

/**
 * 누적 or 사용 포인트 내역 data info
 */
public class PointListDataInfo {
    private String strDate=""; //일자
    private String strContent=""; //내용
    private String strPoint=""; //누적 or 사용 포인트
    private String strRequestState=""; //신청상태
    private String strAccount=""; //계좌번호
    private String strAccountHolder=""; //예금주
    private String strEtc=""; //입금처리 설명

    public String getStrDate() {
        return strDate;
    }

    public void setStrDate(String strDate) {
        this.strDate = strDate;
    }

    public String getStrContent() {
        return strContent;
    }

    public void setStrContent(String strContent) {
        this.strContent = strContent;
    }

    public String getStrPoint() {
        return strPoint;
    }

    public void setStrPoint(String strPoint) {
        this.strPoint = strPoint;
    }

    public String getStrRequestState() {
        return strRequestState;
    }

    public void setStrRequestState(String strRequestState) {
        this.strRequestState = strRequestState;
    }

    public String getStrAccount() {
        return strAccount;
    }

    public void setStrAccount(String strAccount) {
        this.strAccount = strAccount;
    }

    public String getStrAccountHolder() {
        return strAccountHolder;
    }

    public void setStrAccountHolder(String strAccountHolder) {
        this.strAccountHolder = strAccountHolder;
    }

    public String getStrEtc() {
        return strEtc;
    }

    public void setStrEtc(String strEtc) {
        this.strEtc = strEtc;
    }
}
