package com.dasset.wallet.core.wallet.hd;

import com.dasset.wallet.components.constant.Regex;
import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.core.Address;
import com.dasset.wallet.core.BlockChain;
import com.dasset.wallet.core.BloomFilter;
import com.dasset.wallet.core.In;
import com.dasset.wallet.core.Out;
import com.dasset.wallet.core.OutPoint;
import com.dasset.wallet.core.Tx;
import com.dasset.wallet.core.TxBuilder;
import com.dasset.wallet.core.contant.AbstractApp;
import com.dasset.wallet.core.contant.PathType;
import com.dasset.wallet.core.contant.SigHash;
import com.dasset.wallet.core.contant.SplitCoin;
import com.dasset.wallet.core.crypto.ECKey;
import com.dasset.wallet.core.crypto.EncryptedData;
import com.dasset.wallet.core.crypto.TransactionSignature;
import com.dasset.wallet.core.crypto.hd.DeterministicKey;
import com.dasset.wallet.core.crypto.hd.HDKeyDerivation;
import com.dasset.wallet.core.crypto.mnemonic.MnemonicCode;
import com.dasset.wallet.core.crypto.mnemonic.MnemonicException;
import com.dasset.wallet.core.db.AbstractDb;
import com.dasset.wallet.core.exception.KeyCrypterException;
import com.dasset.wallet.core.exception.PasswordException;
import com.dasset.wallet.core.exception.TxBuilderException;
import com.dasset.wallet.core.script.ScriptBuilder;
import com.dasset.wallet.core.utils.PrivateKeyUtil;
import com.dasset.wallet.core.utils.Utils;
import com.dasset.wallet.core.wallet.hd.AbstractHD;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HDAccount extends Address {

    public static final String HDAccountPlaceHolder          = "HDAccount";
    public static final String HDAccountMonitoredPlaceHolder = "HDAccountMonitored";
    public static final int    MaxUnusedNewAddressCount      = 20;

    public interface HDAccountGenerationDelegate {
        void onHDAccountGenerationProgress(double progress);
    }

    private static final double GenerationPreStartProgress = 0.01;

    private static final int LOOK_AHEAD_SIZE = 100;

    private long balance = 0;

    protected transient byte[] mnemonicSeed;
    protected transient byte[] hdSeed;
    protected int hdSeedId = -1;
    protected boolean isFromXRandom;
    private   boolean hasSeed;

    public HDAccount(MnemonicCode mnemonicCode, byte[] mnemonicSeed, CharSequence password) throws MnemonicException.MnemonicLengthException {
        this(mnemonicCode, mnemonicSeed, password, true);
    }

    public HDAccount(MnemonicCode mnemonicCode, byte[] mnemonicSeed, CharSequence password, boolean isSyncedComplete) throws MnemonicException.MnemonicLengthException {
        super();
        this.mnemonicSeed = mnemonicSeed;
        hdSeed = seedFromMnemonic(mnemonicCode, mnemonicSeed);
        DeterministicKey master                = HDKeyDerivation.createMasterPrivateKey(hdSeed);
        EncryptedData    encryptedHDSeed       = new EncryptedData(hdSeed, password, isFromXRandom);
        EncryptedData    encryptedMnemonicSeed = new EncryptedData(mnemonicSeed, password, isFromXRandom);
        DeterministicKey account               = getAccount(master);
        account.clearPrivateKey();
        initHDAccount(account, encryptedMnemonicSeed, encryptedHDSeed, isFromXRandom, isSyncedComplete, null);
    }

    public HDAccount(MnemonicCode mnemonicCode, SecureRandom secureRandom, CharSequence password, HDAccountGenerationDelegate hdAccountGenerationDelegate) throws MnemonicException.MnemonicLengthException {
        isFromXRandom = secureRandom.getClass().getCanonicalName().contains("XRandom");
        LogUtil.getInstance().print("--------------1:" + password);
        mnemonicSeed = new byte[16];
        LogUtil.getInstance().print("--------------2");
        secureRandom.nextBytes(mnemonicSeed);
        LogUtil.getInstance().print("--------------3");
        hdSeed = seedFromMnemonic(mnemonicCode, mnemonicSeed);
        LogUtil.getInstance().print("--------------4:" + hdSeed);
        EncryptedData encryptedHDSeed = new EncryptedData(hdSeed, password, isFromXRandom);
        LogUtil.getInstance().print("--------------5" + mnemonicSeed);
        EncryptedData encryptedMnemonicSeed = new EncryptedData(mnemonicSeed, password, isFromXRandom);
        LogUtil.getInstance().print("--------------6");
        DeterministicKey master = HDKeyDerivation.createMasterPrivateKey(hdSeed);
        LogUtil.getInstance().print("--------------7");
        DeterministicKey account = getAccount(master);
        LogUtil.getInstance().print("--------------8");
        account.clearPrivateKey();
        LogUtil.getInstance().print("--------------9");
        initHDAccount(account, encryptedMnemonicSeed, encryptedHDSeed, isFromXRandom, true, hdAccountGenerationDelegate);
    }

    public HDAccount(MnemonicCode mnemonicCode, EncryptedData encryptedMnemonicSeed, CharSequence password, boolean isSyncedComplete) throws MnemonicException.MnemonicLengthException {
        mnemonicSeed = encryptedMnemonicSeed.decrypt(password);
        hdSeed = seedFromMnemonic(mnemonicCode, mnemonicSeed);
        isFromXRandom = encryptedMnemonicSeed.isXRandom();
        EncryptedData    encryptedHDSeed = new EncryptedData(hdSeed, password, isFromXRandom);
        DeterministicKey master          = HDKeyDerivation.createMasterPrivateKey(hdSeed);
        DeterministicKey account         = getAccount(master);
        account.clearPrivateKey();
        initHDAccount(account, encryptedMnemonicSeed, encryptedHDSeed, isFromXRandom, isSyncedComplete, null);
    }

    public HDAccount(byte[] accountExtentedPub) throws MnemonicException.MnemonicLengthException {
        this(accountExtentedPub, false);
    }

    public HDAccount(byte[] accountExtentedPub, boolean isFromXRandom) throws MnemonicException.MnemonicLengthException {
        this(accountExtentedPub, isFromXRandom, true, null);
    }


    public HDAccount(byte[] accountExtentedPub, boolean isFromXRandom, boolean isSyncedComplete, HDAccountGenerationDelegate generationDelegate) throws
            MnemonicException.MnemonicLengthException {
        super();
        this.isFromXRandom = isFromXRandom;
        initHDAccount(HDKeyDerivation.createMasterPubKeyFromExtendedBytes(accountExtentedPub), null, null, isFromXRandom, isSyncedComplete, generationDelegate);
    }

    private void initHDAccount(DeterministicKey accountKey, EncryptedData encryptedMnemonicSeed, EncryptedData encryptedHDSeed, boolean isFromXRandom, boolean isSyncedComplete, HDAccountGenerationDelegate generationDelegate) {
        LogUtil.getInstance().i("------------------------------------", "initHDAccount start");
        LogUtil.getInstance().i("------>path:", accountKey.getPathAsString());
//        LogUtil.getInstance().i("------>serializePrivB58:", accountKey.serializePrivB58());
        LogUtil.getInstance().i("------>serializePubB58:", accountKey.serializePubB58());
        LogUtil.getInstance().i("------>toAddress:", accountKey.toAddress());
        LogUtil.getInstance().i("------>encryptedMnemonicSeed isCompressed:", String.valueOf(encryptedMnemonicSeed.isCompressed()));
        LogUtil.getInstance().i("------>encryptedMnemonicSeed isXRandom:", String.valueOf(encryptedMnemonicSeed.isXRandom()));
        LogUtil.getInstance().i("------>encryptedMnemonicSeed EncryptedString:", encryptedMnemonicSeed.toEncryptedString());
        LogUtil.getInstance().i("------>encryptedHDSeed isCompressed:", String.valueOf(encryptedHDSeed.isCompressed()));
        LogUtil.getInstance().i("------>encryptedHDSeed isXRandom:", String.valueOf(encryptedHDSeed.isXRandom()));
        LogUtil.getInstance().i("------>encryptedHDSeed EncryptedString:", encryptedHDSeed.toEncryptedString());
        LogUtil.getInstance().i("------>isXRandom:", String.valueOf(isFromXRandom));
        LogUtil.getInstance().i("------>hasSyncedCompleted:", String.valueOf(isSyncedComplete));
        LogUtil.getInstance().i("------------------------------------", "initHDAccount end");
        this.isFromXRandom = isFromXRandom;
        double progress = 0;
        if (generationDelegate != null) {
            generationDelegate.onHDAccountGenerationProgress(progress);
        }
        String address = null;
        if (encryptedMnemonicSeed != null && mnemonicSeed != null) {
            ECKey k = new ECKey(mnemonicSeed, null);
            address = k.toAddress();
            k.clearPrivateKey();
        }

        DeterministicKey internalKey = getChainRootKey(accountKey, PathType.INTERNAL_ROOT_PATH);
        DeterministicKey externalKey = getChainRootKey(accountKey, PathType.EXTERNAL_ROOT_PATH);
//        if (checkDuplicated(externalKey.getPubKeyExtended(), internalKey.getPubKeyExtended())) {
//            throw new DuplicatedHDAccountException();
//        }
        DeterministicKey key          = externalKey.deriveSoftened(0);
        String           firstAddress = key.toAddress();
        accountKey.wipe();

        progress += GenerationPreStartProgress;
        if (generationDelegate != null) {
            generationDelegate.onHDAccountGenerationProgress(progress);
        }

        double itemProgress = (1.0 - GenerationPreStartProgress) / (LOOK_AHEAD_SIZE * 2);

        List<HDAddress> externalAddresses = Lists.newArrayList();
        List<HDAddress> internalAddresses = Lists.newArrayList();
        for (int i = 0; i < LOOK_AHEAD_SIZE; i++) {
            byte[]    subExternalPub  = externalKey.deriveSoftened(i).getPubKey();
            HDAddress externalAddress = new HDAddress(subExternalPub, PathType.EXTERNAL_ROOT_PATH, i, isSyncedComplete, hdSeedId);
            externalAddresses.add(externalAddress);
            progress += itemProgress;
            if (generationDelegate != null) {
                generationDelegate.onHDAccountGenerationProgress(progress);
            }

            byte[]    subInternalPub  = internalKey.deriveSoftened(i).getPubKey();
            HDAddress internalAddress = new HDAddress(subInternalPub, PathType.INTERNAL_ROOT_PATH, i, isSyncedComplete, hdSeedId);
            internalAddresses.add(internalAddress);
            progress += itemProgress;
            if (generationDelegate != null) {
                generationDelegate.onHDAccountGenerationProgress(progress);
            }
        }
        if (encryptedMnemonicSeed == null) {
            hdSeedId = AbstractDb.hdAccountProvider.addMonitoredHDAccount(firstAddress, isFromXRandom, externalKey.getPubKeyExtended(), internalKey.getPubKeyExtended());
            hasSeed = false;
        } else {
            hdSeedId = AbstractDb.hdAccountProvider.addHDAccount(encryptedMnemonicSeed.toEncryptedString(), encryptedHDSeed.toEncryptedString(), firstAddress, isFromXRandom, address, externalKey.getPubKeyExtended(), internalKey.getPubKeyExtended());
            hasSeed = true;
        }
        for (HDAddress hdAddress : externalAddresses) {
            hdAddress.setHdAccountId(hdSeedId);
        }
        for (HDAddress hdAddress : internalAddresses) {
            hdAddress.setHdAccountId(hdSeedId);
        }
        AbstractDb.hdAccountAddressProvider.addAddress(externalAddresses);
        AbstractDb.hdAccountAddressProvider.addAddress(internalAddresses);
        internalKey.wipe();
        externalKey.wipe();
    }

    public HDAccount(int seedId) {
        this.hdSeedId = seedId;
        this.isFromXRandom = AbstractDb.hdAccountProvider.hdAccountIsXRandom(seedId);
        hasSeed = AbstractDb.hdAccountProvider.hasMnemonicSeed(this.hdSeedId);
        updateBalance();
    }

    @Override
    public String getFullEncryptPrivKey() {
        if (!hasPrivKey()) {
            return null;
        }
        String encryptPrivKey = getEncryptedMnemonicSeed();
        return PrivateKeyUtil.getFullencryptHDMKeyChain(isFromXRandom, encryptPrivKey);
    }

    public String getQRCodeFullEncryptPrivKey() {
        if (!hasPrivKey()) {
            return null;
        }
        return MnemonicCode.getInstance().getMnemonicDictionary().getHDQRCodeFlag() + getFullEncryptPrivKey();
    }

    public byte[] getInternalPub() {
        return AbstractDb.hdAccountProvider.getInternalPub(hdSeedId);
    }

    public byte[] getExternalPub() {
        return AbstractDb.hdAccountProvider.getExternalPub(hdSeedId);
    }

    public String getFirstAddressFromDb() {
        return AbstractDb.hdAccountProvider.getHDFirstAddress(hdSeedId);
    }

    public void supplyEnoughKeys(boolean isSyncedComplete) {
        int lackOfExternal = issuedExternalIndex() + 1 + LOOK_AHEAD_SIZE -
                allGeneratedExternalAddressCount();
        if (lackOfExternal > 0) {
            supplyNewExternalKey(lackOfExternal, isSyncedComplete);
        }

        int lackOfInternal = issuedInternalIndex() + 1 + LOOK_AHEAD_SIZE -
                allGeneratedInternalAddressCount();
        if (lackOfInternal > 0) {
            supplyNewInternalKey(lackOfInternal, isSyncedComplete);
        }
    }

    private void supplyNewInternalKey(int count, boolean isSyncedComplete) {
        DeterministicKey     root        = HDKeyDerivation.createMasterPubKeyFromExtendedBytes(getInternalPub());
        int                  firstIndex  = allGeneratedInternalAddressCount();
        ArrayList<HDAddress> hdAddresses = Lists.newArrayList();
        for (int i = firstIndex; i < firstIndex + count; i++) {
            hdAddresses.add(new HDAddress(root.deriveSoftened(i).getPubKey(), PathType.INTERNAL_ROOT_PATH, i, isSyncedComplete, hdSeedId));
        }
        AbstractDb.hdAccountAddressProvider.addAddress(hdAddresses);
        LogUtil.getInstance().print(String.format("HD supplied {%s} internal addresses", hdAddresses.size()));
    }

    private void supplyNewExternalKey(int count, boolean isSyncedComplete) {
        DeterministicKey     root        = HDKeyDerivation.createMasterPubKeyFromExtendedBytes(getExternalPub());
        int                  firstIndex  = allGeneratedExternalAddressCount();
        ArrayList<HDAddress> hdAddresses = Lists.newArrayList();
        for (int i = firstIndex; i < firstIndex + count; i++) {
            hdAddresses.add(new HDAddress(root.deriveSoftened(i).getPubKey(), PathType
                    .EXTERNAL_ROOT_PATH, i, isSyncedComplete, hdSeedId));
        }
        AbstractDb.hdAccountAddressProvider.addAddress(hdAddresses);
        LogUtil.getInstance().print(String.format("HD supplied {%s} addresses addresses", hdAddresses.size()));
    }

    protected String getEncryptedMnemonicSeed() {
        if (!hasPrivKey()) {
            return null;
        }
        return AbstractDb.hdAccountProvider.getHDAccountEncryptMnemonicSeed(hdSeedId);
    }

    protected String getEncryptedHDSeed() {
        if (!hasPrivKey()) {
            return null;
        }
        return AbstractDb.hdAccountProvider.getHDAccountEncryptSeed(hdSeedId);
    }

    @Override
    public String getAddress() {
        return AbstractDb.hdAccountAddressProvider.externalAddress(this.hdSeedId);
    }

    @Override
    public String getShortAddress() {
        return Utils.shortenAddress(getAddress());
    }

    public int issuedInternalIndex() {
        return AbstractDb.hdAccountAddressProvider.issuedIndex(this.hdSeedId, PathType.INTERNAL_ROOT_PATH);
    }

    public int issuedExternalIndex() {
        return AbstractDb.hdAccountAddressProvider.issuedIndex(this.hdSeedId, PathType.EXTERNAL_ROOT_PATH);
    }

    private int allGeneratedInternalAddressCount() {
        return AbstractDb.hdAccountAddressProvider.allGeneratedAddressCount(this.hdSeedId, PathType.INTERNAL_ROOT_PATH);
    }

    private int allGeneratedExternalAddressCount() {
        return AbstractDb.hdAccountAddressProvider.allGeneratedAddressCount(this.hdSeedId, PathType.EXTERNAL_ROOT_PATH);
    }

    public HDAddress addressForPath(PathType type, int index) {
        assert index < (type == PathType.EXTERNAL_ROOT_PATH ? allGeneratedExternalAddressCount() : allGeneratedInternalAddressCount());
        return AbstractDb.hdAccountAddressProvider.addressForPath(this.hdSeedId, type, index);
    }

    public boolean requestNewReceivingAddress() {
        boolean result = AbstractDb.hdAccountAddressProvider.requestNewReceivingAddress(this.hdSeedId);
        if (result) {
            supplyEnoughKeys(true);
        }
        return result;
    }

    public void onNewTx(Tx tx, Tx.TxNotificationType txNotificationType) {
        supplyEnoughKeys(true);
        long deltaBalance = getDeltaBalance();
        AbstractApp.notificationService.notificatTx(hasPrivKey() ? HDAccountPlaceHolder : HDAccountMonitoredPlaceHolder, tx, txNotificationType, deltaBalance);
    }

    public boolean isTxRelated(Tx tx, List<String> inAddresses) {
        return getRelatedAddressesForTx(tx, inAddresses).size() > 0;
    }

    @Override
    public boolean initTxs(List<Tx> txs) {
        AbstractDb.txProvider.addTxs(txs);
        notificatTx(null, Tx.TxNotificationType.txFromApi);
        return true;
    }

    @Override
    public void notificatTx(Tx tx, Tx.TxNotificationType txNotificationType) {
        long deltaBalance = getDeltaBalance();
        AbstractApp.notificationService.notificatTx(hasPrivKey() ? HDAccountPlaceHolder : HDAccountMonitoredPlaceHolder, tx, txNotificationType, deltaBalance);
    }

    private long getDeltaBalance() {
        long oldBalance = this.balance;
        this.updateBalance();
        return this.balance - oldBalance;
    }

    @Override
    public List<Tx> getTxs(int page) {
        return AbstractDb.hdAccountAddressProvider.getTxAndDetailByHDAccount(this.hdSeedId, page);
    }

    @Override
    public List<Tx> getTxs() {
        return AbstractDb.hdAccountAddressProvider.getTxAndDetailByHDAccount(this.hdSeedId);
    }

    @Override
    public int txCount() {
        return AbstractDb.hdAccountAddressProvider.hdAccountTxCount(this.hdSeedId);
    }

    @Override
    public void updateBalance() {
        this.balance = AbstractDb.hdAccountAddressProvider.getHDAccountConfirmedBalance(hdSeedId)
                + calculateUnconfirmedBalance();
    }

    private long calculateUnconfirmedBalance() {
        long balance = 0;

        List<Tx> txs = AbstractDb.hdAccountAddressProvider.getHDAccountUnconfirmedTx(this.hdSeedId);
        Collections.sort(txs);

        Set<byte[]>   invalidTx  = Sets.newHashSet();
        Set<OutPoint> spentOut   = Sets.newHashSet();
        Set<OutPoint> unspendOut = Sets.newHashSet();

        for (int i = txs.size() - 1; i >= 0; i--) {
            Set<OutPoint> spent    = Sets.newHashSet();
            Tx            tx       = txs.get(i);
            Set<byte[]>   inHashes = Sets.newHashSet();
            for (In in : tx.getIns()) {
                spent.add(new OutPoint(in.getPrevTxHash(), in.getPrevOutSn()));
                inHashes.add(in.getPrevTxHash());
            }
            if (tx.getBlockNo() == Tx.TX_UNCONFIRMED && (Utils.isIntersects(spent, spentOut) || Utils.isIntersects(inHashes, invalidTx))) {
                invalidTx.add(tx.getTxHash());
                continue;
            }
            spentOut.addAll(spent);
            HashSet<String> addressSet = getBelongAccountAddresses(tx.getOutAddressList());
            for (Out out : tx.getOuts()) {
                if (addressSet.contains(out.getOutAddress())) {
                    unspendOut.add(new OutPoint(tx.getTxHash(), out.getOutSn()));
                    balance += out.getOutValue();
                }
            }
            spent.clear();
            spent.addAll(unspendOut);
            spent.retainAll(spentOut);
            for (OutPoint o : spent) {
                Tx tx1 = AbstractDb.txProvider.getTxDetailByTxHash(o.getTxHash());
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

    public List<HDAddress> getRelatedAddressesForTx(Tx tx, List<String> inAddresses) {
        List<String>    outAddressList       = Lists.newArrayList();
        List<HDAddress> hdAccountAddressList = Lists.newArrayList();
        for (Out out : tx.getOuts()) {
            String outAddress = out.getOutAddress();
            outAddressList.add(outAddress);
        }
        List<HDAddress> belongAccountOfOutList = AbstractDb.hdAccountAddressProvider
                .belongAccount(this.hdSeedId, outAddressList);
        if (belongAccountOfOutList != null
                && belongAccountOfOutList.size() > 0) {
            hdAccountAddressList.addAll(belongAccountOfOutList);
        }
        List<HDAddress> belongAccountOfInList = getAddressFromIn(inAddresses);
        if (belongAccountOfInList != null && belongAccountOfInList.size() > 0) {
            hdAccountAddressList.addAll(belongAccountOfInList);
        }
        return hdAccountAddressList;
    }

    public HashSet<String> getBelongAccountAddresses(List<String> addressList) {
        return AbstractDb.hdAccountAddressProvider.getBelongAccountAddresses(this.hdSeedId, addressList);
    }

    public Tx newTx(String toAddress, Long amount, CharSequence password) throws TxBuilderException, MnemonicException.MnemonicLengthException {
        return newTx(new String[]{toAddress}, new Long[]{amount}, password);
    }

    public Tx newTx(String[] toAddresses, Long[] amounts, CharSequence password) throws TxBuilderException, MnemonicException.MnemonicLengthException {
        if (password != null && !hasPrivKey()) {
            throw new RuntimeException("Can not sign without private key");
        }
        Tx              tx               = newTx(toAddresses, amounts);
        List<HDAddress> signingAddresses = getSigningAddressesForInputs(tx.getIns());
        assert signingAddresses.size() == tx.getIns().size();

        DeterministicKey master = masterKey(password);
        if (master == null) {
            return null;
        }
        DeterministicKey accountKey = getAccount(master);
        DeterministicKey external   = getChainRootKey(accountKey, PathType.EXTERNAL_ROOT_PATH);
        DeterministicKey internal   = getChainRootKey(accountKey, PathType.INTERNAL_ROOT_PATH);
        accountKey.wipe();
        master.wipe();
        List<byte[]> unsignedHashes = tx.getUnsignedInHashes();
        assert unsignedHashes.size() == signingAddresses.size();
        ArrayList<byte[]>                 signatures      = new ArrayList<byte[]>();
        HashMap<String, DeterministicKey> addressToKeyMap = Maps.newHashMapWithExpectedSize(signingAddresses.size());
        for (int i = 0; i < signingAddresses.size(); i++) {
            HDAddress hdAddress = signingAddresses.get(i);
            if (!addressToKeyMap.containsKey(hdAddress.getAddress())) {
                if (hdAddress.getPathType() == PathType.EXTERNAL_ROOT_PATH) {
                    addressToKeyMap.put(hdAddress.getAddress(), external.deriveSoftened(hdAddress.getIndex()));
                } else {
                    addressToKeyMap.put(hdAddress.getAddress(), internal.deriveSoftened(hdAddress.getIndex()));
                }
            }
            DeterministicKey deterministicKey = addressToKeyMap.get(hdAddress.getAddress());
            assert deterministicKey != null;
            TransactionSignature signature = new TransactionSignature(deterministicKey.sign(unsignedHashes.get(i), null), SigHash.ALL, false);
            signatures.add(ScriptBuilder.createInputScript(signature, deterministicKey).getProgram());
        }
        tx.signWithSignatures(signatures);
        assert tx.verifySignatures();
        external.wipe();
        internal.wipe();
        for (DeterministicKey key : addressToKeyMap.values()) {
            key.wipe();
        }
        return tx;
    }

    public List<Tx> newForkTx(String toAddresses, Long amounts, CharSequence password, SplitCoin splitCoin, String... blockHash) throws TxBuilderException, MnemonicException.MnemonicLengthException {
        if (password != null && !hasPrivKey()) {
            throw new RuntimeException("Can not sign without private key");
        }
        List<Tx> txs = newForkTx(toAddresses, amounts, splitCoin);
        for (Tx tx : txs) {
            if (blockHash != null && blockHash.length > 0) {
                tx.setBlockHash(Utils.hexStringToByteArray(blockHash[0]));
            }
            List<HDAddress> signingAddresses = getSigningAddressesForInputs(tx.getIns());
            assert signingAddresses.size() == tx.getIns().size();
            DeterministicKey master = masterKey(password);
            if (master == null) {
                return null;
            }
            DeterministicKey accountKey = getAccount(master);
            DeterministicKey external   = getChainRootKey(accountKey, PathType.EXTERNAL_ROOT_PATH);
            DeterministicKey internal   = getChainRootKey(accountKey, PathType.INTERNAL_ROOT_PATH);
            accountKey.wipe();
            master.wipe();
            List<byte[]> unsignedHashes = tx.getSplitCoinForkUnsignedInHashes(splitCoin);
            assert unsignedHashes.size() == signingAddresses.size();
            ArrayList<byte[]>                 signatures      = Lists.newArrayList();
            HashMap<String, DeterministicKey> addressToKeyMap = Maps.newHashMapWithExpectedSize(signingAddresses.size());
            for (int i = 0; i < signingAddresses.size(); i++) {
                HDAddress hdAddress = signingAddresses.get(i);
                if (!addressToKeyMap.containsKey(hdAddress.getAddress())) {
                    if (hdAddress.getPathType() == PathType.EXTERNAL_ROOT_PATH) {
                        addressToKeyMap.put(hdAddress.getAddress(), external.deriveSoftened(hdAddress.getIndex()));
                    } else {
                        addressToKeyMap.put(hdAddress.getAddress(), internal.deriveSoftened(hdAddress.getIndex()));
                    }
                }
                DeterministicKey deterministicKey = addressToKeyMap.get(hdAddress.getAddress());
                assert deterministicKey != null;
                TransactionSignature signature = new TransactionSignature(deterministicKey.sign(unsignedHashes.get(i), null), splitCoin.getSigHash(), false);
                signatures.add(ScriptBuilder.createInputScript(signature, deterministicKey).getProgram());
            }
            tx.signWithSignatures(signatures);
            assert tx.verifySignatures();
            external.wipe();
            internal.wipe();
            for (DeterministicKey key : addressToKeyMap.values()) {
                key.wipe();
            }
        }
        return txs;
    }

    public List<Tx> extractBcc(String toAddresses, Long amounts, List<Out> outs, PathType path, int index, CharSequence password) throws
            TxBuilderException, MnemonicException.MnemonicLengthException {
        if (password != null && !hasPrivKey()) {
            throw new RuntimeException("Can not sign without private key");
        }
        List<Tx> txs = newForkTx(toAddresses, amounts, outs, SplitCoin.BCC);
        for (Tx tx : txs) {
            DeterministicKey master = masterKey(password);
            if (master == null) {
                return null;
            }
            long[] preOutValue = new long[outs.size()];
            for (int idx = 0; idx < outs.size(); idx++) {
                preOutValue[idx] = outs.get(idx).getOutValue();
            }
            List<byte[]> unsignedHashes = tx.getUnsignedHashesForBcc(preOutValue);
            assert unsignedHashes.size() == tx.getIns().size();
            ArrayList<byte[]> signatures = Lists.newArrayList();
            for (int i = 0; i < tx.getIns().size(); i++) {
                byte[]           unsigned    = unsignedHashes.get(i);
                DeterministicKey xPrivate    = getAccount(master);
                DeterministicKey pathPrivate = xPrivate.deriveSoftened(path.getType());
                DeterministicKey key         = pathPrivate.deriveSoftened(index);
                pathPrivate.wipe();
                assert key != null;
                TransactionSignature signature = new TransactionSignature(key.sign(unsigned, null), SigHash.BCCFORK, false);
                signatures.add(ScriptBuilder.createInputScript(signature, key).getProgram());
                master.wipe();
                key.wipe();
            }
            tx.signWithSignatures(signatures);
            assert tx.verifySignatures();
        }
        return txs;
    }

    public List<Tx> newForkTx(String toAddress, Long amount, List<Out> outs, SplitCoin splitCoin) throws TxBuilderException, MnemonicException.MnemonicLengthException {
        return TxBuilder.getInstance().buildSplitCoinTxsFromAllAddress(outs, toAddress, Arrays.asList(amount), Arrays.asList(toAddress), splitCoin);
    }

    public List<Tx> newForkTx(String toAddress, Long amount, SplitCoin splitCoin) throws TxBuilderException, MnemonicException.MnemonicLengthException {
        return TxBuilder.getInstance().buildSplitCoinTxsFromAllAddress(AbstractDb.hdAccountAddressProvider.getUnspentOutputByBlockNo(splitCoin.getForkBlockHeight(), hdSeedId), toAddress, Arrays.asList(amount), Arrays.asList(toAddress), splitCoin);
    }

    public Tx newTx(String toAddress, Long amount) throws TxBuilderException, MnemonicException.MnemonicLengthException {
        return newTx(new String[]{toAddress}, new Long[]{amount});
    }


    public Tx newTx(String[] toAddresses, Long[] amounts) throws TxBuilderException, MnemonicException.MnemonicLengthException {
        return TxBuilder.getInstance().buildTxFromAllAddress(AbstractDb.hdAccountAddressProvider.getUnspendOutByHDAccount(hdSeedId), getNewChangeAddress(), Arrays.asList(amounts), Arrays.asList(toAddresses));
    }

    public List<HDAddress> getSigningAddressesForInputs(List<In> inputs) {
        return AbstractDb.hdAccountAddressProvider.getSigningAddressesForInputs(this.hdSeedId, inputs);
    }

    public boolean isSendFromMe(List<String> addresses) {
        return getAddressFromIn(addresses).size() > 0;
    }

    private List<HDAddress> getAddressFromIn(List<String> addresses) {
        return AbstractDb.hdAccountAddressProvider.belongAccount(this.hdSeedId, addresses);
    }

    public void updateIssuedInternalIndex(int index) {
        AbstractDb.hdAccountAddressProvider.updateIssuedIndex(this.hdSeedId, PathType.INTERNAL_ROOT_PATH, index);
    }

    public void updateIssuedExternalIndex(int index) {
        AbstractDb.hdAccountAddressProvider.updateIssuedIndex(this.hdSeedId, PathType.EXTERNAL_ROOT_PATH, index);
    }

    private String getNewChangeAddress() {
        return addressForPath(PathType.INTERNAL_ROOT_PATH, issuedInternalIndex() + 1).getAddress();
    }

    public void updateSyncComplete(HDAddress accountAddress) {
        AbstractDb.hdAccountAddressProvider.updateSyncdComplete(this.hdSeedId, accountAddress);
    }

    public int elementCountForBloomFilter() {
        return allGeneratedExternalAddressCount() * 2 + AbstractDb.hdAccountAddressProvider.getUnspendOutCountByHDAccountWithPath(getHdSeedId(), PathType.INTERNAL_ROOT_PATH) + AbstractDb.hdAccountAddressProvider.getUnconfirmedSpentOutCountByHDAccountWithPath(getHdSeedId(), PathType.INTERNAL_ROOT_PATH);
    }

    public void addElementsForBloomFilter(BloomFilter filter) {
        List<byte[]> pubs = AbstractDb.hdAccountAddressProvider.getPubs(this.hdSeedId, PathType.EXTERNAL_ROOT_PATH);
        for (byte[] pub : pubs) {
            filter.insert(pub);
            filter.insert(Utils.sha256hash160(pub));
        }
        for (Out out : AbstractDb.hdAccountAddressProvider.getUnspendOutByHDAccountWithPath(getHdSeedId(), PathType.INTERNAL_ROOT_PATH)) {
            filter.insert(out.getOutpointData());
        }
        for (Out out : AbstractDb.hdAccountAddressProvider.getUnconfirmedSpentOutByHDAccountWithPath(getHdSeedId(), PathType.INTERNAL_ROOT_PATH)) {
            filter.insert(out.getOutpointData());
        }
    }

    @Override
    public long getBalance() {
        return balance;
    }

    @Override
    public boolean isSyncComplete() {
        return AbstractDb.hdAccountAddressProvider.unSyncedAddressCount(this.hdSeedId) == 0;
    }

    @Override
    public List<Tx> getRecentlyTxsWithConfirmationCntLessThan(int confirmationCnt, int limit) {
        List<Tx> txList = Lists.newArrayList();
        txList.addAll(AbstractDb.hdAccountAddressProvider.getRecentlyTxsByAccount(this.hdSeedId, BlockChain.getInstance().getLastBlock().getBlockNo() - confirmationCnt + 1, limit));
        return txList;
    }

    @Override
    public Tx buildTx(String changeAddress, List<Long> amounts, List<String> addresses) {
        throw new RuntimeException("use newTx() for hdAccountHot");
    }

    @Override
    public boolean hasPrivKey() {
        return hasSeed;
    }

    @Override
    public long getSortTime() {
        return 0;
    }

    @Override
    public String getEncryptPrivKeyOfDb() {
        return null;
    }

    @Override
    public String getFullEncryptPrivKeyOfDb() {
        return null;
    }

    protected DeterministicKey getChainRootKey(DeterministicKey accountKey, PathType pathType) {
        return accountKey.deriveSoftened(pathType.getType());
    }

    protected DeterministicKey getAccount(DeterministicKey master) {
        DeterministicKey purpose  = master.deriveHardened(44);
        DeterministicKey coinType = purpose.deriveHardened(0);
        DeterministicKey account  = coinType.deriveHardened(0);
        purpose.wipe();
        coinType.wipe();
        return account;
    }

    protected DeterministicKey masterKey(CharSequence password) throws MnemonicException.MnemonicLengthException {
        long begin = System.currentTimeMillis();
        decryptHDSeed(password);
        DeterministicKey master = HDKeyDerivation.createMasterPrivateKey(hdSeed);
        wipeHDSeed();
        LogUtil.getInstance().print(String.format("hdm keychain decrypt time: {%s}", System.currentTimeMillis() - begin));
        return master;
    }

    protected void decryptHDSeed(CharSequence password) throws MnemonicException.MnemonicLengthException {
        if (hdSeedId < 0 || password == null) {
            return;
        }
        String encryptedHDSeed = getEncryptedHDSeed();
        if (!Utils.isEmpty(encryptedHDSeed)) {
            hdSeed = new EncryptedData(encryptedHDSeed).decrypt(password);
        }
    }

    public void decryptMnemonicSeed(CharSequence password) throws KeyCrypterException {
        if (hdSeedId < 0) {
            return;
        }
        String encrypted = getEncryptedMnemonicSeed();
        LogUtil.getInstance().i("------------------------------------", "decryptMnemonicSeed start");
        LogUtil.getInstance().i("------>encrypted:", encrypted);
        if (!Utils.isEmpty(encrypted)) {
            mnemonicSeed = new EncryptedData(encrypted).decrypt(password);
            LogUtil.getInstance().i("------>mnemonicSeed:", new String(mnemonicSeed));
        }
        LogUtil.getInstance().i("------------------------------------", "decryptMnemonicSeed end");
    }

    public List<String> getSeedWords(CharSequence password) throws MnemonicException.MnemonicLengthException {
        decryptMnemonicSeed(password);
        List<String> words = MnemonicCode.getInstance().toMnemonic(mnemonicSeed);
        for (String word : words) {
            LogUtil.getInstance().i("------>word:", word);
        }
        wipeMnemonicSeed();
        return words;
    }

    public boolean checkWithPassword(CharSequence password) {
        if (!hasPrivKey()) {
            return true;
        }
        try {
            decryptHDSeed(password);
            decryptMnemonicSeed(password);
            byte[]  hdCopy           = Arrays.copyOf(hdSeed, hdSeed.length);
            boolean hdSeedSafe       = Utils.compareString(getFirstAddressFromDb(), getFirstAddressFromSeed(null));
            boolean mnemonicSeedSafe = Arrays.equals(seedFromMnemonic(MnemonicCode.getInstance(), mnemonicSeed), hdCopy);
            Utils.wipeBytes(hdCopy);
            wipeHDSeed();
            wipeMnemonicSeed();
            return hdSeedSafe && mnemonicSeedSafe;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    protected String getFirstAddressFromSeed(CharSequence password) {
        DeterministicKey key     = getExternalKey(0, password);
        String           address = Utils.toAddress(key.getPubKeyHash());
        key.wipe();
        return address;
    }

    public DeterministicKey getExternalKey(int index, CharSequence password) {
        try {
            DeterministicKey master     = masterKey(password);
            DeterministicKey accountKey = getAccount(master);
            DeterministicKey externalChainRoot = getChainRootKey(accountKey, PathType
                    .EXTERNAL_ROOT_PATH);
            DeterministicKey key = externalChainRoot.deriveSoftened(index);
            master.wipe();
            accountKey.wipe();
            externalChainRoot.wipe();
            return key;
        } catch (KeyCrypterException e) {
            throw new PasswordException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public DeterministicKey getInternalKey(int index, CharSequence password) {
        try {
            DeterministicKey master     = masterKey(password);
            DeterministicKey accountKey = getAccount(master);
            DeterministicKey externalChainRoot = getChainRootKey(accountKey, PathType
                    .INTERNAL_ROOT_PATH);
            DeterministicKey key = externalChainRoot.deriveSoftened(index);
            master.wipe();
            accountKey.wipe();
            externalChainRoot.wipe();
            return key;
        } catch (KeyCrypterException e) {
            throw new PasswordException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String xPubB58(CharSequence password) throws MnemonicException.MnemonicLengthException {
        DeterministicKey master   = masterKey(password);
        DeterministicKey purpose  = master.deriveHardened(44);
        DeterministicKey coinType = purpose.deriveHardened(0);
        DeterministicKey account  = coinType.deriveHardened(0);
        String           xpub     = account.serializePubB58();
        master.wipe();
        purpose.wipe();
        coinType.wipe();
        account.wipe();
        return xpub;
    }

    protected void wipeHDSeed() {
        if (hdSeed == null) {
            return;
        }
        Utils.wipeBytes(hdSeed);
    }

    protected void wipeMnemonicSeed() {
        if (mnemonicSeed == null) {
            return;
        }
        Utils.wipeBytes(mnemonicSeed);
    }

    public int getHdSeedId() {
        return hdSeedId;
    }

    public static final byte[] seedFromMnemonic(MnemonicCode mnemonicCode, byte[] mnemonicSeed) throws MnemonicException.MnemonicLengthException {
        return mnemonicCode.toSeed(mnemonicCode.toMnemonic(mnemonicSeed), Regex.NONE.getRegext());
    }

    @Override
    public boolean isFromXRandom() {
        return isFromXRandom;
    }

    public static final boolean checkDuplicated(byte[] ex, byte[] in) {
        return AbstractDb.hdAccountProvider.isPubExist(ex, in);
    }

    public static class DuplicatedHDAccountException extends RuntimeException {

    }

    public List<HDAddress> getHdHotAddresses(int page, PathType pathType, CharSequence password) {
        ArrayList<HDAddress> addresses = Lists.newArrayList();
        try {
            DeterministicKey master      = masterKey(password);
            DeterministicKey accountKey  = getAccount(master);
            DeterministicKey pathTypeKey = getChainRootKey(accountKey, pathType);
            for (int i = (page - 1) * 10; i < page * 10; i++) {
                DeterministicKey key       = pathTypeKey.deriveSoftened(i);
                HDAddress        HDAddress = new HDAddress(key.toAddress(), key.getPubKeyExtended(), pathType, i, false, true, hdSeedId);
                addresses.add(HDAddress);
            }
            master.wipe();
            accountKey.wipe();
            pathTypeKey.wipe();
            return addresses;
        } catch (KeyCrypterException e) {
            throw new PasswordException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
