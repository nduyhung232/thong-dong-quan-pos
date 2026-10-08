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
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.example.sunmipostester.common.DiscountType;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
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
public final class DiscountCodeDao_Impl implements DiscountCodeDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<DiscountCodeEntity> __insertionAdapterOfDiscountCodeEntity;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<DiscountCodeEntity> __updateAdapterOfDiscountCodeEntity;

  private final SharedSQLiteStatement __preparedStmtOfMarkConsumed;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllUnconsumed;

  public DiscountCodeDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfDiscountCodeEntity = new EntityInsertionAdapter<DiscountCodeEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `discount_codes` (`id`,`syncId`,`code`,`campaignSyncId`,`campaignName`,`valueType`,`value`,`consumed`,`consumedByOrderSyncId`,`consumedAtMs`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DiscountCodeEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getSyncId());
        statement.bindString(3, entity.getCode());
        statement.bindString(4, entity.getCampaignSyncId());
        statement.bindString(5, entity.getCampaignName());
        final String _tmp = __converters.discountTypeToString(entity.getValueType());
        statement.bindString(6, _tmp);
        statement.bindLong(7, entity.getValue());
        final int _tmp_1 = entity.getConsumed() ? 1 : 0;
        statement.bindLong(8, _tmp_1);
        if (entity.getConsumedByOrderSyncId() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getConsumedByOrderSyncId());
        }
        if (entity.getConsumedAtMs() == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, entity.getConsumedAtMs());
        }
      }
    };
    this.__updateAdapterOfDiscountCodeEntity = new EntityDeletionOrUpdateAdapter<DiscountCodeEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `discount_codes` SET `id` = ?,`syncId` = ?,`code` = ?,`campaignSyncId` = ?,`campaignName` = ?,`valueType` = ?,`value` = ?,`consumed` = ?,`consumedByOrderSyncId` = ?,`consumedAtMs` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final DiscountCodeEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getSyncId());
        statement.bindString(3, entity.getCode());
        statement.bindString(4, entity.getCampaignSyncId());
        statement.bindString(5, entity.getCampaignName());
        final String _tmp = __converters.discountTypeToString(entity.getValueType());
        statement.bindString(6, _tmp);
        statement.bindLong(7, entity.getValue());
        final int _tmp_1 = entity.getConsumed() ? 1 : 0;
        statement.bindLong(8, _tmp_1);
        if (entity.getConsumedByOrderSyncId() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getConsumedByOrderSyncId());
        }
        if (entity.getConsumedAtMs() == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, entity.getConsumedAtMs());
        }
        statement.bindLong(11, entity.getId());
      }
    };
    this.__preparedStmtOfMarkConsumed = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "\n"
                + "        UPDATE discount_codes\n"
                + "        SET consumed = 1, consumedByOrderSyncId = ?, consumedAtMs = ?\n"
                + "        WHERE syncId = ?\n"
                + "        ";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAllUnconsumed = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM discount_codes WHERE consumed = 0";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final DiscountCodeEntity code,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfDiscountCodeEntity.insertAndReturnId(code);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final DiscountCodeEntity code,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfDiscountCodeEntity.handle(code);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object markConsumed(final String syncId, final String orderSyncId, final long atMs,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkConsumed.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, orderSyncId);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, atMs);
        _argIndex = 3;
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
          __preparedStmtOfMarkConsumed.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAllUnconsumed(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllUnconsumed.acquire();
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
          __preparedStmtOfDeleteAllUnconsumed.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object bySyncId(final String syncId,
      final Continuation<? super DiscountCodeEntity> $completion) {
    final String _sql = "SELECT * FROM discount_codes WHERE syncId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, syncId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<DiscountCodeEntity>() {
      @Override
      @Nullable
      public DiscountCodeEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "syncId");
          final int _cursorIndexOfCode = CursorUtil.getColumnIndexOrThrow(_cursor, "code");
          final int _cursorIndexOfCampaignSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "campaignSyncId");
          final int _cursorIndexOfCampaignName = CursorUtil.getColumnIndexOrThrow(_cursor, "campaignName");
          final int _cursorIndexOfValueType = CursorUtil.getColumnIndexOrThrow(_cursor, "valueType");
          final int _cursorIndexOfValue = CursorUtil.getColumnIndexOrThrow(_cursor, "value");
          final int _cursorIndexOfConsumed = CursorUtil.getColumnIndexOrThrow(_cursor, "consumed");
          final int _cursorIndexOfConsumedByOrderSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "consumedByOrderSyncId");
          final int _cursorIndexOfConsumedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "consumedAtMs");
          final DiscountCodeEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSyncId;
            _tmpSyncId = _cursor.getString(_cursorIndexOfSyncId);
            final String _tmpCode;
            _tmpCode = _cursor.getString(_cursorIndexOfCode);
            final String _tmpCampaignSyncId;
            _tmpCampaignSyncId = _cursor.getString(_cursorIndexOfCampaignSyncId);
            final String _tmpCampaignName;
            _tmpCampaignName = _cursor.getString(_cursorIndexOfCampaignName);
            final DiscountType _tmpValueType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfValueType);
            _tmpValueType = __converters.stringToDiscountType(_tmp);
            final int _tmpValue;
            _tmpValue = _cursor.getInt(_cursorIndexOfValue);
            final boolean _tmpConsumed;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfConsumed);
            _tmpConsumed = _tmp_1 != 0;
            final String _tmpConsumedByOrderSyncId;
            if (_cursor.isNull(_cursorIndexOfConsumedByOrderSyncId)) {
              _tmpConsumedByOrderSyncId = null;
            } else {
              _tmpConsumedByOrderSyncId = _cursor.getString(_cursorIndexOfConsumedByOrderSyncId);
            }
            final Long _tmpConsumedAtMs;
            if (_cursor.isNull(_cursorIndexOfConsumedAtMs)) {
              _tmpConsumedAtMs = null;
            } else {
              _tmpConsumedAtMs = _cursor.getLong(_cursorIndexOfConsumedAtMs);
            }
            _result = new DiscountCodeEntity(_tmpId,_tmpSyncId,_tmpCode,_tmpCampaignSyncId,_tmpCampaignName,_tmpValueType,_tmpValue,_tmpConsumed,_tmpConsumedByOrderSyncId,_tmpConsumedAtMs);
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
  public Object byCode(final String code,
      final Continuation<? super DiscountCodeEntity> $completion) {
    final String _sql = "SELECT * FROM discount_codes WHERE code = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, code);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<DiscountCodeEntity>() {
      @Override
      @Nullable
      public DiscountCodeEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "syncId");
          final int _cursorIndexOfCode = CursorUtil.getColumnIndexOrThrow(_cursor, "code");
          final int _cursorIndexOfCampaignSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "campaignSyncId");
          final int _cursorIndexOfCampaignName = CursorUtil.getColumnIndexOrThrow(_cursor, "campaignName");
          final int _cursorIndexOfValueType = CursorUtil.getColumnIndexOrThrow(_cursor, "valueType");
          final int _cursorIndexOfValue = CursorUtil.getColumnIndexOrThrow(_cursor, "value");
          final int _cursorIndexOfConsumed = CursorUtil.getColumnIndexOrThrow(_cursor, "consumed");
          final int _cursorIndexOfConsumedByOrderSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "consumedByOrderSyncId");
          final int _cursorIndexOfConsumedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "consumedAtMs");
          final DiscountCodeEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSyncId;
            _tmpSyncId = _cursor.getString(_cursorIndexOfSyncId);
            final String _tmpCode;
            _tmpCode = _cursor.getString(_cursorIndexOfCode);
            final String _tmpCampaignSyncId;
            _tmpCampaignSyncId = _cursor.getString(_cursorIndexOfCampaignSyncId);
            final String _tmpCampaignName;
            _tmpCampaignName = _cursor.getString(_cursorIndexOfCampaignName);
            final DiscountType _tmpValueType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfValueType);
            _tmpValueType = __converters.stringToDiscountType(_tmp);
            final int _tmpValue;
            _tmpValue = _cursor.getInt(_cursorIndexOfValue);
            final boolean _tmpConsumed;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfConsumed);
            _tmpConsumed = _tmp_1 != 0;
            final String _tmpConsumedByOrderSyncId;
            if (_cursor.isNull(_cursorIndexOfConsumedByOrderSyncId)) {
              _tmpConsumedByOrderSyncId = null;
            } else {
              _tmpConsumedByOrderSyncId = _cursor.getString(_cursorIndexOfConsumedByOrderSyncId);
            }
            final Long _tmpConsumedAtMs;
            if (_cursor.isNull(_cursorIndexOfConsumedAtMs)) {
              _tmpConsumedAtMs = null;
            } else {
              _tmpConsumedAtMs = _cursor.getLong(_cursorIndexOfConsumedAtMs);
            }
            _result = new DiscountCodeEntity(_tmpId,_tmpSyncId,_tmpCode,_tmpCampaignSyncId,_tmpCampaignName,_tmpValueType,_tmpValue,_tmpConsumed,_tmpConsumedByOrderSyncId,_tmpConsumedAtMs);
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
  public Object available(final Continuation<? super List<DiscountCodeEntity>> $completion) {
    final String _sql = "SELECT * FROM discount_codes WHERE consumed = 0 ORDER BY campaignName, code";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<DiscountCodeEntity>>() {
      @Override
      @NonNull
      public List<DiscountCodeEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "syncId");
          final int _cursorIndexOfCode = CursorUtil.getColumnIndexOrThrow(_cursor, "code");
          final int _cursorIndexOfCampaignSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "campaignSyncId");
          final int _cursorIndexOfCampaignName = CursorUtil.getColumnIndexOrThrow(_cursor, "campaignName");
          final int _cursorIndexOfValueType = CursorUtil.getColumnIndexOrThrow(_cursor, "valueType");
          final int _cursorIndexOfValue = CursorUtil.getColumnIndexOrThrow(_cursor, "value");
          final int _cursorIndexOfConsumed = CursorUtil.getColumnIndexOrThrow(_cursor, "consumed");
          final int _cursorIndexOfConsumedByOrderSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "consumedByOrderSyncId");
          final int _cursorIndexOfConsumedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "consumedAtMs");
          final List<DiscountCodeEntity> _result = new ArrayList<DiscountCodeEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final DiscountCodeEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSyncId;
            _tmpSyncId = _cursor.getString(_cursorIndexOfSyncId);
            final String _tmpCode;
            _tmpCode = _cursor.getString(_cursorIndexOfCode);
            final String _tmpCampaignSyncId;
            _tmpCampaignSyncId = _cursor.getString(_cursorIndexOfCampaignSyncId);
            final String _tmpCampaignName;
            _tmpCampaignName = _cursor.getString(_cursorIndexOfCampaignName);
            final DiscountType _tmpValueType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfValueType);
            _tmpValueType = __converters.stringToDiscountType(_tmp);
            final int _tmpValue;
            _tmpValue = _cursor.getInt(_cursorIndexOfValue);
            final boolean _tmpConsumed;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfConsumed);
            _tmpConsumed = _tmp_1 != 0;
            final String _tmpConsumedByOrderSyncId;
            if (_cursor.isNull(_cursorIndexOfConsumedByOrderSyncId)) {
              _tmpConsumedByOrderSyncId = null;
            } else {
              _tmpConsumedByOrderSyncId = _cursor.getString(_cursorIndexOfConsumedByOrderSyncId);
            }
            final Long _tmpConsumedAtMs;
            if (_cursor.isNull(_cursorIndexOfConsumedAtMs)) {
              _tmpConsumedAtMs = null;
            } else {
              _tmpConsumedAtMs = _cursor.getLong(_cursorIndexOfConsumedAtMs);
            }
            _item = new DiscountCodeEntity(_tmpId,_tmpSyncId,_tmpCode,_tmpCampaignSyncId,_tmpCampaignName,_tmpValueType,_tmpValue,_tmpConsumed,_tmpConsumedByOrderSyncId,_tmpConsumedAtMs);
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
  public Object deleteUnconsumedNotIn(final List<String> keepSyncIds,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
        _stringBuilder.append("DELETE FROM discount_codes WHERE consumed = 0 AND syncId NOT IN (");
        final int _inputSize = keepSyncIds.size();
        StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
        _stringBuilder.append(")");
        final String _sql = _stringBuilder.toString();
        final SupportSQLiteStatement _stmt = __db.compileStatement(_sql);
        int _argIndex = 1;
        for (String _item : keepSyncIds) {
          _stmt.bindString(_argIndex, _item);
          _argIndex++;
        }
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
