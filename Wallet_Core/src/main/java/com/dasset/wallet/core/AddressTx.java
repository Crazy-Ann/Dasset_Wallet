package com.dasset.wallet.core;

public class AddressTx {

    private String address;
    private String txHash;

    public AddressTx(String address, String txHash) {
        this.address = address;
        this.txHash = txHash;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
