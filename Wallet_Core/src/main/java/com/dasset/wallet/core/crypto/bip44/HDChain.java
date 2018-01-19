package com.dasset.wallet.core.crypto.bip44;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;

/**
 * HDChain.java : a chain in a BIP44 wallet account
 */
public class HDChain {

    private DeterministicKey cKey;
    private boolean hasReceived;
    private String path;
    static private final int DESIRED_MARGIN = 32;
    static private final int ADDRESS_GAP_MAX = 20;
    private NetworkParameters networkParameters;
    public static final int RECEIVE_CHAIN = 0;
    public static final int CHANGE_CHAIN = 1;

    /**
     * Constructor for a chain.
     *
     * @param networkParameters NetworkParameters
     * @param aKey              deterministic key for this chain
     * @param hasReceived       this is the receive chain
     */
    public HDChain(NetworkParameters networkParameters, DeterministicKey aKey, boolean hasReceived) {
        this.networkParameters = networkParameters;
        this.hasReceived = hasReceived;
        int chain = hasReceived ? RECEIVE_CHAIN : CHANGE_CHAIN;
        this.cKey = HDKeyDerivation.deriveChildKey(aKey, chain);
        this.path = cKey.getPathAsString();
    }

    /**
     * Test if this is the receive chain.
     *
     * @return boolean
     */
    public boolean hasHasReceived() {
        return hasReceived;
    }

    /**
     * Return HDAddress at provided index into chain.
     *
     * @return HDAddress
     */
    public HDAddress getAddressAt(int addressIndex) {
        return new HDAddress(networkParameters, cKey, addressIndex);
    }

    /**
     * Return BIP44 path for this chain (m / purpose' / coin_type' / account' / chain).
     *
     * @return String
     */
    public String getPath() {
        return path;
    }

}