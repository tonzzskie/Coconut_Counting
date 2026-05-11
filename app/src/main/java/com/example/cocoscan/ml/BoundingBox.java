package com.example.cocoscan.ml;

public class BoundingBox {
    public float x1, y1, x2, y2;
    public float cx, cy;
    public float w, h;
    public float confidence;
    public int classId;
    public int objectId = -1; // -1 if unassigned

    public BoundingBox(float x1, float y1, float x2, float y2, float cx, float cy, float w, float h, float confidence, int classId) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.cx = cx;
        this.cy = cy;
        this.w = w;
        this.h = h;
        this.confidence = confidence;
        this.classId = classId;
    }
}
