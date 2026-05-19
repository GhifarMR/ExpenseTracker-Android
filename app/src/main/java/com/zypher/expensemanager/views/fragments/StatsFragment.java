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

/**
 * StatsFragment berfungsi untuk menampilkan ringkasan statistik keuangan secara keseluruhan,
 * termasuk total pemasukan, pengeluaran, saldo, dan pembagian saldo per akun menggunakan bar progres.
 */
public class StatsFragment extends Fragment {

    DatabaseReference dbRef;
    TextView tvIncome, tvExpense, tvBalance;
    LinearLayout categoryContainer, accountContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Memuat layout fragment_stats
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        // Inisialisasi komponen UI
        tvIncome          = view.findViewById(R.id.statsIncome);
        tvExpense         = view.findViewById(R.id.statsExpense);
        tvBalance         = view.findViewById(R.id.statsBalance);
        accountContainer  = view.findViewById(R.id.accountContainer);

        // Referensi database ke node "transactions"
        dbRef = FirebaseDatabase.getInstance(
                "https://expense-manager-98f10-default-rtdb.asia-southeast1.firebasedatabase.app"
        ).getReference("transactions");

        /**
         * Mengambil data dari Firebase untuk diolah menjadi statistik.
         */
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                double totalIncome = 0, totalExpense = 0;
                // Map digunakan untuk mengelompokkan jumlah uang berdasarkan kategori dan akun
                Map<String, Double> categoryMap = new LinkedHashMap<>();
                Map<String, Double> accountMap  = new LinkedHashMap<>();

                for (DataSnapshot data : snapshot.getChildren()) {
                    Transaction t = data.getValue(Transaction.class);
                    if (t == null) continue;

                    boolean isIncome = "Income".equals(t.getType());
                    double amt = t.getAmount();

                    // Menghitung total kumulatif pemasukan dan pengeluaran
                    if (isIncome) {
                        totalIncome += amt;
                    } else {
                        totalExpense += amt;
                        // Mengelompokkan pengeluaran berdasarkan kategori
                        String cat = t.getCategory() != null ? t.getCategory() : "Other";
                        categoryMap.put(cat, categoryMap.getOrDefault(cat, 0.0) + amt);
                    }

                    // Menghitung saldo akhir untuk setiap akun (Cash, Bank, dll)
                    String acc = t.getAccount() != null ? t.getAccount() : "-";
                    double delta = isIncome ? amt : -amt;
                    accountMap.put(acc, accountMap.getOrDefault(acc, 0.0) + delta);
                }

                double balance = totalIncome - totalExpense;

                // Menampilkan ringkasan utama dengan simbol $
                tvIncome.setText("$" + fmt(totalIncome));
                tvExpense.setText("$" + fmt(totalExpense));
                tvBalance.setText("$" + fmt(balance));

                /**
                 * MEMBUAT VISUALISASI BAR PER AKUN
                 * Membersihkan kontainer lama dan mengisi dengan data terbaru.
                 */
                accountContainer.removeAllViews();
                double totalAbs = 0;
                // Menghitung total nilai absolut untuk menentukan persentase bar
                for (double v : accountMap.values()) totalAbs += Math.abs(v);

                for (Map.Entry<String, Double> entry : accountMap.entrySet()) {
                    // Hitung persentase untuk panjang ProgressBar
                    int pct = totalAbs > 0 ? (int)((Math.abs(entry.getValue()) / totalAbs) * 100) : 0;

                    // Menambahkan baris baru ke dalam layout secara dinamis
                    addRow(accountContainer, entry.getKey(), entry.getValue(), pct, totalAbs);
                }

                // Jika tidak ada data, tampilkan teks pemberitahuan
                if (accountMap.isEmpty()) {
                    addEmptyText(accountContainer, "No data yet");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });

        return view;
    }

    /**
     * Method untuk menambahkan baris statistik (Label, Angka, Persen, dan Bar) ke dalam UI.
     */
    private void addRow(LinearLayout parent, String label, double value, int pct, double max) {
        // Inflate layout row_stats yang berisi komponen ProgressBar dan TextView
        View row = LayoutInflater.from(requireContext()).inflate(R.layout.row_stats, parent, false);

        TextView tvLabel  = row.findViewById(R.id.rowLabel);
        TextView tvAmount = row.findViewById(R.id.rowAmount);
        TextView tvPct    = row.findViewById(R.id.rowPercent);
        ProgressBar bar   = row.findViewById(R.id.rowBar);

        tvLabel.setText(label);
        tvAmount.setText("$" + fmt(Math.abs(value)));
        tvPct.setText(pct + "%");

        // Mengatur panjang bar sesuai persentase data
        bar.setMax(100);
        bar.setProgress(pct);

        parent.addView(row);
    }

    /**
     * Menampilkan pesan teks jika data masih kosong.
     */
    private void addEmptyText(LinearLayout parent, String msg) {
        TextView tv = new TextView(requireContext());
        tv.setText(msg);
        tv.setTextSize(14f);
        tv.setPadding(0, 8, 0, 8);
        tv.setTextColor(0xFF888888);
        parent.addView(tv);
    }

    /**
     * Helper untuk memformat angka desimal menjadi string tanpa angka di belakang koma.
     */
    private String fmt(double val) {
        return String.format(Locale.getDefault(), "%.0f", val);
    }
}