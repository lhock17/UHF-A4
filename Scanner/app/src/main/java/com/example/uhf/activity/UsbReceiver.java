package com.example.uhf.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

public class UsbReceiver extends BroadcastReceiver {
    private UsbDeviceCallback usbDeviceCallback;

    public UsbReceiver() {
        // Default constructor with no arguments
    }
    public UsbReceiver(UsbDeviceCallback callback) {
        this.usbDeviceCallback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
            UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (device != null) {
                String devicePath = device.getDeviceName();
                if (usbDeviceCallback != null) {
                    usbDeviceCallback.onUsbDeviceConnected(devicePath);
                }
            }
        } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
            UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (device != null) {
                String devicePath = device.getDeviceName();
                if (usbDeviceCallback != null) {
                    usbDeviceCallback.onUsbDeviceDisconnected(devicePath);
                }
            }
        }
    }
}
