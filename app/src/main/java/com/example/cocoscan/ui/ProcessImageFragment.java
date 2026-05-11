package com.example.cocoscan.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.cocoscan.R;
import com.example.cocoscan.ml.BoundingBox;
import com.example.cocoscan.ml.YoloV8Detector;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProcessImageFragment extends Fragment {

    private ImageView ivStaticImage;
    private TextView tvCoconutCount;
    private MaterialButton btnTakePicture;
    private MaterialButton btnGallery;

    private YoloV8Detector detector;
    private ExecutorService executorService;
    private Uri currentPhotoUri;

    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_process_image, container, false);

        ivStaticImage = view.findViewById(R.id.iv_static_image);
        tvCoconutCount = view.findViewById(R.id.tv_coconut_count);
        btnTakePicture = view.findViewById(R.id.btn_take_picture);
        btnGallery = view.findViewById(R.id.btn_gallery);

        executorService = Executors.newSingleThreadExecutor();

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                processUri(uri);
            }
        });

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
            if (success && currentPhotoUri != null) {
                processUri(currentPhotoUri);
            }
        });

        btnGallery.setOnClickListener(v -> galleryLauncher.launch("image/*"));

        btnTakePicture.setOnClickListener(v -> {
            File photoFile = new File(requireContext().getCacheDir(), "camera_capture_" + System.currentTimeMillis() + ".jpg");
            currentPhotoUri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".fileprovider", photoFile);
            cameraLauncher.launch(currentPhotoUri);
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            SharedPreferences prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
            boolean useGpu = prefs.getBoolean("gpu_accel", false);
            detector = new YoloV8Detector(requireContext(), "yolov11_int8.tflite", useGpu);
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Failed to load model", Toast.LENGTH_SHORT).show();
        }
    }

    private void processUri(Uri uri) {
        try {
            InputStream is = requireContext().getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            if (bitmap != null) {
                // Ensure bitmap is mutable and in an acceptable format (ARGB_8888)
                Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                if(mutableBitmap == null) mutableBitmap = bitmap; // fallback

                ivStaticImage.setImageBitmap(mutableBitmap);
                tvCoconutCount.setText("Processing...");

                Bitmap finalBitmap = mutableBitmap;
                executorService.execute(() -> {
                    SharedPreferences prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
                    float conf = prefs.getFloat("conf_thresh", 0.40f); // Lowered for extensive detection
                    float nms = prefs.getFloat("nms_thresh", 0.45f);

                    List<BoundingBox> results = detector.detect(finalBitmap, conf, nms);

                    // Draw on bitmap
                    Canvas canvas = new Canvas(finalBitmap);
                    Paint boxPaint = new Paint();
                    boxPaint.setColor(Color.parseColor("#00E676")); // Matching vibrant green
                    boxPaint.setStyle(Paint.Style.STROKE);
                    boxPaint.setStrokeWidth(Math.max(8.0f, finalBitmap.getWidth() / 150f));
                    boxPaint.setAntiAlias(true);

                    Paint textBgPaint = new Paint();
                    textBgPaint.setColor(Color.parseColor("#00E676"));
                    textBgPaint.setStyle(Paint.Style.FILL);

                    Paint textPaint = new Paint();
                    textPaint.setColor(Color.BLACK);
                    textPaint.setTextSize(Math.max(40.0f, finalBitmap.getWidth() / 30f));
                    textPaint.setFakeBoldText(true);
                    textPaint.setAntiAlias(true);

                    String[] labels = {"Coconut", "Mature", "Premature"};
                    for (BoundingBox box : results) {
                        RectF rect = new RectF(box.x1, box.y1, box.x2, box.y2);
                        canvas.drawRect(rect, boxPaint);

                        String className = box.classId < labels.length ? labels[box.classId] : "Coconut";
                        String label = String.format("%s %d%%", className, (int) (box.confidence * 100));
                        float textWidth = textPaint.measureText(label);
                        float textHeight = textPaint.getTextSize();

                        RectF bgRect = new RectF(box.x1, box.y1 - textHeight - 10, box.x1 + textWidth + 20, box.y1);
                        canvas.drawRect(bgRect, textBgPaint);
                        canvas.drawText(label, box.x1 + 10, box.y1 - 10, textPaint);
                    }

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            ivStaticImage.setImageBitmap(finalBitmap);
                            tvCoconutCount.setText("Coconuts: " + results.size());
                        });
                    }
                });
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (detector != null) {
            detector.close();
        }
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
