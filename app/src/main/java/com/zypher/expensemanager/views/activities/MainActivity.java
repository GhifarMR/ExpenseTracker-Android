package com.zypher.expensemanager.views.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Menu;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zypher.expensemanager.R;
import com.zypher.expensemanager.adapters.TransactionAdapter;
import com.zypher.expensemanager.databinding.ActivityMainBinding;
import com.zypher.expensemanager.models.Transaction;
import com.zypher.expensemanager.views.fragments.AddTransactionFragment;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    TransactionAdapter transactionAdapter;
    ArrayList<Transaction> transactionList = new ArrayList<>();
    DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());

        // ── Toolbar ──────────────────────────────────────────────────────────
        WindowInsetsControllerCompat windowInsetsController =
                ViewCompat.getWindowInsetsController(getWindow().getDecorView());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
            windowInsetsController.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            );
            getSupportActionBar().setTitle("Transactions");
        }

        // ── Setup RecyclerView ───────────────────────────────────────────────
        transactionAdapter = new TransactionAdapter(this, transactionList, transaction -> {
            // Konfirmasi delete saat long press
            new AlertDialog.Builder(this)
                    .setTitle("Hapus Transaksi")
                    .setMessage("Yakin mau hapus transaksi ini?")
                    .setPositiveButton("Hapus", (dialog, which) -> deleteTransaction(transaction))
                    .setNegativeButton("Batal", null)
                    .show();
        });

        binding.transactionsList.setLayoutManager(new LinearLayoutManager(this));
        binding.transactionsList.setAdapter(transactionAdapter);

        // ── Firebase: ambil data realtime ────────────────────────────────────
        dbRef = FirebaseDatabase.getInstance("https://expense-manager-98f10-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("transactions");
        loadTransactions();

        // ── FAB: buka AddTransactionFragment ─────────────────────────────────
        binding.floatingActionButton.setOnClickListener(c -> {
            new AddTransactionFragment().show(getSupportFragmentManager(), null);
        });
    }

    private void loadTransactions() {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                transactionList.clear();

                for (DataSnapshot data : snapshot.getChildren()) {
                    Transaction t = data.getValue(Transaction.class);
                    if (t != null) {
                        transactionList.add(t);
                    }
                }

                transactionAdapter.updateData(transactionList);
                updateSummary(); // hitung ulang income/expense/total
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // bisa tambah log error di sini kalau perlu
            }
        });
    }

    private void deleteTransaction(Transaction transaction) {
        dbRef.child(transaction.getId()).removeValue()
                .addOnSuccessListener(unused -> {
                    // Data otomatis update karena kita pakai addValueEventListener
                });
    }

    private void updateSummary() {
        double totalIncome = 0;
        double totalExpense = 0;

        for (Transaction t : transactionList) {
            if (t.getType().equals("Income")) {
                totalIncome += t.getAmount();
            } else {
                totalExpense += t.getAmount();
            }
        }

        double total = totalIncome - totalExpense;

        // textView7 = income value, textView5 = expense value, textView2 = total value
        // (sesuai id di activity_main.xml)
        binding.textView7.setText(String.format("%.0f", totalIncome));
        binding.textView5.setText(String.format("%.0f", totalExpense));
        binding.textView2.setText(String.format("%.0f", total));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}