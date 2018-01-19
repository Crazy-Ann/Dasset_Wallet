package com.dasset.wallet.core.wallet.hd.crypto;

/**
 * A key cipher allows encrypting and decrypting of arbitrary data with
 * integrity checks.
 */
public interface KeyCipher {

    /**
     * Get the thumbprint of this key cipher
     *
     * @return the thumbprint of this key cipher
     */
    public long getThumbprint();

    /**
     * Decrypt an array of bytes
     *
     * @param data the data to decrypt
     *
     * @return the decrypted data
     *
     * @throws InvalidKeyCipherException If the integrity check failed while decrypting
     */
    public byte[] decrypt(byte[] data) throws InvalidKeyCipherException;

    /**
     * Encrypt an array of bytes
     *
     * @param data the data to encrypt
     *
     * @return the encrypted data
     */
    public byte[] encrypt(byte[] data);
}