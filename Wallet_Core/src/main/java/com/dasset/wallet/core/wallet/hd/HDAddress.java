package com.dasset.wallet.core.wallet.hd;

import com.dasset.wallet.core.In;
import com.dasset.wallet.core.Out;
import com.dasset.wallet.core.OutPoint;
import com.dasset.wallet.core.Tx;
import com.dasset.wallet.core.contant.PathType;
import com.dasset.wallet.core.db.BaseDb;
import com.dasset.wallet.core.utils.Utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class HDAddress {

    private String   address;
    private byte[]   publicKey;
    private int      index;
    private PathType pathType;
    private boolean  hasSyncedCompleted;
    private boolean  isIssued;
    private long     balance;
    private int      hdAccountId;

    public HDAddress(byte[] publicKey, PathType pathType, int index, boolean hasSyncedCompleted, int hdAccountId) {
        this(Utils.toAddress(Utils.sha256hash160(publicKey)), publicKey, pathType, index, false, hasSyncedCompleted, hdAccountId);
    }

    public HDAddress(String address, byte[] publicKey, PathType pathType, int index, boolean isIssued, boolean hasSyncedCompleted, int hdAccountId) {
        this.publicKey = publicKey;
        this.address = address;
        this.pathType = pathType;
        this.index = index;
        this.isIssued = isIssued;
        this.hasSyncedCompleted = hasSyncedCompleted;
        this.hdAccountId = hdAccountId;
    }

    public String getAddress() {
        return address;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public int getIndex() {
        return index;
    }

    public PathType getPathType() {
        return pathType;
    }

    public boolean isIssued() {
        return isIssued;
    }

    public boolean hasSyncedCompleted() {
        return hasSyncedCompleted;
    }

    public void setIssued(boolean isIssued) {
        this.isIssued = isIssued;
    }

    public void setSyncedCompleted(boolean isSynced) {
        this.hasSyncedCompleted = isSynced;
    }


    public int getHdAccountId() {
        return hdAccountId;
    }

    public void setHdAccountId(int hdAccountId) {
        this.hdAccountId = hdAccountId;
    }

    public long getBalance() {
        this.balance = BaseDb.iTxProvider.getConfirmedBalanceWithAddress(getAddress())
                + this.calculateUnconfirmedBalance();
        return balance;
    }

    private long calculateUnconfirmedBalance() {
        long balance = 0;

        List<Tx> txs = BaseDb.iTxProvider.getUnconfirmedTxWithAddress(this.address);
        Collections.sort(txs);

        Set<byte[]>   invalidTx  = new HashSet<byte[]>();
        Set<OutPoint> spentOut   = new HashSet<OutPoint>();
        Set<OutPoint> unspendOut = new HashSet<OutPoint>();

        for (int i = txs.size() - 1; i >= 0; i--) {
            Set<OutPoint> spent = new HashSet<OutPoint>();
            Tx            tx    = txs.get(i);

            Set<byte[]> inHashes = new HashSet<byte[]>();
            for (In in : tx.getIns()) {
                spent.add(new OutPoint(in.getPrevTxHash(), in.getPrevOutSn()));
                inHashes.add(in.getPrevTxHash());
            }

            if (tx.getBlockNo() == Tx.TX_UNCONFIRMED
                    && (Utils.isIntersects(spent, spentOut) || Utils.isIntersects(inHashes, invalidTx))) {
                invalidTx.add(tx.getTxHash());
                continue;
            }

            spentOut.addAll(spent);
            for (Out out : tx.getOuts()) {
                if (Utils.compareString(this.getAddress(), out.getOutAddress())) {
                    unspendOut.add(new OutPoint(tx.getTxHash(), out.getOutSn()));
                    balance += out.getOutValue();
                }
            }
            spent.clear();
            spent.addAll(unspendOut);
            spent.retainAll(spentOut);
            for (OutPoint o : spent) {
                Tx tx1 = BaseDb.iTxProvider.getTxDetailByTxHash(o.getTxHash());
                unspendOut.remove(o);
                for (Out out : tx1.getOuts()) {
                    if (out.getOutSn() == o.getOutSn()) {
                        balance -= out.getOutValue();
                    }
                }
            }
        }
        return balance;
    }

}
