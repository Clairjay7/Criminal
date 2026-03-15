package com.example.criminalgalorpot;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.criminalgalorpot.model.Crime;
import com.example.criminalgalorpot.model.CrimeRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class CrimeListFragment extends Fragment {

    private RecyclerView recyclerView;
    private CrimeAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);

        recyclerView = view.findViewById(R.id.crime_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        List<Crime> crimes = CrimeRepository.getInstance().getCrimes();
        adapter = new CrimeAdapter(crimes, crime -> {
            Intent intent = CrimeDetailActivity.newIntent(requireContext(), crime.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = view.findViewById(R.id.fab_add_crime);
        fab.setOnClickListener(v -> {
            Crime newCrime = new Crime("New Crime");
            CrimeRepository.getInstance().addCrime(newCrime);
            adapter.setCrimes(CrimeRepository.getInstance().getCrimes());
            adapter.notifyItemInserted(adapter.getItemCount() - 1);
            Intent intent = CrimeDetailActivity.newIntent(requireContext(), newCrime.getId());
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.setCrimes(CrimeRepository.getInstance().getCrimes());
            adapter.notifyDataSetChanged();
        }
    }
}
