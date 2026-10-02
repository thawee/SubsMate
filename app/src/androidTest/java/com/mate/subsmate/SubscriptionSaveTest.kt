package com.mate.subsmate

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mate.subsmate.data.local.database.AppDatabase
import com.mate.subsmate.data.repository.SubscriptionRepositoryImpl
import com.mate.subsmate.ui.add_subscription.AddSubscriptionViewModel
import com.mate.subsmate.ui.utils.PaymentActions
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SubscriptionSaveTest {
    @Test
    fun notesCanBeCreatedPreservedOnEditAndCleared() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val repository = SubscriptionRepositoryImpl(db.subscriptionDao(), db.paymentDao())
            val add = AddSubscriptionViewModel(repository).apply {
                onNameChange("Music")
                onPriceChange("99")
                onNotesChange("  Family plan\nRenews monthly  ")
            }
            add.saveSubscription("THB")
            withTimeout(5000) { add.uiState.first { it.isSaved } }

            val saved = repository.getAllActiveSubscriptions().first().single()
            assertEquals("Family plan\nRenews monthly", saved.notes)

            val edit = AddSubscriptionViewModel(repository)
            edit.loadSubscription(saved.id)
            withTimeout(5000) { edit.uiState.first { it.id == saved.id } }
            assertEquals(saved.notes, edit.uiState.value.notes)
            edit.onNameChange("Music Plus")
            edit.saveSubscription("THB")
            withTimeout(5000) { edit.uiState.first { it.isSaved } }
            assertEquals(saved.notes, repository.getSubscriptionById(saved.id).first()!!.notes)

            val clear = AddSubscriptionViewModel(repository)
            clear.loadSubscription(saved.id)
            withTimeout(5000) { clear.uiState.first { it.id == saved.id } }
            clear.onNotesChange("   ")
            clear.saveSubscription("THB")
            withTimeout(5000) { clear.uiState.first { it.isSaved } }
            assertEquals(null, repository.getSubscriptionById(saved.id).first()!!.notes)
        } finally {
            db.close()
        }
    }

    @Test
    fun savingExistingLoanKeepsPaymentsAndChangingDateUpdatesSchedule() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val repository = SubscriptionRepositoryImpl(db.subscriptionDao(), db.paymentDao())
            val add = AddSubscriptionViewModel(repository).apply {
                onNameChange("Loan")
                onPriceChange("100")
                onLoanToggle(true)
                onTotalLoanAmountChange("1200")
                onTotalInstallmentsChange("12")
                onCurrentInstallmentChange("1")
                onFirstBillingDateChange(System.currentTimeMillis() - 60L * 24 * 60 * 60 * 1000)
            }
            add.saveSubscription("THB")
            withTimeout(5000) { add.uiState.first { it.isSaved } }

            val saved = repository.getAllActiveSubscriptions().first().single()
            assertTrue(saved.id > 0)
            assertEquals(saved.id, repository.getAllPayments().first().single().subscriptionId)

            val edit = AddSubscriptionViewModel(repository)
            edit.loadSubscription(saved.id)
            withTimeout(5000) { edit.uiState.first { it.id == saved.id } }
            edit.onNameChange("Edited Loan")
            edit.saveSubscription("USD")
            withTimeout(5000) { edit.uiState.first { it.isSaved } }
            assertEquals(1, repository.getAllPayments().first().size)
            assertEquals("THB", repository.getSubscriptionById(saved.id).first()!!.currency)

            val changeDate = AddSubscriptionViewModel(repository)
            changeDate.loadSubscription(saved.id)
            withTimeout(5000) { changeDate.uiState.first { it.id == saved.id } }
            val newDate = System.currentTimeMillis() + 20L * 24 * 60 * 60 * 1000
            changeDate.onFirstBillingDateChange(newDate)
            changeDate.saveSubscription("USD")
            withTimeout(5000) { changeDate.uiState.first { it.isSaved } }
            assertEquals(newDate, repository.getSubscriptionById(saved.id).first()!!.nextBillingDate)
            assertEquals(1, repository.getAllPayments().first().size)

            val completed = repository.getSubscriptionById(saved.id).first()!!.copy(
                isActive = false,
                currentInstallment = 12
            )
            repository.updateSubscription(completed)
            assertEquals(1, repository.getAllSubscriptions().first().size)
            assertTrue(PaymentActions.undoLastPayment(repository, saved.id))
            val reopened = repository.getSubscriptionById(saved.id).first()!!
            assertTrue(reopened.isActive)
            assertEquals(11, reopened.currentInstallment)
            assertEquals(0, repository.getAllPayments().first().size)
        } finally {
            db.close()
        }
    }
}
