package com.foodapp.bluetoothprinterapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class PrintChek extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<BluetoothDevice> pairedDevicesList;
    private Spinner deviceSpinner;
    private static final UUID PRINTER_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_print_chek);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        deviceSpinner = findViewById(R.id.deviceSpinner);
        Button btnPrint = findViewById(R.id.btnPrint);
        Button backBtn = findViewById(R.id.back);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(PrintChek.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        loadPairedDevices();



        btnPrint.setOnClickListener(new View.OnClickListener() {
            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            @Override
            public void onClick(View v) {
                int selectedIndex = deviceSpinner.getSelectedItemPosition();
                if (selectedIndex >= 0 && selectedIndex < pairedDevicesList.size()) {
                    BluetoothDevice selectedDevice = pairedDevicesList.get(selectedIndex);
                    printToDevice(selectedDevice);
                }
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

    private String readJsonFromAssets(Context context) {
        try {
            InputStream is = context.getAssets().open("receipt.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }


    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private void printToDevice(BluetoothDevice device) {
        try {
            BluetoothSocket socket = device.createRfcommSocketToServiceRecord(PRINTER_UUID);
            socket.connect();
            OutputStream outputStream = socket.getOutputStream();

            String receipt = ""
                    + "      SAMANDAR\n"
                    + "     MILK FOODS\n"
                    + "--------------------------\n"
                    + "Savdo\n"
                    + "Raqam: 000000017\n"
                    + "Foydalanuvchi: Admin\n"
                    + "Kun vaqt: 2025-04-13 19:45:42\n"
                    + "--------------------------\n"
                    + "Mahsulot     Miqdor  Narx   Jami\n"
                    + "Suzma kg     4kg     15000  60000\n"
                    + "Tvorog 200g  5ta     9000   45000\n"
                    + "Suzma ARZONI 7.7kg   12000  92400\n"
                    + "Sut 1 litr   4ta     7000   28000\n"
                    + "Sut 1 litr   4ta     7000   28000\n"
                    + "Sut 1 litr   4ta     7000   28000\n"
                    + "Sut 1 litr   4ta     7000   28000\n"
                    + "Sut 1 litr   4ta     7000   28000\n"
                    + "Sut 1 litr   4ta     7000   28000\n"
                    + "Sut 1 litr   4ta     7000   28000\n"
                    + "Sut 1 litr   4ta     7000   28000\n"
                    + "Sut 1 litr   4ta     7000   28000\n"
                    + "--------------------------\n"
                    + "Umumiy:             225400 so'm\n\n\n";

            writeWithDelay(outputStream, receipt);

            Thread.sleep(1000); // Matn tugashini kutish
            outputStream.write(new byte[]{0x1D, 0x56, 0x41, 0x10}); // kesish komandasi
            outputStream.flush();
            outputStream.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeWithDelay(OutputStream outputStream, String text) throws InterruptedException, IOException {
        String[] lines = text.split("\n");
        for (String line : lines) {
            outputStream.write((line + "\n").getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            Thread.sleep(100); // 100 ms delay – printer ulgura oladi
        }
    }

    private String centerText(String text) {
        int lineWidth = 100; // Qator uzunligi printerga qarab o'zgartiring
        int padding = (lineWidth - text.length()) / 2;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < padding; i++) builder.append(" ");
        builder.append(text);
        return builder.toString();
    }

}