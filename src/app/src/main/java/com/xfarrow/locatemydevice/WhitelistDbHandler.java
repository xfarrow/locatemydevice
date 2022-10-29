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
        db.delete(TABLE_CONTACTS, KEY_PH_NO + " = ?", new String[] { phoneNo });
        db.close();
    }

    public ArrayList<String> getAllContacts() {
        ArrayList<String> array_list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = String.format("SELECT %s FROM %s", KEY_PH_NO, TABLE_CONTACTS);
        Cursor cursor =  db.rawQuery( query, null );
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(KEY_PH_NO);

        while(!cursor.isAfterLast()){
            array_list.add(cursor.getString(columnIndex));
            cursor.moveToNext();
        }
        cursor.close();
        return array_list;
    }

    /*
    * Checks if a contact is already in the database.
    *
    * If a phoneNumber with an international country code is provided, checks if exists the same
    * number without the country code. This is very useful if the user stores in the whitelist a
    * number without the international country code because it'll be checked against the SMS sender's
    * number obtained from the Broadcast, always containing an international country code.
    *
    * This might introduce a security risk: if the user saves the following number: 12345
    * then it will accept communications from +1 12345, +2 12345, +3 12345 and so on...
    * but we do not know what international prefix the regional "12345" has. The alternative would
    * be to deny accepting local phone numbers.
    */
    public boolean isContactPresent(String phoneNo){
        SQLiteDatabase db = this.getWritableDatabase();
        // SELECT KEY_PH_NO FROM TABLE_CONTACTS WHERE KEY_PH_NO = "phoneNo" OR KEY_PH_NO = "phoneNumberWithoutPrefix"
        String query = String.format("SELECT %s FROM %s WHERE %s = \"%s\"",
                KEY_PH_NO,
                TABLE_CONTACTS,
                KEY_PH_NO,
                phoneNo
                );

        String countryCode = Utils.extractCountryCodeFromPhoneNumber(phoneNo);
        if(countryCode != null){
            String phoneNumberWithoutPrefix = phoneNo.replace("+", "").replace(countryCode, "");
            query += String.format(" OR %s = \"%s\"",
                    KEY_PH_NO,
                    phoneNumberWithoutPrefix);
        }

        Cursor cursor = db.rawQuery(query, null);
        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }
}
