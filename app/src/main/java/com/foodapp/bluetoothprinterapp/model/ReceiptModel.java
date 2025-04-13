package com.foodapp.bluetoothprinterapp.model;

import java.util.List;

public class ReceiptModel {
    public String company;
    public float number;
    public String user;
    public String datetime;
    public List<Items> items;

    public static class Items {
        public String name;
        public float qty; // qty float bo‘lishi mumkin
        public int price;
    }
}
