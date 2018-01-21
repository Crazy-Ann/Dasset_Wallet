package com.dasset.wallet.core.contant;

public enum BitcoinUnit {

    BTC(100000000), bits(100), BTW(10000), BCD(10000000);

    public long satoshis;

    BitcoinUnit(long satoshis) {
        this.satoshis = satoshis;
    }
    
}
