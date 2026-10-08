package com.example.sunmipostester.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
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
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ReportDao_Impl implements ReportDao {
  private final RoomDatabase __db;

  private final Converters __converters = new Converters();

  public ReportDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
  }

  @Override
  public Object revenueByMethod(final long fromMs, final long toMs,
      final Continuation<? super List<MethodRevenue>> $completion) {
    final String _sql = "\n"
            + "        SELECT paymentMethod, COUNT(*) AS orderCount, COALESCE(SUM(total), 0) AS revenue\n"
            + "        FROM orders\n"
            + "        WHERE status = 'PAID' AND createdAtMs BETWEEN ? AND ?\n"
            + "        GROUP BY paymentMethod\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, fromMs);
    _argIndex = 2;
    _statement.bindLong(_argIndex, toMs);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<MethodRevenue>>() {
      @Override
      @NonNull
      public List<MethodRevenue> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfPaymentMethod = 0;
          final int _cursorIndexOfOrderCount = 1;
          final int _cursorIndexOfRevenue = 2;
          final List<MethodRevenue> _result = new ArrayList<MethodRevenue>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final MethodRevenue _item;
            final PaymentMethod _tmpPaymentMethod;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfPaymentMethod);
            _tmpPaymentMethod = __converters.stringToPaymentMethod(_tmp);
            final int _tmpOrderCount;
            _tmpOrderCount = _cursor.getInt(_cursorIndexOfOrderCount);
            final int _tmpRevenue;
            _tmpRevenue = _cursor.getInt(_cursorIndexOfRevenue);
            _item = new MethodRevenue(_tmpPaymentMethod,_tmpOrderCount,_tmpRevenue);
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
  public Object topProducts(final long fromMs, final long toMs, final int limit,
      final Continuation<? super List<TopProduct>> $completion) {
    final String _sql = "\n"
            + "        SELECT i.productName AS productName,\n"
            + "               SUM(i.quantity) AS quantity,\n"
            + "               SUM(i.unitPrice * i.quantity) AS revenue\n"
            + "        FROM order_items i\n"
            + "        INNER JOIN orders o ON o.id = i.orderId\n"
            + "        WHERE o.status = 'PAID' AND o.createdAtMs BETWEEN ? AND ?\n"
            + "        GROUP BY i.productName\n"
            + "        ORDER BY quantity DESC\n"
            + "        LIMIT ?\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, fromMs);
    _argIndex = 2;
    _statement.bindLong(_argIndex, toMs);
    _argIndex = 3;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<TopProduct>>() {
      @Override
      @NonNull
      public List<TopProduct> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfProductName = 0;
          final int _cursorIndexOfQuantity = 1;
          final int _cursorIndexOfRevenue = 2;
          final List<TopProduct> _result = new ArrayList<TopProduct>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TopProduct _item;
            final String _tmpProductName;
            _tmpProductName = _cursor.getString(_cursorIndexOfProductName);
            final int _tmpQuantity;
            _tmpQuantity = _cursor.getInt(_cursorIndexOfQuantity);
            final int _tmpRevenue;
            _tmpRevenue = _cursor.getInt(_cursorIndexOfRevenue);
            _item = new TopProduct(_tmpProductName,_tmpQuantity,_tmpRevenue);
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
  public Object allOrderItems(final Continuation<? super List<OrderItemEntity>> $completion) {
    final String _sql = "SELECT * FROM order_items ORDER BY orderId, id";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<OrderItemEntity>>() {
      @Override
      @NonNull
      public List<OrderItemEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "syncId");
          final int _cursorIndexOfOrderId = CursorUtil.getColumnIndexOrThrow(_cursor, "orderId");
          final int _cursorIndexOfProductId = CursorUtil.getColumnIndexOrThrow(_cursor, "productId");
          final int _cursorIndexOfProductSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "productSyncId");
          final int _cursorIndexOfProductName = CursorUtil.getColumnIndexOrThrow(_cursor, "productName");
          final int _cursorIndexOfUnitPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "unitPrice");
          final int _cursorIndexOfQuantity = CursorUtil.getColumnIndexOrThrow(_cursor, "quantity");
          final List<OrderItemEntity> _result = new ArrayList<OrderItemEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final OrderItemEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSyncId;
            _tmpSyncId = _cursor.getString(_cursorIndexOfSyncId);
            final long _tmpOrderId;
            _tmpOrderId = _cursor.getLong(_cursorIndexOfOrderId);
            final long _tmpProductId;
            _tmpProductId = _cursor.getLong(_cursorIndexOfProductId);
            final String _tmpProductSyncId;
            if (_cursor.isNull(_cursorIndexOfProductSyncId)) {
              _tmpProductSyncId = null;
            } else {
              _tmpProductSyncId = _cursor.getString(_cursorIndexOfProductSyncId);
            }
            final String _tmpProductName;
            _tmpProductName = _cursor.getString(_cursorIndexOfProductName);
            final int _tmpUnitPrice;
            _tmpUnitPrice = _cursor.getInt(_cursorIndexOfUnitPrice);
            final int _tmpQuantity;
            _tmpQuantity = _cursor.getInt(_cursorIndexOfQuantity);
            _item = new OrderItemEntity(_tmpId,_tmpSyncId,_tmpOrderId,_tmpProductId,_tmpProductSyncId,_tmpProductName,_tmpUnitPrice,_tmpQuantity);
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
