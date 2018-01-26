package com.dasset.wallet.core.db.facade.implement;

import android.database.sqlite.SQLiteOpenHelper;

import com.dasset.wallet.core.db.base.IDb;
import com.dasset.wallet.core.db.facade.wrapper.BlockProviderWrapper;
import com.dasset.wallet.core.db.helper.TxDataBaseHelper;

public class BlockProvider extends BlockProviderWrapper {

    private static BlockProvider blockProvider;
    private SQLiteOpenHelper sqLiteOpenHelper;

    private BlockProvider(SQLiteOpenHelper sqLiteOpenHelper) {
        this.sqLiteOpenHelper = sqLiteOpenHelper;
    }

    public static synchronized BlockProvider getInstance() {
        if (blockProvider == null) {
            blockProvider = new BlockProvider(TxDataBaseHelper.getInstance());
        }
        return blockProvider;
    }

    public static void releaseInstance() {
        if (blockProvider != null) {
            blockProvider = null;
        }
    }

    @Override
    public IDb getReadDb() {
        return new Db(this.sqLiteOpenHelper.getReadableDatabase());
    }

    @Override
    public IDb getWriteDb() {
        return new Db(this.sqLiteOpenHelper.getWritableDatabase());
    }
}
