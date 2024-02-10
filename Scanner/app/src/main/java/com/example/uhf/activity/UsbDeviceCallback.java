package com.example.uhf.activity;

public interface UsbDeviceCallback {
    void onUsbDeviceConnected(String devicePath);
    void onUsbDeviceDisconnected(String devicePath);
}

