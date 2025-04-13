package com.foodapp.bluetoothprinterapp;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Utils {

    public static Bitmap resizeBitmap(Bitmap source, int targetWidth) {
        int originalWidth = source.getWidth();
        int originalHeight = source.getHeight();

        if (originalWidth <= targetWidth) {
            // Eni allaqachon kichik — o‘zgartirmay qaytaramiz
            return source;
        }

        float aspectRatio = (float) originalHeight / originalWidth;
        int targetHeight = Math.round(targetWidth * aspectRatio);

        return Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true);
    }


    // ✅ Floyd-Steinberg dithering bilan monoxrom bitmapga o‘tkazish
    public static Bitmap floydSteinbergDither(Bitmap source) {
        int width = source.getWidth();
        int height = source.getHeight();
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        float[][] gray = new float[height][width];

        // Grayscale convert
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = source.getPixel(x, y);
                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);
                gray[y][x] = (r + g + b) / 3f;
            }
        }

        // Floyd–Steinberg Dithering
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float oldPixel = gray[y][x];
                float newPixel = oldPixel < 128 ? 0 : 255;
                float error = oldPixel - newPixel;
                gray[y][x] = newPixel;

                if (x + 1 < width) gray[y][x + 1] += error * 7 / 16f;
                if (x - 1 >= 0 && y + 1 < height) gray[y + 1][x - 1] += error * 3 / 16f;
                if (y + 1 < height) gray[y + 1][x] += error * 5 / 16f;
                if (x + 1 < width && y + 1 < height) gray[y + 1][x + 1] += error * 1 / 16f;
            }
        }

        // Result bitmapga yozamiz
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int val = (int) gray[y][x];
                int color = val < 128 ? Color.BLACK : Color.WHITE;
                result.setPixel(x, y, color);
            }
        }

        return result;
    }

    // 🖼 Chizilgan pixel rasmni preview qilish (zoom bilan)
    public static Bitmap drawDitheredToCanvas(Bitmap dithered, int scale) {
        int width = dithered.getWidth();
        int height = dithered.getHeight();
        Bitmap canvasBitmap = Bitmap.createBitmap(width * scale, height * scale, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(canvasBitmap);
        Paint paint = new Paint();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = dithered.getPixel(x, y);
                paint.setColor(color);
                canvas.drawRect(x * scale, y * scale, (x + 1) * scale, (y + 1) * scale, paint);
            }
        }

        return canvasBitmap;
    }

    // 🔄 Dither qilingan bitmapni ESC/POS formatga aylantirish (compressed)
    public static byte[] decodeBitmapCompressed(Bitmap bmp) throws IOException {
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int bytesPerLine = (width + 7) / 8;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        // ESC * compressed raster mode
        stream.write(0x1D);
        stream.write(0x76);
        stream.write(0x30);
        stream.write(0x01); // compressed mode

        stream.write(new byte[]{
                (byte) (bytesPerLine % 256),
                (byte) (bytesPerLine / 256),
                (byte) (height % 256),
                (byte) (height / 256)
        });

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x += 8) {
                byte b = 0;
                for (int bit = 0; bit < 8; bit++) {
                    int pixelX = x + bit;
                    if (pixelX < width) {
                        int pixel = bmp.getPixel(pixelX, y);
                        int gray = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3;
                        if (gray < 128) {
                            b |= (1 << (7 - bit));
                        }
                    }
                }
                stream.write(b);
            }
        }

        return stream.toByteArray();
    }
}
