package com.cashcuk.common;

/**
 * Created by jun on 2017-08-29.
 */

public enum KoreaGuDong {
    GuDong1("달성군")
   ,GuDong2("옹진군")
   ;


    private String guDongName;
    KoreaGuDong(String guDongName) {
        this.guDongName = guDongName;
    }

    public String getGuDongName() {
        return guDongName;
    }

    public void setGuDongName(String guDongName) {
        this.guDongName = guDongName;
    }

}
