package com.foodapp.bluetoothprinterapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_BLUETOOTH = 100;
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothDevice printerDevice;

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        TextView printerList = findViewById(R.id.printerList);
        EditText editText = findViewById(R.id.editText);
        Button printButton = findViewById(R.id.printButton);
        Button nextBtn = findViewById(R.id.next);

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ImagePrint.class);
                startActivity(intent);
                finish();  // LocalPas
            }
        });



        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth mavjud emas", Toast.LENGTH_LONG).show();
            return;
        }

        // Runtime permission (Android 12+)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_CODE_BLUETOOTH);
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }

        // Ulangan qurilmalar
        StringBuilder deviceList = new StringBuilder("Ulangan qurilmalar:\n");
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (!pairedDevices.isEmpty()) {
            for (BluetoothDevice device : pairedDevices) {
                deviceList.append("- ").append(device.getName()).append("\n");

                // Bu yerda istalgan printerni tanlaysiz (hozir birinchisini tanlaymiz)
                if (printerDevice == null) {
                    printerDevice = device;
                }
            }
        } else {
            deviceList.append("Hech qanday qurilma topilmadi");
        }
        printerList.setText(deviceList.toString());

        printButton.setOnClickListener(v -> {
            String textToPrint = editText.getText().toString();
            printViaBluetooth(textToPrint);
        });
    }

    private String getBase64(int pageWidth, String filePath) {
        //You can modify below program as per your requirements provided that you return base64 string corresponding to the image
        //Also check if filepath is not null and your app has required permissions before calling this method
        Bitmap bm = BitmapFactory.decodeFile(filePath);
        int width = bm.getWidth(), height = bm.getHeight();
        int compressValue = 50;
        if(width > 500 || height > 500) {
            compressValue = 20;
            int reqWidth = (int) Math.round(pageWidth * 8);
            if (width < reqWidth && width > 16) {
                int diff = width % 8;
                if (diff != 0) {
                    int newWidth = width - diff;
                    int newHeight = (int) (width - diff) * height / width;
                    float scaleWidth = ((float) newWidth) / width;
                    float scaleHeight = ((float) newHeight) / height;
                    // CREATE A MATRIX FOR THE MANIPULATION
                    Matrix matrix = new Matrix();
                    // RESIZE THE BIT MAP
                    matrix.postScale(scaleWidth, scaleHeight);

                    // "RECREATE" THE NEW BITMAP
                    Bitmap resizedBitmap = Bitmap.createBitmap(
                            bm, 0, 0, width, height, matrix, false);
                    if (bm != null && !bm.isRecycled())
                        bm.recycle();
                    bm = resizedBitmap;
                }
            } else if (width > 16) {
                int newHeight = (int) reqWidth * height / width;
                float scaleWidth = ((float) reqWidth) / width;
                float scaleHeight = ((float) newHeight) / height;
                // CREATE A MATRIX FOR THE MANIPULATION
                Matrix matrix = new Matrix();
                // RESIZE THE BIT MAP
                matrix.postScale(scaleWidth, scaleHeight);

                // "RECREATE" THE NEW BITMAP
                Bitmap resizedBitmap = Bitmap.createBitmap(
                        bm, 0, 0, width, height, matrix, false);
                if (bm != null && !bm.isRecycled())
                    bm.recycle();
                bm = resizedBitmap;
            }
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, compressValue, byteArrayOutputStream); // bm is the bitmap object
        return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private void printViaBluetooth(String text) {
        try {
            BluetoothSocket socket = printerDevice.createRfcommSocketToServiceRecord(SPP_UUID);
            socket.connect();

            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(text.getBytes());

            outputStream.flush();
            outputStream.close();
            socket.close();

            Toast.makeText(this, "Chop etildi!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Xatolik: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}