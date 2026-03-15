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

    private static final String SAVE_SCROLL_POSITION = "recycler_scroll_position";
    private static final String SAVE_SCROLL_OFFSET = "recycler_scroll_offset";

    private RecyclerView recyclerView;
    private CrimeAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);

        recyclerView = view.findViewById(R.id.crime_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        List<Crime> crimes = CrimeRepository.getInstance().getCrimes();
        adapter = new CrimeAdapter(crimes, position -> {
            if (getContext() == null) return;
            Intent intent = CrimeDetailActivity.newIntent(getContext(), position);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = view.findViewById(R.id.fab_add_crime);
        if (fab != null) {
            fab.setOnClickListener(v -> {
                if (getContext() == null) return;
                Crime newCrime = new Crime("New Crime");
                CrimeRepository.getInstance().addCrime(newCrime);
                int newIndex = CrimeRepository.getInstance().getCrimes().size() - 1;
                Intent intent = CrimeDetailActivity.newIntent(getContext(), newIndex);
                startActivity(intent);
            });
        }

        return view;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null && recyclerView != null) {
            int position = savedInstanceState.getInt(SAVE_SCROLL_POSITION, 0);
            int offset = savedInstanceState.getInt(SAVE_SCROLL_OFFSET, 0);
            if (recyclerView.getLayoutManager() instanceof LinearLayoutManager && position >= 0) {
                recyclerView.post(() -> {
                    ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(position, offset);
                });
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (recyclerView != null && recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
            int position = lm.findFirstVisibleItemPosition();
            View first = recyclerView.getChildAt(0);
            int offset = (first != null) ? first.getTop() : 0;
            outState.putInt(SAVE_SCROLL_POSITION, Math.max(0, position));
            outState.putInt(SAVE_SCROLL_OFFSET, offset);
        }
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
