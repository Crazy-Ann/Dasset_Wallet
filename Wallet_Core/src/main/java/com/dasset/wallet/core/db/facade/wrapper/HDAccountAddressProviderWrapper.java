package com.dasset.wallet.core.db.facade.wrapper;

import com.dasset.wallet.core.In;
import com.dasset.wallet.core.Out;
import com.dasset.wallet.core.OutPoint;
import com.dasset.wallet.core.Tx;
import com.dasset.wallet.core.contant.BitherjSettings;
import com.dasset.wallet.core.contant.PathType;
import com.dasset.wallet.core.contant.db.HDAccountAddressesColumns;
import com.dasset.wallet.core.contant.db.OutsColumns;
import com.dasset.wallet.core.db.base.ICursor;
import com.dasset.wallet.core.db.base.IDb;
import com.dasset.wallet.core.db.facade.IHDAccountAddressProvider;
import com.dasset.wallet.core.exception.AddressFormatException;
import com.dasset.wallet.core.utils.Base58;
import com.dasset.wallet.core.utils.Sha256Hash;
import com.dasset.wallet.core.utils.Utils;
import com.dasset.wallet.core.wallet.hd.AbstractHD;
import com.dasset.wallet.core.wallet.hd.HDAccount;
import com.dasset.wallet.core.wallet.hd.HDAddress;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Nullable;

public abstract class HDAccountAddressProviderWrapper extends ProviderWrapper implements IHDAccountAddressProvider {

    @Override
    public void addAddress(List<HDAddress> hdAddresses) {
        String sql = "insert into hd_account_addresses(hd_account_id,path_type,address_index,is_issued,address,publicKey,is_synced) values(?,?,?,?,?,?,?)";
        IDb writeDb = this.getWriteDb();
        writeDb.beginTransaction();
        for (HDAddress hdAddress : hdAddresses) {
            this.execUpdate(writeDb, sql, new String[]{
                    Integer.toString(hdAddress.getHdAccountId())
                    , Integer.toString(hdAddress.getPathType().getType())
                    , Integer.toString(hdAddress.getIndex())
                    , hdAddress.isIssued() ? "1" : "0"
                    , hdAddress.getAddress()
                    , Base58.encode(hdAddress.getPublicKey())
                    , hdAddress.hasSyncedCompleted() ? "1" : "0"
            });
        }
        writeDb.endTransaction();
    }


    @Override
    public int issuedIndex(int hdAccountId, PathType pathType) {
        String sql = "select ifnull(max(address_index),-1) address_index from hd_account_addresses where path_type=? and is_issued=? and hd_account_id=?";
        final int[] issuedIndex = {-1};
        this.execQueryOneRecord(sql, new String[]{Integer.toString(pathType.getType()), "1", String.valueOf(hdAccountId)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(ICursor iCursor) {
                int idColumn = iCursor.getColumnIndex(HDAccountAddressesColumns.ADDRESS_INDEX);
                if (idColumn != -1) {
                    issuedIndex[0] = iCursor.getInt(idColumn);
                }
                return null;
            }
        });
        return issuedIndex[0];
    }

    @Override
    public int allGeneratedAddressCount(int hdAccountId, PathType pathType) {
        String sql = "select ifnull(count(address),0) count from hd_account_addresses where path_type=? and hd_account_id=?";
        final int[] count = {0};
        this.execQueryOneRecord(sql, new String[]{Integer.toString(pathType.getType()), String.valueOf(hdAccountId)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                int idColumn = iCursor.getColumnIndex("count");
                if (idColumn != -1) {
                    count[0] = iCursor.getInt(idColumn);
                }
                return null;
            }
        });
        return count[0];
    }

    @Override
    public String externalAddress(int hdAccountId) {
        String sql = "select address from hd_account_addresses where path_type=? and is_issued=? and hd_account_id=? order by address_index asc limit 1 ";
        final String[] address = {null};
        this.execQueryOneRecord(sql, new String[]{Integer.toString(PathType.EXTERNAL_ROOT_PATH.getType())
                , "0", Integer.toString(hdAccountId)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                int idColumn = iCursor.getColumnIndex(HDAccountAddressesColumns.ADDRESS);
                if (idColumn != -1) {
                    address[0] = iCursor.getString(idColumn);
                }
                return null;
            }
        });
        return address[0];
    }

    @Override
    public HashSet<String> getBelongAccountAddresses(int hdAccountId, List<String> addressList) {
        final HashSet<String> addressSet = Sets.newHashSet();
        List<String> temp = Lists.newArrayList();
        if (addressList != null) {
            for (String str : addressList) {
                temp.add(Utils.format("'%s'", str));
            }
        }
        String sql = Utils.format("select address from hd_account_addresses where hd_account_id=? and address in (%s) ", Utils.joinString(temp, ","));
        this.execQueryLoop(sql, new String[]{Integer.toString(hdAccountId)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                int idColumn = iCursor.getColumnIndex(HDAccountAddressesColumns.ADDRESS);
                if (idColumn != -1) {
                    addressSet.add(iCursor.getString(idColumn));
                }
                return null;
            }
        });
        return addressSet;
    }

    @Override
    public HashSet<String> getBelongAccountAddresses(List<String> addressList) {
        final HashSet<String> addressSet = Sets.newHashSet();
        List<String> temp = Lists.newArrayList();
        if (addressList != null) {
            for (String str : addressList) {
                temp.add(Utils.format("'%s'", str));
            }
        }
        String sql = Utils.format("select address from hd_account_addresses where address in (%s) ", Utils.joinString(temp, ","));
        this.execQueryLoop(sql, null, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                int idColumn = iCursor.getColumnIndex(HDAccountAddressesColumns.ADDRESS);
                if (idColumn != -1) {
                    addressSet.add(iCursor.getString(idColumn));
                }
                return null;
            }
        });
        return addressSet;
    }

    @Override
    public Tx updateOutHDAccountId(Tx tx) {
        final Tx finalTx = tx;
        List<String> addressList = tx.getOutAddressList();
        if (addressList != null && addressList.size() > 0) {
            HashSet<String> set = new HashSet<String>();
            set.addAll(addressList);
            StringBuilder strBuilder = new StringBuilder();
            for (String str : set) {
                strBuilder.append("'").append(str).append("',");
            }

            String sql = Utils.format("select address,hd_account_id from hd_account_addresses where address in (%s) "
                    , strBuilder.substring(0, strBuilder.length() - 1));
            this.execQueryLoop(sql, null, new Function<ICursor, Void>() {
                @Nullable
                @Override
                public Void apply(@Nullable ICursor iCursor) {
                    assert iCursor != null;
                    String address = iCursor.getString(0);
                    int hdAccountId = iCursor.getInt(1);
                    for (Out out : finalTx.getOuts()) {
                        if (Utils.compareString(out.getOutAddress(), address)) {
                            out.setHDAccountId(hdAccountId);
                        }
                    }
                    return null;
                }
            });
        }
        return tx;
    }

    @Override
    public int getRelatedAddressCnt(List<String> addresses) {
        final int[] cnt = {0};
        if (addresses != null && addresses.size() > 0) {
            HashSet<String> set = Sets.newHashSet();
            set.addAll(addresses);
            StringBuilder stringBuilder = new StringBuilder();
            for (String str : set) {
                stringBuilder.append("'").append(str).append("',");
            }
            String sql = Utils.format("select count(0) cnt from hd_account_addresses where address in (%s) ", stringBuilder.substring(0, stringBuilder.length() - 1));
            this.execQueryOneRecord(sql, null, new Function<ICursor, Void>() {
                @Nullable
                @Override
                public Void apply(@Nullable ICursor iCursor) {
                    assert iCursor != null;
                    cnt[0] = iCursor.getInt(0);
                    return null;
                }
            });
        }
        return cnt[0];
    }

    @Override
    public List<Integer> getRelatedHDAccountIdList(List<String> addresses) {
        final List<Integer> hdAccountIdList = Lists.newArrayList();
        if (addresses != null && addresses.size() > 0) {
            HashSet<String> set = Sets.newHashSet();
            set.addAll(addresses);
            StringBuilder strBuilder = new StringBuilder();
            for (String str : set) {
                strBuilder.append("'").append(str).append("',");
            }
            String sql = Utils.format("select distinct hd_account_id from hd_account_addresses where address in (%s) ", strBuilder.substring(0, strBuilder.length() - 1));
            this.execQueryLoop(sql, null, new Function<ICursor, Void>() {
                @Nullable
                @Override
                public Void apply(@Nullable ICursor iCursor) {
                    assert iCursor != null;
                    hdAccountIdList.add(iCursor.getInt(0));
                    return null;
                }
            });
        }
        return hdAccountIdList;
    }

    @Override
    public List<byte[]> getPubs(int hdAccountId, PathType pathType) {
        String sql = "select publicKey from hd_account_addresses where path_type=? and hd_account_id=?";
        final List<byte[]> adressPubList = Lists.newArrayList();
        this.execQueryLoop(sql, new String[]{Integer.toString(pathType.getType()), Integer.toString(hdAccountId)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                assert iCursor != null;
                int idColumn = iCursor.getColumnIndex(HDAccountAddressesColumns.PUB);
                if (idColumn != -1) {
                    try {
                        adressPubList.add(Base58.decode(iCursor.getString(idColumn)));
                    } catch (AddressFormatException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        });
        return adressPubList;
    }

    public List<HDAddress> getAllHDAddress(int hdAccountId) {
        final List<HDAddress> adressPubList = Lists.newArrayList();
        String sql = "select address,publicKey,path_type,address_index,is_issued,is_synced,hd_account_id from hd_account_addresses where hd_account_id=? ";
        this.execQueryLoop(sql, new String[]{Integer.toString(hdAccountId)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                adressPubList.add(formatAddress(iCursor));
                return null;
            }
        });
        return adressPubList;
    }


    @Override
    public List<Out> getUnspendOutByHDAccount(int hdAccountId) {
        final List<Out> outItems = Lists.newArrayList();
        String unspendOutSql = "select a.* from outs a,txs b where a.tx_hash=b.tx_hash and a.out_status=? and a.hd_account_id=?";
        this.execQueryLoop(unspendOutSql, new String[]{Integer.toString(Out.OutStatus.unspent.getValue()), Integer.toString(hdAccountId)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                outItems.add(TxProviderWrapper.applyCursorOut(iCursor));
                return null;
            }
        });
        return outItems;
    }

    @Override
    public HDAddress addressForPath(int hdAccountId, PathType type, int index) {
        String sql = "select address,publicKey,path_type,address_index,is_issued, is_synced,hd_account_id from hd_account_addresses where path_type=? and address_index=? and hd_account_id=?";
        final HDAddress[] accountAddress = {null};
        this.execQueryOneRecord(sql, new String[]{Integer.toString(type.getType()), Integer.toString(index), Integer.toString(hdAccountId)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                accountAddress[0] = formatAddress(iCursor);
                return null;
            }
        });
        return accountAddress[0];
    }

    @Override
    public void updateIssuedIndex(int hdAccountId, PathType pathType, int index) {
        String sql = "update hd_account_addresses set is_issued=? where path_type=? and address_index<=? and hd_account_id=?";
        this.execUpdate(sql, new String[]{"1", Integer.toString(pathType.getType()), Integer.toString(index), Integer.toString(hdAccountId)});
    }


    @Override
    public List<HDAddress> belongAccount(int hdAccountId, List<String> addresses) {
        final List<HDAddress> hdAccountAddressList = new ArrayList<>();
        List<String> temp = Lists.newArrayList();
        for (String address : addresses) {
            temp.add(Utils.format("'%s'", address));
        }
        String sql = "select address,publicKey,path_type,address_index,is_issued,is_synced,hd_account_id from hd_account_addresses where hd_account_id=? and address in (" + Utils.joinString(temp, ",") + ")";
        this.execQueryLoop(sql, new String[]{Integer.toString(hdAccountId)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                hdAccountAddressList.add(formatAddress(iCursor));
                return null;
            }
        });
        return hdAccountAddressList;
    }

    @Override
    public long getHDAccountConfirmedBalance(int hdAccountId) {
        final long[] sum = {0};
        String sql = "select ifnull(sum(a.out_value),0) sum from outs a,txs b where a .tx_hash=b.tx_hash and a.out_status=? and a.hd_account_id=? and b.block_no is not null";
        this.execQueryOneRecord(sql, new String[]{Integer.toString(Out.OutStatus.unspent.getValue()), Integer.toString
                (hdAccountId)}, new Function<ICursor, Void>() {
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


    @Override
    public List<Tx> getHDAccountUnconfirmedTx(int hdAccountId) {
        String sql = "select distinct a.* from txs a,addresses_txs b,hd_account_addresses c  where a.tx_hash=b.tx_hash and b.address=c.address and c.hd_account_id=? and a.block_no is null order by a.tx_hash";
        final List<Tx> txes = Lists.newArrayList();
        final HashMap<Sha256Hash, Tx> txHashMap = Maps.newHashMap();
        IDb db = this.getReadDb();
        this.execQueryLoop(db, sql, new String[]{Integer.toString(hdAccountId)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                Tx txItem = TxProviderWrapper.applyCursor(iCursor);
                txItem.setIns(new ArrayList<In>());
                txItem.setOuts(new ArrayList<Out>());
                txes.add(txItem);
                txHashMap.put(new Sha256Hash(txItem.getTxHash()), txItem);
                return null;
            }
        });
        sql = "select distinct a.* from ins a, txs b,addresses_txs c,hd_account_addresses d where a.tx_hash=b.tx_hash and b.tx_hash=c.tx_hash and c.address=d.address and b.block_no is null and d.hd_account_id=? order by a.tx_hash,a.in_sn";
        this.execQueryLoop(db, sql, new String[]{Integer.toString(hdAccountId)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                In in = TxProviderWrapper.applyCursorIn(iCursor);
                Tx tx = txHashMap.get(new Sha256Hash(in.getTxHash()));
                if (tx != null) {
                    tx.getIns().add(in);
                }
                return null;
            }
        });
        sql = "select distinct a.* from outs a, txs b,addresses_txs c,hd_account_addresses d where a.tx_hash=b.tx_hash and b.tx_hash=c.tx_hash and c.address=d.address and b.block_no is null and d.hd_account_id=? order by a.tx_hash,a.out_sn";
        this.execQueryLoop(db, sql, new String[]{Integer.toString(hdAccountId)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                Out out = TxProviderWrapper.applyCursorOut(iCursor);
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
    public List<HDAddress> getSigningAddressesForInputs(int hdAccountId, List<In> ins) {
        final List<HDAddress> hdAddresses = Lists.newArrayList();
        for (In in : ins) {
            String sql = "select a.address,a.path_type,a.address_index,a.is_synced,a.hd_account_id from hd_account_addresses a ,outs b where a.address=b.out_address and b.tx_hash=? and b.out_sn=? and a.hd_account_id=?";
            OutPoint outPoint = in.getOutpoint();
            this.execQueryOneRecord(sql, new String[]{Base58.encode(in.getPrevTxHash()), Integer.toString
                    (outPoint.getOutSn()), Integer.toString(hdAccountId)}, new Function<ICursor, Void>() {
                @Nullable
                @Override
                public Void apply(@Nullable ICursor iCursor) {
                    hdAddresses.add(formatAddress(iCursor));
                    return null;
                }
            });
        }
        return hdAddresses;
    }


    @Override
    public void updateSyncdComplete(int hdAccountId, HDAddress address) {
        String sql = "update hd_account_addresses set is_synced=? where address=? and hd_account_id=?";
        this.execUpdate(sql, new String[]{address.hasSyncedCompleted() ? "1" : "0", address.getAddress(), Integer.toString(hdAccountId)});
    }

    @Override
    public void updateSyncedForIndex(int hdAccountId, PathType pathType, int index) {
        String sql = "update hd_account_addresses set is_synced=? where path_type=? and address_index>? and hd_account_id=?";
        this.execUpdate(sql, new String[]{"1", Integer.toString(pathType.getType()), Integer.toString(index), Integer.toString(hdAccountId)});
    }

    @Override
    public void setSyncedNotComplete() {
        String sql = "update hd_account_addresses set is_synced=?";
        this.execUpdate(sql, new String[]{"0"});
    }

    @Override
    public int unSyncedAddressCount(int hdAccountId) {
        String sql = "select count(address) cnt from hd_account_addresses where is_synced=? and hd_account_id=? ";
        final int[] cnt = {0};
        this.execQueryOneRecord(sql, new String[]{"0", Integer.toString(hdAccountId)}, new Function<ICursor, Void>() {
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
        return cnt[0];
    }

    @Override
    public List<Tx> getRecentlyTxsByAccount(int hdAccountId, int greaterThanBlockNo, int limit) {
        final List<Tx> txes = Lists.newArrayList();
        String sql = "select distinct a.* from txs a, addresses_txs b, hd_account_addresses c where a.tx_hash=b.tx_hash and b.address=c.address and ((a.block_no is null) or (a.block_no is not null and a.block_no>?)) and c.hd_account_id=? order by ifnull(a.block_no,4294967295) desc, a.tx_time desc limit ?";
        IDb db = this.getReadDb();
        this.execQueryLoop(db, sql, new String[]{Integer.toString(greaterThanBlockNo)
                , Integer.toString(hdAccountId), Integer.toString(limit)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                Tx txItem = TxProviderWrapper.applyCursor(iCursor);
                txes.add(txItem);
                return null;
            }
        });
        for (Tx tx : txes) {
            this.addInsAndOuts(db, tx);
        }
        return txes;
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
                In in = TxProviderWrapper.applyCursorIn(iCursor);
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
                Out out = TxProviderWrapper.applyCursorOut(iCursor);
                out.setTx(tx);
                tx.getOuts().add(out);
                return null;
            }
        });
    }

    @Override
    public long sentFromAccount(int hdAccountId, byte[] txHash) {
        String sql = "select  sum(o.out_value) out_value from ins i,outs o where i.tx_hash=? and o.tx_hash=i.prev_tx_hash and i.prev_out_sn=o.out_sn and o .hd_account_id=?";
        final long[] sum = {0};
        this.execQueryOneRecord(sql, new String[]{Base58.encode(txHash), Integer.toString(hdAccountId)}, new Function<ICursor, Void>() {
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
    public List<Tx> getTxAndDetailByHDAccount(int hdAccountId) {
        final List<Tx> txes = Lists.newArrayList();
        final HashMap<Sha256Hash, Tx> txHashMap = Maps.newHashMap();
        String sql = "select distinct a.* from txs a,addresses_txs b,hd_account_addresses c where a.tx_hash=b.tx_hash and b.address=c.address and c.hd_account_id=? order by ifnull(block_no,4294967295) desc,a.tx_hash";
        IDb db = this.getReadDb();
        final StringBuilder stringBuilder = new StringBuilder();
        this.execQueryLoop(db, sql, new String[]{Integer.toString(hdAccountId)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                Tx tx = TxProviderWrapper.applyCursor(iCursor);
                tx.setIns(new ArrayList<In>());
                tx.setOuts(new ArrayList<Out>());
                txes.add(tx);
                txHashMap.put(new Sha256Hash(tx.getTxHash()), tx);
                stringBuilder.append("'").append(Base58.encode(tx.getTxHash())).append("'").append(",");
                return null;
            }
        });
        if (stringBuilder.length() > 1) {
            String tx = stringBuilder.substring(0, stringBuilder.length() - 1);
            sql = Utils.format("select b.* from ins b where b.tx_hash in (%s) order by b.tx_hash ,b.in_sn", tx);
            this.execQueryLoop(db, sql, null, new Function<ICursor, Void>() {
                @Nullable
                @Override
                public Void apply(@Nullable ICursor iCursor) {
                    In in = TxProviderWrapper.applyCursorIn(iCursor);
                    Tx tx = txHashMap.get(new Sha256Hash(in.getTxHash()));
                    if (tx != null) {
                        tx.getIns().add(in);
                    }
                    return null;
                }
            });
            sql = Utils.format("select b.* from outs b where b.tx_hash in (%s) order by b.tx_hash,b.out_sn", tx);
            this.execQueryLoop(db, sql, null, new Function<ICursor, Void>() {
                @Nullable
                @Override
                public Void apply(@Nullable ICursor iCursor) {
                    Out out = TxProviderWrapper.applyCursorOut(iCursor);
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
    public List<Tx> getTxAndDetailByHDAccount(int hdAccountId, int page) {
        final List<Tx> txes = Lists.newArrayList();
        final HashMap<Sha256Hash, Tx> txHashMap = Maps.newHashMap();
        String sql = "select distinct a.* from txs a,addresses_txs b,hd_account_addresses c where a.tx_hash=b.tx_hash and b.address=c.address and c.hd_account_id=? order by ifnull(block_no,4294967295) desc,a.tx_hash limit ?,?";
        IDb db = this.getReadDb();
        final StringBuilder stringBuilder = new StringBuilder();
        this.execQueryLoop(db, sql, new String[]{
                Integer.toString(hdAccountId)
                , Integer.toString((page - 1) * BitherjSettings.TX_PAGE_SIZE)
                , Integer.toString(BitherjSettings.TX_PAGE_SIZE)
        }, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                Tx tx = TxProviderWrapper.applyCursor(iCursor);
                tx.setIns(new ArrayList<In>());
                tx.setOuts(new ArrayList<Out>());
                txes.add(tx);
                txHashMap.put(new Sha256Hash(tx.getTxHash()), tx);
                stringBuilder.append("'").append(Base58.encode(tx.getTxHash())).append("'").append(",");
                return null;
            }
        });
        if (stringBuilder.length() > 1) {
            String tx = stringBuilder.substring(0, stringBuilder.length() - 1);
            sql = Utils.format("select b.* from ins b where b.tx_hash in (%s) order by b.tx_hash ,b.in_sn", tx);
            this.execQueryLoop(db, sql, null, new Function<ICursor, Void>() {
                @Nullable
                @Override
                public Void apply(@Nullable ICursor iCursor) {
                    In in = TxProviderWrapper.applyCursorIn(iCursor);
                    Tx tx = txHashMap.get(new Sha256Hash(in.getTxHash()));
                    if (tx != null) {
                        tx.getIns().add(in);
                    }
                    return null;
                }
            });
            sql = Utils.format("select b.* from outs b where b.tx_hash in (%s) order by b.tx_hash,b.out_sn", tx);
            this.execQueryLoop(sql, null, new Function<ICursor, Void>() {
                @Nullable
                @Override
                public Void apply(@Nullable ICursor iCursor) {
                    Out out = TxProviderWrapper.applyCursorOut(iCursor);
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
    public int hdAccountTxCount(int hdAccountId) {
        final int[] result = {0};
        String sql = "select count( distinct a.tx_hash) cnt from addresses_txs a, hd_account_addresses b where a.address=b.address and b.hd_account_id=? ";
        this.execQueryOneRecord(sql, new String[]{Integer.toString(hdAccountId)}, new Function<ICursor, Void>() {
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
    public int getUnspendOutCountByHDAccountWithPath(int hdAccountId, PathType pathType) {
        final int[] result = {0};
        String sql = "select count(tx_hash) cnt from outs where out_address in (select address from hd_account_addresses where path_type =? and out_status=?) and hd_account_id=?";
        this.execQueryOneRecord(sql, new String[]{Integer.toString(pathType.getType())
                , Integer.toString(Out.OutStatus.unspent.getValue())
                , Integer.toString(hdAccountId)
        }, new Function<ICursor, Void>() {
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
    public List<Out> getUnspendOutByHDAccountWithPath(int hdAccountId, PathType pathType) {
        String sql = "select * from outs where out_address in " +
                "(select address from hd_account_addresses where path_type =? and out_status=?) and hd_account_id=?";
        final List<Out> outs = Lists.newArrayList();
        this.execQueryLoop(sql, new String[]{Integer.toString(pathType.getType())
                , Integer.toString(Out.OutStatus.unspent.getValue())
                , Integer.toString(hdAccountId)
        }, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                outs.add(TxProviderWrapper.applyCursorOut(iCursor));
                return null;
            }
        });
        return outs;
    }

    @Override
    public int getUnconfirmedSpentOutCountByHDAccountWithPath(int hdAccountId, PathType pathType) {
        final int[] result = {0};
        String sql = "select count(0) cnt from outs o, ins i, txs t, hd_account_addresses a where o.tx_hash=i.prev_tx_hash and o.out_sn=i.prev_out_sn and t.tx_hash=i.tx_hash  and o.out_address=a.address and a.path_type=? and o.out_status=? and t.block_no is null and a.hd_account_id=?";
        this.execQueryOneRecord(sql, new String[]{Integer.toString(pathType.getType())
                , Integer.toString(Out.OutStatus.spent.getValue())
                , Integer.toString(hdAccountId)
        }, new Function<ICursor, Void>() {
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
    public List<Out> getUnconfirmedSpentOutByHDAccountWithPath(int hdAccountId, PathType pathType) {
        String sql = "select o.* from outs o, ins i, txs t, hd_account_addresses a where o.tx_hash=i.prev_tx_hash and o.out_sn=i.prev_out_sn and t.tx_hash=i.tx_hash and o.out_address=a.address and a.path_type=?  and o.out_status=? and t.block_no is null and a.hd_account_id=?";
        final List<Out> outs = Lists.newArrayList();
        this.execQueryLoop(sql, new String[]{Integer.toString(pathType.getType())
                , Integer.toString(Out.OutStatus.spent.getValue())
                , Integer.toString(hdAccountId)
        }, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                outs.add(TxProviderWrapper.applyCursorOut(iCursor));
                return null;
            }
        });
        return outs;
    }

    @Override
    public boolean requestNewReceivingAddress(int hdAccountId) {
        int issuedIndex = this.issuedIndex(hdAccountId, PathType.EXTERNAL_ROOT_PATH);
        final boolean[] result = {false};
        if (issuedIndex >= HDAccount.MAX_UNUSED_NEW_ADDRESS_COUNT - 2) {
            String sql = "select count(0) from hd_account_addresses a,outs b where a.address=b.out_address and a.hd_account_id=? and a.path_type=0 and a.address_index>? and a.is_issued=?";
            this.execQueryOneRecord(sql, new String[]{Integer.toString(hdAccountId), Integer.toString(issuedIndex - HDAccount.MAX_UNUSED_NEW_ADDRESS_COUNT + 1), "1"}, new Function<ICursor, Void>() {
                @Nullable
                @Override
                public Void apply(@Nullable ICursor iCursor) {
                    assert iCursor != null;
                    result[0] = iCursor.getInt(0) > 0;
                    return null;
                }
            });
        } else {
            result[0] = true;
        }
        if (result[0]) {
            this.updateIssuedIndex(hdAccountId, PathType.EXTERNAL_ROOT_PATH, issuedIndex + 1);
        }
        return result[0];
    }

    private HDAddress formatAddress(ICursor iCursor) {
        String address = null;
        byte[] pubs = null;
        PathType pathType = PathType.EXTERNAL_ROOT_PATH;
        int index = 0;
        boolean isIssued = false;
        boolean isSynced = true;
        int hdAccountId = 0;
        int idColumn = iCursor.getColumnIndex(HDAccountAddressesColumns.ADDRESS);
        if (idColumn != -1) {
            address = iCursor.getString(idColumn);
        }
        idColumn = iCursor.getColumnIndex(HDAccountAddressesColumns.PUB);
        if (idColumn != -1) {
            try {
                pubs = Base58.decode(iCursor.getString(idColumn));
            } catch (AddressFormatException e) {
                e.printStackTrace();
            }
        }
        idColumn = iCursor.getColumnIndex(HDAccountAddressesColumns.PATH_TYPE);
        if (idColumn != -1) {
            pathType = AbstractHD.getTernalRootType(iCursor.getInt(idColumn));
        }
        idColumn = iCursor.getColumnIndex(HDAccountAddressesColumns.ADDRESS_INDEX);
        if (idColumn != -1) {
            index = iCursor.getInt(idColumn);
        }
        idColumn = iCursor.getColumnIndex(HDAccountAddressesColumns.IS_ISSUED);
        if (idColumn != -1) {
            isIssued = iCursor.getInt(idColumn) == 1;
        }
        idColumn = iCursor.getColumnIndex(HDAccountAddressesColumns.IS_SYNCED);
        if (idColumn != -1) {
            isSynced = iCursor.getInt(idColumn) == 1;
        }
        idColumn = iCursor.getColumnIndex(HDAccountAddressesColumns.HD_ACCOUNT_ID);
        if (idColumn != -1) {
            hdAccountId = iCursor.getInt(idColumn);
        }
        return new HDAddress(address, pubs, pathType, index, isIssued, isSynced, hdAccountId);
    }

    public List<Out> getUnspentOutputByBlockNo(long blockNo, int hdSeedId) {
        final List<Out> outs = Lists.newArrayList();
        String sqlPreUnspentOut = "select a.* from outs a,txs b where a.tx_hash=b.tx_hash and a.hd_account_id=? and a.out_status=? and b.block_no is not null and b.block_no<?";
        String sqlPostSpentOuts = "select a.* from outs a, txs out_b, ins i, txs b where a.tx_hash=out_b.tx_hash and a.out_sn=i.prev_out_sn and a.tx_hash=i.prev_tx_hash and a.hd_account_id=? and b.tx_hash=i.tx_hash and a.out_status=? and out_b.block_no is not null and out_b.block_no<? and (b.block_no>=? or b.block_no is null)";
        this.execQueryLoop(sqlPreUnspentOut, new String[] {Integer.toString(hdSeedId),Integer.toString(0),
                Long.toString(blockNo)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                outs.add(TxProviderWrapper.applyCursorOut(iCursor));
                return null;
            }
        });

        this.execQueryLoop(sqlPostSpentOuts, new String[] {Integer.toString(hdSeedId),Integer.toString(1),
                Long.toString(blockNo),Long.toString(blockNo)}, new Function<ICursor, Void>() {
            @Nullable
            @Override
            public Void apply(@Nullable ICursor iCursor) {
                outs.add(TxProviderWrapper.applyCursorOut(iCursor));
                return null;
            }
        });
        return outs;
    }
}
