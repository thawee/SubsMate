# SubsMate Architecture & Design Document

## 1. Overview
SubsMate is an offline-first Android application designed to track recurring subscriptions, payments, and installment loans. It is built as part of the "Mate Series" of applications (sharing design DNA with FileMate).

## 2. Tech Stack
- **Language**: Kotlin
- **UI Toolkit**: Jetpack Compose (Material Design 3)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Local Storage**: Room Database (SQLite)
- **Background Tasks**: Android WorkManager (Periodic background syncs and notifications)
- **Navigation**: Jetpack Navigation Compose

## 3. Data Architecture (Room)
The app is fully offline and stores data securely on the device.

### Tables
1. **SubscriptionEntity (`subscriptions`)**
   - Stores the recurring item (e.g., Netflix, Loan Repayment).
   - Tracks `nextBillingDate`, `billingCycle` (e.g., Monthly, Yearly), `amount`, and styling (`colorHex`, `iconResId`).
   - For loans, it tracks `totalInstallments` and `currentInstallment`.
2. **PaymentEntity (`payments`)**
   - Records every time a payment is marked as "Paid".
   - Used to generate historical spending charts and trend data.

## 4. Core Logic
- **Date Calculation**: The app uses `VendorUtils.advanceByOneCycle(date, cycle)` to accurately compute the next billing date based on calendar rules (e.g., preserving the 31st of the month when advancing through February).
- **Notification Engine**: `RenewalNotificationWorker` checks the database for subscriptions due in the next 3 days and fires Android system notifications. It hooks directly into user SharedPreferences to ensure users can toggle alerts globally.

## 5. Security & Privacy
- **Offline First**: No cloud sync. Financial data remains strictly on the user's device.
