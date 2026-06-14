package com.mate.subsmate.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {
    val MIGRATION_8_9 = object : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Add loan-related columns if they don't exist
            val columns = mutableListOf<Pair<String, String>>()
            
            // Check which columns exist
            val cursor = db.query("PRAGMA table_info(subscriptions)")
            val existingColumns = mutableSetOf<String>()
            cursor.use {
                while (it.moveToNext()) {
                    existingColumns.add(it.getString(it.getColumnIndexOrThrow("name")))
                }
            }
            
            // Add missing columns
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
}
