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

    // 🖼 Har doim 576px gacha kamaytirish
    public static Bitmap resizeBitmap(Bitmap source, int targetWidth) {
        int originalWidth = source.getWidth();
        int originalHeight = source.getHeight();

        float aspectRatio = (float) originalHeight / originalWidth;
        int targetHeight = Math.round(targetWidth * aspectRatio);

        return Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true);
    }

    // ⚫⚪ Oddiy qora-oqga o‘tkazish (threshold 128)
    public static Bitmap toMonochrome(Bitmap original) {
        int width = original.getWidth();
        int height = original.getHeight();
        Bitmap bwBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = original.getPixel(x, y);
                int gray = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3;
                int newPixel = gray < 128 ? Color.BLACK : Color.WHITE;
                bwBitmap.setPixel(x, y, newPixel);
            }
        }

        return bwBitmap;
    }

    // 🖼 Dither qilingan rasmni Canvasga zoom bilan chizish (preview uchun)
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

    // 🧾 ESC/POS formatga aylantirish (Compressed Raster Bit Image)
    public static byte[] decodeBitmapCompressed(Bitmap bmp) throws IOException {
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int bytesPerLine = (width + 7) / 8;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        // ⬇️ ESC * (Raster bit image)
        stream.write(0x1D);
        stream.write(0x76);
        stream.write(0x30);
        stream.write(0x01); // Mode 0x01: compressed

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
