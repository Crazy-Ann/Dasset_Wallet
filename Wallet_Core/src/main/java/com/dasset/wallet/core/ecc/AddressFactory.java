package com.dasset.wallet.core.ecc;

import com.dasset.wallet.components.utils.LogUtil;

import org.spongycastle.crypto.digests.RIPEMD160Digest;
import org.spongycastle.jcajce.provider.digest.Keccak;
import org.spongycastle.util.Arrays;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import static java.util.Arrays.copyOfRange;

public final class AddressFactory {

    public static String generatorAddress(byte[] publicKey, int type) throws InvalidAlgorithmParameterException, NoSuchProviderException, NoSuchAlgorithmException, IOException {
        switch (type) {
            case Constant.AddressType.BTC: {
                //Convert public key into byte array and prepend 0x04 byte to front
//        byte[] publicKeyBytes = DatatypeConverter.parseHexBinary("04" + publicKey);
                //Perform sha256 hash first
                byte[] shaHashedKey = sha256Hash(publicKey);
                //Perform RIPEMD-160 hash on result
                byte[] ripemdHashedKey = ripeMD160Hash(shaHashedKey);
                //Append 0x00 as main network identifier
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byteArrayOutputStream.write((byte) 0);
                byteArrayOutputStream.write(ripemdHashedKey);
                byte[] hashedKeyWithID = byteArrayOutputStream.toByteArray();
                //Calculate checksum
                byte[] firstSHAHash  = sha256Hash(hashedKeyWithID);
                byte[] secondSHAHash = sha256Hash(firstSHAHash);
                byte[] checksum      = Arrays.copyOfRange(secondSHAHash, 0, 4);
                //Add checksum to end to get binary Bitcoin address
                byteArrayOutputStream.write(checksum);
                byte[] binaryAddress = byteArrayOutputStream.toByteArray();
                //Encode in base58check and print
                return encodeBase58(binaryAddress);
            }
            case Constant.AddressType.ETH: {
                //Convert public key into a byte array to use in hash function
//        byte[] publicKeyBytes = DatatypeConverter.parseHexBinary(publicKey);
                String hashedPublicKey = getAddress(publicKey);
                //Take only last 40 characters of hash as the address
                String address = hashedPublicKey.substring(hashedPublicKey.length() - 40);
                //Prepend a 0x to the address before printing
                LogUtil.getInstance().print("Ethereum Address: 0x" + address);
                return address;
            }
            case Constant.AddressType.HYC: {
                //Convert public key into byte array and prepend 0x04 byte to front
//        byte[] publicKeyBytes = DatatypeConverter.parseHexBinary("04" + publicKey);
                //Perform sha256 hash first
                byte[] shaHashedKey = sha256Hash(publicKey);
                //Perform RIPEMD-160 hash on result
                byte[] ripemdHashedKey = ripeMD160Hash(shaHashedKey);
                //Append 0x00 as main network identifier
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byteArrayOutputStream.write((byte) 0);
                byteArrayOutputStream.write(ripemdHashedKey);
                byte[] hashedKeyWithID = byteArrayOutputStream.toByteArray();
                //Calculate checksum
                byte[] firstSHAHash  = sha256Hash(hashedKeyWithID);
                byte[] secondSHAHash = sha256Hash(firstSHAHash);
                byte[] checksum      = Arrays.copyOfRange(secondSHAHash, 0, 4);
                //Add checksum to end to get binary Bitcoin address
                byteArrayOutputStream.write(checksum);
                byte[] binaryAddress = byteArrayOutputStream.toByteArray();
                //Encode in base58check and print
                return "hc" + encodeBase58(binaryAddress);
            }
            default:
                return null;
        }
    }


    private static byte[] sha256Hash(byte[] input) throws NoSuchAlgorithmException {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        sha256.update(input);
        return sha256.digest();
    }

    private static byte[] ripeMD160Hash(byte[] input) {
        RIPEMD160Digest ripemd160 = new RIPEMD160Digest();
        ripemd160.update(input, 0, input.length);
        byte[] output = new byte[ripemd160.getDigestSize()];
        ripemd160.doFinal(output, 0);
        return output;
    }

    private static String encodeBase58(byte[] input) {
        char[] ALPHABET      = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();
        int    leadingZeroes = 0;
        while (leadingZeroes < input.length && input[leadingZeroes] == 0) {
            ++leadingZeroes;
        }
        //Set array size to maximum possible size
        byte[] temp    = new byte[input.length * 2];
        int    j       = temp.length;
        int    startAt = leadingZeroes;
        while (startAt < input.length) {
            byte mod = divmod58(input, startAt);
            if (input[startAt] == 0) {
                ++startAt;
            }
            temp[--j] = (byte) ALPHABET[mod];
        }
        while (j < temp.length && temp[j] == ALPHABET[0]) {
            ++j;
        }
        while (--leadingZeroes >= 0) {
            temp[--j] = (byte) ALPHABET[0];
        }
        byte[] output = copyOfRange(temp, j, temp.length);
        return new String(output);
    }

    private static byte divmod58(byte[] number, int startAt) {
        int remainder = 0;
        for (int i = startAt; i < number.length; i++) {
            int digit256 = (int) number[i] & 0xFF;
            int temp     = remainder * 256 + digit256;
            number[i] = (byte) (temp / 58);
            remainder = temp % 58;
        }
        return (byte) remainder;
    }

    private static String getAddress(byte[] publicKey) {
        //Use Keccak256 hash on public key
        Keccak.DigestKeccak keccak = new Keccak.Digest256();
        keccak.update(publicKey);
        return toHexString(keccak.digest(), 0, keccak.digest().length);
    }

    public static String toHexString(byte[] input, int offset, int length) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = offset; i < offset + length; i++) {
            stringBuilder.append(String.format("%02x", input[i] & 0xFF));
        }
        return stringBuilder.toString();
    }
}

