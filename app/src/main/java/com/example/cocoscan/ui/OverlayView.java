package com.example.cocoscan.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.example.cocoscan.ml.BoundingBox;

import java.util.ArrayList;
import java.util.List;

public class OverlayView extends View {

    private List<BoundingBox> boxes = new ArrayList<>();
    private Paint boxPaint;
    private Paint textBackgroundPaint;
    private Paint textPaint;
    
    private int frameWidth = 1;
    private int frameHeight = 1;

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaints();
    }

    private void initPaints() {
        boxPaint = new Paint();
        boxPaint.setColor(Color.parseColor("#00E676")); // Vibrant green for visibility
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(8.0f);
        boxPaint.setAntiAlias(true);

        textBackgroundPaint = new Paint();
        textBackgroundPaint.setColor(Color.parseColor("#00E676"));
        textBackgroundPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(40.0f);
        textPaint.setFakeBoldText(true);
        textPaint.setAntiAlias(true);
    }

    public void setResults(List<BoundingBox> detections, int frameWidth, int frameHeight) {
        this.boxes = detections;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (boxes == null || frameWidth == 0 || frameHeight == 0) return;

        float scaleX = (float) getWidth() / frameWidth;
        float scaleY = (float) getHeight() / frameHeight;

        for (BoundingBox box : boxes) {
            float left = box.x1 * scaleX;
            float top = box.y1 * scaleY;
            float right = box.x2 * scaleX;
            float bottom = box.y2 * scaleY;

            canvas.drawRect(left, top, right, bottom, boxPaint);

            String[] labels = {"Coconut", "Mature", "Premature"};
            String className = box.classId < labels.length ? labels[box.classId] : "Coconut";
            String label = String.format("#%d %s %d%%", box.objectId, className, (int) (box.confidence * 100));
            
            float textWidth = textPaint.measureText(label);
            float textHeight = textPaint.getTextSize();

            canvas.drawRect(left, top - textHeight - 10, left + textWidth + 20, top, textBackgroundPaint);
            canvas.drawText(label, left + 10, top - 10, textPaint);
        }
    }
}
