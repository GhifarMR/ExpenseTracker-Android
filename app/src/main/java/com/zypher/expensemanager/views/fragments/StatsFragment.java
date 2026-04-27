package com.zypher.expensemanager.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zypher.expensemanager.R;
import com.zypher.expensemanager.models.Transaction;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class StatsFragment extends Fragment {

    DatabaseReference dbRef;
    TextView tvIncome, tvExpense, tvBalance;
    LinearLayout categoryContainer, accountContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        tvIncome          = view.findViewById(R.id.statsIncome);
        tvExpense         = view.findViewById(R.id.statsExpense);
        tvBalance         = view.findViewById(R.id.statsBalance);
        accountContainer  = view.findViewById(R.id.accountContainer);

        dbRef = FirebaseDatabase.getInstance(
                "https://expense-manager-98f10-default-rtdb.asia-southeast1.firebasedatabase.app"
        ).getReference("transactions");

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                double totalIncome = 0, totalExpense = 0;
                Map<String, Double> categoryMap = new LinkedHashMap<>();
                Map<String, Double> accountMap  = new LinkedHashMap<>();

                for (DataSnapshot data : snapshot.getChildren()) {
                    Transaction t = data.getValue(Transaction.class);
                    if (t == null) continue;

                    boolean isIncome = "Income".equals(t.getType());
                    double amt = t.getAmount();

                    if (isIncome) {
                        totalIncome += amt;
                    } else {
                        totalExpense += amt;
                        String cat = t.getCategory() != null ? t.getCategory() : "Other";
                        categoryMap.put(cat, categoryMap.getOrDefault(cat, 0.0) + amt);
                    }

                    String acc = t.getAccount() != null ? t.getAccount() : "-";
                    double delta = isIncome ? amt : -amt;
                    accountMap.put(acc, accountMap.getOrDefault(acc, 0.0) + delta);
                }

                double balance = totalIncome - totalExpense;

                tvIncome.setText("Rp " + fmt(totalIncome));
                tvExpense.setText("Rp " + fmt(totalExpense));
                tvBalance.setText("Rp " + fmt(balance));


                // Breakdown per akun (saldo)
                accountContainer.removeAllViews();
                double totalAbs = 0;
                for (double v : accountMap.values()) totalAbs += Math.abs(v);
                for (Map.Entry<String, Double> entry : accountMap.entrySet()) {
                    int pct = totalAbs > 0 ? (int)((Math.abs(entry.getValue()) / totalAbs) * 100) : 0;
                    addRow(accountContainer, entry.getKey(), entry.getValue(), pct, totalAbs);
                }
                if (accountMap.isEmpty()) {
                    addEmptyText(accountContainer, "Belum ada data.");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });

        return view;
    }

    private void addRow(LinearLayout parent, String label, double value, int pct, double max) {
        // Inflate row dari XML
        View row = LayoutInflater.from(requireContext()).inflate(R.layout.row_stats, parent, false);

        TextView tvLabel  = row.findViewById(R.id.rowLabel);
        TextView tvAmount = row.findViewById(R.id.rowAmount);
        TextView tvPct    = row.findViewById(R.id.rowPercent);
        ProgressBar bar   = row.findViewById(R.id.rowBar);

        tvLabel.setText(label);
        tvAmount.setText("Rp " + fmt(Math.abs(value)));
        tvPct.setText(pct + "%");
        bar.setMax(100);
        bar.setProgress(pct);

        parent.addView(row);
    }

    private void addEmptyText(LinearLayout parent, String msg) {
        TextView tv = new TextView(requireContext());
        tv.setText(msg);
        tv.setTextSize(14f);
        tv.setPadding(0, 8, 0, 8);
        tv.setTextColor(0xFF888888);
        parent.addView(tv);
    }

    private String fmt(double val) {
        return String.format(Locale.getDefault(), "%.0f", val);
    }
}