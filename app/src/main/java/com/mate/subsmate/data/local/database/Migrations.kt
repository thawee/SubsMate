package com.mate.subsmate.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {
    val MIGRATION_8_9 = object : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            val cursor = db.query("PRAGMA table_info(subscriptions)")
            val existingColumns = mutableSetOf<String>()
            cursor.use {
                while (it.moveToNext()) {
                    existingColumns.add(it.getString(it.getColumnIndexOrThrow("name")))
                }
            }

            if (!existingColumns.contains("customCycleDays")) {
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN customCycleDays INTEGER")
            }
            if (!existingColumns.contains("isVariablePrice")) {
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN isVariablePrice INTEGER NOT NULL DEFAULT 0")
            }
            if (!existingColumns.contains("totalInstallments")) {
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN totalInstallments INTEGER")
            }
            if (!existingColumns.contains("currentInstallment")) {
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN currentInstallment INTEGER NOT NULL DEFAULT 0")
            }
            if (!existingColumns.contains("totalLoanAmount")) {
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN totalLoanAmount REAL")
            }
            if (!existingColumns.contains("interestRate")) {
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN interestRate REAL")
            }
            if (!existingColumns.contains("extraPrincipalPaid")) {
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN extraPrincipalPaid REAL NOT NULL DEFAULT 0.0")
            }
        }
    }

    val MIGRATION_9_10 = object : Migration(9, 10) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE payment_history_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, subscriptionId INTEGER NOT NULL, subscriptionName TEXT NOT NULL, amount REAL NOT NULL, currency TEXT NOT NULL, paymentDate INTEGER NOT NULL, billingPeriodStart INTEGER NOT NULL, billingPeriodEnd INTEGER NOT NULL, FOREIGN KEY (subscriptionId) REFERENCES subscriptions(id) ON DELETE CASCADE)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_payment_history_subscriptionId ON payment_history_new (subscriptionId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_payment_history_billingPeriodStart ON payment_history_new (billingPeriodStart)")
            db.execSQL("INSERT INTO payment_history_new (id, subscriptionId, subscriptionName, amount, currency, paymentDate, billingPeriodStart, billingPeriodEnd) SELECT id, subscriptionId, subscriptionName, amount, currency, paymentDate, billingPeriodStart, billingPeriodEnd FROM payment_history")
            db.execSQL("DROP TABLE payment_history")
            db.execSQL("ALTER TABLE payment_history_new RENAME TO payment_history")
        }
    }
}
