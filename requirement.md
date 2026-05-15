# SubsMate - Android Subscription Tracker

## Vision
You probably have more subscriptions than you think. SubsMate helps you see exactly how much you’re paying every month and every year — so you can take control before small charges turn into big leaks.

---

## Core Features

### 1. Subscription Dashboard
*   **Real-time Cost Analysis:** Instantly calculate total monthly and yearly spending.
*   **Upcoming Charges:** A focused view of all charges occurring in the next 14 days.
*   **Category Breakdown:** (New) Visual representation of spending by category (Streaming, Utilities, Software, etc.).

### 2. Smart Reminders
*   **Renewal Notifications:** Configurable alerts before a subscription renews (e.g., 24h or 48h before).
*   **Trial End Alerts:** Specifically flag subscriptions marked as "Trials" to avoid accidental charges.

### 3. Service Template Library (New)
*   **Popular Templates:** Pre-defined configurations for global services (Netflix, YouTube Premium, Spotify, Viu Premium, Disney+, etc.) including logos and brand colors.
*   **AI Templates:** (New) Ready-to-use templates for AI services like ChatGPT Plus, Claude Pro, Midjourney, and Perplexity.
*   **Utility Templates:** Templates for recurring household costs like Internet, Electricity, Water, Mobile, and Rent.
*   **Auto-Fill Logic:** Selecting a template automatically sets the Category, Icon, and Color, reducing manual entry time.

### 4. Standard Categories
To provide better insights, SubsMate uses the following pre-defined categories:
1.  **Streaming:** Video and Music (Netflix, Spotify).
2.  **AI & Tools:** AI assistants and generation tools (ChatGPT, Claude).
3.  **Utilities:** Home services (Electricity, Water, Internet).
4.  **Communication:** Mobile plans and Landlines.
5.  **Software:** Productivity and Cloud apps (Google One, Adobe).
6.  **Entertainment:** Gaming, News, and Magazines (PS Plus).
7.  **Health:** Gym, Meditation, and Insurance.
8.  **Lifestyle:** Shopping and Food (Amazon Prime).
9.  **Education:** Courses and Learning apps (Duolingo).
10. **Finance:** Credit card fees and Trading tools.

### 5. Android Integration
*   **Home Screen Widgets:** Quick-glance widgets showing the next upcoming charge or total monthly spend.
*   **Material 3 Design:** A clean, minimal interface utilizing Material You dynamic color themes.
*   **Adaptive Notifications:** Rich notifications with action buttons (e.g., "Remind me later", "Mark as Paid").

---

## Pro Features (SubsMate Premium)
*   **Unlimited Subscriptions:** (Free tier limited to 5).
*   **Advanced Reminders:** Multiple reminder offsets.
*   **Cloud Sync:** Securely backup and sync data across Android devices using Google Drive or a backend.
*   **Premium Widgets:** Extra widget styles and customization.
*   **Ad-Free Experience.**

---

## Screen Flow & UI Architecture

### 1. Dashboard (Home)
*   **Overview:** High-level summary of financial health.
*   **Key UI:** Total spend cards (Monthly/Yearly), "Next 14 Days" list, and quick category spending chart.
*   **Primary Action:** FAB (Floating Action Button) to add a new subscription.

### 2. Subscription List
*   **Overview:** The "Digital Wallet" containing all active and inactive subscriptions.
*   **Key UI:** Searchable list with sorting by Name, Price, or Next Charge date.

### 3. Add / Edit Subscription
*   **Overview:** Multi-functional form with "Template Search" to quickly add famous services.
*   **Key UI:** Search bar for Service Templates, fields for Price (Currency), Billing Cycle, Category selection (with icons), Trial toggle, and Reminder configuration.

### 4. Insights & Analytics
*   **Overview:** Visual breakdown of spending habits over time.
*   **Key UI:** Bar charts for monthly trends and detailed category-wise cost distribution.

### 5. Settings & Profile
*   **Overview:** App configuration and monetization gateway.
*   **Key UI:** Subscription management (Pro upgrade), Notification preferences, Cloud Sync (Google Drive), and Theme selection.

---

## Technical Specifications (Android)
*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose (Material 3)
*   **Database:** Room (for local persistence)
*   **Background Tasks:** WorkManager (for scheduling notifications)
*   **Monetization:** Google Play Billing Library
*   **Architecture:** MVVM (Model-View-ViewModel)

---

## Subscription Plans
SubsMate offers optional auto-renewable subscriptions:
*   **Monthly:** Standard premium access.
*   **Yearly:** Best value for long-term control.
