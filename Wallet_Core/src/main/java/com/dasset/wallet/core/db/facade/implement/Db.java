package com.dasset.wallet.core.db.facade.implement;

import android.database.sqlite.SQLiteDatabase;

import com.dasset.wallet.core.db.base.ICursor;
import com.dasset.wallet.core.db.base.IDb;
import com.google.common.base.Function;

public class Db implements IDb {

    private SQLiteDatabase sqLiteDatabase;

    public Db(SQLiteDatabase sqLiteDatabase) {
        this.sqLiteDatabase = sqLiteDatabase;
    }

    public SQLiteDatabase getSQLiteDatabase() {
        return this.sqLiteDatabase;
    }

    @Override
    public void beginTransaction() {
        this.sqLiteDatabase.beginTransaction();
    }

    @Override
    public void endTransaction() {
        this.sqLiteDatabase.setTransactionSuccessful();
        this.sqLiteDatabase.endTransaction();
    }

    @Override
    public void close() {
        this.sqLiteDatabase.close();
    }

    @Override
    public void execUpdate(String sql, String[] parameterss) {
        if (parameterss == null) {
            parameterss = new String[]{};
        }
        this.getSQLiteDatabase().execSQL(sql, parameterss);
    }

    @Override
    public void execQueryOneRecord(String sql, String[] parameterss, Function<ICursor, Void> func) {
        ICursor c = new Cursor(this.getSQLiteDatabase().rawQuery(sql, parameterss));
        if (c.moveToNext()) {
            func.apply(c);
        }
        c.close();
    }

    @Override
    public void execQueryLoop(String sql, String[] parameterss, Function<ICursor, Void> func) {
        ICursor c = new Cursor(this.getSQLiteDatabase().rawQuery(sql, parameterss));
        while (c.moveToNext()) {
            func.apply(c);
        }
        c.close();
    }
}
