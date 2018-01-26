package com.dasset.wallet.core.db.facade.implement;

import com.dasset.wallet.core.db.facade.IAddressProvider;
import com.dasset.wallet.core.db.facade.IBlockProvider;
import com.dasset.wallet.core.db.facade.IDesktopAddressProvider;
import com.dasset.wallet.core.db.facade.IDesktopTxProvider;
import com.dasset.wallet.core.db.facade.IEnterpriseHDMProvider;
import com.dasset.wallet.core.db.facade.IHDAccountAddressProvider;
import com.dasset.wallet.core.db.facade.IHDAccountProvider;
import com.dasset.wallet.core.db.facade.IPeerProvider;
import com.dasset.wallet.core.db.facade.ITxProvider;
import com.dasset.wallet.core.db.facade.BaseProvider;

public class ProviderFactory extends BaseProvider {


    @Override
    public IBlockProvider intializeBlockProvider() {
        return BlockProvider.getInstance();
    }

    @Override
    public IPeerProvider intializePeerProvider() {
        return PeerProvider.getInstance();
    }

    @Override
    public ITxProvider intializeTxProvider() {
        return TxProvider.getInstance();
    }

    @Override
    public IAddressProvider intializeAddressProvider() {
        return AddressProvider.getInstance();
    }

    @Override
    public IHDAccountAddressProvider intializeHDAccountAddressProvider() {
        return HDAccountAddressProvider.getInstance();
    }

    @Override
    public IHDAccountProvider intializeHDAccountProvider() {
        return HDAccountProvider.getInstance();
    }

    @Override
    public IEnterpriseHDMProvider intializeEnterpriseHDMProvider() {
        return null;
    }

    @Override
    public IDesktopAddressProvider intializeEnDesktopAddressProvider() {
        return null;
    }

    @Override
    public IDesktopTxProvider intializeDesktopTxProvider() {
        return null;
    }
}
