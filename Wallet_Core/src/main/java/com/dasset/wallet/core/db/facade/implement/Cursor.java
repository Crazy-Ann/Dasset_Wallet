package com.dasset.wallet.core.db.facade.implement;

import com.dasset.wallet.core.db.base.ICursor;

public class Cursor implements ICursor {

    private android.database.Cursor cursor;

    public Cursor(android.database.Cursor cursor) {
        this.cursor = cursor;
    }

    @Override
    public int getCount() {
        return cursor.getCount();
    }

    @Override
    public int getPosition() {
        return cursor.getPosition();
    }

    @Override
    public boolean move(int var1) {
        return cursor.move(var1);
    }

    @Override
    public boolean moveToPosition(int var1) {
        return cursor.moveToPosition(var1);
    }

    @Override
    public boolean moveToFirst() {
        return cursor.moveToFirst();
    }

    @Override
    public boolean moveToLast() {
        return cursor.moveToLast();
    }

    @Override
    public boolean moveToNext() {
        return cursor.moveToNext();
    }

    @Override
    public boolean moveToPrevious() {
        return cursor.moveToPrevious();
    }

    @Override
    public boolean isFirst() {
        return cursor.isFirst();
    }

    @Override
    public boolean isLast() {
        return cursor.isFirst();
    }

    @Override
    public boolean isBeforeFirst() {
        return cursor.isBeforeFirst();
    }

    @Override
    public boolean isAfterLast() {
        return cursor.isAfterLast();
    }

    @Override
    public int getColumnIndex(String var1) {
        return cursor.getColumnIndex(var1);
    }

    @Override
    public int getColumnIndexOrThrow(String var1) throws IllegalArgumentException {
        return cursor.getColumnIndexOrThrow(var1);
    }

    @Override
    public String getColumnName(int var1) {
        return cursor.getColumnName(var1);
    }

    @Override
    public String[] getColumnNames() {
        return cursor.getColumnNames();
    }

    @Override
    public int getColumnCount() {
        return cursor.getColumnCount();
    }

    @Override
    public byte[] getBlob(int var1) {
        return cursor.getBlob(var1);
    }

    @Override
    public String getString(int var1) {
        return cursor.getString(var1);
    }

    @Override
    public short getShort(int var1) {
        return cursor.getShort(var1);
    }

    @Override
    public int getInt(int var1) {
        return cursor.getInt(var1);
    }

    @Override
    public long getLong(int var1) {
        return cursor.getLong(var1);
    }

    @Override
    public float getFloat(int var1) {
        return cursor.getFloat(var1);
    }

    @Override
    public double getDouble(int var1) {
        return cursor.getDouble(var1);
    }

    @Override
    public int getType(int var1) {
        return cursor.getType(var1);
    }

    @Override
    public boolean isNull(int var1) {
        return cursor.isNull(var1);
    }

    @Override
    public void close() {
        cursor.close();
    }

    @Override
    public boolean isClosed() {
        return cursor.isClosed();
    }
}
