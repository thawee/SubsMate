# SubsMate "Best in Class" UX/UI Polish Plan

## Phase 1: Premium Empty States
- [x] Create a reusable `EmptyStateView` composable (Icon + Title + Subtitle + Optional Action Button).
- [x] Integrate `EmptyStateView` into `DashboardScreen` (when no upcoming payments/loans).
- [x] Integrate `EmptyStateView` into `InsightsScreen` (when no spending data exists).

## Phase 2: List Micro-Interactions (Fluid UI)
- [x] Update `DashboardScreen` lists to use `Modifier.animateItem()` so items slide beautifully when marked as paid or undone.
- [x] Update `SubscriptionsScreen` list to use `Modifier.animateItem()` for smooth additions/removals.
- [x] Ensure `paymentHistory` lists use animations.

## Phase 3: Accessibility (a11y) For Data Visualizations
- [x] Add `semantics` blocks to `DonutChart` to read total spending out loud to screen readers.
- [x] Add `semantics` blocks to `MonthlySpendChart` to announce the highest spending month.
- [x] Add `semantics` blocks to `SpendingTrendChart` in the Dashboard.

## Phase 4: First-Launch Onboarding Flow
- [x] Add `KEY_HAS_COMPLETED_ONBOARDING` to `PreferenceManager`.
- [x] Create `OnboardingScreen.kt` using `HorizontalPager` with 3 premium slides:
  1. Track Subscriptions
  2. Manage Loans & Payments
  3. Get Notified
- [x] Update `MainActivity.kt` to route new users to `OnboardingScreen` first, then `DashboardScreen`.

## Phase 5: Verification
- [x] Build and test the app.
- [x] Verify onboarding only shows once.
- [x] Verify screen readers can read charts.

## Functional Review (2026-10-02)
- [x] Trace navigation, subscription, payment, loan, settings, and notification flows.
- [x] Run the project unit tests and check device availability for runtime validation.
- [x] Validate suspected defects with code tracing and a SQLite foreign-key reproduction.

## Functional Fixes (2026-10-02)
- [x] Preserve payment history on edits and use generated IDs for new loan payments.
- [x] Recalculate changed billing schedules and give projected charges unique list keys.
- [x] Keep snoozed reminders delayed and respect notification settings.
- [x] Add focused regression coverage, run checks, and review the final diff.

## Dashboard and Insights Review (2026-10-02)
- [x] Trace displayed values and actions back to payment and subscription data.
- [x] Inspect chart, list, empty, and loading states for functional and accessibility issues.
- [x] Validate findings with focused checks and report severity with file references.

## Dashboard and Insights Device Check (2026-10-02)
- [x] Open the app in an isolated emulator and capture Dashboard and Insights screen states.
- [x] Exercise date filters and screen navigation without changing stored entries.
- [x] Compare observed behavior with the code review findings and report what is verified.

## Dashboard and Insights Improvements (2026-10-02)
- [x] Compute totals, category breakdowns, and history within a selected currency; include zero-spend months.
- [x] Show completed loans in management and make the final payment reversible.
- [x] Separate recently paid items from upcoming charges and clarify the filter.
- [x] Fix theme-aware card colors and small nonzero percentage labels.
- [x] Add targeted tests, build, and validate screens on the emulator.

## Insights Next 12 Months Forecast
- [x] Add a pure forecast calculator for active subscriptions in the selected currency. Sum scheduled charges from today through the same date next year (end exclusive), grouped by category ID; advance dates with `BillingUtils.advanceByOneCycle` and stop loans at their remaining installment count.
- [x] Define edge-case treatment in the calculator: show overdue charges separately rather than silently moving them into the forecast; use the saved amount for variable-price charges only when it is known; omit unknown amounts and mark the forecast as incomplete; treat a trial's next billing date as its first projected charge.
- [x] Add Monthly equivalent / Next 12 months controls to Insights. Switch the overview total, donut, and category rows together; label the forecast as estimated and keep the six-month payment-history chart labelled as actual spending.
- [x] Cover monthly, yearly, custom-cycle, trial, finite-loan, overdue, unknown-price, category, currency, and date-boundary cases with focused calculator tests.
- [x] Run unit tests and a debug build; check both modes, empty states, labels, and narrow-screen layout on an emulator; review the diff against `main`.

## Subscription Notes UI
- [x] Add an optional multiline Notes input to the add/edit subscription form.
- [x] Load and save notes through the existing subscription entity without losing them on edits.
- [x] Show a short notes preview in subscription management for active and completed items.
- [x] Verify note creation, editing, clearing, and screen layout with focused checks and a build.

## Insights Upgrade: Timeline, Top Subscriptions, Drill-down (2026-10-02)
- [x] Add monthly forecast buckets to `ForecastUtils` and combine 6 past actual months with 12 forecast months into one timeline chart (forecast bars faded, "Today" divider).
- [x] Add a "Top subscriptions" ranked list by yearly cost for the selected currency.
- [x] Make category rows expandable to list their subscriptions with yearly cost.
- [x] Layout fixes: keep the donut next to its category list, collapse forecast caveats into one expandable line, format chart semantics amounts.
- [x] Unit tests for monthly buckets, yearly cost ranking, and category grouping; run tests, build, and check on the emulator.
