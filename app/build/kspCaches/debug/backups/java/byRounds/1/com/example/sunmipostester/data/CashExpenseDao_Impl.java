package com.example.sunmipostester.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
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
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class CashExpenseDao_Impl implements CashExpenseDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<CashExpenseEntity> __insertionAdapterOfCashExpenseEntity;

  public CashExpenseDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCashExpenseEntity = new EntityInsertionAdapter<CashExpenseEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `cash_expenses` (`id`,`shiftId`,`amount`,`description`,`createdAtMs`,`staffId`,`staffName`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CashExpenseEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getShiftId());
        statement.bindLong(3, entity.getAmount());
        statement.bindString(4, entity.getDescription());
        statement.bindLong(5, entity.getCreatedAtMs());
        if (entity.getStaffId() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getStaffId());
        }
        if (entity.getStaffName() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getStaffName());
        }
      }
    };
  }

  @Override
  public Object insert(final CashExpenseEntity expense,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfCashExpenseEntity.insertAndReturnId(expense);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object totalForShift(final long shiftId, final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COALESCE(SUM(amount), 0) FROM cash_expenses WHERE shiftId = ?";
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
  public Object allForShift(final long shiftId,
      final Continuation<? super List<CashExpenseEntity>> $completion) {
    final String _sql = "SELECT * FROM cash_expenses WHERE shiftId = ? ORDER BY createdAtMs DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, shiftId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<CashExpenseEntity>>() {
      @Override
      @NonNull
      public List<CashExpenseEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfShiftId = CursorUtil.getColumnIndexOrThrow(_cursor, "shiftId");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfCreatedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtMs");
          final int _cursorIndexOfStaffId = CursorUtil.getColumnIndexOrThrow(_cursor, "staffId");
          final int _cursorIndexOfStaffName = CursorUtil.getColumnIndexOrThrow(_cursor, "staffName");
          final List<CashExpenseEntity> _result = new ArrayList<CashExpenseEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CashExpenseEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpShiftId;
            _tmpShiftId = _cursor.getLong(_cursorIndexOfShiftId);
            final int _tmpAmount;
            _tmpAmount = _cursor.getInt(_cursorIndexOfAmount);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final long _tmpCreatedAtMs;
            _tmpCreatedAtMs = _cursor.getLong(_cursorIndexOfCreatedAtMs);
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
            _item = new CashExpenseEntity(_tmpId,_tmpShiftId,_tmpAmount,_tmpDescription,_tmpCreatedAtMs,_tmpStaffId,_tmpStaffName);
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
  public Object all(final Continuation<? super List<CashExpenseEntity>> $completion) {
    final String _sql = "SELECT * FROM cash_expenses ORDER BY createdAtMs DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<CashExpenseEntity>>() {
      @Override
      @NonNull
      public List<CashExpenseEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfShiftId = CursorUtil.getColumnIndexOrThrow(_cursor, "shiftId");
          final int _cursorIndexOfAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "amount");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfCreatedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtMs");
          final int _cursorIndexOfStaffId = CursorUtil.getColumnIndexOrThrow(_cursor, "staffId");
          final int _cursorIndexOfStaffName = CursorUtil.getColumnIndexOrThrow(_cursor, "staffName");
          final List<CashExpenseEntity> _result = new ArrayList<CashExpenseEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CashExpenseEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpShiftId;
            _tmpShiftId = _cursor.getLong(_cursorIndexOfShiftId);
            final int _tmpAmount;
            _tmpAmount = _cursor.getInt(_cursorIndexOfAmount);
            final String _tmpDescription;
            _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            final long _tmpCreatedAtMs;
            _tmpCreatedAtMs = _cursor.getLong(_cursorIndexOfCreatedAtMs);
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
            _item = new CashExpenseEntity(_tmpId,_tmpShiftId,_tmpAmount,_tmpDescription,_tmpCreatedAtMs,_tmpStaffId,_tmpStaffName);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
