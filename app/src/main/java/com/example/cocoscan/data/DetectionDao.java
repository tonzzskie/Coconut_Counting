package com.example.cocoscan.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DetectionDao {
    @Insert
    long insert(DetectionRecord record);

    @Query("SELECT * FROM detections ORDER BY timestamp DESC")
    LiveData<List<DetectionRecord>> getAllDetections();

    @Delete
    void delete(DetectionRecord record);

    @Query("DELETE FROM detections")
    void deleteAll();
}
