# SubsMate - Android Subscription Tracker

SubsMate is a clean, minimal, and powerful subscription management app for Android. Part of the **Mate Series** (MusicMate, TradingMate, FarmMate), it helps users visualize their recurring costs and avoid "financial leaks."

## 🚀 Key Features

- **Dashboard:** Instantly see your total monthly and yearly spending.
- **Smart Templates:** Quick-add famous services like Netflix, ChatGPT Plus, YouTube Premium, and more.
- **Insights:** Visual breakdown of spending by category (Streaming, AI, Utilities, etc.).
- **Smart Reminders:** Integrated with Android WorkManager for renewal notifications.
- **Material 3:** Modern, adaptive UI using Jetpack Compose and Material You.
- **Pro Features:** Unlock unlimited subscriptions and cloud sync.

## 🛠 Technical Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Database:** Room Persistence Library
- **Architecture:** MVVM + Clean Architecture
- **Background Tasks:** WorkManager for notifications
- **Asynchronous Flow:** Kotlin Coroutines & Flow

## 📂 Project Structure

- `data/`: Room entities, DAOs, Database, and WorkManager implementations.
- `domain/`: Business logic models and Repository interfaces.
- `ui/`: Compose-based screens, ViewModels, and App Theme.

## 📈 Roadmap

- [ ] Google Drive Cloud Sync integration.
- [ ] Customizable Home Screen Widgets.
- [ ] Receipt scanning for auto-subscription entry.

---
Built as part of the **Mate Series** utility ecosystem.
