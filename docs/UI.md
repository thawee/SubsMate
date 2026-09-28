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
- **Haptic Feedback**: Meaningful interactions (marking as paid, undoing, deleting) are grounded with physical `HapticFeedbackType.LongPress` feedback.

## 4. Accessibility (a11y)
- **Charts**: Complex data visualisations (`DonutChart`, `MonthlySpendChart`) utilize `semantics` modifiers so screen readers (TalkBack) can read out summarized insights.
- **Contrast**: Text primarily uses pure white (`#FFFFFF`) or high-contrast Slate variants against the deep Navy backgrounds.

## 5. Onboarding
New users are greeted with a swipeable, 3-slide `OnboardingScreen` explaining:
1. Tracking subscriptions.
2. Managing loans.
3. Enabling notifications.
This sets expectations and establishes the premium feel immediately upon launch.
