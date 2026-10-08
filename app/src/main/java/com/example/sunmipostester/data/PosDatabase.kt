package com.example.sunmipostester.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.sunmipostester.common.DiscountType

/** Room type converters for the enums stored as text. */
class Converters {
    @TypeConverter
    fun paymentMethodToString(value: PaymentMethod): String = value.name

    @TypeConverter
    fun stringToPaymentMethod(value: String): PaymentMethod = PaymentMethod.valueOf(value)

    @TypeConverter
    fun orderStatusToString(value: OrderStatus): String = value.name

    @TypeConverter
    fun stringToOrderStatus(value: String): OrderStatus = OrderStatus.valueOf(value)

    @TypeConverter
    fun shiftStatusToString(value: ShiftStatus): String = value.name

    @TypeConverter
    fun stringToShiftStatus(value: String): ShiftStatus = ShiftStatus.valueOf(value)

    @TypeConverter
    fun staffRoleToString(value: StaffRole): String = value.name

    @TypeConverter
    fun stringToStaffRole(value: String): StaffRole = StaffRole.valueOf(value)

    @TypeConverter
    fun orderTypeToString(value: OrderType): String = value.name

    @TypeConverter
    fun stringToOrderType(value: String): OrderType = OrderType.valueOf(value)

    @TypeConverter
    fun discountTypeToString(value: DiscountType): String = value.name

    @TypeConverter
    fun stringToDiscountType(value: String): DiscountType = DiscountType.valueOf(value)
}

@Database(
    entities = [
        ProductEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        ShiftEntity::class,
        StaffEntity::class,
        DiscountCodeEntity::class,
        TableDraftEntity::class,
        TableDraftItemEntity::class,
        CashExpenseEntity::class
    ],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PosDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao
    abstract fun shiftDao(): ShiftDao
    abstract fun reportDao(): ReportDao
    abstract fun staffDao(): StaffDao
    abstract fun discountCodeDao(): DiscountCodeDao
    abstract fun tableDraftDao(): TableDraftDao
    abstract fun cashExpenseDao(): CashExpenseDao

    companion object {
        @Volatile
        private var instance: PosDatabase? = null

        fun get(context: Context): PosDatabase =
            instance ?: synchronized(this) {
                instance ?: build(context).also { instance = it }
            }

        private fun build(context: Context): PosDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                PosDatabase::class.java,
                "pos.db"
            )
                .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                // Unsupported upgrade paths remain destructive; add explicit
                // migrations before deploying any further schema changes.
                .fallbackToDestructiveMigration()
                .build()

        private val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `table_drafts` (`tableName` TEXT NOT NULL, `updatedAtMs` INTEGER NOT NULL, PRIMARY KEY(`tableName`))"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_table_drafts_updatedAtMs` ON `table_drafts` (`updatedAtMs`)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `table_draft_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `tableName` TEXT NOT NULL, `productId` INTEGER NOT NULL, `productSyncId` TEXT NOT NULL, `productName` TEXT NOT NULL, `category` TEXT NOT NULL, `unitPrice` INTEGER NOT NULL, `quantity` INTEGER NOT NULL, FOREIGN KEY(`tableName`) REFERENCES `table_drafts`(`tableName`) ON UPDATE NO ACTION ON DELETE CASCADE )"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_table_draft_items_tableName` ON `table_draft_items` (`tableName`)"
                )
            }
        }

        private val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_orders_createdAtMs` ON `orders` (`createdAtMs`)")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `cash_expenses` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `shiftId` INTEGER NOT NULL, `amount` INTEGER NOT NULL, `description` TEXT NOT NULL, `createdAtMs` INTEGER NOT NULL, `staffId` INTEGER, `staffName` TEXT)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_cash_expenses_shiftId` ON `cash_expenses` (`shiftId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_cash_expenses_createdAtMs` ON `cash_expenses` (`createdAtMs`)")
            }
        }

        /** Repairs early v6 installs whose cash_expenses table had an undeclared FK. */
        private val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS `cash_expenses_rebuild`")
                db.execSQL(
                    "CREATE TABLE `cash_expenses_rebuild` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `shiftId` INTEGER NOT NULL, `amount` INTEGER NOT NULL, `description` TEXT NOT NULL, `createdAtMs` INTEGER NOT NULL, `staffId` INTEGER, `staffName` TEXT)"
                )
                db.execSQL(
                    "INSERT INTO `cash_expenses_rebuild` (`id`, `shiftId`, `amount`, `description`, `createdAtMs`, `staffId`, `staffName`) SELECT `id`, `shiftId`, `amount`, `description`, `createdAtMs`, `staffId`, `staffName` FROM `cash_expenses`"
                )
                db.execSQL("DROP TABLE `cash_expenses`")
                db.execSQL("ALTER TABLE `cash_expenses_rebuild` RENAME TO `cash_expenses`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_cash_expenses_shiftId` ON `cash_expenses` (`shiftId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_cash_expenses_createdAtMs` ON `cash_expenses` (`createdAtMs`)")
            }
        }
    }
}
