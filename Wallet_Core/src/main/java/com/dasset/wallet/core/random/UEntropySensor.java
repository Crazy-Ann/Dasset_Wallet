package com.dasset.wallet.core.random;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.core.random.view.SensorVisualizerView;
import com.google.common.collect.Lists;
import com.google.common.primitives.Floats;
import com.google.common.primitives.Ints;

import java.util.ArrayList;
import java.util.List;

public class UEntropySensor implements SensorEventListener, IUEntropySource {

    private UEntropyCollector uEntropyCollector;
    private SensorManager sensorManager;
    private List<Sensor> sensors;
    private SensorVisualizerView sensorVisualizerView;
    private boolean hasPaused = true;

    public UEntropySensor(Context context, UEntropyCollector uEntropyCollector, SensorVisualizerView sensorVisualizerView) {
        this.uEntropyCollector = uEntropyCollector;
        this.sensorVisualizerView = sensorVisualizerView;
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.sensors = Lists.newArrayList();
    }

    private void registerAllSensors() {
        if (sensors.size() == 0) {
            sensors.clear();
            sensors.addAll(sensorManager.getSensorList(Sensor.TYPE_ALL));
        }
        ArrayList<Sensor> unregisteredSensors = Lists.newArrayList();
        for (Sensor sensor : sensors) {
            boolean registered = false;
            try {
                registered = sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!registered) {
                unregisteredSensors.add(sensor);
            }
            LogUtil.getInstance().print(String.format((registered ? "Success to" : "Failure to ") + "register sensor &s", sensor.getName()));
        }
        sensors.removeAll(unregisteredSensors);
        if (sensors.size() == 0) {
            if (sensorVisualizerView != null) {
                sensorVisualizerView.postDelayed(new Runnable() {

                    @Override
                    public void run() {

                        uEntropyCollector.onError(new Exception("no sensor registered"),
                                                  UEntropySensor.this);
                    }
                }, 100);
            } else {
                uEntropyCollector.onError(new Exception("no sensor registered"), UEntropySensor.this);
            }
        }
        sensorVisualizerView.setSensors(sensors);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (hasPaused) {
            return;
        }
        if (event != null && event.values != null) {
            byte[] data = new byte[event.values.length * Ints.BYTES];
            byte[] everyData;
            for (int i = 0;
                 i < event.values.length;
                 i++) {
                int hash = Floats.hashCode(event.values[i]);
                everyData = Ints.toByteArray(hash);
                for (int j = 0;
                     j < Ints.BYTES;
                     j++) {
                    if (everyData.length > j) {
                        data[i * Ints.BYTES + j] = everyData[j];
                    } else {
                        data[i * Ints.BYTES + j] = 0;
                    }
                }
            }
            uEntropyCollector.onNewData(data, UEntropyCollector.UEntropySource.Sensor);
            sensorVisualizerView.onSensorData(event.sensor);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onResume() {
        if (hasPaused) {
            hasPaused = false;
            registerAllSensors();
        }
    }

    @Override
    public void onPause() {
        if (!hasPaused) {
            hasPaused = true;
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public UEntropyCollector.UEntropySource type() {
        return UEntropyCollector.UEntropySource.Sensor;
    }
}
