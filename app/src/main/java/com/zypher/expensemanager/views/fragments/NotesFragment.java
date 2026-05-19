package com.zypher.expensemanager.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.zypher.expensemanager.R;
import com.zypher.expensemanager.models.Transaction;

import java.util.ArrayList;

/**
 * NotesFragment berfungsi untuk menampilkan daftar catatan (notes) dari transaksi.
 * Fragment ini membuat tampilan secara dinamis (programmatic) tanpa adapter tambahan.
 */
public class NotesFragment extends Fragment {

    // Kunci untuk mengirim data melalui Bundle
    private static final String ARG_TRANSACTIONS = "transactions";
    private ArrayList<Transaction> transactions;

    /**
     * Factory method untuk membuat instance baru dari NotesFragment.
     * Digunakan untuk mengirim data list transaksi dari Activity ke Fragment.
     */
    public static NotesFragment newInstance(ArrayList<Transaction> transactions) {
        NotesFragment fragment = new NotesFragment();
        Bundle args = new Bundle();
        // Memasukkan data list ke dalam Bundle
        args.putSerializable(ARG_TRANSACTIONS, transactions);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Mengambil data transaksi dari Bundle saat fragment dibuat
        if (getArguments() != null) {
            transactions = (ArrayList<Transaction>) getArguments().getSerializable(ARG_TRANSACTIONS);
        }
        if (transactions == null) transactions = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate layout dasar untuk container catatan
        View view = inflater.inflate(R.layout.fragment_notes, container, false);

        LinearLayout notesContainer = view.findViewById(R.id.notesContainer);
        TextView tvEmpty = view.findViewById(R.id.tvNotesEmpty);

        // Flag untuk mengecek apakah ada transaksi yang memiliki catatan
        boolean hasNotes = false;

        // Iterasi melalui semua transaksi yang dikirim dari MainActivity
        for (Transaction t : transactions) {
            // Hanya proses transaksi yang memiliki catatan (tidak null dan tidak kosong)
            if (t.getNote() != null && !t.getNote().trim().isEmpty()) {
                hasNotes = true;

                /**
                 * MEMBUAT TAMPILAN SECARA PROGRAMMATIC
                 * Bagian ini membuat CardView dan isinya langsung melalui kode Java.
                 */

                // 1. Membuat CardView sebagai container utama catatan
                CardView card = new CardView(requireContext());
                LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                cardParams.setMargins(0, 0, 0, 24); // Memberikan jarak antar card
                card.setLayoutParams(cardParams);
                card.setRadius(16f);
                card.setCardElevation(0f);
                card.setCardBackgroundColor(requireContext().getColor(android.R.color.white));

                // 2. Inner layout (LinearLayout Vertikal) untuk menumpuk teks ke bawah
                LinearLayout inner = new LinearLayout(requireContext());
                inner.setOrientation(LinearLayout.VERTICAL);
                inner.setPadding(40, 32, 40, 32);

                // 3. TextView Header: Menampilkan Kategori dan Tanggal
                TextView tvHeader = new TextView(requireContext());
                tvHeader.setText(t.getCategory() + "  ·  " + t.getDate());
                tvHeader.setTextSize(13f);
                tvHeader.setTextColor(requireContext().getColor(R.color.blue));
                tvHeader.setTypeface(null, android.graphics.Typeface.BOLD);

                // 4. TextView Jumlah: Menampilkan nominal dengan simbol $
                String sign = "Income".equals(t.getType()) ? "+ $" : "- $";
                TextView tvAmount = new TextView(requireContext());
                tvAmount.setText(sign + String.format(java.util.Locale.getDefault(), "%.0f", t.getAmount()));
                tvAmount.setTextSize(13f);

                // Pewarnaan angka: Hijau tua untuk Income, Merah tua untuk Expense
                int color = "Income".equals(t.getType())
                        ? android.graphics.Color.parseColor("#006400")
                        : android.graphics.Color.parseColor("#8B0000");
                tvAmount.setTextColor(color);

                // 5. Divider (Garis Pemisah horizontal)
                View divider = new View(requireContext());
                LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 2);
                divParams.setMargins(0, 16, 0, 16);
                divider.setLayoutParams(divParams);
                divider.setBackgroundColor(android.graphics.Color.parseColor("#EEEEEE"));

                // 6. TextView Note: Menampilkan isi catatan dari user
                TextView tvNote = new TextView(requireContext());
                tvNote.setText(t.getNote());
                tvNote.setTextSize(15f);
                tvNote.setTextColor(android.graphics.Color.parseColor("#333333"));

                // Menyusun komponen (Header -> Amount -> Garis -> Catatan) ke dalam inner layout
                inner.addView(tvHeader);
                inner.addView(tvAmount);
                inner.addView(divider);
                inner.addView(tvNote);

                // Masukkan layout ke dalam CardView
                card.addView(inner);

                // Masukkan CardView ke dalam container utama di XML
                notesContainer.addView(card);
            }
        }

        /**
         * Logika Empty State:
         * Jika tidak ada satupun catatan ditemukan, tampilkan teks "Empty Notes".
         */
        if (!hasNotes) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }

        return view;
    }
}