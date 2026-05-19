package com.zypher.expensemanager.models;

import java.io.Serializable;

/**
 * Class Transaction berfungsi sebagai Model data untuk mencatat setiap transaksi keuangan.
 * Mengimplementasikan Serializable agar objek ini dapat dikirim antar Activity/Fragment.
 */
public class Transaction implements Serializable {
    // ID unik untuk setiap transaksi (biasanya dihasilkan oleh Firebase push key)
    private String id;

    // Tipe transaksi, misalnya "Income" (Pemasukan) atau "Expense" (Pengeluaran)
    private String type;

    // Tanggal terjadinya transaksi dalam format String
    private String date;

    // Jumlah nilai uang dalam transaksi
    private double amount;

    // Nama kategori terkait transaksi (misal: "Gaji", "Belanja")
    private String category;

    // Nama akun yang digunakan (misal: "Dompet", "Rekening Bank")
    private String account;

    // Catatan tambahan mengenai transaksi
    private String note;

    /**
     * Constructor kosong (no-argument constructor).
     * Wajib ada untuk keperluan Firebase Realtime Database saat membaca data.
     */
    public Transaction() {}

    /**
     * Constructor dengan parameter (tanpa ID).
     * Digunakan saat membuat objek transaksi baru sebelum disimpan ke database.
     */
    public Transaction(String type, String date, double amount, String category, String account, String note) {
        this.type = type;
        this.date = date;
        this.amount = amount;
        this.category = category;
        this.account = account;
        this.note = note;
    }

    /**
     * Getter dan Setter.
     * Digunakan oleh Firebase untuk proses serialisasi (simpan) dan deserialisasi (ambil) data.
     */

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

    public double getAmount() {
        return amount;
    }
    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }

    public String getAccount() {
        return account;
    }
    public void setAccount(String account) {
        this.account = account;
    }

    public String getNote() {
        return note;
    }
    public void setNote(String note) {
        this.note = note;
    }
}