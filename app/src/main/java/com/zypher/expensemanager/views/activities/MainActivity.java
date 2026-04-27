package com.zypher.expensemanager.views.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.tabs.TabLayout;
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
import com.zypher.expensemanager.views.fragments.SummaryFragment;
import com.zypher.expensemanager.views.fragments.NotesFragment;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    TransactionAdapter transactionAdapter;
    ArrayList<Transaction> transactionList = new ArrayList<>();
    DatabaseReference dbRef;

    // 0 = Daily, 1 = Monthly, 2 = Summary, 3 = Notes
    int currentTab = 1; // default Monthly

    // Untuk navigasi tanggal
    Calendar currentCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Transactions");
        }

        // Set tab Monthly sebagai default yang aktif
        binding.tabLayout.selectTab(binding.tabLayout.getTabAt(1));

        updateDateDisplay();

        binding.previousDate.setOnClickListener(v -> {
            if (currentTab == 0) {
                currentCalendar.add(Calendar.DAY_OF_MONTH, -1);
            } else {
                currentCalendar.add(Calendar.MONTH, -1);
            }
            updateDateDisplay();
            applyCurrentTab();
        });

        binding.nextDate.setOnClickListener(v -> {
            if (currentTab == 0) {
                currentCalendar.add(Calendar.DAY_OF_MONTH, 1);
            } else {
                currentCalendar.add(Calendar.MONTH, 1);
            }
            updateDateDisplay();
            applyCurrentTab();
        });

        // Setup RecyclerView
        transactionAdapter = new TransactionAdapter(this, transactionList, transaction -> {
            new AlertDialog.Builder(this)
                    .setTitle("Hapus Transaksi")
                    .setMessage("Yakin mau hapus transaksi ini?")
                    .setPositiveButton("Hapus", (dialog, which) -> deleteTransaction(transaction))
                    .setNegativeButton("Batal", null)
                    .show();
        });

        binding.transactionsList.setLayoutManager(new LinearLayoutManager(this));
        binding.transactionsList.setAdapter(transactionAdapter);

        dbRef = FirebaseDatabase.getInstance(
                "https://expense-manager-98f10-default-rtdb.asia-southeast1.firebasedatabase.app"
        ).getReference("transactions");

        loadTransactions();

        binding.floatingActionButton.setOnClickListener(c -> {
            new AddTransactionFragment().show(getSupportFragmentManager(), null);
        });

        // Tab listener
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                applyCurrentTab();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Bottom Navigation
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.transactions) {
                binding.transactionsList.setVisibility(View.VISIBLE);
                binding.fragmentContainer.setVisibility(View.GONE);
                binding.floatingActionButton.setVisibility(View.VISIBLE);
                binding.tabLayout.setVisibility(View.VISIBLE);
                binding.linearLayout.setVisibility(View.VISIBLE);
                binding.linearLayout2.setVisibility(View.VISIBLE);
                return true;
            } else if (id == R.id.stats) {
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

    private void applyCurrentTab() {
        switch (currentTab) {
            case 0: // Daily
                showTransactionList();
                filterByDay();
                break;
            case 1: // Monthly
                showTransactionList();
                filterByMonth();
                break;
            case 2: // Summary
                showSummaryFragment();
                break;
            case 3: // Notes
                showNotesFragment();
                break;
        }
    }

    private void showTransactionList() {
        binding.transactionsList.setVisibility(View.VISIBLE);
        binding.fragmentContainer.setVisibility(View.GONE);
        binding.floatingActionButton.setVisibility(View.VISIBLE);
        updateDateDisplay();
    }

    private void showSummaryFragment() {
        // Kirim data summary bulan aktif ke fragment
        ArrayList<Transaction> monthly = getMonthlyTransactions();
        SummaryFragment fragment = SummaryFragment.newInstance(monthly);
        binding.transactionsList.setVisibility(View.GONE);
        binding.fragmentContainer.setVisibility(View.VISIBLE);
        binding.floatingActionButton.setVisibility(View.GONE);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void showNotesFragment() {
        ArrayList<Transaction> monthly = getMonthlyTransactions();
        NotesFragment fragment = NotesFragment.newInstance(monthly);
        binding.transactionsList.setVisibility(View.GONE);
        binding.fragmentContainer.setVisibility(View.VISIBLE);
        binding.floatingActionButton.setVisibility(View.GONE);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void showFragment(androidx.fragment.app.Fragment fragment) {
        binding.transactionsList.setVisibility(View.GONE);
        binding.fragmentContainer.setVisibility(View.VISIBLE);
        binding.floatingActionButton.setVisibility(View.GONE);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
        binding.fragmentContainer.bringToFront();
    }

    private void updateDateDisplay() {
        if (currentTab == 0) {
            // Daily: tampilkan tanggal
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
            binding.currentDate.setText(sdf.format(currentCalendar.getTime()));
        } else {
            // Monthly/Summary/Notes: tampilkan bulan
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
            binding.currentDate.setText(sdf.format(currentCalendar.getTime()));
        }
    }

    private void loadTransactions() {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                transactionList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Transaction t = data.getValue(Transaction.class);
                    if (t != null) transactionList.add(t);
                }
                applyCurrentTab();
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    // Filter transaksi per hari (Daily)
    private void filterByDay() {
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault());
        ArrayList<Transaction> filtered = new ArrayList<>();
        for (Transaction t : transactionList) {
            try {
                java.util.Date tDate = inputFormat.parse(t.getDate());
                Calendar tCal = Calendar.getInstance();
                tCal.setTime(tDate);
                if (tCal.get(Calendar.DAY_OF_MONTH) == currentCalendar.get(Calendar.DAY_OF_MONTH)
                        && tCal.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH)
                        && tCal.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR)) {
                    filtered.add(t);
                }
            } catch (Exception e) { /* skip */ }
        }
        transactionAdapter.updateData(filtered);
        updateSummary(filtered);
    }

    // Filter transaksi per bulan (Monthly)
    private void filterByMonth() {
        transactionAdapter.updateData(getMonthlyTransactions());
        updateSummary(getMonthlyTransactions());
    }

    // Ambil transaksi bulan aktif
    private ArrayList<Transaction> getMonthlyTransactions() {
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault());
        ArrayList<Transaction> filtered = new ArrayList<>();
        for (Transaction t : transactionList) {
            try {
                java.util.Date tDate = inputFormat.parse(t.getDate());
                Calendar tCal = Calendar.getInstance();
                tCal.setTime(tDate);
                if (tCal.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH)
                        && tCal.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR)) {
                    filtered.add(t);
                }
            } catch (Exception e) { /* skip */ }
        }
        return filtered;
    }

    private void deleteTransaction(Transaction transaction) {
        dbRef.child(transaction.getId()).removeValue();
    }

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