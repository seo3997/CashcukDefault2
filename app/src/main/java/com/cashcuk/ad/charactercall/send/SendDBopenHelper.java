package com.cashcuk.ad.charactercall.send;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.SQLException;

/**
 * 발신측 DB OpenHelper
 */
public class SendDBopenHelper {
    private static final String DATABASE_NAME_SEND = "SendChar.db";
    private static final int DATABASE_VERSION = 1;
    public static SQLiteDatabase mDB;
    private DatabaseHelper mDBHelper;
    private Context mContext;

    private class DatabaseHelper extends SQLiteOpenHelper {

        // 생성자
        public DatabaseHelper(Context context, String name,
                              SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        // 최초 DB를 만들때 한번만 호출된다.
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SendDB.CreateDB._CREATE);
        }

        // 버전이 업데이트 되었을 경우 DB를 다시 만들어 준다.
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS "+SendDB.CreateDB._TABLENAME);
            onCreate(db);
        }
    }

    public SendDBopenHelper(Context context){
        this.mContext = context;
    }

    public SendDBopenHelper open() throws SQLException {
        mDBHelper = new DatabaseHelper(mContext, DATABASE_NAME_SEND, null, DATABASE_VERSION);
//        mDB = mDBHelper.getWritableDatabase();
        return this;
    }

    public void close(){
        mDB.close();
    }

    //저장
    public void insert(String kind, String email, String receiveNum, String localPath, String imgIdx, String sendMsg, String title){
        mDB = mDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SendDB.CHAR_KIND, kind);
        values.put(SendDB.HP_EMAIL, email);
        values.put(SendDB.HP_NUM, receiveNum);
        values.put(SendDB.CHAR_IMG_PATH, localPath);
        values.put(SendDB.CHAR_IMG_IDX, imgIdx);
        values.put(SendDB.CHAR_SEND_MSG, sendMsg);
        values.put(SendDB.CHAR_TITLE_TXT, title);

        mDB.insert(SendDB.CreateDB._TABLENAME, null, values);
    }

    //수정
    public void update(String id, String strCharKind, String localPath, String imgIdx, String sendMsg, String title) {
        mDB = mDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SendDB.CHAR_KIND, strCharKind);
        values.put(SendDB.CHAR_IMG_PATH, localPath);
        values.put(SendDB.CHAR_IMG_IDX, imgIdx);
        values.put(SendDB.CHAR_SEND_MSG, sendMsg);
        values.put(SendDB.CHAR_TITLE_TXT, title);
        mDB.update(SendDB.CreateDB._TABLENAME, values, SendDB.CreateDB._ID + "=?", new String[]{id});
    }//end update

    public void deleteAll() {
        mDB = mDBHelper.getWritableDatabase();
        String sql = "delete from "+SendDB.CreateDB._TABLENAME;
        mDB.execSQL(sql);
    }

    public Cursor Search(String strWhere){
        mDB = mDBHelper.getReadableDatabase();
        String sql = "select * from " + SendDB.CreateDB._TABLENAME + " where " + strWhere;
        Cursor cursor = mDB.rawQuery(sql, null);
        return cursor;
    }

    // Data 삭제
    public void removeData(int index){
        String sql = "delete from " + SendDB.CreateDB._TABLENAME + " where id = "+index+";";
        mDB.execSQL(sql);
    }
}
