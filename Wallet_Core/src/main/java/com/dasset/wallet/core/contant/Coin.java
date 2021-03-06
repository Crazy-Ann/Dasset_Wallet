package com.dasset.wallet.core.contant;


import com.dasset.wallet.core.crypto.TransactionSignature;

public enum Coin {

    BTC, BCC, BTG, SBTC, BTW, BCD, BTF, BTP, BTN;

    public SplitCoin getSplitCoin() {
        switch (this) {
            case BCC:
                return SplitCoin.BCC;
            case BTG:
                return SplitCoin.BTG;
            case SBTC:
                return SplitCoin.SBTC;
            case BTW:
                return SplitCoin.BTW;
            case BCD:
                return SplitCoin.BCD;
            case BTF:
                return SplitCoin.BTF;
            case BTP:
                return SplitCoin.BTP;
            case BTN:
                return SplitCoin.BTN;
        }
        return SplitCoin.BCC;
    }

    public long getForkBlockHeight() {
        return this.getSplitCoin().getForkBlockHeight();
    }

    public SigHash getSigHash() {
        switch (this) {
            case BCD:
            case BTF:
                return SigHash.BTFFORK;
            case BTP:
                return SigHash.BTPFORK;
            case BTN:
                return SigHash.BTNFORK;
            case BTC:
                return SigHash.ALL;
            case BCC:
                return SigHash.BCCFORK;
            case BTG:
                return SigHash.BTGFORK;
            case BTW:
                return SigHash.BTWFORK;
            case SBTC:
                return SigHash.SBTCFORK;
        }
        return SigHash.ALL;
    }

    static public Coin getCoin(int sigHashValue) {
        for (Coin coin : Coin.values()) {
            if (coin.getSigHash().value == sigHashValue) {
                return coin;
            }
        }
        return BTC;
    }

    public int getP2shHeader() {
        switch (this) {
            case BTP:
                return BitherjSettings.btpP2shHeader;
            case BTF:
                return BitherjSettings.btfP2shHeader;
            case BTG:
                return BitherjSettings.btgP2shHeader;
            case BTW:
                return BitherjSettings.btwP2shHeader;
            default:
                return BitherjSettings.p2shHeader;
        }
    }

    public int getAddressHeader() {
        switch (this) {
            case BTP:
                return BitherjSettings.btpAddressHeader;
            case BTF:
                return BitherjSettings.btfAddressHeader;
            case BTG:
                return BitherjSettings.btgAddressHeader;
            case BTW:
                return BitherjSettings.btwAddressHeader;
            default:
                return BitherjSettings.addressHeader;
        }
    }

    public long getSplitNormalFee() {
        return getSplitCoin().getSplitNormalFee();
    }
}
