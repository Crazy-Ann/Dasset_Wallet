package com.dasset.wallet.core.db.facade.implement;

import android.database.sqlite.SQLiteOpenHelper;

import com.dasset.wallet.core.db.base.IDb;
import com.dasset.wallet.core.db.facade.wrapper.PeerProviderWrapper;
import com.dasset.wallet.core.db.helper.TxDataBaseHelper;

public class PeerProvider extends PeerProviderWrapper {

    private static PeerProvider blockProvider;
    private SQLiteOpenHelper sqLiteOpenHelper;

    private PeerProvider(SQLiteOpenHelper sqLiteOpenHelper) {
        this.sqLiteOpenHelper = sqLiteOpenHelper;
    }

    public static synchronized PeerProvider getInstance() {
        if (blockProvider == null) {
            blockProvider = new PeerProvider(TxDataBaseHelper.getInstance());
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
}
