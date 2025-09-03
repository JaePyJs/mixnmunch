# Mix & Munch - Filipino Recipe Finder

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org)
[![Material3](https://img.shields.io/badge/Design-Material%203-blue.svg)](https://m3.material.io)

**Mix & Munch** is a Filipino-first Android app that helps users discover authentic Filipino recipes based on ingredients they have at home. The app combines sourced recipes from TheMealDB with transparent ingredient matching and Filipino cuisine expertise.

## 🎯 **Features**

- **Filipino Ingredient Support** - Enter ingredients in Filipino (sibuyas, bawang, kamatis) or English
- **Smart Recipe Matching** - Intelligent ranking with exact and partial match indicators  
- **Offline Recipe Storage** - Save recipes for offline access
- **Transparent Sourcing** - Clear attribution for all recipe sources
- **Accessibility First** - Large text support and screen reader compatibility
- **Material 3 Design** - Filipino-inspired color palette with modern Android UI

## 🏗️ **Architecture**

Built using **Clean Architecture** principles with:

- **UI Layer** - Jetpack Compose with Material 3
- **Presentation Layer** - ViewModels with UI state management
- **Domain Layer** - Use cases and business logic
- **Data Layer** - Repository pattern with Room database and Retrofit networking

## 🛠️ **Tech Stack**

| Component | Technology |
|-----------|------------|
| **Language** | Kotlin 1.9.24 |
| **UI Framework** | Jetpack Compose |
| **Architecture** | MVVM + Clean Architecture |
| **Dependency Injection** | Hilt |
| **Database** | Room with KSP |
| **Network** | Retrofit + OkHttp |
| **Serialization** | kotlinx.serialization |
| **Image Loading** | Coil |
| **Design System** | Material 3 |

## 📱 **Screenshots**

*Screenshots will be added as development progresses*

## 🚀 **Getting Started**

### **Prerequisites**
- Android Studio Arctic Fox or newer
- JDK 17 or higher
- Android SDK (API 34)
- Minimum Android 7.0 (API 24)

### **Setup**
1. Clone the repository
   ```bash
   git clone https://github.com/[your-username]/mix-and-munch-android.git
   cd mix-and-munch-android
   ```

2. Open in Android Studio
   ```bash
   # File → Open → Select project folder
   ```

3. Sync project and run
   ```bash
   # Wait for Gradle sync to complete
   # Build → Make Project (Ctrl+F9)
   # Run app on device/emulator
   ```

## 📖 **Project Structure**

```
app/
├── src/main/java/com/jmbar/mixandmunch/
│   ├── data/           # Data layer (API, database, repositories)
│   ├── domain/         # Business logic and use cases  
│   ├── presentation/   # UI components and ViewModels
│   ├── utils/          # Utility classes (ingredient normalization)
│   └── di/             # Dependency injection modules
├── src/main/res/       # Android resources
└── src/test/           # Unit tests
```

## 🧪 **Testing**

Run the test suite:
```bash
./gradlew test                    # Unit tests
./gradlew connectedAndroidTest    # Instrumentation tests
```

## 🌟 **Key Features Implementation**

### **Ingredient Normalization**
- Filipino to English translation (sibuyas → onion, bawang → garlic)
- Typo correction and text cleaning
- Maximum 6 ingredients per search

### **Recipe Ranking Algorithm**
Smart scoring system that considers:
- Ingredient match count
- Filipino cuisine relevance  
- Recipe completeness
- User search intent

### **Offline Support**
- Cached recipe data with TTL management
- Saved recipes accessible without internet
- Efficient data synchronization

## 🎨 **Design System**

The app uses a **Filipino-inspired Material 3** color palette:
- **Primary**: Filipino Red (#D32F2F)
- **Secondary**: Filipino Yellow (#FFA000)  
- **Tertiary**: Filipino Blue (#1976D2)

## 📋 **Roadmap**

- [x] Core architecture setup
- [x] Ingredient normalization system
- [x] TheMealDB API integration
- [x] Recipe ranking algorithm
- [ ] UI screen implementations
- [ ] Navigation between screens
- [ ] Comprehensive testing suite
- [ ] Performance optimizations
- [ ] Accessibility enhancements

## 🤝 **Contributing**

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

### **Development Guidelines**
- Follow Clean Architecture principles
- Write unit tests for new features
- Follow Material 3 design guidelines
- Ensure accessibility compliance
- Test on different screen sizes

## 📄 **License**

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 **Contact**

**JaePy** - [Your GitHub Profile]

Project Link: [https://github.com/[your-username]/mix-and-munch-android](https://github.com/[your-username]/mix-and-munch-android)

## 🙏 **Acknowledgments**

- [TheMealDB](https://www.themealdb.com/) for recipe data
- [Material Design](https://material.io/) for design guidelines
- [Android Open Source Project](https://source.android.com/) for the platform

---

*Built with ❤️ for Filipino cuisine enthusiasts*