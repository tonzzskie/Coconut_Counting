package com.example.cocoscan.data;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class DetectionDao_Impl implements DetectionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<DetectionRecord> __insertionAdapterOfDetectionRecord;

  private final EntityDeletionOrUpdateAdapter<DetectionRecord> __deletionAdapterOfDetectionRecord;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public DetectionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfDetectionRecord = new EntityInsertionAdapter<DetectionRecord>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `detections` (`id`,`timestamp`,`totalCoconuts`,`averageConfidence`,`imagePath`,`location`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final DetectionRecord entity) {
        statement.bindLong(1, entity.id);
        statement.bindLong(2, entity.timestamp);
        statement.bindLong(3, entity.totalCoconuts);
        statement.bindDouble(4, entity.averageConfidence);
        if (entity.imagePath == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.imagePath);
        }
        if (entity.location == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.location);
        }
      }
    };
    this.__deletionAdapterOfDetectionRecord = new EntityDeletionOrUpdateAdapter<DetectionRecord>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `detections` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final DetectionRecord entity) {
        statement.bindLong(1, entity.id);
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM detections";
        return _query;
      }
    };
  }

  @Override
  public long insert(final DetectionRecord record) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfDetectionRecord.insertAndReturnId(record);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void delete(final DetectionRecord record) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfDetectionRecord.handle(record);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteAll() {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteAll.release(_stmt);
    }
  }

  @Override
  public LiveData<List<DetectionRecord>> getAllDetections() {
    final String _sql = "SELECT * FROM detections ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"detections"}, false, new Callable<List<DetectionRecord>>() {
      @Override
      @Nullable
      public List<DetectionRecord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfTotalCoconuts = CursorUtil.getColumnIndexOrThrow(_cursor, "totalCoconuts");
          final int _cursorIndexOfAverageConfidence = CursorUtil.getColumnIndexOrThrow(_cursor, "averageConfidence");
          final int _cursorIndexOfImagePath = CursorUtil.getColumnIndexOrThrow(_cursor, "imagePath");
          final int _cursorIndexOfLocation = CursorUtil.getColumnIndexOrThrow(_cursor, "location");
          final List<DetectionRecord> _result = new ArrayList<DetectionRecord>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DetectionRecord _item;
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final int _tmpTotalCoconuts;
            _tmpTotalCoconuts = _cursor.getInt(_cursorIndexOfTotalCoconuts);
            final float _tmpAverageConfidence;
            _tmpAverageConfidence = _cursor.getFloat(_cursorIndexOfAverageConfidence);
            final String _tmpImagePath;
            if (_cursor.isNull(_cursorIndexOfImagePath)) {
              _tmpImagePath = null;
            } else {
              _tmpImagePath = _cursor.getString(_cursorIndexOfImagePath);
            }
            final String _tmpLocation;
            if (_cursor.isNull(_cursorIndexOfLocation)) {
              _tmpLocation = null;
            } else {
              _tmpLocation = _cursor.getString(_cursorIndexOfLocation);
            }
            _item = new DetectionRecord(_tmpTimestamp,_tmpTotalCoconuts,_tmpAverageConfidence,_tmpImagePath,_tmpLocation);
            _item.id = _cursor.getInt(_cursorIndexOfId);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
