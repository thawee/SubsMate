# Changelog

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

