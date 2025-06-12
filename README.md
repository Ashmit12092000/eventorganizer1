# Event Organizer Android App

A feature-rich, modern Android application built in Java for managing and discovering local events. This app features a clean, intuitive user interface and a robust backend powered by Firebase. It includes distinct roles for regular users and administrators, ensuring a secure and well-managed event ecosystem.

## Table of Contents
- [Features](#features)
  - [User Features](#user-features)
  - [Admin Features](#admin-features)
- [App Architecture](#app-architecture)
- [Tech Stack & Libraries](#tech-stack--libraries)
- [Setup & Installation](#setup--installation)
- [Screenshots](#screenshots)

## Features

### User Features

* **Authentication**: Secure user registration and login with email and password.
* **Automatic Login**: Remembers user sessions for a seamless experience.
* **Randomized Avatars**: New users are automatically assigned one of several unique profile pictures upon registration.
* **Profile Management**: Users can view their profile and change their profile picture at any time.
* **Event Creation**: Users can create new events with details like title, description, date, time, location, and a poster image uploaded from their device's gallery.
* **Hosted Events List**: The user's profile displays a clean list of all events they have personally created.
* **Event Discovery**: A main "Events" tab shows a list of all public events, which can be viewed in detail.
* **Interactive Commenting**: Users can post and delete their own comments on any event detail page.

### Admin Features

* **Role-Based Privileges**: Admins have all the features of a regular user, plus powerful moderation capabilities.
* **Universal Event Management**: From the dedicated "Manage Events" screen, an admin can **edit** or **delete any event** created by any user.
* **Universal Comment Moderation**: Admins can **delete any comment** on any event, ensuring a safe and positive community environment.

## App Architecture

This project follows a modern **Single-Activity, Multi-Fragment** architecture, which is the recommended standard for Android development.

* **Single Host Activity (`MainActivity`)**: Manages the `BottomNavigationView` and hosts the different fragments.
* **Fragments**: `HomeFragment`, `EventsFragment`, and `ProfileFragment` are used to display the main sections of the app, providing a smooth, flicker-free user experience.
* **MVVM Pattern (Model-View-ViewModel)**: The project structure is organized to separate UI from business logic, making the code cleaner and more maintainable.
* **Repository Pattern**: Handles data operations, abstracting the data source (Firebase) from the rest of the app.

## Tech Stack & Libraries

* **Language**: **Java**
* **UI Toolkit**: **XML** with Material Design 3 Components for a modern and responsive user interface.
* **Backend Services**:
    * **Firebase Authentication**: For secure user login and registration.
    * **Cloud Firestore**: As the primary NoSQL database for storing event details, user profiles, and comments.
* **Image Uploads**: **ImgBB** (via a REST API) is used as a free, no-billing-required alternative for permanent image hosting.
* **Image Loading**: **Glide** for efficiently loading, caching, and displaying images from URLs.
* **Networking**: **OkHttp** for making robust API calls to the ImgBB service.
* **UI Enhancements**:
    * **Facebook Shimmer**: Provides a professional loading animation while data is being fetched.
    * **Material Components**: `MaterialToolbar`, `BottomNavigationView`, `ShapeableImageView`, etc.

## Setup & Installation

Follow these steps to get the project running on your local machine.

1.  **Clone the Repository**
    ```bash
    git clone [https://github.com/your-username/event-organizer-app.git](https://github.com/your-username/event-organizer-app.git)
    ```

2.  **Firebase Setup**
    * Go to the [Firebase Console](https://console.firebase.google.com/) and create a new project.
    * Add a new Android app to the project with the package name `com.example.eventorganizer`.
    * Follow the setup steps to download the `google-services.json` file.
    * Place the downloaded `google-services.json` file in the `app/` directory of the project.
    * In the Firebase Console, enable **Authentication** (with the Email/Password provider) and **Cloud Firestore**.

3.  **ImgBB API Key Setup**
    * Go to [https://api.imgbb.com/](https://api.imgbb.com/) and get a free API key.
    * In the root directory of the project, find or create a file named `local.properties`.
    * Add your API key to this file. This file is ignored by Git and keeps your key secure.
        ```properties
        IMGBB_API_KEY="YOUR_API_KEY_FROM_IMGBB"
        ```

4.  **Add SHA-1 Keys to Firebase**
    * In Android Studio, open the **Gradle** tab on the right.
    * Navigate to **`YourProjectName` > `:app` > `Tasks` > `android`** and run the `signingReport` task.
    * Copy the **SHA-1** certificate fingerprint for the `debug` variant.
    * In your Firebase Project Settings, under the "Your apps" card, add this SHA-1 fingerprint.

5.  **Build and Run**
    * Open the project in Android Studio. It will sync the Gradle files automatically.
    * Build and run the app on an emulator or a physical device.

## Screenshots

*(Here you would insert screenshots of your app's main screens)*

| Login Screen | Home (Dashboard) | Events List |
| :----------: | :--------------: | :---------: |
|   *Image* |      *Image* |   *Image* |

| Event Details | Profile Screen | Add New Event |
| :-----------: | :------------: | :-----------: |
|    *Image* |     *Image* |     *Image* |

---
*This README was generated for the Event Organizer app project.*
