package com.zypher.expensemanager.models;

/**
 * Class Account merupakan "Model" dalam arsitektur MVC.
 * Class ini merepresentasikan data akun (seperti Dompet, Bank, atau Cash)
 * yang akan disimpan ke dalam Firebase Realtime Database.
 */
public class Account {
    // Atribut/Variabel untuk menyimpan jumlah saldo di akun
    private double accountAmount;

    // Atribut/Variabel untuk menyimpan nama akun (misal: "Tabungan")
    private String accountName;

    /**
     * Getter: Digunakan untuk mengambil nilai accountAmount.
     * Firebase memerlukan getter untuk membaca data dari objek saat proses upload.
     */
    public double getAccountAmount() {
        return accountAmount;
    }

    /**
     * Setter: Digunakan untuk mengubah nilai accountAmount.
     * Firebase memerlukan setter untuk mengisi data ke dalam objek saat proses download/fetching.
     */
    public void setAccountAmount(double accountAmount) {
        this.accountAmount = accountAmount;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    /**
     * CONSTRUCTOR KOSONG (Wajib Ada)
     * Firebase Realtime Database memerlukan constructor kosong tanpa argumen
     * untuk merekonstruksi objek saat mengambil data (deserialization).
     * Jika ini dihapus, aplikasi akan error saat memanggil dataSnapshot.getValue(Account.class).
     */
    public Account() {
    }

    /**
     * CONSTRUCTOR DENGAN PARAMETER
     * Memudahkan programmer untuk membuat objek Account baru secara instan
     * Contoh: Account myAccount = new Account(500000, "Dompet");
     */
    public Account(double accountAmount, String accountName) {
        this.accountAmount = accountAmount;
        this.accountName = accountName;
    }
}