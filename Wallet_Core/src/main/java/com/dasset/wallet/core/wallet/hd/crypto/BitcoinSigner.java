package com.dasset.wallet.core.wallet.hd.crypto;

public interface BitcoinSigner {

    byte[] makeStandardBitcoinSignature(Sha256Hash transactionSigningHash);
}
