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

public class AccountsAdapter extends RecyclerView.Adapter<AccountsAdapter.AccountsViewHolder> {

    Context context;
    ArrayList<Account> accounts;

    public AccountsAdapter(Context context, ArrayList<Account> accounts) {
        this.context = context;
        this.accounts = accounts;
    }

    @NonNull
    @Override
    public AccountsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_accounts, parent, false);
        return new AccountsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountsViewHolder holder, int position) {
        Account account = accounts.get(position);
        holder.binding.accountName.setText(account.getAccountName());
        holder.binding.accountAmount.setText(String.valueOf(account.getAccountAmount()));
    }

    @Override
    public int getItemCount() {
        return accounts.size();
    }

    public static class AccountsViewHolder extends RecyclerView.ViewHolder {
        RowAccountsBinding binding;

        public AccountsViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = RowAccountsBinding.bind(itemView);
        }
    }
}
