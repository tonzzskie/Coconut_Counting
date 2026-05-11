package com.example.cocoscan.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cocoscan.R;
import com.example.cocoscan.data.AppDatabase;
import com.example.cocoscan.data.DetectionRecord;
import com.example.cocoscan.data.FileStorageHelper;
import com.google.android.material.button.MaterialButton;

public class SnapshotDetailFragment extends Fragment {

    private Bitmap snapshotBitmap;
    private int totalCount;
    private float avgConfidence;

    public static SnapshotDetailFragment newInstance(Bitmap bitmap, int count, float avgConf) {
        SnapshotDetailFragment fragment = new SnapshotDetailFragment();
        fragment.snapshotBitmap = bitmap;
        fragment.totalCount = count;
        fragment.avgConfidence = avgConf;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_snapshot_detail, container, false);

        ImageView ivSnapshot = view.findViewById(R.id.iv_snapshot);
        if (snapshotBitmap != null) {
            ivSnapshot.setImageBitmap(snapshotBitmap);
        }

        TextView tvTotalCount = view.findViewById(R.id.tv_total_count);
        tvTotalCount.setText(String.valueOf(totalCount));

        TextView tvAvgConf = view.findViewById(R.id.tv_avg_conf);
        tvAvgConf.setText(String.format("%d%%", (int) (avgConfidence * 100)));

        MaterialButton btnSave = view.findViewById(R.id.btn_save);
        btnSave.setOnClickListener(v -> saveToHistory());

        MaterialButton btnDiscard = view.findViewById(R.id.btn_discard);
        btnDiscard.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        return view;
    }

    private void saveToHistory() {
        if (snapshotBitmap == null) return;

        AppDatabase.databaseWriteExecutor.execute(() -> {
            String filename = "scan_" + System.currentTimeMillis();
            String path = FileStorageHelper.saveBitmapToInternalStorage(requireContext(), snapshotBitmap, filename);

            if (path != null) {
                DetectionRecord record = new DetectionRecord(
                        System.currentTimeMillis(),
                        totalCount,
                        avgConfidence,
                        path,
                        "Unknown Location"
                );
                AppDatabase.getDatabase(requireContext()).detectionDao().insert(record);

                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Saved to History", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                });
            } else {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Failed to save image", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
