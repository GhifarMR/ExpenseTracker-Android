package com.zypher.expensemanager.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zypher.expensemanager.R;
import com.zypher.expensemanager.databinding.RowTransactionBinding;
import com.zypher.expensemanager.models.Transaction;

import java.util.ArrayList;

/**
 * TransactionAdapter berfungsi untuk mengelola dan menampilkan daftar transaksi (pemasukan/pengeluaran).
 * Adapter ini menangani pewarnaan teks secara dinamis dan aksi hapus data.
 */
public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    Context context;
    ArrayList<Transaction> transactions;

    /**
     * Interface untuk menangani aksi hapus (delete).
     * Akan diimplementasikan di Activity/Fragment agar logika database tetap terpusat.
     */
    public interface TransactionDeleteListener {
        void onDeleteClicked(Transaction transaction);
    }

    TransactionDeleteListener deleteListener;

    /**
     * Constructor untuk inisialisasi Context, daftar transaksi, dan listener hapus.
     */
    public TransactionAdapter(Context context, ArrayList<Transaction> transactions, TransactionDeleteListener deleteListener) {
        this.context = context;
        this.transactions = transactions;
        this.deleteListener = deleteListener;
    }

    /**
     * ViewHolder yang memegang referensi komponen UI dari layout row_transaction.
     */
    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        RowTransactionBinding binding;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            // Menggunakan View Binding untuk menghubungkan ID komponen
            binding = RowTransactionBinding.bind(itemView);
        }
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout XML row_transaction menjadi tampilan baris list
        View view = LayoutInflater.from(context).inflate(R.layout.row_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        // Mengambil objek transaksi berdasarkan posisi list
        Transaction transaction = transactions.get(position);

        // Menampilkan data teks ke UI
        holder.binding.transactionCategory.setText(transaction.getCategory());
        holder.binding.transactionDate.setText(transaction.getDate());
        holder.binding.accountLbl.setText(transaction.getAccount());

        /**
         * Logika Pewarnaan Saldo:
         * Jika tipe adalah "Income", teks berwarna hijau dengan awalan "+".
         * Jika tipe adalah "Expense", teks berwarna merah dengan awalan "-".
         */
        if ("Income".equals(transaction.getType())) {
            holder.binding.transactionAmount.setText("+ $" + (long) transaction.getAmount());
            holder.binding.transactionAmount.setTextColor(Color.parseColor("#4CAF50")); // Warna Hijau
        } else {
            holder.binding.transactionAmount.setText("- $" + (long) transaction.getAmount());
            holder.binding.transactionAmount.setTextColor(Color.parseColor("#F44336")); // Warna Merah
        }

        // Mengatur icon kategori secara otomatis berdasarkan nama kategori
        holder.binding.categoryIcon.setImageResource(getIconForCategory(transaction.getCategory()));

        /**
         * Event Long Click (Klik Lama).
         * Digunakan untuk memicu aksi hapus melalui interface deleteListener.
         */
        holder.itemView.setOnLongClickListener(v -> {
            deleteListener.onDeleteClicked(transaction);
            return true;
        });
    }

    /**
     * Method Helper untuk menentukan ID resource icon berdasarkan nama kategori.
     */
    private int getIconForCategory(String category) {
        if (category == null) return R.drawable.ic_wallet;
        switch (category) {
            case "Salary":     return R.drawable.ic_income;
            case "Business":   return R.drawable.ic_briefcase;
            case "Investment": return R.drawable.ic_bar_chart;
            case "Loan":       return R.drawable.ic_loan;
            case "Rent":       return R.drawable.ic_key;
            default:           return R.drawable.ic_wallet;
        }
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    /**
     * Method untuk memperbarui data di dalam adapter ketika ada perubahan di Firebase.
     * notifyDataSetChanged() memberitahu RecyclerView untuk menggambar ulang tampilan.
     */
    public void updateData(ArrayList<Transaction> newList) {
        this.transactions = newList;
        notifyDataSetChanged();
    }
}