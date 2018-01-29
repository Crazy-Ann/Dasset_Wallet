package com.dasset.wallet.core.db.base;

import com.google.common.base.Function;

public interface IProvider {

    IDb getReadableDatabase();

    IDb getWritableDatabase();

    void execUpdate(String sql, String[] parameters);

    void execQueryOneRecord(String sql, String[] parameters, Function<ICursor, Void> function);

    void execQueryLoop(String sql, String[] parameters, Function<ICursor, Void> function);

    void execUpdate(IDb iDb, String sql, String[] parameters);

    void execQueryOneRecord(IDb iDb, String sql, String[] parameters, Function<ICursor, Void> function);

    void execQueryLoop(IDb iDb, String sql, String[] parameters, Function<ICursor, Void> function);
}
