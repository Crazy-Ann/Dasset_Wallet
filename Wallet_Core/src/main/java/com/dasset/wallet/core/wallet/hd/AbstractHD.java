package com.dasset.wallet.core.wallet.hd;

import com.dasset.wallet.core.contant.PathType;
import com.dasset.wallet.core.crypto.EncryptedData;
import com.dasset.wallet.core.crypto.hd.DeterministicKey;
import com.dasset.wallet.core.crypto.hd.HDKeyDerivation;
import com.dasset.wallet.core.crypto.mnemonic.MnemonicCode;
import com.dasset.wallet.core.crypto.mnemonic.MnemonicException;
import com.dasset.wallet.core.db.facade.BaseProvider;
import com.dasset.wallet.core.exception.KeyCrypterException;
import com.dasset.wallet.core.utils.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class AbstractHD {


    public static class PathTypeIndex {
        public PathType pathType;
        public int      index;
    }


    public static PathType getTernalRootType(int value) {
        switch (value) {
            case 0:
                return PathType.EXTERNAL_ROOT_PATH;
            default:
                return PathType.INTERNAL_ROOT_PATH;
        }
    }

    protected transient byte[] mnemonicSeed;
    protected transient byte[] hdSeed;
    protected int hdSeedId = -1;
    protected boolean isFromXRandom;

    private static final Logger log = LoggerFactory.getLogger(AbstractHD.class);


    protected abstract String getEncryptedHDSeed();

    protected abstract String getEncryptedMnemonicSeed();


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
        log.info("hdm keychain decryptAESEBC time: {}", System.currentTimeMillis() - begin);
        return master;
    }

    protected void decryptHDSeed(CharSequence password) throws MnemonicException
            .MnemonicLengthException {
        if (hdSeedId < 0 || password == null) {
            return;
        }
        String encryptedHDSeed = getEncryptedHDSeed();
        if (Utils.isEmpty(encryptedHDSeed)) {
            initHDSeedFromMnemonicSeed(password);
        } else {
            hdSeed = new EncryptedData(encryptedHDSeed).decrypt(password);
        }
    }

    private void initHDSeedFromMnemonicSeed(CharSequence password) throws MnemonicException
            .MnemonicLengthException {
        decryptMnemonicSeed(password);
        hdSeed = seedFromMnemonic(mnemonicSeed);
        wipeMnemonicSeed();
        BaseProvider.iAddressProvider.updateEncryptedMnmonicSeed(getHdSeedId(), new EncryptedData(hdSeed,
                                                                                                  password, isFromXRandom).toEncryptedString());
    }

    public void decryptMnemonicSeed(CharSequence password) throws KeyCrypterException {
        if (hdSeedId < 0) {
            return;
        }
        String encrypted = getEncryptedMnemonicSeed();
        if (!Utils.isEmpty(encrypted)) {
            mnemonicSeed = new EncryptedData(encrypted).decrypt(password);
        }
    }

    public List<String> getSeedWords(CharSequence password) throws MnemonicException
            .MnemonicLengthException {
        decryptMnemonicSeed(password);
        List<String> words = MnemonicCode.getInstance().toMnemonic(mnemonicSeed);
        wipeMnemonicSeed();
        return words;
    }

    public boolean isFromXRandom() {
        return isFromXRandom;
    }

    protected String getFirstAddressFromSeed(CharSequence password) {
        DeterministicKey key     = getExternalKey(0, password);
        String           address = Utils.toAddress(key.getPublicKeyHash());
        key.wipe();
        return address;
    }

    public DeterministicKey getInternalKey(int index, CharSequence password) {
        try {
            DeterministicKey master            = masterKey(password);
            DeterministicKey accountKey        = getAccount(master);
            DeterministicKey externalChainRoot = getChainRootKey(accountKey, PathType.INTERNAL_ROOT_PATH);
            DeterministicKey key               = externalChainRoot.deriveSoftened(index);
            master.wipe();
            accountKey.wipe();
            externalChainRoot.wipe();
            return key;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public DeterministicKey getExternalKey(int index, CharSequence password) {
        try {
            DeterministicKey master            = masterKey(password);
            DeterministicKey accountKey        = getAccount(master);
            DeterministicKey externalChainRoot = getChainRootKey(accountKey, PathType.EXTERNAL_ROOT_PATH);
            DeterministicKey key               = externalChainRoot.deriveSoftened(index);
            master.wipe();
            accountKey.wipe();
            externalChainRoot.wipe();
            return key;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected byte[] getMasterPubKeyExtended(CharSequence password) {
        try {
            DeterministicKey master     = masterKey(password);
            DeterministicKey accountKey = getAccount(master);
            return accountKey.getPubKeyExtended();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    public static final byte[] seedFromMnemonic(byte[] mnemonicSeed) throws MnemonicException.MnemonicLengthException {
        return MnemonicCode.getInstance().toSeed(MnemonicCode.getInstance().toMnemonic(mnemonicSeed), "");
    }


}
