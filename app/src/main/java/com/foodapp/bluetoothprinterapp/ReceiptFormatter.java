package com.foodapp.bluetoothprinterapp;

import com.foodapp.bluetoothprinterapp.model.ReceiptModel;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

public class ReceiptFormatter {


    public static String format(String json, int maxLength) {
        Gson gson = new Gson();
        ReceiptModel data = gson.fromJson(json, ReceiptModel.class);

        StringBuilder receipt = new StringBuilder(maxLength * 2);  // Kattaroq boshlang'ich hajm berish

        // Chek o'lchami (80mm va 58mm uchun maxLength 48 va 40 bo'lishi mumkin)
        int maxProductNameLength = maxLength - 4;  // Mahsulot nomiga ajratilgan maydon

        // Matnni birlashtirishda bir nechta chaqiruvlarni kamaytirish
        receipt.append(horizontalLine(maxLength))
                .append("\n")
                .append(centerText(data.company, maxLength))
                .append("\n")
                .append(centerText("No: " + data.number, maxLength))
                .append("\n")
                .append(horizontalLine(maxLength))
                .append("\n")
                .append("Savdo\n")
                .append("Foydalanuvchi: ")
                .append(data.user)
                .append("\n")
                .append("Kun vaqt: ")
                .append(data.datetime)
                .append("\n")
                .append(horizontalLine(maxLength))
                .append("\n")
                .append("Mahsulotlar:")
                .append("\n")
                .append(horizontalLine(maxLength))
                .append("\n");

        int totalSum = 0;

        // Mahsulotlar uchun izlash jarayonini optimallashtirish
        for (ReceiptModel.Items p : data.items) {
            float qty = p.qty;
            int total = (int) (qty * p.price);
            totalSum += total;

            // Mahsulot nomini qisqartirishni optimallashtirish
            receipt.append(leftAlignText(p.name, maxProductNameLength))
                    .append("\n");

            // Miqdor x narx va jami
            String qtyStr = buildQtyStr(p, qty);
            receipt.append(leftAlignText(qtyStr, maxLength - 10))
                    .append(String.format("%7d\n", total)); // Osonroq qo'shish
        }

        receipt.append(horizontalLine(maxLength))
                .append("\n")
                .append(leftAlignText("Umumiy:", maxLength - 10))
                .append(String.format("%7d\n", totalSum))
                .append("\n\n");

        return receipt.toString();
    }

    private void setFontSize(OutputStream outputStream, boolean isLarge) throws IOException {
        if (isLarge) {
            // ESC/POS komandasini yuborish orqali katta o'lchamli shriftda chop qilish
            outputStream.write(new byte[]{0x1B, 0x21, 0x10}); // Katta shrift
        } else {
            // ESC/POS komandasini yuborish orqali oddiy o'lchamli shrift
            outputStream.write(new byte[]{0x1B, 0x21, 0x00}); // Oddiy shrift
        }
    }

    private void setBold(OutputStream outputStream, boolean isBold) throws IOException {
        if (isBold) {
            // ESC/POS komandasini yuborish orqali bold shrift
            outputStream.write(new byte[]{0x1B, 0x45, 0x01});
        } else {
            // ESC/POS komandasini yuborish orqali bold shriftni o'chirish
            outputStream.write(new byte[]{0x1B, 0x45, 0x00});
        }
    }

    private void setItalic(OutputStream outputStream, boolean isItalic) throws IOException {
        if (isItalic) {
            // ESC/POS komandasini yuborish orqali italik shrift
            outputStream.write(new byte[]{0x1B, 0x34});
        } else {
            // ESC/POS komandasini yuborish orqali italik shriftni o'chirish
            outputStream.write(new byte[]{0x1B, 0x35});
        }
    }

    private static String buildQtyStr(ReceiptModel.Items p, float qty) {
        String qtyStr;
        if (p.unit != null && !p.unit.trim().isEmpty()) {
            qtyStr = String.format(Locale.getDefault(), "%s %s x %d",
                    (qty == (int) qty ? String.valueOf((int) qty) : String.format("%.2f", qty)),
                    p.unit, p.price);
        } else {
            qtyStr = String.format(Locale.getDefault(), "%s x %d",
                    (qty == (int) qty ? String.valueOf((int) qty) : String.format("%.2f", qty)),
                    p.price);
        }
        return qtyStr;
    }

    private static String centerText(String text, int maxLength) {
        int space = (maxLength - text.length()) / 2;
        return " ".repeat(space) + text + " ".repeat(space);
    }

    private static String leftAlignText(String text, int maxLength) {
        return String.format("%-" + maxLength + "s", text);
    }

    private static String horizontalLine(int length) {
        return "-".repeat(length);
    }




    public static String formatToHtml(String json) {
        Gson gson = new Gson();
        ReceiptModel data = gson.fromJson(json, ReceiptModel.class);

        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset='UTF-8'>");
        html.append("<style>")
                .append("body { font-family: monospace; font-size: 14px; }")
                .append(".center { text-align: center; }")
                .append(".line { border-top: 1px dashed black; margin: 5px 0; }")
                .append(".item { display: flex; flex-direction: column; margin-bottom: 4px; }")
                .append(".item-top { display: flex; justify-content: space-between; }")
                .append("</style></head><body>");

        html.append("<div class='center'><strong>")
                .append(data.company).append("</strong><br>")
                .append(data.number).append("</div>");
        html.append("<div class='line'></div>");
        html.append("<div><strong>Savdo</strong><br>")
                .append("Foydalanuvchi: ").append(data.user).append("<br>")
                .append("Kun vaqt: ").append(data.datetime).append("</div>");
        html.append("<div class='line'></div>");

        html.append("<div>Mahsulotlar:</div>");
        int totalSum = 0;

        for (ReceiptModel.Items p : data.items) {
            float qty = p.qty;
            int total = (int)(qty * p.price);
            totalSum += total;

            html.append("<div class='item'>")
                    .append("<div>").append(p.name).append("</div>")
                    .append("<div class='item-top'>")
                    .append("<span>").append(String.format(Locale.getDefault(), "%.1f x %d", qty, p.price)).append("</span>")
                    .append("<span>").append(String.format("%,d", total)).append(" so'm</span>")
                    .append("</div>")
                    .append("</div>");
        }

        html.append("<div class='line'></div>");
        html.append("<div class='item-top'><strong>Umumiy:</strong>")
                .append("<strong>").append(String.format("%,d", totalSum)).append(" so'm</strong></div>");
        html.append("</body></html>");

        return html.toString();
    }
}
