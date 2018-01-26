package com.dasset.wallet.core.db.facade;

import com.dasset.wallet.core.DesktopHDMAddress;
import com.dasset.wallet.core.DesktopHDMKeychain;
import com.dasset.wallet.core.In;
import com.dasset.wallet.core.Out;
import com.dasset.wallet.core.Tx;
import com.dasset.wallet.core.contant.PathType;
import com.dasset.wallet.core.wallet.hd.HDMAddress;

import java.util.HashSet;
import java.util.List;

public interface IDesktopTxProvider {

    void addAddress(List<DesktopHDMAddress> address);

    int maxHDMAddressPubIndex();

    String externalAddress();

    boolean hasAddress();

    long getHDAccountConfirmedBanlance(int hdSeedId);

    HashSet<String> getBelongAccountAddresses(List<String> addressList);

    void updateIssuedIndex(PathType pathType, int index);

    int issuedIndex(PathType pathType);

    int allGeneratedAddressCount(PathType pathType);

    void updateSyncdForIndex(PathType pathType, int index);

    void updateSyncdComplete(DesktopHDMAddress address);

    List<Tx> getHDAccountUnconfirmedTx();

    List<HDMAddress.Pubs> getPubs(PathType pathType);

    int getUnspendOutCountByHDAccountWithPath(int hdAccountId, PathType pathType);

    List<Out> getUnspendOutByHDAccountWithPath(int hdAccountId, PathType pathType);

    DesktopHDMAddress addressForPath(DesktopHDMKeychain desktopHDMKeychain, PathType type, int index);

    List<DesktopHDMAddress> getSigningAddressesForInputs(DesktopHDMKeychain desktopHDMKeychain, List<In> ins);

    List<DesktopHDMAddress> belongAccount(DesktopHDMKeychain keychain, List<String> addresses);

    List<Out> getUnspendOutByHDAccount(int hdAccountId);

    int unSyncedAddressCount();
}
