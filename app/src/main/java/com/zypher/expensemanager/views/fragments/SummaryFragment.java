package com.zypher.expensemanager.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.zypher.expensemanager.R;
import com.zypher.expensemanager.models.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

public class SummaryFragment extends Fragment {

    private static final String ARG_TRANSACTIONS = "transactions";
    private ArrayList<Transaction> transactions;

    public static SummaryFragment newInstance(ArrayList<Transaction> transactions) {
        SummaryFragment fragment = new SummaryFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TRANSACTIONS, transactions);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            transactions = (ArrayList<Transaction>) getArguments().getSerializable(ARG_TRANSACTIONS);
        }
        if (transactions == null) transactions = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_summary, container, false);

        TextView tvIncome      = view.findViewById(R.id.summaryIncome);
        TextView tvExpense     = view.findViewById(R.id.summaryExpense);
        TextView tvBalance     = view.findViewById(R.id.summaryBalance);
        TextView tvTopCategory = view.findViewById(R.id.summaryTopCategory);
        TextView tvTopAccount  = view.findViewById(R.id.summaryTopAccount);
        TextView tvTxCount     = view.findViewById(R.id.summaryTxCount);

        double totalIncome = 0, totalExpense = 0;
        Map<String, Double> categoryMap = new HashMap<>();
        Map<String, Double> accountMap  = new HashMap<>();

        for (Transaction t : transactions) {
            double amount = t.getAmount();
            if ("Income".equals(t.getType())) {
                totalIncome += amount;
            } else {
                totalExpense += amount;
                String cat = t.getCategory() != null ? t.getCategory() : "Other";
                categoryMap.put(cat, categoryMap.getOrDefault(cat, 0.0) + amount);
            }
            String acc = t.getAccount() != null ? t.getAccount() : "-";
            accountMap.put(acc, accountMap.getOrDefault(acc, 0.0) + amount);
        }

        double balance = totalIncome - totalExpense;

        // Cari kategori expense terbesar
        String topCat = "-";
        double topCatAmount = 0;
        for (Map.Entry<String, Double> e : categoryMap.entrySet()) {
            if (e.getValue() > topCatAmount) {
                topCatAmount = e.getValue();
                topCat = e.getKey();
            }
        }

        // Cari account paling banyak dipakai (berdasarkan total transaksi)
        String topAcc = "-";
        double topAccAmount = 0;
        for (Map.Entry<String, Double> e : accountMap.entrySet()) {
            if (e.getValue() > topAccAmount) {
                topAccAmount = e.getValue();
                topAcc = e.getKey();
            }
        }

        tvIncome.setText("Total Income: Rp " + String.format(Locale.getDefault(), "%.0f", totalIncome));
        tvExpense.setText("Total Expense: Rp " + String.format(Locale.getDefault(), "%.0f", totalExpense));
        tvBalance.setText("Balance This Month: Rp " + String.format(Locale.getDefault(), "%.0f", balance));
        tvTopCategory.setText("Highest expense: " + topCat
                + " (Rp " + String.format(Locale.getDefault(), "%.0f", topCatAmount) + ")");
        tvTopAccount.setText("Most Active Account: " + topAcc);
        tvTxCount.setText("Transaction Count: " + transactions.size());

        return view;
    }
}
