package com.foodapp.bluetoothprinterapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import androidx.annotation.RequiresPermission;

import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothPrinterHelper {
    private static final String PRINTER_NAME = "YourPrinterName"; // ← O'ZGARTIRING!
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void printText(String text,String p_name) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Log.e("BT", "Bluetooth yoqilmagan");
            return;
        }

        BluetoothDevice printerDevice = null;
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        for (BluetoothDevice device : pairedDevices) {
            if (device.getName().equals(p_name)) {
                printerDevice = device;
                break;
            }
        }

        if (printerDevice == null) {
            Log.e("BT", "Printer topilmadi");
            return;
        }

        try {
            BluetoothSocket socket = printerDevice.createRfcommSocketToServiceRecord(SPP_UUID);
            socket.connect();

            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(text.getBytes());

            outputStream.close();
            socket.close();
        } catch (Exception e) {
            Log.e("BT", "Xatolik: ", e);
        }
    }


}
