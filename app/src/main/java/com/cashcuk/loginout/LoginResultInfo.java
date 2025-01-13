package com.cashcuk.loginout;

/**
 * 로그인 결과 info
 */
public class LoginResultInfo {
    private String strIdx="";
    private String strToken="";
    private String strSi="";
    private String strGu="";
    private String strSex="";
    private String strAge="";

    public String getStrSi() {
        return strSi;
    }

    public void setStrSi(String strSi) {
        this.strSi = strSi;
    }

    public String getStrGu() {
        return strGu;
    }

    public void setStrGu(String strGu) {
        this.strGu = strGu;
    }

    public String getStrIdx() {
        return strIdx;
    }

    public void setStrIdx(String strIdx) {
        this.strIdx = strIdx;
    }

    public String getStrToken() {
        return strToken;
    }

    public void setStrToken(String strToken) {
        this.strToken = strToken;
    }

    public String getStrSex() {
        return strSex;
    }

    public void setStrSex(String strSex) {
        this.strSex = strSex;
    }

    public String getStrAge() {
        return strAge;
    }

    public void setStrAge(String strAge) {
        this.strAge = strAge;
    }
}
