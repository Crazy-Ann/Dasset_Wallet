package com.dasset.wallet.core.db.helper;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.dasset.wallet.base.application.BaseApplication;
import com.dasset.wallet.core.contant.Constant;
import com.dasset.wallet.core.db.facade.BaseProvider;

public class TxDataBaseHelper extends SQLiteOpenHelper {

    private static TxDataBaseHelper txDataBaseHelper;

    private TxDataBaseHelper() {
        super(BaseApplication.getInstance(), Constant.DATA_BASE_TX, null, Constant.DATA_BASE_TX_VERSION);
    }

    public static synchronized TxDataBaseHelper getInstance() {
        if (txDataBaseHelper == null) {
            txDataBaseHelper = new TxDataBaseHelper();
        }
        return txDataBaseHelper;
    }

    public static void releaseInstance() {
        if (txDataBaseHelper != null) {
            txDataBaseHelper = null;
        }
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        createBlocksTable(sqLiteDatabase);
        createTxsTable(sqLiteDatabase);
        createAddressTxsTable(sqLiteDatabase);
        createInsTable(sqLiteDatabase);
        createOutsTable(sqLiteDatabase);
        createPeersTable(sqLiteDatabase);
        createHDAccountAddress(sqLiteDatabase);
        sqLiteDatabase.execSQL(BaseProvider.CREATE_HD_ACCOUNT_ACCOUNT_ID_AND_PATH_TYPE_INDEX);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1:
                v1Tov2(sqLiteDatabase);
            case 2:
                v2Tov3(sqLiteDatabase);

        }
    }


    private void createBlocksTable(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(BaseProvider.CREATE_BLOCKS_SQL);
        sqLiteDatabase.execSQL(BaseProvider.CREATE_BLOCK_NO_INDEX);
        sqLiteDatabase.execSQL(BaseProvider.CREATE_BLOCK_PREV_INDEX);
    }

    private void createTxsTable(SQLiteDatabase db) {
        db.execSQL(BaseProvider.CREATE_TXS_SQL);
        db.execSQL(BaseProvider.CREATE_TX_BLOCK_NO_INDEX);
    }

    private void createAddressTxsTable(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(BaseProvider.CREATE_ADDRESSTXS_SQL);
    }

    private void createInsTable(SQLiteDatabase db) {
        db.execSQL(BaseProvider.CREATE_INS_SQL);
        db.execSQL(BaseProvider.CREATE_IN_PREV_TX_HASH_INDEX);
    }

    private void createOutsTable(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(BaseProvider.CREATE_OUTS_SQL);
        sqLiteDatabase.execSQL(BaseProvider.CREATE_OUT_ADDRESS_INDEX);
        sqLiteDatabase.execSQL(BaseProvider.CREATE_OUT_HD_ACCOUNT_ID_INDEX);
    }

    private void createPeersTable(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(BaseProvider.CREATE_PEER_SQL);
    }

    private void createHDAccountAddress(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(BaseProvider.CREATE_HD_ACCOUNT_ADDRESSES);
        sqLiteDatabase.execSQL(BaseProvider.CREATE_HD_ACCOUNT_ADDRESS_INDEX);
    }

    /**
     * v1.34
     *
     * @param sqLiteDatabase
     */
    private void v1Tov2(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(BaseProvider.ADD_HD_ACCOUNT_ID_FOR_OUTS);
        createHDAccountAddress(sqLiteDatabase);
    }

    /**
     * v1.37
     *
     * @param sqLiteDatabase
     */
    private void v2Tov3(SQLiteDatabase sqLiteDatabase) {
        // add hd_account_id to hd_account_addresses
        Cursor cursor = sqLiteDatabase.rawQuery("select count(0) from hd_account_addresses", null);
        int cnt = 0;
        if (cursor.moveToNext()) {
            cnt = cursor.getInt(0);
        }
        cursor.close();

        sqLiteDatabase.execSQL("create table if not exists " +
                                       "hd_account_addresses2 " +
                                       "(hd_account_id integer not null" +
                                       ", path_type integer not null" +
                                       ", address_index integer not null" +
                                       ", is_issued integer not null" +
                                       ", address text not null" +
                                       ", pub text not null" +
                                       ", is_synced integer not null" +
                                       ", primary key (address));");
        if (cnt > 0) {
            sqLiteDatabase.execSQL("ALTER TABLE hd_account_addresses ADD COLUMN hd_account_id integer");
            int hdAccountId;
            cursor = AddressDatabaseHelper.getInstance().getReadableDatabase().rawQuery("select hd_account_id from hd_account", null);
            if (cursor.moveToNext()) {
                hdAccountId = cursor.getInt(0);
                if (cursor.moveToNext()) {
                    cursor.close();
                    throw new RuntimeException("tx db upgrade from 2 to 3 failed. more than one record in hd_account");
                } else {
                    cursor.close();
                }
            } else {
                cursor.close();
                throw new RuntimeException("tx db upgrade from 2 to 3 failed. no record in hd_account");
            }

            sqLiteDatabase.execSQL("update hd_account_addresses set hd_account_id=?", new String[]{Integer.toString(hdAccountId)});
            sqLiteDatabase.execSQL("INSERT INTO hd_account_addresses2(hd_account_id,path_type,address_index,is_issued,address,pub,is_synced) " +
                                           "SELECT hd_account_id,path_type,address_index,is_issued,address,pub,is_synced FROM hd_account_addresses;");
        }
        int oldCnt = 0;
        int newCnt = 0;
        cursor = sqLiteDatabase.rawQuery("select count(0) cnt from hd_account_addresses", null);
        if (cursor.moveToNext()) {
            oldCnt = cursor.getInt(0);
        }
        cursor.close();
        cursor = sqLiteDatabase.rawQuery("select count(0) cnt from hd_account_addresses2", null);
        if (cursor.moveToNext()) {
            newCnt = cursor.getInt(0);
        }
        cursor.close();
        if (oldCnt != newCnt) {
            throw new RuntimeException("tx db upgrade from 2 to 3 failed. new hd_account_addresses table record count not the same as old one");
        } else {
            sqLiteDatabase.execSQL("DROP TABLE hd_account_addresses;");
            sqLiteDatabase.execSQL("ALTER TABLE hd_account_addresses2 RENAME TO hd_account_addresses;");
        }

        sqLiteDatabase.execSQL(BaseProvider.CREATE_OUT_HD_ACCOUNT_ID_INDEX);
        sqLiteDatabase.execSQL(BaseProvider.CREATE_HD_ACCOUNT_ACCOUNT_ID_AND_PATH_TYPE_INDEX);
    }
}
