package com.foodapp.bluetoothprinterapp;
import com.foodapp.bluetoothprinterapp.model.ReceiptModel;
import com.google.gson.Gson;

import java.util.Locale;

public class ReceiptFormatter {
    public static String format(String json) {
        Gson gson = new Gson();
        ReceiptModel data = gson.fromJson(json, ReceiptModel.class);

        StringBuilder receipt = new StringBuilder();
        receipt.append("      ").append(data.company).append("\n");
        receipt.append("      ").append(data.number).append("\n");
        receipt.append("--------------------------\n");
        receipt.append("Savdo\n");
        receipt.append("Foydalanuvchi: ").append(data.user).append("\n");
        receipt.append("Kun vaqt: ").append(data.datetime).append("\n");
        receipt.append("--------------------------\n");
        receipt.append("Mahsulot            Miqdor    Narx    Jami\n");
        receipt.append("-----------------------------------------\n");

        int totalSum = 0;
        int maxNameLength = 18; // Mahsulot nomining maksimal uzunligi

        for (ReceiptModel.Items p : data.items) {
            String name = p.name.length() > maxNameLength ? p.name.substring(0, maxNameLength) : p.name;
            float qty = p.qty;
            int total = (int)(qty * p.price);

            // Ustunlarni to‘g‘ri joylashtirish uchun formatni yaxshilash
            receipt.append(String.format(Locale.getDefault(), "%-18s %-8.1f %-8d %-8d\n",
                    name, qty, p.price, total));

            totalSum += total;
        }

        receipt.append("--------------------------\n");
        receipt.append("Umumiy:             ").append(totalSum).append(" so'm\n");
        receipt.append("\n\n");

        return receipt.toString();
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

        html.append("<div><strong>Mahsulotlar:</strong></div>");
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
