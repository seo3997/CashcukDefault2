package com.cashcuk.ad.adlist;

import java.io.Serializable;

/**
 * 광고 보기 list에 필요한 data
 */
public class ListADInfo implements Serializable {
    private String strIdx="";
    private String strName="";
    private String strDetail="";
    private String strTitleImgUrl="";
    private String strPoint="";
    private String strStatus=""; //심사여부
    private String strRejectCuz=""; //승인거부사유
    private String strEtc="";
    private String strGrade=""; //평점
    private String strEventYN=""; //행사여부
    private String strContents="";
    private String strIsRead=""; //광고확인 여부 0:확인안함, 1: 확인
    private String strIsDel = ""; //광고 취소(삭제) 가능 여부(모바일에서 REQA(승인요청), RREQ(심사재요청)일 때 사용. 0: 취소버튼 d/p)


    public String getStrIdx() {
        return strIdx;
    }

    public void setStrIdx(String strIdx) {
        this.strIdx = strIdx;
    }

    public String getStrName() {
        return strName;
    }

    public void setStrName(String strName) {
        this.strName = strName;
    }

    public String getStrDetail() {
        return strDetail;
    }

    public void setStrDetail(String strDetail) {
        this.strDetail = strDetail;
    }

    public String getStrTitleImgUrl() {
        return strTitleImgUrl;
    }

    public void setStrTitleImgUrl(String strTitleImgUrl) {
        this.strTitleImgUrl = strTitleImgUrl;
    }

    public String getStrPoint() {
        return strPoint;
    }

    public void setStrPoint(String strPoint) {
        this.strPoint = strPoint;
    }

    public String getStrStatus() {
        return strStatus;
    }

    public void setStrStatus(String strStatus) {
        this.strStatus = strStatus;
    }

    public String getStrRejectCuz() {
        return strRejectCuz;
    }

    public void setStrRejectCuz(String strRejectCuz) {
        this.strRejectCuz = strRejectCuz;
    }

    public String getStrEtc() {
        return strEtc;
    }

    public void setStrEtc(String strEtc) {
        this.strEtc = strEtc;
    }

    public String getStrGrade() {
        return strGrade;
    }

    public void setStrGrade(String strGrade) {
        this.strGrade = strGrade;
    }

    public String getStrEventYN() {
        return strEventYN;
    }

    public void setStrEventYN(String strEventYN) {
        this.strEventYN = strEventYN;
    }

    public String getStrContents() {
        return strContents;
    }

    public void setStrContents(String strContents) {
        this.strContents = strContents;
    }

    public String getStrIsRead() {
        return strIsRead;
    }

    public void setStrIsRead(String strIsRead) {
        this.strIsRead = strIsRead;
    }

    public String getStrIsDel() {
        return strIsDel;
    }

    public void setStrIsDel(String strIsDel) {
        this.strIsDel = strIsDel;
    }
}
