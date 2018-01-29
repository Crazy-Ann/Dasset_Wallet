package com.dasset.wallet.core.db.facade.wrapper;

import com.google.common.base.Function;
import com.dasset.wallet.core.db.base.ICursor;
import com.dasset.wallet.core.db.base.IDb;
import com.dasset.wallet.core.db.base.IProvider;

public abstract class ProviderWrapper implements IProvider {

    @Override
    public void execUpdate(String sql, String[] parameters) {
        this.getWritableDatabase().execUpdate(sql, parameters);
    }

    @Override
    public void execQueryOneRecord(String sql, String[] parameters, Function<ICursor, Void> function) {
        this.getReadableDatabase().execQueryOneRecord(sql, parameters, function);
    }

    @Override
    public void execQueryLoop(String sql, String[] parameters, Function<ICursor, Void> function) {
        this.getReadableDatabase().execQueryLoop(sql, parameters, function);
    }

    @Override
    public void execUpdate(IDb iDb, String sql, String[] parameters) {
        iDb.execUpdate(sql, parameters);
    }

    @Override
    public void execQueryOneRecord(IDb iDb, String sql, String[] parameters, Function<ICursor, Void> function) {
        iDb.execQueryOneRecord(sql, parameters, function);
    }

    @Override
    public void execQueryLoop(IDb iDb, String sql, String[] parameters, Function<ICursor, Void> function) {
        iDb.execQueryLoop(sql, parameters, function);
    }
}
