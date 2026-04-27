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

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    Context context;
    ArrayList<Transaction> transactions;

    // Interface untuk delete (dipanggil dari MainActivity)
    public interface TransactionDeleteListener {
        void onDeleteClicked(Transaction transaction);
    }

    TransactionDeleteListener deleteListener;

    public TransactionAdapter(Context context, ArrayList<Transaction> transactions, TransactionDeleteListener deleteListener) {
        this.context = context;
        this.transactions = transactions;
        this.deleteListener = deleteListener;
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        RowTransactionBinding binding;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = RowTransactionBinding.bind(itemView);
        }
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);

        // Isi teks
        holder.binding.transactionCategory.setText(transaction.getCategory());
        holder.binding.transactionDate.setText(transaction.getDate());
        holder.binding.accountLbl.setText(transaction.getAccount());

        // Warna amount: hijau = income, merah = expense
        if ("Income".equals(transaction.getType())) {
            holder.binding.transactionAmount.setText("+ " + (long) transaction.getAmount());
            holder.binding.transactionAmount.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            holder.binding.transactionAmount.setText("- " + (long) transaction.getAmount());
            holder.binding.transactionAmount.setTextColor(Color.parseColor("#F44336"));
        }

        // Icon berbeda sesuai kategori
        holder.binding.categoryIcon.setImageResource(getIconForCategory(transaction.getCategory()));

        // Long press untuk delete
        holder.itemView.setOnLongClickListener(v -> {
            deleteListener.onDeleteClicked(transaction);
            return true;
        });
    }

    // Pilih icon berdasarkan nama kategori
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

    // Dipanggil saat data Firebase berubah
    public void updateData(ArrayList<Transaction> newList) {
        this.transactions = newList;
        notifyDataSetChanged();
    }
}
