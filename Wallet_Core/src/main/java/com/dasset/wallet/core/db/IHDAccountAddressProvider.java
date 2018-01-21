/*
 * Copyright 2014 http://Bither.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.dasset.wallet.core.db;

import com.dasset.wallet.core.Tx;
import com.dasset.wallet.core.contant.PathType;
import com.dasset.wallet.core.wallet.hd.AbstractHD;
import com.dasset.wallet.core.In;
import com.dasset.wallet.core.Out;
import com.dasset.wallet.core.wallet.hd.HDAddress;

import java.util.HashSet;
import java.util.List;

public interface IHDAccountAddressProvider {


    void addAddress(List<HDAddress> hdAccountAddresses);

    int issuedIndex(int hdAccountId, PathType pathType);

    int allGeneratedAddressCount(int hdAccountId, PathType pathType);

    void updateIssuedIndex(int hdAccountId, PathType pathType, int index);

    String externalAddress(int hdAccountId);

    HashSet<String> getBelongAccountAddresses(int hdAccountId, List<String> addressList);

    HashSet<String> getBelongAccountAddresses(List<String> addressList);

    Tx updateOutHDAccountId(Tx tx);

    int getRelatedAddressCnt(List<String> addresses);

    List<Integer> getRelatedHDAccountIdList(List<String> addresses);


    HDAddress addressForPath(int hdAccountId, PathType type, int index);

    List<byte[]> getPubs(int hdAccountId, PathType pathType);

    List<HDAddress> belongAccount(int hdAccountId, List<String> addresses);

    void updateSyncdComplete(int hdAccountId, HDAddress address);

    void setSyncedNotComplete();

    int unSyncedAddressCount(int hdAccountId);

    void updateSyncedForIndex(int hdAccountId, PathType pathType, int index);

    List<HDAddress> getSigningAddressesForInputs(int hdAccountId, List<In> inList);

    int hdAccountTxCount(int hdAccountId);

    long getHDAccountConfirmedBalance(int hdAccountId);

    List<Tx> getHDAccountUnconfirmedTx(int hdAccountId);

    long sentFromAccount(int hdAccountId, byte[] txHash);

    List<Tx> getTxAndDetailByHDAccount(int hdAccountId, int page);

    List<Tx> getTxAndDetailByHDAccount(int hdAccountId);

    List<Out> getUnspendOutByHDAccount(int hdAccountId);

    List<Tx> getRecentlyTxsByAccount(int hdAccountId, int greaterThanBlockNo, int limit);

    int getUnspendOutCountByHDAccountWithPath(int hdAccountId, PathType pathType);

    List<Out> getUnspendOutByHDAccountWithPath(int hdAccountId, PathType pathType);

    int getUnconfirmedSpentOutCountByHDAccountWithPath(int hdAccountId, PathType pathType);

    List<Out> getUnconfirmedSpentOutByHDAccountWithPath(int hdAccountId, PathType pathType);

    boolean requestNewReceivingAddress(int hdAccountId);

    List<Out> getUnspentOutputByBlockNo(long BlockNo, int hdSeedId);
}
