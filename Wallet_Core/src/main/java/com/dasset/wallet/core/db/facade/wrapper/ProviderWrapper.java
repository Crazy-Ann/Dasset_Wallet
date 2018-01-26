package com.dasset.wallet.core.db.facade.wrapper;

import com.google.common.base.Function;
import com.dasset.wallet.core.db.base.ICursor;
import com.dasset.wallet.core.db.base.IDb;
import com.dasset.wallet.core.db.base.IProvider;

public abstract class ProviderWrapper implements IProvider {

    @Override
    public void execUpdate(String sql, String[] parameters) {
        this.getWriteDb().execUpdate(sql, parameters);
    }

    @Override
    public void execQueryOneRecord(String sql, String[] parameters, Function<ICursor, Void> function) {
        this.getReadDb().execQueryOneRecord(sql, parameters, function);
    }

    @Override
    public void execQueryLoop(String sql, String[] parameters, Function<ICursor, Void> function) {
        this.getReadDb().execQueryLoop(sql, parameters, function);
    }

    @Override
    public void execUpdate(IDb db, String sql, String[] parameters) {
        db.execUpdate(sql, parameters);
    }

    @Override
    public void execQueryOneRecord(IDb db, String sql, String[] parameters, Function<ICursor, Void> function) {
        db.execQueryOneRecord(sql, parameters, function);
    }

    @Override
    public void execQueryLoop(IDb db, String sql, String[] parameters, Function<ICursor, Void> function) {
        db.execQueryLoop(sql, parameters, function);
    }
}
