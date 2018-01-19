package com.dasset.wallet.core.random;


import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.core.random.provider.LinuxSecureRandomProvider;
import com.dasset.wallet.core.wallet.Constant;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.SecureRandomSpi;
import java.security.Security;

/**
 * A SecureRandom implementation that is able to override the standard JVM
 * provided implementation, and which simply serves random numbers by reading
 * /dev/FILE_INPUT_STREAM. That is, it delegates to the kernel on UNIX systems and is
 * unusable on other platforms. Attempts to manually set the seed are ignored.
 * There is no difference between seed bytes and non-seed bytes, they are all
 * from the same source.
 */
public class LinuxSecureRandom extends SecureRandomSpi {

    private static final FileInputStream FILE_INPUT_STREAM;
    private final DataInputStream dataInputStream;

    static {
        try {
            File file = new File(Constant.FILE_URANDOM);
            if (file.exists()) {
                // This stream is deliberately leaked.
                FILE_INPUT_STREAM = new FileInputStream(file);
                // Now override the default SecureRandom implementation witht this one.
                Security.insertProviderAt(new LinuxSecureRandomProvider(LinuxSecureRandom.class.getName(), 1.0, "A Linux specific random number provider that uses /dev/FILE_INPUT_STREAM"), 1);
            } else {
                FILE_INPUT_STREAM = null;
                Security.insertProviderAt(new LinuxSecureRandomProvider(LinuxSecureRandom.class.getName(), 1.0, "A Linux specific random number provider that uses /dev/FILE_INPUT_STREAM"), 1);
            }
        } catch (FileNotFoundException e) {
            // Should never happen.
            throw new RuntimeException(e);
        }
    }

    public LinuxSecureRandom() {
        // DataInputStream is not thread safe, so each random object has its own.
        dataInputStream = new DataInputStream(FILE_INPUT_STREAM);
    }

    @Override
    protected void engineSetSeed(byte[] bytes) {
        // Ignore.
    }

    @Override
    protected void engineNextBytes(byte[] bytes) {
        if (FILE_INPUT_STREAM == null) {
            throw new RuntimeException("Not has dev/urandom in your device");
        }
        try {
            LogUtil.getInstance().print(String.format("LinuxSecureRandom get %s bytes.", bytes.length));
            // This will block until all the bytes can be read.
            dataInputStream.readFully(bytes);
        } catch (IOException e) {
            // Fatal error. Do not attempt to recover from this.
            throw new RuntimeException(e);
        }
    }

    @Override
    protected byte[] engineGenerateSeed(int i) {
        byte[] bytes = new byte[i];
        engineNextBytes(bytes);
        return bytes;
    }
}
