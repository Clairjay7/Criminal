package com.example.criminalgalorpot;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction()
                    .add(R.id.fragment_container, new CrimeListFragment())
                    .commit();
        }
    }
}
