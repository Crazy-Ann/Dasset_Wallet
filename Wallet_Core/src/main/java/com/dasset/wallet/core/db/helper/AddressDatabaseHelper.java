package com.dasset.wallet.core.db.helper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.dasset.wallet.base.application.BaseApplication;
import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.components.utils.SharedPreferenceUtil;
import com.dasset.wallet.core.contant.Constant;
import com.dasset.wallet.core.db.facade.BaseProvider;
import com.dasset.wallet.core.utils.Utils;

public class AddressDatabaseHelper extends SQLiteOpenHelper {

    private static AddressDatabaseHelper addressDatabaseHelper;

    private AddressDatabaseHelper() {
        super(BaseApplication.getInstance(), Constant.DATA_BASE_ADDRESS, null, Constant.DATA_BASE_ADDRESS_VERSION);
        LogUtil.getInstance().print("AddressDatabaseHelper");
    }

    public static synchronized AddressDatabaseHelper getInstance() {
        if (addressDatabaseHelper == null) {
            addressDatabaseHelper = new AddressDatabaseHelper();
        }
        return addressDatabaseHelper;
    }

    public static void releaseInstance() {
        if (addressDatabaseHelper != null) {
            addressDatabaseHelper = null;
        }
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        LogUtil.getInstance().print("AddressDatabaseHelper onCreate");
        sqLiteDatabase.execSQL(BaseProvider.CREATE_ADDRESSES_SQL);
        sqLiteDatabase.execSQL(BaseProvider.CREATE_HDM_BID_SQL);
        sqLiteDatabase.execSQL(BaseProvider.CREATE_HD_SEEDS_SQL);
        sqLiteDatabase.execSQL(BaseProvider.CREATE_HDM_ADDRESSES_SQL);
        sqLiteDatabase.execSQL(BaseProvider.CREATE_PASSWORD_SEED_SQL);
        sqLiteDatabase.execSQL(BaseProvider.CREATE_ALIASES_SQL);
        sqLiteDatabase.execSQL(BaseProvider.CREATE_HD_ACCOUNT);
        sqLiteDatabase.execSQL(BaseProvider.CREATE_VANITY_ADDRESS_SQL);
        sqLiteDatabase.execSQL(BaseProvider.CREATE_ENTERPRISE_HD_ACCOUNT);
        sqLiteDatabase.execSQL(BaseProvider.CREATE_ENTERPRISE_HDM_ADDRESSES_SQL);
        sqLiteDatabase.execSQL(BaseProvider.CREATE_MULTI_SIGN_SET);

        sqLiteDatabase.execSQL(BaseProvider.CREATE_BLOCKS_SQL);
        sqLiteDatabase.execSQL(BaseProvider.CREATE_BLOCK_NO_INDEX);
        sqLiteDatabase.execSQL(BaseProvider.CREATE_BLOCK_PREV_INDEX);
        sqLiteDatabase.execSQL(BaseProvider.CREATE_TXS_SQL);
        sqLiteDatabase.execSQL(BaseProvider.CREATE_TX_BLOCK_NO_INDEX);
        sqLiteDatabase.execSQL(BaseProvider.CREATE_ADDRESSTXS_SQL);
        sqLiteDatabase.execSQL(BaseProvider.CREATE_INS_SQL);
        sqLiteDatabase.execSQL(BaseProvider.CREATE_IN_PREV_TX_HASH_INDEX);
        sqLiteDatabase.execSQL(BaseProvider.CREATE_OUTS_SQL);
        sqLiteDatabase.execSQL(BaseProvider.CREATE_OUT_ADDRESS_INDEX);
        sqLiteDatabase.execSQL(BaseProvider.CREATE_OUT_HD_ACCOUNT_ID_INDEX);
        sqLiteDatabase.execSQL(BaseProvider.CREATE_PEER_SQL);
        sqLiteDatabase.execSQL(BaseProvider.CREATE_HD_ACCOUNT_ADDRESSES);
        sqLiteDatabase.execSQL(BaseProvider.CREATE_HD_ACCOUNT_ADDRESS_INDEX);
        sqLiteDatabase.execSQL(BaseProvider.CREATE_HD_ACCOUNT_ACCOUNT_ID_AND_PATH_TYPE_INDEX);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1:
                v1ToV2(sqLiteDatabase);
            case 2:
                v2ToV3(sqLiteDatabase);
            case 3:
                v3ToV4(sqLiteDatabase);
            case 4:
                v4Tov5(sqLiteDatabase);
            case 5:
                v5ToV6(sqLiteDatabase);
            case 6:
                v6Tov7(sqLiteDatabase);

        }
    }

    /**
     * v1.3.1
     *
     * @param sqLiteDatabase
     */
    private void v1ToV2(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("alter table hd_seeds add column encrypt_hd_seed text;");
        sqLiteDatabase.execSQL(BaseProvider.CREATE_PASSWORD_SEED_SQL);
        String passwordSeed = SharedPreferenceUtil.getInstance().getString(BaseApplication.getInstance(), com.dasset.wallet.components.constant.Constant.ShreadPreference.FILE_WALLET, Context.MODE_PRIVATE, com.dasset.wallet.components.constant.Constant.ShreadPreference.PASSWORD_SEED, null);
        if (!Utils.isEmpty(passwordSeed)) {
            sqLiteDatabase.execSQL("insert into password_seed (password_seed) values (?) ", new String[]{passwordSeed});
        }
    }

    private void v2ToV3(SQLiteDatabase sqLiteDatabase) {
        // v1.3.2
        sqLiteDatabase.execSQL(BaseProvider.CREATE_ALIASES_SQL);
        sqLiteDatabase.execSQL("alter table hd_seeds add column singular_mode_backup text;");
    }

    /**
     * v1.3.3 ensure v2 & v3 's script executed.
     *
     * @param sqLiteDatabase
     */
    private void v3ToV4(SQLiteDatabase sqLiteDatabase) {
        Cursor cursor = sqLiteDatabase.rawQuery("select count(0) from sqlite_master where name='aliases'", null);
        int cnt = 0;
        if (cursor.moveToNext()) {
            cnt = cursor.getInt(0);
        }
        cursor.close();
        if (cnt == 0) {
            v1ToV2(sqLiteDatabase);
            v2ToV3(sqLiteDatabase);
        }
    }

    /**
     * v1.3.4
     *
     * @param db
     */
    private void v4Tov5(SQLiteDatabase db) {
        db.execSQL(BaseProvider.CREATE_HD_ACCOUNT);
    }

    /**
     * v1.3.5
     *
     * @param db
     */
    private void v5ToV6(SQLiteDatabase db) {
        db.execSQL(BaseProvider.CREATE_VANITY_ADDRESS_SQL);

    }

    /**
     * 1.3.8
     *
     * @param sqLiteDatabase
     */
    private void v6Tov7(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(BaseProvider.CREATE_ENTERPRISE_HD_ACCOUNT);
        sqLiteDatabase.execSQL(BaseProvider.CREATE_ENTERPRISE_HDM_ADDRESSES_SQL);
        sqLiteDatabase.execSQL(BaseProvider.CREATE_MULTI_SIGN_SET);
        // modify encrypt_seed null
        sqLiteDatabase.execSQL("create table if not exists  hd_account2 " +
                                       "( hd_account_id integer not null primary key autoincrement" +
                                       ", encrypt_seed text" +
                                       ", encrypt_mnemonic_seed text" +
                                       ", hd_address text not null" +
                                       ", external_pub text not null" +
                                       ", internal_pub text not null" +
                                       ", is_xrandom integer not null);");
        sqLiteDatabase.execSQL("INSERT INTO hd_account2(hd_account_id,encrypt_seed,encrypt_mnemonic_seed,hd_address,external_pub,internal_pub,is_xrandom) " +
                                       " SELECT hd_account_id,encrypt_seed,encrypt_mnemonic_seed,hd_address,external_pub,internal_pub,is_xrandom FROM hd_account;");
        int oldCnt = 0;
        int newCnt = 0;
        Cursor cursor = sqLiteDatabase.rawQuery("select count(0) cnt from hd_account", null);
        if (cursor.moveToNext()) {
            oldCnt = cursor.getInt(0);
        }
        cursor.close();
        cursor = sqLiteDatabase.rawQuery("select count(0) cnt from hd_account2", null);
        if (cursor.moveToNext()) {
            newCnt = cursor.getInt(0);
        }
        cursor.close();
        if (oldCnt != newCnt) {
            throw new RuntimeException("address db upgrade from 6 to 7 failed. new hd_account_addresses table record count not the same as old one");
        } else {
            sqLiteDatabase.execSQL("DROP TABLE hd_account;");
            sqLiteDatabase.execSQL("ALTER TABLE hd_account2 RENAME TO hd_account;");
        }
    }
}
