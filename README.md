# Smart Pantry Manager

## Project Description
Smart Pantry Manager is an Android application developed in Java to help users manage pantry ingredients and identify recipes that can be prepared using the ingredients they have available.

## Features
- Add pantry ingredients with their available quantities.
- Edit ingredient names and quantities.
- Delete ingredients from the pantry.
- Store pantry information locally using SQLite.
- Browse 20 recipe options.
- Suggest recipes only when every required ingredient is available in a sufficient quantity.
- View recipe details, required ingredients, and preparation guidance.
- Access a profile and settings screen.
- Receive feedback when no recipes match the available pantry ingredients.

## Technology Used
- Java
- Android Studio
- SQLite
- Android SDK
- Git and GitHub

## Database
The application uses a local SQLite database named `smart_pantry.db` to store pantry ingredients and their quantities. Ingredient names are matched without case sensitivity. Recipe suggestions are checked against the required ingredient quantities.

## How to Run the Project
1. Download or clone this repository.
2. Open Android Studio.
3. Select **Open** and choose the downloaded project folder.
4. Allow Gradle to sync.
5. Select an Android emulator or connected Android device.
6. Run the application.

## Repository Contents
- `app/` — Android application source code and resources.
- `build.gradle` — Project-level Gradle configuration.
- `settings.gradle` — Gradle project settings.
- `gradle/` — Gradle wrapper configuration.
## Current Implementation Notes

- **Pantry storage:** Ingredient names and quantities are stored in a local SQLite database.
- **Recipe matching:** Suggested recipes are displayed only when all required ingredients are available in sufficient quantities.
- **Input validation:** The app checks ingredient names and quantities before saving.
- **Recipe data:** The current recipe collection is defined in the Java source code.
- **Profile:** Profile information is currently held in memory and is not permanently stored.

## Testing

The pantry and recipe-matching functionality can be tested by adding ingredients with different quantities and checking whether the suggested recipes change accordingly. Invalid ingredient entries should display a validation message.

## Student Information
**Student Number:** 402410460  
**Module:** Mobile App Development 700
