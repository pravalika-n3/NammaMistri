#  Namma Mistri

A Kotlin Android application designed for **rural construction site management**.
The app helps contractors, site supervisors, and local builders efficiently manage construction activities, workers, materials, and cost estimations in one place.



#  Problem Statement

Managing rural construction sites manually can lead to:

* Poor worker tracking
* Incorrect material calculations
* Difficulty maintaining daily expenses
* Lack of organized site records
* Miscommunication between owners and supervisors

**Namma Mistri** solves these challenges by providing a simple Android-based digital management system tailored for construction activities in rural and small-scale projects.


# Features

## 🏠 Site Management

* Add and manage construction sites
* Store:

  * Site owner name
  * Location details
  * Contact number

## 🧱 Material Calculator

Calculate construction materials required for:

* Walls
* Rooms

Includes estimation for:

* Bricks
* Cement bags
* Sand loads

## 👷 Worker Management

Track:

* Worker names
* Daily wages
* Days present
* Advance payments
* Balance due

## 💰 Material Rate Management

* Maintain local construction material prices
* Update rates anytime

## 📸 Site Progress Tracking

* Add progress photo references
* Select images directly from the device gallery

## 📱 Modern Android UI

* Built using **Jetpack Compose**
* Clean and responsive UI


# 🛠️ Tech Stack

| Technology      | Usage                     |
| --------------- | ------------------------- |
| Kotlin          | Main programming language |
| Jetpack Compose | Modern Android UI toolkit |
| Android Studio  | Development IDE           |
| Gradle          | Build system              |
| Material Design | UI components and styling |


# 📦 Package Name

```kotlin
com.example.nammamistri
```

---

#  Installation Steps

## 1️⃣ Clone the Repository

```bash
git clone <repository-url>
```

## 2️⃣ Open in Android Studio

* Launch Android Studio
* Select **Open**
* Choose the project folder

## 3️⃣ Sync Gradle

Allow Gradle to:

* Download dependencies
* Configure Android Gradle Plugin
* Sync Jetpack Compose libraries

## 4️⃣ Run the App

* Connect an Android device
  OR
* Start an emulator

Then click:

```bash
Run ▶
```


# ▶️ Run Command

Using Gradle:

```bash
./gradlew assembleDebug
```

Or directly run from Android Studio.

---

#  Folder Structure

```plaintext
NammaMistri/
│
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/nammamistri/
│   │   │   ├── res/
│   │   │   └── AndroidManifest.xml
│   │
│   ├── build.gradle
│
├── gradle/
├── build.gradle
├── settings.gradle
└── README.md
```

#  Future Improvements

* Cloud database integration
* Offline data synchronization
* Multi-language support
* PDF report generation
* Expense analytics dashboard
* Attendance calendar view
* GPS-based site tracking
* Admin and worker login system
* Firebase authentication
* Data backup & restore

---

# Developed By

**Pravalika N**



# 📄 License

This project is developed for educational and practical rural construction management purposes.

