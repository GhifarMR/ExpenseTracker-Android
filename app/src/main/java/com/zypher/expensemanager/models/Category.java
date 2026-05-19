package com.zypher.expensemanager.models;

/**
 * Class Category berfungsi sebagai Model data untuk mendefinisikan kategori transaksi.
 * Class ini menyimpan informasi nama, icon, dan warna identitas kategori.
 */
public class Category {
    // Variabel untuk menyimpan nama kategori (misal: "Makanan", "Transportasi")
    private String categoryName;

    // Variabel untuk menyimpan ID resource gambar/icon (menggunakan tipe int untuk merujuk pada R.drawable)
    private int categoryImage;

    // Variabel untuk menyimpan ID resource warna (menggunakan tipe int untuk merujuk pada R.color)
    private int categoryColor;

    /**
     * Constructor kosong (no-argument constructor).
     * Wajib ada agar Firebase dapat melakukan deserialisasi data menjadi objek Category.
     */
    public Category() {
    }

    /**
     * Constructor dengan parameter.
     * Digunakan untuk membuat objek Category baru dengan nilai yang langsung ditentukan.
     */
    public Category(String categoryName, int categoryImage, int categoryColor) {
        this.categoryName = categoryName;
        this.categoryImage = categoryImage;
        this.categoryColor = categoryColor;
    }

    /**
     * Mengambil nama kategori.
     */
    public String getCategoryName() {
        return categoryName;
    }

    /**
     * Mengatur atau mengubah nama kategori.
     */
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    /**
     * Mengambil ID resource gambar/icon.
     */
    public int getCategoryImage() {
        return categoryImage;
    }

    /**
     * Mengatur ID resource gambar/icon.
     */
    public void setCategoryImage(int categoryImage) {
        this.categoryImage = categoryImage;
    }

    /**
     * Mengambil ID resource warna.
     */
    public int getCategoryColor() {
        return categoryColor;
    }

    /**
     * Mengatur ID resource warna.
     */
    public void setCategoryColor(int categoryColor) {
        this.categoryColor = categoryColor;
    }
}