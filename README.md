# HealthTracker - Android Health & Fitness App

A comprehensive health tracking Android app built with **Kotlin** and **Jetpack Compose**. Track your weight, diet, gym lifts, and get personalized predictions and tips for reaching your fitness goals.

---

## Features

### 1. Onboarding & Profile Setup
When you first launch the app, you'll be greeted with a setup screen that collects:
- **Name** — personalized greetings throughout the app
- **Current Weight** (lbs) — your starting point
- **Goal Weight** (lbs) — your target
- **Height** (feet/inches) — used for BMR calculations
- **Age** — used for metabolic rate estimation
- **Activity Level** — Sedentary, Lightly Active, Moderately Active, Very Active, or Extra Active

This profile is stored locally in a Room database and is used to power all calculations and predictions.

### 2. Dashboard (Home)
The main screen shows:
- **Personalized greeting** with your name
- **Current weight** and **goal weight** side by side
- **Total weight lost** since you started
- **Progress bar** showing how far you are toward your goal
- **Today's calorie and protein intake** at a glance
- **Recent weight log** — a chronological list of your logged weigh-ins
- **FAB button** to quickly log a new weight entry

### 3. Diet Tracking
Full meal logging with macro breakdowns:
- **Log meals** with name, calories, protein, carbs, and fat
- **Meal type categorization** — Breakfast, Lunch, Dinner, Snack, or Other
- **Daily summary cards** showing:
  - Calories consumed vs. your TDEE target (with progress bar)
  - Protein consumed vs. your recommended intake (1.6g per kg body weight)
  - Carbs and fat totals
- **Swipe-to-delete** entries you want to remove
- All entries are timestamped and grouped by day

### 4. Lift Tracker & Personal Records
Track your strength training progress:
- **Log any exercise** — enter exercise name, weight, reps, and sets
- **Auto-detect PRs** — the app automatically flags when you hit a new personal record for any exercise
- **PR badges** displayed prominently at the top with a trophy icon
- **Filter by exercise** — chip-based filter to view history for specific lifts
- **Exercise suggestions** — previously logged exercises appear in a dropdown for quick re-entry
- **PR highlighting** — entries that were personal records get a special colored card

### 5. Insights & Predictions
Data-driven predictions and personalized recommendations:

**Your Numbers:**
- **BMR** (Basal Metabolic Rate) — calories burned at complete rest, calculated using the Mifflin-St Jeor equation
- **TDEE** (Total Daily Energy Expenditure) — BMR adjusted for your activity level
- **Average daily calorie intake** — computed from your diet log
- **Daily caloric deficit/surplus**

**Weight Predictions:**
- Projected weight at **4 weeks**, **8 weeks**, and **12 weeks** based on your current eating patterns
- Shows how many pounds you'll gain or lose at each milestone
- Based on the principle that 3,500 calories = 1 pound of body weight

**Personalized Tips:**
- Whether you're in a surplus or deficit and by how much
- Protein intake recommendations based on body weight
- Time-to-goal estimates at your current rate
- Hydration recommendations (half your body weight in ounces)
- General nutrition guidance

---

## Architecture

```
com.healthtracker/
├── data/
│   ├── model/          # Data classes: UserProfile, WeightEntry, DietEntry, LiftEntry, Exercise
│   ├── database/       # Room database, DAOs for each entity
│   └── repository/     # HealthRepository — single source of truth, business logic
├── ui/
│   ├── theme/          # Material 3 theming (green health-oriented palette)
│   ├── navigation/     # Screen definitions, bottom nav items
│   ├── components/     # Reusable composables: StatCard, ProgressCard, PRBadge, NumberTextField
│   ├── screens/        # OnboardingScreen, DashboardScreen, DietScreen, LiftsScreen, PredictionsScreen
│   └── HealthViewModel.kt  # Single ViewModel managing all app state
├── util/
│   └── DateUtils.kt    # Date formatting and day-range helpers
└── MainActivity.kt     # Entry point, NavHost setup, bottom navigation
```

### Tech Stack
| Layer | Technology |
|-------|-----------|
| UI | Jetpack Compose + Material 3 |
| Navigation | Compose Navigation with bottom nav |
| State | StateFlow + collectAsState |
| Persistence | Room Database (SQLite) |
| Architecture | MVVM (ViewModel + Repository) |
| Build | Gradle KTS with KSP for Room annotation processing |

### Data Flow
1. **User actions** trigger ViewModel functions (e.g., `addWeight()`, `addDietEntry()`)
2. **ViewModel** calls Repository methods inside `viewModelScope` coroutines
3. **Repository** executes Room DAO operations
4. **Room** returns `Flow<T>` for reactive data
5. **Compose UI** collects flows via `collectAsState()` and recomposes automatically

---

## How the Predictions Work

### BMR Calculation (Mifflin-St Jeor Equation)
```
BMR = (10 × weight_kg) + (6.25 × height_cm) - (5 × age) + 5
```

### TDEE (Total Daily Energy Expenditure)
```
TDEE = BMR × Activity Multiplier

Multipliers:
  Sedentary:        1.2
  Lightly Active:   1.375
  Moderately Active: 1.55
  Very Active:      1.725
  Extra Active:     1.9
```

### Weight Prediction
```
Weekly Deficit = (TDEE - Avg Daily Calories) × 7
Weekly Weight Change = Weekly Deficit / 3500 lbs
Predicted Weight = Current Weight - (Weekly Change × Weeks)
```

### Protein Recommendation
```
Recommended Daily Protein = Body Weight (kg) × 1.6g
```

---

## Setup & Building

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34

### Steps
1. Open the `HealthTracker/` directory in Android Studio
2. Let Gradle sync (it will download dependencies automatically)
3. Connect a device or start an emulator (API 26+)
4. Click **Run** or use `./gradlew installDebug`

### First Launch
1. The app opens to the **onboarding screen**
2. Fill in your profile details and tap **Get Started**
3. You'll land on the **Dashboard** — log your first weight!
4. Navigate between tabs using the bottom navigation bar:
   - **Dashboard** — weight tracking + overview
   - **Diet** — meal logging + macros
   - **Lifts** — gym tracking + PRs
   - **Insights** — predictions + tips

---

## Key Design Decisions

- **All local storage** — no network calls, no accounts. Your data stays on your device.
- **Single ViewModel** — keeps things simple for a focused app. Scales well for this feature set.
- **Auto PR detection** — every lift entry is compared against your previous best. No manual flagging needed.
- **TDEE as calorie target** — the diet screen uses your calculated TDEE as the daily calorie target, making deficit tracking automatic.
- **Flow-based reactivity** — all data flows from Room through the ViewModel to the UI. Changes are reflected instantly.
- **Material You support** — on Android 12+, the app picks up your device's dynamic color theme. Falls back to a green health-oriented palette on older devices.
