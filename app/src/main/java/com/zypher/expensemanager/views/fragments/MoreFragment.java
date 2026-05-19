package com.zypher.expensemanager.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.zypher.expensemanager.R;

/**
 * MoreFragment adalah fragment sederhana yang berfungsi sebagai placeholder
 * untuk menu tambahan atau pengaturan aplikasi.
 */
public class MoreFragment extends Fragment {

    /**
     * Method ini digunakan untuk membuat dan menampilkan tampilan (UI) fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate layout fragment_more: Mengubah file XML layout menjadi tampilan visual di layar
        return inflater.inflate(R.layout.fragment_more, container, false);
    }
}