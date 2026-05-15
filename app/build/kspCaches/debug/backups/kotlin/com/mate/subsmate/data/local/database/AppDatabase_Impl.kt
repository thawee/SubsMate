package com.mate.subsmate.`data`.local.database

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.mate.subsmate.`data`.local.dao.PaymentDao
import com.mate.subsmate.`data`.local.dao.PaymentDao_Impl
import com.mate.subsmate.`data`.local.dao.SubscriptionDao
import com.mate.subsmate.`data`.local.dao.SubscriptionDao_Impl
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDatabase_Impl : AppDatabase() {
  private val _subscriptionDao: Lazy<SubscriptionDao> = lazy {
    SubscriptionDao_Impl(this)
  }

  private val _paymentDao: Lazy<PaymentDao> = lazy {
    PaymentDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(5, "2012abe9c73d0f39ed0d3cd295806f3b", "1ff7e9224b05e5be661414be05475c1e") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `subscriptions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `price` REAL NOT NULL, `currency` TEXT NOT NULL, `categoryId` INTEGER NOT NULL, `billingCycle` TEXT NOT NULL, `paymentType` TEXT NOT NULL, `customCycleDays` INTEGER, `firstBillingDate` INTEGER NOT NULL, `nextBillingDate` INTEGER NOT NULL, `isTrial` INTEGER NOT NULL, `trialEndDate` INTEGER, `reminderDaysBefore` INTEGER NOT NULL, `iconResId` TEXT, `colorHex` TEXT, `isActive` INTEGER NOT NULL, `notes` TEXT, `lastNotifiedDate` INTEGER)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `categories` (`id` INTEGER NOT NULL, `name` TEXT NOT NULL, `iconName` TEXT NOT NULL, `defaultColorHex` TEXT NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `payment_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `subscriptionId` INTEGER NOT NULL, `subscriptionName` TEXT NOT NULL, `amount` REAL NOT NULL, `currency` TEXT NOT NULL, `paymentDate` INTEGER NOT NULL, `billingPeriodStart` INTEGER NOT NULL, `billingPeriodEnd` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '2012abe9c73d0f39ed0d3cd295806f3b')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `subscriptions`")
        connection.execSQL("DROP TABLE IF EXISTS `categories`")
        connection.execSQL("DROP TABLE IF EXISTS `payment_history`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsSubscriptions: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSubscriptions.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSubscriptions.put("name", TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSubscriptions.put("price", TableInfo.Column("price", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSubscriptions.put("currency", TableInfo.Column("currency", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSubscriptions.put("categoryId", TableInfo.Column("categoryId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSubscriptions.put("billingCycle", TableInfo.Column("billingCycle", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSubscriptions.put("paymentType", TableInfo.Column("paymentType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSubscriptions.put("customCycleDays", TableInfo.Column("customCycleDays", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSubscriptions.put("firstBillingDate", TableInfo.Column("firstBillingDate", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSubscriptions.put("nextBillingDate", TableInfo.Column("nextBillingDate", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSubscriptions.put("isTrial", TableInfo.Column("isTrial", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSubscriptions.put("trialEndDate", TableInfo.Column("trialEndDate", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSubscriptions.put("reminderDaysBefore", TableInfo.Column("reminderDaysBefore", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSubscriptions.put("iconResId", TableInfo.Column("iconResId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSubscriptions.put("colorHex", TableInfo.Column("colorHex", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSubscriptions.put("isActive", TableInfo.Column("isActive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSubscriptions.put("notes", TableInfo.Column("notes", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSubscriptions.put("lastNotifiedDate", TableInfo.Column("lastNotifiedDate", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSubscriptions: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesSubscriptions: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoSubscriptions: TableInfo = TableInfo("subscriptions", _columnsSubscriptions, _foreignKeysSubscriptions, _indicesSubscriptions)
        val _existingSubscriptions: TableInfo = read(connection, "subscriptions")
        if (!_infoSubscriptions.equals(_existingSubscriptions)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |subscriptions(com.mate.subsmate.data.local.entities.SubscriptionEntity).
              | Expected:
              |""".trimMargin() + _infoSubscriptions + """
              |
              | Found:
              |""".trimMargin() + _existingSubscriptions)
        }
        val _columnsCategories: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsCategories.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCategories.put("name", TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCategories.put("iconName", TableInfo.Column("iconName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCategories.put("defaultColorHex", TableInfo.Column("defaultColorHex", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysCategories: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesCategories: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoCategories: TableInfo = TableInfo("categories", _columnsCategories, _foreignKeysCategories, _indicesCategories)
        val _existingCategories: TableInfo = read(connection, "categories")
        if (!_infoCategories.equals(_existingCategories)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |categories(com.mate.subsmate.data.local.entities.CategoryEntity).
              | Expected:
              |""".trimMargin() + _infoCategories + """
              |
              | Found:
              |""".trimMargin() + _existingCategories)
        }
        val _columnsPaymentHistory: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsPaymentHistory.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPaymentHistory.put("subscriptionId", TableInfo.Column("subscriptionId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPaymentHistory.put("subscriptionName", TableInfo.Column("subscriptionName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPaymentHistory.put("amount", TableInfo.Column("amount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPaymentHistory.put("currency", TableInfo.Column("currency", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPaymentHistory.put("paymentDate", TableInfo.Column("paymentDate", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPaymentHistory.put("billingPeriodStart", TableInfo.Column("billingPeriodStart", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPaymentHistory.put("billingPeriodEnd", TableInfo.Column("billingPeriodEnd", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysPaymentHistory: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesPaymentHistory: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoPaymentHistory: TableInfo = TableInfo("payment_history", _columnsPaymentHistory, _foreignKeysPaymentHistory, _indicesPaymentHistory)
        val _existingPaymentHistory: TableInfo = read(connection, "payment_history")
        if (!_infoPaymentHistory.equals(_existingPaymentHistory)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |payment_history(com.mate.subsmate.data.local.entities.PaymentHistoryEntity).
              | Expected:
              |""".trimMargin() + _infoPaymentHistory + """
              |
              | Found:
              |""".trimMargin() + _existingPaymentHistory)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "subscriptions", "categories", "payment_history")
  }

  public override fun clearAllTables() {
    super.performClear(false, "subscriptions", "categories", "payment_history")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(SubscriptionDao::class, SubscriptionDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(PaymentDao::class, PaymentDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun subscriptionDao(): SubscriptionDao = _subscriptionDao.value

  public override fun paymentDao(): PaymentDao = _paymentDao.value
}
