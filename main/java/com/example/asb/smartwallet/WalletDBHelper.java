package com.example.asb.smartwallet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;

public class WalletDBHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "SmartWallet.db";
    public static final String TRANSACTIONS_TABLE_NAME = "Wallet_transactions";
    public static final String TRANSACTIONS_COLUMN_ID = "ID";
    public static final String TRANSACTIONS_COLUMN_TIMESTAMP = "Timestamp";
    public static final String TRANSACTIONS_COLUMN_AMOUNT = "Amount";
    public static final String TRANSACTIONS_COLUMN_TYPE = "Incoming_outgoing";
    private HashMap hp;

    public WalletDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists Wallet_transactions (ID integer primary key, Amount integer, Incoming_outgoing integer, Timestamp varchar)");
 }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL("DROP TABLE IF EXISTS "+TRANSACTIONS_TABLE_NAME);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public boolean insertTransaction (Integer type, Integer amount, String timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
//        contentValues.put(TRANSACTIONS_COLUMN_ID, NULL);

        contentValues.put(TRANSACTIONS_COLUMN_TYPE, type);
        contentValues.put(TRANSACTIONS_COLUMN_AMOUNT, amount);
        contentValues.put(TRANSACTIONS_COLUMN_TIMESTAMP, timestamp);
        try{
            db.insert("Wallet_transactions", null, contentValues);
            Log.i("Wallet","Transaction recorded successfully");

        }catch (Exception e){
            Log.i("Wallet","Error inserting");

        }
        return true;
    }

    public Cursor getDataById(Integer id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery( "select * from "+TRANSACTIONS_TABLE_NAME+" where "+ TRANSACTIONS_COLUMN_ID+"="+id+"", null );
        return res;
    }

    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, TRANSACTIONS_TABLE_NAME);
        return numRows;
    }

    public boolean updateTransaction (Integer id, Integer type, Integer amount, String timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TRANSACTIONS_COLUMN_TYPE, type);
        contentValues.put(TRANSACTIONS_COLUMN_AMOUNT, amount);
        contentValues.put(TRANSACTIONS_COLUMN_TIMESTAMP, timestamp);
        db.update(TRANSACTIONS_TABLE_NAME, contentValues, TRANSACTIONS_COLUMN_ID+" = ? ", new String[] { Integer.toString(id)} );
        return true;
    }

    public Integer deleteTransaction (Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TRANSACTIONS_TABLE_NAME,TRANSACTIONS_COLUMN_ID+" = ? ",new String[] { Integer.toString(id) });
    }

    public ArrayList<String> getAllTransactions() {
        ArrayList<String> array_list = new ArrayList<String>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery( "select * from Wallet_transactions", null );
        Integer num = res.getCount();
        Log.e("Wallet", num.toString());
        res.moveToFirst();

        while(!res.isAfterLast()){
            array_list.add(res.getString(res.getColumnIndex(TRANSACTIONS_COLUMN_AMOUNT)));
            Log.e("Bla", res.getString(res.getColumnIndex(TRANSACTIONS_COLUMN_TIMESTAMP)));
            res.moveToNext();
        }
        res.close();
        return array_list;
    }
    public ArrayList<Transaction> getCompleteTransactions() {
        ArrayList<Transaction> array_list = new ArrayList<Transaction>();

        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery( "select * from Wallet_transactions", null );
        Integer num = res.getCount();
        Log.e("Wallet", num.toString());
        res.moveToFirst();

        while(!res.isAfterLast()){
            Integer amt = res.getInt(res.getColumnIndex(TRANSACTIONS_COLUMN_AMOUNT));
            Integer typ = res.getInt(res.getColumnIndex(TRANSACTIONS_COLUMN_TYPE));
            String timstp = res.getString(res.getColumnIndex(TRANSACTIONS_COLUMN_TIMESTAMP));
            Transaction t = new Transaction(amt,typ,timstp);
            array_list.add(t);
            //Log.e("Bla", res.getString(res.getColumnIndex(TRANSACTIONS_COLUMN_TIMESTAMP)));
            res.moveToNext();
        }
        res.close();
        return array_list;
    }
    public ArrayList<Transaction> getCompleteTransactionsWithIDs() {
        ArrayList<Transaction> array_list = new ArrayList<Transaction>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery( "select * from Wallet_transactions", null );
        Integer num = res.getCount();
        Log.e("Wallet", num.toString());
        res.moveToFirst();

        while(!res.isAfterLast()){
            Integer id = res.getInt(res.getColumnIndex(TRANSACTIONS_COLUMN_ID));
            Integer amt = res.getInt(res.getColumnIndex(TRANSACTIONS_COLUMN_AMOUNT));
            Integer typ = res.getInt(res.getColumnIndex(TRANSACTIONS_COLUMN_TYPE));
            String timstp = res.getString(res.getColumnIndex(TRANSACTIONS_COLUMN_TIMESTAMP));
            Transaction t = new Transaction(amt,typ,timstp,id);
            array_list.add(t);
            //Log.e("Bla", res.getString(res.getColumnIndex(TRANSACTIONS_COLUMN_TIMESTAMP)));
            res.moveToNext();
        }
        res.close();
        return array_list;
    }
}