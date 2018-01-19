package com.dasset.wallet.core.crypto.bip44;

import org.apache.commons.lang3.ArrayUtils;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Utils;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;

import java.math.BigInteger;

/**
 * HDAddress.java : an address in a BIP44 wallet account chain
 */
public class HDAddress {

    private int childNumber;
    private String path;
    private ECKey ecKey;
    private byte[] publicKey;
    private byte[] publicKeyHash;
    private NetworkParameters networkParameters;

    /**
     * Constructor an HD address.
     *
     * @param networkParameters NetworkParameters
     * @param cKey              deterministic key for this address
     * @param child             index of this address in its chain
     */
    public HDAddress(NetworkParameters networkParameters, DeterministicKey cKey, int child) {
        this.networkParameters = networkParameters;
        this.childNumber = child;
        DeterministicKey deterministicKey = HDKeyDerivation.deriveChildKey(cKey, new ChildNumber(childNumber, false));
        // compressed WIF private key format
        if (deterministicKey.hasPrivKey()) {
            byte[] prepended0Byte = ArrayUtils.addAll(new byte[1], deterministicKey.getPrivKeyBytes());
            this.ecKey = ECKey.fromPrivate(new BigInteger(prepended0Byte), true);
        } else {
            this.ecKey = ECKey.fromPublicOnly(deterministicKey.getPubKey());
        }
        ecKey.setCreationTimeSeconds(Utils.now().getTime() / 1000);// use Unix time (in seconds)
        this.publicKey = ecKey.getPubKey();
        this.publicKeyHash = ecKey.getPubKeyHash();
        this.path = deterministicKey.getPathAsString();
    }

    /**
     * Get publicKey as byte array.
     *
     * @return byte[]
     */
    public byte[] getPublicKey() {
        return publicKey;
    }

    /**
     * Get publicKeyHash as byte array.
     *
     * @return byte[]
     */
    public byte[] getPublicKeyHash() {
        return publicKeyHash;
    }

    /**
     * Return public address for this instance.
     *
     * @return String
     */
    public String getAddressString() {
        if (ecKey != null) {
            return ecKey.toAddress(networkParameters).toString();
        }
        return null;
    }

    /**
     * Return private key for this address (compressed WIF format).
     *
     * @return String
     */
    public String getPrivateKeyString() {
        if (ecKey != null && ecKey.hasPrivKey()) {
            return ecKey.getPrivateKeyEncoded(networkParameters).toString();
        } else {
            return null;
        }
    }

    /**
     * Return Bitcoinj address instance for this HDAddress.
     *
     * @return org.bitcoinj.core.HDAddress
     */
    public org.bitcoinj.core.Address getAddress() {
        if (ecKey != null) {
            return ecKey.toAddress(networkParameters);
        }
        return null;
    }

    /**
     * Return BIP44 path for this address (m / purpose' / coin_type' / account' / chain /
     * address_index).
     *
     * @return String
     */
    public String getPath() {
        return path;
    }

    public int getChildNumber() {
        return childNumber;
    }

}