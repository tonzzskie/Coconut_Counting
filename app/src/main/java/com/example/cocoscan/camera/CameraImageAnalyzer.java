package com.example.cocoscan.camera;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.example.cocoscan.ml.BoundingBox;
import com.example.cocoscan.ml.CentroidTracker;
import com.example.cocoscan.ml.YoloV8Detector;
import com.example.cocoscan.utils.ImageUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class CameraImageAnalyzer implements ImageAnalysis.Analyzer {

    private final YoloV8Detector detector;
    private final CentroidTracker tracker;
    private final DetectionListener listener;
    private final AtomicBoolean isProcessing = new AtomicBoolean(false);

    private float confThreshold = 0.75f;
    private float nmsThreshold = 0.45f;

    public interface DetectionListener {
        void onDetections(List<BoundingBox> boxes, long inferenceTimeMs, int width, int height);
    }

    public CameraImageAnalyzer(YoloV8Detector detector, DetectionListener listener) {
        this.detector = detector;
        this.tracker = new CentroidTracker();
        this.listener = listener;
    }

    public void setThresholds(float conf, float nms) {
        this.confThreshold = conf;
        this.nmsThreshold = nms;
    }

    @Override
    public void analyze(@NonNull ImageProxy image) {
        if (isProcessing.get()) {
            image.close();
            return;
        }

        isProcessing.set(true);
        long startTime = System.currentTimeMillis();

        // 1. Efficient YUV to RGB Conversion
        Bitmap bitmap = ImageUtils.toBitmap(image);
        image.close();

        // 2. Inference
        List<BoundingBox> boxes = detector.detect(bitmap, confThreshold, nmsThreshold);

        // 3. Tracking
        List<BoundingBox> trackedBoxes = tracker.update(boxes);

        long inferenceTime = System.currentTimeMillis() - startTime;

        // 4. Callback
        if (listener != null) {
            listener.onDetections(trackedBoxes, inferenceTime, bitmap.getWidth(), bitmap.getHeight());
        }

        isProcessing.set(false);
    }
}
