package com.dasset.wallet.core.wallet.hd.crypto;

public interface RandomSource {
    /**
     * Generates a user specified number of random bytes
     *
     * @param bytes The array to fill with random bytes
     */
    void nextBytes(byte[] bytes);
}
