package com.foodapp.bluetoothprinterapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Picture;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class ReceiptPrint extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<BluetoothDevice> pairedDevicesList;
    private Spinner deviceSpinner;
    private static final UUID PRINTER_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_receipt_print);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        showReceiptInWebView();

        deviceSpinner = findViewById(R.id.deviceSpinner);
        Button btnPrint = findViewById(R.id.btnPrint);

        Button backBtn = findViewById(R.id.back);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(ReceiptPrint.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        loadPairedDevices();

        btnPrint.setOnClickListener(v -> {
            int selectedIndex = deviceSpinner.getSelectedItemPosition();
            if (selectedIndex >= 0 && selectedIndex < pairedDevicesList.size()) {
                BluetoothDevice selectedDevice = pairedDevicesList.get(selectedIndex);
//                printImageToDevice(selectedDevice);
                printToDevice(selectedDevice);
            }
        });

    }
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private void loadPairedDevices() {
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        pairedDevicesList = new ArrayList<>(bondedDevices);
        ArrayList<String> deviceNames = new ArrayList<>();

        for (BluetoothDevice device : pairedDevicesList) {
            deviceNames.add(device.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, deviceNames);
        deviceSpinner.setAdapter(adapter);
    }

    public void showReceiptInWebView() {
        String jsonReceipt = getReceiptJson();
        if (jsonReceipt == null) {
            Toast.makeText(this, "❌ JSON topilmadi", Toast.LENGTH_LONG).show();
            return;
        }
        String receipt = ReceiptFormatter.formatToHtml(jsonReceipt);
        WebView webView = findViewById(R.id.webview);
        webView.loadDataWithBaseURL(null, receipt, "text/html", "UTF-8", null);
    }

    private String getReceiptJson() {
        String json = null;
        try {
            InputStream is = getAssets().open("receipt.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return json;
    }



    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private void printToDevice(BluetoothDevice device) {
        try {
            BluetoothSocket socket = device.createRfcommSocketToServiceRecord(PRINTER_UUID);
            socket.connect();
            OutputStream outputStream = socket.getOutputStream();

            String jsonReceipt = getReceiptJson();
            if (jsonReceipt == null) {
                Toast.makeText(this, "❌ JSON topilmadi", Toast.LENGTH_LONG).show();
                return;
            }

            // Qog'oz o'lchamini aniqlash
            int maxLength = 48;  // 80mm uchun 48, 58mm uchun 40

            String receipt = ReceiptFormatter.format(jsonReceipt, maxLength);

            // Chekni bo‘lib-bo‘lib yuborish
            int chunkSize = 512; // Har bir yuboriladigan bo‘lakning uzunligi
            for (int i = 0; i < receipt.length(); i += chunkSize) {
                int end = Math.min(i + chunkSize, receipt.length());
                String chunk = receipt.substring(i, end);

                // Har bir bo‘lakni yuborish
                outputStream.write(chunk.getBytes("UTF-8"));
                // Printerning javobini kutish (masalan, 100-200ms)
                Thread.sleep(1500);
            }

            // Kesish komandasini yuborish
            outputStream.write(new byte[]{0x1D, 0x56, 0x41, 0x10}); // Kesish komandasi
            outputStream.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}