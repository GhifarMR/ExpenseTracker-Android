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

public class NotesFragment extends Fragment {

    private static final String ARG_TRANSACTIONS = "transactions";
    private ArrayList<Transaction> transactions;

    public static NotesFragment newInstance(ArrayList<Transaction> transactions) {
        NotesFragment fragment = new NotesFragment();
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
        View view = inflater.inflate(R.layout.fragment_notes, container, false);

        LinearLayout notesContainer = view.findViewById(R.id.notesContainer);
        TextView tvEmpty = view.findViewById(R.id.tvNotesEmpty);

        // Filter transaksi yang punya notes
        boolean hasNotes = false;
        for (Transaction t : transactions) {
            if (t.getNote() != null && !t.getNote().trim().isEmpty()) {
                hasNotes = true;

                // Buat CardView secara programmatic
                CardView card = new CardView(requireContext());
                LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                cardParams.setMargins(0, 0, 0, 24);
                card.setLayoutParams(cardParams);
                card.setRadius(16f);
                card.setCardElevation(0f);
                card.setCardBackgroundColor(requireContext().getColor(android.R.color.white));

                // Inner layout
                LinearLayout inner = new LinearLayout(requireContext());
                inner.setOrientation(LinearLayout.VERTICAL);
                inner.setPadding(40, 32, 40, 32);

                // Header: kategori + tanggal
                TextView tvHeader = new TextView(requireContext());
                tvHeader.setText(t.getCategory() + "  ·  " + t.getDate());
                tvHeader.setTextSize(13f);
                tvHeader.setTextColor(requireContext().getColor(R.color.blue));
                tvHeader.setTypeface(null, android.graphics.Typeface.BOLD);

                // Jumlah
                String sign = "Income".equals(t.getType()) ? "+ Rp " : "- Rp ";
                TextView tvAmount = new TextView(requireContext());
                tvAmount.setText(sign + String.format(java.util.Locale.getDefault(), "%.0f", t.getAmount()));
                tvAmount.setTextSize(13f);
                int color = "Income".equals(t.getType())
                        ? android.graphics.Color.parseColor("#006400")
                        : android.graphics.Color.parseColor("#8B0000");
                tvAmount.setTextColor(color);

                // Divider
                View divider = new View(requireContext());
                LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 2);
                divParams.setMargins(0, 16, 0, 16);
                divider.setLayoutParams(divParams);
                divider.setBackgroundColor(android.graphics.Color.parseColor("#EEEEEE"));

                // Note text
                TextView tvNote = new TextView(requireContext());
                tvNote.setText(t.getNote());
                tvNote.setTextSize(15f);
                tvNote.setTextColor(android.graphics.Color.parseColor("#333333"));

                inner.addView(tvHeader);
                inner.addView(tvAmount);
                inner.addView(divider);
                inner.addView(tvNote);
                card.addView(inner);
                notesContainer.addView(card);
            }
        }

        if (!hasNotes) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }

        return view;
    }
}
