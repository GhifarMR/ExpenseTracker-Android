package com.zypher.expensemanager.views.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
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
import com.zypher.expensemanager.views.fragments.AccountsFragment;
import com.zypher.expensemanager.views.fragments.AddTransactionFragment;
import com.zypher.expensemanager.views.fragments.MoreFragment;
import com.zypher.expensemanager.views.fragments.StatsFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    TransactionAdapter transactionAdapter;
    ArrayList<Transaction> transactionList = new ArrayList<>();
    DatabaseReference dbRef;

    // Untuk navigasi tanggal (bulan)
    Calendar currentCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Transactions");
        }

        // Tampilkan tanggal awal
        updateDateDisplay();

        // Tombol panah kiri = bulan sebelumnya
        binding.previousDate.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            updateDateDisplay();
            filterTransactions();
        });

        // Tombol panah kanan = bulan berikutnya
        binding.nextDate.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            updateDateDisplay();
            filterTransactions();
        });

        // Setup RecyclerView untuk list transaksi
        transactionAdapter = new TransactionAdapter(this, transactionList, transaction -> {
            // Tampil dialog konfirmasi hapus
            new AlertDialog.Builder(this)
                    .setTitle("Hapus Transaksi")
                    .setMessage("Yakin mau hapus transaksi ini?")
                    .setPositiveButton("Hapus", (dialog, which) -> deleteTransaction(transaction))
                    .setNegativeButton("Batal", null)
                    .show();
        });

        binding.transactionsList.setLayoutManager(new LinearLayoutManager(this));
        binding.transactionsList.setAdapter(transactionAdapter);

        // Koneksi ke Firebase
        dbRef = FirebaseDatabase.getInstance(
                "https://expense-manager-98f10-default-rtdb.asia-southeast1.firebasedatabase.app"
        ).getReference("transactions");

        loadTransactions();

        // FAB: buka form tambah transaksi
        binding.floatingActionButton.setOnClickListener(c -> {
            new AddTransactionFragment().show(getSupportFragmentManager(), null);
        });

        // Bottom Navigation
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.transactions) {
                binding.transactionsList.setVisibility(View.VISIBLE);
                binding.fragmentContainer.setVisibility(View.GONE);
                binding.floatingActionButton.setVisibility(View.VISIBLE);
                return true;
            }
             else if (id == R.id.stats) {
                showFragment(new StatsFragment());
                return true;
            } else if (id == R.id.accounts) {
                showFragment(new AccountsFragment());
                return true;
            } else if (id == R.id.more) {
                showFragment(new MoreFragment());
                return true;
            }
            return false;
        });
    }

    // Fungsi tampilkan fragment di atas RecyclerView
    private void showFragment(androidx.fragment.app.Fragment fragment) {
        binding.transactionsList.setVisibility(View.GONE);
        binding.fragmentContainer.setVisibility(View.VISIBLE);
        binding.floatingActionButton.setVisibility(View.GONE);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();

        binding.fragmentContainer.bringToFront(); // 🔥 TARUH DI SINI
    }

    // Update teks tanggal di header (contoh: "April 2026")
    private void updateDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        binding.currentDate.setText(sdf.format(currentCalendar.getTime()));
    }

    // Load semua transaksi dari Firebase secara realtime
    private void loadTransactions() {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                transactionList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Transaction t = data.getValue(Transaction.class);
                    if (t != null) transactionList.add(t);
                }
                filterTransactions(); // filter sesuai bulan aktif
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    // Filter transaksi sesuai bulan yang dipilih
    private void filterTransactions() {
        String bulanAktif = new SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                .format(currentCalendar.getTime());

        ArrayList<Transaction> filtered = new ArrayList<>();
        for (Transaction t : transactionList) {
            // Cek apakah tanggal transaksi mengandung bulan+tahun yang aktif
            if (t.getDate() != null && t.getDate().contains(
                    new SimpleDateFormat("yyyy", Locale.getDefault()).format(currentCalendar.getTime()))) {
                // Filter lebih sederhana: cek bulan dari string tanggal
                try {
                    java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault());
                    java.util.Date tDate = inputFormat.parse(t.getDate());
                    Calendar tCal = Calendar.getInstance();
                    tCal.setTime(tDate);
                    if (tCal.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH)
                            && tCal.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR)) {
                        filtered.add(t);
                    }
                } catch (Exception e) {
                    // kalau format tanggal tidak cocok, skip
                }
            }
        }

        transactionAdapter.updateData(filtered);
        updateSummary(filtered);
    }

    // Hapus transaksi dari Firebase
    private void deleteTransaction(Transaction transaction) {
        dbRef.child(transaction.getId()).removeValue();
    }

    // Hitung dan tampilkan total income, expense, dan saldo
    private void updateSummary(ArrayList<Transaction> list) {
        double totalIncome = 0;
        double totalExpense = 0;

        for (Transaction t : list) {
            if ("Income".equals(t.getType())) {
                totalIncome += t.getAmount();
            } else {
                totalExpense += t.getAmount();
            }
        }

        double total = totalIncome - totalExpense;

        binding.textView7.setText(String.format(Locale.getDefault(), "%.0f", totalIncome));
        binding.textView5.setText(String.format(Locale.getDefault(), "%.0f", totalExpense));
        binding.textView2.setText(String.format(Locale.getDefault(), "%.0f", total));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
