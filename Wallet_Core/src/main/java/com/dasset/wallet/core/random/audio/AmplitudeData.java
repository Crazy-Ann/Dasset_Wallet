package com.dasset.wallet.core.random.audio;

import com.google.common.primitives.Shorts;

public class AmplitudeData {

    private static final int SAMPLE_COUNT = 256;
    private int amplitude;

    public AmplitudeData(byte[] rawData) {
        if (rawData == null) {
            amplitude = 0;
            return;
        }
        int step = rawData.length / Shorts.BYTES / SAMPLE_COUNT;
        int count = 0;
        double sum = 0;
        for (int i = 0; i < rawData.length - Shorts.BYTES; i += step) {
            byte[] bytes = new byte[Shorts.BYTES];
            for (int j = 0; j < Shorts.BYTES; j++) {
                bytes[j] = rawData[i + j];
            }
            short s = Shorts.fromByteArray(bytes);
            sum += s * s;
            count++;
        }
        amplitude = (int) Math.sqrt(sum / count);
    }

    public int getAmplitude() {
        return amplitude;
    }
}
