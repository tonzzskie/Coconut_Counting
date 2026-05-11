package com.example.cocoscan.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "detections")
public class DetectionRecord {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public long timestamp;
    public int totalCoconuts;
    public float averageConfidence;
    public String imagePath; // Local path to the compressed image
    public String location; // Placeholder for location metadata

    public DetectionRecord(long timestamp, int totalCoconuts, float averageConfidence, String imagePath, String location) {
        this.timestamp = timestamp;
        this.totalCoconuts = totalCoconuts;
        this.averageConfidence = averageConfidence;
        this.imagePath = imagePath;
        this.location = location;
    }
}
