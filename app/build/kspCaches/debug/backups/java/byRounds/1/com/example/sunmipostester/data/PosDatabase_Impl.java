package com.example.sunmipostester.data;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class PosDatabase_Impl extends PosDatabase {
  private volatile ProductDao _productDao;

  private volatile OrderDao _orderDao;

  private volatile ShiftDao _shiftDao;

  private volatile ReportDao _reportDao;

  private volatile StaffDao _staffDao;

  private volatile DiscountCodeDao _discountCodeDao;

  private volatile TableDraftDao _tableDraftDao;

  private volatile CashExpenseDao _cashExpenseDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(7) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `products` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `syncId` TEXT NOT NULL, `name` TEXT NOT NULL, `price` INTEGER NOT NULL, `category` TEXT NOT NULL, `active` INTEGER NOT NULL, `updatedAtMs` INTEGER NOT NULL)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_products_syncId` ON `products` (`syncId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `orders` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `syncId` TEXT NOT NULL, `subtotal` INTEGER NOT NULL, `discountAmount` INTEGER NOT NULL, `total` INTEGER NOT NULL, `discountType` TEXT NOT NULL, `discountInput` INTEGER NOT NULL, `discountCode` TEXT, `orderType` TEXT NOT NULL, `paymentMethod` TEXT NOT NULL, `cashReceived` INTEGER NOT NULL, `changeAmount` INTEGER NOT NULL, `status` TEXT NOT NULL, `createdAtMs` INTEGER NOT NULL, `shiftId` INTEGER, `shiftSyncId` TEXT, `staffId` INTEGER, `staffName` TEXT, `staffSyncId` TEXT, `cancelledByStaffId` INTEGER, `cancelledAtMs` INTEGER, `cancelledByStaffSyncId` TEXT, `syncedAtMs` INTEGER)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_orders_shiftId` ON `orders` (`shiftId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_orders_staffId` ON `orders` (`staffId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_orders_createdAtMs` ON `orders` (`createdAtMs`)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_orders_syncId` ON `orders` (`syncId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `order_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `syncId` TEXT NOT NULL, `orderId` INTEGER NOT NULL, `productId` INTEGER NOT NULL, `productSyncId` TEXT, `productName` TEXT NOT NULL, `unitPrice` INTEGER NOT NULL, `quantity` INTEGER NOT NULL, FOREIGN KEY(`orderId`) REFERENCES `orders`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_order_items_orderId` ON `order_items` (`orderId`)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_order_items_syncId` ON `order_items` (`syncId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `shifts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `syncId` TEXT NOT NULL, `openedAtMs` INTEGER NOT NULL, `closedAtMs` INTEGER, `openingCash` INTEGER NOT NULL, `countedCash` INTEGER, `expectedCash` INTEGER, `cashDifference` INTEGER, `cashBreakdown` TEXT, `status` TEXT NOT NULL, `staffId` INTEGER, `staffName` TEXT, `closedByStaffId` INTEGER, `staffSyncId` TEXT, `closedByStaffSyncId` TEXT, `syncedAtMs` INTEGER)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_shifts_staffId` ON `shifts` (`staffId`)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_shifts_syncId` ON `shifts` (`syncId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `staff` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `syncId` TEXT NOT NULL, `name` TEXT NOT NULL, `role` TEXT NOT NULL, `pinHash` TEXT NOT NULL, `pinSalt` TEXT NOT NULL, `active` INTEGER NOT NULL, `createdAtMs` INTEGER NOT NULL, `updatedAtMs` INTEGER NOT NULL)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_staff_syncId` ON `staff` (`syncId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `discount_codes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `syncId` TEXT NOT NULL, `code` TEXT NOT NULL, `campaignSyncId` TEXT NOT NULL, `campaignName` TEXT NOT NULL, `valueType` TEXT NOT NULL, `value` INTEGER NOT NULL, `consumed` INTEGER NOT NULL, `consumedByOrderSyncId` TEXT, `consumedAtMs` INTEGER)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_discount_codes_syncId` ON `discount_codes` (`syncId`)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_discount_codes_code` ON `discount_codes` (`code`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `table_drafts` (`tableName` TEXT NOT NULL, `updatedAtMs` INTEGER NOT NULL, PRIMARY KEY(`tableName`))");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_table_drafts_updatedAtMs` ON `table_drafts` (`updatedAtMs`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `table_draft_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `tableName` TEXT NOT NULL, `productId` INTEGER NOT NULL, `productSyncId` TEXT NOT NULL, `productName` TEXT NOT NULL, `category` TEXT NOT NULL, `unitPrice` INTEGER NOT NULL, `quantity` INTEGER NOT NULL, FOREIGN KEY(`tableName`) REFERENCES `table_drafts`(`tableName`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_table_draft_items_tableName` ON `table_draft_items` (`tableName`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `cash_expenses` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `shiftId` INTEGER NOT NULL, `amount` INTEGER NOT NULL, `description` TEXT NOT NULL, `createdAtMs` INTEGER NOT NULL, `staffId` INTEGER, `staffName` TEXT)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cash_expenses_shiftId` ON `cash_expenses` (`shiftId`)");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_cash_expenses_createdAtMs` ON `cash_expenses` (`createdAtMs`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'a2db3f3e4093ce3dac75be3bd9a3cc2d')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `products`");
        db.execSQL("DROP TABLE IF EXISTS `orders`");
        db.execSQL("DROP TABLE IF EXISTS `order_items`");
        db.execSQL("DROP TABLE IF EXISTS `shifts`");
        db.execSQL("DROP TABLE IF EXISTS `staff`");
        db.execSQL("DROP TABLE IF EXISTS `discount_codes`");
        db.execSQL("DROP TABLE IF EXISTS `table_drafts`");
        db.execSQL("DROP TABLE IF EXISTS `table_draft_items`");
        db.execSQL("DROP TABLE IF EXISTS `cash_expenses`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsProducts = new HashMap<String, TableInfo.Column>(7);
        _columnsProducts.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("syncId", new TableInfo.Column("syncId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("price", new TableInfo.Column("price", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("category", new TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("active", new TableInfo.Column("active", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("updatedAtMs", new TableInfo.Column("updatedAtMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysProducts = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesProducts = new HashSet<TableInfo.Index>(1);
        _indicesProducts.add(new TableInfo.Index("index_products_syncId", true, Arrays.asList("syncId"), Arrays.asList("ASC")));
        final TableInfo _infoProducts = new TableInfo("products", _columnsProducts, _foreignKeysProducts, _indicesProducts);
        final TableInfo _existingProducts = TableInfo.read(db, "products");
        if (!_infoProducts.equals(_existingProducts)) {
          return new RoomOpenHelper.ValidationResult(false, "products(com.example.sunmipostester.data.ProductEntity).\n"
                  + " Expected:\n" + _infoProducts + "\n"
                  + " Found:\n" + _existingProducts);
        }
        final HashMap<String, TableInfo.Column> _columnsOrders = new HashMap<String, TableInfo.Column>(23);
        _columnsOrders.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOrders.put("syncId", new TableInfo.Column("syncId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOrders.put("subtotal", new TableInfo.Column("subtotal", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOrders.put("discountAmount", new TableInfo.Column("discountAmount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOrders.put("total", new TableInfo.Column("total", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOrders.put("discountType", new TableInfo.Column("discountType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOrders.put("discountInput", new TableInfo.Column("discountInput", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOrders.put("discountCode", new TableInfo.Column("discountCode", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOrders.put("orderType", new TableInfo.Column("orderType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOrders.put("paymentMethod", new TableInfo.Column("paymentMethod", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOrders.put("cashReceived", new TableInfo.Column("cashReceived", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOrders.put("changeAmount", new TableInfo.Column("changeAmount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOrders.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOrders.put("createdAtMs", new TableInfo.Column("createdAtMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOrders.put("shiftId", new TableInfo.Column("shiftId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOrders.put("shiftSyncId", new TableInfo.Column("shiftSyncId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOrders.put("staffId", new TableInfo.Column("staffId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOrders.put("staffName", new TableInfo.Column("staffName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOrders.put("staffSyncId", new TableInfo.Column("staffSyncId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOrders.put("cancelledByStaffId", new TableInfo.Column("cancelledByStaffId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOrders.put("cancelledAtMs", new TableInfo.Column("cancelledAtMs", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOrders.put("cancelledByStaffSyncId", new TableInfo.Column("cancelledByStaffSyncId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOrders.put("syncedAtMs", new TableInfo.Column("syncedAtMs", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysOrders = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesOrders = new HashSet<TableInfo.Index>(4);
        _indicesOrders.add(new TableInfo.Index("index_orders_shiftId", false, Arrays.asList("shiftId"), Arrays.asList("ASC")));
        _indicesOrders.add(new TableInfo.Index("index_orders_staffId", false, Arrays.asList("staffId"), Arrays.asList("ASC")));
        _indicesOrders.add(new TableInfo.Index("index_orders_createdAtMs", false, Arrays.asList("createdAtMs"), Arrays.asList("ASC")));
        _indicesOrders.add(new TableInfo.Index("index_orders_syncId", true, Arrays.asList("syncId"), Arrays.asList("ASC")));
        final TableInfo _infoOrders = new TableInfo("orders", _columnsOrders, _foreignKeysOrders, _indicesOrders);
        final TableInfo _existingOrders = TableInfo.read(db, "orders");
        if (!_infoOrders.equals(_existingOrders)) {
          return new RoomOpenHelper.ValidationResult(false, "orders(com.example.sunmipostester.data.OrderEntity).\n"
                  + " Expected:\n" + _infoOrders + "\n"
                  + " Found:\n" + _existingOrders);
        }
        final HashMap<String, TableInfo.Column> _columnsOrderItems = new HashMap<String, TableInfo.Column>(8);
        _columnsOrderItems.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOrderItems.put("syncId", new TableInfo.Column("syncId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOrderItems.put("orderId", new TableInfo.Column("orderId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOrderItems.put("productId", new TableInfo.Column("productId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOrderItems.put("productSyncId", new TableInfo.Column("productSyncId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOrderItems.put("productName", new TableInfo.Column("productName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOrderItems.put("unitPrice", new TableInfo.Column("unitPrice", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOrderItems.put("quantity", new TableInfo.Column("quantity", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysOrderItems = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysOrderItems.add(new TableInfo.ForeignKey("orders", "CASCADE", "NO ACTION", Arrays.asList("orderId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesOrderItems = new HashSet<TableInfo.Index>(2);
        _indicesOrderItems.add(new TableInfo.Index("index_order_items_orderId", false, Arrays.asList("orderId"), Arrays.asList("ASC")));
        _indicesOrderItems.add(new TableInfo.Index("index_order_items_syncId", true, Arrays.asList("syncId"), Arrays.asList("ASC")));
        final TableInfo _infoOrderItems = new TableInfo("order_items", _columnsOrderItems, _foreignKeysOrderItems, _indicesOrderItems);
        final TableInfo _existingOrderItems = TableInfo.read(db, "order_items");
        if (!_infoOrderItems.equals(_existingOrderItems)) {
          return new RoomOpenHelper.ValidationResult(false, "order_items(com.example.sunmipostester.data.OrderItemEntity).\n"
                  + " Expected:\n" + _infoOrderItems + "\n"
                  + " Found:\n" + _existingOrderItems);
        }
        final HashMap<String, TableInfo.Column> _columnsShifts = new HashMap<String, TableInfo.Column>(16);
        _columnsShifts.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsShifts.put("syncId", new TableInfo.Column("syncId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsShifts.put("openedAtMs", new TableInfo.Column("openedAtMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsShifts.put("closedAtMs", new TableInfo.Column("closedAtMs", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsShifts.put("openingCash", new TableInfo.Column("openingCash", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsShifts.put("countedCash", new TableInfo.Column("countedCash", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsShifts.put("expectedCash", new TableInfo.Column("expectedCash", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsShifts.put("cashDifference", new TableInfo.Column("cashDifference", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsShifts.put("cashBreakdown", new TableInfo.Column("cashBreakdown", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsShifts.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsShifts.put("staffId", new TableInfo.Column("staffId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsShifts.put("staffName", new TableInfo.Column("staffName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsShifts.put("closedByStaffId", new TableInfo.Column("closedByStaffId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsShifts.put("staffSyncId", new TableInfo.Column("staffSyncId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsShifts.put("closedByStaffSyncId", new TableInfo.Column("closedByStaffSyncId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsShifts.put("syncedAtMs", new TableInfo.Column("syncedAtMs", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysShifts = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesShifts = new HashSet<TableInfo.Index>(2);
        _indicesShifts.add(new TableInfo.Index("index_shifts_staffId", false, Arrays.asList("staffId"), Arrays.asList("ASC")));
        _indicesShifts.add(new TableInfo.Index("index_shifts_syncId", true, Arrays.asList("syncId"), Arrays.asList("ASC")));
        final TableInfo _infoShifts = new TableInfo("shifts", _columnsShifts, _foreignKeysShifts, _indicesShifts);
        final TableInfo _existingShifts = TableInfo.read(db, "shifts");
        if (!_infoShifts.equals(_existingShifts)) {
          return new RoomOpenHelper.ValidationResult(false, "shifts(com.example.sunmipostester.data.ShiftEntity).\n"
                  + " Expected:\n" + _infoShifts + "\n"
                  + " Found:\n" + _existingShifts);
        }
        final HashMap<String, TableInfo.Column> _columnsStaff = new HashMap<String, TableInfo.Column>(9);
        _columnsStaff.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStaff.put("syncId", new TableInfo.Column("syncId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStaff.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStaff.put("role", new TableInfo.Column("role", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStaff.put("pinHash", new TableInfo.Column("pinHash", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStaff.put("pinSalt", new TableInfo.Column("pinSalt", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStaff.put("active", new TableInfo.Column("active", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStaff.put("createdAtMs", new TableInfo.Column("createdAtMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsStaff.put("updatedAtMs", new TableInfo.Column("updatedAtMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysStaff = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesStaff = new HashSet<TableInfo.Index>(1);
        _indicesStaff.add(new TableInfo.Index("index_staff_syncId", true, Arrays.asList("syncId"), Arrays.asList("ASC")));
        final TableInfo _infoStaff = new TableInfo("staff", _columnsStaff, _foreignKeysStaff, _indicesStaff);
        final TableInfo _existingStaff = TableInfo.read(db, "staff");
        if (!_infoStaff.equals(_existingStaff)) {
          return new RoomOpenHelper.ValidationResult(false, "staff(com.example.sunmipostester.data.StaffEntity).\n"
                  + " Expected:\n" + _infoStaff + "\n"
                  + " Found:\n" + _existingStaff);
        }
        final HashMap<String, TableInfo.Column> _columnsDiscountCodes = new HashMap<String, TableInfo.Column>(10);
        _columnsDiscountCodes.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDiscountCodes.put("syncId", new TableInfo.Column("syncId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDiscountCodes.put("code", new TableInfo.Column("code", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDiscountCodes.put("campaignSyncId", new TableInfo.Column("campaignSyncId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDiscountCodes.put("campaignName", new TableInfo.Column("campaignName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDiscountCodes.put("valueType", new TableInfo.Column("valueType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDiscountCodes.put("value", new TableInfo.Column("value", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDiscountCodes.put("consumed", new TableInfo.Column("consumed", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDiscountCodes.put("consumedByOrderSyncId", new TableInfo.Column("consumedByOrderSyncId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsDiscountCodes.put("consumedAtMs", new TableInfo.Column("consumedAtMs", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysDiscountCodes = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesDiscountCodes = new HashSet<TableInfo.Index>(2);
        _indicesDiscountCodes.add(new TableInfo.Index("index_discount_codes_syncId", true, Arrays.asList("syncId"), Arrays.asList("ASC")));
        _indicesDiscountCodes.add(new TableInfo.Index("index_discount_codes_code", true, Arrays.asList("code"), Arrays.asList("ASC")));
        final TableInfo _infoDiscountCodes = new TableInfo("discount_codes", _columnsDiscountCodes, _foreignKeysDiscountCodes, _indicesDiscountCodes);
        final TableInfo _existingDiscountCodes = TableInfo.read(db, "discount_codes");
        if (!_infoDiscountCodes.equals(_existingDiscountCodes)) {
          return new RoomOpenHelper.ValidationResult(false, "discount_codes(com.example.sunmipostester.data.DiscountCodeEntity).\n"
                  + " Expected:\n" + _infoDiscountCodes + "\n"
                  + " Found:\n" + _existingDiscountCodes);
        }
        final HashMap<String, TableInfo.Column> _columnsTableDrafts = new HashMap<String, TableInfo.Column>(2);
        _columnsTableDrafts.put("tableName", new TableInfo.Column("tableName", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTableDrafts.put("updatedAtMs", new TableInfo.Column("updatedAtMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTableDrafts = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesTableDrafts = new HashSet<TableInfo.Index>(1);
        _indicesTableDrafts.add(new TableInfo.Index("index_table_drafts_updatedAtMs", false, Arrays.asList("updatedAtMs"), Arrays.asList("ASC")));
        final TableInfo _infoTableDrafts = new TableInfo("table_drafts", _columnsTableDrafts, _foreignKeysTableDrafts, _indicesTableDrafts);
        final TableInfo _existingTableDrafts = TableInfo.read(db, "table_drafts");
        if (!_infoTableDrafts.equals(_existingTableDrafts)) {
          return new RoomOpenHelper.ValidationResult(false, "table_drafts(com.example.sunmipostester.data.TableDraftEntity).\n"
                  + " Expected:\n" + _infoTableDrafts + "\n"
                  + " Found:\n" + _existingTableDrafts);
        }
        final HashMap<String, TableInfo.Column> _columnsTableDraftItems = new HashMap<String, TableInfo.Column>(8);
        _columnsTableDraftItems.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTableDraftItems.put("tableName", new TableInfo.Column("tableName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTableDraftItems.put("productId", new TableInfo.Column("productId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTableDraftItems.put("productSyncId", new TableInfo.Column("productSyncId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTableDraftItems.put("productName", new TableInfo.Column("productName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTableDraftItems.put("category", new TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTableDraftItems.put("unitPrice", new TableInfo.Column("unitPrice", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTableDraftItems.put("quantity", new TableInfo.Column("quantity", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTableDraftItems = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysTableDraftItems.add(new TableInfo.ForeignKey("table_drafts", "CASCADE", "NO ACTION", Arrays.asList("tableName"), Arrays.asList("tableName")));
        final HashSet<TableInfo.Index> _indicesTableDraftItems = new HashSet<TableInfo.Index>(1);
        _indicesTableDraftItems.add(new TableInfo.Index("index_table_draft_items_tableName", false, Arrays.asList("tableName"), Arrays.asList("ASC")));
        final TableInfo _infoTableDraftItems = new TableInfo("table_draft_items", _columnsTableDraftItems, _foreignKeysTableDraftItems, _indicesTableDraftItems);
        final TableInfo _existingTableDraftItems = TableInfo.read(db, "table_draft_items");
        if (!_infoTableDraftItems.equals(_existingTableDraftItems)) {
          return new RoomOpenHelper.ValidationResult(false, "table_draft_items(com.example.sunmipostester.data.TableDraftItemEntity).\n"
                  + " Expected:\n" + _infoTableDraftItems + "\n"
                  + " Found:\n" + _existingTableDraftItems);
        }
        final HashMap<String, TableInfo.Column> _columnsCashExpenses = new HashMap<String, TableInfo.Column>(7);
        _columnsCashExpenses.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCashExpenses.put("shiftId", new TableInfo.Column("shiftId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCashExpenses.put("amount", new TableInfo.Column("amount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCashExpenses.put("description", new TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCashExpenses.put("createdAtMs", new TableInfo.Column("createdAtMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCashExpenses.put("staffId", new TableInfo.Column("staffId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCashExpenses.put("staffName", new TableInfo.Column("staffName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCashExpenses = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCashExpenses = new HashSet<TableInfo.Index>(2);
        _indicesCashExpenses.add(new TableInfo.Index("index_cash_expenses_shiftId", false, Arrays.asList("shiftId"), Arrays.asList("ASC")));
        _indicesCashExpenses.add(new TableInfo.Index("index_cash_expenses_createdAtMs", false, Arrays.asList("createdAtMs"), Arrays.asList("ASC")));
        final TableInfo _infoCashExpenses = new TableInfo("cash_expenses", _columnsCashExpenses, _foreignKeysCashExpenses, _indicesCashExpenses);
        final TableInfo _existingCashExpenses = TableInfo.read(db, "cash_expenses");
        if (!_infoCashExpenses.equals(_existingCashExpenses)) {
          return new RoomOpenHelper.ValidationResult(false, "cash_expenses(com.example.sunmipostester.data.CashExpenseEntity).\n"
                  + " Expected:\n" + _infoCashExpenses + "\n"
                  + " Found:\n" + _existingCashExpenses);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "a2db3f3e4093ce3dac75be3bd9a3cc2d", "8d00a73790e67bc93e0dd72206774368");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "products","orders","order_items","shifts","staff","discount_codes","table_drafts","table_draft_items","cash_expenses");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `products`");
      _db.execSQL("DELETE FROM `orders`");
      _db.execSQL("DELETE FROM `order_items`");
      _db.execSQL("DELETE FROM `shifts`");
      _db.execSQL("DELETE FROM `staff`");
      _db.execSQL("DELETE FROM `discount_codes`");
      _db.execSQL("DELETE FROM `table_drafts`");
      _db.execSQL("DELETE FROM `table_draft_items`");
      _db.execSQL("DELETE FROM `cash_expenses`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(ProductDao.class, ProductDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(OrderDao.class, OrderDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ShiftDao.class, ShiftDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ReportDao.class, ReportDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(StaffDao.class, StaffDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(DiscountCodeDao.class, DiscountCodeDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(TableDraftDao.class, TableDraftDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(CashExpenseDao.class, CashExpenseDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public ProductDao productDao() {
    if (_productDao != null) {
      return _productDao;
    } else {
      synchronized(this) {
        if(_productDao == null) {
          _productDao = new ProductDao_Impl(this);
        }
        return _productDao;
      }
    }
  }

  @Override
  public OrderDao orderDao() {
    if (_orderDao != null) {
      return _orderDao;
    } else {
      synchronized(this) {
        if(_orderDao == null) {
          _orderDao = new OrderDao_Impl(this);
        }
        return _orderDao;
      }
    }
  }

  @Override
  public ShiftDao shiftDao() {
    if (_shiftDao != null) {
      return _shiftDao;
    } else {
      synchronized(this) {
        if(_shiftDao == null) {
          _shiftDao = new ShiftDao_Impl(this);
        }
        return _shiftDao;
      }
    }
  }

  @Override
  public ReportDao reportDao() {
    if (_reportDao != null) {
      return _reportDao;
    } else {
      synchronized(this) {
        if(_reportDao == null) {
          _reportDao = new ReportDao_Impl(this);
        }
        return _reportDao;
      }
    }
  }

  @Override
  public StaffDao staffDao() {
    if (_staffDao != null) {
      return _staffDao;
    } else {
      synchronized(this) {
        if(_staffDao == null) {
          _staffDao = new StaffDao_Impl(this);
        }
        return _staffDao;
      }
    }
  }

  @Override
  public DiscountCodeDao discountCodeDao() {
    if (_discountCodeDao != null) {
      return _discountCodeDao;
    } else {
      synchronized(this) {
        if(_discountCodeDao == null) {
          _discountCodeDao = new DiscountCodeDao_Impl(this);
        }
        return _discountCodeDao;
      }
    }
  }

  @Override
  public TableDraftDao tableDraftDao() {
    if (_tableDraftDao != null) {
      return _tableDraftDao;
    } else {
      synchronized(this) {
        if(_tableDraftDao == null) {
          _tableDraftDao = new TableDraftDao_Impl(this);
        }
        return _tableDraftDao;
      }
    }
  }

  @Override
  public CashExpenseDao cashExpenseDao() {
    if (_cashExpenseDao != null) {
      return _cashExpenseDao;
    } else {
      synchronized(this) {
        if(_cashExpenseDao == null) {
          _cashExpenseDao = new CashExpenseDao_Impl(this);
        }
        return _cashExpenseDao;
      }
    }
  }
}
