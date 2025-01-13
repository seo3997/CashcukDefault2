package com.cashcuk.advertiser.sendpush.view;

import java.io.Serializable;

/**
 * Push 상세보기 View
 */
public class PushSendViewInfo implements Serializable {
    private String strPushIdx; //push idx
    private String strADIdx; //공고 idx
    private String strAddr; //지역 (시/군) - 형식: 1>부산, 2>서울, #3>경기도
    private String strAddrSub; //지역 (시/군/구) - 형식: 1>남구, 2>구로구, 3>안산시
    private String strInfo; //일반정보 (title) - 형식: 1>취미, 2>기호음식
    private String strInfoSub; //일반정보(sub) - 형식: 1>농구, 2>떡볶이
    private String strTargetNum; //대상자 수
    private String strPushSendNum; //발송건수
    private String strSendCost; //발송 단가
    private String strPushState; //push 상태
    private String strPushImgUrl; //push 이미지 url
    private String strPushYN; //재발송 여부
    private String strPushDate; // 요청 시간

    public String getStrPushIdx() {
        return strPushIdx;
    }

    public void setStrPushIdx(String strPushIdx) {
        this.strPushIdx = strPushIdx;
    }

    public String getStrADIdx() {
        return strADIdx;
    }

    public void setStrADIdx(String strADIdx) {
        this.strADIdx = strADIdx;
    }

    public String getStrAddr() {
        return strAddr;
    }

    public void setStrAddr(String strAddr) {
        this.strAddr = strAddr;
    }

    public String getStrAddrSub() {
        return strAddrSub;
    }

    public void setStrAddrSub(String strAddrSub) {
        this.strAddrSub = strAddrSub;
    }

    public String getStrInfo() {
        return strInfo;
    }

    public void setStrInfo(String strInfo) {
        this.strInfo = strInfo;
    }

    public String getStrInfoSub() {
        return strInfoSub;
    }

    public void setStrInfoSub(String strInfoSub) {
        this.strInfoSub = strInfoSub;
    }

    public String getStrTargetNum() {
        return strTargetNum;
    }

    public void setStrTargetNum(String strTargetNum) {
        this.strTargetNum = strTargetNum;
    }

    public String getStrPushSendNum() {
        return strPushSendNum;
    }

    public void setStrPushSendNum(String strPushSendNum) {
        this.strPushSendNum = strPushSendNum;
    }

    public String getStrSendCost() {
        return strSendCost;
    }

    public void setStrSendCost(String strSendCost) {
        this.strSendCost = strSendCost;
    }

    public String getStrPushState() {
        return strPushState;
    }

    public void setStrPushState(String strPushState) {
        this.strPushState = strPushState;
    }

    public String getStrPushImgUrl() {
        return strPushImgUrl;
    }

    public void setStrPushImgUrl(String strPushImgUrl) {
        this.strPushImgUrl = strPushImgUrl;
    }

    public String getStrPushYN() {
        return strPushYN;
    }

    public void setStrPushYN(String strPushYN) {
        this.strPushYN = strPushYN;
    }

    public String getStrPushDate() {
        return strPushDate;
    }

    public void setStrPushDate(String strPushDate) {
        this.strPushDate = strPushDate;
    }
}
