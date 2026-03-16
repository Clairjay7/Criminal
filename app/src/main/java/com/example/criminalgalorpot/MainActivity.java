package com.example.criminalgalorpot;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onPause() {
        super.onPause();
        try {
            com.example.criminalgalorpot.model.CrimeRepository.getInstance().save(this);
        } catch (Throwable ignored) {
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main);
            try {
                com.example.criminalgalorpot.model.CrimeRepository.getInstance().load(this);
            } catch (Throwable ignored) {
            }
            if (savedInstanceState == null) {
                FragmentManager fm = getSupportFragmentManager();
                if (fm != null) {
                    fm.beginTransaction()
                            .add(R.id.fragment_container, new CrimeListFragment())
                            .commit();
                }
            }
        } catch (Throwable t) {
            try {
                setContentView(android.R.layout.simple_list_item_1);
            } catch (Throwable ignored) {
            }
        }
    }
}
