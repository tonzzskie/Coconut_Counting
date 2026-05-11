package com.example.cocoscan.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.cocoscan.R;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsFragment extends Fragment {

    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE);

        Slider sliderConf = view.findViewById(R.id.slider_confidence);
        TextView tvConfVal = view.findViewById(R.id.tv_conf_val);

        Slider sliderNms = view.findViewById(R.id.slider_nms);
        TextView tvNmsVal = view.findViewById(R.id.tv_nms_val);

        SwitchMaterial switchGpu = view.findViewById(R.id.switch_gpu);
        SwitchMaterial switchPerf = view.findViewById(R.id.switch_perf_mode);

        // Load saved values
        float savedConf = prefs.getFloat("conf_thresh", 0.75f);
        float savedNms = prefs.getFloat("nms_thresh", 0.45f);
        boolean savedGpu = prefs.getBoolean("gpu_accel", false);
        boolean savedPerf = prefs.getBoolean("perf_mode", false);

        sliderConf.setValue(savedConf);
        tvConfVal.setText(String.format("%.2f", savedConf));

        sliderNms.setValue(savedNms);
        tvNmsVal.setText(String.format("%.2f", savedNms));

        switchGpu.setChecked(savedGpu);
        switchPerf.setChecked(savedPerf);

        // Listeners
        sliderConf.addOnChangeListener((slider, value, fromUser) -> {
            tvConfVal.setText(String.format("%.2f", value));
            prefs.edit().putFloat("conf_thresh", value).apply();
        });

        sliderNms.addOnChangeListener((slider, value, fromUser) -> {
            tvNmsVal.setText(String.format("%.2f", value));
            prefs.edit().putFloat("nms_thresh", value).apply();
        });

        switchGpu.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("gpu_accel", isChecked).apply();
        });

        switchPerf.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("perf_mode", isChecked).apply();
        });

        return view;
    }
}
