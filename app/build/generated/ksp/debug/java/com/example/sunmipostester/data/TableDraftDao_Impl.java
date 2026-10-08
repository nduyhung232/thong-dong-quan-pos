package com.example.sunmipostester.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomDatabaseKt;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
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
public final class TableDraftDao_Impl implements TableDraftDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TableDraftEntity> __insertionAdapterOfTableDraftEntity;

  private final EntityInsertionAdapter<TableDraftItemEntity> __insertionAdapterOfTableDraftItemEntity;

  private final SharedSQLiteStatement __preparedStmtOfClearItems;

  private final SharedSQLiteStatement __preparedStmtOfDeleteDraft;

  public TableDraftDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTableDraftEntity = new EntityInsertionAdapter<TableDraftEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `table_drafts` (`tableName`,`updatedAtMs`) VALUES (?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TableDraftEntity entity) {
        statement.bindString(1, entity.getTableName());
        statement.bindLong(2, entity.getUpdatedAtMs());
      }
    };
    this.__insertionAdapterOfTableDraftItemEntity = new EntityInsertionAdapter<TableDraftItemEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `table_draft_items` (`id`,`tableName`,`productId`,`productSyncId`,`productName`,`category`,`unitPrice`,`quantity`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TableDraftItemEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getTableName());
        statement.bindLong(3, entity.getProductId());
        statement.bindString(4, entity.getProductSyncId());
        statement.bindString(5, entity.getProductName());
        statement.bindString(6, entity.getCategory());
        statement.bindLong(7, entity.getUnitPrice());
        statement.bindLong(8, entity.getQuantity());
      }
    };
    this.__preparedStmtOfClearItems = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM table_draft_items WHERE tableName = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteDraft = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM table_drafts WHERE tableName = ?";
        return _query;
      }
    };
  }

  @Override
  public Object putDraft(final TableDraftEntity draft,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfTableDraftEntity.insert(draft);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertItems(final List<TableDraftItemEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfTableDraftItemEntity.insert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object saveDraft(final TableDraftEntity draft, final List<TableDraftItemEntity> items,
      final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> TableDraftDao.DefaultImpls.saveDraft(TableDraftDao_Impl.this, draft, items, __cont), $completion);
  }

  @Override
  public Object clearItems(final String tableName, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearItems.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, tableName);
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
          __preparedStmtOfClearItems.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteDraft(final String tableName, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteDraft.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, tableName);
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
          __preparedStmtOfDeleteDraft.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object drafts(final Continuation<? super List<TableDraftEntity>> $completion) {
    final String _sql = "SELECT * FROM table_drafts ORDER BY tableName";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<TableDraftEntity>>() {
      @Override
      @NonNull
      public List<TableDraftEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfTableName = CursorUtil.getColumnIndexOrThrow(_cursor, "tableName");
          final int _cursorIndexOfUpdatedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAtMs");
          final List<TableDraftEntity> _result = new ArrayList<TableDraftEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TableDraftEntity _item;
            final String _tmpTableName;
            _tmpTableName = _cursor.getString(_cursorIndexOfTableName);
            final long _tmpUpdatedAtMs;
            _tmpUpdatedAtMs = _cursor.getLong(_cursorIndexOfUpdatedAtMs);
            _item = new TableDraftEntity(_tmpTableName,_tmpUpdatedAtMs);
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
  public Object items(final String tableName,
      final Continuation<? super List<TableDraftItemEntity>> $completion) {
    final String _sql = "SELECT * FROM table_draft_items WHERE tableName = ? ORDER BY id";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, tableName);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<TableDraftItemEntity>>() {
      @Override
      @NonNull
      public List<TableDraftItemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTableName = CursorUtil.getColumnIndexOrThrow(_cursor, "tableName");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfProductSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "productSyncId");
          final int _cursorIndexOfProductName = CursorUtil.getColumnIndexOrThrow(_cursor, "productName");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfUnitPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "unitPrice");
          final int _cursorIndexOfQuantity = CursorUtil.getColumnIndexOrThrow(_cursor, "quantity");
          final List<TableDraftItemEntity> _result = new ArrayList<TableDraftItemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TableDraftItemEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTableName;
            _tmpTableName = _cursor.getString(_cursorIndexOfTableName);
            final long _tmpProductId;
            _tmpProductId = _cursor.getLong(_cursorIndexOfProductId);
            final String _tmpProductSyncId;
            _tmpProductSyncId = _cursor.getString(_cursorIndexOfProductSyncId);
            final String _tmpProductName;
            _tmpProductName = _cursor.getString(_cursorIndexOfProductName);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final int _tmpUnitPrice;
            _tmpUnitPrice = _cursor.getInt(_cursorIndexOfUnitPrice);
            final int _tmpQuantity;
            _tmpQuantity = _cursor.getInt(_cursorIndexOfQuantity);
            _item = new TableDraftItemEntity(_tmpId,_tmpTableName,_tmpProductId,_tmpProductSyncId,_tmpProductName,_tmpCategory,_tmpUnitPrice,_tmpQuantity);
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
