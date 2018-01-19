package com.dasset.wallet.core.random.provider;

import com.dasset.wallet.core.random.LinuxSecureRandom;

import java.security.Provider;

public class LinuxSecureRandomProvider extends Provider {


    /**
     * Constructs a provider with the specified name, version number,
     * and information.
     *
     * @param name    the provider name.
     * @param version the provider version number.
     * @param info    a description of the provider and its services.
     */
    public LinuxSecureRandomProvider(String name, double version, String info) {
        super(name, version, info);
//        super("LinuxSecureRandom", 1.0, "A Linux specific random number provider that uses /dev/urandom");
        put(name, LinuxSecureRandom.class.getName());
    }
}
