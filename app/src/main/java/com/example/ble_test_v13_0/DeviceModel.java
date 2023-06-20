package com.example.ble_test_v13_0;

import android.bluetooth.BluetoothDevice;

public class DeviceModel {

    private final BluetoothDevice address; // MAC-address
    private int signal; // RSSI
    private final String name;

    public DeviceModel(BluetoothDevice address, String name, int signal) {
        this.address = address;
        this.signal = signal;
        this.name = name;
    }

    public BluetoothDevice getBTDeviceAddress() {
        return this.address;
    }
    public String getBTDeviceName() {
        return this.name;
    }
    public int getSignalLevel() {
        return this.signal;
    }

}
