package com.dasset.wallet.core.random;

import com.dasset.wallet.core.random.listener.UEntropyCollectorListener;
import com.dasset.wallet.core.utils.Sha256Hash;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UEntropyCollector implements IUEntropy, IUEntropySource {

    public static final int POOL_SIZE = 32 * 200;
    private static final int ENTROPY_XOR_MULTIPLIER = (int) Math.pow(2, 4);

    private boolean shouldCollectData;
    private UEntropyCollectorListener uEntropyCollectorListener;
    private PipedInputStream pipedInputStream;
    private PipedOutputStream pipedOutputStream;
    private HashSet<IUEntropySource> iuEntropySources;
    private boolean hasPaused;
    private ExecutorService executorService;

    public UEntropyCollector(UEntropyCollectorListener uEntropyCollectorListener) {
        this.uEntropyCollectorListener = uEntropyCollectorListener;
        this.hasPaused = true;
        this.iuEntropySources = Sets.newHashSet();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void onNewData(final byte[] data, final UEntropySource source) {
        if (!shouldCollectData()) {
            return;
        }
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                if (!shouldCollectData()) {
                    return;
                }
                byte[] processedData = source.processData(data);
                try {
                    int available = pipedInputStream.available();
                    int extraBytes = available + processedData.length - POOL_SIZE;
                    if (extraBytes <= 0) {
                        pipedOutputStream.write(processedData);
                    } else if (extraBytes < processedData.length) {
                        pipedOutputStream.write(Arrays.copyOf(processedData, processedData.length - extraBytes));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void onError(Exception e, IUEntropySource source) {
        if (iuEntropySources.contains(source)) {
            source.onPause();
            iuEntropySources.remove(source);
        }
        if (uEntropyCollectorListener != null) {
            uEntropyCollectorListener.onUEntropySourceError(e, source);
        }
    }

    public void start() throws IOException {
        if (shouldCollectData) {
            return;
        }
        shouldCollectData = true;
        pipedInputStream = new PipedInputStream(POOL_SIZE);
        pipedOutputStream = new PipedOutputStream(pipedInputStream);
    }

    public void stop() {
        if (!shouldCollectData) {
            return;
        }
        shouldCollectData = false;
        try {
            pipedOutputStream.close();
            pipedInputStream.close();
            pipedOutputStream = null;
            pipedInputStream = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean shouldCollectData() {
        return shouldCollectData;
    }

    @Override
    public byte[] nextBytes(int length) {
        byte[] bytes = null;
        if (!shouldCollectData()) {
            throw new IllegalStateException("UEntropyCollector is not running");
        }
        try {
            for (int i = 0;
                 i < ENTROPY_XOR_MULTIPLIER;
                 i++) {
                byte[] itemBytes = new byte[length];
                while (pipedInputStream.available() < itemBytes.length) {
                    if (!shouldCollectData()) {
                        throw new IllegalStateException("UEntropyCollector is not running");
                    }
                }
                pipedInputStream.read(itemBytes);
                if (i == ENTROPY_XOR_MULTIPLIER - 1) {
                    itemBytes = Sha256Hash.create(itemBytes).getBytes();
                }
                if (bytes == null) {
                    bytes = itemBytes;
                } else {
                    for (int k = 0; k < bytes.length && k < itemBytes.length; k++) {
                        bytes[k] = (byte) (bytes[k] ^ itemBytes[k]);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[length];
        }
        return bytes;
    }

    public enum UEntropySource {

        Unknown, Camera(8), Mic(16), Sensor;

        private int bytesInOneBatch;

        UEntropySource(int bytesInOneBatch) {
            this.bytesInOneBatch = bytesInOneBatch;
        }

        UEntropySource() {
            this(1);
        }

        public byte[] processData(byte[] data) {
            if (data.length <= bytesInOneBatch) {
                return data;
            }
            byte[] result = new byte[bytesInOneBatch + 1];
            byte[] locatorBytes;
            for (int i = 0; i < bytesInOneBatch; i++) {
                int position = (int) (Math.random() * data.length);
                try {
                    locatorBytes = URandom.nextBytes(Ints.BYTES);
                    position = (int) (((float) Math.abs(Ints.fromByteArray(locatorBytes)) / (float) Integer.MAX_VALUE) * data.length);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                position = Math.min(Math.max(position, 0), data.length - 1);
                result[i] = data[position];
            }
            byte[] timestampBytes = Longs.toByteArray(System.currentTimeMillis());
            result[bytesInOneBatch] = timestampBytes[timestampBytes.length - 1];
            return result;
        }
    }

    public void addSource(IUEntropySource source) {
        iuEntropySources.add(source);
        if (!hasPaused) {
            source.onResume();
        }
    }

    public void addSources(IUEntropySource... sources) {
        for (IUEntropySource source : sources) {
            addSource(source);
        }
    }

    @Override
    public void onResume() {
        hasPaused = false;
        for (IUEntropySource source : iuEntropySources) {
            source.onResume();
        }
    }

    @Override
    public void onPause() {
        hasPaused = true;
        for (IUEntropySource source : iuEntropySources) {
            source.onPause();
        }
    }

    @Override
    public UEntropySource type() {
        return UEntropySource.Unknown;
    }

    public HashSet<IUEntropySource> sources() {
        return iuEntropySources;
    }
}
