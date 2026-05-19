package com.zypher.expensemanager.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zypher.expensemanager.R;
import com.zypher.expensemanager.databinding.RowAccountsBinding;
import com.zypher.expensemanager.models.Account;

import java.util.ArrayList;

/**
 * AccountsAdapter berfungsi sebagai jembatan (bridge) antara data (ArrayList of Account)
 * dengan tampilan UI (RecyclerView) untuk menampilkan daftar akun.
 */
public class AccountsAdapter extends RecyclerView.Adapter<AccountsAdapter.AccountsViewHolder> {

    Context context;
    ArrayList<Account> accounts;

    /**
     * Constructor untuk menerima data dari Activity/Fragment agar bisa dikelola oleh adapter.
     */
    public AccountsAdapter(Context context, ArrayList<Account> accounts) {
        this.context = context;
        this.accounts = accounts;
    }

    /**
     * Method untuk membuat ViewHolder baru.
     * Di sini kita menghubungkan layout XML row_accounts ke dalam Java.
     */
    @NonNull
    @Override
    public AccountsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate: Mengubah file XML layout menjadi objek View
        View view = LayoutInflater.from(context).inflate(R.layout.row_accounts, parent, false);
        return new AccountsViewHolder(view);
    }

    /**
     * Method untuk menghubungkan data dengan komponen UI pada setiap baris list.
     */
    @Override
    public void onBindViewHolder(@NonNull AccountsViewHolder holder, int position) {
        // Mengambil objek Account berdasarkan posisi baris di RecyclerView
        Account account = accounts.get(position);

        // Menampilkan nama akun ke TextView melalui View Binding
        holder.binding.accountName.setText(account.getAccountName());

        // Menampilkan saldo akun (konversi double ke String) ke TextView
        holder.binding.accountAmount.setText(String.valueOf(account.getAccountAmount()));
    }

    /**
     * Menentukan jumlah total item yang akan ditampilkan di dalam list.
     */
    @Override
    public int getItemCount() {
        return accounts.size();
    }

    /**
     * Class ViewHolder yang memegang referensi ke komponen UI di setiap baris.
     * Menggunakan View Binding untuk mempermudah akses ke ID komponen (R.id).
     */
    public static class AccountsViewHolder extends RecyclerView.ViewHolder {
        RowAccountsBinding binding;

        public AccountsViewHolder(@NonNull View itemView) {
            super(itemView);
            // Menghubungkan layout yang di-inflate dengan class binding
            binding = RowAccountsBinding.bind(itemView);
        }
    }
}