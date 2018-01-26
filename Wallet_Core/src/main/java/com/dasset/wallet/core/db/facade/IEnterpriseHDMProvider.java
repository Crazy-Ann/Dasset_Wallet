package com.dasset.wallet.core.db.facade;

import com.dasset.wallet.core.EnterpriseHDMAddress;
import com.dasset.wallet.core.EnterpriseHDMKeychain;

import java.util.List;

public interface IEnterpriseHDMProvider {

    String getEnterpriseEncryptMnemonicSeed(int hdSeedId);

    String getEnterpriseEncryptHDSeed(int hdSeedId);

    String getEnterpriseHDFristAddress(int hdSeedId);

    boolean isEnterpriseHDMSeedFromXRandom(int hdSeedId);

    void addEnterpriseHDMAddress(List<EnterpriseHDMAddress> enterpriseHDMAddresses);

    List<EnterpriseHDMAddress> getEnterpriseHDMAddress(EnterpriseHDMKeychain enterpriseHDMKeychain);

    void addMultiSignSet(int n, int m);

    void updateSyncComplete(EnterpriseHDMAddress enterpriseHDMAddress);

    List<Integer> getEnterpriseHDMKeychainIds();

    int getEnterpriseHDMSeedId();

    int getPubCount();

    int getThreshold();
}
