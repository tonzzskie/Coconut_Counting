package com.example.cocoscan.ml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CentroidTracker {
    private int nextObjectId = 0;
    private final Map<Integer, float[]> objects = new HashMap<>(); // ID -> [cx, cy]
    private final Map<Integer, Integer> disappeared = new HashMap<>();
    
    private final int maxDisappeared = 15; // Increased for better persistence
    private final float maxDistance = 150.0f; // Increased for larger frames

    public List<BoundingBox> update(List<BoundingBox> rects) {
        if (rects.isEmpty()) {
            for (Integer objectId : new ArrayList<>(objects.keySet())) {
                int count = disappeared.getOrDefault(objectId, 0) + 1;
                disappeared.put(objectId, count);
                if (count > maxDisappeared) {
                    objects.remove(objectId);
                    disappeared.remove(objectId);
                }
            }
            return rects;
        }

        if (objects.isEmpty()) {
            for (BoundingBox b : rects) {
                b.objectId = register(b.cx, b.cy);
            }
        } else {
            List<Integer> objectIds = new ArrayList<>(objects.keySet());
            List<float[]> objectCentroids = new ArrayList<>(objects.values());

            float[][] D = new float[objectIds.size()][rects.size()];
            for (int i = 0; i < objectCentroids.size(); i++) {
                for (int j = 0; j < rects.size(); j++) {
                    float[] c1 = objectCentroids.get(i);
                    BoundingBox c2 = rects.get(j);
                    D[i][j] = (float) Math.hypot(c1[0] - c2.cx, c1[1] - c2.cy);
                }
            }

            boolean[] usedRows = new boolean[D.length];
            boolean[] usedCols = new boolean[D[0].length];

            // Greedy matching
            for (int count = 0; count < Math.min(D.length, D[0].length); count++) {
                float min = Float.MAX_VALUE;
                int r = -1, c = -1;
                for (int i = 0; i < D.length; i++) {
                    if (usedRows[i]) continue;
                    for (int j = 0; j < D[0].length; j++) {
                        if (usedCols[j]) continue;
                        if (D[i][j] < min) {
                            min = D[i][j];
                            r = i;
                            c = j;
                        }
                    }
                }

                if (r == -1 || c == -1) break;
                
                if (min > maxDistance) {
                    break;
                }

                usedRows[r] = true;
                usedCols[c] = true;
                
                int objectId = objectIds.get(r);
                objects.put(objectId, new float[]{rects.get(c).cx, rects.get(c).cy});
                disappeared.put(objectId, 0);
                rects.get(c).objectId = objectId;
            }

            for (int i = 0; i < usedRows.length; i++) {
                if (!usedRows[i]) {
                    int objectId = objectIds.get(i);
                    int dCount = disappeared.getOrDefault(objectId, 0) + 1;
                    disappeared.put(objectId, dCount);
                    if (dCount > maxDisappeared) {
                        objects.remove(objectId);
                        disappeared.remove(objectId);
                    }
                }
            }

            for (int i = 0; i < usedCols.length; i++) {
                if (!usedCols[i]) {
                    BoundingBox b = rects.get(i);
                    b.objectId = register(b.cx, b.cy);
                }
            }
        }
        return rects;
    }

    private int register(float cx, float cy) {
        int id = nextObjectId++;
        objects.put(id, new float[]{cx, cy});
        disappeared.put(id, 0);
        return id;
    }
}
