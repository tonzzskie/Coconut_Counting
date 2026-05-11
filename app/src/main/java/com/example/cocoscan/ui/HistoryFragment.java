package com.example.cocoscan.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cocoscan.R;
import com.example.cocoscan.data.AppDatabase;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private View emptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detection_history, container, false);

        recyclerView = view.findViewById(R.id.recycler_history);
        emptyState = view.findViewById(R.id.empty_state);

        adapter = new HistoryAdapter(requireContext());
        recyclerView.setAdapter(adapter);

        AppDatabase.getDatabase(requireContext()).detectionDao().getAllDetections().observe(getViewLifecycleOwner(), records -> {
            adapter.setRecords(records);
            if (records.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyState.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyState.setVisibility(View.GONE);
            }
        });

        return view;
    }
}
