package com.mate.subsmate.`data`.local.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.mate.subsmate.`data`.local.entities.PaymentHistoryEntity
import javax.`annotation`.processing.Generated
import kotlin.Double
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
public class PaymentDao_Impl(
  __db: RoomDatabase,
) : PaymentDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfPaymentHistoryEntity: EntityInsertAdapter<PaymentHistoryEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfPaymentHistoryEntity = object : EntityInsertAdapter<PaymentHistoryEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `payment_history` (`id`,`subscriptionId`,`subscriptionName`,`amount`,`currency`,`paymentDate`,`billingPeriodStart`,`billingPeriodEnd`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: PaymentHistoryEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.subscriptionId)
        statement.bindText(3, entity.subscriptionName)
        statement.bindDouble(4, entity.amount)
        statement.bindText(5, entity.currency)
        statement.bindLong(6, entity.paymentDate)
        statement.bindLong(7, entity.billingPeriodStart)
        statement.bindLong(8, entity.billingPeriodEnd)
      }
    }
  }

  public override suspend fun insertPayment(payment: PaymentHistoryEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfPaymentHistoryEntity.insert(_connection, payment)
  }

  public override fun getAllPayments(): Flow<List<PaymentHistoryEntity>> {
    val _sql: String = "SELECT * FROM payment_history ORDER BY paymentDate DESC"
    return createFlow(__db, false, arrayOf("payment_history")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSubscriptionId: Int = getColumnIndexOrThrow(_stmt, "subscriptionId")
        val _columnIndexOfSubscriptionName: Int = getColumnIndexOrThrow(_stmt, "subscriptionName")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfCurrency: Int = getColumnIndexOrThrow(_stmt, "currency")
        val _columnIndexOfPaymentDate: Int = getColumnIndexOrThrow(_stmt, "paymentDate")
        val _columnIndexOfBillingPeriodStart: Int = getColumnIndexOrThrow(_stmt, "billingPeriodStart")
        val _columnIndexOfBillingPeriodEnd: Int = getColumnIndexOrThrow(_stmt, "billingPeriodEnd")
        val _result: MutableList<PaymentHistoryEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: PaymentHistoryEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpSubscriptionId: Long
          _tmpSubscriptionId = _stmt.getLong(_columnIndexOfSubscriptionId)
          val _tmpSubscriptionName: String
          _tmpSubscriptionName = _stmt.getText(_columnIndexOfSubscriptionName)
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpCurrency: String
          _tmpCurrency = _stmt.getText(_columnIndexOfCurrency)
          val _tmpPaymentDate: Long
          _tmpPaymentDate = _stmt.getLong(_columnIndexOfPaymentDate)
          val _tmpBillingPeriodStart: Long
          _tmpBillingPeriodStart = _stmt.getLong(_columnIndexOfBillingPeriodStart)
          val _tmpBillingPeriodEnd: Long
          _tmpBillingPeriodEnd = _stmt.getLong(_columnIndexOfBillingPeriodEnd)
          _item = PaymentHistoryEntity(_tmpId,_tmpSubscriptionId,_tmpSubscriptionName,_tmpAmount,_tmpCurrency,_tmpPaymentDate,_tmpBillingPeriodStart,_tmpBillingPeriodEnd)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getPaymentsForSubscription(subId: Long): Flow<List<PaymentHistoryEntity>> {
    val _sql: String = "SELECT * FROM payment_history WHERE subscriptionId = ? ORDER BY paymentDate DESC"
    return createFlow(__db, false, arrayOf("payment_history")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, subId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSubscriptionId: Int = getColumnIndexOrThrow(_stmt, "subscriptionId")
        val _columnIndexOfSubscriptionName: Int = getColumnIndexOrThrow(_stmt, "subscriptionName")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfCurrency: Int = getColumnIndexOrThrow(_stmt, "currency")
        val _columnIndexOfPaymentDate: Int = getColumnIndexOrThrow(_stmt, "paymentDate")
        val _columnIndexOfBillingPeriodStart: Int = getColumnIndexOrThrow(_stmt, "billingPeriodStart")
        val _columnIndexOfBillingPeriodEnd: Int = getColumnIndexOrThrow(_stmt, "billingPeriodEnd")
        val _result: MutableList<PaymentHistoryEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: PaymentHistoryEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpSubscriptionId: Long
          _tmpSubscriptionId = _stmt.getLong(_columnIndexOfSubscriptionId)
          val _tmpSubscriptionName: String
          _tmpSubscriptionName = _stmt.getText(_columnIndexOfSubscriptionName)
          val _tmpAmount: Double
          _tmpAmount = _stmt.getDouble(_columnIndexOfAmount)
          val _tmpCurrency: String
          _tmpCurrency = _stmt.getText(_columnIndexOfCurrency)
          val _tmpPaymentDate: Long
          _tmpPaymentDate = _stmt.getLong(_columnIndexOfPaymentDate)
          val _tmpBillingPeriodStart: Long
          _tmpBillingPeriodStart = _stmt.getLong(_columnIndexOfBillingPeriodStart)
          val _tmpBillingPeriodEnd: Long
          _tmpBillingPeriodEnd = _stmt.getLong(_columnIndexOfBillingPeriodEnd)
          _item = PaymentHistoryEntity(_tmpId,_tmpSubscriptionId,_tmpSubscriptionName,_tmpAmount,_tmpCurrency,_tmpPaymentDate,_tmpBillingPeriodStart,_tmpBillingPeriodEnd)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteLastPaymentForSubscription(subId: Long) {
    val _sql: String = "DELETE FROM payment_history WHERE id = (SELECT id FROM payment_history WHERE subscriptionId = ? ORDER BY paymentDate DESC LIMIT 1)"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, subId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
