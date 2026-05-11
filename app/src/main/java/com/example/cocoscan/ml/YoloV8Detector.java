package com.example.cocoscan.ml;

import android.content.Context;
import android.graphics.Bitmap;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.CompatibilityList;
import org.tensorflow.lite.gpu.GpuDelegate;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class YoloV8Detector {

    private Interpreter interpreter;
    private int inputWidth;
    private int inputHeight;
    private int numElements;
    private int numChannels;
    private boolean isGpuDelegateEnabled = false;

    // Output tensor shape
    private int[] outputShape;

    public YoloV8Detector(Context context, String modelPath, boolean useGpu) throws IOException {
        Interpreter.Options options = new Interpreter.Options();

        if (useGpu) {
            try {
                CompatibilityList compatList = new CompatibilityList();
                if (compatList.isDelegateSupportedOnThisDevice()) {
                    GpuDelegate.Options delegateOptions = compatList.getBestOptionsForThisDevice();
                    GpuDelegate gpuDelegate = new GpuDelegate(delegateOptions);
                    options.addDelegate(gpuDelegate);
                    isGpuDelegateEnabled = true;
                } else {
                    options.setNumThreads(4);
                }
            } catch (Exception e) {
                options.setNumThreads(4);
                isGpuDelegateEnabled = false;
            }
        } else {
            options.setNumThreads(4);
        }

        MappedByteBuffer tfliteModel = FileUtil.loadMappedFile(context, modelPath);
        interpreter = new Interpreter(tfliteModel, options);

        // Get Input shape and type
        org.tensorflow.lite.Tensor inputTensor = interpreter.getInputTensor(0);
        int[] inputShape = inputTensor.shape();
        
        // Handle different input shape formats and dynamic shapes
        if (inputShape.length == 4) {
            // NHWC: [Batch, Height, Width, Channels]
            inputHeight = inputShape[1] > 0 ? inputShape[1] : 320;
            inputWidth = inputShape[2] > 0 ? inputShape[2] : 320;
        } else if (inputShape.length == 3) {
            // HWC: [Height, Width, Channels]
            inputHeight = inputShape[0] > 0 ? inputShape[0] : 320;
            inputWidth = inputShape[1] > 0 ? inputShape[1] : 320;
        } else {
            // Fallback
            inputHeight = 320;
            inputWidth = 320;
        }
        
        DataType inputDataType = inputTensor.dataType();

        // Get Output shape and type
        org.tensorflow.lite.Tensor outputTensor = interpreter.getOutputTensor(0);
        outputShape = outputTensor.shape();
        DataType outputDataType = outputTensor.dataType();

        // Safe extraction of numElements and numChannels
        if (outputShape.length >= 2) {
            int lastDim = outputShape[outputShape.length - 1];
            int secondLastDim = outputShape[outputShape.length - 2];
            
            // YOLOv8/v11 usually have one dimension as 8400 (elements) and one as 84 (channels)
            if (secondLastDim > lastDim) {
                numElements = secondLastDim;
                numChannels = lastDim;
            } else {
                numElements = lastDim;
                numChannels = secondLastDim;
            }
        } else {
            numElements = 0;
            numChannels = 0;
        }
    }

    public List<BoundingBox> detect(Bitmap bitmap, float confThreshold, float iouThreshold) {
        if (interpreter == null || numElements <= 0 || numChannels <= 0) return new ArrayList<>();

        // 1. Preprocessing: Scaling & Letterboxing
        float scale = Math.min((float) inputWidth / bitmap.getWidth(), (float) inputHeight / bitmap.getHeight());
        float padX = (inputWidth - bitmap.getWidth() * scale) / 2f;
        float padY = (inputHeight - bitmap.getHeight() * scale) / 2f;

        // Ensure dimensions are valid
        int finalInputWidth = Math.max(inputWidth, 1);
        int finalInputHeight = Math.max(inputHeight, 1);

        Bitmap letterboxed = Bitmap.createBitmap(finalInputWidth, finalInputHeight, Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(letterboxed);
        canvas.drawColor(android.graphics.Color.rgb(114, 114, 114)); // Standard YOLO gray padding
        
        int newW = Math.round(bitmap.getWidth() * scale);
        int newH = Math.round(bitmap.getHeight() * scale);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, newW, newH, true);
        canvas.drawBitmap(scaledBitmap, padX, padY, null);

        org.tensorflow.lite.Tensor inputTensor = interpreter.getInputTensor(0);
        DataType inputDataType = inputTensor.dataType();
        
        // 2. Normalization (0.0 - 1.0)
        ImageProcessor.Builder imageProcessorBuilder = new ImageProcessor.Builder()
                .add(new NormalizeOp(0, 255.0f));

        if (inputDataType == DataType.INT8 || inputDataType == DataType.UINT8) {
            float inputScale = inputTensor.quantizationParams().getScale();
            int inputZeroPoint = inputTensor.quantizationParams().getZeroPoint();
            imageProcessorBuilder.add(new org.tensorflow.lite.support.common.ops.QuantizeOp(inputZeroPoint, inputScale));
        }

        ImageProcessor imageProcessor = imageProcessorBuilder.build();
        TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
        tensorImage.load(letterboxed);
        tensorImage = imageProcessor.process(tensorImage);

        // 3. Inference
        org.tensorflow.lite.Tensor outputTensor = interpreter.getOutputTensor(0);
        DataType outputDataType = outputTensor.dataType();
        TensorBuffer outputBuffer = TensorBuffer.createFixedSize(outputShape, outputDataType);
        
        interpreter.run(tensorImage.getBuffer(), outputBuffer.getBuffer().rewind());

        // 4. Parsing the Output (Dequantize if needed)
        if (outputDataType == DataType.INT8 || outputDataType == DataType.UINT8) {
            float outputScale = outputTensor.quantizationParams().getScale();
            int outputZeroPoint = outputTensor.quantizationParams().getZeroPoint();
            org.tensorflow.lite.support.common.TensorProcessor tensorProcessor = new org.tensorflow.lite.support.common.TensorProcessor.Builder()
                    .add(new org.tensorflow.lite.support.common.ops.DequantizeOp(outputZeroPoint, outputScale))
                    .build();
            outputBuffer = tensorProcessor.process(outputBuffer);
        }

        float[] outputArray = outputBuffer.getFloatArray();
        return postprocess(outputArray, confThreshold, iouThreshold, scale, padX, padY);
    }

    private List<BoundingBox> postprocess(float[] array, float confThreshold, float iouThreshold, float scale, float padX, float padY) {
        List<BoundingBox> boxes = new ArrayList<>();
        boolean isTransposed = outputShape[1] > outputShape[2];

        for (int i = 0; i < numElements; i++) {
            float maxConf = -1.0f;
            int maxClassId = -1;
            float cx = 0, cy = 0, w = 0, h = 0;

            if (isTransposed) {
                int baseIdx = i * numChannels;
                cx = array[baseIdx];
                cy = array[baseIdx + 1];
                w = array[baseIdx + 2];
                h = array[baseIdx + 3];

                for (int c = 4; c < numChannels; c++) {
                    float conf = array[baseIdx + c];
                    if (conf > maxConf) {
                        maxConf = conf;
                        maxClassId = c - 4;
                    }
                }
            } else {
                cx = array[0 * numElements + i];
                cy = array[1 * numElements + i];
                w = array[2 * numElements + i];
                h = array[3 * numElements + i];

                for (int c = 4; c < numChannels; c++) {
                    float conf = array[c * numElements + i];
                    if (conf > maxConf) {
                        maxConf = conf;
                        maxClassId = c - 4;
                    }
                }
            }

            if (maxConf >= confThreshold) {
                // 5. Bounding Box Calculation (Padding Removal & Rescaling)
                float x1 = (cx - w / 2f - padX) / scale;
                float y1 = (cy - h / 2f - padY) / scale;
                float x2 = (cx + w / 2f - padX) / scale;
                float y2 = (cy + h / 2f - padY) / scale;

                boxes.add(new BoundingBox(
                        x1, y1, x2, y2,
                        (cx - padX) / scale, (cy - padY) / scale, w / scale, h / scale,
                        maxConf, maxClassId
                ));
            }
        }

        // 6. Post-Processing (NMS)
        return applyNMS(boxes, iouThreshold);
    }

    private List<BoundingBox> applyNMS(List<BoundingBox> boxes, float iouThreshold) {
        List<BoundingBox> result = new ArrayList<>();
        Collections.sort(boxes, (b1, b2) -> Float.compare(b2.confidence, b1.confidence));

        boolean[] active = new boolean[boxes.size()];
        for (int i = 0; i < active.length; i++) active[i] = true;

        for (int i = 0; i < boxes.size(); i++) {
            if (!active[i]) continue;
            BoundingBox box1 = boxes.get(i);
            result.add(box1);

            for (int j = i + 1; j < boxes.size(); j++) {
                if (!active[j]) continue;
                BoundingBox box2 = boxes.get(j);

                float iou = computeIOU(box1, box2);
                if (iou > iouThreshold) {
                    active[j] = false;
                }
            }
        }
        return result;
    }

    private float computeIOU(BoundingBox b1, BoundingBox b2) {
        float x1 = Math.max(b1.x1, b2.x1);
        float y1 = Math.max(b1.y1, b2.y1);
        float x2 = Math.min(b1.x2, b2.x2);
        float y2 = Math.min(b1.y2, b2.y2);

        float w = Math.max(0, x2 - x1);
        float h = Math.max(0, y2 - y1);
        float interArea = w * h;

        float area1 = b1.w * b1.h;
        float area2 = b2.w * b2.h;

        return interArea / (area1 + area2 - interArea);
    }

    public void close() {
        if (interpreter != null) {
            interpreter.close();
            interpreter = null;
        }
    }
}
