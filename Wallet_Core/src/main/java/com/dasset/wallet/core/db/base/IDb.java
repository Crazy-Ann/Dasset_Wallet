package com.dasset.wallet.core.db.base;

import com.google.common.base.Function;

public interface IDb {

    void beginTransaction();

    void endTransaction();

    void close();

    void execUpdate(String sql, String[] params);

    void execQueryOneRecord(String sql, String[] params, Function<ICursor, Void> func);

    void execQueryLoop(String sql, String[] params, Function<ICursor, Void> func);
}
