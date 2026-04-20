package com.zypher.expensemanager.views.fragments;

import android.app.AlertDialog;import android.app.DatePickerDialog;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.GridLayoutManager;import androidx.recyclerview.widget.LinearLayoutManager;import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.zypher.expensemanager.R;
import com.zypher.expensemanager.adapters.CategoryAdapter;import com.zypher.expensemanager.databinding.FragmentAddTransactionBinding;import com.zypher.expensemanager.databinding.ListDialogBinding;import com.zypher.expensemanager.models.Category;

import java.text.SimpleDateFormat;
import java.util.ArrayList;import java.util.Calendar;

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

        binding.date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext());
                datePickerDialog.setOnDateSetListener((datePicker, year, month, day) -> {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
                    calendar.set(Calendar.MONTH, datePicker.getMonth());
                    calendar.set(Calendar.YEAR, datePicker.getYear());

                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM, yyyy");
                    String dateToShow = dateFormat.format(calendar.getTime());

                    binding.date.setText(dateToShow);
                });

                datePickerDialog.show();

            }
        });

        binding.category.setOnClickListener(c -> {
            ListDialogBinding dialogBinding = ListDialogBinding.inflate(inflater);
            AlertDialog categoryDialog = new AlertDialog.Builder(getContext()).create();
            categoryDialog.setView(dialogBinding.getRoot());

            ArrayList<Category> categories = new ArrayList<>();
            categories.add(new Category("Salary", R.drawable.ic_income, R.color.category1));
            categories.add(new Category("Business", R.drawable.ic_briefcase, R.color.category2));
            categories.add(new Category("Investment", R.drawable.ic_bar_chart, R.color.category3));
            categories.add(new Category("Loan", R.drawable.ic_loan, R.color.category4));
            categories.add(new Category("Rent", R.drawable.ic_key, R.color.category5));
            categories.add(new Category("Other", R.drawable.ic_wallet, R.color.category6));

            CategoryAdapter categoryAdapter = new CategoryAdapter(getContext(), categories, new CategoryAdapter.CategoryClickListener() {
            @Override
                public void onCategoryClicked(Category category) {
                    binding.category.setText(category.getCategoryName());
                    categoryDialog.dismiss();
                }});

            dialogBinding.recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
            dialogBinding.recyclerView.setAdapter(categoryAdapter);

            categoryDialog.show();
        });


        return binding.getRoot();
    }
}