package com.example.cocoscan.ui;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.cocoscan.R;
import com.example.cocoscan.camera.CameraImageAnalyzer;
import com.example.cocoscan.ml.YoloV8Detector;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LiveDetectionFragment extends Fragment {

    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA};

    private PreviewView viewFinder;
    private OverlayView overlayView;
    private TextView tvCoconutCount;
    private TextView tvFps;
    private TextView tvInferenceTime;

    private ExecutorService cameraExecutor;
    private YoloV8Detector detector;
    private CameraImageAnalyzer analyzer;

    private int currentCount = 0;
    private float currentAvgConf = 0f;
    private Bitmap currentBitmap = null;
    private final java.util.Set<Integer> uniqueObjectIds = new java.util.HashSet<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_live_detection, container, false);
        viewFinder = view.findViewById(R.id.view_finder);
        overlayView = view.findViewById(R.id.overlay_view);
        tvCoconutCount = view.findViewById(R.id.tv_coconut_count);
        tvFps = view.findViewById(R.id.tv_fps);
        tvInferenceTime = view.findViewById(R.id.tv_inference_time);

        // Reset set when fragment starts
        uniqueObjectIds.clear();

        FloatingActionButton btnCapture = view.findViewById(R.id.btn_capture);
        btnCapture.setOnClickListener(v -> takeSnapshot());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        cameraExecutor = Executors.newSingleThreadExecutor();

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    private void startCamera() {
        try {
            SharedPreferences prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
            boolean useGpu = prefs.getBoolean("gpu_accel", false);
            float conf = prefs.getFloat("conf_thresh", 0.40f); // Lowered for "extensive" finding
            float nms = prefs.getFloat("nms_thresh", 0.45f);

            detector = new YoloV8Detector(requireContext(), "yolov11_int8.tflite", useGpu);
            analyzer = new CameraImageAnalyzer(detector, (boxes, inferenceTimeMs, width, height) -> {
                if (getActivity() == null) return;
                
                int count = boxes.size();
                float totalConf = 0;
                for (com.example.cocoscan.ml.BoundingBox b : boxes) {
                    totalConf += b.confidence;
                    uniqueObjectIds.add(b.objectId);
                }
                float avgConf = count > 0 ? totalConf / count : 0;
                int totalCount = uniqueObjectIds.size();
                
                getActivity().runOnUiThread(() -> {
                    overlayView.setResults(boxes, width, height);
                    tvCoconutCount.setText(String.valueOf(totalCount));
                    tvInferenceTime.setText("Inference: " + inferenceTimeMs + "ms");
                    if (inferenceTimeMs > 0) {
                        tvFps.setText("FPS: " + (1000 / inferenceTimeMs));
                    }
                    
                    currentCount = totalCount; // Using total count for snapshots
                    currentAvgConf = avgConf;
                });
            });
            analyzer.setThresholds(conf, nms);

            ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
            cameraProviderFuture.addListener(() -> {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                    Preview preview = new Preview.Builder().build();
                    preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                    ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build();

                    imageAnalysis.setAnalyzer(cameraExecutor, analyzer);

                    CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                    cameraProvider.unbindAll();
                    cameraProvider.bindToLifecycle(getViewLifecycleOwner(), cameraSelector, preview, imageAnalysis);

                } catch (ExecutionException | InterruptedException e) {
                    Toast.makeText(getContext(), "Camera init failed.", Toast.LENGTH_SHORT).show();
                }
            }, ContextCompat.getMainExecutor(requireContext()));

        } catch (IOException e) {
            Toast.makeText(getContext(), "Failed to load model. Is yolov11_int8.tflite in assets?", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void takeSnapshot() {
        Bitmap bitmap = viewFinder.getBitmap();
        if (bitmap != null) {
            SnapshotDetailFragment fragment = SnapshotDetailFragment.newInstance(bitmap, currentCount, currentAvgConf);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.nav_host_fragment, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(requireContext(), "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
        if (detector != null) {
            detector.close();
        }
    }
}
