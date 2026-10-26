# FyndStay

An Android app for finding and listing PG (paying-guest) accommodation in Mumbai.

Students browse PG listings, search and filter them, save favourites to a wishlist, and send an
interest request to the owner. Owners register their PGs, manage their listings, and see the
requests students have sent them. Everything is backed by Firebase (Auth + Firestore + Storage).

---

## Features

**Students**
- Email/password sign-up and login (Firebase Auth), with "forgot password" reset email
- Browse all PG listings, newest first
- Suggested PGs: curated sample listings across 18+ Mumbai areas (Andheri, Bandra, Juhu, Powai, Vile Parle, Santacruz, Dadar, Kurla, Chembur, Goregaon, Malad, Borivali, Colaba, Fort, Lower Parel, Worli, Navi Mumbai, Thane), marked "Suggested", so the app never looks empty
- Quick-search chips for popular Mumbai areas above the listings
- Live search as you type
- Filters: locality, floor, gender restriction, rent range, in-time restriction, meals (breakfast / lunch / dinner)
- Wishlist - tap the heart on any listing to save it, view saved PGs in a separate screen (works for suggested listings too)
- PG detail screen with full amenity info
- "Send Request" to express interest to the owner
- "Contact" opens the dialer with the owner's number

**Owners**
- Separate owner registration flow (name, email, PG name, city)
- Add a PG listing with address, rent, contact, locality, floor, gender restriction, in-time, and amenity toggles (meals, veg/non-veg, kitchen access, induction, iron, overnight pass)
- Edit or delete an existing listing, including replacing the listing photo (uploaded to Firebase Storage; camera uses a thumbnail)
- View all requests students have sent for their PGs

The app decides which dashboard to show after login by reading the `role` field on the user's
Firestore document (`student` or `owner`).

---

## Tech stack

| Piece | What's used |
|---|---|
| Language | Kotlin (all app and test source files) |
| Min / target SDK | 24 / 34 (compileSdk 34) |
| Build | Gradle 8.7 via the wrapper, Android Gradle Plugin 8.4.1, Kotlin Gradle plugin 2.1.20 |
| JVM bytecode | Java 11 (Kotlin jvmTarget 11), Gradle runs on JDK 17 |
| UI | Views + ViewBinding, Material Components 1.12.0, ConstraintLayout, RecyclerView |
| Backend | Firebase Auth, Cloud Firestore, Firebase Storage (BoM 34.4.0) |
| Images | Glide 4.16.0 |
| Fonts | Poppins (bundled in `res/font`) |

---

## Project structure

```
FyndStay/
├── app/
│   ├── google-services.json          ← NOT included; add your own (setup step 5)
│   ├── build.gradle.kts              ← app dependencies
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/fyndstay/  ← Kotlin .kt files (Android source root)
│       │   ├── MainActivity.kt             splash (2s) → Login
│       │   ├── LoginActivity.kt            login + role-based routing
│       │   ├── ChooseRoleActivity.kt       "register as student or owner"
│       │   ├── RegisterStudentActivity.kt
│       │   ├── RegisterOwnerActivity.kt
│       │   ├── StudentDashboardActivity.kt listings, search, filters, wishlist toggles
│       │   ├── FiltersActivity.kt          filter sheet, returns extras to the dashboard
│       │   ├── WishlistActivity.kt
│       │   ├── PGDetailActivity.kt         detail view + request/contact/edit
│       │   ├── OwnerDashboardActivity.kt   owner's own listings
│       │   ├── AddPGActivity.kt
│       │   ├── EditPGActivity.kt           edit / delete / replace photo
│       │   ├── OwnerRequestsActivity.kt
│       │   ├── PG.kt, RequestItem.kt      data models
│       │   ├── PGAdapter.kt, RequestAdapter.kt
│       │   └── Utils.kt                    toast, logout dialog, list chunking
│       └── res/                      layouts, drawables, menus, fonts, arrays
├── gradle/                           Gradle wrapper
├── build.gradle.kts                  root build file
├── settings.gradle.kts
└── local.properties                  ← machine-specific, regenerated per laptop
```

---

## Firestore data model

```
users/{uid}
  uid, name, email, role ("student" | "owner")
  owners also have: pgName, city

PGs/{autoId}
  pgName, address, rent, contactNumber, locality, floor,
  genderRestriction, city, ownerId, imageUrl, timestamp,
  hasInTime, inTime,
  breakfastProvided, lunchProvided, dinnerProvided,
  vegMeals, nonVegAllowed, kitchenAccess,
  inductionProvided, ironAvailable, overnightPassAvailable

Wishlists/{studentUid}/UserWishlist/{pgId}
  pgId, timestamp

Requests/{ownerUid}/receivedRequests/{autoId}
  studentId, studentName, pgId, pgName, timestamp
```

Listing photos live in Firebase Storage under `pg_images/`.

---

# Setup — running this on a fresh laptop

Follow these in order. Works on Windows, macOS, or Linux.

### 1. Install Android Studio

Download from <https://developer.android.com/studio> and install it (the latest stable release is
fine — anything from Hedgehog onwards). Android Studio ships with its own JDK, so **you do not need
to install Java separately** as long as you build from inside Android Studio.

On first launch, accept the setup wizard defaults. It will download the Android SDK for you.

### 2. Install the right SDK pieces

In Android Studio: **Settings/Preferences → Languages & Frameworks → Android SDK**
(or **More Actions → SDK Manager** from the welcome screen).

- **SDK Platforms** tab → tick **Android 14.0 (API level 34)**
- **SDK Tools** tab → make sure these are ticked:
  - Android SDK Build-Tools
  - Android SDK Platform-Tools
  - Android Emulator
  - Android SDK Command-line Tools

Click **Apply** and let it download.

### 3. Open the project

**File → Open** → select the `FyndStay` folder (the one containing `settings.gradle.kts`) → **OK**.

Android Studio will index and then run a Gradle sync automatically. The first sync downloads Gradle
8.7 and all dependencies — this takes a few minutes on a fresh machine. Let it finish.

If it asks about a Gradle JDK, pick a **JDK 17** entry (Android Studio bundles one — usually listed
as "jbr-17" or "Embedded JDK").

### 4. `local.properties`

This file points Gradle at your Android SDK, and it is **specific to each machine**, so it isn't
included here. Android Studio normally creates it for you during the first sync — if the sync
succeeds, you can skip this step entirely.

If you hit `SDK location not found`, create `local.properties` in the project root with:

```properties
# macOS
sdk.dir=/Users/YOUR_NAME/Library/Android/sdk

# Windows (note the double backslashes)
sdk.dir=C\:\\Users\\YOUR_NAME\\AppData\\Local\\Android\\Sdk

# Linux
sdk.dir=/home/YOUR_NAME/Android/Sdk
```

Keep only the line for your OS.

### 5. Firebase config — you need your own Firebase project

The app talks to Firebase for everything: accounts, listings, wishlists, requests, photos. The
config file that connects it (`app/google-services.json`) is **not included in this repo** — it
is tied to your own Firebase project, so add your own. **The build fails without it**,
with `File google-services.json is missing`.

In the Firebase console:

1. Go to <https://console.firebase.google.com> and click **Create a project**.
   Give it any name (e.g. `fyndstay-dev`). Google Analytics is optional — you can turn it off.
2. On the project overview page, click the **Android** icon to register an Android app:
   - **Package name:** `com.example.fyndstay` — this must match exactly, or the build will reject the file
   - App nickname and SHA-1: leave blank, they aren't needed here
3. Click **Register app**, then **Download google-services.json**.
4. Put that file in the **`app/`** folder of this project — i.e. `app/google-services.json`,
   sitting next to `app/build.gradle.kts`. Don't rename it.
5. Back in the Firebase console sidebar, turn on the three services the app uses. It will crash or
   silently fail without them:
   - **Build → Authentication** → *Get started* → **Sign-in method** tab → **Email/Password** → *Enable* → Save
   - **Build → Firestore Database** → *Create database* → choose a location → choose rules suitable for your own demo environment
   - **Build → Storage** → *Get started* → choose rules suitable for your own demo environment

No collections or documents need to be created by hand. The app writes them the first
time you register a user and add a listing.

> **Security:** Public test-mode Firestore or Storage rules expose your data and may expire. Use an isolated demo project and restrict rules to signed-in users before sharing the app. Firebase Storage availability and billing depend on the current Firebase plan; check the console for your project.

Because the Firebase project is yours, its data is yours too — you'll start with an empty database
and populate it with your own test accounts in step 8.

### 6. Set up a device

**Emulator:** **Tools → Device Manager → Create Device** → pick e.g. Pixel 6 → choose a system image
of **API 34** (download it if prompted) → Finish → hit ▶ to boot it.

**Physical phone:** enable Developer Options (Settings → About phone → tap "Build number" 7 times),
turn on **USB debugging**, plug it in, and accept the debugging prompt on the phone.

Either way the device needs an internet connection — everything is served from Firebase.

### 7. Run it

Pick your device in the toolbar dropdown and click the green ▶ **Run** button (or `Shift + F10`).

First build takes a few minutes; after that it's fast. The app opens on a 2-second splash, then the
login screen.

### 8. Try it out

There are no seeded accounts — create your own:

1. On the login screen tap **Register**
2. Choose **Owner**, sign up, and add a PG listing from the owner dashboard
3. Log out, register again as a **Student** with a different email
4. The listing you created appears on the student dashboard — search it, filter it,
   heart it into the wishlist, open it, and send a request
5. Log back in as the owner to see that request under **Requests**

---

## Building from the command line (optional)

You still need the Android SDK installed and `local.properties` pointing at it. You also need
**JDK 17** as the Gradle JDK (check `java -version` for command-line builds).

```bash
./gradlew assembleDebug
```

On Windows use `gradlew.bat assembleDebug`. The APK lands in
`app/build/outputs/apk/debug/app-debug.apk`.

Install it on a connected device:

```bash
./gradlew installDebug
```

---

## Troubleshooting

| Problem | Fix |
|---|---|
| `SDK location not found` | `local.properties` is missing or has the wrong path — see step 4. |
| `File google-services.json is missing` | Expected on a fresh clone — create a Firebase project and drop the file into `app/`. See step 5. |
| Gradle sync fails on a fresh machine | Check you're online; the first sync downloads Gradle 8.7 and every dependency. Then **File → Invalidate Caches → Invalidate and Restart**. |
| `Unsupported class file major version` / JDK errors | Set the Gradle JDK to 17: **Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK**. |
| Login says "User record not found in Firestore" | The Auth account exists but its `users/{uid}` document doesn't. Register a fresh account instead of reusing that email. |
| Listings screen is empty | No PGs have been created yet, or Firestore rules are blocking reads — create one from an owner account and check the rules. |
| Images don't load | Firebase Storage isn't enabled, or its rules are blocking reads. |
| Emulator can't reach Firebase | The emulator has no network — cold boot it from Device Manager, or check the host machine's connection. |

---

## Verification and what is still needed

- All app source and test files are Kotlin (`.kt`); no Java source remains.
- `:app:assembleDebug` and `:app:testDebugUnitTest` passed locally with JDK 17, SDK API 34, and a **throwaway config used for build validation only**. That config and the resulting APK are deliberately not in this ZIP. The passing unit test is a simple template test, not a Firebase integration test.
- You must provide your own `app/google-services.json` for `com.example.fyndstay` before Gradle sync/build can complete, then enable Email/Password Auth, Firestore, and Storage. Do not use a made-up JSON: an APK can compile but Firebase calls will not work.
- No emulator/physical-device run or live Firebase end-to-end test was possible here. After installing your real config, check registration, listings, wishlist, requests, and photo upload on a connected test device. No Teams submission was made.

---

## Known gaps

Worth knowing if you're picking this up:

- Device/emulator flows and Firebase account, listing, wishlist, request, and photo behavior have not been tested against a real Firebase project. Build verification used a temporary, invalid test config that is **not** included.
- `city` is hard-coded to `"Mumbai"` when adding a PG (`AddPGActivity`), and the locality lists in
  `res/values/arrays.xml` are Mumbai-only.
- `rent` is stored as a string, so rent filtering parses it at read time rather than querying on it.
- The "Remember me" checkbox on the login screen isn't wired up to anything yet.
- Camera capture uses the small thumbnail returned by the camera intent, not a full-resolution image.
- Firestore/Storage rules must be configured in your own Firebase project. Never publish with public demo rules.
