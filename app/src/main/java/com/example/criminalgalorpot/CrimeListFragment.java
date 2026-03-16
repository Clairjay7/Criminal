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
        View view = null;
        try {
            view = inflater.inflate(R.layout.fragment_crime_list, container, false);
            if (view == null) {
                view = container != null ? new View(container.getContext()) : new android.widget.FrameLayout(inflater.getContext());
                return view;
            }

            android.content.Context ctx = getContext();
            if (ctx == null) ctx = getActivity();
            if (ctx == null) return view;

            recyclerView = view.findViewById(R.id.crime_recycler_view);
            if (recyclerView != null) {
                recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
            }

            List<Crime> crimes = CrimeRepository.getInstance().getCrimes();
            if (crimes == null) crimes = new java.util.ArrayList<>();
            adapter = new CrimeAdapter(crimes, position -> {
            try {
                android.app.Activity activity = getActivity();
                if (activity == null) return;
                java.util.List<Crime> list = CrimeRepository.getInstance().getCrimes();
                if (list == null || position < 0 || position >= list.size()) return;
                Intent intent = CrimeDetailActivity.newIntent(activity, position);
                activity.startActivity(intent);
            } catch (Throwable ignored) {
            }
        });
            if (recyclerView != null) recyclerView.setAdapter(adapter);

            FloatingActionButton fab = view.findViewById(R.id.fab_add_crime);
            if (fab != null) {
                fab.setOnClickListener(v -> {
                    try {
                        android.app.Activity activity = getActivity();
                        if (activity == null) return;
                        Crime newCrime = new Crime("New Crime");
                        CrimeRepository.getInstance().addCrime(newCrime);
                        try { CrimeRepository.getInstance().save(activity); } catch (Throwable ignored) { }
                        java.util.List<Crime> list = CrimeRepository.getInstance().getCrimes();
                        if (list == null || list.isEmpty()) return;
                        Intent intent = CrimeDetailActivity.newIntent(activity, list.size() - 1);
                        activity.startActivity(intent);
                    } catch (Throwable ignored) { }
                });
            }
        } catch (Throwable t) {
            if (view == null && container != null)
                view = inflater.inflate(android.R.layout.simple_list_item_1, container, false);
        }
        return view != null ? view : (container != null ? new View(container.getContext()) : new View(inflater.getContext()));
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
        try {
            if (recyclerView != null && recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
                LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                int position = lm.findFirstVisibleItemPosition();
                View first = recyclerView.getChildCount() > 0 ? recyclerView.getChildAt(0) : null;
                int offset = (first != null) ? first.getTop() : 0;
                outState.putInt(SAVE_SCROLL_POSITION, Math.max(0, position));
                outState.putInt(SAVE_SCROLL_OFFSET, offset);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            if (adapter != null) {
                adapter.setCrimes(CrimeRepository.getInstance().getCrimes());
                adapter.notifyDataSetChanged();
            }
        } catch (Exception ignored) {
        }
    }
}
