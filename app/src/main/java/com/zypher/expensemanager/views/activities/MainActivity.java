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

/**
 * MainActivity adalah Controller utama aplikasi.
 * Mengatur navigasi antar fitur, manajemen tab, filter waktu, dan sinkronisasi Firebase.
 */
public class MainActivity extends AppCompatActivity {

    // View Binding untuk mengakses komponen UI tanpa findViewById
    ActivityMainBinding binding;

    // Adapter dan List untuk menampilkan data transaksi di RecyclerView
    TransactionAdapter transactionAdapter;
    ArrayList<Transaction> transactionList = new ArrayList<>();

    // Referensi ke Firebase Realtime Database
    DatabaseReference dbRef;

    // Status tab aktif: 0=Daily, 1=Monthly, 2=Summary, 3=Notes
    int currentTab = 1;

    // Objek Calendar untuk menyimpan status tanggal/bulan yang sedang dipilih user
    Calendar currentCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inisialisasi View Binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup Toolbar/ActionBar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Transactions");
        }

        // Mengatur tab "Monthly" sebagai pilihan awal di UI
        binding.tabLayout.selectTab(binding.tabLayout.getTabAt(1));

        // Menampilkan tanggal/bulan awal di UI
        updateDateDisplay();

        /**
         * Navigasi Tanggal (Back): Mengurangi hari atau bulan
         * tergantung pada tab yang sedang aktif.
         */
        binding.previousDate.setOnClickListener(v -> {
            if (currentTab == 0) {
                currentCalendar.add(Calendar.DAY_OF_MONTH, -1); // Mundur 1 hari
            } else {
                currentCalendar.add(Calendar.MONTH, -1); // Mundur 1 bulan
            }
            updateDateDisplay();
            applyCurrentTab(); // Refresh tampilan data
        });

        /**
         * Navigasi Tanggal (Forward): Menambah hari atau bulan.
         */
        binding.nextDate.setOnClickListener(v -> {
            if (currentTab == 0) {
                currentCalendar.add(Calendar.DAY_OF_MONTH, 1); // Maju 1 hari
            } else {
                currentCalendar.add(Calendar.MONTH, 1); // Maju 1 bulan
            }
            updateDateDisplay();
            applyCurrentTab();
        });

        /**
         * Inisialisasi Adapter dengan listener klik lama untuk menghapus data.
         * Menampilkan dialog konfirmasi sebelum menghapus ke database.
         */
        transactionAdapter = new TransactionAdapter(this, transactionList, transaction -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete the transaction")
                    .setMessage("Are you sure you want to delete this transaction?")
                    .setPositiveButton("Delete", (dialog, which) -> deleteTransaction(transaction))
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Pengaturan RecyclerView
        binding.transactionsList.setLayoutManager(new LinearLayoutManager(this));
        binding.transactionsList.setAdapter(transactionAdapter);

        // Menghubungkan ke node "transactions" di Firebase
        dbRef = FirebaseDatabase.getInstance(
                "https://expense-manager-98f10-default-rtdb.asia-southeast1.firebasedatabase.app"
        ).getReference("transactions");

        // Mulai memantau perubahan data dari Firebase secara real-time
        loadTransactions();

        // FAB (Floating Action Button) untuk menambah transaksi baru
        binding.floatingActionButton.setOnClickListener(c -> {
            new AddTransactionFragment().show(getSupportFragmentManager(), null);
        });

        /**
         * Listener untuk mendeteksi perpindahan Tab (Daily, Monthly, dll).
         */
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                applyCurrentTab(); // Update konten sesuai tab yang dipilih
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        /**
         * Bottom Navigation: Mengatur tampilan utama atau memunculkan Fragment lain.
         */
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.transactions) {
                // Kembali ke tampilan utama (List Transaksi)
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

    /**
     * Mengatur konten apa yang harus muncul berdasarkan tab yang aktif.
     */
    private void applyCurrentTab() {
        switch (currentTab) {
            case 0: // Daily: List transaksi harian
                showTransactionList();
                filterByDay();
                break;
            case 1: // Monthly: List transaksi bulanan
                showTransactionList();
                filterByMonth();
                break;
            case 2: // Summary: Ringkasan statistik bulanan
                showSummaryFragment();
                break;
            case 3: // Notes: Daftar catatan dari transaksi
                showNotesFragment();
                break;
        }
    }

    /**
     * Menampilkan kembali RecyclerView dan elemen navigasi tanggal.
     */
    private void showTransactionList() {
        binding.transactionsList.setVisibility(View.VISIBLE);
        binding.fragmentContainer.setVisibility(View.GONE);
        binding.floatingActionButton.setVisibility(View.VISIBLE);
        updateDateDisplay();
    }

    /**
     * Memunculkan SummaryFragment dan mengirimkan data transaksi bulan ini.
     */
    private void showSummaryFragment() {
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

    /**
     * Memunculkan NotesFragment untuk melihat catatan transaksi bulan ini.
     */
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

    /**
     * Method umum untuk mengganti container utama dengan sebuah Fragment.
     */
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

    /**
     * Mengubah format teks tanggal di header berdasarkan pilihan tab (Harian vs Bulanan).
     */
    private void updateDateDisplay() {
        if (currentTab == 0) {
            // Daily: Format hari tanggal bulan tahun
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
            binding.currentDate.setText(sdf.format(currentCalendar.getTime()));
        } else {
            // Monthly/Lainnya: Format bulan dan tahun saja
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
            binding.currentDate.setText(sdf.format(currentCalendar.getTime()));
        }
    }

    /**
     * Mengambil seluruh data dari Firebase.
     * addValueEventListener akan terpanggil otomatis setiap kali ada perubahan data di cloud.
     */
    private void loadTransactions() {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                transactionList.clear(); // Bersihkan list lama agar tidak duplikat
                for (DataSnapshot data : snapshot.getChildren()) {
                    Transaction t = data.getValue(Transaction.class);
                    if (t != null) transactionList.add(t);
                }
                applyCurrentTab(); // Tampilkan data yang sudah diupdate
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    /**
     * Menyaring transactionList hanya untuk data yang tanggalnya sama dengan pilihan user.
     */
    private void filterByDay() {
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault());
        ArrayList<Transaction> filtered = new ArrayList<>();
        for (Transaction t : transactionList) {
            try {
                java.util.Date tDate = inputFormat.parse(t.getDate());
                Calendar tCal = Calendar.getInstance();
                tCal.setTime(tDate);
                // Bandingkan Hari, Bulan, dan Tahun
                if (tCal.get(Calendar.DAY_OF_MONTH) == currentCalendar.get(Calendar.DAY_OF_MONTH)
                        && tCal.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH)
                        && tCal.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR)) {
                    filtered.add(t);
                }
            } catch (Exception e) { /* abaikan error format */ }
        }
        transactionAdapter.updateData(filtered); // Update RecyclerView
        updateSummary(filtered); // Update widget total saldo
    }

    /**
     * Menyaring data berdasarkan bulan dan tahun yang sedang dipilih.
     */
    private void filterByMonth() {
        transactionAdapter.updateData(getMonthlyTransactions());
        updateSummary(getMonthlyTransactions());
    }

    /**
     * Method helper untuk mendapatkan list transaksi dalam bulan yang aktif.
     */
    private ArrayList<Transaction> getMonthlyTransactions() {
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault());
        ArrayList<Transaction> filtered = new ArrayList<>();
        for (Transaction t : transactionList) {
            try {
                java.util.Date tDate = inputFormat.parse(t.getDate());
                Calendar tCal = Calendar.getInstance();
                tCal.setTime(tDate);
                // Bandingkan Bulan dan Tahun
                if (tCal.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH)
                        && tCal.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR)) {
                    filtered.add(t);
                }
            } catch (Exception e) { /* abaikan */ }
        }
        return filtered;
    }

    /**
     * Menghapus transaksi di Firebase berdasarkan ID uniknya.
     */
    private void deleteTransaction(Transaction transaction) {
        dbRef.child(transaction.getId()).removeValue();
    }

    /**
     * Menghitung total Pemasukan, Pengeluaran, dan Saldo (Total)
     * dari list yang sedang ditampilkan, lalu menampilkannya ke UI.
     */
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
        binding.textView7.setText(String.format(Locale.getDefault(), "$%.0f", totalIncome));
        binding.textView5.setText(String.format(Locale.getDefault(), "$%.0f", totalExpense));
        binding.textView2.setText(String.format(Locale.getDefault(), "$%.0f", total));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}