/*
 * Copyright 2014 http://Bither.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dasset.wallet.core.db.facade.wrapper;

import com.dasset.wallet.core.AddressTx;
import com.dasset.wallet.core.In;
import com.dasset.wallet.core.Out;
import com.dasset.wallet.core.Tx;
import com.dasset.wallet.core.contant.BitherjSettings;
import com.dasset.wallet.core.contant.db.InsColumns;
import com.dasset.wallet.core.contant.db.OutsColumns;
import com.dasset.wallet.core.contant.db.Tables;
import com.dasset.wallet.core.contant.db.TxsColumns;
import com.dasset.wallet.core.db.base.ICursor;
import com.dasset.wallet.core.db.base.IDb;
import com.dasset.wallet.core.db.facade.BaseProvider;
import com.dasset.wallet.core.db.facade.ITxProvider;
import com.dasset.wallet.core.exception.AddressFormatException;
import com.dasset.wallet.core.utils.Base58;
import com.dasset.wallet.core.utils.Sha256Hash;
import com.dasset.wallet.core.utils.Utils;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Nullable;

public abstract class TxProviderWrapper extends ProviderWrapper implements ITxProvider {

    @Override
    public List<Tx> getTxAndDetailByAddress(String address) {
        final List<Tx> txes = Lists.newArrayList();
        final HashMap<Sha256Hash, Tx> txHashMap = Maps.newHashMap();
        String sql = "select b.* from addresses_txs a, txs b  where a.tx_hash=b.tx_hash and a.address=? order by ifnull(b.block_no,4294967295) desc";
        IDb db = this.getReadableDatabase();
        this.execQueryLoop(db, sql, new String[]{address}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                Tx tx = applyCursor(iCursor);
                tx.setIns(new ArrayList<In>());
                tx.setOuts(new ArrayList<Out>());
                txes.add(tx);
                txHashMap.put(new Sha256Hash(tx.getTxHash()), tx);
                return null;
            }
        });
        addInForTxDetail(db, address, txHashMap);
        addOutForTxDetail(db, address, txHashMap);

        return txes;
    }

    private void addInForTxDetail(IDb db, String address, final HashMap<Sha256Hash, Tx> txDict) {
        String sql = "select b.* from addresses_txs a, ins b where a.tx_hash=b.tx_hash and a.address=? order by b.tx_hash ,b.in_sn";
        this.execQueryLoop(db, sql, new String[]{address}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                In in = applyCursorIn(iCursor);
                Tx tx = txDict.get(new Sha256Hash(in.getTxHash()));
                if (tx != null) {
                    tx.getIns().add(in);
                }
                return null;
            }
        });
    }

    private void addOutForTxDetail(IDb db, String address, final HashMap<Sha256Hash, Tx> txHashMap) {
        String sql = "select b.* from addresses_txs a, outs b where a.tx_hash=b.tx_hash and a.address=? order by b.tx_hash,b.out_sn";
        this.execQueryLoop(db, sql, new String[]{address}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                Out out = applyCursorOut(iCursor);
                Tx tx = txHashMap.get(new Sha256Hash(out.getTxHash()));
                if (tx != null) {
                    tx.getOuts().add(out);
                }
                return null;
            }
        });
    }

    @Override
    public List<Tx> getTxAndDetailByAddress(String address, int page) {
        final List<Tx> txes = Lists.newArrayList();
        final HashMap<Sha256Hash, Tx> txHashMap = Maps.newHashMap();
        IDb db = this.getReadableDatabase();
        String sql = "select b.* from addresses_txs a, txs b where a.tx_hash=b.tx_hash and a.address=? order by ifnull(b.block_no,4294967295) desc limit ?,? ";
        final StringBuilder stringBuilder = new StringBuilder();
        this.execQueryLoop(db, sql, new String[]{address
                                   , Integer.toString((page - 1) * BitherjSettings.TX_PAGE_SIZE)
                                   , Integer.toString(BitherjSettings.TX_PAGE_SIZE)}
                , new Function<ICursor, Void>() {
                    @Nullable
                    @Override
                    public Void apply(@Nullable ICursor iCursor) {
                        Tx tx = applyCursor(iCursor);
                        tx.setIns(new ArrayList<In>());
                        tx.setOuts(new ArrayList<Out>());
                        txes.add(tx);
                        txHashMap.put(new Sha256Hash(tx.getTxHash()), tx);
                        stringBuilder.append("'").append(Base58.encode(tx.getTxHash())).append("'").append(",");
                        return null;
                    }
                });

        if (stringBuilder.length() > 1) {
            String txs = stringBuilder.substring(0, stringBuilder.length() - 1);
            sql = Utils.format("select b.* from ins b where b.tx_hash in (%s) order by b.tx_hash ,b.in_sn", txs);
            this.execQueryLoop(db, sql, null, new Function<ICursor, Void>() {
                @Nullable
                @Override
                public Void apply(@Nullable ICursor iCursor) {
                    In in = applyCursorIn(iCursor);
                    Tx tx = txHashMap.get(new Sha256Hash(in.getTxHash()));
                    if (tx != null) {
                        tx.getIns().add(in);
                    }
                    return null;
                }
            });
            sql = Utils.format("select b.* from outs b where b.tx_hash in (%s) order by b.tx_hash,b.out_sn", txs);
            this.execQueryLoop(db, sql, null, new Function<ICursor, Void>() {
                @Nullable
                @Override
                public Void apply(@Nullable ICursor iCursor) {
                    Out out = applyCursorOut(iCursor);
                    Tx tx = txHashMap.get(new Sha256Hash(out.getTxHash()));
                    if (tx != null) {
                        tx.getOuts().add(out);
                    }
                    return null;
                }
            });
        }
        return txes;
    }

    @Override
    public List<Tx> getPublishedTxes() {
        final List<Tx> txes = Lists.newArrayList();
        final HashMap<Sha256Hash, Tx> txHashMap = Maps.newHashMap();
        IDb db = this.getReadableDatabase();
        String sql = "select * from txs where block_no is null";
        this.execQueryLoop(db, sql, null, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                Tx tx = applyCursor(iCursor);
                tx.setIns(new ArrayList<In>());
                tx.setOuts(new ArrayList<Out>());
                txes.add(tx);
                txHashMap.put(new Sha256Hash(tx.getTxHash()), tx);
                return null;
            }
        });

        sql = "select b.* from txs a, ins b  where a.tx_hash=b.tx_hash  and a.block_no is null order by b.tx_hash ,b.in_sn";
        this.execQueryLoop(db, sql, null, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                In in = applyCursorIn(iCursor);
                Tx tx = txHashMap.get(new Sha256Hash(in.getTxHash()));
                tx.getIns().add(in);
                return null;
            }
        });
        sql = "select b.* from txs a, outs b where a.tx_hash=b.tx_hash and a.block_no is null order by b.tx_hash,b.out_sn";
        this.execQueryLoop(db, sql, null, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                Out out = applyCursorOut(iCursor);
                Tx tx = txHashMap.get(new Sha256Hash(out.getTxHash()));
                tx.getOuts().add(out);
                return null;
            }
        });
        return txes;
    }

    @Override
    public Tx getTxDetailByTxHash(byte[] txHash) {
        final Tx[] txes = {null};
        final boolean[] txExists = {false};
        String sql = "select * from txs where tx_hash=?";
        IDb db = this.getReadableDatabase();
        this.execQueryOneRecord(db, sql, new String[]{Base58.encode(txHash)}, new Function<ICursor, Void>() {
            @Nullable

            @Override
            public Void apply(@Nullable ICursor iCursor) {
                txes[0] = applyCursor(iCursor);
                txExists[0] = true;
                return null;
            }
        });
        if (txExists[0]) {
            addInsAndOuts(db, txes[0]);
        }
        return txes[0];
    }

    @Override
    public long sentFromAddress(byte[] txHash, String address) {
        String sql = "select  sum(o.out_value) out_value from ins i,outs o where i.tx_hash=? and o.tx_hash=i.prev_tx_hash and i.prev_out_sn=o.out_sn and o.out_address=?";
        final long[] sum = {0};
        this.execQueryOneRecord(sql, new String[]{Base58.encode(txHash), address}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                int idColumn = iCursor.getColumnIndex(OutsColumns.OUT_VALUE);
                if (idColumn != -1) {
                    sum[0] = iCursor.getLong(idColumn);
                }
                return null;
            }
        });
        return sum[0];
    }


    @Override
    public boolean isExist(byte[] txHash) {
        final boolean[] result = {false};
        String sql = "select count(0) from txs where tx_hash=?";
        this.execQueryOneRecord(sql, new String[]{Base58.encode(txHash)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                result[0] = iCursor.getInt(0) > 0;
                return null;
            }
        });
        return result[0];
    }

    @Override
    public void add(Tx tx) {
        IDb db = this.getWritableDatabase();
        db.beginTransaction();
        addTxToDb(db, tx);
        db.endTransaction();
    }

    @Override
    public void addTxs(List<Tx> txes) {
        if (txes.size() > 0) {
            IDb db = this.getWritableDatabase();
            db.beginTransaction();
            for (Tx txItem : txes) {
                addTxToDb(db, txItem);
            }
            db.endTransaction();
        }
    }

    private void addTxToDb(IDb db, Tx tx) {
        this.insertTx(db, tx);
        List<AddressTx> addressTxes = Lists.newArrayList();
        List<AddressTx> tempaddressTxes = insertIn(db, tx);
        if (tempaddressTxes != null && tempaddressTxes.size() > 0) {
            addressTxes.addAll(tempaddressTxes);
        }
        tempaddressTxes = insertOut(db, tx);
        if (tempaddressTxes != null && tempaddressTxes.size() > 0) {
            addressTxes.addAll(tempaddressTxes);
        }
        String sql = "insert or ignore into addresses_txs(address, tx_hash) values(?,?)";
        for (AddressTx addressTx : addressTxes) {
            this.execUpdate(db, sql, new String[]{addressTx.getAddress(), addressTx.getTxHash()});
        }
    }

    @Override
    public void remove(byte[] txHash) {
        List<String> txHashes = Lists.newArrayList();
        List<String> needRemoveTxHashes = Lists.newArrayList();
        txHashes.add(Base58.encode(txHash));
        while (txHashes.size() > 0) {
            String hash = txHashes.get(0);
            txHashes.remove(0);
            needRemoveTxHashes.add(hash);
            List<String> temp = getRelayTx(hash);
            txHashes.addAll(temp);
        }
        IDb db = this.getWritableDatabase();
        db.beginTransaction();
        for (String hash : needRemoveTxHashes) {
            removeSingleTx(db, hash);
        }
        db.endTransaction();
    }

    private void removeSingleTx(IDb db, String tx) {
        String deleteTx = "delete from txs where tx_hash=?";
        String deleteIn = "delete from ins where tx_hash=?";
        String deleteOut = "delete from outs where tx_hash=?";
        String deleteAddressesTx = "delete from addresses_txs where tx_hash=?";
        String inSql = "select prev_tx_hash,prev_out_sn from ins where tx_hash=?";
        String existOtherIn = "select count(0) cnt from ins where prev_tx_hash=? and prev_out_sn=?";
        String updatePrevOut = "update outs set out_status=? where tx_hash=? and out_sn=?";
        final List<Object[]> objects = Lists.newArrayList();
        this.execQueryLoop(db, inSql, new String[]{tx}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                int idColumn = iCursor.getColumnIndex(InsColumns.PREV_TX_HASH);
                String prevTxHash = null;
                int prevOutSn = 0;
                if (idColumn != -1) {
                    prevTxHash = iCursor.getString(idColumn);
                }
                idColumn = iCursor.getColumnIndex(InsColumns.PREV_OUT_SN);
                if (idColumn != -1) {
                    prevOutSn = iCursor.getInt(idColumn);
                }
                objects.add(new Object[]{prevTxHash, prevOutSn});
                return null;
            }
        });
        this.execUpdate(db, deleteAddressesTx, new String[]{tx});
        this.execUpdate(db, deleteOut, new String[]{tx});
        this.execUpdate(db, deleteIn, new String[]{tx});
        this.execUpdate(db, deleteTx, new String[]{tx});
        for (Object[] array : objects) {
            final boolean[] isExist = {false};
            this.execQueryLoop(db, existOtherIn, new String[]{array[0].toString(), array[1].toString()}, new Function<ICursor, Void>() {
                @Nullable
                @Override
                public Void apply(@Nullable ICursor iCursor) {
                    assert iCursor != null;
                    if (iCursor.getInt(0) == 0) {
                        isExist[0] = true;
                    }
                    return null;
                }
            });
            if (isExist[0]) {
                this.execUpdate(db, updatePrevOut, new String[]{"0", array[0].toString(), array[1].toString()});
            }
        }
    }

    private List<String> getRelayTx(String txHash) {
        final List<String> relayTxHashes = Lists.newArrayList();
        String relayTxSql = "select distinct tx_hash from ins where prev_tx_hash=?";
        this.execQueryLoop(relayTxSql, new String[]{txHash}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                relayTxHashes.add(iCursor.getString(0));
                return null;
            }
        });
        return relayTxHashes;
    }

    @Override
    public boolean isAddressContainsTx(String address, Tx tx) {
        boolean result = false;
        String sql = "select count(0) from ins a, txs b where a.tx_hash=b.tx_hash and b.block_no is not null and a.prev_tx_hash=? and a.prev_out_sn=?";
        IDb db = this.getReadableDatabase();
        for (In in : tx.getIns()) {
            final boolean[] isDoubleSpent = {false};
            this.execQueryOneRecord(db, sql, new String[]{Base58.encode(in.getPrevTxHash()), Integer.toString(in.getPrevOutSn())}, new Function<ICursor, Void>() {
                @Nullable
                @Override
                public Void apply(@Nullable ICursor iCursor) {
                    assert iCursor != null;
                    isDoubleSpent[0] = iCursor.getInt(0) > 0;
                    return null;
                }
            });
            if (isDoubleSpent[0]) {
                result = false;
            }
        }
        sql = "select count(0) from addresses_txs where tx_hash=? and address=?";
        final boolean[] isRecordInRel = {false};
        this.execQueryOneRecord(db, sql, new String[]{Base58.encode(tx.getTxHash()), address}
                , new Function<ICursor, Void>() {
                    @Nullable
                    @Override
                    public Void apply(@Nullable ICursor iCursor) {
                        assert iCursor != null;
                        isRecordInRel[0] = iCursor.getInt(0) > 0;
                        return null;
                    }
                });
        if (isRecordInRel[0]) {
            result = true;
        }
        sql = "select count(0) from outs where tx_hash=? and out_sn=? and out_address=?";
        for (In in : tx.getIns()) {
            final int[] cnt = {0};
            this.execQueryOneRecord(db, sql, new String[]{Base58.encode(in.getPrevTxHash())
                    , Integer.toString(in.getPrevOutSn()), address}, new Function<ICursor, Void>() {
                @Nullable
                @Override
                public Void apply(@Nullable ICursor iCursor) {
                    assert iCursor != null;
                    cnt[0] = iCursor.getInt(0);
                    return null;
                }
            });
            if (cnt[0] > 0) {
                result = true;
            }
        }
        return result;
    }

    @Override
    public boolean isTxDoubleSpendWithConfirmedTx(Tx tx) {
        String sql = "select count(0) from ins a, txs b where a.tx_hash=b.tx_hash and b.block_no is not null and a.prev_tx_hash=? and a.prev_out_sn=?";
        IDb db = this.getReadableDatabase();
        for (In inItem : tx.getIns()) {
            final int[] cnt = {0};
            this.execQueryOneRecord(db, sql, new String[]{Base58.encode(inItem.getPrevTxHash()), Integer.toString(inItem.getPrevOutSn())}, new Function<ICursor, Void>() {
                @Nullable
                @Override
                public Void apply(@Nullable ICursor iCursor) {
                    assert iCursor != null;
                    cnt[0] = iCursor.getInt(0);
                    return null;
                }
            });
            if (cnt[0] > 0) {
                return true;
            }
        }
        return false;
    }

    public List<String> getInAddresses(Tx tx) {
        final List<String> result = Lists.newArrayList();
        String sql = "select out_address from outs where tx_hash=? and out_sn=?";
        IDb db = this.getReadableDatabase();
        for (In in : tx.getIns()) {
            this.execQueryOneRecord(db, sql, new String[]{Base58.encode(in.getPrevTxHash())
                    , Integer.toString(in.getPrevOutSn())}, new Function<ICursor, Void>() {
                @Nullable
                @Override
                public Void apply(@Nullable ICursor iCursor) {
                    assert iCursor != null;
                    if (!iCursor.isNull(0)) {
                        result.add(iCursor.getString(0));
                    }
                    return null;
                }
            });
        }
        return result;
    }

    @Override
    public void confirmTx(int blockNo, List<byte[]> txHashes) {
        if (blockNo == Tx.TX_UNCONFIRMED || txHashes == null) {
            return;
        }
        String updateBlockNoSql = "update txs set block_no=? where tx_hash=?";
        String existSql = "select count(0) from txs where block_no=? and tx_hash=?";
        String doubleSpendSql = "select a.tx_hash from ins a, ins b where a.prev_tx_hash=b.prev_tx_hash and a.prev_out_sn=b.prev_out_sn and a.tx_hash<>b.tx_hash and b.tx_hash=?";
        String blockTimeSql = "select block_time from blocks where block_no=?";
        String updateTxTimeThatMoreThanBlockTime = "update txs set tx_time=? where block_no=? and tx_time>?";
        IDb db = this.getWritableDatabase();
        db.beginTransaction();
        for (byte[] txHash : txHashes) {
            final int[] cnt = {0};
            this.execQueryOneRecord(db, existSql, new String[]{Integer.toString(blockNo), Base58.encode(txHash)}, new Function<ICursor, Void>() {
                @Nullable
                @Override
                public Void apply(@Nullable ICursor iCursor) {
                    assert iCursor != null;
                    cnt[0] = iCursor.getInt(0);
                    return null;
                }
            });
            if (cnt[0] > 0) {
                continue;
            }
            this.execUpdate(db, updateBlockNoSql, new String[]{Integer.toString(blockNo), Base58.encode(txHash)});
            final List<String> tempTxHashes = Lists.newArrayList();
            this.execQueryLoop(db, doubleSpendSql, new String[]{Base58.encode(txHash)}, new Function<ICursor, Void>() {
                @Nullable
                @Override
                public Void apply(@Nullable ICursor iCursor) {
                    assert iCursor != null;
                    int idColumn = iCursor.getColumnIndex("tx_hash");
                    if (idColumn != -1) {
                        tempTxHashes.add(iCursor.getString(idColumn));
                    }
                    return null;
                }
            });
            List<String> needRemoveTxHashes = Lists.newArrayList();
            while (tempTxHashes.size() > 0) {
                String hash = tempTxHashes.get(0);
                tempTxHashes.remove(0);
                needRemoveTxHashes.add(hash);
                List<String> temp = getRelayTx(hash);
                tempTxHashes.addAll(temp);
            }
            for (String hash : needRemoveTxHashes) {
                removeSingleTx(db, hash);
            }
        }
        final int[] blockTime = {-1};
        this.execQueryOneRecord(db, blockTimeSql, new String[]{Integer.toString(blockNo)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                int idColumn = iCursor.getColumnIndex("block_time");
                if (idColumn != -1) {
                    blockTime[0] = iCursor.getInt(idColumn);
                }
                return null;
            }
        });
        if (blockTime[0] > 0) {
            this.execUpdate(db, updateTxTimeThatMoreThanBlockTime, new String[]{Integer.toString(blockTime[0]), Integer.toString(blockNo), Integer.toString(blockTime[0])});
        }
        db.endTransaction();
    }

    @Override
    public void unConfirmTxByBlockNo(int blockNo) {
        String sql = "update txs set block_no=null where block_no>=?";
        this.execUpdate(sql, new String[]{Integer.toString(blockNo)});
    }

    @Override
    public List<Tx> getUnspendTxWithAddress(String address) {
        String unspendOutSql = "select a.*,b.tx_ver,b.tx_locktime,b.tx_time,b.block_no,b.source,ifnull(b.block_no,0)*a.out_value coin_depth from outs a,txs b where a.tx_hash=b.tx_hash and a.out_address=? and a.out_status=?";
        final List<Tx> txes = Lists.newArrayList();
        this.execQueryLoop(unspendOutSql, new String[]{address, Integer.toString(Out.OutStatus.unspent.getValue())}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                int idColumn = iCursor.getColumnIndex("coin_depth");
                Tx tx = applyCursor(iCursor);
                Out out = applyCursorOut(iCursor);
                if (idColumn != -1) {
                    out.setCoinDepth(iCursor.getLong(idColumn));
                }
                out.setTx(tx);
                tx.setOuts(new ArrayList<Out>());
                tx.getOuts().add(out);
                txes.add(tx);
                return null;
            }
        });
        return txes;
    }

    @Override
    public long getConfirmedBalanceWithAddress(String address) {
        final long[] sum = {0};
        String unspendOutSql = "select ifnull(sum(a.out_value),0) sum from outs a,txs b where a.tx_hash=b.tx_hash and a.out_address=? and a.out_status=? and b.block_no is not null";
        this.execQueryOneRecord(unspendOutSql, new String[]{address, Integer.toString(Out.OutStatus.unspent.getValue())}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                int idColumn = iCursor.getColumnIndex("sum");
                if (idColumn != -1) {
                    sum[0] = iCursor.getLong(idColumn);
                }
                return null;
            }
        });
        return sum[0];
    }

    public List<Tx> getUnconfirmedTxWithAddress(String address) {
        final List<Tx> txes = Lists.newArrayList();
        final HashMap<Sha256Hash, Tx> txHashMap = Maps.newHashMap();
        IDb db = this.getReadableDatabase();
        String sql = "select b.* from addresses_txs a, txs b where a.tx_hash=b.tx_hash and a.address=? and b.block_no is null order by b.block_no desc";
        this.execQueryLoop(db, sql, new String[]{address}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                Tx tx = applyCursor(iCursor);
                tx.setIns(new ArrayList<In>());
                tx.setOuts(new ArrayList<Out>());
                txes.add(tx);
                txHashMap.put(new Sha256Hash(tx.getTxHash()), tx);
                return null;
            }
        });
        sql = "select b.tx_hash,b.in_sn,b.prev_tx_hash,b.prev_out_sn from addresses_txs a, ins b, txs c where a.tx_hash=b.tx_hash and b.tx_hash=c.tx_hash and c.block_no is null and a.address=? order by b.tx_hash ,b.in_sn";
        this.execQueryLoop(db, sql, new String[]{address}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                In in = applyCursorIn(iCursor);
                Tx tx = txHashMap.get(new Sha256Hash(in.getTxHash()));
                if (tx != null) {
                    tx.getIns().add(in);
                }
                return null;
            }
        });

        sql = "select b.tx_hash,b.out_sn,b.out_value,b.out_address from addresses_txs a, outs b, txs c where a.tx_hash=b.tx_hash and b.tx_hash=c.tx_hash and c.block_no is null and a.address=? order by b.tx_hash,b.out_sn";
        this.execQueryLoop(db, sql, new String[]{address}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                Out out = applyCursorOut(iCursor);
                Tx tx = txHashMap.get(new Sha256Hash(out.getTxHash()));
                if (tx != null) {
                    tx.getOuts().add(out);
                }
                return null;
            }
        });
        return txes;
    }

    @Override
    public int txCount(String address) {
        final int[] result = {0};
        String sql = "select count(0) cnt from addresses_txs where address=?";
        this.execQueryOneRecord(sql, new String[]{address}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                int idColumn = iCursor.getColumnIndex("cnt");
                if (idColumn != -1) {
                    result[0] = iCursor.getInt(idColumn);
                }
                return null;
            }
        });
        return result[0];
    }

    @Override
    public long totalReceive(String address) {
        final long[] result = {0};
        String sql = "select sum(aa.receive-ifnull(bb.send,0)) sum from(select a.tx_hash,sum(a.out_value) receive from outs a where a.out_address=? group by a.tx_hash) aa LEFT OUTER JOIN (select b.tx_hash,sum(a.out_value) send from outs a, ins b where a.tx_hash=b.prev_tx_hash and a.out_sn=b.prev_out_sn and a.out_address=? group by b.tx_hash) bb on aa.tx_hash=bb.tx_hash where aa.receive>ifnull(bb.send, 0)";
        this.execQueryOneRecord(sql, new String[]{address, address}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                result[0] = iCursor.getLong(0);
                return null;
            }
        });
        return result[0];
    }

    @Override
    public void txSentBySelfHasSaw(byte[] txHash) {
        String sql = "update txs set source=source+1 where tx_hash=? and source>=1";
        this.execUpdate(sql, new String[]{Base58.encode(txHash)});
    }

    @Override
    public List<Out> getOuts() {
        final List<Out> outs = Lists.newArrayList();
        String sql = "select * from outs ";
        this.execQueryLoop(sql, null, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                outs.add(applyCursorOut(iCursor));
                return null;
            }
        });
        return outs;
    }

    public List<Tx> getRecentlyTxsByAddress(String address, int greateThanBlockNo, int limit) {
        final List<Tx> txes = Lists.newArrayList();
        String sql = Utils.format("select b.* from addresses_txs a, txs b where a.tx_hash=b.tx_hash and a.address='%s' and ((b.block_no is null) or (b.block_no is not null and b.block_no>%d)) order by ifnull(b.block_no,4294967295) desc, b.tx_time desc limit %d", address, greateThanBlockNo, limit);
        IDb db = this.getReadableDatabase();
        this.execQueryLoop(db, sql, null, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                Tx tx = applyCursor(iCursor);
                txes.add(tx);
                return null;
            }
        });
        for (Tx tx : txes) {
            addInsAndOuts(db, tx);
        }
        return txes;
    }

    @Override
    public void clearAllTx() {
        IDb db = this.getWritableDatabase();
        db.beginTransaction();
        this.execUpdate(db, "drop table " + Tables.TXS + ";", null);
        this.execUpdate(db, "drop table " + Tables.OUTS + ";", null);
        this.execUpdate(db, "drop table " + Tables.INS + ";", null);
        this.execUpdate(db, "drop table " + Tables.ADDRESSES_TXS + ";", null);
        this.execUpdate(db, "drop table " + Tables.PEERS + ";", null);
        this.execUpdate(db, BaseProvider.CREATE_TXS_SQL, null);
        this.execUpdate(db, BaseProvider.CREATE_TX_BLOCK_NO_INDEX, null);
        this.execUpdate(db, BaseProvider.CREATE_OUTS_SQL, null);
        this.execUpdate(db, BaseProvider.CREATE_OUT_ADDRESS_INDEX, null);
        this.execUpdate(db, BaseProvider.CREATE_INS_SQL, null);
        this.execUpdate(db, BaseProvider.CREATE_IN_PREV_TX_HASH_INDEX, null);
        this.execUpdate(db, BaseProvider.CREATE_ADDRESSTXS_SQL, null);
        this.execUpdate(db, BaseProvider.CREATE_PEER_SQL, null);
        db.endTransaction();
    }

    @Override
    public void completeInSignature(List<In> ins) {
        IDb db = this.getWritableDatabase();
        db.beginTransaction();
        String sql = "update ins set in_signature=? where tx_hash=? and in_sn=? and ifnull(in_signature,'')=''";
        for (In in : ins) {
            this.execUpdate(db, sql, new String[]{Base58.encode(in.getInSignature()), Base58.encode(in.getTxHash()), Integer.toString(in.getInSn())});
        }
        db.endTransaction();
    }

    @Override
    public int needCompleteInSignature(String address) {
        final int[] result = {0};
        String sql = "select max(txs.block_no) from outs,ins,txs where outs.out_address=? and ins.prev_tx_hash=outs.tx_hash and ins.prev_out_sn=outs.out_sn and ifnull(ins.in_signature,'')='' and txs.tx_hash=ins.tx_hash";
        this.execQueryOneRecord(sql, new String[]{address}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                result[0] = iCursor.getInt(0);
                return null;
            }
        });
        return result[0];
    }

    public static Tx applyCursor(ICursor iCursor) {
        return applyCursor(iCursor, null);
    }

    public static Tx applyCursor(ICursor iCursor, @Nullable Tx tx) {
        Tx tempTx;
        if (tx == null) {
            tempTx = new Tx();
        } else {
            tempTx = tx;
        }
        int idColumn = iCursor.getColumnIndex(TxsColumns.BLOCK_NO);
        if (!iCursor.isNull(idColumn)) {
            tempTx.setBlockNo(iCursor.getInt(idColumn));
        } else {
            tempTx.setBlockNo(Tx.TX_UNCONFIRMED);
        }
        idColumn = iCursor.getColumnIndex(TxsColumns.TX_HASH);
        if (idColumn != -1) {
            try {
                tempTx.setTxHash(Base58.decode(iCursor.getString(idColumn)));
            } catch (AddressFormatException e) {
                e.printStackTrace();
            }
        }
        idColumn = iCursor.getColumnIndex(TxsColumns.SOURCE);
        if (idColumn != -1) {
            tempTx.setSource(iCursor.getInt(idColumn));
        }
        if (tempTx.getSource() >= 1) {
            tempTx.setSawByPeerCnt(tempTx.getSource() - 1);
            tempTx.setSource(1);
        } else {
            tempTx.setSawByPeerCnt(0);
            tempTx.setSource(0);
        }
        idColumn = iCursor.getColumnIndex(TxsColumns.TX_TIME);
        if (idColumn != -1) {
            tempTx.setTxTime(iCursor.getInt(idColumn));
        }
        idColumn = iCursor.getColumnIndex(TxsColumns.TX_VER);
        if (idColumn != -1) {
            tempTx.setTxVer(iCursor.getInt(idColumn));
        }
        idColumn = iCursor.getColumnIndex(TxsColumns.TX_LOCKTIME);
        if (idColumn != -1) {
            tempTx.setTxLockTime(iCursor.getInt(idColumn));
        }
        return tempTx;
    }

    public static In applyCursorIn(ICursor iCursor) {
        In in = new In();
        int idColumn = iCursor.getColumnIndex(InsColumns.TX_HASH);
        if (idColumn != -1) {
            try {
                in.setTxHash(Base58.decode(iCursor.getString(idColumn)));
            } catch (AddressFormatException e) {
                e.printStackTrace();
            }
        }
        idColumn = iCursor.getColumnIndex(InsColumns.IN_SN);
        if (idColumn != -1) {
            in.setInSn(iCursor.getInt(idColumn));
        }
        idColumn = iCursor.getColumnIndex(InsColumns.PREV_TX_HASH);
        if (idColumn != -1) {
            try {
                in.setPrevTxHash(Base58.decode(iCursor.getString(idColumn)));
            } catch (AddressFormatException e) {
                e.printStackTrace();
            }
        }
        idColumn = iCursor.getColumnIndex(InsColumns.PREV_OUT_SN);
        if (idColumn != -1) {
            in.setPrevOutSn(iCursor.getInt(idColumn));
        }
        idColumn = iCursor.getColumnIndex(InsColumns.IN_SIGNATURE);
        if (idColumn != -1) {
            String inSignature = iCursor.getString(idColumn);
            if (!Utils.isEmpty(inSignature)) {
                try {
                    in.setInSignature(Base58.decode(iCursor.getString(idColumn)));
                } catch (AddressFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        idColumn = iCursor.getColumnIndex(InsColumns.IN_SEQUENCE);
        if (idColumn != -1) {
            in.setInSequence(iCursor.getInt(idColumn));
        }
        return in;
    }

    public static Out applyCursorOut(ICursor iCursor) {
        Out out = new Out();
        int idColumn = iCursor.getColumnIndex(OutsColumns.TX_HASH);
        if (idColumn != -1) {
            try {
                out.setTxHash(Base58.decode(iCursor.getString(idColumn)));
            } catch (AddressFormatException e) {
                e.printStackTrace();
            }
        }
        idColumn = iCursor.getColumnIndex(OutsColumns.OUT_SN);
        if (idColumn != -1) {
            out.setOutSn(iCursor.getInt(idColumn));
        }
        idColumn = iCursor.getColumnIndex(OutsColumns.OUT_SCRIPT);
        if (idColumn != -1) {
            try {
                out.setOutScript(Base58.decode(iCursor.getString(idColumn)));
            } catch (AddressFormatException e) {
                e.printStackTrace();
            }
        }
        idColumn = iCursor.getColumnIndex(OutsColumns.OUT_VALUE);
        if (idColumn != -1) {
            out.setOutValue(iCursor.getLong(idColumn));
        }
        idColumn = iCursor.getColumnIndex(OutsColumns.OUT_STATUS);
        if (idColumn != -1) {
            out.setOutStatus(Out.getOutStatus(iCursor.getInt(idColumn)));
        }
        idColumn = iCursor.getColumnIndex(OutsColumns.OUT_ADDRESS);
        if (idColumn != -1) {
            out.setOutAddress(iCursor.getString(idColumn));
        }
        idColumn = iCursor.getColumnIndex(OutsColumns.HD_ACCOUNT_ID);
        if (idColumn != -1 && !iCursor.isNull(idColumn)) {
            out.setHDAccountId(iCursor.getInt(idColumn));
        }
        return out;
    }

    public void addInsAndOuts(IDb db, final Tx tx) {
        String txHash = Base58.encode(tx.getTxHash());
        tx.setOuts(new ArrayList<Out>());
        tx.setIns(new ArrayList<In>());
        String sql = "select * from ins where tx_hash=? order by in_sn";
        this.execQueryLoop(db, sql, new String[]{txHash}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                In in = applyCursorIn(iCursor);
                in.setTx(tx);
                tx.getIns().add(in);
                return null;
            }
        });

        sql = "select * from outs where tx_hash=? order by out_sn";
        this.execQueryLoop(db, sql, new String[]{txHash}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                Out out = applyCursorOut(iCursor);
                out.setTx(tx);
                tx.getOuts().add(out);
                return null;
            }
        });
    }

    public void insertTx(IDb db, Tx tx) {
        final int[] cnt = {0};
        String existSql = "select count(0) cnt from txs where tx_hash=?";
        this.execQueryOneRecord(db, existSql, new String[]{Base58.encode(tx.getTxHash())}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                int idColumn = iCursor.getColumnIndex("cnt");
                if (idColumn != -1) {
                    cnt[0] = iCursor.getInt(idColumn);
                }
                return null;
            }
        });
        if (cnt[0] == 0) {
            this.insertTxToDb(db, tx);
        }
    }

    protected abstract void insertTxToDb(IDb db, Tx tx);


    public List<AddressTx> insertIn(IDb db, final Tx tx) {
        final List<AddressTx> addressTxes = Lists.newArrayList();
        String existSql = "select count(0) cnt from ins where tx_hash=? and in_sn=?";
        String outAddressSql = "select out_address from outs where tx_hash=? and out_sn=?";
        String updateOutStatusSql = "update outs set out_status=? where tx_hash=? and out_sn=?";
        for (In in : tx.getIns()) {
            final int[] cnt = {0};
            this.execQueryOneRecord(db, existSql, new String[]{Base58.encode(in.getTxHash()), Integer.toString(in.getInSn())}
                    , new Function<ICursor, Void>() {
                        @Nullable
                        @Override
                        public Void apply(@Nullable ICursor iCursor) {
                            assert iCursor != null;
                            int idColumn = iCursor.getColumnIndex("cnt");
                            if (idColumn != -1) {
                                cnt[0] = iCursor.getInt(idColumn);
                            }
                            return null;
                        }
                    });
            if (cnt[0] == 0) {
                this.insertInToDb(db, in);
            }
            this.execQueryLoop(db, outAddressSql, new String[]{Base58.encode(in.getPrevTxHash())
                                       , Integer.toString(in.getPrevOutSn())}
                    , new Function<ICursor, Void>() {
                        @Nullable
                        @Override
                        public Void apply(@Nullable ICursor iCursor) {
                            assert iCursor != null;
                            int idColumn = iCursor.getColumnIndex("out_address");
                            if (idColumn != -1) {
                                addressTxes.add(new AddressTx(iCursor.getString(idColumn), Base58.encode(tx.getTxHash())));
                            }
                            return null;
                        }
                    });

            this.execUpdate(db, updateOutStatusSql, new String[]{Integer.toString(Out.OutStatus.spent.getValue()), Base58.encode(in.getPrevTxHash()), Integer.toString(in.getPrevOutSn())});
        }
        return addressTxes;
    }

    protected abstract void insertInToDb(IDb db, In in);

    public List<AddressTx> insertOut(IDb db, Tx tx) {
        String existSql = "select count(0) cnt from outs where tx_hash=? and out_sn=?";
        String updateHDAccountIdSql = "update outs set hd_account_id=? where tx_hash=? and out_sn=?";
        String queryHDAddressSql = "select hd_account_id,path_type,address_index from hd_account_addresses where address=?";
        String updateHDAddressIssuedSql = "update hd_account_addresses set is_issued=? where path_type=? and address_index<=? and hd_account_id=?";
        String queryPrevTxHashSql = "select tx_hash from ins where prev_tx_hash=? and prev_out_sn=?";
        String updateOutStatusSql = "update outs set out_status=? where tx_hash=? and out_sn=?";
        final List<AddressTx> addressTxes = Lists.newArrayList();
        for (final Out outItem : tx.getOuts()) {
            final int[] cnt = {0};
            this.execQueryOneRecord(db, existSql, new String[]{Base58.encode(outItem.getTxHash()), Integer
                    .toString(outItem.getOutSn())}, new Function<ICursor, Void>() {
                @Nullable
                @Override
                public Void apply(@Nullable ICursor iCursor) {
                    assert iCursor != null;
                    int idColumn = iCursor.getColumnIndex("cnt");
                    if (idColumn != -1) {
                        cnt[0] = iCursor.getInt(idColumn);
                    }
                    return null;
                }
            });
            if (cnt[0] == 0) {
                this.insertOutToDb(db, outItem);
            } else {
                if (outItem.getHDAccountId() > -1) {
                    this.execUpdate(db, updateHDAccountIdSql, new String[]{Integer.toString(outItem.getHDAccountId()), Base58.encode(tx.getTxHash()), Integer.toString(outItem.getOutSn())});
                }
            }
            if (outItem.getHDAccountId() > -1) {
                final int[] tmpHDAccountId = {-1};
                final int[] tmpPathType = {0};
                final int[] tmpAddressIndex = {0};
                this.execQueryOneRecord(db, queryHDAddressSql, new String[]{outItem.getOutAddress()}, new Function<ICursor, Void>() {
                    @Nullable
                    @Override
                    public Void apply(@Nullable ICursor iCursor) {
                        tmpHDAccountId[0] = iCursor.getInt(0);
                        tmpPathType[0] = iCursor.getInt(1);
                        tmpAddressIndex[0] = iCursor.getInt(2);
                        return null;
                    }
                });
                if (tmpHDAccountId[0] > 0) {
                    this.execUpdate(db, updateHDAddressIssuedSql, new String[]{"1", Integer.toString(tmpPathType[0]), Integer.toString(tmpAddressIndex[0]), Integer.toString(tmpHDAccountId[0])});
                }
            }
            if (!Utils.isEmpty(outItem.getOutAddress())) {
                addressTxes.add(new AddressTx(outItem.getOutAddress(), Base58.encode(tx.getTxHash())));
            }
            final boolean[] isSpentByExistTx = {false};
            this.execQueryOneRecord(db, queryPrevTxHashSql, new String[]{Base58.encode(tx.getTxHash())
                    , Integer.toString(outItem.getOutSn())}, new Function<ICursor, Void>() {
                @Nullable
                @Override
                public Void apply(@Nullable ICursor iCursor) {
                    assert iCursor != null;
                    int idColumn = iCursor.getColumnIndex("tx_hash");
                    if (idColumn != -1) {
                        addressTxes.add(new AddressTx(outItem.getOutAddress(), iCursor.getString(idColumn)));
                    }
                    isSpentByExistTx[0] = true;
                    return null;
                }
            });
            if (isSpentByExistTx[0]) {
                this.execUpdate(db, updateOutStatusSql, new String[]{Integer.toString(Out.OutStatus.spent.getValue()), Base58.encode(tx.getTxHash()), Integer.toString(outItem.getOutSn())});
            }
        }
        return addressTxes;
    }

    @Override
    public byte[] isIdentify(Tx tx) {
        HashSet<String> result = Sets.newHashSet();
        for (In in : tx.getIns()) {
            String queryPrevTxHashSql = "select tx_hash from ins where prev_tx_hash=? and prev_out_sn=?";
            final HashSet<String> each = Sets.newHashSet();
            this.execQueryOneRecord(this.getReadableDatabase(), queryPrevTxHashSql, new String[]{Base58.encode(in.getPrevTxHash())
                    , Integer.toString(in.getPrevOutSn())}, new Function<ICursor, Void>() {
                @Nullable
                @Override
                public Void apply(@Nullable ICursor iCursor) {
                    assert iCursor != null;
                    each.add(iCursor.getString(0));
                    return null;
                }
            });
            each.remove(Base58.encode(tx.getTxHash()));
            result.retainAll(each);
            if (result.size() == 0) {
                break;
            }
        }
        if (result.size() == 0) {
            return new byte[0];
        } else {
            try {
                return Base58.decode((String) result.toArray()[0]);
            } catch (AddressFormatException e) {
                e.printStackTrace();
                return new byte[0];
            }
        }
    }

    protected abstract void insertOutToDb(IDb db, Out out);


}
