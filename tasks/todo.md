# Plan: Support Bills with Variable/Unknown Amounts

To support bills like water and electricity that do not have a fixed amount until the bill arrives, we extended the app to allow "Variable Amount" subscriptions.

## Tasks

- [x] **1. Extend Database Model**
  - Add `isVariablePrice: Boolean = false` to [SubscriptionEntity.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/data/local/entities/SubscriptionEntity.kt).
  - Increment the database version from 5 to 6 in [AppDatabase.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/data/local/database/AppDatabase.kt).

- [x] **2. Update Add/Edit Subscription UI & ViewModel**
  - Add `isVariablePrice` property to `AddSubscriptionUiState` in [AddSubscriptionViewModel.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/ui/add_subscription/AddSubscriptionViewModel.kt).
  - Modify save validation logic in [AddSubscriptionViewModel.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/ui/add_subscription/AddSubscriptionViewModel.kt) to allow an empty price if `isVariablePrice` is active.
  - Automatically toggle `isVariablePrice = true` in `onTemplateSelected()` when selecting service templates with null/missing prices (e.g. "Electricity").
  - Add a "Variable Amount" switch/toggle to [AddSubscriptionScreen.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/ui/add_subscription/AddSubscriptionScreen.kt).
  - Make the price text field optional when "Variable Amount" is selected.

- [x] **3. Update Cost Estimations & Calculations**
  - Checked that the system handles `price = 0.0` gracefully in cost calculations (it naturally does not distort totals or budgets unless an estimate is entered).

- [x] **4. Update Dashboard and Subscription Lists UI**
  - Update `SubscriptionItem` in [DashboardScreen.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/ui/dashboard/DashboardScreen.kt) to display "Variable" (or "Est. $X.XX" if an estimate is provided) instead of the price value.
  - Update `SubscriptionRow` in [SubscriptionsScreen.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/ui/subscriptions/SubscriptionsScreen.kt) similarly.

- [x] **5. Update WorkManager Notifications**
  - Update [RenewalNotificationWorker.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/data/worker/RenewalNotificationWorker.kt) to omit formatting the specific price if `isVariablePrice` is enabled and no price is set.

---

# Plan: Support Loans and Leases (Home Loan, Car Lease, etc.)

Track installment-based payments (finite duration) alongside standard subscriptions.

## Tasks

- [x] **1. Extend Database Model**
  - Add `totalInstallments: Int? = null`, `currentInstallment: Int = 0`, and `totalLoanAmount: Double? = null` to [SubscriptionEntity.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/data/local/entities/SubscriptionEntity.kt).
  - Increment the database version to `7` in [AppDatabase.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/data/local/database/AppDatabase.kt).

- [x] **2. Update Add/Edit ViewModel & UI**
  - Add loan properties to state and input validation in [AddSubscriptionViewModel.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/val/ui/add_subscription/AddSubscriptionViewModel.kt).
  - Add checkbox "Installment Payment (Loan/Lease)" and inputs for total installments, current paid count, and total loan amount in [AddSubscriptionScreen.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/ui/add_subscription/AddSubscriptionScreen.kt).

- [x] **3. Implement Installment Advancement & Completion**
  - Update `markAsPaid()` in [DashboardViewModel.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/ui/dashboard/DashboardViewModel.kt) to increment `currentInstallment` and mark the loan as inactive (`isActive = false`) when completed.
  - Implement similar logic in the auto-renew code in [RenewalNotificationWorker.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/data/worker/RenewalNotificationWorker.kt).

- [x] **4. Update UI to Render Progress & Status**
  - Update `SubscriptionItem` in [DashboardScreen.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/ui/dashboard/DashboardScreen.kt) to render `"Payment X / Y"` and progress.
  - Update `SubscriptionRow` in [SubscriptionsScreen.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/ui/subscriptions/SubscriptionsScreen.kt) to show remaining payments and a visual progress indicator.

---

# Plan: Advanced Financial Tracking Enhancements

Implement Seasonality Graph, Extra Principal Logging, and Budget Overflow Alerts.

## Tasks

- [x] **1. Extend Model for Extra Principal Payments**
  - Add `extraPrincipalPaid: Double = 0.0` to [SubscriptionEntity.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/data/local/entities/SubscriptionEntity.kt).
  - Increment database version to `8` in [AppDatabase.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/data/local/database/AppDatabase.kt).
  - Update [LoanUtils.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/ui/utils/LoanUtils.kt) balance calculations to subtract `extraPrincipalPaid`.

- [x] **2. Update Add/Edit Subscription UI for Extra Principal**
  - Add "Extra Principal Paid" text field under the loan details card in [AddSubscriptionScreen.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/ui/add_subscription/AddSubscriptionScreen.kt).
  - Wire up properties and change handlers in [AddSubscriptionViewModel.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/ui/add_subscription/AddSubscriptionViewModel.kt).

- [x] **3. Implement Budget Overflow Alert banner**
  - Add an alert banner in [DashboardScreen.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/ui/dashboard/DashboardScreen.kt) when estimated monthly spend exceeds the monthly budget. Show how much the user is over budget.

- [x] **4. Implement Seasonality/History Graph inside Insights**
  - Update [InsightsViewModel.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/ui/insights/InsightsViewModel.kt) to retrieve payment history and aggregate spent amounts grouped by month.
  - Create a custom-rendered `BarChart` / `MonthlySpendChart` using canvas drawings in [InsightsScreen.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/ui/insights/InsightsScreen.kt) to show spending trends.

---

## Review & Verification Results
* **Compilation Status:** Successful. Command `./gradlew compileDebugKotlin` completed with no errors.
* **Migration Strategy:** Database version updated to `8` and schema migrations are handled destructive-first.
* **Feature Correctness:**
  * Added [LoanUtils.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/ui/utils/LoanUtils.kt) containing standard monthly compounded interest/amortization calculations.
  * Added switch toggle "Loan / Leasing Payment" with fields for total installments, paid count, initial principal, and interest rate.
  * Added an input field for "Extra Principal Paid (Optional)" under the loan card, which subtracts from the calculated remaining principal balance dynamically.
  * When marking paid, `currentInstallment` is incremented. When terms reach the total, `isActive` is set to false (completed).
  * Rendered custom linear progress bar and installment status inside both lists.
  * Rendered principal/interest split for upcoming payments and remaining balance based on amortization formulas.
  * Added a prominent warning banner inside the BudgetUsageCard on the Dashboard when the user exceeds their configured monthly budget.
  * Created a dynamic monthly spending bar chart in the Insights screen, aggregating actual payment histories chronologically for the last 6 months.

---

# Plan: Bug Fixes, Input Validation, and UX Polish

Address identified defects, add robust inputs validation, and polish styling.

## Tasks

- [x] **1. Fix Inactive/Active State Preservation on Edit**
  - Modify `saveSubscription()` in [AddSubscriptionViewModel.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/ui/add_subscription/AddSubscriptionViewModel.kt) to preserve the `isActive` state of existing subscriptions. If a loan's installments are modified to have remaining terms, reactivate it automatically.
- [x] **2. Fix Loan Installment Decrement on Payment Undo**
  - Modify `undoPayment()` in [DashboardViewModel.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/ui/dashboard/DashboardViewModel.kt) to decrement the `currentInstallment` for loans and ensure the subscription is reactivated (`isActive = true`).
- [x] **3. Implement Robust Input Validation for Loan Fields**
  - In [AddSubscriptionScreen.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/ui/add_subscription/AddSubscriptionScreen.kt), update the form validation:
    - `totalInstallments` must be > 0.
    - `currentInstallment` must be >= 0 and <= `totalInstallments`.
    - `totalLoanAmount` (Initial Principal) must be > 0.0.
    - `interestRate` must be >= 0.0 (if entered).
    - `extraPrincipalPaid` must be >= 0.0 (if entered).
- [x] **4. Add Visual Error Indicators to Form Text Fields**
  - Bind `isError` in [AddSubscriptionScreen.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/ui/add_subscription/AddSubscriptionScreen.kt) to display red borders when user input is invalid or non-numeric.
- [x] **5. Improve Budget Overflow Warning Banner Text Wrapping**
  - Add `Modifier.weight(1f)` to the warning message text in [DashboardScreen.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/ui/dashboard/DashboardScreen.kt) to avoid clipping and allow proper wrapping.

---

## Review & Verification Results (Fixes & Polish)
* **Compilation Status:** Successful. `./gradlew compileDebugKotlin` completed with no compile errors.
* **Logical Bug Fixes:**
  * In [AddSubscriptionViewModel.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/ui/add_subscription/AddSubscriptionViewModel.kt), we now compute a proper active status logic that preserves `existingSub.isActive` when editing, while reactivating a loan if installments are increased.
  * In [DashboardViewModel.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/ui/dashboard/DashboardViewModel.kt), we now correctly decrement `currentInstallment` and reactivate completed/inactive items (`isActive = true`) when undoing a payment.
* **Input Validation & UX Polish:**
  * Configured rigorous verification in `isLoanValid` in [AddSubscriptionScreen.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/ui/add_subscription/AddSubscriptionScreen.kt) to prevent negative, empty, or logically invalid values.
  * Wired up `isError` to all key input fields in [AddSubscriptionScreen.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/ui/add_subscription/AddSubscriptionScreen.kt), providing clear red outlines on invalid input.
  * Added `Modifier.weight(1f)` to the text element in the dashboard budget overflow warning card in [DashboardScreen.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/ui/dashboard/DashboardScreen.kt) to guarantee proper text wrapping.

---

# Plan: Dynamic Currency Support

Support any currency code, default to THB, and allow custom currency inputs.

## Tasks

- [x] **1. Change Default Currency to THB**
  - Update default currency to `"THB"` in [SettingsViewModel.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/ui/settings/SettingsViewModel.kt) and [SubscriptionEntity.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/data/local/entities/SubscriptionEntity.kt).
- [x] **2. Pass Currency to SubscriptionEntity on Save**
  - Update `saveSubscription(currency: String)` in [AddSubscriptionViewModel.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/ui/add_subscription/AddSubscriptionViewModel.kt) to store the currency code when creating a new or editing a subscription.
- [x] **3. Implement Dynamic Currency Symbols in Screen UIs**
  - Replace hardcoded currency checks with [CurrencyUtils.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/ui/utils/CurrencyUtils.kt) in:
    - [AddSubscriptionScreen.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/ui/add_subscription/AddSubscriptionScreen.kt)
    - [DashboardScreen.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/ui/dashboard/DashboardScreen.kt)
    - [SubscriptionsScreen.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/ui/subscriptions/SubscriptionsScreen.kt)
    - [InsightsScreen.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/ui/insights/InsightsScreen.kt)
- [x] **4. Build Currency Picker with Custom Option in Settings**
  - Modify currency selection in [SettingsScreen.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/ui/settings/SettingsScreen.kt) to display a dialog with common currencies (THB, USD, EUR, JPY, GBP) and a custom currency text input.

---

## Review & Verification Results (Dynamic Currency)
* **Compilation Status:** Successful. `./gradlew compileDebugKotlin` completed with no compile errors.
* **Default Settings Change:** Updated default currency from `"USD"` to `"THB"` in [SettingsViewModel.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/ui/settings/SettingsViewModel.kt) and [SubscriptionEntity.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/data/local/entities/SubscriptionEntity.kt).
* **Save Handling:** Passing selected currency code to [AddSubscriptionViewModel.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/ui/add_subscription/AddSubscriptionViewModel.kt) when calling `saveSubscription(currency)`, saving it correctly into the DB.
* **Dynamic Symbol Resolution:** Created [CurrencyUtils.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/ui/utils/CurrencyUtils.kt) using Java standard libraries, mapping any currency code to its localized symbol, with smart fallbacks.
* **Currency Dialog Picker**: Users can select from common choices (THB, USD, EUR, GBP, JPY) or pick "Custom..." and type any 3-letter currency code (e.g. AUD, CAD) directly from the settings screen.
* **Notification Integration**: Updated [RenewalNotificationWorker.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/data/worker/RenewalNotificationWorker.kt) to format prices in notifications using the dynamic currency symbol from `CurrencyUtils`.

---

# Plan: Audit Defect Resolutions

Address identified defects from the code audit review:
- Fix database builder concurrency (implement DB singleton).
- Fix stale UI update race condition in markAsPaid and undoPayment.
- Match AddSubscriptionViewModel validation with stricter UI rules.
- Add confirmation dialog to Delete Subscription action.

## Tasks

- [x] **1. Implement Database Singleton**
  - Update [AppDatabase.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/data/local/database/AppDatabase.kt) to use a synchronized companion object singleton instance.
  - Refactor [MainActivity.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/MainActivity.kt) and [RenewalNotificationWorker.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/data/worker/RenewalNotificationWorker.kt) to retrieve this database instance.
- [x] **2. Resolve Race Condition in Payments**
  - In [DashboardViewModel.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/ui/dashboard/DashboardViewModel.kt), query the latest database entity state inside the coroutine block before calculating next dates and performing updates.
- [x] **3. Align ViewModel Save Validation**
  - In [AddSubscriptionViewModel.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/ui/add_subscription/AddSubscriptionViewModel.kt), tighten the save conditions to validate all loan inputs.
- [x] **4. Add Delete Confirmation Dialog**
  - In [SubscriptionsScreen.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/ui/subscriptions/SubscriptionsScreen.kt), add a confirmation AlertDialog before invoking subscription deletion.

---

## Review & Verification Results (Audit Resolutions)
* **Compilation Status:** Successful. `./gradlew compileDebugKotlin` completed with no compile errors.
* **Database Singleton Concurrency**: Introduced a thread-safe synchronized Singleton inside [AppDatabase.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/data/local/database/AppDatabase.kt) and refactored the app and worker to use it, preventing multi-instance concurrency locks or file corruptions.
* **Race Condition Fix**: Queries the latest DB record in [DashboardViewModel.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/ui/dashboard/DashboardViewModel.kt)'s payment handlers before running computations and updating, making fast clicks on stale UI elements safe.
* **Corrected Undo Completion logic**: Undoing a completed loan now checks if the previous installment count is still completed before setting `isActive` state (supporting multi-payment edge cases).
* **Aligned Validation**: Synchronized the save logic in [AddSubscriptionViewModel.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/ui/add_subscription/AddSubscriptionViewModel.kt) to match the strict inputs validation in the UI form.
* **Confirmation Dialog**: Implemented an explicit `AlertDialog` in [SubscriptionsScreen.kt](file:///Users/thawee.p/Workspaces/github/SubsMate/app/src/main/java/com/mate/subsmate/ui/subscriptions/SubscriptionsScreen.kt) to confirm deletions and prevent accidental data loss.

---

# Plan: Update GitHub Release Workflow

Modify the GitHub Action workflow to automatically build debug and release APKs and publish a new GitHub Release when a version tag is pushed.

## Tasks

- [x] **1. Update release.yml Triggers and Steps**
  - Modify [.github/workflows/release.yml](file:///Users/thawee.p/Workspaces/github/SubsMate/.github/workflows/release.yml) to trigger on tag pushes matching `v*`.
  - Add build steps for both `./gradlew assembleDebug` and `./gradlew assembleRelease`.
  - Configure the release step to automatically create a release and upload both debug and release APKs.

---

## Review & Verification Results (Workflow Update)
* **Configuration Integrity:** The updated [.github/workflows/release.yml](file:///Users/thawee.p/Workspaces/github/SubsMate/.github/workflows/release.yml) syntax is fully standard and valid.
* **Triggers:** Triggers automatically on any pushed tag beginning with `v` (e.g. `v1.0.0`) or via manual `workflow_dispatch`.
* **Actions Setup:** Generates both debug (`app-debug.apk`) and release (`app-release-unsigned.apk`) build configurations.
* **Release Creation:** Automatically publishes a new GitHub Release with the tag name and mounts both artifacts to it.



