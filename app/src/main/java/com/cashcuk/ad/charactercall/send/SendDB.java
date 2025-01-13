package com.cashcuk.ad.charactercall.send;

import android.provider.BaseColumns;

/**
 * 발신측 DB
 */
public class SendDB {
    public static final String CHAR_KIND = "kind";
    public static final String HP_NUM = "hp_num";
    public static final String HP_EMAIL = "hp_email";
    public static final String CHAR_IMG_PATH = "path";

    public static final String CHAR_IMG_IDX = "char_idx";
    //    public static final String CHAR_IMG_URL = "char_url";
    public static final String CHAR_SEND_MSG = "char_send_msg";
    public static final String CHAR_TITLE_TXT = "title_txt";

    public static abstract class CreateDB implements BaseColumns {
        public static final String _TABLENAME = "table_send";
        public static final String _CREATE =
                "create table " + _TABLENAME + "("
                        + _ID + " integer primary key autoincrement, "
                        + CHAR_KIND + " text , "
                        + HP_NUM + " text , "
                        + HP_EMAIL + " text , "
                        + CHAR_IMG_PATH + " text , "
                        + CHAR_IMG_IDX + " text not null , "
                        + CHAR_SEND_MSG + " text , "
                        + CHAR_TITLE_TXT + " text);";
    }
}
