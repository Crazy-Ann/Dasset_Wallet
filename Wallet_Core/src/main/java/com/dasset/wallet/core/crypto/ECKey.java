package com.dasset.wallet.core.crypto;

import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.core.exception.KeyCrypterException;
import com.dasset.wallet.core.utils.Sha256Hash;
import com.dasset.wallet.core.utils.Utils;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

import org.spongycastle.asn1.ASN1InputStream;
import org.spongycastle.asn1.ASN1Integer;
import org.spongycastle.asn1.ASN1OctetString;
import org.spongycastle.asn1.DERBitString;
import org.spongycastle.asn1.DEROctetString;
import org.spongycastle.asn1.DERSequenceGenerator;
import org.spongycastle.asn1.DERTaggedObject;
import org.spongycastle.asn1.DLSequence;
import org.spongycastle.asn1.sec.SECNamedCurves;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.asn1.x9.X9IntegerConverter;
import org.spongycastle.crypto.AsymmetricCipherKeyPair;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.crypto.ec.CustomNamedCurves;
import org.spongycastle.crypto.generators.ECKeyPairGenerator;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.crypto.params.ECKeyGenerationParameters;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.crypto.signers.ECDSASigner;
import org.spongycastle.crypto.signers.HMacDSAKCalculator;
import org.spongycastle.math.ec.ECAlgorithms;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.math.ec.FixedPointUtil;
import org.spongycastle.math.ec.custom.sec.SecP256K1Curve;
import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.util.Arrays;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

// TODO: This class is quite a mess by now. Once users are migrated away from Java serialization for the wallets,
// refactor this to have better internal layout and a more consistent API.

/**
 * <p>Represents an elliptic curve public and (optionally) private key, usable for digital signatures but not encryption.
 * Creating a new ECKey with the empty constructor will generate a new random keypair. Other constructors can be used
 * when you already have the public or private parts. If you create a key with only the public part, you can check
 * signatures but not create them.</p>
 * <p/>
 * <p>ECKey also provides access to Bitcoin-Qt compatible text message signing, as accessible via the UI or JSON-RPC.
 * This is slightly different to signing raw bytes - if you want to sign your own data and it won't be exposed as
 * text to people, you don't want to use this. If in doubt, ask on the mailing list.</p>
 * <p/>
 * <p>The ECDSA algorithm supports <i>key recovery</i> in which a signature plus a couple of discriminator bits can
 * be reversed to find the public key used to calculate it. This can be convenient when you have a message and a
 * signature and want to find out who signed it, rather than requiring the user to provide the expected identity.</p>
 */
public class ECKey implements Serializable {

    // The two parts of the key. If "privateKey" is set, "publicKey" can always be calculated. If "publicKey" is set but not "privateKey", we can only verify signatures not make them.
    // TODO: Redesign this class to use consistent internals and more efficient serialization.
    protected BigInteger privateKey;
    protected byte[] publicKey;
    private boolean isFromXRandom;
    // Creation time of the key in seconds since the epoch, or zero if the key was deserialized from a version that did  not have this field.
    protected long creationTimeSeconds;
    // Instance of the KeyCrypter interface to use for encrypting and decrypting the key.
    transient protected KeyCrypter keyCrypter;
    // The encrypted private key information.
    protected EncryptedPrivateKey encryptedPrivateKey;
    // Transient because it's calculated on demand.
    transient private byte[] pubKeyHash;
    //  The parameters of the secp256k1 curve that Bitcoin uses.
    public static final ECDomainParameters CURVE;
    public static final X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName("secp256k1");
    //  Equal to CURVE.getN().shiftRight(1), used for canonicalising the S value of a signature. If you aren't sure what this is about, you can ignore it.
    public static final BigInteger HALF_CURVE_ORDER;

    static {
        // Tell Bouncy Castle to precompute data that's needed during secp256k1 calculations. Increasing the width
        // number makes calculations faster, but at a cost of extra memory usage and with decreasing returns. 12 was
        // picked after consulting with the BC team.
        FixedPointUtil.precompute(CURVE_PARAMS.getG(), 12);
        CURVE = new ECDomainParameters(CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(), CURVE_PARAMS.getH());
        HALF_CURVE_ORDER = CURVE_PARAMS.getN().shiftRight(1);
    }

    /**
     * Generates an entirely new keypair. Point compression is used so the resulting public key will be 33 bytes
     * (32 for the co-ordinate and 1 byte to represent the y bit).
     */
    public static ECKey generateECKey(SecureRandom secureRandom, boolean compressed) {
        ECKeyPairGenerator ecKeyPairGenerator = new ECKeyPairGenerator();
        ECKeyGenerationParameters ecKeyGenerationParameters = new ECKeyGenerationParameters(CURVE, secureRandom);
        ecKeyPairGenerator.init(ecKeyGenerationParameters);
        AsymmetricCipherKeyPair asymmetricCipherKeyPair = ecKeyPairGenerator.generateKeyPair();
        ECPrivateKeyParameters ecPrivateKeyParameters = (ECPrivateKeyParameters) asymmetricCipherKeyPair.getPrivate();
        ECPublicKeyParameters ecPublicKeyParameters = (ECPublicKeyParameters) asymmetricCipherKeyPair.getPublic();
        BigInteger privateKey = ecPrivateKeyParameters.getD();
        ECKey ecKey = new ECKey(privateKey, ecPublicKeyParameters.getQ().getEncoded(compressed));
        ecKey.setCreationTimeSeconds(Utils.currentTimeSeconds());
        return ecKey;
    }

    public final static ECPoint compressPoint(ECPoint uncompressed) {
        return CURVE.getCurve().decodePoint(uncompressed.getEncoded(true));
    }

    public final static ECPoint checkPoint(byte[] pubs) {
        return CURVE.getCurve().decodePoint(pubs);
    }


    /**
     * Creates an ECKey given the private key only.  The public key is calculated from it (this is slow)
     */
    public ECKey(BigInteger privateKey) {
        this(privateKey, null);
    }

    /**
     * A constructor variant with BigInteger pubkey. See {@link net.bither.bitherj.crypto.ECKey#ECKey(BigInteger, byte[])}.
     */
//    public ECKey(BigInteger privKey, BigInteger pubKey) {
//        this(privKey, Utils.bigIntegerToBytes(pubKey, 65));
//    }

    /**
     * Creates an ECKey given only the private key bytes. This is the same as using the BigInteger constructor, but
     * is more convenient if you are importing a key from elsewhere. The public key will be automatically derived
     * from the private key.
     */
    public ECKey(@Nullable byte[] privateKeyBytes, @Nullable byte[] publicKey) {
        this(privateKeyBytes == null ? null : new BigInteger(1, privateKeyBytes), publicKey);
    }

    /**
     * Create a new ECKey with an encrypted private key, a public key and a KeyCrypter.
     *
     * @param encryptedPrivateKey The private key, encrypted,
     * @param pubKey              The keys public key
     * @param keyCrypter          The KeyCrypter that will be used, with an AES key, to encrypt and decryptAESEBC the private key
     */
    public ECKey(@Nullable EncryptedPrivateKey encryptedPrivateKey, @Nullable byte[] pubKey, KeyCrypter keyCrypter) {
        this((byte[]) null, pubKey);

        this.keyCrypter = Preconditions.checkNotNull(keyCrypter);
        this.encryptedPrivateKey = encryptedPrivateKey;
    }

    /**
     * Creates an ECKey given either the private key only, the public key only, or both. If only the private key
     * is supplied, the public key will be calculated from it (this is slow). If both are supplied, it's assumed
     * the public key already correctly matches the public key. If only the public key is supplied, this ECKey cannot
     * be used for signing.
     *
     * @param compressed If set to true and pubKey is null, the derived public key will be in compressed form.
     */
    public ECKey(@Nullable BigInteger privateKey, @Nullable byte[] publicKey, boolean compressed) {
        if (privateKey == null && publicKey == null) {
            throw new IllegalArgumentException("ECKey requires at least private or public key");
        }
        this.privateKey = privateKey;
        this.publicKey = null;
        if (publicKey == null) {
            // Derive public from private.
            this.publicKey = publicKeyFromPrivateKey(privateKey, compressed);
            LogUtil.getInstance().print("-------17-------publicKey(Private):" + Hex.toHexString(this.publicKey));
        } else {
            // We expect the pubkey to be in regular encoded form, just as a BigInteger. Therefore the first byte is
            // a special marker byte.
            // TODO: This is probably not a useful API and may be confusing.
            this.publicKey = publicKey;
            LogUtil.getInstance().print("-------17-------publicKey:" + Hex.toHexString(publicKey));
        }
        LogUtil.getInstance().print("------------------------------------------------------------------");
    }

    /**
     * Creates an ECKey given either the private key only, the public key only, or both. If only the private key
     * is supplied, the public key will be calculated from it (this is slow). If both are supplied, it's assumed
     * the public key already correctly matches the public key. If only the public key is supplied, this ECKey cannot
     * be used for signing.
     */
    private ECKey(@Nullable BigInteger privateKey, @Nullable byte[] publicKey) {
        this(privateKey, publicKey, true);
    }

    public boolean isPublicKeyOnly() {
        return privateKey == null;
    }

    public boolean hasPrivateKey() {
        return privateKey != null;
    }

    /**
     * Output this ECKey as an ASN.1 encoded private key, as understood by OpenSSL or used by the BitCoin reference
     * implementation in its wallet storage format.
     */
    public byte[] toASN1() {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(400);
            // ASN1_SEQUENCE(EC_PRIVATEKEY) = {
            //   ASN1_SIMPLE(EC_PRIVATEKEY, version, LONG),
            //   ASN1_SIMPLE(EC_PRIVATEKEY, privateKey, ASN1_OCTET_STRING),
            //   ASN1_EXP_OPT(EC_PRIVATEKEY, parameters, ECPKPARAMETERS, 0),
            //   ASN1_EXP_OPT(EC_PRIVATEKEY, publicKey, ASN1_BIT_STRING, 1)
            // } ASN1_SEQUENCE_END(EC_PRIVATEKEY)
            DERSequenceGenerator derSequenceGenerator = new DERSequenceGenerator(byteArrayOutputStream);
            derSequenceGenerator.addObject(new ASN1Integer(1)); // version
            derSequenceGenerator.addObject(new DEROctetString(privateKey.toByteArray()));
            derSequenceGenerator.addObject(new DERTaggedObject(0, SECNamedCurves.getByName("secp256k1").toASN1Primitive()));
            derSequenceGenerator.addObject(new DERTaggedObject(1, new DERBitString(getPublicKey())));
            derSequenceGenerator.close();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);  // Cannot happen, writing to memory stream.
        }
    }

    /**
     * Returns public key bytes from the given private key. To convert a byte array into a BigInteger, use <tt>
     * new BigInteger(1, bytes);</tt>
     */
    public static byte[] publicKeyFromPrivateKey(BigInteger privateKey, boolean compressed) {
        ECPoint ecPoint = CURVE.getG().multiply(privateKey);
        if (compressed) {
            ecPoint = compressPoint(ecPoint);
        }
        return ecPoint.getEncoded();
    }

    /**
     * Gets the hash160 form of the public key (as seen in addresses).
     */
    public byte[] getPublicKeyHash() {
        if (pubKeyHash == null) {
            pubKeyHash = Utils.sha256hash160(this.publicKey);
        }
        return pubKeyHash;
    }

    /**
     * Gets the raw public key value. This appears in transaction scriptSigs. Note that this is <b>not</b> the same
     * as the pubKeyHash/address.
     */
    public byte[] getPublicKey() {
        return publicKey;
    }

    /**
     * Gets the public key in the form of an elliptic curve point object from Bouncy Castle.
     */
    public ECPoint getPublicKeyPoint() {
        return CURVE.getCurve().decodePoint(publicKey);
    }

    /**
     * Returns whether this key is using the compressed form or not. Compressed pubkeys are only 33 bytes, not 64.
     */
    public boolean isCompressed() {
        return publicKey.length == 33;
    }

    public boolean isFromXRandom() {
        return isFromXRandom;
    }

    public void setFromXRandom(boolean fromXRandom) {
        isFromXRandom = fromXRandom;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("publicKey:").append(Utils.bytesToHexString(publicKey));
        if (creationTimeSeconds != 0) {
            stringBuilder.append(" timestamp:").append(creationTimeSeconds);
        }
        if (isEncrypted()) {
            stringBuilder.append(" encrypted");
        }
        return stringBuilder.toString();
    }

    /**
     * Produce a string rendering of the ECKey INCLUDING the private key.
     * Unless you absolutely need the private key it is better for security reasons to just use toString().
     */
    public String toStringWithPrivateKey() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(toString());
        if (privateKey != null) {
            stringBuilder.append(" privateKey:").append(Utils.bytesToHexString(privateKey.toByteArray()));
        }
        return stringBuilder.toString();
    }

    /**
     * Returns the address that corresponds to the public part of this ECKey. Note that an address is derived from
     * the RIPEMD-160 hash of the public key and is not the public key itself (which is too large to be convenient).
     */
    public String toAddress() {
        return Utils.toAddress(Utils.sha256hash160(publicKey));
    }

    /**
     * Clears all the ECKey private key contents from memory.
     * WARNING - this method irreversibly deletes the private key information.
     * It turns the ECKEy into a watch only key.
     */
    public void clearPrivateKey() {
        privateKey = BigInteger.ZERO;
        if (encryptedPrivateKey != null) {
            encryptedPrivateKey.clear();
        }
    }

    /**
     * Groups the two components that make up a signature, and provides a way to encode to DER form, which is
     * how ECDSA signatures are represented when embedded in other data structures in the Bitcoin protocol. The raw
     * components can be useful for doing further EC maths on them.
     */
    public static class ECDSASignature {
        // The two components of the signature.
        public BigInteger r, s;

        // Constructs a signature with the given components. Does NOT automatically canonicalise the signature.
        public ECDSASignature(BigInteger r, BigInteger s) {
            this.r = r;
            this.s = s;
        }

        /**
         * Will automatically adjust the S component to be less than or equal to half the curve order, if necessary.
         * This is required because for every signature (r,s) the signature (r, -s (mod N)) is a valid signature of
         * the same message. However, we dislike the ability to modify the bits of a Bitcoin transaction after it's
         * been signed, as that violates various assumed invariants. Thus in future only one of those forms will be
         * considered legal and the other will be banned.
         */
        public void ensureCanonical() {
            if (s.compareTo(HALF_CURVE_ORDER) > 0) {
                // The order of the curve is the number of valid points that exist on that curve. If S is in the upper
                // half of the number of valid points, then bring it back to the lower half. Otherwise, imagine that
                //    N = 10
                //    s = 8, so (-8 % 10 == 2) thus both (r, 8) and (r, 2) are valid solutions.
                //    10 - 8 == 2, giving us always the latter solution, which is canonical.
                s = CURVE.getN().subtract(s);
            }
        }

        /**
         * DER is an international standard for serializing data structures which is widely used in cryptography.
         * It's somewhat like protocol buffers but less convenient. This method returns a standard DER encoding
         * of the signature, as recognized by OpenSSL and other libraries.
         */
        public byte[] encodeToDER() {
            try {
                return derByteStream().toByteArray();
            } catch (IOException e) {
                throw new RuntimeException(e);  // Cannot happen.
            }
        }

        public static ECDSASignature decodeFromDER(byte[] bytes) {
            try {
                ASN1InputStream asn1InputStream = new ASN1InputStream(bytes);
                DLSequence dlSequence = (DLSequence) asn1InputStream.readObject();
                ASN1Integer r, s;
                try {
                    r = (ASN1Integer) dlSequence.getObjectAt(0);
                    s = (ASN1Integer) dlSequence.getObjectAt(1);
                } catch (ClassCastException e) {
                    throw new IllegalArgumentException(e);
                }
                asn1InputStream.close();
                // OpenSSL deviates from the DER spec by interpreting these values as unsigned, though they should not be
                // Thus, we always use the positive versions. See: http://r6.ca/blog/20111119T211504Z.html
                return new ECDSASignature(r.getPositiveValue(), s.getPositiveValue());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        protected ByteArrayOutputStream derByteStream() throws IOException {
            // Usually 70-72 bytes.
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(72);
            DERSequenceGenerator derSequenceGenerator = new DERSequenceGenerator(byteArrayOutputStream);
            derSequenceGenerator.addObject(new ASN1Integer(r));
            derSequenceGenerator.addObject(new ASN1Integer(s));
            derSequenceGenerator.close();
            return byteArrayOutputStream;
        }
    }

    /**
     * Signs the given hash and returns the R and S components as BigIntegers. In the Bitcoin protocol, they are
     * usually encoded using DER format, so you want {@link net.bither.bitherj.crypto.ECKey.ECDSASignature#toASN1()}
     * instead. However sometimes the independent components can be useful, for instance, if you're doing to do
     * further EC maths on them.
     *
     * @throws net.bither.bitherj.crypto.KeyCrypterException if this ECKey doesn't have a private part.
     */
    public ECDSASignature sign(byte[] input) throws KeyCrypterException {
        return sign(input, null);
    }

    /**
     * If this global variable is set to true, sign() creates a dummy signature and verify() always returns true.
     * This is intended to help accelerate unit tests that do a lot of signing/verifying, which in the debugger
     * can be painfully slow.
     */
    @VisibleForTesting
    public static boolean FAKE_SIGNATURES = false;

    /**
     * Signs the given hash and returns the R and S components as BigIntegers. In the Bitcoin protocol, they are
     * usually encoded using DER format, so you want {@link net.bither.bitherj.crypto.ECKey.ECDSASignature#encodeToDER()}
     * instead. However sometimes the independent components can be useful, for instance, if you're doing to do further
     * EC maths on them.
     *
     * @param aesKey The AES key to use for decryption of the private key. If null then no decryption is required.
     *
     * @throws net.bither.bitherj.crypto.KeyCrypterException if this ECKey doesn't have a private part.
     */
    public ECDSASignature sign(byte[] input, @Nullable KeyParameter aesKey) throws KeyCrypterException {
        if (FAKE_SIGNATURES) {
            return TransactionSignature.dummy();
        }
        // The private key bytes to use for signing.
        BigInteger privateKeyForSigning;
        if (isEncrypted()) {
            // The private key needs decrypting before use.
            if (aesKey == null) {
                throw new KeyCrypterException("This ECKey is encrypted but no decryption key has been supplied.");
            }
            if (keyCrypter == null) {
                throw new KeyCrypterException("There is no KeyCrypter to decryptAESEBC the private key for signing.");
            }
            privateKeyForSigning = new BigInteger(1, keyCrypter.decrypt(encryptedPrivateKey, aesKey));
            // Check encryption was correct.
            if (!Arrays.equals(publicKey, publicKeyFromPrivateKey(privateKeyForSigning, isCompressed())))
                throw new KeyCrypterException("Could not decryptAESEBC bytes");
        } else {
            // No decryption of private key required.
            if (privateKey == null) {
                throw new KeyCrypterException("This ECKey does not have the private key necessary for signing.");
            } else {
                privateKeyForSigning = privateKey;
            }
        }

        ECDSASigner ecdsaSigner = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
        ECPrivateKeyParameters ecPrivateKeyParameters = new ECPrivateKeyParameters(privateKeyForSigning, CURVE);
        ecdsaSigner.init(true, ecPrivateKeyParameters);
        BigInteger[] components = ecdsaSigner.generateSignature(input);
        final ECDSASignature ecdsaSignature = new ECDSASignature(components[0], components[1]);
        ecdsaSignature.ensureCanonical();
        return ecdsaSignature;
    }

    /**
     * <p>Verifies the given ECDSA signature against the message bytes using the public key bytes.</p>
     * <p/>
     * <p>When using native ECDSA verification, data must be 32 bytes, and no element may be
     * larger than 520 bytes.</p>
     *
     * @param data      Hash of the data to verify.
     * @param signature ASN.1 encoded signature.
     * @param pub       The public key bytes to use.
     */
    public static boolean verify(byte[] data, ECDSASignature signature, byte[] pub) {
        if (FAKE_SIGNATURES) {
            return true;
        }
        if (NativeSecp256k1.enabled) {
            return NativeSecp256k1.verify(data, signature.encodeToDER(), pub);
        }
        ECDSASigner signer = new ECDSASigner();
        ECPublicKeyParameters params = new ECPublicKeyParameters(CURVE.getCurve().decodePoint(pub), CURVE);
        signer.init(false, params);
        try {
            return signer.verifySignature(data, signature.r, signature.s);
        } catch (NullPointerException e) {
            // Bouncy Castle contains a bug that can cause NPEs given specially crafted signatures. Those signatures
            // are inherently invalid/attack sigs so we just fail them here rather than crash the thread.
            LogUtil.getInstance().print("Caught NPE inside bouncy castle");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Verifies the given ASN.1 encoded ECDSA signature against a hash using the public key.
     *
     * @param data      Hash of the data to verify.
     * @param signature ASN.1 encoded signature.
     * @param pub       The public key bytes to use.
     */
    public static boolean verify(byte[] data, byte[] signature, byte[] pub) {
        if (NativeSecp256k1.enabled) {
            return NativeSecp256k1.verify(data, signature, pub);
        }
        return verify(data, ECDSASignature.decodeFromDER(signature), pub);
    }

    /**
     * Verifies the given ASN.1 encoded ECDSA signature against a hash using the public key.
     *
     * @param data      Hash of the data to verify.
     * @param signature ASN.1 encoded signature.
     */
    public boolean verify(byte[] data, byte[] signature) {
        return ECKey.verify(data, signature, getPublicKey());
    }

    /**
     * Verifies the given R/S pair (signature) against a hash using the public key.
     */
    public boolean verify(Sha256Hash sigHash, ECDSASignature signature) {
        return ECKey.verify(sigHash.getBytes(), signature, getPublicKey());
    }

    /**
     * Returns true if this pubkey is canonical, i.e. the correct length taking into account compression.
     */
    public boolean isPubKeyCanonical() {
        return isPubKeyCanonical(publicKey);
    }

    /**
     * Returns true if the given pubkey is canonical, i.e. the correct length taking into account compression.
     */
    public static boolean isPubKeyCanonical(byte[] publickey) {
        if (publickey.length < 33)
            return false;
        if (publickey[0] == 0x04) {
            // Uncompressed pubkey
            if (publickey.length != 65)
                return false;
        } else if (publickey[0] == 0x02 || publickey[0] == 0x03) {
            // Compressed pubkey
            if (publickey.length != 33)
                return false;
        } else
            return false;
        return true;
    }

    private static BigInteger extractPrivateKeyFromASN1(byte[] asn1privkey) {
        // To understand this code, see the definition of the ASN.1 format for EC private keys in the OpenSSL source
        // code in ec_asn1.c:
        //
        // ASN1_SEQUENCE(EC_PRIVATEKEY) = {
        //   ASN1_SIMPLE(EC_PRIVATEKEY, version, LONG),
        //   ASN1_SIMPLE(EC_PRIVATEKEY, privateKey, ASN1_OCTET_STRING),
        //   ASN1_EXP_OPT(EC_PRIVATEKEY, parameters, ECPKPARAMETERS, 0),
        //   ASN1_EXP_OPT(EC_PRIVATEKEY, publicKey, ASN1_BIT_STRING, 1)
        // } ASN1_SEQUENCE_END(EC_PRIVATEKEY)
        //
        try {
            ASN1InputStream asn1InputStream = new ASN1InputStream(asn1privkey);
            DLSequence dlSequence = (DLSequence) asn1InputStream.readObject();
            checkArgument(dlSequence.size() == 4, "Input does not appear to be an ASN.1 OpenSSL EC private key");
            checkArgument(((ASN1Integer) dlSequence.getObjectAt(0)).getValue().equals(BigInteger.ONE),
                          "Input is of wrong version");
            byte[] bits = ((ASN1OctetString) dlSequence.getObjectAt(1)).getOctets();
            asn1InputStream.close();
            return new BigInteger(1, bits);
        } catch (IOException e) {
            throw new RuntimeException(e);  // Cannot happen, reading from memory stream.
        }
    }

    /**
     * Signs a text message using the standard Bitcoin messaging signing format and returns the signature as a base64
     * encoded string.
     *
     * @throws IllegalStateException                         if this ECKey does not have the private part.
     * @throws net.bither.bitherj.crypto.KeyCrypterException if this ECKey is encrypted and no AESKey is provided or it does not decryptAESEBC the ECKey.
     */
    public String signMessage(String message) throws KeyCrypterException {
        return signMessage(message, null);
    }

    /**
     * Signs a text message using the standard Bitcoin messaging signing format and returns the signature as a base64
     * encoded string.
     *
     * @throws IllegalStateException                         if this ECKey does not have the private part.
     * @throws net.bither.bitherj.crypto.KeyCrypterException if this ECKey is encrypted and no AESKey is provided or it does not decryptAESEBC the ECKey.
     */
    public String signMessage(String message, @Nullable KeyParameter aesKey) throws KeyCrypterException {
//        if (privateKey == null)
//            throw new IllegalStateException("This ECKey does not have the private key necessary for signing.");
        byte[] data = Utils.formatMessageForSigning(message);
        byte[] hash = Utils.doubleDigest(data);
        byte[] sigData = signHash(hash, aesKey);
        return new String(Base64.encode(sigData), Charset.forName("UTF-8"));
    }

    public byte[] signHash(byte[] hash, @Nullable KeyParameter aesKey) throws KeyCrypterException {
        ECDSASignature ecdsaSignature = sign(hash, aesKey);
        // Now we have to work backwards to figure out the recId needed to recover the signature.
        int recId = -1;
        for (int i = 0; i < 4; i++) {
            ECKey ecKey = ECKey.recoverFromSignature(i, ecdsaSignature, hash, isCompressed());
            if (ecKey != null && Arrays.equals(ecKey.publicKey, publicKey)) {
                recId = i;
                break;
            }
        }
        if (recId == -1) {
            throw new RuntimeException("Could not construct a recoverable key. This should never happen.");
        }
        int headerByte = recId + 27 + (isCompressed() ? 4 : 0);
        byte[] sigData = new byte[65];  // 1 header + 32 bytes for R + 32 bytes for S
        sigData[0] = (byte) headerByte;
        System.arraycopy(Utils.bigIntegerToBytes(ecdsaSignature.r, 32), 0, sigData, 1, 32);
        System.arraycopy(Utils.bigIntegerToBytes(ecdsaSignature.s, 32), 0, sigData, 33, 32);
        return sigData;
    }

    /**
     * Given an arbitrary piece of text and a Bitcoin-format message signature encoded in base64, returns an ECKey
     * containing the public key that was used to sign it. This can then be compared to the expected public key to
     * determine if the signature was correct. These sorts of signatures are compatible with the Bitcoin-Qt/bitcoind
     * format generated by signmessage/verifymessage RPCs and GUI menu options. They are intended for humans to verify
     * their communications with each other, hence the base64 format and the fact that the input is text.
     *
     * @param message         Some piece of human readable text.
     * @param signatureBase64 The Bitcoin-format message signature in base64
     *
     * @throws SignatureException If the public key could not be recovered or if there was a signature format error.
     */
    public static ECKey signedMessageToKey(String message, String signatureBase64) throws SignatureException {
        byte[] signatureEncoded;
        try {
            signatureEncoded = Base64.decode(signatureBase64);
        } catch (RuntimeException e) {
            // This is what you get back from Bouncy Castle if base64 doesn't decode :(
            throw new SignatureException("Could not decode base64", e);
        }
        byte[] messageBytes = Utils.formatMessageForSigning(message);
        return signedMessageToKey(messageBytes, signatureEncoded);

    }

    public static ECKey signedMessageToKey(byte[] messageBytes, byte[] signatureEncoded) throws SignatureException {
        // Parse the signature bytes into r/s and the selector value.
        if (signatureEncoded.length < 65)
            throw new SignatureException("ECSignatureFactory truncated, expected 65 bytes and got " + signatureEncoded.length);
        int header = signatureEncoded[0] & 0xFF;
        // The header byte: 0x1B = first key with even y, 0x1C = first key with odd y,
        //                  0x1D = second key with even y, 0x1E = second key with odd y
        if (header < 27 || header > 34)
            throw new SignatureException("Header byte out of range: " + header);
        BigInteger r = new BigInteger(1, Arrays.copyOfRange(signatureEncoded, 1, 33));
        BigInteger s = new BigInteger(1, Arrays.copyOfRange(signatureEncoded, 33, 65));
        ECDSASignature ecdsaSignature = new ECDSASignature(r, s);
        // Note that the C++ code doesn't actually seem to specify any character encoding. Presumably it's whatever
        // JSON-SPIRIT hands back. Assume UTF-8 for now.
        byte[] messageHash = Utils.doubleDigest(messageBytes);
        boolean compressed = false;
        if (header >= 31) {
            compressed = true;
            header -= 4;
        }
        int recId = header - 27;
        ECKey ecKey = ECKey.recoverFromSignature(recId, ecdsaSignature, messageHash, compressed);
        if (ecKey == null) {
            throw new SignatureException("Could not recover public key from signature");
        }
        return ecKey;
    }

    /**
     * Convenience wrapper around {@link net.bither.bitherj.crypto.ECKey#signedMessageToKey(String, String)}. If the key derived from the
     * signature is not the same as this one, throws a SignatureException.
     */
    public void verifyMessage(String message, String signatureBase64) throws SignatureException {
        if (!Arrays.equals(ECKey.signedMessageToKey(message, signatureBase64).getPublicKey(), publicKey))
            throw new SignatureException("ECSignatureFactory did not match for message");
    }


    /**
     * <p>Given the components of a signature and a selector value, recover and return the public key
     * that generated the signature according to the algorithm in SEC1v2 section 4.1.6.</p>
     * <p/>
     * <p>The recId is an index from 0 to 3 which indicates which of the 4 possible keys is the correct one. Because
     * the key recovery operation yields multiple potential keys, the correct key must either be stored alongside the
     * signature, or you must be willing to try each recId in turn until you find one that outputs the key you are
     * expecting.</p>
     * <p/>
     * <p>If this method returns null it means recovery was not possible and recId should be iterated.</p>
     * <p/>
     * <p>Given the above two points, a correct usage of this method is inside a for loop from 0 to 3, and if the
     * output is null OR a key that is not the one you expect, you try again with the next recId.</p>
     *
     * @param recId      Which possible key to recover.
     * @param sig        the R and S components of the signature, wrapped.
     * @param message    Hash of the data that was signed.
     * @param compressed Whether or not the original pubkey was compressed.
     *
     * @return An ECKey containing only the public part, or null if recovery wasn't possible.
     */
    @Nullable
    public static ECKey recoverFromSignature(int recId, ECDSASignature sig, byte[] message, boolean compressed) {
        ECPoint ecPoint = recoverECPointFromSignature(recId, sig, message);
        if (ecPoint != null) {
            return new ECKey((BigInteger) null, ecPoint.getEncoded(compressed));
        } else {
            return null;
        }
    }

    public static ECPoint recoverECPointFromSignature(int recId, ECDSASignature sig, byte[] message) {
        Preconditions.checkArgument(recId >= 0, "recId must be positive");
        Preconditions.checkArgument(sig.r.signum() >= 0, "r must be positive");
        Preconditions.checkArgument(sig.s.signum() >= 0, "s must be positive");
        Preconditions.checkNotNull(message);
        BigInteger n = CURVE.getN();
        BigInteger i = BigInteger.valueOf((long) recId / 2L);
        BigInteger x = sig.r.add(i.multiply(n));
        BigInteger prime = SecP256K1Curve.q;
        if (x.compareTo(prime) >= 0) {
            return null;
        } else {
            ECPoint R = decompressKey(x, (recId & 1) == 1);
            if (!R.multiply(n).isInfinity()) {
                return null;
            } else {
                BigInteger e = new BigInteger(1, message);
                BigInteger eInv = BigInteger.ZERO.subtract(e).mod(n);
                BigInteger rInv = sig.r.modInverse(n);
                BigInteger srInv = rInv.multiply(sig.s).mod(n);
                BigInteger eInvrInv = rInv.multiply(eInv).mod(n);
                ECPoint q = ECAlgorithms.sumOfTwoMultiplies(CURVE.getG(), eInvrInv, R, srInv);
                return q;
            }

        }
    }

    /**
     * Decompress a compressed public key (x co-ord and low-bit of y-coord).
     */
    private static ECPoint decompressKey(BigInteger xBN, boolean yBit) {
        X9IntegerConverter x9IntegerConverter = new X9IntegerConverter();
        byte[] encode = x9IntegerConverter.integerToBytes(xBN, 1 + x9IntegerConverter.getByteLength(CURVE.getCurve()));
        encode[0] = (byte) (yBit ? 0x03 : 0x02);
        return CURVE.getCurve().decodePoint(encode);
    }

    /**
     * Returns a 32 byte array containing the private key, or null if the key is encrypted or public only
     */
    @Nullable
    public byte[] getPrivKeyBytes() {
        return Utils.bigIntegerToBytes(privateKey, 32);
    }


    /**
     * Sets the creation time of this key. Zero is a convention to mean "unavailable". This method can be useful when
     * you have a raw key you are importing from somewhere else.
     */
    public void setCreationTimeSeconds(long newCreationTimeSeconds) {
        if (newCreationTimeSeconds < 0) {
            throw new IllegalArgumentException("Cannot set creation time to negative value: " + newCreationTimeSeconds);
        }
        creationTimeSeconds = newCreationTimeSeconds;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || !(object instanceof ECKey)) {
            return false;
        }
        ECKey ecKey = (ECKey) object;
        return Arrays.equals(publicKey, ecKey.publicKey);
    }

    @Override
    public int hashCode() {
        // Public keys are random already so we can just use a part of them as the hashcode. Read from the start to
        // avoid picking up the type code (compressed vs uncompressed) which is tacked on the end.
        return (publicKey[0] & 0xFF) | ((publicKey[1] & 0xFF) << 8) | ((publicKey[2] & 0xFF) << 16) | ((publicKey[3] & 0xFF) << 24);
    }

    /**
     * Create an encrypted private key with the keyCrypter and the AES key supplied.
     * This method returns a new encrypted key and leaves the original unchanged.
     * To be secure you need to clear the original, unencrypted private key bytes.
     *
     * @param keyCrypter The keyCrypter that specifies exactly how the encrypted bytes are created.
     * @param aesKey     The KeyParameter with the AES encryption key (usually constructed with keyCrypter#deriveKey and cached as it is slow to create).
     *
     * @return encryptedKey
     */
    public ECKey encrypt(KeyCrypter keyCrypter, KeyParameter aesKey) throws KeyCrypterException {
        Preconditions.checkNotNull(keyCrypter);
        final byte[] privKeyBytes = getPrivKeyBytes();
        checkState(privKeyBytes != null, "Private key is not available");
        EncryptedPrivateKey encryptedPrivateKey = keyCrypter.encrypt(privKeyBytes, aesKey);
        ECKey ecKey = new ECKey(encryptedPrivateKey, getPublicKey(), keyCrypter);
        ecKey.setFromXRandom(this.isFromXRandom);
        return ecKey;
    }

    /**
     * Create a decrypted private key with the keyCrypter and AES key supplied. Note that if the aesKey is wrong, this
     * has some chance of throwing KeyCrypterException due to the corrupted padding that will result, but it can also
     * just yield a garbage key.
     *
     * @param keyCrypter The keyCrypter that specifies exactly how the decrypted bytes are created.
     * @param aesKey     The KeyParameter with the AES encryption key (usually constructed with keyCrypter#deriveKey and cached).
     *
     * @return unencryptedKey
     */
    public ECKey decrypt(KeyCrypter keyCrypter, KeyParameter aesKey) throws KeyCrypterException {
        Preconditions.checkNotNull(keyCrypter);
        // Check that the keyCrypter matches the one used to encrypt the keys, if set.
        if (this.keyCrypter != null && !this.keyCrypter.equals(keyCrypter)) {
            throw new KeyCrypterException("The keyCrypter being used to decryptAESEBC the key is different to the one that was used to encrypt it");
        }
        byte[] unencryptedPrivateKey = keyCrypter.decrypt(encryptedPrivateKey, aesKey);
        ECKey ecKey = new ECKey(new BigInteger(1, unencryptedPrivateKey), null, isCompressed());
        if (!Arrays.equals(ecKey.getPublicKey(), getPublicKey())) {
            throw new KeyCrypterException("Provided AES key is wrong");
        }
        return ecKey;
    }

    /**
     * Check that it is possible to decryptAESEBC the key with the keyCrypter and that the original key is returned.
     * <p/>
     * Because it is a critical failure if the private keys cannot be decrypted successfully (resulting of loss of all bitcoins controlled
     * by the private key) you can use this method to check when you *encrypt* a wallet that it can definitely be decrypted successfully.
     *
     * @return true if the encrypted key can be decrypted back to the original key successfully.
     */
    //todo: See {@link net.bither.bitherj.core.Address#encrypt(KeyCrypter keyCrypter, KeyParameter aesKey)} for example usage.
    public static boolean encryptionIsReversible(ECKey originalKey, ECKey encryptedKey, KeyCrypter keyCrypter, KeyParameter aesKey) {
        String genericErrorText = "The check that encryption could be reversed failed for key " + originalKey.toString() + ". ";
        try {
            ECKey rebornUnencryptedKey = encryptedKey.decrypt(keyCrypter, aesKey);
            if (rebornUnencryptedKey == null) {
                LogUtil.getInstance().print(genericErrorText + "The test decrypted key was missing.");
                return false;
            }
            byte[] originalPrivateKeyBytes = originalKey.getPrivKeyBytes();
            if (originalPrivateKeyBytes != null) {
                if (rebornUnencryptedKey.getPrivKeyBytes() == null) {
                    LogUtil.getInstance().print(genericErrorText + "The test decrypted key was missing.");
                    return false;
                } else {
                    if (originalPrivateKeyBytes.length != rebornUnencryptedKey.getPrivKeyBytes().length) {
                        LogUtil.getInstance().print(genericErrorText + "The test decrypted private key was a different length to the original.");
                        return false;
                    } else {
                        for (int i = 0; i < originalPrivateKeyBytes.length; i++) {
                            if (originalPrivateKeyBytes[i] != rebornUnencryptedKey.getPrivKeyBytes()[i]) {
                                LogUtil.getInstance().print(genericErrorText + "Byte " + i + " of the private key did not match the original.");
                                return false;
                            }
                        }
                    }
                }
            }
        } catch (KeyCrypterException e) {
            e.printStackTrace();
            return false;
        }
        // Key can successfully be decrypted.
        return true;
    }

    /**
     * Indicates whether the private key is encrypted (true) or not (false).
     * A private key is deemed to be encrypted when there is both a KeyCrypter and the encryptedPrivateKey is non-zero.
     */
    public boolean isEncrypted() {
        return keyCrypter != null && encryptedPrivateKey != null && encryptedPrivateKey.getEncryptedBytes() != null && encryptedPrivateKey.getEncryptedBytes().length > 0;
    }

    /**
     * @return The encryptedPrivateKey (containing the encrypted private key bytes and initialisation vector) for this ECKey,
     * or null if the ECKey is not encrypted.
     */
    @Nullable
    public EncryptedPrivateKey getEncryptedPrivateKey() {
        if (encryptedPrivateKey == null) {
            return null;
        } else {
            return encryptedPrivateKey.clone();
        }
    }

    /**
     * @return The KeyCrypter that was used to encrypt to encrypt this ECKey. You need this to decryptAESEBC the ECKey.
     */
    public KeyCrypter getKeyCrypter() {
        return keyCrypter;
    }
}
