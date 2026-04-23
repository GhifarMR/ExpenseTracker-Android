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

public class AccountsFragment extends Fragment {

    TextView tvCash, tvBank, tvEwallet;
    DatabaseReference dbRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_accounts, container, false);

        tvCash    = view.findViewById(R.id.accountCash);
        tvBank    = view.findViewById(R.id.accountBank);
        tvEwallet = view.findViewById(R.id.accountEwallet);

        dbRef = FirebaseDatabase.getInstance(
                "https://expense-manager-98f10-default-rtdb.asia-southeast1.firebasedatabase.app"
        ).getReference("transactions");

        loadAccounts();

        return view;
    }

    private void loadAccounts() {
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                double cash = 0, bank = 0, ewallet = 0;

                for (DataSnapshot data : snapshot.getChildren()) {
                    Transaction t = data.getValue(Transaction.class);
                    if (t == null || t.getAccount() == null) continue;

                    double amount = "Income".equals(t.getType()) ? t.getAmount() : -t.getAmount();

                    switch (t.getAccount()) {
                        case "Cash":     cash += amount; break;
                        case "Bank":     bank += amount; break;
                        case "E-Wallet": ewallet += amount; break;
                    }
                }

                tvCash.setText("Cash: Rp " + (long) cash);
                tvBank.setText("Bank: Rp " + (long) bank);
                tvEwallet.setText("E-Wallet: Rp " + (long) ewallet);
            }

            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }
}
