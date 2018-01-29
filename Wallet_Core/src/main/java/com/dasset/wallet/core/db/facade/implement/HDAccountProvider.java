package com.dasset.wallet.core.db.facade.implement;

import android.content.ContentValues;
import android.database.sqlite.SQLiteOpenHelper;

import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.core.contant.db.HDAccountColumns;
import com.dasset.wallet.core.contant.db.Tables;
import com.dasset.wallet.core.crypto.PasswordSeed;
import com.dasset.wallet.core.db.base.IDb;
import com.dasset.wallet.core.db.facade.wrapper.HDAccountProviderWrapper;
import com.dasset.wallet.core.db.helper.TxDataBaseHelper;
import com.dasset.wallet.core.utils.Base58;

public class HDAccountProvider extends HDAccountProviderWrapper {

    private static HDAccountProvider blockProvider;
    private SQLiteOpenHelper sqLiteOpenHelper;

    private HDAccountProvider(SQLiteOpenHelper sqLiteOpenHelper) {
        this.sqLiteOpenHelper = sqLiteOpenHelper;
    }

    public static synchronized HDAccountProvider getInstance() {
        if (blockProvider == null) {
            blockProvider = new HDAccountProvider(TxDataBaseHelper.getInstance());
        }
        return blockProvider;
    }

    public static void releaseInstance() {
        if (blockProvider != null) {
            blockProvider = null;
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
    protected int insertHDAccountToDb(IDb iDb, String encryptedMnemonicSeed, String encryptSeed, String firstAddress, boolean isXrandom, byte[] externalPublicKey, byte[] internalPublicKey) {
        Db db = (Db) iDb;
        ContentValues cv = new ContentValues();
        cv.put(HDAccountColumns.ENCRYPT_SEED, encryptSeed);
        cv.put(HDAccountColumns.ENCRYPT_MNMONIC_SEED, encryptedMnemonicSeed);
        cv.put(HDAccountColumns.IS_XRANDOM, isXrandom ? 1 : 0);
        cv.put(HDAccountColumns.HD_ADDRESS, firstAddress);
        cv.put(HDAccountColumns.EXTERNAL_PUB, Base58.encode(externalPublicKey));
        cv.put(HDAccountColumns.INTERNAL_PUB, Base58.encode(internalPublicKey));
        return (int) db.getSQLiteDatabase().insert(Tables.HD_ACCOUNT, null, cv);
    }

    @Override
    protected boolean hasPasswordSeed(IDb db) {
        return AddressProvider.getInstance().hasPasswordSeed(db);
    }

    @Override
    protected void addPasswordSeed(IDb db, PasswordSeed passwordSeed) {
        AddressProvider.getInstance().addPasswordSeed(db, passwordSeed);
    }

    @Override
    protected int insertMonitorHDAccountToDb(IDb iDb, String firstAddress, boolean isXrandom, byte[] externalPublicKey, byte[] internalPublicKey) {
        LogUtil.getInstance().print(String.format("firstAddress:%s", firstAddress));
        LogUtil.getInstance().print(String.format("externalPublicKey:%s", Base58.encode(externalPublicKey)));
        LogUtil.getInstance().print(String.format("internalPublicKey:%s", Base58.encode(internalPublicKey)));
        Db db = (Db) iDb;
        ContentValues cv = new ContentValues();
        cv.put(HDAccountColumns.HD_ADDRESS, firstAddress);
        cv.put(HDAccountColumns.IS_XRANDOM, isXrandom ? 1 : 0);
        cv.put(HDAccountColumns.EXTERNAL_PUB, Base58.encode(externalPublicKey));
        cv.put(HDAccountColumns.INTERNAL_PUB, Base58.encode(internalPublicKey));
        return (int) db.getSQLiteDatabase().insert(Tables.HD_ACCOUNT, null, cv);
    }
}
