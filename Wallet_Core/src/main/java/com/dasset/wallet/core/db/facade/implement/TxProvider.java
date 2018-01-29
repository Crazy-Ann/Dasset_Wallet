package com.dasset.wallet.core.db.facade.implement;

import android.content.ContentValues;
import android.database.sqlite.SQLiteOpenHelper;

import com.dasset.wallet.core.In;
import com.dasset.wallet.core.Out;
import com.dasset.wallet.core.Tx;
import com.dasset.wallet.core.contant.db.InsColumns;
import com.dasset.wallet.core.contant.db.OutsColumns;
import com.dasset.wallet.core.contant.db.Tables;
import com.dasset.wallet.core.contant.db.TxsColumns;
import com.dasset.wallet.core.db.base.IDb;
import com.dasset.wallet.core.db.facade.wrapper.TxProviderWrapper;
import com.dasset.wallet.core.db.helper.TxDataBaseHelper;
import com.dasset.wallet.core.utils.Base58;
import com.dasset.wallet.core.utils.Utils;

import java.util.List;

public class TxProvider extends TxProviderWrapper {

    private static TxProvider addressProvider;
    private SQLiteOpenHelper sqLiteOpenHelper;

    private TxProvider(SQLiteOpenHelper sqLiteOpenHelper) {
        this.sqLiteOpenHelper = sqLiteOpenHelper;
    }

    public static synchronized TxProvider getInstance() {
        if (addressProvider == null) {
            addressProvider = new TxProvider(TxDataBaseHelper.getInstance());
        }
        return addressProvider;
    }

    public static void releaseInstance() {
        if (addressProvider != null) {
            addressProvider = null;
        }
    }

    @Override
    public IDb getReadableDatabase() {
        return new Db(this.sqLiteOpenHelper.getReadableDatabase());
    }

    @Override
    public IDb getWritableDatabase() {
        return new Db(this.sqLiteOpenHelper.getWritableDatabase());
    }

    @Override
    public List<Tx> getUnspendTxWithAddress(String address, List<Out> unSpentOuts) {
        return null;
    }

    @Override
    public List<Out> getUnspentOutputByBlockNo(long BlockNo, String address) {
        return null;
    }

    @Override
    public Out getTxPreOut(byte[] txHash, int OutSn) {
        return null;
    }

    @Override
    protected void insertTxToDb(IDb iDb, Tx tx) {
        Db db = (Db) iDb;
        ContentValues contentValues = new ContentValues();
        if (tx.getBlockNo() != Tx.TX_UNCONFIRMED) {
            contentValues.put(TxsColumns.BLOCK_NO, tx.getBlockNo());
        } else {
            contentValues.putNull(TxsColumns.BLOCK_NO);
        }
        contentValues.put(TxsColumns.TX_HASH, Base58.encode(tx.getTxHash()));
        contentValues.put(TxsColumns.SOURCE, tx.getSource());
        contentValues.put(TxsColumns.TX_TIME, tx.getTxTime());
        contentValues.put(TxsColumns.TX_VER, tx.getTxVer());
        contentValues.put(TxsColumns.TX_LOCKTIME, tx.getTxLockTime());
        db.getSQLiteDatabase().insert(Tables.TXS, null, contentValues);
    }

    @Override
    protected void insertInToDb(IDb iDb, In in) {
        Db db = (Db) iDb;
        ContentValues contentValues = new ContentValues();
        contentValues.put(InsColumns.TX_HASH, Base58.encode(in.getTxHash()));
        contentValues.put(InsColumns.IN_SN, in.getInSn());
        contentValues.put(InsColumns.PREV_TX_HASH, Base58.encode(in.getPrevTxHash()));
        contentValues.put(InsColumns.PREV_OUT_SN, in.getPrevOutSn());
        if (in.getInSignature() != null) {
            contentValues.put(InsColumns.IN_SIGNATURE, Base58.encode(in.getInSignature()));
        } else {
            contentValues.putNull(InsColumns.IN_SIGNATURE);
        }
        contentValues.put(InsColumns.IN_SEQUENCE, in.getInSequence());
        db.getSQLiteDatabase().insert(Tables.INS, null, contentValues);
    }

    @Override
    protected void insertOutToDb(IDb iDb, Out out) {
        Db mdb = (Db) iDb;
        ContentValues cv = new ContentValues();
        cv.put(OutsColumns.TX_HASH, Base58.encode(out.getTxHash()));
        cv.put(OutsColumns.OUT_SN, out.getOutSn());
        cv.put(OutsColumns.OUT_SCRIPT, Base58.encode(out.getOutScript()));
        cv.put(OutsColumns.OUT_VALUE, out.getOutValue());
        cv.put(OutsColumns.OUT_STATUS, out.getOutStatus().getValue());
        if (!Utils.isEmpty(out.getOutAddress())) {
            cv.put(OutsColumns.OUT_ADDRESS, out.getOutAddress());
        } else {
            cv.putNull(OutsColumns.OUT_ADDRESS);
        }
        //support hd
        if (out.getHDAccountId() != -1) {
            cv.put(OutsColumns.HD_ACCOUNT_ID,
                   out.getHDAccountId());
        } else {
            cv.putNull(OutsColumns.HD_ACCOUNT_ID);
        }
        mdb.getSQLiteDatabase().insert(Tables.OUTS, null, cv);
    }
}
