package com.mate.subsmate.`data`.local.dao

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.mate.subsmate.`data`.local.database.Converters
import com.mate.subsmate.`data`.local.entities.SubscriptionEntity
import com.mate.subsmate.domain.model.BillingCycle
import com.mate.subsmate.domain.model.PaymentType
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Double
import kotlin.IllegalArgumentException
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class SubscriptionDao_Impl(
  __db: RoomDatabase,
) : SubscriptionDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfSubscriptionEntity: EntityInsertAdapter<SubscriptionEntity>

  private val __converters: Converters = Converters()

  private val __deleteAdapterOfSubscriptionEntity: EntityDeleteOrUpdateAdapter<SubscriptionEntity>

  private val __updateAdapterOfSubscriptionEntity: EntityDeleteOrUpdateAdapter<SubscriptionEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfSubscriptionEntity = object : EntityInsertAdapter<SubscriptionEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `subscriptions` (`id`,`name`,`price`,`currency`,`categoryId`,`billingCycle`,`paymentType`,`customCycleDays`,`firstBillingDate`,`nextBillingDate`,`isTrial`,`trialEndDate`,`reminderDaysBefore`,`iconResId`,`colorHex`,`isActive`,`notes`,`lastNotifiedDate`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: SubscriptionEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindDouble(3, entity.price)
        statement.bindText(4, entity.currency)
        statement.bindLong(5, entity.categoryId.toLong())
        val _tmp: String = __converters.fromBillingCycle(entity.billingCycle)
        statement.bindText(6, _tmp)
        statement.bindText(7, __PaymentType_enumToString(entity.paymentType))
        val _tmpCustomCycleDays: Int? = entity.customCycleDays
        if (_tmpCustomCycleDays == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpCustomCycleDays.toLong())
        }
        statement.bindLong(9, entity.firstBillingDate)
        statement.bindLong(10, entity.nextBillingDate)
        val _tmp_1: Int = if (entity.isTrial) 1 else 0
        statement.bindLong(11, _tmp_1.toLong())
        val _tmpTrialEndDate: Long? = entity.trialEndDate
        if (_tmpTrialEndDate == null) {
          statement.bindNull(12)
        } else {
          statement.bindLong(12, _tmpTrialEndDate)
        }
        statement.bindLong(13, entity.reminderDaysBefore.toLong())
        val _tmpIconResId: String? = entity.iconResId
        if (_tmpIconResId == null) {
          statement.bindNull(14)
        } else {
          statement.bindText(14, _tmpIconResId)
        }
        val _tmpColorHex: String? = entity.colorHex
        if (_tmpColorHex == null) {
          statement.bindNull(15)
        } else {
          statement.bindText(15, _tmpColorHex)
        }
        val _tmp_2: Int = if (entity.isActive) 1 else 0
        statement.bindLong(16, _tmp_2.toLong())
        val _tmpNotes: String? = entity.notes
        if (_tmpNotes == null) {
          statement.bindNull(17)
        } else {
          statement.bindText(17, _tmpNotes)
        }
        val _tmpLastNotifiedDate: Long? = entity.lastNotifiedDate
        if (_tmpLastNotifiedDate == null) {
          statement.bindNull(18)
        } else {
          statement.bindLong(18, _tmpLastNotifiedDate)
        }
      }
    }
    this.__deleteAdapterOfSubscriptionEntity = object : EntityDeleteOrUpdateAdapter<SubscriptionEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `subscriptions` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: SubscriptionEntity) {
        statement.bindLong(1, entity.id)
      }
    }
    this.__updateAdapterOfSubscriptionEntity = object : EntityDeleteOrUpdateAdapter<SubscriptionEntity>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `subscriptions` SET `id` = ?,`name` = ?,`price` = ?,`currency` = ?,`categoryId` = ?,`billingCycle` = ?,`paymentType` = ?,`customCycleDays` = ?,`firstBillingDate` = ?,`nextBillingDate` = ?,`isTrial` = ?,`trialEndDate` = ?,`reminderDaysBefore` = ?,`iconResId` = ?,`colorHex` = ?,`isActive` = ?,`notes` = ?,`lastNotifiedDate` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: SubscriptionEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindDouble(3, entity.price)
        statement.bindText(4, entity.currency)
        statement.bindLong(5, entity.categoryId.toLong())
        val _tmp: String = __converters.fromBillingCycle(entity.billingCycle)
        statement.bindText(6, _tmp)
        statement.bindText(7, __PaymentType_enumToString(entity.paymentType))
        val _tmpCustomCycleDays: Int? = entity.customCycleDays
        if (_tmpCustomCycleDays == null) {
          statement.bindNull(8)
        } else {
          statement.bindLong(8, _tmpCustomCycleDays.toLong())
        }
        statement.bindLong(9, entity.firstBillingDate)
        statement.bindLong(10, entity.nextBillingDate)
        val _tmp_1: Int = if (entity.isTrial) 1 else 0
        statement.bindLong(11, _tmp_1.toLong())
        val _tmpTrialEndDate: Long? = entity.trialEndDate
        if (_tmpTrialEndDate == null) {
          statement.bindNull(12)
        } else {
          statement.bindLong(12, _tmpTrialEndDate)
        }
        statement.bindLong(13, entity.reminderDaysBefore.toLong())
        val _tmpIconResId: String? = entity.iconResId
        if (_tmpIconResId == null) {
          statement.bindNull(14)
        } else {
          statement.bindText(14, _tmpIconResId)
        }
        val _tmpColorHex: String? = entity.colorHex
        if (_tmpColorHex == null) {
          statement.bindNull(15)
        } else {
          statement.bindText(15, _tmpColorHex)
        }
        val _tmp_2: Int = if (entity.isActive) 1 else 0
        statement.bindLong(16, _tmp_2.toLong())
        val _tmpNotes: String? = entity.notes
        if (_tmpNotes == null) {
          statement.bindNull(17)
        } else {
          statement.bindText(17, _tmpNotes)
        }
        val _tmpLastNotifiedDate: Long? = entity.lastNotifiedDate
        if (_tmpLastNotifiedDate == null) {
          statement.bindNull(18)
        } else {
          statement.bindLong(18, _tmpLastNotifiedDate)
        }
        statement.bindLong(19, entity.id)
      }
    }
  }

  public override suspend fun insertSubscription(subscription: SubscriptionEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfSubscriptionEntity.insert(_connection, subscription)
  }

  public override suspend fun deleteSubscription(subscription: SubscriptionEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfSubscriptionEntity.handle(_connection, subscription)
  }

  public override suspend fun updateSubscription(subscription: SubscriptionEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfSubscriptionEntity.handle(_connection, subscription)
  }

  public override fun getAllActiveSubscriptions(): Flow<List<SubscriptionEntity>> {
    val _sql: String = "SELECT * FROM subscriptions WHERE isActive = 1 ORDER BY nextBillingDate ASC"
    return createFlow(__db, false, arrayOf("subscriptions")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfPrice: Int = getColumnIndexOrThrow(_stmt, "price")
        val _columnIndexOfCurrency: Int = getColumnIndexOrThrow(_stmt, "currency")
        val _columnIndexOfCategoryId: Int = getColumnIndexOrThrow(_stmt, "categoryId")
        val _columnIndexOfBillingCycle: Int = getColumnIndexOrThrow(_stmt, "billingCycle")
        val _columnIndexOfPaymentType: Int = getColumnIndexOrThrow(_stmt, "paymentType")
        val _columnIndexOfCustomCycleDays: Int = getColumnIndexOrThrow(_stmt, "customCycleDays")
        val _columnIndexOfFirstBillingDate: Int = getColumnIndexOrThrow(_stmt, "firstBillingDate")
        val _columnIndexOfNextBillingDate: Int = getColumnIndexOrThrow(_stmt, "nextBillingDate")
        val _columnIndexOfIsTrial: Int = getColumnIndexOrThrow(_stmt, "isTrial")
        val _columnIndexOfTrialEndDate: Int = getColumnIndexOrThrow(_stmt, "trialEndDate")
        val _columnIndexOfReminderDaysBefore: Int = getColumnIndexOrThrow(_stmt, "reminderDaysBefore")
        val _columnIndexOfIconResId: Int = getColumnIndexOrThrow(_stmt, "iconResId")
        val _columnIndexOfColorHex: Int = getColumnIndexOrThrow(_stmt, "colorHex")
        val _columnIndexOfIsActive: Int = getColumnIndexOrThrow(_stmt, "isActive")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfLastNotifiedDate: Int = getColumnIndexOrThrow(_stmt, "lastNotifiedDate")
        val _result: MutableList<SubscriptionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SubscriptionEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpPrice: Double
          _tmpPrice = _stmt.getDouble(_columnIndexOfPrice)
          val _tmpCurrency: String
          _tmpCurrency = _stmt.getText(_columnIndexOfCurrency)
          val _tmpCategoryId: Int
          _tmpCategoryId = _stmt.getLong(_columnIndexOfCategoryId).toInt()
          val _tmpBillingCycle: BillingCycle
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfBillingCycle)
          _tmpBillingCycle = __converters.toBillingCycle(_tmp)
          val _tmpPaymentType: PaymentType
          _tmpPaymentType = __PaymentType_stringToEnum(_stmt.getText(_columnIndexOfPaymentType))
          val _tmpCustomCycleDays: Int?
          if (_stmt.isNull(_columnIndexOfCustomCycleDays)) {
            _tmpCustomCycleDays = null
          } else {
            _tmpCustomCycleDays = _stmt.getLong(_columnIndexOfCustomCycleDays).toInt()
          }
          val _tmpFirstBillingDate: Long
          _tmpFirstBillingDate = _stmt.getLong(_columnIndexOfFirstBillingDate)
          val _tmpNextBillingDate: Long
          _tmpNextBillingDate = _stmt.getLong(_columnIndexOfNextBillingDate)
          val _tmpIsTrial: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsTrial).toInt()
          _tmpIsTrial = _tmp_1 != 0
          val _tmpTrialEndDate: Long?
          if (_stmt.isNull(_columnIndexOfTrialEndDate)) {
            _tmpTrialEndDate = null
          } else {
            _tmpTrialEndDate = _stmt.getLong(_columnIndexOfTrialEndDate)
          }
          val _tmpReminderDaysBefore: Int
          _tmpReminderDaysBefore = _stmt.getLong(_columnIndexOfReminderDaysBefore).toInt()
          val _tmpIconResId: String?
          if (_stmt.isNull(_columnIndexOfIconResId)) {
            _tmpIconResId = null
          } else {
            _tmpIconResId = _stmt.getText(_columnIndexOfIconResId)
          }
          val _tmpColorHex: String?
          if (_stmt.isNull(_columnIndexOfColorHex)) {
            _tmpColorHex = null
          } else {
            _tmpColorHex = _stmt.getText(_columnIndexOfColorHex)
          }
          val _tmpIsActive: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsActive).toInt()
          _tmpIsActive = _tmp_2 != 0
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          val _tmpLastNotifiedDate: Long?
          if (_stmt.isNull(_columnIndexOfLastNotifiedDate)) {
            _tmpLastNotifiedDate = null
          } else {
            _tmpLastNotifiedDate = _stmt.getLong(_columnIndexOfLastNotifiedDate)
          }
          _item = SubscriptionEntity(_tmpId,_tmpName,_tmpPrice,_tmpCurrency,_tmpCategoryId,_tmpBillingCycle,_tmpPaymentType,_tmpCustomCycleDays,_tmpFirstBillingDate,_tmpNextBillingDate,_tmpIsTrial,_tmpTrialEndDate,_tmpReminderDaysBefore,_tmpIconResId,_tmpColorHex,_tmpIsActive,_tmpNotes,_tmpLastNotifiedDate)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getActiveSubscriptionsOneShot(): List<SubscriptionEntity> {
    val _sql: String = "SELECT * FROM subscriptions WHERE isActive = 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfPrice: Int = getColumnIndexOrThrow(_stmt, "price")
        val _columnIndexOfCurrency: Int = getColumnIndexOrThrow(_stmt, "currency")
        val _columnIndexOfCategoryId: Int = getColumnIndexOrThrow(_stmt, "categoryId")
        val _columnIndexOfBillingCycle: Int = getColumnIndexOrThrow(_stmt, "billingCycle")
        val _columnIndexOfPaymentType: Int = getColumnIndexOrThrow(_stmt, "paymentType")
        val _columnIndexOfCustomCycleDays: Int = getColumnIndexOrThrow(_stmt, "customCycleDays")
        val _columnIndexOfFirstBillingDate: Int = getColumnIndexOrThrow(_stmt, "firstBillingDate")
        val _columnIndexOfNextBillingDate: Int = getColumnIndexOrThrow(_stmt, "nextBillingDate")
        val _columnIndexOfIsTrial: Int = getColumnIndexOrThrow(_stmt, "isTrial")
        val _columnIndexOfTrialEndDate: Int = getColumnIndexOrThrow(_stmt, "trialEndDate")
        val _columnIndexOfReminderDaysBefore: Int = getColumnIndexOrThrow(_stmt, "reminderDaysBefore")
        val _columnIndexOfIconResId: Int = getColumnIndexOrThrow(_stmt, "iconResId")
        val _columnIndexOfColorHex: Int = getColumnIndexOrThrow(_stmt, "colorHex")
        val _columnIndexOfIsActive: Int = getColumnIndexOrThrow(_stmt, "isActive")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfLastNotifiedDate: Int = getColumnIndexOrThrow(_stmt, "lastNotifiedDate")
        val _result: MutableList<SubscriptionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SubscriptionEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpPrice: Double
          _tmpPrice = _stmt.getDouble(_columnIndexOfPrice)
          val _tmpCurrency: String
          _tmpCurrency = _stmt.getText(_columnIndexOfCurrency)
          val _tmpCategoryId: Int
          _tmpCategoryId = _stmt.getLong(_columnIndexOfCategoryId).toInt()
          val _tmpBillingCycle: BillingCycle
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfBillingCycle)
          _tmpBillingCycle = __converters.toBillingCycle(_tmp)
          val _tmpPaymentType: PaymentType
          _tmpPaymentType = __PaymentType_stringToEnum(_stmt.getText(_columnIndexOfPaymentType))
          val _tmpCustomCycleDays: Int?
          if (_stmt.isNull(_columnIndexOfCustomCycleDays)) {
            _tmpCustomCycleDays = null
          } else {
            _tmpCustomCycleDays = _stmt.getLong(_columnIndexOfCustomCycleDays).toInt()
          }
          val _tmpFirstBillingDate: Long
          _tmpFirstBillingDate = _stmt.getLong(_columnIndexOfFirstBillingDate)
          val _tmpNextBillingDate: Long
          _tmpNextBillingDate = _stmt.getLong(_columnIndexOfNextBillingDate)
          val _tmpIsTrial: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsTrial).toInt()
          _tmpIsTrial = _tmp_1 != 0
          val _tmpTrialEndDate: Long?
          if (_stmt.isNull(_columnIndexOfTrialEndDate)) {
            _tmpTrialEndDate = null
          } else {
            _tmpTrialEndDate = _stmt.getLong(_columnIndexOfTrialEndDate)
          }
          val _tmpReminderDaysBefore: Int
          _tmpReminderDaysBefore = _stmt.getLong(_columnIndexOfReminderDaysBefore).toInt()
          val _tmpIconResId: String?
          if (_stmt.isNull(_columnIndexOfIconResId)) {
            _tmpIconResId = null
          } else {
            _tmpIconResId = _stmt.getText(_columnIndexOfIconResId)
          }
          val _tmpColorHex: String?
          if (_stmt.isNull(_columnIndexOfColorHex)) {
            _tmpColorHex = null
          } else {
            _tmpColorHex = _stmt.getText(_columnIndexOfColorHex)
          }
          val _tmpIsActive: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsActive).toInt()
          _tmpIsActive = _tmp_2 != 0
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          val _tmpLastNotifiedDate: Long?
          if (_stmt.isNull(_columnIndexOfLastNotifiedDate)) {
            _tmpLastNotifiedDate = null
          } else {
            _tmpLastNotifiedDate = _stmt.getLong(_columnIndexOfLastNotifiedDate)
          }
          _item = SubscriptionEntity(_tmpId,_tmpName,_tmpPrice,_tmpCurrency,_tmpCategoryId,_tmpBillingCycle,_tmpPaymentType,_tmpCustomCycleDays,_tmpFirstBillingDate,_tmpNextBillingDate,_tmpIsTrial,_tmpTrialEndDate,_tmpReminderDaysBefore,_tmpIconResId,_tmpColorHex,_tmpIsActive,_tmpNotes,_tmpLastNotifiedDate)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getSubscriptionById(id: Long): Flow<SubscriptionEntity?> {
    val _sql: String = "SELECT * FROM subscriptions WHERE id = ?"
    return createFlow(__db, false, arrayOf("subscriptions")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfPrice: Int = getColumnIndexOrThrow(_stmt, "price")
        val _columnIndexOfCurrency: Int = getColumnIndexOrThrow(_stmt, "currency")
        val _columnIndexOfCategoryId: Int = getColumnIndexOrThrow(_stmt, "categoryId")
        val _columnIndexOfBillingCycle: Int = getColumnIndexOrThrow(_stmt, "billingCycle")
        val _columnIndexOfPaymentType: Int = getColumnIndexOrThrow(_stmt, "paymentType")
        val _columnIndexOfCustomCycleDays: Int = getColumnIndexOrThrow(_stmt, "customCycleDays")
        val _columnIndexOfFirstBillingDate: Int = getColumnIndexOrThrow(_stmt, "firstBillingDate")
        val _columnIndexOfNextBillingDate: Int = getColumnIndexOrThrow(_stmt, "nextBillingDate")
        val _columnIndexOfIsTrial: Int = getColumnIndexOrThrow(_stmt, "isTrial")
        val _columnIndexOfTrialEndDate: Int = getColumnIndexOrThrow(_stmt, "trialEndDate")
        val _columnIndexOfReminderDaysBefore: Int = getColumnIndexOrThrow(_stmt, "reminderDaysBefore")
        val _columnIndexOfIconResId: Int = getColumnIndexOrThrow(_stmt, "iconResId")
        val _columnIndexOfColorHex: Int = getColumnIndexOrThrow(_stmt, "colorHex")
        val _columnIndexOfIsActive: Int = getColumnIndexOrThrow(_stmt, "isActive")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfLastNotifiedDate: Int = getColumnIndexOrThrow(_stmt, "lastNotifiedDate")
        val _result: SubscriptionEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpPrice: Double
          _tmpPrice = _stmt.getDouble(_columnIndexOfPrice)
          val _tmpCurrency: String
          _tmpCurrency = _stmt.getText(_columnIndexOfCurrency)
          val _tmpCategoryId: Int
          _tmpCategoryId = _stmt.getLong(_columnIndexOfCategoryId).toInt()
          val _tmpBillingCycle: BillingCycle
          val _tmp: String
          _tmp = _stmt.getText(_columnIndexOfBillingCycle)
          _tmpBillingCycle = __converters.toBillingCycle(_tmp)
          val _tmpPaymentType: PaymentType
          _tmpPaymentType = __PaymentType_stringToEnum(_stmt.getText(_columnIndexOfPaymentType))
          val _tmpCustomCycleDays: Int?
          if (_stmt.isNull(_columnIndexOfCustomCycleDays)) {
            _tmpCustomCycleDays = null
          } else {
            _tmpCustomCycleDays = _stmt.getLong(_columnIndexOfCustomCycleDays).toInt()
          }
          val _tmpFirstBillingDate: Long
          _tmpFirstBillingDate = _stmt.getLong(_columnIndexOfFirstBillingDate)
          val _tmpNextBillingDate: Long
          _tmpNextBillingDate = _stmt.getLong(_columnIndexOfNextBillingDate)
          val _tmpIsTrial: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfIsTrial).toInt()
          _tmpIsTrial = _tmp_1 != 0
          val _tmpTrialEndDate: Long?
          if (_stmt.isNull(_columnIndexOfTrialEndDate)) {
            _tmpTrialEndDate = null
          } else {
            _tmpTrialEndDate = _stmt.getLong(_columnIndexOfTrialEndDate)
          }
          val _tmpReminderDaysBefore: Int
          _tmpReminderDaysBefore = _stmt.getLong(_columnIndexOfReminderDaysBefore).toInt()
          val _tmpIconResId: String?
          if (_stmt.isNull(_columnIndexOfIconResId)) {
            _tmpIconResId = null
          } else {
            _tmpIconResId = _stmt.getText(_columnIndexOfIconResId)
          }
          val _tmpColorHex: String?
          if (_stmt.isNull(_columnIndexOfColorHex)) {
            _tmpColorHex = null
          } else {
            _tmpColorHex = _stmt.getText(_columnIndexOfColorHex)
          }
          val _tmpIsActive: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfIsActive).toInt()
          _tmpIsActive = _tmp_2 != 0
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          val _tmpLastNotifiedDate: Long?
          if (_stmt.isNull(_columnIndexOfLastNotifiedDate)) {
            _tmpLastNotifiedDate = null
          } else {
            _tmpLastNotifiedDate = _stmt.getLong(_columnIndexOfLastNotifiedDate)
          }
          _result = SubscriptionEntity(_tmpId,_tmpName,_tmpPrice,_tmpCurrency,_tmpCategoryId,_tmpBillingCycle,_tmpPaymentType,_tmpCustomCycleDays,_tmpFirstBillingDate,_tmpNextBillingDate,_tmpIsTrial,_tmpTrialEndDate,_tmpReminderDaysBefore,_tmpIconResId,_tmpColorHex,_tmpIsActive,_tmpNotes,_tmpLastNotifiedDate)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getEstimatedMonthlyTotal(): Flow<Double?> {
    val _sql: String = "SELECT SUM(CASE WHEN billingCycle = 'MONTHLY' THEN price WHEN billingCycle = 'YEARLY' THEN price / 12 ELSE 0 END) FROM subscriptions WHERE isActive = 1"
    return createFlow(__db, false, arrayOf("subscriptions")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Double?
        if (_stmt.step()) {
          val _tmp: Double?
          if (_stmt.isNull(0)) {
            _tmp = null
          } else {
            _tmp = _stmt.getDouble(0)
          }
          _result = _tmp
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateCategoryId(oldId: Int, newId: Int) {
    val _sql: String = "UPDATE subscriptions SET categoryId = ? WHERE categoryId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, newId.toLong())
        _argIndex = 2
        _stmt.bindLong(_argIndex, oldId.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  private fun __PaymentType_enumToString(_value: PaymentType): String = when (_value) {
    PaymentType.AUTO_PAY -> "AUTO_PAY"
    PaymentType.MANUAL -> "MANUAL"
  }

  private fun __PaymentType_stringToEnum(_value: String): PaymentType = when (_value) {
    "AUTO_PAY" -> PaymentType.AUTO_PAY
    "MANUAL" -> PaymentType.MANUAL
    else -> throw IllegalArgumentException("Can't convert value to enum, unknown value: " + _value)
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
