/*
 *
 *  Copyright 2014 http://Bither.net
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */

package com.dasset.wallet.core;

import com.dasset.wallet.core.api.CreateHDMAddressApi;
import com.dasset.wallet.core.contant.AbstractApp;
import com.dasset.wallet.core.contant.Constant;
import com.dasset.wallet.core.contant.PathType;
import com.dasset.wallet.core.contant.SigHash;
import com.dasset.wallet.core.crypto.ECKey;
import com.dasset.wallet.core.crypto.EncryptedData;
import com.dasset.wallet.core.crypto.TransactionSignature;
import com.dasset.wallet.core.crypto.hd.DeterministicKey;
import com.dasset.wallet.core.crypto.hd.HDKeyDerivation;
import com.dasset.wallet.core.crypto.mnemonic.MnemonicCode;
import com.dasset.wallet.core.crypto.mnemonic.MnemonicException;
import com.dasset.wallet.core.db.facade.BaseProvider;
import com.dasset.wallet.core.exception.AddressFormatException;
import com.dasset.wallet.core.exception.TxBuilderException;
import com.dasset.wallet.core.qrcode.QRCodeUtil;
import com.dasset.wallet.core.script.ScriptBuilder;
import com.dasset.wallet.core.utils.Base58;
import com.dasset.wallet.core.utils.PrivateKeyUtil;
import com.dasset.wallet.core.utils.Utils;
import com.dasset.wallet.core.wallet.hd.AbstractHD;
import com.dasset.wallet.core.wallet.hd.HDMAddress;
import com.dasset.wallet.core.wallet.hd.HDMBId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class DesktopHDMKeychain extends AbstractHD {

    public static final  String DesktopHDMKeychainPlaceHolder = "DesktopHDMKeychain";
    private              long   balance                       = 0;
    private static final int    LOOK_AHEAD_SIZE               = 100;

    private LinkedBlockingQueue<HashMap<String, Long>> sendRequestList = new LinkedBlockingQueue<HashMap<String, Long>>();

    public static interface DesktopHDMFetchOtherSignatureDelegate {
        List<TransactionSignature> getOtherSignature(Tx tx,
                                                     List<byte[]> unsignHash, List<PathTypeIndex> pathTypeIndexLsit);
    }


    private static final Logger log = LoggerFactory.getLogger(DesktopHDMKeychain.class);


    public DesktopHDMKeychain(byte[] mnemonicSeed, CharSequence password) throws MnemonicException
            .MnemonicLengthException {
        this.mnemonicSeed = mnemonicSeed;
        String        firstAddress          = null;
        EncryptedData encryptedMnemonicSeed = null;
        EncryptedData encryptedHDSeed       = null;
        ECKey         k                     = new ECKey(mnemonicSeed, null);
        String        address               = k.toAddress();
        k.clearPrivateKey();

        hdSeed = seedFromMnemonic(mnemonicSeed);
        encryptedHDSeed = new EncryptedData(hdSeed, password, isFromXRandom);
        encryptedMnemonicSeed = new EncryptedData(mnemonicSeed, password, isFromXRandom);
        DeterministicKey master = HDKeyDerivation.createMasterPrivateKey(hdSeed);
        initHDAccount(master, encryptedMnemonicSeed, encryptedHDSeed, true);

    }


    // Create With Random
    public DesktopHDMKeychain(SecureRandom random, CharSequence password) {
        isFromXRandom = random.getClass().getCanonicalName().indexOf("XRandom") >= 0;
        mnemonicSeed = new byte[32];

        EncryptedData encryptedMnemonicSeed = null;
        EncryptedData encryptedHDSeed       = null;

        try {
            random.nextBytes(mnemonicSeed);
            hdSeed = seedFromMnemonic(mnemonicSeed);
            encryptedHDSeed = new EncryptedData(hdSeed, password, isFromXRandom);
            encryptedMnemonicSeed = new EncryptedData(mnemonicSeed, password, isFromXRandom);

        } catch (Exception e) {
            e.printStackTrace();
        }

        DeterministicKey master = HDKeyDerivation.createMasterPrivateKey(hdSeed);
        initHDAccount(master, encryptedMnemonicSeed, encryptedHDSeed, true);
    }


    // From DB
    public DesktopHDMKeychain(int seedId) {
        this.hdSeedId = seedId;
        isFromXRandom = BaseProvider.iDesktopAddressProvider.isHDSeedFromXRandom(getHdSeedId());
        updateBalance();
    }

    // Import
    public DesktopHDMKeychain(EncryptedData encryptedMnemonicSeed, CharSequence password) throws
            HDMBitherIdNotMatchException, MnemonicException.MnemonicLengthException {
        mnemonicSeed = encryptedMnemonicSeed.decrypt(password);
        hdSeed = seedFromMnemonic(mnemonicSeed);
        isFromXRandom = encryptedMnemonicSeed.isXRandom();
        EncryptedData                encryptedHDSeed = new EncryptedData(hdSeed, password, isFromXRandom);
        ArrayList<DesktopHDMAddress> as              = new ArrayList<DesktopHDMAddress>();
        ArrayList<HDMAddress.Pubs>   uncompPubs      = new ArrayList<HDMAddress.Pubs>();

        ECKey  k       = new ECKey(mnemonicSeed, null);
        String address = k.toAddress();
        k.clearPrivateKey();
        String firstAddress = getFirstAddressFromSeed(password);
        wipeMnemonicSeed();
        wipeHDSeed();

        this.hdSeedId = BaseProvider.iDesktopAddressProvider.addHDKey(encryptedMnemonicSeed
                                                                           .toEncryptedString(), encryptedHDSeed.toEncryptedString(), firstAddress,
                                                                      isFromXRandom, address, null, null);
        if (as.size() > 0) {
            //   EnDesktopAddressProvider.getInstance().completeHDMAddresses(getHdSeedId(), as);

            if (uncompPubs.size() > 0) {
                //  EnDesktopAddressProvider.getInstance().prepareHDMAddresses(getHdSeedId(), uncompPubs);
                for (HDMAddress.Pubs p : uncompPubs) {
                    BaseProvider.iAddressProvider.setHDMPubsRemote(getHdSeedId(), p.index, p.remote);
                }
            }
        }
    }


    private void initHDAccount(DeterministicKey master, EncryptedData encryptedMnemonicSeed,
                               EncryptedData encryptedHDSeed, boolean isSyncedComplete) {
        String firstAddress;
        ECKey  k       = new ECKey(mnemonicSeed, null);
        String address = k.toAddress();
        k.clearPrivateKey();
        DeterministicKey accountKey  = getAccount(master);
        DeterministicKey internalKey = getChainRootKey(accountKey, PathType.INTERNAL_ROOT_PATH);
        DeterministicKey externalKey = getChainRootKey(accountKey, PathType.EXTERNAL_ROOT_PATH);
        DeterministicKey key         = externalKey.deriveSoftened(0);
        firstAddress = key.toAddress();
        accountKey.wipe();
        master.wipe();

        wipeHDSeed();
        wipeMnemonicSeed();
        hdSeedId = BaseProvider.iDesktopAddressProvider.addHDKey(encryptedMnemonicSeed.toEncryptedString(),
                                                                 encryptedHDSeed.toEncryptedString(), firstAddress, isFromXRandom, address, externalKey.getPubKeyExtended(), internalKey
                                                                      .getPubKeyExtended());
        internalKey.wipe();
        externalKey.wipe();


    }

    public void addAccountKey(byte[] firstByte, byte[] secondByte) {
        if (new BigInteger(1, firstByte).compareTo(new BigInteger(1, secondByte)) > 0) {
            byte[] temp = firstByte;
            firstByte = secondByte;
            secondByte = temp;
        }

        DeterministicKey firstAccountKey = HDKeyDerivation.createMasterPubKeyFromExtendedBytes
                (firstByte);
        DeterministicKey secondAccountKey = HDKeyDerivation.createMasterPubKeyFromExtendedBytes
                (secondByte);

        DeterministicKey firestInternalKey = getChainRootKey(firstAccountKey, PathType.INTERNAL_ROOT_PATH);
        DeterministicKey firestExternalKey = getChainRootKey(firstAccountKey, PathType.EXTERNAL_ROOT_PATH);

        DeterministicKey secondInternalKey = getChainRootKey(secondAccountKey, PathType.INTERNAL_ROOT_PATH);
        DeterministicKey secondExternalKey = getChainRootKey(secondAccountKey, PathType.EXTERNAL_ROOT_PATH);
        List<byte[]>     externalPubs      = new ArrayList<byte[]>();
        List<byte[]>     internalPubs      = new ArrayList<byte[]>();
        externalPubs.add(firestExternalKey.getPubKeyExtended());
        externalPubs.add(secondExternalKey.getPubKeyExtended());
        internalPubs.add(firestInternalKey.getPubKeyExtended());
        internalPubs.add(secondInternalKey.getPubKeyExtended());
        BaseProvider.iDesktopAddressProvider.addHDMPublicKey(externalPubs, internalPubs);
        addDesktopAddress(PathType.EXTERNAL_ROOT_PATH, LOOK_AHEAD_SIZE);
        addDesktopAddress(PathType.INTERNAL_ROOT_PATH, LOOK_AHEAD_SIZE);
    }

    private void addDesktopAddress(PathType pathType, int count) {
        if (pathType == PathType.EXTERNAL_ROOT_PATH) {
            List<DesktopHDMAddress> desktopHDMAddresses = new ArrayList<DesktopHDMAddress>();
            List<byte[]>            externalPubs        = BaseProvider.iDesktopAddressProvider.getExternalPublicKeys();
            DeterministicKey externalKey1 = HDKeyDerivation.createMasterPubKeyFromExtendedBytes
                    (externalPubs.get(0));
            DeterministicKey externalKey2 = HDKeyDerivation.createMasterPubKeyFromExtendedBytes
                    (externalPubs.get(1));
            DeterministicKey externalKey3 = HDKeyDerivation.createMasterPubKeyFromExtendedBytes
                    (externalPubs.get(2));
            for (int i = 0;
                 i < count;
                 i++) {
                byte[]          subExternalPub1 = externalKey1.deriveSoftened(i).getPublicKey();
                byte[]          subExternalPub2 = externalKey2.deriveSoftened(i).getPublicKey();
                byte[]          subExternalPub3 = externalKey3.deriveSoftened(i).getPublicKey();
                HDMAddress.Pubs pubs            = new HDMAddress.Pubs();
                pubs.hot = subExternalPub1;
                pubs.cold = subExternalPub2;
                pubs.remote = subExternalPub3;
                pubs.index = i;
                DesktopHDMAddress desktopHDMAddress = new DesktopHDMAddress(pubs, pathType, DesktopHDMKeychain.this, false);
                desktopHDMAddresses.add(desktopHDMAddress);

            }
            BaseProvider.iDesktopTxProvider.addAddress(desktopHDMAddresses);
        } else {
            List<DesktopHDMAddress> desktopHDMAddresses = new ArrayList<DesktopHDMAddress>();
            List<byte[]>            internalPubs        = BaseProvider.iDesktopAddressProvider.getInternalPublicKeys();
            DeterministicKey internalKey1 = HDKeyDerivation.createMasterPubKeyFromExtendedBytes
                    (internalPubs.get(0));
            DeterministicKey internalKey2 = HDKeyDerivation.createMasterPubKeyFromExtendedBytes
                    (internalPubs.get(1));
            DeterministicKey internalKey3 = HDKeyDerivation.createMasterPubKeyFromExtendedBytes
                    (internalPubs.get(2));
            for (int i = 0;
                 i < count;
                 i++) {
                byte[]          subInternalPub1 = internalKey1.deriveSoftened(i).getPublicKey();
                byte[]          subInternalPub2 = internalKey2.deriveSoftened(i).getPublicKey();
                byte[]          subInternalPub3 = internalKey3.deriveSoftened(i).getPublicKey();
                HDMAddress.Pubs pubs            = new HDMAddress.Pubs();
                pubs.hot = subInternalPub1;
                pubs.cold = subInternalPub2;
                pubs.remote = subInternalPub3;
                pubs.index = i;
                DesktopHDMAddress desktopHDMAddress = new DesktopHDMAddress(pubs, pathType, DesktopHDMKeychain.this, false);
                desktopHDMAddresses.add(desktopHDMAddress);

            }
            BaseProvider.iDesktopTxProvider.addAddress(desktopHDMAddresses);
        }


    }

    private void supplyNewInternalKey(int count, boolean isSyncedComplete) {
        List<DesktopHDMAddress> desktopHDMAddresses = new ArrayList<DesktopHDMAddress>();
        List<byte[]>            internalPubs        = BaseProvider.iDesktopAddressProvider.getInternalPublicKeys();
        DeterministicKey internalKey1 = HDKeyDerivation.createMasterPubKeyFromExtendedBytes
                (internalPubs.get(0));
        DeterministicKey internalKey2 = HDKeyDerivation.createMasterPubKeyFromExtendedBytes
                (internalPubs.get(1));
        DeterministicKey internalKey3 = HDKeyDerivation.createMasterPubKeyFromExtendedBytes
                (internalPubs.get(2));
        int firstIndex = allGeneratedInternalAddressCount();
        for (int i = firstIndex;
             i < count + firstIndex;
             i++) {
            byte[]          subInternalPub1 = internalKey1.deriveSoftened(i).getPublicKey();
            byte[]          subInternalPub2 = internalKey2.deriveSoftened(i).getPublicKey();
            byte[]          subInternalPub3 = internalKey3.deriveSoftened(i).getPublicKey();
            HDMAddress.Pubs pubs            = new HDMAddress.Pubs();
            pubs.hot = subInternalPub1;
            pubs.cold = subInternalPub2;
            pubs.remote = subInternalPub3;
            pubs.index = i;
            DesktopHDMAddress desktopHDMAddress = new DesktopHDMAddress(pubs, PathType.INTERNAL_ROOT_PATH, DesktopHDMKeychain.this, isSyncedComplete);
            desktopHDMAddresses.add(desktopHDMAddress);

        }
        BaseProvider.iDesktopTxProvider.addAddress(desktopHDMAddresses);

    }

    private void supplyNewExternalKey(int count, boolean isSyncedComplete) {
        List<byte[]> externalPubs = BaseProvider.iDesktopAddressProvider.getExternalPublicKeys();
        DeterministicKey externalKey1 = HDKeyDerivation.createMasterPubKeyFromExtendedBytes
                (externalPubs.get(0));
        DeterministicKey externalKey2 = HDKeyDerivation.createMasterPubKeyFromExtendedBytes
                (externalPubs.get(1));
        DeterministicKey externalKey3 = HDKeyDerivation.createMasterPubKeyFromExtendedBytes
                (externalPubs.get(2));
        List<DesktopHDMAddress> desktopHDMAddresses = new ArrayList<DesktopHDMAddress>();
        int                     firstIndex          = allGeneratedExternalAddressCount();
        for (int i = firstIndex;
             i < count + firstIndex;
             i++) {
            byte[]          subExternalPub1 = externalKey1.deriveSoftened(i).getPublicKey();
            byte[]          subExternalPub2 = externalKey2.deriveSoftened(i).getPublicKey();
            byte[]          subExternalPub3 = externalKey3.deriveSoftened(i).getPublicKey();
            HDMAddress.Pubs pubs            = new HDMAddress.Pubs();
            pubs.hot = subExternalPub1;
            pubs.cold = subExternalPub2;
            pubs.remote = subExternalPub3;
            pubs.index = i;
            DesktopHDMAddress desktopHDMAddress = new DesktopHDMAddress(pubs, PathType.EXTERNAL_ROOT_PATH, DesktopHDMKeychain.this, isSyncedComplete);
            desktopHDMAddresses.add(desktopHDMAddress);

        }
        BaseProvider.iDesktopTxProvider.addAddress(desktopHDMAddresses);
        log.info("HD supplied {} internal addresses", desktopHDMAddresses.size());
    }


    public boolean initTxs(List<Tx> txs) {
        BaseProvider.iTxProvider.addTxs(txs);
        notificatTx(null, Tx.TxNotificationType.txFromApi);
        return true;
    }

    public void notificatTx(Tx tx, Tx.TxNotificationType txNotificationType) {
        long deltaBalance = getDeltaBalance();
        AbstractApp.notificationService.notificatTx(DesktopHDMKeychainPlaceHolder
                , tx, txNotificationType, deltaBalance);
    }

    public boolean hasDesktopHDMAddress() {
        return BaseProvider.iDesktopTxProvider.hasAddress();
    }

    public void updateSyncComplete(DesktopHDMAddress accountAddress) {
        BaseProvider.iDesktopTxProvider.updateSyncdComplete(accountAddress);
    }


    private DeterministicKey externalChainRoot(CharSequence password) throws MnemonicException.MnemonicLengthException {
        DeterministicKey master      = masterKey(password);
        DeterministicKey accountKey  = getAccount(master);
        DeterministicKey externalKey = getChainRootKey(accountKey, PathType.EXTERNAL_ROOT_PATH);
        master.wipe();
        accountKey.wipe();
        return externalKey;
    }

    public byte[] getExternalChainRootPubExtended(CharSequence password) throws MnemonicException
            .MnemonicLengthException {
        DeterministicKey ex  = externalChainRoot(password);
        byte[]           pub = ex.getPubKeyExtended();
        ex.wipe();
        return pub;
    }

    public String getExternalChainRootPubExtendedAsHex(CharSequence password) throws
            MnemonicException.MnemonicLengthException {
        return Utils.bytesToHexString(getExternalChainRootPubExtended(password)).toUpperCase();
    }

    public boolean isTxRelated(Tx tx, List<String> inAddresses) {
        return getRelatedAddressesForTx(tx, inAddresses).size() > 0;
    }

    public Tx newTx(String toAddress, Long amount) throws TxBuilderException, MnemonicException.MnemonicLengthException, AddressFormatException {
        return newTx(new String[]{toAddress}, new Long[]{amount});
    }


    public Tx newTx(String[] toAddresses, Long[] amounts) throws TxBuilderException, MnemonicException.MnemonicLengthException, AddressFormatException {
        List<Out> outs = BaseProvider.iDesktopTxProvider.getUnspendOutByHDAccount(hdSeedId);

        Tx tx = TxBuilder.getInstance().buildTxFromAllAddress(outs, getNewChangeAddress(), Arrays
                .asList(amounts), Arrays.asList(toAddresses));
        List<DesktopHDMAddress> signingAddresses = getSigningAddressesForInputs(tx.getIns());
        assert signingAddresses.size() == tx.getIns().size();

        List<byte[]> unsignedHashes = tx.getUnsignedInHashes();

        assert unsignedHashes.size() == signingAddresses.size();

//        DeterministicKey master = masterKey(password);
//        if (master == null) {
//            return null;
//        }
//        DeterministicKey accountKey = getKeyStoreData(master);
//        DeterministicKey external = getChainRootKey(accountKey, PathType.EXTERNAL_ROOT_PATH);
//        DeterministicKey internal = getChainRootKey(accountKey, PathType.INTERNAL_ROOT_PATH);
//        accountKey.wipe();
//        master.wipe();
//        ArrayList<byte[]> signatures = new ArrayList<byte[]>();
//        HashMap<String, DeterministicKey> addressToKeyMap = new HashMap<String, DeterministicKey>
//                (signingAddresses.size());
//
//        for (int i = 0;
//             i < signingAddresses.size();
//             i++) {
//            DesktopHDMAddress a = signingAddresses.get(i);
//            byte[] unsigned = unsignedHashes.get(i);
//
//            if (!addressToKeyMap.containsKey(a.getAddress2())) {
//                if (a.getPathType() == PathType.EXTERNAL_ROOT_PATH) {
//                    addressToKeyMap.put(a.getAddress2(), external.deriveSoftened(a.getIndex()));
//                } else {
//                    addressToKeyMap.put(a.getAddress2(), internal.deriveSoftened(a.getIndex()));
//                }
//            }
//
//            DeterministicKey key = addressToKeyMap.get(a.getAddress2());
//            assert key != null;
//
//            TransactionSignature signature = new TransactionSignature(key.sign(unsigned, null),
//                    SigHash.ALL, false);
//            signatures.add(ScriptBuilder.createInputScript(signature, key).getProgram());
//        }
//
//        tx.signWithSignatures(signatures);
        assert tx.verifySignatures();

//        external.wipe();
//        internal.wipe();
//        for (DeterministicKey key : addressToKeyMap.values()) {
//            key.wipe();
//        }

        return tx;
    }


    public void signTx(Tx tx, List<byte[]> unSignHash, CharSequence passphrase, List<DesktopHDMAddress> desktopHDMAddresslist,
                       DesktopHDMFetchOtherSignatureDelegate delegate) {
        tx.signWithSignatures(this.signWithOther(unSignHash,
                                                 passphrase, tx, desktopHDMAddresslist, delegate));
    }

    public List<byte[]> signWithOther(List<byte[]> unsignHash, CharSequence password, Tx tx, List<DesktopHDMAddress> desktopHDMAddresslist,
                                      DesktopHDMFetchOtherSignatureDelegate delegate
    ) {
        List<PathTypeIndex> pathTypeIndexList = new ArrayList<PathTypeIndex>();
        for (DesktopHDMAddress desktopHDMAddress : desktopHDMAddresslist) {
            PathTypeIndex pathTypeIndex = new PathTypeIndex();
            pathTypeIndex.index = desktopHDMAddress.getIndex();
            pathTypeIndex.pathType = desktopHDMAddress.getPathType();
            pathTypeIndexList.add(pathTypeIndex);
        }
        ArrayList<TransactionSignature> hotSigs = signMyPart(unsignHash, password, pathTypeIndexList);
        List<TransactionSignature> otherSigs = delegate.getOtherSignature(
                tx, unsignHash, pathTypeIndexList);
        assert hotSigs.size() == otherSigs.size() && hotSigs.size() == unsignHash.size();
        return formatInScript(hotSigs, otherSigs, desktopHDMAddresslist);
    }

    public ArrayList<byte[]> signWithCold(List<byte[]> unsignedHashes,
                                          CharSequence password,
                                          List<PathTypeIndex> pathTypeIndexList) {


        ArrayList<byte[]> sigs = new ArrayList<byte[]>();
        for (int i = 0;
             i < unsignedHashes.size();
             i++) {
            PathTypeIndex    pathTypeIndex = pathTypeIndexList.get(i);
            DeterministicKey key;
            if (pathTypeIndex.pathType == PathType.EXTERNAL_ROOT_PATH) {
                key = getExternalKey(pathTypeIndex.index, password);
                System.out.println("publicKey:" + Base58.encode(key.getPublicKey()));
            } else {
                key = getInternalKey(pathTypeIndex.index, password);
            }
            ECKey.ECDSASignature signed = key.sign(unsignedHashes.get(i));
            sigs.add(signed.encodeToDER());
            key.wipe();
        }

        return sigs;
    }


    public ArrayList<TransactionSignature> signMyPart(List<byte[]> unsignedHashes,
                                                      CharSequence password,
                                                      List<PathTypeIndex> pathTypeIndexList) {


        ArrayList<TransactionSignature> sigs = new ArrayList<TransactionSignature>();
        for (int i = 0;
             i < unsignedHashes.size();
             i++) {
            PathTypeIndex    pathTypeIndex = pathTypeIndexList.get(i);
            DeterministicKey key;
            if (pathTypeIndex.pathType == PathType.EXTERNAL_ROOT_PATH) {
                key = getExternalKey(pathTypeIndex.index, password);
            } else {
                key = getInternalKey(pathTypeIndex.index, password);
            }
            TransactionSignature transactionSignature = new TransactionSignature(key.sign
                    (unsignedHashes.get(i)), SigHash.ALL, false);
            sigs.add(transactionSignature);
            key.wipe();
        }

        return sigs;
    }


    public static List<byte[]> formatInScript(List<TransactionSignature> signs1,
                                              List<TransactionSignature> signs2,
                                              List<DesktopHDMAddress> addressList) {
        List<byte[]> result = new ArrayList<byte[]>();
        for (int i = 0;
             i < signs1.size();
             i++) {
            DesktopHDMAddress          a     = addressList.get(i);
            List<TransactionSignature> signs = new ArrayList<TransactionSignature>(2);
            signs.add(signs1.get(i));
            signs.add(signs2.get(i));
            result.add(ScriptBuilder.createP2SHMultiSigInputScript(signs,
                                                                   a.getPubKey()).getProgram());
        }
        return result;
    }

    public List<DesktopHDMAddress> getRelatedAddressesForTx(Tx tx, List<String> inAddresses) {
        List<String>            outAddressList       = new ArrayList<String>();
        List<DesktopHDMAddress> hdAccountAddressList = new ArrayList<DesktopHDMAddress>();
        for (Out out : tx.getOuts()) {
            String outAddress = out.getOutAddress();
            outAddressList.add(outAddress);
        }
        List<DesktopHDMAddress> belongAccountOfOutList = BaseProvider.iDesktopTxProvider.belongAccount(DesktopHDMKeychain.this, outAddressList);
        if (belongAccountOfOutList != null
                && belongAccountOfOutList.size() > 0) {
            hdAccountAddressList.addAll(belongAccountOfOutList);
        }

        List<DesktopHDMAddress> belongAccountOfInList = getAddressFromIn(inAddresses);
        if (belongAccountOfInList != null && belongAccountOfInList.size() > 0) {
            hdAccountAddressList.addAll(belongAccountOfInList);
        }

        return hdAccountAddressList;
    }

    private List<DesktopHDMAddress> getAddressFromIn(List<String> addresses) {

        List<DesktopHDMAddress> hdAccountAddressList = BaseProvider.iDesktopTxProvider.belongAccount(DesktopHDMKeychain.this, addresses);
        return hdAccountAddressList;
    }


    public String getNewChangeAddress() {
        return addressForPath(PathType.INTERNAL_ROOT_PATH, issuedInternalIndex() + 1).getAddress();
    }

    private DesktopHDMAddress addressForPath(PathType type, int index) {
        assert index < (type == PathType.EXTERNAL_ROOT_PATH ? allGeneratedExternalAddressCount()
                : allGeneratedInternalAddressCount());
        return BaseProvider.iDesktopTxProvider.addressForPath(DesktopHDMKeychain.this, type, index);
    }

    @Override
    public boolean isFromXRandom() {
        return isFromXRandom;
    }


    public String getFullEncryptPrivKey() {
        String encryptPrivKey = getEncryptedMnemonicSeed();
        return PrivateKeyUtil.getFullencryptHDMKeyChain(isFromXRandom, encryptPrivKey);
    }

    public List<DesktopHDMAddress> getSigningAddressesForInputs(List<In> inputs) {
        return BaseProvider.iDesktopTxProvider.getSigningAddressesForInputs(DesktopHDMKeychain.this, inputs);
    }

    public String getQRCodeFullEncryptPrivKey() {
        return Constant.HDM_QR_CODE_FLAG + getFullEncryptPrivKey();
    }

    @Override
    protected String getEncryptedHDSeed() {

        String encrypted = BaseProvider.iDesktopAddressProvider.getEncryptHDSeed(hdSeedId);
        if (encrypted == null) {
            return null;
        }
        return encrypted.toUpperCase();
    }

    @Override
    public String getEncryptedMnemonicSeed() {

        return BaseProvider.iDesktopAddressProvider.getEncryptMnemonicSeed(hdSeedId).toUpperCase();
    }

    public String getFirstAddressFromDb() {
        return BaseProvider.iDesktopAddressProvider.getHDMFristAddress(hdSeedId);
    }

    public boolean checkWithPassword(CharSequence password) {
        try {
            decryptHDSeed(password);
            decryptMnemonicSeed(password);
            byte[] hdCopy = Arrays.copyOf(hdSeed, hdSeed.length);
            boolean hdSeedSafe = Utils.compareString(getFirstAddressFromDb(),
                                                     getFirstAddressFromSeed(null));
            boolean mnemonicSeedSafe = Arrays.equals(seedFromMnemonic(mnemonicSeed), hdCopy);
            Utils.wipeBytes(hdCopy);
            wipeHDSeed();
            wipeMnemonicSeed();
            return hdSeedSafe && mnemonicSeedSafe;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public static void getRemotePublicKeys(HDMBId hdmBId, CharSequence password,
                                           List<HDMAddress.Pubs> partialPubs) throws Exception {
        byte[] decryptedPassword = hdmBId.decryptHDMBIdPassword(password);
        CreateHDMAddressApi createHDMAddressApi = new CreateHDMAddressApi(hdmBId.getAddress(),
                                                                          partialPubs, decryptedPassword);
        createHDMAddressApi.handleHttpPost();
        List<byte[]> remotePubs = createHDMAddressApi.getResult();
        for (int i = 0;
             i < partialPubs.size();
             i++) {
            HDMAddress.Pubs pubs = partialPubs.get(i);
            pubs.remote = remotePubs.get(i);
        }
    }


    public static final class HDMBitherIdNotMatchException extends RuntimeException {
        public static final String msg = "HDM Bid Not Match";

        public HDMBitherIdNotMatchException() {
            super(msg);
        }
    }

    public static boolean checkPassword(String keysString, CharSequence password) throws
            MnemonicException.MnemonicLengthException {
        String[] passwordSeeds = QRCodeUtil.splitOfPasswordSeed(keysString);
        String   address       = Base58.hexToBase58WithAddress(passwordSeeds[0]);
        String encreyptString = Utils.joinString(new String[]{passwordSeeds[1], passwordSeeds[2],
                passwordSeeds[3]}, Constant.QR_CODE_SPLIT);
        byte[]           seed     = new EncryptedData(encreyptString).decrypt(password);
        DeterministicKey master   = HDKeyDerivation.createMasterPrivateKey(MnemonicCode.getInstance().toSeed(MnemonicCode.getInstance().toMnemonic(seed), ""));
        DeterministicKey purpose  = master.deriveHardened(44);
        DeterministicKey coinType = purpose.deriveHardened(0);
        DeterministicKey account  = coinType.deriveHardened(0);
        DeterministicKey external = account.deriveSoftened(0);
        external.clearPrivateKey();
        DeterministicKey deterministicKey = external.deriveSoftened(0);
        boolean          result           = Utils.compareString(address, Utils.toAddress(deterministicKey.getPublicKeyHash()));
        deterministicKey.wipe();

        return result;
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


    public void onNewTx(Tx tx, List<DesktopHDMAddress> relatedAddresses, Tx.TxNotificationType txNotificationType) {
        if (relatedAddresses == null || relatedAddresses.size() == 0) {
            return;
        }

        int maxInternal = -1, maxExternal = -1;
        for (DesktopHDMAddress a : relatedAddresses) {
            if (a.getPathType() == PathType.EXTERNAL_ROOT_PATH) {
                if (a.getIndex() > maxExternal) {
                    maxExternal = a.getIndex();
                }
            } else {
                if (a.getIndex() > maxInternal) {
                    maxInternal = a.getIndex();
                }
            }
        }

        log.info("HD on new tx issued ex {}, issued in {}", maxExternal, maxInternal);
        if (maxExternal >= 0 && maxExternal > issuedExternalIndex()) {
            updateIssuedExternalIndex(maxExternal);
        }
        if (maxInternal >= 0 && maxInternal > issuedInternalIndex()) {
            updateIssuedInternalIndex(maxInternal);
        }
        supplyEnoughKeys(true);
        long deltaBalance = getDeltaBalance();
        AbstractApp.notificationService.notificatTx(DesktopHDMKeychainPlaceHolder, tx, txNotificationType,
                                                    deltaBalance);
    }


    public int elementCountForBloomFilter() {
        return allGeneratedExternalAddressCount() * 2 + BaseProvider.iDesktopTxProvider
                .getUnspendOutCountByHDAccountWithPath(getHdSeedId(), PathType
                        .INTERNAL_ROOT_PATH);
    }

    public void addElementsForBloomFilter(BloomFilter filter) {
        List<HDMAddress.Pubs> pubses = BaseProvider.iDesktopTxProvider.getPubs(PathType.EXTERNAL_ROOT_PATH);
        for (HDMAddress.Pubs pub : pubses) {
            byte[] pubByte = pub.getMultiSigScript().getProgram();
            filter.insert(pubByte);
            filter.insert(Utils.sha256hash160(pubByte));
            // System.out.println("address:" + Utils.toP2SHAddress(Utils.sha256hash160(pubByte)));
        }
        List<Out> outs = BaseProvider.iDesktopTxProvider.getUnspendOutByHDAccountWithPath
                (getHdSeedId(), PathType.INTERNAL_ROOT_PATH);
        for (Out out : outs) {
            filter.insert(out.getOutpointData());
        }
    }

    private long calculateUnconfirmedBalance() {
        long balance = 0;

        List<Tx> txs = BaseProvider.iDesktopTxProvider.getHDAccountUnconfirmedTx();
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
                Tx tx1 = BaseProvider.iTxProvider.getTxDetailByTxHash(o.getTxHash());
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

    private long getDeltaBalance() {
        long oldBalance = this.balance;
        this.updateBalance();
        return this.balance - oldBalance;
    }

    public void updateBalance() {
        this.balance = BaseProvider.iDesktopTxProvider.getHDAccountConfirmedBanlance(hdSeedId)
                + calculateUnconfirmedBalance();
    }

    public long getBalance() {
        return this.balance;
    }

    public HashSet<String> getBelongAccountAddresses(List<String> addressList) {
        return BaseProvider.iDesktopTxProvider.getBelongAccountAddresses(addressList);
    }

    public void updateIssuedInternalIndex(int index) {
        BaseProvider.iDesktopTxProvider.updateIssuedIndex(PathType.INTERNAL_ROOT_PATH, index);
    }

    public void updateIssuedExternalIndex(int index) {
        BaseProvider.iDesktopTxProvider.updateIssuedIndex(PathType.EXTERNAL_ROOT_PATH, index);
    }

    public byte[] getInternalPub() {
        //   return BaseProvider.iAddressProvider.getInternalPublicKey(hdSeedId);
        return new byte[]{};
    }

    public byte[] getExternalPub() {

        //return BaseProvider.iAddressProvider.getExternalPublicKey(hdSeedId);
        return new byte[]{};
    }

    public int issuedInternalIndex() {

        return BaseProvider.iDesktopTxProvider.issuedIndex(PathType.INTERNAL_ROOT_PATH);
    }

    public int issuedExternalIndex() {
        return BaseProvider.iDesktopTxProvider.issuedIndex(PathType.EXTERNAL_ROOT_PATH);

    }

    private int allGeneratedInternalAddressCount() {
        return BaseProvider.iDesktopTxProvider.allGeneratedAddressCount(PathType
                                                                             .INTERNAL_ROOT_PATH);
    }

    private int allGeneratedExternalAddressCount() {
        return BaseProvider.iDesktopTxProvider.allGeneratedAddressCount(PathType
                                                                             .EXTERNAL_ROOT_PATH);
    }

    public String getMasterPubKeyExtendedStr(CharSequence password) {
        byte[] bytes = getMasterPubKeyExtended(password);
        return Utils.bytesToHexString(bytes).toUpperCase(Locale.US);
    }

    public boolean isSyncComplete() {
        int unsyncedAddressCount = BaseProvider.iDesktopTxProvider.unSyncedAddressCount();
        return unsyncedAddressCount == 0;
    }

    public String externalAddress() {
        return BaseProvider.iDesktopTxProvider.externalAddress();
    }

    public LinkedBlockingQueue<HashMap<String, Long>> getSendRequestList() {
        return this.sendRequestList;
    }
}
