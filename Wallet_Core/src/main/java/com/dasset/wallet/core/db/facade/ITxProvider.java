package com.dasset.wallet.core.db.facade;

import com.dasset.wallet.core.In;
import com.dasset.wallet.core.Out;
import com.dasset.wallet.core.Tx;

import java.util.List;

public interface ITxProvider {

    List<Tx> getTxAndDetailByAddress(String address);

    List<Tx> getTxAndDetailByAddress(String address, int page);

    List<Tx> getPublishedTxes();

    Tx getTxDetailByTxHash(byte[] txHash);

    long sentFromAddress(byte[] txHash, String address);

    boolean isExist(byte[] txHash);

    void add(Tx txItem);

    void addTxs(List<Tx> txItems);

    void remove(byte[] txHash);

    boolean isAddressContainsTx(String address, Tx txItem);

    boolean isTxDoubleSpendWithConfirmedTx(Tx tx);

    List<String> getInAddresses(Tx tx);

    void confirmTx(int blockNo, List<byte[]> txHashes);

    void unConfirmTxByBlockNo(int blockNo);

    List<Tx> getUnspendTxWithAddress(String address);

    List<Tx> getUnspendTxWithAddress(String address, List<Out> unSpentOuts);
    
//    List<Out> getUnspendOutWithAddress(String address);

    List<Out> getUnspentOutputByBlockNo(long BlockNo, String address);

    Out getTxPreOut(byte[] txHash, int OutSn);

    // for calculate balance
    long getConfirmedBalanceWithAddress(String address);

    List<Tx> getUnconfirmedTxWithAddress(String address);

    int txCount(String address);

    long totalReceive(String address);

    void txSentBySelfHasSaw(byte[] txHash);

    List<Out> getOuts();

//    List<In> getRelatedIn(String address);

    List<Tx> getRecentlyTxsByAddress(String address, int greateThanBlockNo, int limit);

    // do not check tx 's dependency fo now
//    HashMap<Sha256Hash, Tx> getTxDependencies(Tx txItem);

    // complete in signature
    void completeInSignature(List<In> ins);

    int needCompleteInSignature(String address);

    byte[] isIdentify(Tx tx);

    void clearAllTx();
}
