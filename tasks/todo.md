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
