package com.example.sunmipostester.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
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
import java.lang.Integer;
import java.lang.Long;
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

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ShiftDao_Impl implements ShiftDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ShiftEntity> __insertionAdapterOfShiftEntity;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<ShiftEntity> __updateAdapterOfShiftEntity;

  private final SharedSQLiteStatement __preparedStmtOfMarkSynced;

  public ShiftDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfShiftEntity = new EntityInsertionAdapter<ShiftEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `shifts` (`id`,`syncId`,`openedAtMs`,`closedAtMs`,`openingCash`,`countedCash`,`expectedCash`,`cashDifference`,`cashBreakdown`,`status`,`staffId`,`staffName`,`closedByStaffId`,`staffSyncId`,`closedByStaffSyncId`,`syncedAtMs`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ShiftEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getSyncId());
        statement.bindLong(3, entity.getOpenedAtMs());
        if (entity.getClosedAtMs() == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, entity.getClosedAtMs());
        }
        statement.bindLong(5, entity.getOpeningCash());
        if (entity.getCountedCash() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getCountedCash());
        }
        if (entity.getExpectedCash() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getExpectedCash());
        }
        if (entity.getCashDifference() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getCashDifference());
        }
        if (entity.getCashBreakdown() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getCashBreakdown());
        }
        final String _tmp = __converters.shiftStatusToString(entity.getStatus());
        statement.bindString(10, _tmp);
        if (entity.getStaffId() == null) {
          statement.bindNull(11);
        } else {
          statement.bindLong(11, entity.getStaffId());
        }
        if (entity.getStaffName() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getStaffName());
        }
        if (entity.getClosedByStaffId() == null) {
          statement.bindNull(13);
        } else {
          statement.bindLong(13, entity.getClosedByStaffId());
        }
        if (entity.getStaffSyncId() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getStaffSyncId());
        }
        if (entity.getClosedByStaffSyncId() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getClosedByStaffSyncId());
        }
        if (entity.getSyncedAtMs() == null) {
          statement.bindNull(16);
        } else {
          statement.bindLong(16, entity.getSyncedAtMs());
        }
      }
    };
    this.__updateAdapterOfShiftEntity = new EntityDeletionOrUpdateAdapter<ShiftEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `shifts` SET `id` = ?,`syncId` = ?,`openedAtMs` = ?,`closedAtMs` = ?,`openingCash` = ?,`countedCash` = ?,`expectedCash` = ?,`cashDifference` = ?,`cashBreakdown` = ?,`status` = ?,`staffId` = ?,`staffName` = ?,`closedByStaffId` = ?,`staffSyncId` = ?,`closedByStaffSyncId` = ?,`syncedAtMs` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ShiftEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getSyncId());
        statement.bindLong(3, entity.getOpenedAtMs());
        if (entity.getClosedAtMs() == null) {
          statement.bindNull(4);
        } else {
          statement.bindLong(4, entity.getClosedAtMs());
        }
        statement.bindLong(5, entity.getOpeningCash());
        if (entity.getCountedCash() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getCountedCash());
        }
        if (entity.getExpectedCash() == null) {
          statement.bindNull(7);
        } else {
          statement.bindLong(7, entity.getExpectedCash());
        }
        if (entity.getCashDifference() == null) {
          statement.bindNull(8);
        } else {
          statement.bindLong(8, entity.getCashDifference());
        }
        if (entity.getCashBreakdown() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getCashBreakdown());
        }
        final String _tmp = __converters.shiftStatusToString(entity.getStatus());
        statement.bindString(10, _tmp);
        if (entity.getStaffId() == null) {
          statement.bindNull(11);
        } else {
          statement.bindLong(11, entity.getStaffId());
        }
        if (entity.getStaffName() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getStaffName());
        }
        if (entity.getClosedByStaffId() == null) {
          statement.bindNull(13);
        } else {
          statement.bindLong(13, entity.getClosedByStaffId());
        }
        if (entity.getStaffSyncId() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getStaffSyncId());
        }
        if (entity.getClosedByStaffSyncId() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getClosedByStaffSyncId());
        }
        if (entity.getSyncedAtMs() == null) {
          statement.bindNull(16);
        } else {
          statement.bindLong(16, entity.getSyncedAtMs());
        }
        statement.bindLong(17, entity.getId());
      }
    };
    this.__preparedStmtOfMarkSynced = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE shifts SET syncedAtMs = ? WHERE syncId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final ShiftEntity shift, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfShiftEntity.insertAndReturnId(shift);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final ShiftEntity shift, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfShiftEntity.handle(shift);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object markSynced(final String syncId, final long atMs,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkSynced.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, atMs);
        _argIndex = 2;
        _stmt.bindString(_argIndex, syncId);
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
          __preparedStmtOfMarkSynced.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object openShift(final Continuation<? super ShiftEntity> $completion) {
    final String _sql = "SELECT * FROM shifts WHERE status = 'OPEN' ORDER BY openedAtMs DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ShiftEntity>() {
      @Override
      @Nullable
      public ShiftEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "syncId");
          final int _cursorIndexOfOpenedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "openedAtMs");
          final int _cursorIndexOfClosedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "closedAtMs");
          final int _cursorIndexOfOpeningCash = CursorUtil.getColumnIndexOrThrow(_cursor, "openingCash");
          final int _cursorIndexOfCountedCash = CursorUtil.getColumnIndexOrThrow(_cursor, "countedCash");
          final int _cursorIndexOfExpectedCash = CursorUtil.getColumnIndexOrThrow(_cursor, "expectedCash");
          final int _cursorIndexOfCashDifference = CursorUtil.getColumnIndexOrThrow(_cursor, "cashDifference");
          final int _cursorIndexOfCashBreakdown = CursorUtil.getColumnIndexOrThrow(_cursor, "cashBreakdown");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfStaffId = CursorUtil.getColumnIndexOrThrow(_cursor, "staffId");
          final int _cursorIndexOfStaffName = CursorUtil.getColumnIndexOrThrow(_cursor, "staffName");
          final int _cursorIndexOfClosedByStaffId = CursorUtil.getColumnIndexOrThrow(_cursor, "closedByStaffId");
          final int _cursorIndexOfStaffSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "staffSyncId");
          final int _cursorIndexOfClosedByStaffSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "closedByStaffSyncId");
          final int _cursorIndexOfSyncedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "syncedAtMs");
          final ShiftEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSyncId;
            _tmpSyncId = _cursor.getString(_cursorIndexOfSyncId);
            final long _tmpOpenedAtMs;
            _tmpOpenedAtMs = _cursor.getLong(_cursorIndexOfOpenedAtMs);
            final Long _tmpClosedAtMs;
            if (_cursor.isNull(_cursorIndexOfClosedAtMs)) {
              _tmpClosedAtMs = null;
            } else {
              _tmpClosedAtMs = _cursor.getLong(_cursorIndexOfClosedAtMs);
            }
            final int _tmpOpeningCash;
            _tmpOpeningCash = _cursor.getInt(_cursorIndexOfOpeningCash);
            final Integer _tmpCountedCash;
            if (_cursor.isNull(_cursorIndexOfCountedCash)) {
              _tmpCountedCash = null;
            } else {
              _tmpCountedCash = _cursor.getInt(_cursorIndexOfCountedCash);
            }
            final Integer _tmpExpectedCash;
            if (_cursor.isNull(_cursorIndexOfExpectedCash)) {
              _tmpExpectedCash = null;
            } else {
              _tmpExpectedCash = _cursor.getInt(_cursorIndexOfExpectedCash);
            }
            final Integer _tmpCashDifference;
            if (_cursor.isNull(_cursorIndexOfCashDifference)) {
              _tmpCashDifference = null;
            } else {
              _tmpCashDifference = _cursor.getInt(_cursorIndexOfCashDifference);
            }
            final String _tmpCashBreakdown;
            if (_cursor.isNull(_cursorIndexOfCashBreakdown)) {
              _tmpCashBreakdown = null;
            } else {
              _tmpCashBreakdown = _cursor.getString(_cursorIndexOfCashBreakdown);
            }
            final ShiftStatus _tmpStatus;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.stringToShiftStatus(_tmp);
            final Long _tmpStaffId;
            if (_cursor.isNull(_cursorIndexOfStaffId)) {
              _tmpStaffId = null;
            } else {
              _tmpStaffId = _cursor.getLong(_cursorIndexOfStaffId);
            }
            final String _tmpStaffName;
            if (_cursor.isNull(_cursorIndexOfStaffName)) {
              _tmpStaffName = null;
            } else {
              _tmpStaffName = _cursor.getString(_cursorIndexOfStaffName);
            }
            final Long _tmpClosedByStaffId;
            if (_cursor.isNull(_cursorIndexOfClosedByStaffId)) {
              _tmpClosedByStaffId = null;
            } else {
              _tmpClosedByStaffId = _cursor.getLong(_cursorIndexOfClosedByStaffId);
            }
            final String _tmpStaffSyncId;
            if (_cursor.isNull(_cursorIndexOfStaffSyncId)) {
              _tmpStaffSyncId = null;
            } else {
              _tmpStaffSyncId = _cursor.getString(_cursorIndexOfStaffSyncId);
            }
            final String _tmpClosedByStaffSyncId;
            if (_cursor.isNull(_cursorIndexOfClosedByStaffSyncId)) {
              _tmpClosedByStaffSyncId = null;
            } else {
              _tmpClosedByStaffSyncId = _cursor.getString(_cursorIndexOfClosedByStaffSyncId);
            }
            final Long _tmpSyncedAtMs;
            if (_cursor.isNull(_cursorIndexOfSyncedAtMs)) {
              _tmpSyncedAtMs = null;
            } else {
              _tmpSyncedAtMs = _cursor.getLong(_cursorIndexOfSyncedAtMs);
            }
            _result = new ShiftEntity(_tmpId,_tmpSyncId,_tmpOpenedAtMs,_tmpClosedAtMs,_tmpOpeningCash,_tmpCountedCash,_tmpExpectedCash,_tmpCashDifference,_tmpCashBreakdown,_tmpStatus,_tmpStaffId,_tmpStaffName,_tmpClosedByStaffId,_tmpStaffSyncId,_tmpClosedByStaffSyncId,_tmpSyncedAtMs);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object recentShifts(final int limit,
      final Continuation<? super List<ShiftEntity>> $completion) {
    final String _sql = "SELECT * FROM shifts ORDER BY openedAtMs DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ShiftEntity>>() {
      @Override
      @NonNull
      public List<ShiftEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "syncId");
          final int _cursorIndexOfOpenedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "openedAtMs");
          final int _cursorIndexOfClosedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "closedAtMs");
          final int _cursorIndexOfOpeningCash = CursorUtil.getColumnIndexOrThrow(_cursor, "openingCash");
          final int _cursorIndexOfCountedCash = CursorUtil.getColumnIndexOrThrow(_cursor, "countedCash");
          final int _cursorIndexOfExpectedCash = CursorUtil.getColumnIndexOrThrow(_cursor, "expectedCash");
          final int _cursorIndexOfCashDifference = CursorUtil.getColumnIndexOrThrow(_cursor, "cashDifference");
          final int _cursorIndexOfCashBreakdown = CursorUtil.getColumnIndexOrThrow(_cursor, "cashBreakdown");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfStaffId = CursorUtil.getColumnIndexOrThrow(_cursor, "staffId");
          final int _cursorIndexOfStaffName = CursorUtil.getColumnIndexOrThrow(_cursor, "staffName");
          final int _cursorIndexOfClosedByStaffId = CursorUtil.getColumnIndexOrThrow(_cursor, "closedByStaffId");
          final int _cursorIndexOfStaffSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "staffSyncId");
          final int _cursorIndexOfClosedByStaffSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "closedByStaffSyncId");
          final int _cursorIndexOfSyncedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "syncedAtMs");
          final List<ShiftEntity> _result = new ArrayList<ShiftEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ShiftEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSyncId;
            _tmpSyncId = _cursor.getString(_cursorIndexOfSyncId);
            final long _tmpOpenedAtMs;
            _tmpOpenedAtMs = _cursor.getLong(_cursorIndexOfOpenedAtMs);
            final Long _tmpClosedAtMs;
            if (_cursor.isNull(_cursorIndexOfClosedAtMs)) {
              _tmpClosedAtMs = null;
            } else {
              _tmpClosedAtMs = _cursor.getLong(_cursorIndexOfClosedAtMs);
            }
            final int _tmpOpeningCash;
            _tmpOpeningCash = _cursor.getInt(_cursorIndexOfOpeningCash);
            final Integer _tmpCountedCash;
            if (_cursor.isNull(_cursorIndexOfCountedCash)) {
              _tmpCountedCash = null;
            } else {
              _tmpCountedCash = _cursor.getInt(_cursorIndexOfCountedCash);
            }
            final Integer _tmpExpectedCash;
            if (_cursor.isNull(_cursorIndexOfExpectedCash)) {
              _tmpExpectedCash = null;
            } else {
              _tmpExpectedCash = _cursor.getInt(_cursorIndexOfExpectedCash);
            }
            final Integer _tmpCashDifference;
            if (_cursor.isNull(_cursorIndexOfCashDifference)) {
              _tmpCashDifference = null;
            } else {
              _tmpCashDifference = _cursor.getInt(_cursorIndexOfCashDifference);
            }
            final String _tmpCashBreakdown;
            if (_cursor.isNull(_cursorIndexOfCashBreakdown)) {
              _tmpCashBreakdown = null;
            } else {
              _tmpCashBreakdown = _cursor.getString(_cursorIndexOfCashBreakdown);
            }
            final ShiftStatus _tmpStatus;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.stringToShiftStatus(_tmp);
            final Long _tmpStaffId;
            if (_cursor.isNull(_cursorIndexOfStaffId)) {
              _tmpStaffId = null;
            } else {
              _tmpStaffId = _cursor.getLong(_cursorIndexOfStaffId);
            }
            final String _tmpStaffName;
            if (_cursor.isNull(_cursorIndexOfStaffName)) {
              _tmpStaffName = null;
            } else {
              _tmpStaffName = _cursor.getString(_cursorIndexOfStaffName);
            }
            final Long _tmpClosedByStaffId;
            if (_cursor.isNull(_cursorIndexOfClosedByStaffId)) {
              _tmpClosedByStaffId = null;
            } else {
              _tmpClosedByStaffId = _cursor.getLong(_cursorIndexOfClosedByStaffId);
            }
            final String _tmpStaffSyncId;
            if (_cursor.isNull(_cursorIndexOfStaffSyncId)) {
              _tmpStaffSyncId = null;
            } else {
              _tmpStaffSyncId = _cursor.getString(_cursorIndexOfStaffSyncId);
            }
            final String _tmpClosedByStaffSyncId;
            if (_cursor.isNull(_cursorIndexOfClosedByStaffSyncId)) {
              _tmpClosedByStaffSyncId = null;
            } else {
              _tmpClosedByStaffSyncId = _cursor.getString(_cursorIndexOfClosedByStaffSyncId);
            }
            final Long _tmpSyncedAtMs;
            if (_cursor.isNull(_cursorIndexOfSyncedAtMs)) {
              _tmpSyncedAtMs = null;
            } else {
              _tmpSyncedAtMs = _cursor.getLong(_cursorIndexOfSyncedAtMs);
            }
            _item = new ShiftEntity(_tmpId,_tmpSyncId,_tmpOpenedAtMs,_tmpClosedAtMs,_tmpOpeningCash,_tmpCountedCash,_tmpExpectedCash,_tmpCashDifference,_tmpCashBreakdown,_tmpStatus,_tmpStaffId,_tmpStaffName,_tmpClosedByStaffId,_tmpStaffSyncId,_tmpClosedByStaffSyncId,_tmpSyncedAtMs);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object unsyncedShifts(final Continuation<? super List<ShiftEntity>> $completion) {
    final String _sql = "SELECT * FROM shifts WHERE syncedAtMs IS NULL ORDER BY openedAtMs";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ShiftEntity>>() {
      @Override
      @NonNull
      public List<ShiftEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "syncId");
          final int _cursorIndexOfOpenedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "openedAtMs");
          final int _cursorIndexOfClosedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "closedAtMs");
          final int _cursorIndexOfOpeningCash = CursorUtil.getColumnIndexOrThrow(_cursor, "openingCash");
          final int _cursorIndexOfCountedCash = CursorUtil.getColumnIndexOrThrow(_cursor, "countedCash");
          final int _cursorIndexOfExpectedCash = CursorUtil.getColumnIndexOrThrow(_cursor, "expectedCash");
          final int _cursorIndexOfCashDifference = CursorUtil.getColumnIndexOrThrow(_cursor, "cashDifference");
          final int _cursorIndexOfCashBreakdown = CursorUtil.getColumnIndexOrThrow(_cursor, "cashBreakdown");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfStaffId = CursorUtil.getColumnIndexOrThrow(_cursor, "staffId");
          final int _cursorIndexOfStaffName = CursorUtil.getColumnIndexOrThrow(_cursor, "staffName");
          final int _cursorIndexOfClosedByStaffId = CursorUtil.getColumnIndexOrThrow(_cursor, "closedByStaffId");
          final int _cursorIndexOfStaffSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "staffSyncId");
          final int _cursorIndexOfClosedByStaffSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "closedByStaffSyncId");
          final int _cursorIndexOfSyncedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "syncedAtMs");
          final List<ShiftEntity> _result = new ArrayList<ShiftEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ShiftEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSyncId;
            _tmpSyncId = _cursor.getString(_cursorIndexOfSyncId);
            final long _tmpOpenedAtMs;
            _tmpOpenedAtMs = _cursor.getLong(_cursorIndexOfOpenedAtMs);
            final Long _tmpClosedAtMs;
            if (_cursor.isNull(_cursorIndexOfClosedAtMs)) {
              _tmpClosedAtMs = null;
            } else {
              _tmpClosedAtMs = _cursor.getLong(_cursorIndexOfClosedAtMs);
            }
            final int _tmpOpeningCash;
            _tmpOpeningCash = _cursor.getInt(_cursorIndexOfOpeningCash);
            final Integer _tmpCountedCash;
            if (_cursor.isNull(_cursorIndexOfCountedCash)) {
              _tmpCountedCash = null;
            } else {
              _tmpCountedCash = _cursor.getInt(_cursorIndexOfCountedCash);
            }
            final Integer _tmpExpectedCash;
            if (_cursor.isNull(_cursorIndexOfExpectedCash)) {
              _tmpExpectedCash = null;
            } else {
              _tmpExpectedCash = _cursor.getInt(_cursorIndexOfExpectedCash);
            }
            final Integer _tmpCashDifference;
            if (_cursor.isNull(_cursorIndexOfCashDifference)) {
              _tmpCashDifference = null;
            } else {
              _tmpCashDifference = _cursor.getInt(_cursorIndexOfCashDifference);
            }
            final String _tmpCashBreakdown;
            if (_cursor.isNull(_cursorIndexOfCashBreakdown)) {
              _tmpCashBreakdown = null;
            } else {
              _tmpCashBreakdown = _cursor.getString(_cursorIndexOfCashBreakdown);
            }
            final ShiftStatus _tmpStatus;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.stringToShiftStatus(_tmp);
            final Long _tmpStaffId;
            if (_cursor.isNull(_cursorIndexOfStaffId)) {
              _tmpStaffId = null;
            } else {
              _tmpStaffId = _cursor.getLong(_cursorIndexOfStaffId);
            }
            final String _tmpStaffName;
            if (_cursor.isNull(_cursorIndexOfStaffName)) {
              _tmpStaffName = null;
            } else {
              _tmpStaffName = _cursor.getString(_cursorIndexOfStaffName);
            }
            final Long _tmpClosedByStaffId;
            if (_cursor.isNull(_cursorIndexOfClosedByStaffId)) {
              _tmpClosedByStaffId = null;
            } else {
              _tmpClosedByStaffId = _cursor.getLong(_cursorIndexOfClosedByStaffId);
            }
            final String _tmpStaffSyncId;
            if (_cursor.isNull(_cursorIndexOfStaffSyncId)) {
              _tmpStaffSyncId = null;
            } else {
              _tmpStaffSyncId = _cursor.getString(_cursorIndexOfStaffSyncId);
            }
            final String _tmpClosedByStaffSyncId;
            if (_cursor.isNull(_cursorIndexOfClosedByStaffSyncId)) {
              _tmpClosedByStaffSyncId = null;
            } else {
              _tmpClosedByStaffSyncId = _cursor.getString(_cursorIndexOfClosedByStaffSyncId);
            }
            final Long _tmpSyncedAtMs;
            if (_cursor.isNull(_cursorIndexOfSyncedAtMs)) {
              _tmpSyncedAtMs = null;
            } else {
              _tmpSyncedAtMs = _cursor.getLong(_cursorIndexOfSyncedAtMs);
            }
            _item = new ShiftEntity(_tmpId,_tmpSyncId,_tmpOpenedAtMs,_tmpClosedAtMs,_tmpOpeningCash,_tmpCountedCash,_tmpExpectedCash,_tmpCashDifference,_tmpCashBreakdown,_tmpStatus,_tmpStaffId,_tmpStaffName,_tmpClosedByStaffId,_tmpStaffSyncId,_tmpClosedByStaffSyncId,_tmpSyncedAtMs);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object shiftById(final long shiftId, final Continuation<? super ShiftEntity> $completion) {
    final String _sql = "SELECT * FROM shifts WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, shiftId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ShiftEntity>() {
      @Override
      @Nullable
      public ShiftEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "syncId");
          final int _cursorIndexOfOpenedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "openedAtMs");
          final int _cursorIndexOfClosedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "closedAtMs");
          final int _cursorIndexOfOpeningCash = CursorUtil.getColumnIndexOrThrow(_cursor, "openingCash");
          final int _cursorIndexOfCountedCash = CursorUtil.getColumnIndexOrThrow(_cursor, "countedCash");
          final int _cursorIndexOfExpectedCash = CursorUtil.getColumnIndexOrThrow(_cursor, "expectedCash");
          final int _cursorIndexOfCashDifference = CursorUtil.getColumnIndexOrThrow(_cursor, "cashDifference");
          final int _cursorIndexOfCashBreakdown = CursorUtil.getColumnIndexOrThrow(_cursor, "cashBreakdown");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfStaffId = CursorUtil.getColumnIndexOrThrow(_cursor, "staffId");
          final int _cursorIndexOfStaffName = CursorUtil.getColumnIndexOrThrow(_cursor, "staffName");
          final int _cursorIndexOfClosedByStaffId = CursorUtil.getColumnIndexOrThrow(_cursor, "closedByStaffId");
          final int _cursorIndexOfStaffSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "staffSyncId");
          final int _cursorIndexOfClosedByStaffSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "closedByStaffSyncId");
          final int _cursorIndexOfSyncedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "syncedAtMs");
          final ShiftEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSyncId;
            _tmpSyncId = _cursor.getString(_cursorIndexOfSyncId);
            final long _tmpOpenedAtMs;
            _tmpOpenedAtMs = _cursor.getLong(_cursorIndexOfOpenedAtMs);
            final Long _tmpClosedAtMs;
            if (_cursor.isNull(_cursorIndexOfClosedAtMs)) {
              _tmpClosedAtMs = null;
            } else {
              _tmpClosedAtMs = _cursor.getLong(_cursorIndexOfClosedAtMs);
            }
            final int _tmpOpeningCash;
            _tmpOpeningCash = _cursor.getInt(_cursorIndexOfOpeningCash);
            final Integer _tmpCountedCash;
            if (_cursor.isNull(_cursorIndexOfCountedCash)) {
              _tmpCountedCash = null;
            } else {
              _tmpCountedCash = _cursor.getInt(_cursorIndexOfCountedCash);
            }
            final Integer _tmpExpectedCash;
            if (_cursor.isNull(_cursorIndexOfExpectedCash)) {
              _tmpExpectedCash = null;
            } else {
              _tmpExpectedCash = _cursor.getInt(_cursorIndexOfExpectedCash);
            }
            final Integer _tmpCashDifference;
            if (_cursor.isNull(_cursorIndexOfCashDifference)) {
              _tmpCashDifference = null;
            } else {
              _tmpCashDifference = _cursor.getInt(_cursorIndexOfCashDifference);
            }
            final String _tmpCashBreakdown;
            if (_cursor.isNull(_cursorIndexOfCashBreakdown)) {
              _tmpCashBreakdown = null;
            } else {
              _tmpCashBreakdown = _cursor.getString(_cursorIndexOfCashBreakdown);
            }
            final ShiftStatus _tmpStatus;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.stringToShiftStatus(_tmp);
            final Long _tmpStaffId;
            if (_cursor.isNull(_cursorIndexOfStaffId)) {
              _tmpStaffId = null;
            } else {
              _tmpStaffId = _cursor.getLong(_cursorIndexOfStaffId);
            }
            final String _tmpStaffName;
            if (_cursor.isNull(_cursorIndexOfStaffName)) {
              _tmpStaffName = null;
            } else {
              _tmpStaffName = _cursor.getString(_cursorIndexOfStaffName);
            }
            final Long _tmpClosedByStaffId;
            if (_cursor.isNull(_cursorIndexOfClosedByStaffId)) {
              _tmpClosedByStaffId = null;
            } else {
              _tmpClosedByStaffId = _cursor.getLong(_cursorIndexOfClosedByStaffId);
            }
            final String _tmpStaffSyncId;
            if (_cursor.isNull(_cursorIndexOfStaffSyncId)) {
              _tmpStaffSyncId = null;
            } else {
              _tmpStaffSyncId = _cursor.getString(_cursorIndexOfStaffSyncId);
            }
            final String _tmpClosedByStaffSyncId;
            if (_cursor.isNull(_cursorIndexOfClosedByStaffSyncId)) {
              _tmpClosedByStaffSyncId = null;
            } else {
              _tmpClosedByStaffSyncId = _cursor.getString(_cursorIndexOfClosedByStaffSyncId);
            }
            final Long _tmpSyncedAtMs;
            if (_cursor.isNull(_cursorIndexOfSyncedAtMs)) {
              _tmpSyncedAtMs = null;
            } else {
              _tmpSyncedAtMs = _cursor.getLong(_cursorIndexOfSyncedAtMs);
            }
            _result = new ShiftEntity(_tmpId,_tmpSyncId,_tmpOpenedAtMs,_tmpClosedAtMs,_tmpOpeningCash,_tmpCountedCash,_tmpExpectedCash,_tmpCashDifference,_tmpCashBreakdown,_tmpStatus,_tmpStaffId,_tmpStaffName,_tmpClosedByStaffId,_tmpStaffSyncId,_tmpClosedByStaffSyncId,_tmpSyncedAtMs);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object cashSalesOfShift(final long shiftId,
      final Continuation<? super Integer> $completion) {
    final String _sql = "\n"
            + "        SELECT COALESCE(SUM(total), 0) FROM orders\n"
            + "        WHERE shiftId = ? AND status = 'PAID' AND paymentMethod = 'CASH'\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, shiftId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
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
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object totalSalesOfShift(final long shiftId,
      final Continuation<? super Integer> $completion) {
    final String _sql = "\n"
            + "        SELECT COALESCE(SUM(total), 0) FROM orders\n"
            + "        WHERE shiftId = ? AND status = 'PAID'\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, shiftId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
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
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object paidCountOfShift(final long shiftId,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM orders WHERE shiftId = ? AND status = 'PAID'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, shiftId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
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
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
