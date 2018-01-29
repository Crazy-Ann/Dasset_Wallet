package com.dasset.wallet.core.db.facade.implement;

import com.dasset.wallet.components.utils.LogUtil;
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
        LogUtil.getInstance().print("intializeBlockProvider");
        return BlockProvider.getInstance();
    }

    @Override
    public IPeerProvider intializePeerProvider() {
        LogUtil.getInstance().print("intializePeerProvider");
        return PeerProvider.getInstance();
    }

    @Override
    public ITxProvider intializeTxProvider() {
        LogUtil.getInstance().print("intializeTxProvider");
        return TxProvider.getInstance();
    }

    @Override
    public IAddressProvider intializeAddressProvider() {
        LogUtil.getInstance().print("intializeAddressProvider");
        return AddressProvider.getInstance();
    }

    @Override
    public IHDAccountAddressProvider intializeHDAccountAddressProvider() {
        LogUtil.getInstance().print("intializeHDAccountAddressProvider");
        return HDAccountAddressProvider.getInstance();
    }

    @Override
    public IHDAccountProvider intializeHDAccountProvider() {
        LogUtil.getInstance().print("intializeHDAccountProvider");
        return HDAccountProvider.getInstance();
    }

    @Override
    public IEnterpriseHDMProvider intializeEnterpriseHDMProvider() {
        LogUtil.getInstance().print("intializeEnterpriseHDMProvider");
        return null;
    }

    @Override
    public IDesktopAddressProvider intializeEnDesktopAddressProvider() {
        LogUtil.getInstance().print("intializeEnDesktopAddressProvider");
        return null;
    }

    @Override
    public IDesktopTxProvider intializeDesktopTxProvider() {
        LogUtil.getInstance().print("intializeDesktopTxProvider");
        return null;
    }
}
