# Mess Management System - Project Proposal

## 1. Introduction
The Mess Management System is a mobile-based application designed to automate and streamline the operations of hostel or cafeteria mess management. Traditional systems rely on manual record-keeping, which often leads to inefficiencies, errors, and lack of transparency. This system aims to digitize meal tracking, billing, and user management to improve overall efficiency.

## 2. Objectives
- Automate meal tracking and billing processes.
- Reduce manual errors and paperwork associated with ledger maintenance.
- Provide real-time updates on meal consumption and estimated costs.
- Improve communication between users and management through automated notifications.
- Ensure transparency in daily and monthly cost calculation.

## 3. Problem Statement
Manual mess management involves maintaining physical registers for daily meals, attendance, and payments. This setup frequently results in data inconsistency, difficulty in tracking individual usage, delayed and disputed billing, and an overall lack of transparency for the consumers.

## 4. Proposed Solution
The proposed system is an Android application that allows both consumers (users) and administrators (mess managers) to manage mess operations digitally. It includes user authentication, daily meal tracking, automated calculation of bills based on the dynamic meal rate, and real-time push notifications.

## 5. Key Features

### Consumer (User) Features:
- **Authentication:** Secure Registration and Login.
- **Dashboard:** View daily meal schedule, current meal rate, and upcoming meals.
- **Meal Management:** Select, edit, or cancel meal preferences within a stipulated time frame.
- **Billing & History:** View detailed itemized monthly bills and past payment history.
- **Profile:** Manage personal information and dietary preferences.

### Admin Features:
- **User Management:** Approve, suspend, or manage user profiles.
- **Meal & Inventory Management:** Update daily menus, track inventory costs, and set pricing.
- **Dynamic Billing Calculation:** Automatically calculate the "meal rate" (Total Cost / Total Meals) and assign charges to users.
- **Reporting:** Generate daily, weekly, or monthly consumption and financial reports.
- **Communication:** Send push notifications for announcements, dues, or schedule changes.

## 6. System Requirements

### Functional Requirements:
- Role-Based Access Control (RBAC) system for Admins and normal Users.
- Meal tracking system with cutoff times for registering/canceling meals.
- Automated billing module synchronized with total monthly expenses.
- Report generation module with export options (e.g., PDF/CSV).

### Non-Functional Requirements:
- **Usability:** Intuitive, user-friendly interface built with modern Material Design 3 guidelines.
- **Security:** Secure data handling and authentication mechanisms.
- **Performance:** Fast response times and offline-read capabilities.
- **Scalability:** System engineered to support a growing number of users without degraded performance.

## 7. Technology Stack
- **Frontend:** Android (Kotlin, Jetpack Compose, Material 3)
- **Architecture Pattern:** MVVM (Model-View-ViewModel) + Clean Architecture principles
- **Backend/BaaS:** Firebase
  - *Authentication:* Firebase Auth (Email/Password, potentially Google Sign-In)
  - *Database:* Cloud Firestore (NoSQL, real-time sync)
  - *Storage:* Firebase Cloud Storage (for profile pictures/receipts)
  - *Notifications:* Firebase Cloud Messaging (FCM)
- **Local Persistence (Optional/Recommended):** Room Database or Datastore (for caching user preferences)
- **Version Control & CI/CD:** GitLab/GitHub, GitHub Actions.

## 8. Expanded Technical Architecture
The system follows a client-server architecture with reactive data streams. 
1. **UI Layer:** Jetpack Compose screens observe StateFlow out of ViewModels. 
2. **Domain Layer (ViewModels):** Handle UI logic and invoke specific UseCases/Repositories.
3. **Data Layer (Repositories):** Interface directly with Firebase Services.
   - *Firestore Structure Overview:* 
     - `/users/{userId}`: Stores profile and role data.
     - `/meals/{date}`: Daily meal rosters and menus.
     - `/transactions/{txId}`: Payment logs and billings.
4. **Offline Support:** Firestore’s built-in offline caching ensures users can view data even when momentarily disconnected.

## 9. Project Timeline & Roadmap (10-Week Plan)

- **Phase 1: Requirements & Design (Weeks 1-2)**
  - Finalize database schema and user flows.
  - Create UI/UX wireframes and high-fidelity mockups.
- **Phase 2: Project Setup & Authentication (Weeks 3-4)**
  - Initialize Android repository and dependency injection (e.g., Hilt/Dagger).
  - Setup Firebase Auth and user profile creation logic.
- **Phase 3: Core Logic & Meal Tracking (Weeks 5-6)**
  - Implement Consumer dashboard and meal selection functionality.
  - Build Admin dashboard for meal scheduling and daily management.
- **Phase 4: Billing, Reports & Admin Features (Weeks 7-8)**
  - Develop the dynamic billing calculation engine.
  - Create billing views and reporting interfaces.
- **Phase 5: Refinement & Testing (Weeks 9-10)**
  - Integrate Firebase Cloud Messaging for notifications.
  - Conduct Quality Assurance (QA), unit tests, and fix bugs.
  - Final deployment and release preparation.

## 10. Expected Outcomes
- Highly efficient and transparent mess operations.
- Accurate and dispute-free billing.
- Improved user satisfaction through a modern mobile interface.
- Drastically reduced administrative workload for mess managers.

## 11. Future Enhancements
- **Payment Gateway Integration:** Direct payment via mobile banking (bKash, Nagad).
- **Advanced Analytics:** Visual dashboard for historical cost trends and predictions.
- **Feedback System:** Rating system for daily meals to improve food quality.
- **Inventory Tracking Integration:** Linking daily purchases to automatically affect the dynamic meal rate.

## 12. Conclusion
The Mess Management System provides a modern, efficient, and scalable solution to traditional mess management problems. By leveraging Firebase and modern Android development practices, it enhances transparency, reduces manual work, and significantly improves the overall user experience.
