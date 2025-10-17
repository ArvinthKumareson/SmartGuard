package com.smartguard.app.db;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ScanRecordDao_Impl implements ScanRecordDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ScanRecordEntity> __insertionAdapterOfScanRecordEntity;

  private final SharedSQLiteStatement __preparedStmtOfClearAllForUser;

  public ScanRecordDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfScanRecordEntity = new EntityInsertionAdapter<ScanRecordEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR IGNORE INTO `scan_records` (`id`,`message`,`matchedKeywords`,`sourceApp`,`timestamp`,`userId`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ScanRecordEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getMessage());
        statement.bindString(3, entity.getMatchedKeywords());
        if (entity.getSourceApp() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getSourceApp());
        }
        statement.bindLong(5, entity.getTimestamp());
        statement.bindString(6, entity.getUserId());
      }
    };
    this.__preparedStmtOfClearAllForUser = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM scan_records WHERE userId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final ScanRecordEntity record,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfScanRecordEntity.insert(record);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object clearAllForUser(final String userId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearAllForUser.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, userId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearAllForUser.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ScanRecordEntity>> recentForUser(final String userId) {
    final String _sql = "SELECT * FROM scan_records WHERE userId = ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"scan_records"}, new Callable<List<ScanRecordEntity>>() {
      @Override
      @NonNull
      public List<ScanRecordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "message");
          final int _cursorIndexOfMatchedKeywords = CursorUtil.getColumnIndexOrThrow(_cursor, "matchedKeywords");
          final int _cursorIndexOfSourceApp = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceApp");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final List<ScanRecordEntity> _result = new ArrayList<ScanRecordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ScanRecordEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpMessage;
            _tmpMessage = _cursor.getString(_cursorIndexOfMessage);
            final String _tmpMatchedKeywords;
            _tmpMatchedKeywords = _cursor.getString(_cursorIndexOfMatchedKeywords);
            final String _tmpSourceApp;
            if (_cursor.isNull(_cursorIndexOfSourceApp)) {
              _tmpSourceApp = null;
            } else {
              _tmpSourceApp = _cursor.getString(_cursorIndexOfSourceApp);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            _item = new ScanRecordEntity(_tmpId,_tmpMessage,_tmpMatchedKeywords,_tmpSourceApp,_tmpTimestamp,_tmpUserId);
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

  @Override
  public Flow<Integer> countForUser(final String userId) {
    final String _sql = "SELECT COUNT(*) FROM scan_records WHERE userId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"scan_records"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
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

  @Override
  public Flow<List<ScanRecordEntity>> recordsBySource(final String userId, final String source) {
    final String _sql = "SELECT * FROM scan_records WHERE userId = ? AND sourceApp = ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    _argIndex = 2;
    _statement.bindString(_argIndex, source);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"scan_records"}, new Callable<List<ScanRecordEntity>>() {
      @Override
      @NonNull
      public List<ScanRecordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "message");
          final int _cursorIndexOfMatchedKeywords = CursorUtil.getColumnIndexOrThrow(_cursor, "matchedKeywords");
          final int _cursorIndexOfSourceApp = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceApp");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final List<ScanRecordEntity> _result = new ArrayList<ScanRecordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ScanRecordEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpMessage;
            _tmpMessage = _cursor.getString(_cursorIndexOfMessage);
            final String _tmpMatchedKeywords;
            _tmpMatchedKeywords = _cursor.getString(_cursorIndexOfMatchedKeywords);
            final String _tmpSourceApp;
            if (_cursor.isNull(_cursorIndexOfSourceApp)) {
              _tmpSourceApp = null;
            } else {
              _tmpSourceApp = _cursor.getString(_cursorIndexOfSourceApp);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            _item = new ScanRecordEntity(_tmpId,_tmpMessage,_tmpMatchedKeywords,_tmpSourceApp,_tmpTimestamp,_tmpUserId);
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

  @Override
  public Flow<List<ScanRecordEntity>> riskyRecords(final String userId, final int minKeywords) {
    final String _sql = "\n"
            + "        SELECT * FROM scan_records \n"
            + "        WHERE userId = ? AND \n"
            + "              LENGTH(matchedKeywords) - LENGTH(REPLACE(matchedKeywords, ',', '')) + 1 >= ? \n"
            + "        ORDER BY timestamp DESC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, minKeywords);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"scan_records"}, new Callable<List<ScanRecordEntity>>() {
      @Override
      @NonNull
      public List<ScanRecordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "message");
          final int _cursorIndexOfMatchedKeywords = CursorUtil.getColumnIndexOrThrow(_cursor, "matchedKeywords");
          final int _cursorIndexOfSourceApp = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceApp");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final List<ScanRecordEntity> _result = new ArrayList<ScanRecordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ScanRecordEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpMessage;
            _tmpMessage = _cursor.getString(_cursorIndexOfMessage);
            final String _tmpMatchedKeywords;
            _tmpMatchedKeywords = _cursor.getString(_cursorIndexOfMatchedKeywords);
            final String _tmpSourceApp;
            if (_cursor.isNull(_cursorIndexOfSourceApp)) {
              _tmpSourceApp = null;
            } else {
              _tmpSourceApp = _cursor.getString(_cursorIndexOfSourceApp);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            _item = new ScanRecordEntity(_tmpId,_tmpMessage,_tmpMatchedKeywords,_tmpSourceApp,_tmpTimestamp,_tmpUserId);
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

  @Override
  public Flow<List<ScanRecordEntity>> searchByKeyword(final String userId, final String keyword) {
    final String _sql = "\n"
            + "        SELECT * FROM scan_records \n"
            + "        WHERE userId = ? AND matchedKeywords LIKE '%' || ? || '%' \n"
            + "        ORDER BY timestamp DESC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, userId);
    _argIndex = 2;
    _statement.bindString(_argIndex, keyword);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"scan_records"}, new Callable<List<ScanRecordEntity>>() {
      @Override
      @NonNull
      public List<ScanRecordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "message");
          final int _cursorIndexOfMatchedKeywords = CursorUtil.getColumnIndexOrThrow(_cursor, "matchedKeywords");
          final int _cursorIndexOfSourceApp = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceApp");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final List<ScanRecordEntity> _result = new ArrayList<ScanRecordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ScanRecordEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpMessage;
            _tmpMessage = _cursor.getString(_cursorIndexOfMessage);
            final String _tmpMatchedKeywords;
            _tmpMatchedKeywords = _cursor.getString(_cursorIndexOfMatchedKeywords);
            final String _tmpSourceApp;
            if (_cursor.isNull(_cursorIndexOfSourceApp)) {
              _tmpSourceApp = null;
            } else {
              _tmpSourceApp = _cursor.getString(_cursorIndexOfSourceApp);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpUserId;
            _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            _item = new ScanRecordEntity(_tmpId,_tmpMessage,_tmpMatchedKeywords,_tmpSourceApp,_tmpTimestamp,_tmpUserId);
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
