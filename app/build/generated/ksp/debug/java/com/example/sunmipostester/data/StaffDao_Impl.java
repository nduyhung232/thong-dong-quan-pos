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
public final class StaffDao_Impl implements StaffDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<StaffEntity> __insertionAdapterOfStaffEntity;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<StaffEntity> __updateAdapterOfStaffEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeactivate;

  private final SharedSQLiteStatement __preparedStmtOfReactivate;

  public StaffDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfStaffEntity = new EntityInsertionAdapter<StaffEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `staff` (`id`,`syncId`,`name`,`role`,`pinHash`,`pinSalt`,`active`,`createdAtMs`,`updatedAtMs`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final StaffEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getSyncId());
        statement.bindString(3, entity.getName());
        final String _tmp = __converters.staffRoleToString(entity.getRole());
        statement.bindString(4, _tmp);
        statement.bindString(5, entity.getPinHash());
        statement.bindString(6, entity.getPinSalt());
        final int _tmp_1 = entity.getActive() ? 1 : 0;
        statement.bindLong(7, _tmp_1);
        statement.bindLong(8, entity.getCreatedAtMs());
        statement.bindLong(9, entity.getUpdatedAtMs());
      }
    };
    this.__updateAdapterOfStaffEntity = new EntityDeletionOrUpdateAdapter<StaffEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `staff` SET `id` = ?,`syncId` = ?,`name` = ?,`role` = ?,`pinHash` = ?,`pinSalt` = ?,`active` = ?,`createdAtMs` = ?,`updatedAtMs` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final StaffEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getSyncId());
        statement.bindString(3, entity.getName());
        final String _tmp = __converters.staffRoleToString(entity.getRole());
        statement.bindString(4, _tmp);
        statement.bindString(5, entity.getPinHash());
        statement.bindString(6, entity.getPinSalt());
        final int _tmp_1 = entity.getActive() ? 1 : 0;
        statement.bindLong(7, _tmp_1);
        statement.bindLong(8, entity.getCreatedAtMs());
        statement.bindLong(9, entity.getUpdatedAtMs());
        statement.bindLong(10, entity.getId());
      }
    };
    this.__preparedStmtOfDeactivate = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE staff SET active = 0 WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfReactivate = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE staff SET active = 1 WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final StaffEntity staff, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfStaffEntity.insertAndReturnId(staff);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final StaffEntity staff, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfStaffEntity.handle(staff);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deactivate(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeactivate.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
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
          __preparedStmtOfDeactivate.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object reactivate(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfReactivate.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
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
          __preparedStmtOfReactivate.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object activeStaff(final Continuation<? super List<StaffEntity>> $completion) {
    final String _sql = "SELECT * FROM staff WHERE active = 1 ORDER BY role, name";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<StaffEntity>>() {
      @Override
      @NonNull
      public List<StaffEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "syncId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
          final int _cursorIndexOfPinHash = CursorUtil.getColumnIndexOrThrow(_cursor, "pinHash");
          final int _cursorIndexOfPinSalt = CursorUtil.getColumnIndexOrThrow(_cursor, "pinSalt");
          final int _cursorIndexOfActive = CursorUtil.getColumnIndexOrThrow(_cursor, "active");
          final int _cursorIndexOfCreatedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtMs");
          final int _cursorIndexOfUpdatedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAtMs");
          final List<StaffEntity> _result = new ArrayList<StaffEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final StaffEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSyncId;
            _tmpSyncId = _cursor.getString(_cursorIndexOfSyncId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final StaffRole _tmpRole;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfRole);
            _tmpRole = __converters.stringToStaffRole(_tmp);
            final String _tmpPinHash;
            _tmpPinHash = _cursor.getString(_cursorIndexOfPinHash);
            final String _tmpPinSalt;
            _tmpPinSalt = _cursor.getString(_cursorIndexOfPinSalt);
            final boolean _tmpActive;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfActive);
            _tmpActive = _tmp_1 != 0;
            final long _tmpCreatedAtMs;
            _tmpCreatedAtMs = _cursor.getLong(_cursorIndexOfCreatedAtMs);
            final long _tmpUpdatedAtMs;
            _tmpUpdatedAtMs = _cursor.getLong(_cursorIndexOfUpdatedAtMs);
            _item = new StaffEntity(_tmpId,_tmpSyncId,_tmpName,_tmpRole,_tmpPinHash,_tmpPinSalt,_tmpActive,_tmpCreatedAtMs,_tmpUpdatedAtMs);
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
  public Object allStaff(final Continuation<? super List<StaffEntity>> $completion) {
    final String _sql = "SELECT * FROM staff ORDER BY active DESC, role, name";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<StaffEntity>>() {
      @Override
      @NonNull
      public List<StaffEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "syncId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
          final int _cursorIndexOfPinHash = CursorUtil.getColumnIndexOrThrow(_cursor, "pinHash");
          final int _cursorIndexOfPinSalt = CursorUtil.getColumnIndexOrThrow(_cursor, "pinSalt");
          final int _cursorIndexOfActive = CursorUtil.getColumnIndexOrThrow(_cursor, "active");
          final int _cursorIndexOfCreatedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtMs");
          final int _cursorIndexOfUpdatedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAtMs");
          final List<StaffEntity> _result = new ArrayList<StaffEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final StaffEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSyncId;
            _tmpSyncId = _cursor.getString(_cursorIndexOfSyncId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final StaffRole _tmpRole;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfRole);
            _tmpRole = __converters.stringToStaffRole(_tmp);
            final String _tmpPinHash;
            _tmpPinHash = _cursor.getString(_cursorIndexOfPinHash);
            final String _tmpPinSalt;
            _tmpPinSalt = _cursor.getString(_cursorIndexOfPinSalt);
            final boolean _tmpActive;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfActive);
            _tmpActive = _tmp_1 != 0;
            final long _tmpCreatedAtMs;
            _tmpCreatedAtMs = _cursor.getLong(_cursorIndexOfCreatedAtMs);
            final long _tmpUpdatedAtMs;
            _tmpUpdatedAtMs = _cursor.getLong(_cursorIndexOfUpdatedAtMs);
            _item = new StaffEntity(_tmpId,_tmpSyncId,_tmpName,_tmpRole,_tmpPinHash,_tmpPinSalt,_tmpActive,_tmpCreatedAtMs,_tmpUpdatedAtMs);
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
  public Object byId(final long id, final Continuation<? super StaffEntity> $completion) {
    final String _sql = "SELECT * FROM staff WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<StaffEntity>() {
      @Override
      @Nullable
      public StaffEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "syncId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
          final int _cursorIndexOfPinHash = CursorUtil.getColumnIndexOrThrow(_cursor, "pinHash");
          final int _cursorIndexOfPinSalt = CursorUtil.getColumnIndexOrThrow(_cursor, "pinSalt");
          final int _cursorIndexOfActive = CursorUtil.getColumnIndexOrThrow(_cursor, "active");
          final int _cursorIndexOfCreatedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtMs");
          final int _cursorIndexOfUpdatedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAtMs");
          final StaffEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSyncId;
            _tmpSyncId = _cursor.getString(_cursorIndexOfSyncId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final StaffRole _tmpRole;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfRole);
            _tmpRole = __converters.stringToStaffRole(_tmp);
            final String _tmpPinHash;
            _tmpPinHash = _cursor.getString(_cursorIndexOfPinHash);
            final String _tmpPinSalt;
            _tmpPinSalt = _cursor.getString(_cursorIndexOfPinSalt);
            final boolean _tmpActive;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfActive);
            _tmpActive = _tmp_1 != 0;
            final long _tmpCreatedAtMs;
            _tmpCreatedAtMs = _cursor.getLong(_cursorIndexOfCreatedAtMs);
            final long _tmpUpdatedAtMs;
            _tmpUpdatedAtMs = _cursor.getLong(_cursorIndexOfUpdatedAtMs);
            _result = new StaffEntity(_tmpId,_tmpSyncId,_tmpName,_tmpRole,_tmpPinHash,_tmpPinSalt,_tmpActive,_tmpCreatedAtMs,_tmpUpdatedAtMs);
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
  public Object bySyncId(final String syncId, final Continuation<? super StaffEntity> $completion) {
    final String _sql = "SELECT * FROM staff WHERE syncId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, syncId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<StaffEntity>() {
      @Override
      @Nullable
      public StaffEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "syncId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
          final int _cursorIndexOfPinHash = CursorUtil.getColumnIndexOrThrow(_cursor, "pinHash");
          final int _cursorIndexOfPinSalt = CursorUtil.getColumnIndexOrThrow(_cursor, "pinSalt");
          final int _cursorIndexOfActive = CursorUtil.getColumnIndexOrThrow(_cursor, "active");
          final int _cursorIndexOfCreatedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtMs");
          final int _cursorIndexOfUpdatedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAtMs");
          final StaffEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSyncId;
            _tmpSyncId = _cursor.getString(_cursorIndexOfSyncId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final StaffRole _tmpRole;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfRole);
            _tmpRole = __converters.stringToStaffRole(_tmp);
            final String _tmpPinHash;
            _tmpPinHash = _cursor.getString(_cursorIndexOfPinHash);
            final String _tmpPinSalt;
            _tmpPinSalt = _cursor.getString(_cursorIndexOfPinSalt);
            final boolean _tmpActive;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfActive);
            _tmpActive = _tmp_1 != 0;
            final long _tmpCreatedAtMs;
            _tmpCreatedAtMs = _cursor.getLong(_cursorIndexOfCreatedAtMs);
            final long _tmpUpdatedAtMs;
            _tmpUpdatedAtMs = _cursor.getLong(_cursorIndexOfUpdatedAtMs);
            _result = new StaffEntity(_tmpId,_tmpSyncId,_tmpName,_tmpRole,_tmpPinHash,_tmpPinSalt,_tmpActive,_tmpCreatedAtMs,_tmpUpdatedAtMs);
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
  public Object count(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM staff";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
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
  public Object activeManagerCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM staff WHERE active = 1 AND role = 'MANAGER'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
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
