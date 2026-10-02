# SubsMate UI/UX Guidelines

## 1. Brand Identity & "Mate Series" DNA
SubsMate follows the "Mate Series" design language, prioritizing a premium, dark-themed, glassmorphic aesthetic.

- **Primary Colors**: Deep Navy (`Navy900`, `Navy700`).
- **Backgrounds**: Slate/Navy 950 (`#0F172A`).
- **Accents**: 
  - Vibrant Emerald (`#10B981`) for positive actions/financial tracking.
  - Accent Blue (`#60A5FA`) for links, interactive elements, and chart highlights.
  - Gold (`#F59E0B`) for premium/money-related tertiary accents.

## 2. Iconography
The launcher icon reflects the premium nature of the app:
- **Metaphor**: A sleek, front-pocket wallet containing an abstract "Infinity-Link" geometry.
- **Symbolism**: The Infinity-Link represents the endless, cyclic nature of recurring subscriptions.
- **Monogram**: The classic Mate Series 'M', perfectly centered in the wallet pocket.

## 3. UI Components & Patterns
- **Glassmorphism**: Component cards (e.g., `GlassyCard`, `SubscriptionManageItem`) use translucent backgrounds (`GlassNavy.copy(alpha = 0.3f)`) over glowing `Canvas` background blobs.
- **Fluid Micro-Interactions**: Lists utilize `Modifier.animateItem()` to smoothly shift content when items are added, removed, or marked as paid.
- **Empty States**: A custom `EmptyStateView` greets users when lists are empty, providing branded illustrations and encouraging micro-copy rather than dead space.
- **Notes**: The add/edit form has an optional multi-line Notes card (3 to 5 lines, then scrolls). `SubscriptionManageItem` shows the first two lines with an ellipsis.
- **Insights Modes**: A Monthly equivalent / Next 12 months toggle switches the overview total, donut, category rows, and Top Subscriptions together. Forecast caveats sit under one expandable "About this estimate" line.
- **Insights Sections**: Order is overview (donut), category list (its legend, each row expandable to its subscriptions), Top Subscriptions (top 5, "Show all"), then the Spending Timeline.
- **Spending Timeline**: A horizontally scrolling bar chart of 17 months: 6 months of actual payments (solid primary) and scheduled charges for this month plus 11 ahead (primary at 45% alpha), stacked in the current month. It opens scrolled near the current month, which is labelled in bold primary; January labels carry the year.
- **Haptic Feedback**: Meaningful interactions (marking as paid, undoing, deleting) are grounded with physical `HapticFeedbackType.LongPress` feedback.

## 4. Accessibility (a11y)
- **Charts**: Complex data visualisations (`DonutChart`, `SpendingTimelineChart`, `SpendingTrendChart`) utilize `semantics` modifiers so screen readers (TalkBack) can read out summarized insights, with amounts formatted the same way as on screen.
- **Contrast**: Text primarily uses pure white (`#FFFFFF`) or high-contrast Slate variants against the deep Navy backgrounds.

## 5. Onboarding
New users are greeted with a swipeable, 3-slide `OnboardingScreen` explaining:
1. Tracking subscriptions.
2. Managing loans.
3. Enabling notifications.
This sets expectations and establishes the premium feel immediately upon launch.
