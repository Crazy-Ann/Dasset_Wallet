package com.dasset.wallet.core.db.facade.implement;

import android.content.ContentValues;
import android.database.sqlite.SQLiteOpenHelper;

import com.dasset.wallet.core.Address;
import com.dasset.wallet.core.contant.db.AddressesColumns;
import com.dasset.wallet.core.contant.db.EnterpriseHDAccountColumns;
import com.dasset.wallet.core.contant.db.HDMAddressesColumns;
import com.dasset.wallet.core.contant.db.HDSeedsColumns;
import com.dasset.wallet.core.contant.db.Tables;
import com.dasset.wallet.core.db.base.IDb;
import com.dasset.wallet.core.db.facade.wrapper.AddressProviderWrapper;
import com.dasset.wallet.core.db.helper.AddressDatabaseHelper;
import com.dasset.wallet.core.utils.Base58;
import com.dasset.wallet.core.utils.Utils;

public class AddressProvider extends AddressProviderWrapper {

    private static AddressProvider addressProvider;
    private SQLiteOpenHelper sqLiteOpenHelper;

    private AddressProvider(SQLiteOpenHelper sqLiteOpenHelper) {
        this.sqLiteOpenHelper = sqLiteOpenHelper;
    }

    public static synchronized AddressProvider getInstance() {
        if (addressProvider == null) {
            addressProvider = new AddressProvider(AddressDatabaseHelper.getInstance());
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
    protected int insertHDKeyToDb(IDb iDb, String encryptedMnemonicSeed, String encryptHdSeed, String firstAddress, boolean isXrandom) {
        Db db = (Db) iDb;
        ContentValues contentValues = new ContentValues();
        contentValues.put(HDSeedsColumns.ENCRYPT_MNEMONIC_SEED, encryptedMnemonicSeed);
        contentValues.put(HDSeedsColumns.ENCRYPT_HD_SEED, encryptHdSeed);
        contentValues.put(HDSeedsColumns.IS_XRANDOM, isXrandom ? 1 : 0);
        contentValues.put(HDSeedsColumns.HDM_ADDRESS, firstAddress);
        return (int) db.getSQLiteDatabase().insert(Tables.HDSEEDS, null, contentValues);
    }

    @Override
    protected int insertEnterpriseHDKeyToDb(IDb db, String encryptedMnemonicSeed, String encryptHdSeed, String firstAddress, boolean isXrandom) {
        Db mdb = (Db) db;
        ContentValues contentValues = new ContentValues();
        contentValues.put(EnterpriseHDAccountColumns.ENCRYPT_MNEMONIC_SEED, encryptedMnemonicSeed);
        contentValues.put(EnterpriseHDAccountColumns.ENCRYPT_SEED, encryptHdSeed);
        contentValues.put(EnterpriseHDAccountColumns.IS_XRANDOM, isXrandom ? 1 : 0);
        contentValues.put(EnterpriseHDAccountColumns.HD_ADDRESS, firstAddress);
        return (int) mdb.getSQLiteDatabase().insert(Tables.ENTERPRISE_HD_ACCOUNT, null, contentValues);
    }

    @Override
    protected void insertHDMAddressToDb(IDb db, String address, int hdSeedId, int index, byte[] pubKeysHot, byte[] pubKeysCold, byte[] pubKeysRemote, boolean isSynced) {
        Db mdb = (Db) db;
        ContentValues contentValues = new ContentValues();
        contentValues.put(HDMAddressesColumns.HD_SEED_ID, hdSeedId);
        contentValues.put(HDMAddressesColumns.HD_SEED_INDEX, index);
        contentValues.put(HDMAddressesColumns.PUB_KEY_HOT, Base58.encode(pubKeysHot));
        contentValues.put(HDMAddressesColumns.PUB_KEY_COLD, Base58.encode(pubKeysCold));
        if (Utils.isEmpty(address)) {
            contentValues.putNull(HDMAddressesColumns.ADDRESS);
        } else {
            contentValues.put(HDMAddressesColumns.ADDRESS, address);
        }
        if (pubKeysRemote == null) {
            contentValues.putNull(HDMAddressesColumns.PUB_KEY_REMOTE);
        } else {
            contentValues.put(HDMAddressesColumns.PUB_KEY_REMOTE, Base58.encode(pubKeysRemote));
        }
        contentValues.put(HDMAddressesColumns.IS_SYNCED, isSynced ? 1 : 0);
        mdb.getSQLiteDatabase().insert(Tables.HDMADDRESSES, null, contentValues);
    }

    @Override
    protected void insertAddressToDb(IDb iDb, Address address) {
        Db db = (Db) iDb;
        ContentValues contentValues = new ContentValues();
        contentValues.put(AddressesColumns.ADDRESS, address.getAddress());
        if (address.hasPrivateKey()) {
            contentValues.put(AddressesColumns.ENCRYPT_PRIVATE_KEY, address.getEncryptPrivKeyOfDb());
        }
        contentValues.put(AddressesColumns.PUB_KEY, Base58.encode(address.getPubKey()));
        contentValues.put(AddressesColumns.IS_XRANDOM, address.isFromXRandom() ? 1 : 0);
        contentValues.put(AddressesColumns.IS_SYNCED, address.isSyncComplete() ? 1 : 0);
        contentValues.put(AddressesColumns.IS_TRASH, address.isTrashed() ? 1 : 0);
        contentValues.put(AddressesColumns.SORT_TIME, address.getSortTime());
        db.getSQLiteDatabase().insert(Tables.Addresses, null, contentValues);
    }
}
