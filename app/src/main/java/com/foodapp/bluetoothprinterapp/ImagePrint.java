package com.foodapp.bluetoothprinterapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class ImagePrint extends AppCompatActivity {
    private static final int REQUEST_IMAGE_PICK = 1;
    private static final int PICK_IMAGE_REQUEST = 1;

    private Uri imageUri;
    private ImageView imageView;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice selectedDevice;
    private BluetoothSocket bluetoothSocket;

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_print);

        imageView = findViewById(R.id.imageView);
        Button btnPickImage = findViewById(R.id.selectImageButton);
        Button btnPrint = findViewById(R.id.printImageButton);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        btnPickImage.setOnClickListener(v -> openImagePicker());
        btnPrint.setOnClickListener(v -> connectAndPrint());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.getData();
            imageView.setImageURI(imageUri); // Tanlangan rasmni ImageViewda ko‘rsatish
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private void connectAndPrint() {
        if (imageUri == null) {
            Toast.makeText(this, "Rasm tanlanmadi", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ulangan printerni topish
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : pairedDevices) {
            if (device.getName().contains("XP") || device.getName().startsWith("Printer")) {
                selectedDevice = device;
                break;
            }
        }

        if (selectedDevice == null) {
            Toast.makeText(this, "Printer topilmadi", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                bluetoothSocket = selectedDevice.createRfcommSocketToServiceRecord(
                        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                bluetoothSocket.connect();

                OutputStream outputStream = bluetoothSocket.getOutputStream();

                Bitmap original = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                Bitmap resized = Utils.resizeBitmap(original, 576); // XP-P810 uchun mos o‘lcham
                Bitmap bwBitmap = Utils.floydSteinbergDither(resized);
                byte[] imageData = Utils.decodeBitmapCompressed(bwBitmap);
                outputStream.write(imageData);

                // 🔽 Matn ham chiqaramiz rasm tagidan
                outputStream.write("Sizga rahmat!\n\n\n\n\n\n".getBytes("UTF-8"));
                outputStream.flush();

                runOnUiThread(() -> Toast.makeText(this, "Chop etildi!", Toast.LENGTH_SHORT).show());

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Xatolik: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }



}