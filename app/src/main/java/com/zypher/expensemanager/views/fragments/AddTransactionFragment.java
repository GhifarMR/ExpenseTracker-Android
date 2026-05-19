package com.zypher.expensemanager.views.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.zypher.expensemanager.R;
import com.zypher.expensemanager.adapters.CategoryAdapter;
import com.zypher.expensemanager.databinding.FragmentAddTransactionBinding;
import com.zypher.expensemanager.databinding.ListDialogBinding;
import com.zypher.expensemanager.models.Category;
import com.zypher.expensemanager.models.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * AddTransactionFragment menggunakan BottomSheetDialogFragment agar muncul sebagai
 * panel dari bawah layar. Berfungsi untuk input data transaksi baru.
 */
public class AddTransactionFragment extends BottomSheetDialogFragment {

    public AddTransactionFragment() {}

    FragmentAddTransactionBinding binding;
    // Variabel untuk menentukan apakah transaksi ini Income atau Expense
    String selectedType = "Income";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inisialisasi View Binding untuk layout fragment_add_transaction
        binding = FragmentAddTransactionBinding.inflate(inflater);

        /**
         * Logika klik tombol Income:
         * Mengubah warna background tombol dan memperbarui variabel selectedType.
         */
        binding.incomeBtn.setOnClickListener(view -> {
            selectedType = "Income";
            binding.incomeBtn.setBackgroundResource(R.drawable.income_selector);
            binding.expenseBtn.setBackgroundResource(R.drawable.default_selector);
            binding.incomeBtn.setTextColor(getContext().getColor(R.color.white));
            binding.expenseBtn.setTextColor(getContext().getColor(R.color.black));
        });

        /**
         * Logika klik tombol Expense:
         * Mengubah warna background menjadi merah (sesuai selector) dan set tipe ke Expense.
         */
        binding.expenseBtn.setOnClickListener(view -> {
            selectedType = "Expense";
            binding.incomeBtn.setBackgroundResource(R.drawable.default_selector);
            binding.expenseBtn.setBackgroundResource(R.drawable.expense_selector);
            binding.incomeBtn.setTextColor(getContext().getColor(R.color.black));
            binding.expenseBtn.setTextColor(getContext().getColor(R.color.white));
        });

        /**
         * Memunculkan DatePickerDialog untuk memilih tanggal transaksi.
         */
        binding.date.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext());
            datePickerDialog.setOnDateSetListener((datePicker, year, month, day) -> {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
                calendar.set(Calendar.MONTH, datePicker.getMonth());
                calendar.set(Calendar.YEAR, datePicker.getYear());

                // Format tanggal agar seragam saat disimpan ke database
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM, yyyy");
                binding.date.setText(dateFormat.format(calendar.getTime()));
            });
            datePickerDialog.show();
        });

        /**
         * Memunculkan Custom Dialog untuk memilih kategori.
         * Menggunakan RecyclerView di dalam dialog dengan tampilan Grid (3 kolom).
         */
        binding.category.setOnClickListener(c -> {
            ListDialogBinding dialogBinding = ListDialogBinding.inflate(inflater);
            AlertDialog categoryDialog = new AlertDialog.Builder(getContext()).create();
            categoryDialog.setView(dialogBinding.getRoot());

            // List kategori yang tersedia secara statis
            ArrayList<Category> categories = new ArrayList<>();
            categories.add(new Category("Salary",     R.drawable.ic_income,     R.color.category1));
            categories.add(new Category("Business",   R.drawable.ic_briefcase,  R.color.category2));
            categories.add(new Category("Investment", R.drawable.ic_bar_chart,  R.color.category3));
            categories.add(new Category("Loan",       R.drawable.ic_loan,       R.color.category4));
            categories.add(new Category("Rent",       R.drawable.ic_key,        R.color.category5));
            categories.add(new Category("Other",      R.drawable.ic_wallet,     R.color.category6));

            // Setup adapter untuk list kategori di dalam dialog
            CategoryAdapter categoryAdapter = new CategoryAdapter(getContext(), categories, category -> {
                binding.category.setText(category.getCategoryName());
                categoryDialog.dismiss(); // Tutup dialog setelah memilih
            });

            dialogBinding.recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
            dialogBinding.recyclerView.setAdapter(categoryAdapter);
            categoryDialog.show();
        });

        /**
         * Memunculkan Dialog pilihan akun menggunakan AlertDialog standar.
         */
        binding.account.setOnClickListener(c -> {
            String[] accounts = {"Cash", "Bank", "E-Wallet"};
            new AlertDialog.Builder(getContext())
                    .setTitle("Pilih Account")
                    .setItems(accounts, (dialog, which) -> binding.account.setText(accounts[which]))
                    .show();
        });

        // Tombol simpan untuk memproses data ke Firebase
        binding.saveTransactionBtn.setOnClickListener(v -> saveTransaction());

        return binding.getRoot();
    }

    /**
     * Mengambil data dari inputan user, melakukan validasi,
     * dan mengirimkannya ke Firebase Realtime Database.
     */
    private void saveTransaction() {
        String date      = binding.date.getText().toString().trim();
        String amountStr = binding.amount.getText().toString().trim();
        String category  = binding.category.getText().toString().trim();
        String account   = binding.account.getText().toString().trim();
        String note      = binding.note.getText().toString().trim();

        // Validasi: Memastikan field wajib sudah terisi
        if (date.isEmpty()) { Toast.makeText(getContext(), "Enter Date!", Toast.LENGTH_SHORT).show(); return; }
        if (amountStr.isEmpty()) { Toast.makeText(getContext(), "Enter Amount!", Toast.LENGTH_SHORT).show(); return; }
        if (category.isEmpty()) { Toast.makeText(getContext(), "Select Category!", Toast.LENGTH_SHORT).show(); return; }
        if (account.isEmpty()) { Toast.makeText(getContext(), "Select Account!", Toast.LENGTH_SHORT).show(); return; }

        double amount = Double.parseDouble(amountStr);

        // Membuat objek Transaction berdasarkan data input
        Transaction transaction = new Transaction(selectedType, date, amount, category, account, note);

        // Koneksi ke node "transactions" di Firebase
        DatabaseReference dbRef = FirebaseDatabase.getInstance("https://expense-manager-98f10-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("transactions");

        // Membuat push key unik dari Firebase agar data tidak saling menimpa
        String key = dbRef.push().getKey();
        transaction.setId(key);

        // Menyimpan data ke Firebase berdasarkan key tersebut
        dbRef.child(key).setValue(transaction)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "Saved!", Toast.LENGTH_SHORT).show();
                    dismiss(); // Menutup panel BottomSheet setelah berhasil simpan
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}