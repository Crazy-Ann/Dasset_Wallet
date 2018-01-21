package com.dasset.wallet.core.wallet;

import com.dasset.wallet.core.contant.Constant;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ArrayUtils;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.params.MainNetParams;
import org.spongycastle.util.encoders.Hex;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PrivateKeyFactory {

    private boolean isTestNet;
    private static PrivateKeyFactory privateKeyFactory;

    private PrivateKeyFactory() {
        // cannot be instantiated
    }

    public static synchronized PrivateKeyFactory getInstance() {
        if (privateKeyFactory == null) {
            privateKeyFactory = new PrivateKeyFactory();
        }
        return privateKeyFactory;
    }

    public static void releaseInstance() {
        if (privateKeyFactory != null) {
            privateKeyFactory = null;
        }
    }

    public void setTestNet(boolean testNet) {
        isTestNet = testNet;
    }

    public String getFormat(String key) {
        // 51 characters base58, always starts with a '5'  (or '9', for testnet)
        if (!isTestNet && key.matches("^5[1-9A-HJ-NP-Za-km-z]{50}$") || isTestNet && key.matches("^9[1-9A-HJ-NP-Za-km-z]{50}$")) {
            return Constant.WIF_UNCOMPRESSED;
            // 52 characters, always starts with 'K' or 'L' (or 'c' for testnet)
        } else if (!isTestNet && key.matches("^[LK][1-9A-HJ-NP-Za-km-z]{51}$") || isTestNet && key.matches("^[c][1-9A-HJ-NP-Za-km-z]{51}$")) {
            return Constant.WIF_COMPRESSED;
        } else if (key.matches("^[1-9A-HJ-NP-Za-km-z]{44}$") || key.matches("^[1-9A-HJ-NP-Za-km-z]{43}$")) {
            return Constant.BASE58;
        } else if (key.matches("^[A-Fa-f0-9]{64}$")) {
            return Constant.HEX;
        } else if (key.matches("^[A-Za-z0-9/=+]{44}$")) {
            return Constant.BASE64;
        } else if (key.matches("^6P[1-9A-HJ-NP-Za-km-z]{56}$")) {
            return Constant.BIP38;
        } else if (key.matches("^S[1-9A-HJ-NP-Za-km-z]{21}$") || key.matches("^S[1-9A-HJ-NP-Za-km-z]{25}$") || key.matches("^S[1-9A-HJ-NP-Za-km-z]{29}$") || key.matches("^S[1-9A-HJ-NP-Za-km-z]{30}$")) {
            try {
                String data = key + "?";
                Hash hash = new Hash(MessageDigest.getInstance(Constant.SHA_256).digest(data.getBytes(Constant.UTF_8)));
                if ((hash.getHash()[0] == 0x00)) {
                    return Constant.MINI;
                }
            } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return null;
        } else {
            return null;
        }
    }

    public ECKey getECKey(String format, String data, boolean compressed) throws Exception {
        switch (format) {
            case Constant.WIF_UNCOMPRESSED:
            case Constant.WIF_COMPRESSED:
                return DumpedPrivateKey.fromBase58(MainNetParams.get(), data).getKey();
            case Constant.BASE58:
                return decodeBase5PrivateKey(data);
            case Constant.BASE64:
                return decodeBase64PrivateKey(data);
            case Constant.HEX:
                return decodeHexPrivateKey(data, compressed);
            case Constant.MINI:
                //todo
                return decodeMiniPrivateKey(data, false);
            default:
                throw new Exception("Unknown key format: " + format);
        }
    }

    private ECKey decodeBase5PrivateKey(String base58PrivateKey) {
        // Prepend a zero byte to make the biginteger unsigned
        return ECKey.fromPrivate(new BigInteger(ArrayUtils.addAll(new byte[1], Base58.decode(base58PrivateKey))), true);
    }

    private ECKey decodeBase64PrivateKey(String base64PrivateKey) {
        // Prepend a zero byte to make the biginteger unsigned
        return ECKey.fromPrivate(new BigInteger(ArrayUtils.addAll(new byte[1], Base64.decodeBase64(base64PrivateKey.getBytes()))), true);
    }

    private ECKey decodeHexPrivateKey(String hexPrivateKey, boolean compressed) {
        // Prepend a zero byte to make the biginteger unsigned
        return ECKey.fromPrivate(new BigInteger(ArrayUtils.addAll(new byte[1], Hex.decode(hexPrivateKey))), compressed);
    }

    private ECKey decodeMiniPrivateKey(String miniPrivateKey, boolean compressed) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        return decodeHexPrivateKey(new Hash(MessageDigest.getInstance(Constant.SHA_256).digest(miniPrivateKey.getBytes(Constant.UTF_8))).toString(), compressed);
//        return determineKey(new Hash(MessageDigest.getInstance(Constant.SHA_256).digest(miniPrivateKey.getBytes(Constant.UTF_8))).toString());
    }

//    private ECKey determineKey(String hash) {
//        ECKey uncompressedECKey = decodeHexPrivateKey(hash, false);
//        ECKey compressedECKey = decodeHexPrivateKey(hash, true);
//        try {
//            String uncompressedAddress = uncompressedECKey.toAddress(MainNetParams.get()).toString();
//            String compressedAddress = compressedECKey.toAddress(MainNetParams.get()).toString();
//            ArrayList<String> ecKyes = Lists.newArrayList();
//            ecKyes.add(uncompressedAddress);
//            ecKyes.add(compressedAddress);
//            BlockExplorer blockExplorer = new BlockExplorer(BlockchainFramework.getRetrofitExplorerInstance(), BlockchainFramework.getApiCode());
//            Call<HashMap<String, Balance>> call = blockExplorer.getBalance(ecKyes, FilterType.RemoveUnspendable);
//            Response<HashMap<String, Balance>> exe = call.execute();
//            if (!exe.isSuccessful()) {
//                throw new ApiException("Failed to connect to server.");
//            }
//            HashMap<String, Balance> body = exe.body();
//            BigInteger uncompressedBalance = body.get(uncompressedAddress).getFinalBalance();
//            BigInteger compressedBalance = body.get(compressedAddress).getFinalBalance();
//            if (compressedBalance != null && compressedBalance.compareTo(BigInteger.ZERO) == 0 && uncompressedBalance != null && uncompressedBalance.compareTo(BigInteger.ZERO) == 1) {
//                return uncompressedECKey;
//            } else {
//                return compressedECKey;
//            }
//        } catch (Exception e) {
//            // TODO: 08/03/2017 Is this safe? Could this not return an uninitialized ECKey?
//            e.printStackTrace();
//            return compressedECKey;
//        }
//    }

    private byte[] hash(byte[] data, int offset, int len) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(Constant.SHA_256);
            messageDigest.update(data, offset, len);
            return messageDigest.digest(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] hash(byte[] data) {
        return hash(data, 0, data.length);
    }
}
