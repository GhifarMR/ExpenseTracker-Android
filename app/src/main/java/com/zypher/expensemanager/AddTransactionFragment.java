package com.zypher.expensemanager;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.zypher.expensemanager.databinding.FragmentAddTransactionBinding;

public class AddTransactionFragment extends BottomSheetDialogFragment {


    public AddTransactionFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    FragmentAddTransactionBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddTransactionBinding.inflate(inflater);

        binding.incomeBtn.setOnClickListener(view -> {
            binding.incomeBtn.setBackgroundResource(R.drawable.income_selector);
            binding.expenseBtn.setBackgroundResource(R.drawable.default_selector);
            binding.incomeBtn.setTextColor(getContext().getColor(R.color.white));
            binding.expenseBtn.setTextColor(getContext().getColor(R.color.black));
        });

        binding.expenseBtn.setOnClickListener(view -> {
            binding.incomeBtn.setBackgroundResource(R.drawable.default_selector);
            binding.expenseBtn.setBackgroundResource(R.drawable.expense_selector);
            binding.incomeBtn.setTextColor(getContext().getColor(R.color.black));
            binding.expenseBtn.setTextColor(getContext().getColor(R.color.white));
        });


        return binding.getRoot();
    }
}