package com.example.cocoscan.data;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileStorageHelper {

    public static String saveBitmapToInternalStorage(Context context, Bitmap bitmap, String filename) {
        File directory = new File(context.getFilesDir(), "snapshots");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File file = new File(directory, filename + ".jpg");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            // Compress and save as JPEG
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean deleteFile(String absolutePath) {
        if (absolutePath == null) return false;
        File file = new File(absolutePath);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }
}
