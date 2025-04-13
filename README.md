
# 🖨️ Android Bluetooth Printer App – XP-P810 uchun

Bu Android ilovasi orqali foydalanuvchi **telefon galereyasidan rasm tanlab**, uni **Bluetooth orqali XP-P810 printerda oq-qora formatda chop etishi** mumkin. Shuningdek, rasm ostiga kerakli **matn (kvitansiya ma’lumotlari)** ham chiqariladi.

---

## 📌 Asosiy funksiyalar

✅ Galereyadan rasm tanlash  
✅ Rasmni oq-qora formatga aylantirish (monoxrome)  
✅ Printer o‘lchamiga moslab rasmni `resize` qilish  
✅ Matn va rasmni birgalikda chop etish  
✅ Bluetooth orqali avtomatik printerga ulanish (XP-P810)  
✅ ESC/POS formatga o‘tkazib chop etish  
✅ Printerda tiniq, silliq va optimal tezlikda chop etish  

---

## 🧱 Loyiha tuzilishi

```
app/
├── java/
│   └── com.example.bluetoothprinter/
│       ├── MainActivity.java         # UI va rasm tanlash
│       ├── ImagePrintActivity.java   # Printerga ulanish va chop qilish
│       └── Utils.java                # Rasmni monoxrome va ESC/POS kodga aylantirish
├── res/
│   ├── drawable/                     # Misol rasm (sample_image)
│   └── layout/
│       └── activity_main.xml         # UI fayllar
└── AndroidManifest.xml               # Ruxsatlar va konfiguratsiyalar
```

---

## 🎨 Natija namunasi (Chop etilgan chek)

<p align="center">
  <img src="screenshots/sample1.png" width="250"/>
  <img src="screenshots/sample1.png](https://github.com/user-attachments/assets/6ff143fa-932f-4efb-9f0e-4a0bfdf0bf35)" width="250"/>
  <img src="screenshots/sample2.png](https://github.com/user-attachments/assets/cd1b2db1-21fe-4503-9d3a-cae191ce3c7b)" width="250"/>
  <img src="screenshots/sample3.png](https://github.com/user-attachments/assets/db4e8198-3b89-4596-8da4-47a7a6ee4db2)" width="250"/>
</p>

> Ushbu kvitansiya XP-P810 printerda chop etilgan real misol hisoblanadi.

---

## 📲 Ishlatish bo‘yicha qo‘llanma

1. **GitHub'dan yuklab oling:**
   ```bash
   git clone https://github.com/username/bluetooth-printer-xpp810.git
   ```

2. **Android Studio** orqali loyihani oching.

3. Qurilmada quyidagi **ruxsatlar** so‘raladi:
   - `BLUETOOTH_CONNECT`
   - `BLUETOOTH_ADMIN`
   - `READ_EXTERNAL_STORAGE`

4. Telefonni printer bilan Bluetooth orqali ulab qo‘ying.

5. Ilovani ishga tushiring, rasm tanlang va `CHOP ETISH` tugmasini bosing.

---

## ⚙️ Texnik ma’lumotlar

| Funksiya        | Tavsif                                                |
|-----------------|--------------------------------------------------------|
| Printer turi    | XP-P810 (ESC/POS termal printer)                      |
| Chop formati    | Oq-qora (black/white) - bitmap to ESC/POS              |
| Chop kengligi   | 384px (yoki printer modeliga qarab 576px)             |
| Android versiyasi | Android 8.0+ (`API 26`) va yuqori versiyalar         |

---

## 🛠 Texnologiyalar

- 📱 Java / Android SDK
- 🎨 Bitmap / Canvas / ColorMatrix
- 📶 Bluetooth Classic (SPP UUID)
- 🖨 ESC/POS byte[] generator (custom)
- 📦 Android Permissions API

---

## ✍️ Muallif

- 👨‍💻 Ism: **Temur Yuldoshev**
- 🔗 GitHub: [@Tamurbek](https://github.com/Tamurbek)
- 📧 Email: `temuryoldoshev10@gmail.com`

---

## 📝 Litsenziya

Bu loyiha **MIT License** asosida tarqatiladi. Bepul foydalanish, o‘zgartirish va tarqatishga ruxsat beriladi.

---

## 🔖 Qo‘shimcha

Agar siz printer chop qilish tezligini oshirmoqchi bo‘lsangiz:
- Bitmapni **kamroq noziklikda** tayyorlang
- Rang o‘zgartirishni **oddiyroq algoritmga** o‘tkazing
- Qora fondagi oddiy belgilar (text + logotip) ishlating

---
