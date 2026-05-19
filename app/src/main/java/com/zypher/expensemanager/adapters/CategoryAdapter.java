package com.zypher.expensemanager.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zypher.expensemanager.R;
import com.zypher.expensemanager.databinding.SimpleCategoryItemBinding;
import com.zypher.expensemanager.models.Category;

import java.util.ArrayList;

/**
 * CategoryAdapter berfungsi untuk menampilkan daftar kategori dalam bentuk list/grid.
 * Adapter ini juga menangani event klik pada setiap kategori menggunakan Interface.
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    Context context;
    ArrayList<Category> categories;

    /**
     * Interface CategoryClickListener.
     * Digunakan agar Activity/Fragment yang menggunakan adapter ini bisa menerima
     * notifikasi ketika salah satu item kategori diklik.
     */
    public interface CategoryClickListener {
        void onCategoryClicked(Category category);
    }

    CategoryClickListener categoryClickListener;

    /**
     * Constructor untuk inisialisasi Context, data kategori, dan listener klik.
     */
    public CategoryAdapter(Context context, ArrayList<Category> categories, CategoryClickListener categoryClickListener) {
        this.context = context;
        this.categories = categories;
        this.categoryClickListener = categoryClickListener;
    }

    /**
     * ViewHolder untuk menyimpan referensi View pada setiap item kategori.
     */
    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        SimpleCategoryItemBinding binding;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            // Binding komponen UI dari layout simple_category_item
            binding = SimpleCategoryItemBinding.bind(itemView);
        }
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Mengubah layout XML simple_category_item menjadi objek View
        View view = LayoutInflater.from(context).inflate(R.layout.simple_category_item, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        // Mengambil data kategori berdasarkan posisi
        Category category = categories.get(position);

        // Mengatur teks nama kategori
        holder.binding.categoryText.setText(category.getCategoryName());

        // Mengatur gambar/icon kategori
        holder.binding.categoryIcon.setImageResource(category.getCategoryImage());

        // Mengatur warna latar belakang icon secara dinamis dari data model
        holder.binding.categoryIcon.setBackgroundTintList(context.getColorStateList(category.getCategoryColor()));

        // Mengatur warna icon (foreground) menjadi putih agar kontras
        holder.binding.categoryIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);

        /**
         * Menangani event klik pada item.
         * Saat item diklik, method onCategoryClicked pada interface akan dijalankan.
         */
        holder.itemView.setOnClickListener(c -> {
            categoryClickListener.onCategoryClicked(category);
        });
    }

    /**
     * Mengembalikan jumlah total data kategori dalam list.
     */
    @Override
    public int getItemCount() {
        return categories.size();
    }
}