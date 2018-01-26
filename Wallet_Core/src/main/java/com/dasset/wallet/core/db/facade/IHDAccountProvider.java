
package com.dasset.wallet.core.db.facade;

import java.util.List;

public interface IHDAccountProvider {

    int addHDAccount(String encryptedMnemonicSeed, String encryptSeed, String firstAddress, boolean isXrandom, String addressOfPS, byte[] externalPublicKey, byte[] internalPublicKey);

    int addMonitoredHDAccount(String firstAddress, boolean isXrandom, byte[] externalPublicKey, byte[] internalPublicKey);

    boolean hasMnemonicSeed(int hdAccountId);

    String getHDFirstAddress(int hdSeedId);

    byte[] getExternalPublicKey(int hdSeedId);

    byte[] getInternalPublicKey(int hdSeedId);

    String getHDAccountEncryptSeed(int hdSeedId);

    String getHDAccountEncryptMnemonicSeed(int hdSeedId);

    boolean hdAccountIsXRandom(int seedId);

    List<Integer> getHDAccountSeeds();

    boolean isPublicKeyExist(byte[] externalPublicKey, byte[] internalPublicKey);
}
