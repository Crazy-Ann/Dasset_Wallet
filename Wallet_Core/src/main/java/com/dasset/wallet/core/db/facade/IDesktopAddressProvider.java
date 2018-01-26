package com.dasset.wallet.core.db.facade;

import java.util.List;

public interface IDesktopAddressProvider {

    int addHDKey(String encryptedMnemonicSeed, String encryptHdSeed, String firstAddress, boolean isXrandom, String addressOfPS, byte[] externalPublicKey, byte[] internalPublicKey);

    void addHDMPublicKey(List<byte[]> externalPublicKeys, List<byte[]> internalPublicKeys);

    List<byte[]> getExternalPublicKeys();

    List<byte[]> getInternalPublicKeys();

    boolean isHDSeedFromXRandom(int hdSeedId);

    String getEncryptMnemonicSeed(int hdSeedId);

    String getEncryptHDSeed(int hdSeedId);

    String getHDMFristAddress(int hdSeedId);

    List<Integer> getDesktopKeyChainSeed();
}
