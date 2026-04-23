package com.zypher.expensemanager.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zypher.expensemanager.R;
import com.zypher.expensemanager.models.Transaction;

import java.util.HashMap;
import java.util.Map;

public class StatsFragment extends Fragment {

    // Karena tidak ada XML khusus untuk fragment ini,
    // kita buat layout sederhana secara programmatik
    TextView tvIncome, tvExpense, tvTotal, tvTopCategory;
    DatabaseReference dbRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate layout stats
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        tvIncome      = view.findViewById(R.id.statsIncome);
        tvExpense     = view.findViewById(R.id.statsExpense);
        tvTotal       = view.findViewById(R.id.statsTotal);
        tvTopCategory = view.findViewById(R.id.statsTopCategory);

        dbRef = FirebaseDatabase.getInstance(
                "https://expense-manager-98f10-default-rtdb.asia-southeast1.firebasedatabase.app"
        ).getReference("transactions");

        loadStats();

        return view;
    }

    private void loadStats() {
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                double totalIncome = 0;
                double totalExpense = 0;
                Map<String, Double> categoryExpense = new HashMap<>();

                for (DataSnapshot data : snapshot.getChildren()) {
                    Transaction t = data.getValue(Transaction.class);
                    if (t == null) continue;

                    if ("Income".equals(t.getType())) {
                        totalIncome += t.getAmount();
                    } else {
                        totalExpense += t.getAmount();
                        // Hitung expense per kategori
                        String cat = t.getCategory() != null ? t.getCategory() : "Other";
                        categoryExpense.put(cat, categoryExpense.getOrDefault(cat, 0.0) + t.getAmount());
                    }
                }

                // Cari kategori dengan expense terbesar
                String topCat = "-";
                double topAmount = 0;
                for (Map.Entry<String, Double> entry : categoryExpense.entrySet()) {
                    if (entry.getValue() > topAmount) {
                        topAmount = entry.getValue();
                        topCat = entry.getKey();
                    }
                }

                double total = totalIncome - totalExpense;

                tvIncome.setText("Income: " + (long) totalIncome);
                tvExpense.setText("Expense: " + (long) totalExpense);
                tvTotal.setText("Balance: " + (long) total);
                tvTopCategory.setText("Highest Expense: (" + topCat + ") " + (long) topAmount + "");
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }
}
