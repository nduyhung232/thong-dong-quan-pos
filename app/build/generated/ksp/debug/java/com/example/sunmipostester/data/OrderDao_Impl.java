package com.example.sunmipostester.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomDatabaseKt;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.example.sunmipostester.common.DiscountType;
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
public final class OrderDao_Impl implements OrderDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<OrderEntity> __insertionAdapterOfOrderEntity;

  private final Converters __converters = new Converters();

  private final EntityInsertionAdapter<OrderItemEntity> __insertionAdapterOfOrderItemEntity;

  private final SharedSQLiteStatement __preparedStmtOfMarkSynced;

  private final SharedSQLiteStatement __preparedStmtOfCancelOrder;

  public OrderDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfOrderEntity = new EntityInsertionAdapter<OrderEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `orders` (`id`,`syncId`,`subtotal`,`discountAmount`,`total`,`discountType`,`discountInput`,`discountCode`,`orderType`,`paymentMethod`,`cashReceived`,`changeAmount`,`status`,`createdAtMs`,`shiftId`,`shiftSyncId`,`staffId`,`staffName`,`staffSyncId`,`cancelledByStaffId`,`cancelledAtMs`,`cancelledByStaffSyncId`,`syncedAtMs`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final OrderEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getSyncId());
        statement.bindLong(3, entity.getSubtotal());
        statement.bindLong(4, entity.getDiscountAmount());
        statement.bindLong(5, entity.getTotal());
        final String _tmp = __converters.discountTypeToString(entity.getDiscountType());
        statement.bindString(6, _tmp);
        statement.bindLong(7, entity.getDiscountInput());
        if (entity.getDiscountCode() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getDiscountCode());
        }
        final String _tmp_1 = __converters.orderTypeToString(entity.getOrderType());
        statement.bindString(9, _tmp_1);
        final String _tmp_2 = __converters.paymentMethodToString(entity.getPaymentMethod());
        statement.bindString(10, _tmp_2);
        statement.bindLong(11, entity.getCashReceived());
        statement.bindLong(12, entity.getChangeAmount());
        final String _tmp_3 = __converters.orderStatusToString(entity.getStatus());
        statement.bindString(13, _tmp_3);
        statement.bindLong(14, entity.getCreatedAtMs());
        if (entity.getShiftId() == null) {
          statement.bindNull(15);
        } else {
          statement.bindLong(15, entity.getShiftId());
        }
        if (entity.getShiftSyncId() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getShiftSyncId());
        }
        if (entity.getStaffId() == null) {
          statement.bindNull(17);
        } else {
          statement.bindLong(17, entity.getStaffId());
        }
        if (entity.getStaffName() == null) {
          statement.bindNull(18);
        } else {
          statement.bindString(18, entity.getStaffName());
        }
        if (entity.getStaffSyncId() == null) {
          statement.bindNull(19);
        } else {
          statement.bindString(19, entity.getStaffSyncId());
        }
        if (entity.getCancelledByStaffId() == null) {
          statement.bindNull(20);
        } else {
          statement.bindLong(20, entity.getCancelledByStaffId());
        }
        if (entity.getCancelledAtMs() == null) {
          statement.bindNull(21);
        } else {
          statement.bindLong(21, entity.getCancelledAtMs());
        }
        if (entity.getCancelledByStaffSyncId() == null) {
          statement.bindNull(22);
        } else {
          statement.bindString(22, entity.getCancelledByStaffSyncId());
        }
        if (entity.getSyncedAtMs() == null) {
          statement.bindNull(23);
        } else {
          statement.bindLong(23, entity.getSyncedAtMs());
        }
      }
    };
    this.__insertionAdapterOfOrderItemEntity = new EntityInsertionAdapter<OrderItemEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `order_items` (`id`,`syncId`,`orderId`,`productId`,`productSyncId`,`productName`,`unitPrice`,`quantity`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final OrderItemEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getSyncId());
        statement.bindLong(3, entity.getOrderId());
        statement.bindLong(4, entity.getProductId());
        if (entity.getProductSyncId() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getProductSyncId());
        }
        statement.bindString(6, entity.getProductName());
        statement.bindLong(7, entity.getUnitPrice());
        statement.bindLong(8, entity.getQuantity());
      }
    };
    this.__preparedStmtOfMarkSynced = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE orders SET syncedAtMs = ? WHERE syncId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfCancelOrder = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "\n"
                + "        UPDATE orders\n"
                + "        SET status = 'CANCELLED', cancelledByStaffId = ?,\n"
                + "            cancelledByStaffSyncId = ?, cancelledAtMs = ?, syncedAtMs = NULL\n"
                + "        WHERE id = ?\n"
                + "        ";
        return _query;
      }
    };
  }

  @Override
  public Object insertOrder(final OrderEntity order, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfOrderEntity.insertAndReturnId(order);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertItems(final List<OrderItemEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfOrderItemEntity.insert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object saveOrder(final OrderEntity order, final List<OrderItemEntity> items,
      final Continuation<? super Long> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> OrderDao.DefaultImpls.saveOrder(OrderDao_Impl.this, order, items, __cont), $completion);
  }

  @Override
  public Object orderWithItems(final long orderId,
      final Continuation<? super OrderWithItems> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> OrderDao.DefaultImpls.orderWithItems(OrderDao_Impl.this, orderId, __cont), $completion);
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
  public Object cancelOrder(final long orderId, final Long staffId, final String staffSyncId,
      final long atMs, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfCancelOrder.acquire();
        int _argIndex = 1;
        if (staffId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindLong(_argIndex, staffId);
        }
        _argIndex = 2;
        if (staffSyncId == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, staffSyncId);
        }
        _argIndex = 3;
        _stmt.bindLong(_argIndex, atMs);
        _argIndex = 4;
        _stmt.bindLong(_argIndex, orderId);
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
          __preparedStmtOfCancelOrder.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object recentOrders(final int limit,
      final Continuation<? super List<OrderEntity>> $completion) {
    final String _sql = "SELECT * FROM orders ORDER BY createdAtMs DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, limit);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<OrderEntity>>() {
      @Override
      @NonNull
      public List<OrderEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "syncId");
          final int _cursorIndexOfSubtotal = CursorUtil.getColumnIndexOrThrow(_cursor, "subtotal");
          final int _cursorIndexOfDiscountAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "discountAmount");
          final int _cursorIndexOfTotal = CursorUtil.getColumnIndexOrThrow(_cursor, "total");
          final int _cursorIndexOfDiscountType = CursorUtil.getColumnIndexOrThrow(_cursor, "discountType");
          final int _cursorIndexOfDiscountInput = CursorUtil.getColumnIndexOrThrow(_cursor, "discountInput");
          final int _cursorIndexOfDiscountCode = CursorUtil.getColumnIndexOrThrow(_cursor, "discountCode");
          final int _cursorIndexOfOrderType = CursorUtil.getColumnIndexOrThrow(_cursor, "orderType");
          final int _cursorIndexOfPaymentMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentMethod");
          final int _cursorIndexOfCashReceived = CursorUtil.getColumnIndexOrThrow(_cursor, "cashReceived");
          final int _cursorIndexOfChangeAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "changeAmount");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfCreatedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtMs");
          final int _cursorIndexOfShiftId = CursorUtil.getColumnIndexOrThrow(_cursor, "shiftId");
          final int _cursorIndexOfShiftSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "shiftSyncId");
          final int _cursorIndexOfStaffId = CursorUtil.getColumnIndexOrThrow(_cursor, "staffId");
          final int _cursorIndexOfStaffName = CursorUtil.getColumnIndexOrThrow(_cursor, "staffName");
          final int _cursorIndexOfStaffSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "staffSyncId");
          final int _cursorIndexOfCancelledByStaffId = CursorUtil.getColumnIndexOrThrow(_cursor, "cancelledByStaffId");
          final int _cursorIndexOfCancelledAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "cancelledAtMs");
          final int _cursorIndexOfCancelledByStaffSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "cancelledByStaffSyncId");
          final int _cursorIndexOfSyncedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "syncedAtMs");
          final List<OrderEntity> _result = new ArrayList<OrderEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final OrderEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSyncId;
            _tmpSyncId = _cursor.getString(_cursorIndexOfSyncId);
            final int _tmpSubtotal;
            _tmpSubtotal = _cursor.getInt(_cursorIndexOfSubtotal);
            final int _tmpDiscountAmount;
            _tmpDiscountAmount = _cursor.getInt(_cursorIndexOfDiscountAmount);
            final int _tmpTotal;
            _tmpTotal = _cursor.getInt(_cursorIndexOfTotal);
            final DiscountType _tmpDiscountType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfDiscountType);
            _tmpDiscountType = __converters.stringToDiscountType(_tmp);
            final int _tmpDiscountInput;
            _tmpDiscountInput = _cursor.getInt(_cursorIndexOfDiscountInput);
            final String _tmpDiscountCode;
            if (_cursor.isNull(_cursorIndexOfDiscountCode)) {
              _tmpDiscountCode = null;
            } else {
              _tmpDiscountCode = _cursor.getString(_cursorIndexOfDiscountCode);
            }
            final OrderType _tmpOrderType;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfOrderType);
            _tmpOrderType = __converters.stringToOrderType(_tmp_1);
            final PaymentMethod _tmpPaymentMethod;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfPaymentMethod);
            _tmpPaymentMethod = __converters.stringToPaymentMethod(_tmp_2);
            final int _tmpCashReceived;
            _tmpCashReceived = _cursor.getInt(_cursorIndexOfCashReceived);
            final int _tmpChangeAmount;
            _tmpChangeAmount = _cursor.getInt(_cursorIndexOfChangeAmount);
            final OrderStatus _tmpStatus;
            final String _tmp_3;
            _tmp_3 = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.stringToOrderStatus(_tmp_3);
            final long _tmpCreatedAtMs;
            _tmpCreatedAtMs = _cursor.getLong(_cursorIndexOfCreatedAtMs);
            final Long _tmpShiftId;
            if (_cursor.isNull(_cursorIndexOfShiftId)) {
              _tmpShiftId = null;
            } else {
              _tmpShiftId = _cursor.getLong(_cursorIndexOfShiftId);
            }
            final String _tmpShiftSyncId;
            if (_cursor.isNull(_cursorIndexOfShiftSyncId)) {
              _tmpShiftSyncId = null;
            } else {
              _tmpShiftSyncId = _cursor.getString(_cursorIndexOfShiftSyncId);
            }
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
            final String _tmpStaffSyncId;
            if (_cursor.isNull(_cursorIndexOfStaffSyncId)) {
              _tmpStaffSyncId = null;
            } else {
              _tmpStaffSyncId = _cursor.getString(_cursorIndexOfStaffSyncId);
            }
            final Long _tmpCancelledByStaffId;
            if (_cursor.isNull(_cursorIndexOfCancelledByStaffId)) {
              _tmpCancelledByStaffId = null;
            } else {
              _tmpCancelledByStaffId = _cursor.getLong(_cursorIndexOfCancelledByStaffId);
            }
            final Long _tmpCancelledAtMs;
            if (_cursor.isNull(_cursorIndexOfCancelledAtMs)) {
              _tmpCancelledAtMs = null;
            } else {
              _tmpCancelledAtMs = _cursor.getLong(_cursorIndexOfCancelledAtMs);
            }
            final String _tmpCancelledByStaffSyncId;
            if (_cursor.isNull(_cursorIndexOfCancelledByStaffSyncId)) {
              _tmpCancelledByStaffSyncId = null;
            } else {
              _tmpCancelledByStaffSyncId = _cursor.getString(_cursorIndexOfCancelledByStaffSyncId);
            }
            final Long _tmpSyncedAtMs;
            if (_cursor.isNull(_cursorIndexOfSyncedAtMs)) {
              _tmpSyncedAtMs = null;
            } else {
              _tmpSyncedAtMs = _cursor.getLong(_cursorIndexOfSyncedAtMs);
            }
            _item = new OrderEntity(_tmpId,_tmpSyncId,_tmpSubtotal,_tmpDiscountAmount,_tmpTotal,_tmpDiscountType,_tmpDiscountInput,_tmpDiscountCode,_tmpOrderType,_tmpPaymentMethod,_tmpCashReceived,_tmpChangeAmount,_tmpStatus,_tmpCreatedAtMs,_tmpShiftId,_tmpShiftSyncId,_tmpStaffId,_tmpStaffName,_tmpStaffSyncId,_tmpCancelledByStaffId,_tmpCancelledAtMs,_tmpCancelledByStaffSyncId,_tmpSyncedAtMs);
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
  public Object ordersBetween(final long fromMs, final long toMs, final int limit, final int offset,
      final Continuation<? super List<OrderEntity>> $completion) {
    final String _sql = "SELECT * FROM orders WHERE createdAtMs BETWEEN ? AND ? ORDER BY createdAtMs DESC LIMIT ? OFFSET ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 4);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, fromMs);
    _argIndex = 2;
    _statement.bindLong(_argIndex, toMs);
    _argIndex = 3;
    _statement.bindLong(_argIndex, limit);
    _argIndex = 4;
    _statement.bindLong(_argIndex, offset);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<OrderEntity>>() {
      @Override
      @NonNull
      public List<OrderEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "syncId");
          final int _cursorIndexOfSubtotal = CursorUtil.getColumnIndexOrThrow(_cursor, "subtotal");
          final int _cursorIndexOfDiscountAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "discountAmount");
          final int _cursorIndexOfTotal = CursorUtil.getColumnIndexOrThrow(_cursor, "total");
          final int _cursorIndexOfDiscountType = CursorUtil.getColumnIndexOrThrow(_cursor, "discountType");
          final int _cursorIndexOfDiscountInput = CursorUtil.getColumnIndexOrThrow(_cursor, "discountInput");
          final int _cursorIndexOfDiscountCode = CursorUtil.getColumnIndexOrThrow(_cursor, "discountCode");
          final int _cursorIndexOfOrderType = CursorUtil.getColumnIndexOrThrow(_cursor, "orderType");
          final int _cursorIndexOfPaymentMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentMethod");
          final int _cursorIndexOfCashReceived = CursorUtil.getColumnIndexOrThrow(_cursor, "cashReceived");
          final int _cursorIndexOfChangeAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "changeAmount");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfCreatedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtMs");
          final int _cursorIndexOfShiftId = CursorUtil.getColumnIndexOrThrow(_cursor, "shiftId");
          final int _cursorIndexOfShiftSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "shiftSyncId");
          final int _cursorIndexOfStaffId = CursorUtil.getColumnIndexOrThrow(_cursor, "staffId");
          final int _cursorIndexOfStaffName = CursorUtil.getColumnIndexOrThrow(_cursor, "staffName");
          final int _cursorIndexOfStaffSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "staffSyncId");
          final int _cursorIndexOfCancelledByStaffId = CursorUtil.getColumnIndexOrThrow(_cursor, "cancelledByStaffId");
          final int _cursorIndexOfCancelledAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "cancelledAtMs");
          final int _cursorIndexOfCancelledByStaffSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "cancelledByStaffSyncId");
          final int _cursorIndexOfSyncedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "syncedAtMs");
          final List<OrderEntity> _result = new ArrayList<OrderEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final OrderEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSyncId;
            _tmpSyncId = _cursor.getString(_cursorIndexOfSyncId);
            final int _tmpSubtotal;
            _tmpSubtotal = _cursor.getInt(_cursorIndexOfSubtotal);
            final int _tmpDiscountAmount;
            _tmpDiscountAmount = _cursor.getInt(_cursorIndexOfDiscountAmount);
            final int _tmpTotal;
            _tmpTotal = _cursor.getInt(_cursorIndexOfTotal);
            final DiscountType _tmpDiscountType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfDiscountType);
            _tmpDiscountType = __converters.stringToDiscountType(_tmp);
            final int _tmpDiscountInput;
            _tmpDiscountInput = _cursor.getInt(_cursorIndexOfDiscountInput);
            final String _tmpDiscountCode;
            if (_cursor.isNull(_cursorIndexOfDiscountCode)) {
              _tmpDiscountCode = null;
            } else {
              _tmpDiscountCode = _cursor.getString(_cursorIndexOfDiscountCode);
            }
            final OrderType _tmpOrderType;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfOrderType);
            _tmpOrderType = __converters.stringToOrderType(_tmp_1);
            final PaymentMethod _tmpPaymentMethod;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfPaymentMethod);
            _tmpPaymentMethod = __converters.stringToPaymentMethod(_tmp_2);
            final int _tmpCashReceived;
            _tmpCashReceived = _cursor.getInt(_cursorIndexOfCashReceived);
            final int _tmpChangeAmount;
            _tmpChangeAmount = _cursor.getInt(_cursorIndexOfChangeAmount);
            final OrderStatus _tmpStatus;
            final String _tmp_3;
            _tmp_3 = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.stringToOrderStatus(_tmp_3);
            final long _tmpCreatedAtMs;
            _tmpCreatedAtMs = _cursor.getLong(_cursorIndexOfCreatedAtMs);
            final Long _tmpShiftId;
            if (_cursor.isNull(_cursorIndexOfShiftId)) {
              _tmpShiftId = null;
            } else {
              _tmpShiftId = _cursor.getLong(_cursorIndexOfShiftId);
            }
            final String _tmpShiftSyncId;
            if (_cursor.isNull(_cursorIndexOfShiftSyncId)) {
              _tmpShiftSyncId = null;
            } else {
              _tmpShiftSyncId = _cursor.getString(_cursorIndexOfShiftSyncId);
            }
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
            final String _tmpStaffSyncId;
            if (_cursor.isNull(_cursorIndexOfStaffSyncId)) {
              _tmpStaffSyncId = null;
            } else {
              _tmpStaffSyncId = _cursor.getString(_cursorIndexOfStaffSyncId);
            }
            final Long _tmpCancelledByStaffId;
            if (_cursor.isNull(_cursorIndexOfCancelledByStaffId)) {
              _tmpCancelledByStaffId = null;
            } else {
              _tmpCancelledByStaffId = _cursor.getLong(_cursorIndexOfCancelledByStaffId);
            }
            final Long _tmpCancelledAtMs;
            if (_cursor.isNull(_cursorIndexOfCancelledAtMs)) {
              _tmpCancelledAtMs = null;
            } else {
              _tmpCancelledAtMs = _cursor.getLong(_cursorIndexOfCancelledAtMs);
            }
            final String _tmpCancelledByStaffSyncId;
            if (_cursor.isNull(_cursorIndexOfCancelledByStaffSyncId)) {
              _tmpCancelledByStaffSyncId = null;
            } else {
              _tmpCancelledByStaffSyncId = _cursor.getString(_cursorIndexOfCancelledByStaffSyncId);
            }
            final Long _tmpSyncedAtMs;
            if (_cursor.isNull(_cursorIndexOfSyncedAtMs)) {
              _tmpSyncedAtMs = null;
            } else {
              _tmpSyncedAtMs = _cursor.getLong(_cursorIndexOfSyncedAtMs);
            }
            _item = new OrderEntity(_tmpId,_tmpSyncId,_tmpSubtotal,_tmpDiscountAmount,_tmpTotal,_tmpDiscountType,_tmpDiscountInput,_tmpDiscountCode,_tmpOrderType,_tmpPaymentMethod,_tmpCashReceived,_tmpChangeAmount,_tmpStatus,_tmpCreatedAtMs,_tmpShiftId,_tmpShiftSyncId,_tmpStaffId,_tmpStaffName,_tmpStaffSyncId,_tmpCancelledByStaffId,_tmpCancelledAtMs,_tmpCancelledByStaffSyncId,_tmpSyncedAtMs);
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
  public Object unsyncedOrders(final Continuation<? super List<OrderEntity>> $completion) {
    final String _sql = "SELECT * FROM orders WHERE syncedAtMs IS NULL ORDER BY createdAtMs";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<OrderEntity>>() {
      @Override
      @NonNull
      public List<OrderEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "syncId");
          final int _cursorIndexOfSubtotal = CursorUtil.getColumnIndexOrThrow(_cursor, "subtotal");
          final int _cursorIndexOfDiscountAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "discountAmount");
          final int _cursorIndexOfTotal = CursorUtil.getColumnIndexOrThrow(_cursor, "total");
          final int _cursorIndexOfDiscountType = CursorUtil.getColumnIndexOrThrow(_cursor, "discountType");
          final int _cursorIndexOfDiscountInput = CursorUtil.getColumnIndexOrThrow(_cursor, "discountInput");
          final int _cursorIndexOfDiscountCode = CursorUtil.getColumnIndexOrThrow(_cursor, "discountCode");
          final int _cursorIndexOfOrderType = CursorUtil.getColumnIndexOrThrow(_cursor, "orderType");
          final int _cursorIndexOfPaymentMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentMethod");
          final int _cursorIndexOfCashReceived = CursorUtil.getColumnIndexOrThrow(_cursor, "cashReceived");
          final int _cursorIndexOfChangeAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "changeAmount");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfCreatedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtMs");
          final int _cursorIndexOfShiftId = CursorUtil.getColumnIndexOrThrow(_cursor, "shiftId");
          final int _cursorIndexOfShiftSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "shiftSyncId");
          final int _cursorIndexOfStaffId = CursorUtil.getColumnIndexOrThrow(_cursor, "staffId");
          final int _cursorIndexOfStaffName = CursorUtil.getColumnIndexOrThrow(_cursor, "staffName");
          final int _cursorIndexOfStaffSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "staffSyncId");
          final int _cursorIndexOfCancelledByStaffId = CursorUtil.getColumnIndexOrThrow(_cursor, "cancelledByStaffId");
          final int _cursorIndexOfCancelledAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "cancelledAtMs");
          final int _cursorIndexOfCancelledByStaffSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "cancelledByStaffSyncId");
          final int _cursorIndexOfSyncedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "syncedAtMs");
          final List<OrderEntity> _result = new ArrayList<OrderEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final OrderEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSyncId;
            _tmpSyncId = _cursor.getString(_cursorIndexOfSyncId);
            final int _tmpSubtotal;
            _tmpSubtotal = _cursor.getInt(_cursorIndexOfSubtotal);
            final int _tmpDiscountAmount;
            _tmpDiscountAmount = _cursor.getInt(_cursorIndexOfDiscountAmount);
            final int _tmpTotal;
            _tmpTotal = _cursor.getInt(_cursorIndexOfTotal);
            final DiscountType _tmpDiscountType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfDiscountType);
            _tmpDiscountType = __converters.stringToDiscountType(_tmp);
            final int _tmpDiscountInput;
            _tmpDiscountInput = _cursor.getInt(_cursorIndexOfDiscountInput);
            final String _tmpDiscountCode;
            if (_cursor.isNull(_cursorIndexOfDiscountCode)) {
              _tmpDiscountCode = null;
            } else {
              _tmpDiscountCode = _cursor.getString(_cursorIndexOfDiscountCode);
            }
            final OrderType _tmpOrderType;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfOrderType);
            _tmpOrderType = __converters.stringToOrderType(_tmp_1);
            final PaymentMethod _tmpPaymentMethod;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfPaymentMethod);
            _tmpPaymentMethod = __converters.stringToPaymentMethod(_tmp_2);
            final int _tmpCashReceived;
            _tmpCashReceived = _cursor.getInt(_cursorIndexOfCashReceived);
            final int _tmpChangeAmount;
            _tmpChangeAmount = _cursor.getInt(_cursorIndexOfChangeAmount);
            final OrderStatus _tmpStatus;
            final String _tmp_3;
            _tmp_3 = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.stringToOrderStatus(_tmp_3);
            final long _tmpCreatedAtMs;
            _tmpCreatedAtMs = _cursor.getLong(_cursorIndexOfCreatedAtMs);
            final Long _tmpShiftId;
            if (_cursor.isNull(_cursorIndexOfShiftId)) {
              _tmpShiftId = null;
            } else {
              _tmpShiftId = _cursor.getLong(_cursorIndexOfShiftId);
            }
            final String _tmpShiftSyncId;
            if (_cursor.isNull(_cursorIndexOfShiftSyncId)) {
              _tmpShiftSyncId = null;
            } else {
              _tmpShiftSyncId = _cursor.getString(_cursorIndexOfShiftSyncId);
            }
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
            final String _tmpStaffSyncId;
            if (_cursor.isNull(_cursorIndexOfStaffSyncId)) {
              _tmpStaffSyncId = null;
            } else {
              _tmpStaffSyncId = _cursor.getString(_cursorIndexOfStaffSyncId);
            }
            final Long _tmpCancelledByStaffId;
            if (_cursor.isNull(_cursorIndexOfCancelledByStaffId)) {
              _tmpCancelledByStaffId = null;
            } else {
              _tmpCancelledByStaffId = _cursor.getLong(_cursorIndexOfCancelledByStaffId);
            }
            final Long _tmpCancelledAtMs;
            if (_cursor.isNull(_cursorIndexOfCancelledAtMs)) {
              _tmpCancelledAtMs = null;
            } else {
              _tmpCancelledAtMs = _cursor.getLong(_cursorIndexOfCancelledAtMs);
            }
            final String _tmpCancelledByStaffSyncId;
            if (_cursor.isNull(_cursorIndexOfCancelledByStaffSyncId)) {
              _tmpCancelledByStaffSyncId = null;
            } else {
              _tmpCancelledByStaffSyncId = _cursor.getString(_cursorIndexOfCancelledByStaffSyncId);
            }
            final Long _tmpSyncedAtMs;
            if (_cursor.isNull(_cursorIndexOfSyncedAtMs)) {
              _tmpSyncedAtMs = null;
            } else {
              _tmpSyncedAtMs = _cursor.getLong(_cursorIndexOfSyncedAtMs);
            }
            _item = new OrderEntity(_tmpId,_tmpSyncId,_tmpSubtotal,_tmpDiscountAmount,_tmpTotal,_tmpDiscountType,_tmpDiscountInput,_tmpDiscountCode,_tmpOrderType,_tmpPaymentMethod,_tmpCashReceived,_tmpChangeAmount,_tmpStatus,_tmpCreatedAtMs,_tmpShiftId,_tmpShiftSyncId,_tmpStaffId,_tmpStaffName,_tmpStaffSyncId,_tmpCancelledByStaffId,_tmpCancelledAtMs,_tmpCancelledByStaffSyncId,_tmpSyncedAtMs);
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
  public Object orderById(final long orderId, final Continuation<? super OrderEntity> $completion) {
    final String _sql = "SELECT * FROM orders WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, orderId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<OrderEntity>() {
      @Override
      @Nullable
      public OrderEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "syncId");
          final int _cursorIndexOfSubtotal = CursorUtil.getColumnIndexOrThrow(_cursor, "subtotal");
          final int _cursorIndexOfDiscountAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "discountAmount");
          final int _cursorIndexOfTotal = CursorUtil.getColumnIndexOrThrow(_cursor, "total");
          final int _cursorIndexOfDiscountType = CursorUtil.getColumnIndexOrThrow(_cursor, "discountType");
          final int _cursorIndexOfDiscountInput = CursorUtil.getColumnIndexOrThrow(_cursor, "discountInput");
          final int _cursorIndexOfDiscountCode = CursorUtil.getColumnIndexOrThrow(_cursor, "discountCode");
          final int _cursorIndexOfOrderType = CursorUtil.getColumnIndexOrThrow(_cursor, "orderType");
          final int _cursorIndexOfPaymentMethod = CursorUtil.getColumnIndexOrThrow(_cursor, "paymentMethod");
          final int _cursorIndexOfCashReceived = CursorUtil.getColumnIndexOrThrow(_cursor, "cashReceived");
          final int _cursorIndexOfChangeAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "changeAmount");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfCreatedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAtMs");
          final int _cursorIndexOfShiftId = CursorUtil.getColumnIndexOrThrow(_cursor, "shiftId");
          final int _cursorIndexOfShiftSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "shiftSyncId");
          final int _cursorIndexOfStaffId = CursorUtil.getColumnIndexOrThrow(_cursor, "staffId");
          final int _cursorIndexOfStaffName = CursorUtil.getColumnIndexOrThrow(_cursor, "staffName");
          final int _cursorIndexOfStaffSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "staffSyncId");
          final int _cursorIndexOfCancelledByStaffId = CursorUtil.getColumnIndexOrThrow(_cursor, "cancelledByStaffId");
          final int _cursorIndexOfCancelledAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "cancelledAtMs");
          final int _cursorIndexOfCancelledByStaffSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "cancelledByStaffSyncId");
          final int _cursorIndexOfSyncedAtMs = CursorUtil.getColumnIndexOrThrow(_cursor, "syncedAtMs");
          final OrderEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSyncId;
            _tmpSyncId = _cursor.getString(_cursorIndexOfSyncId);
            final int _tmpSubtotal;
            _tmpSubtotal = _cursor.getInt(_cursorIndexOfSubtotal);
            final int _tmpDiscountAmount;
            _tmpDiscountAmount = _cursor.getInt(_cursorIndexOfDiscountAmount);
            final int _tmpTotal;
            _tmpTotal = _cursor.getInt(_cursorIndexOfTotal);
            final DiscountType _tmpDiscountType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfDiscountType);
            _tmpDiscountType = __converters.stringToDiscountType(_tmp);
            final int _tmpDiscountInput;
            _tmpDiscountInput = _cursor.getInt(_cursorIndexOfDiscountInput);
            final String _tmpDiscountCode;
            if (_cursor.isNull(_cursorIndexOfDiscountCode)) {
              _tmpDiscountCode = null;
            } else {
              _tmpDiscountCode = _cursor.getString(_cursorIndexOfDiscountCode);
            }
            final OrderType _tmpOrderType;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfOrderType);
            _tmpOrderType = __converters.stringToOrderType(_tmp_1);
            final PaymentMethod _tmpPaymentMethod;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfPaymentMethod);
            _tmpPaymentMethod = __converters.stringToPaymentMethod(_tmp_2);
            final int _tmpCashReceived;
            _tmpCashReceived = _cursor.getInt(_cursorIndexOfCashReceived);
            final int _tmpChangeAmount;
            _tmpChangeAmount = _cursor.getInt(_cursorIndexOfChangeAmount);
            final OrderStatus _tmpStatus;
            final String _tmp_3;
            _tmp_3 = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __converters.stringToOrderStatus(_tmp_3);
            final long _tmpCreatedAtMs;
            _tmpCreatedAtMs = _cursor.getLong(_cursorIndexOfCreatedAtMs);
            final Long _tmpShiftId;
            if (_cursor.isNull(_cursorIndexOfShiftId)) {
              _tmpShiftId = null;
            } else {
              _tmpShiftId = _cursor.getLong(_cursorIndexOfShiftId);
            }
            final String _tmpShiftSyncId;
            if (_cursor.isNull(_cursorIndexOfShiftSyncId)) {
              _tmpShiftSyncId = null;
            } else {
              _tmpShiftSyncId = _cursor.getString(_cursorIndexOfShiftSyncId);
            }
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
            final String _tmpStaffSyncId;
            if (_cursor.isNull(_cursorIndexOfStaffSyncId)) {
              _tmpStaffSyncId = null;
            } else {
              _tmpStaffSyncId = _cursor.getString(_cursorIndexOfStaffSyncId);
            }
            final Long _tmpCancelledByStaffId;
            if (_cursor.isNull(_cursorIndexOfCancelledByStaffId)) {
              _tmpCancelledByStaffId = null;
            } else {
              _tmpCancelledByStaffId = _cursor.getLong(_cursorIndexOfCancelledByStaffId);
            }
            final Long _tmpCancelledAtMs;
            if (_cursor.isNull(_cursorIndexOfCancelledAtMs)) {
              _tmpCancelledAtMs = null;
            } else {
              _tmpCancelledAtMs = _cursor.getLong(_cursorIndexOfCancelledAtMs);
            }
            final String _tmpCancelledByStaffSyncId;
            if (_cursor.isNull(_cursorIndexOfCancelledByStaffSyncId)) {
              _tmpCancelledByStaffSyncId = null;
            } else {
              _tmpCancelledByStaffSyncId = _cursor.getString(_cursorIndexOfCancelledByStaffSyncId);
            }
            final Long _tmpSyncedAtMs;
            if (_cursor.isNull(_cursorIndexOfSyncedAtMs)) {
              _tmpSyncedAtMs = null;
            } else {
              _tmpSyncedAtMs = _cursor.getLong(_cursorIndexOfSyncedAtMs);
            }
            _result = new OrderEntity(_tmpId,_tmpSyncId,_tmpSubtotal,_tmpDiscountAmount,_tmpTotal,_tmpDiscountType,_tmpDiscountInput,_tmpDiscountCode,_tmpOrderType,_tmpPaymentMethod,_tmpCashReceived,_tmpChangeAmount,_tmpStatus,_tmpCreatedAtMs,_tmpShiftId,_tmpShiftSyncId,_tmpStaffId,_tmpStaffName,_tmpStaffSyncId,_tmpCancelledByStaffId,_tmpCancelledAtMs,_tmpCancelledByStaffSyncId,_tmpSyncedAtMs);
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
  public Object itemsOf(final long orderId,
      final Continuation<? super List<OrderItemEntity>> $completion) {
    final String _sql = "SELECT * FROM order_items WHERE orderId = ? ORDER BY id";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, orderId);
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

  @Override
  public Object revenueBetween(final long fromMs, final long toMs,
      final Continuation<? super Integer> $completion) {
    final String _sql = "\n"
            + "        SELECT COALESCE(SUM(total), 0) FROM orders\n"
            + "        WHERE status = 'PAID' AND createdAtMs BETWEEN ? AND ?\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, fromMs);
    _argIndex = 2;
    _statement.bindLong(_argIndex, toMs);
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
  public Object paidCountBetween(final long fromMs, final long toMs,
      final Continuation<? super Integer> $completion) {
    final String _sql = "\n"
            + "        SELECT COUNT(*) FROM orders\n"
            + "        WHERE status = 'PAID' AND createdAtMs BETWEEN ? AND ?\n"
            + "        ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, fromMs);
    _argIndex = 2;
    _statement.bindLong(_argIndex, toMs);
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
