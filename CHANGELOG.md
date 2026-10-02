# Changelog

## [Unreleased]
### Added
- **Subscription Notes**: Add an optional multi-line note when adding or editing a subscription. The Subscriptions list shows a two-line preview, and clearing the note removes it.
- **Next 12 Months Forecast**: Insights can switch between the monthly equivalent and an estimate of scheduled charges over the next 12 months. The total, donut chart, and category list switch together. Overdue charges are shown separately, and variable-price charges without a saved amount are flagged as excluded.
- **Recently Paid**: The Dashboard lists recently paid items separately from upcoming charges.

### Changed
- **Per-Currency Totals**: Dashboard and Insights totals, category breakdowns, and payment history are calculated within the selected currency.
- **Payment History Chart**: Months with no spending now appear as zero, and the chart is labelled as actual past payments.
- **Completed Loans**: Completed loans appear under subscription management, and the final payment can be undone.

### Fixed
- Editing a subscription no longer erases its payment history, and new loan payments get their own IDs.
- Changing a billing date or cycle now recalculates the schedule, and projected charges no longer share list keys.
- Snoozed reminders stay delayed and respect the notification setting.
- Card colors follow the theme, and small nonzero percentages no longer show as 0%.

## [0.5.0] - UX & Polish Update
### Added
- **Premium Onboarding Flow**: Added a beautiful 3-slide introduction for first-time users.
- **Empty States**: Added branded illustrations and text for screens with no data (Dashboard, Insights, Subscriptions).
- **Chart Accessibility (a11y)**: Screen readers (TalkBack) now seamlessly read out summarized spending data from the Donut and Bar charts.
- **List Animations**: Used Compose 1.7 `Modifier.animateItem()` to fluidly animate cards sliding out when marked as paid or deleted.
- **Documentation**: Added `docs/DESIGN.md` and `docs/UI.md` to define architecture and the "Mate Series" design language.

### Changed
- **Launcher Icon**: Completely redesigned the launcher icon to match the premium "Mate Series" UI. Features a deep Navy/Slate background, a front-pocket wallet, and a custom Infinity-Link symbol for recurring payments.
- **Date Calculation Engine**: Refactored the billing cycle mathematics (`advanceByOneCycle`) to completely eliminate calendar skewing and skipping on edge-case dates (like Jan 31st).
- **Theming Cohesion**: Adjusted various accent colors to ensure the UI stays rooted in `Navy900` rather than clashing with global Emerald colors.

### Fixed
- Fixed an issue where editing a subscription would accidentally erase internal `nextBillingDate` sync progress.
- Fixed `RenewalNotificationWorker` to properly respect the in-app notification toggle instantly.

