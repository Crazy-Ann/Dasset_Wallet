package com.dasset.wallet.core.crypto;

import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.core.exception.KeyCrypterException;
import com.dasset.wallet.core.utils.Utils;
import com.lambdaworks.crypto.SCrypt;

import org.spongycastle.crypto.BufferedBlockCipher;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.engines.AESFastEngine;
import org.spongycastle.crypto.modes.CBCBlockCipher;
import org.spongycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.params.ParametersWithIV;
import org.spongycastle.util.encoders.Hex;

import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import static com.google.common.base.Preconditions.checkNotNull;

public class KeyCrypterScrypt implements KeyCrypter, Serializable {

    private static final int BITCOINJ_SCRYPT_N = 16384;
    private static final int BITCOINJ_SCRYPT_R = 8;
    private static final int BITCOINJ_SCRYPT_P = 1;
    private static final SecureRandom secureRandom;

    /**
     * Key length in bytes.
     */
    public static final int KEY_LENGTH = 32; // = 256 bits.

    /**
     * The size of an AES block in bytes.
     * This is also the length of the initialisation vector.
     */
    public static final int BLOCK_LENGTH = 16;  // = 128 bits.

    /**
     * The length of the salt used.
     */
    public static final int SALT_LENGTH = 8;


    // Scrypt parameters.
    private byte[] salt;

    static {
        secureRandom = new SecureRandom();
    }

    /**
     * Encryption/ Decryption using default parameters and a random salt
     */
    public KeyCrypterScrypt() {
        this.salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);
        LogUtil.getInstance().print("-------5-------salt:" + Hex.toHexString(salt));
    }

    public KeyCrypterScrypt(byte[] salt) {
        this.salt = checkNotNull(salt);
        if (salt == null || salt.length == 0) {
            LogUtil.getInstance().print("You are using a ScryptParameters with no salt. Your encryption may be vulnerable to a dictionary attack.");
        }
    }


    @Override
    public KeyParameter deriveKey(CharSequence password) throws KeyCrypterException {
        byte[] bytes = null;
        try {
            bytes = convertToByteArray(password);
            byte[] salt = new byte[0];
            if (this.salt != null) {
                salt = this.salt;
            } else {
                // Warn the user that they are not using a salt.
                // (Some early MultiBit wallets had a blank salt).
                LogUtil.getInstance().print("You are using a ScryptParameters with no salt. Your encryption may be vulnerable to a dictionary attack.");
            }
            byte[] encryptedPassword = SCrypt.scrypt(bytes, salt, BITCOINJ_SCRYPT_N, BITCOINJ_SCRYPT_R, BITCOINJ_SCRYPT_P, KEY_LENGTH);
            LogUtil.getInstance().print("-------9-------encryptedPassword:" + Hex.toHexString(encryptedPassword));
            return new KeyParameter(encryptedPassword);
        } catch (GeneralSecurityException e) {
            throw new KeyCrypterException("Could not generate key from password and salt.", e);
        } finally {
            // Zero the password bytes.
            if (bytes != null) {
                java.util.Arrays.fill(bytes, (byte) 0);
            }
        }
    }

    /**
     * Password based encryption using AES - CBC 256 bits.
     */
    @Override
    public EncryptedPrivateKey encrypt(byte[] plainBytes, KeyParameter keyParameter) throws KeyCrypterException {
        checkNotNull(plainBytes);
        checkNotNull(keyParameter);
        try {
            // Generate iv - each encryption call has a different iv.
            byte[] iv = new byte[BLOCK_LENGTH];
            secureRandom.nextBytes(iv);
            LogUtil.getInstance().print("-------6-------iv(初始向量):" + Hex.toHexString(iv));
            ParametersWithIV parametersWithIV = new ParametersWithIV(keyParameter, iv);
            // Encrypt using AES.
            BufferedBlockCipher bufferedBlockCipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESFastEngine()));
            bufferedBlockCipher.init(true, parametersWithIV);
            byte[] encryptedBytes = new byte[bufferedBlockCipher.getOutputSize(plainBytes.length)];
            LogUtil.getInstance().print("-------7-------toEncryptedBytes:" + Hex.toHexString(plainBytes));
            int length = bufferedBlockCipher.processBytes(plainBytes, 0, plainBytes.length, encryptedBytes, 0);
            LogUtil.getInstance().print("-------8-------length:" + String.valueOf(length));
            bufferedBlockCipher.doFinal(encryptedBytes, length);
            return new EncryptedPrivateKey(iv, encryptedBytes);
        } catch (InvalidCipherTextException e) {
            throw new KeyCrypterException("Could not encrypt bytes.", e);
        }
    }

    /**
     * Decrypt bytes previously encrypted with this class.
     *
     * @param privateKeyToDecode The private key to decryptAESEBC
     * @param aesKey             The AES key to use for decryption
     *
     * @return The decrypted bytes
     *
     * @throws KeyCrypterException if bytes could not be decoded to a valid key
     */
    @Override
    public byte[] decrypt(EncryptedPrivateKey privateKeyToDecode, KeyParameter aesKey) throws KeyCrypterException {
        checkNotNull(privateKeyToDecode);
        checkNotNull(aesKey);
        try {
            ParametersWithIV parametersWithIV = new ParametersWithIV(new KeyParameter(aesKey.getKey()), privateKeyToDecode.getInitialisationVector());
            // Decrypt the message.
            BufferedBlockCipher bufferedBlockCipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESFastEngine()));
            bufferedBlockCipher.init(false, parametersWithIV);
            byte[] cipherBytes = privateKeyToDecode.getEncryptedBytes();
            int minimumSize = bufferedBlockCipher.getOutputSize(cipherBytes.length);
            byte[] outputBuffer = new byte[minimumSize];
            int length1 = bufferedBlockCipher.processBytes(cipherBytes, 0, cipherBytes.length, outputBuffer, 0);
            int length2 = bufferedBlockCipher.doFinal(outputBuffer, length1);
            int actualLength = length1 + length2;
            byte[] decryptedBytes = new byte[actualLength];
            System.arraycopy(outputBuffer, 0, decryptedBytes, 0, actualLength);
            Utils.wipeBytes(outputBuffer);
            return decryptedBytes;
        } catch (Exception e) {
            throw new KeyCrypterException("Could not decryptAESEBC bytes", e);
        }
    }

    /**
     * Convert a CharSequence (which are UTF16) into a byte array.
     * <p/>
     * Note: a String.getBytes() is not used to avoid creating a String of the password in the JVM.
     */
    private static byte[] convertToByteArray(CharSequence charSequence) {
        checkNotNull(charSequence);
        byte[] bytes = new byte[charSequence.length() << 1];
        for (int i = 0; i < charSequence.length(); i++) {
            int bytePosition = i << 1;
            bytes[bytePosition] = (byte) ((charSequence.charAt(i) & 0xFF00) >> 8);
            bytes[bytePosition + 1] = (byte) (charSequence.charAt(i) & 0x00FF);
        }
        return bytes;
    }

    public byte[] getSalt() {
        return this.salt;
    }


    @Override
    public String toString() {
        return "Scrypt/AES";
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(this.salt);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof KeyCrypterScrypt)) {
            return false;
        }
        final KeyCrypterScrypt other = (KeyCrypterScrypt) obj;

        return com.google.common.base.Objects.equal(this.salt, other.getSalt());
    }
}
