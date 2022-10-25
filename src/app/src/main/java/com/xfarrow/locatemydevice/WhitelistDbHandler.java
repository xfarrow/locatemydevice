package com.xfarrow.locatemydevice;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class WhitelistDbHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "WhitelistDb";
    private static final String TABLE_CONTACTS = "Contacts";
    private static final String KEY_ID = "id";
    private static final String KEY_PH_NO = "phone_number";

    public WhitelistDbHandler(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
                + KEY_ID + " " + "INTEGER PRIMARY KEY,"
                + KEY_PH_NO + " " +  "TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
        onCreate(db);
    }

    // Add a contact
    void addContact(String phoneNo){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_PH_NO, phoneNo); // Contact Phone
        db.insert(TABLE_CONTACTS, null, values);
        db.close(); // Closing database connection
    }

    // Deleting single contact
    public void deleteContact(String phoneNo) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTACTS, KEY_ID + " = ?", new String[] { phoneNo });
        db.close();
    }

    public ArrayList<String> getAllContacts() {
        ArrayList<String> array_list = new ArrayList<String>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor =  db.rawQuery( "select * from " + TABLE_CONTACTS, null );
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(KEY_PH_NO);

        while(!cursor.isAfterLast()){
            array_list.add(cursor.getString(columnIndex));
            cursor.moveToNext();
        }
        cursor.close();
        return array_list;
    }

    public boolean isContactPresent(String phoneNo){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "Select * from " + TABLE_CONTACTS + " where " + KEY_PH_NO + " = " + "\"" + phoneNo + "\"";
        Cursor cursor = db.rawQuery(query, null);
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

}
