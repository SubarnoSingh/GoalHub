# GoalHub — Goals, Targets & Tasks

A personal target and goal management app for Android, built with **Kotlin + Jetpack Compose**.
It is *not* a to-do list: it manages **long-term goals → projects → short-term targets → daily tasks**
in one hierarchy, offline-first on your device.

> "I don't just want to remember what I need to do. I want to know what I'm trying to achieve,
> what I'm currently working toward, and what actions I need to take to get there."

## The three questions the home screen answers

1. **What am I trying to achieve?** — Your active long-term goals (large gradient cards).
2. **What am I currently working toward?** — Active short-term targets and projects (each spans a date *range*).
3. **What should I do right now?** — Today's tasks.

## What's implemented

- **Four item types** with full CRUD: Goal, Project, Target, Task — plus milestones (goals/projects),
  subtasks and notes (targets). The Goal → Project → Target → Task link is optional; everything can float free.
- **Date-range targets** — a target has a start *and* an end date and stays active across every day it
  spans. No fake "today/tomorrow only" structure. A calendar view shows these ranges spanning days.
- **Automatic progress** — targets derive progress from subtasks, projects from tasks, goals from
  milestones (with fallbacks). A slider lets you override any of it manually; "Reset to auto" restores it.
- **Status + priority + categories** — Not Started / In Progress / Completed / Paused / Archived (+ derived
  *Overdue*), Low–Critical priority, and 7 easily-editable categories.
- **Offline-first** — Room database, everything reactive through `Flow` + `StateFlow`. No network calls.
- **Deadline reminders** — offline `AlarmManager` + notification channel, behind a `ReminderScheduler`
  interface so the backend can be swapped later.
- **Sample data** — seeded on first launch so the UI never looks empty (3 goals, 1 project, 5 targets,
  28 tasks, milestones, subtasks, notes — all dated relative to "today").

## Tech stack

| Concern          | Choice                                   |
|------------------|------------------------------------------|
| Language         | Kotlin 2.2                                |
| UI               | Jetpack Compose + Material 3             |
| Navigation       | Navigation Compose                       |
| Persistence      | Room (KSP)                               |
| Async            | Coroutines + Flow                        |
| Architecture     | MVVM: UI → ViewModel → Repository → Room |
| DI               | Manual container (`GoalHubApplication`)  |

Dependencies are pinned in `gradle/libs.versions.toml`. The only non-AndroidX dependency is none —
the project intentionally stays lean so it's easy to extend.

## Project structure

```
app/src/main/java/com/example/goalhub
├── data
│   ├── local
│   │   ├── GoalPlannerDatabase.kt        // Room database
│   │   ├── SettingsStore.kt              // SharedPreferences (user name, reminders)
│   │   ├── dao/                          // Goal/Project/Target/Task DAOs
│   │   ├── entities/                     // Room entities (+ foreign keys)
│   │   └── relations/                    // @Relation POJOs (aggregate roots)
│   ├── mapper/EntityMappers.kt           // DB → domain (derives Overdue status)
│   ├── notifier/                         // ReminderScheduler + AlarmManager impl
│   └── repository
│       ├── GoalPlannerRepository.kt      // single facade: reads, CRUD, progress recompute
│       ├── Drafts.kt                     // immutable form inputs
│       └── SampleDataProvider.kt         // first-run sample content
├── domain
│   ├── model/                            // Goal, Project, Target, Task, enums,
│   │                                     // ProgressCalculator, PlanningPeriod
│   └── usecase/                          // (thin wrappers live in the ViewModels)
├── presentation
│   ├── home / goals / targets / tasks / calendar / create / more
│   └── AppViewModelProvider.kt           // central ViewModel factory wiring
├── navigation/Navigation.kt              // routes + bottom bar + NavHost
└── ui
    ├── components/                       // reusable cards, chips, pickers, dialogs…
    ├── theme/                            // colors, typography, shapes, gradients
    └── util/DateTime.kt                  // date helpers + formatters
```

### How progress flows

Every write that affects progress recomputes the owning item and stores the result, so reads are O(1):

```
toggle subtask  → recompute target → recompute its project → recompute its goal
toggle task     → recompute project (if linked)            → recompute its goal
toggle milestone→ recompute goal or project
```

Rules live in `domain/model/ProgressCalculator.kt` — the one place to change the logic.

## Customizing

Everything you're likely to touch first:

- **Categories** — add a value in `domain/model/Enums.kt` (`Category`), pick a gradient index
  (`presetIndex`), optionally add an icon in `ui/components/CategoryIcons.kt`.
- **Gradients / colors** — `ui/theme/Gradients.kt` and `ui/theme/Color.kt`. Dark mode is automatic
  (follows the system setting): `Color.kt` holds both palettes (`GoalHubPalette`), and the app-level
  colors like `Ink`, `CardSurface`, `SoftLine`, `Primary` adapt to the active theme. Tweak the light
  values in `LightGoalHubPalette` and the dark ones in `DarkGoalHubPalette`.
- **Card look** — `ui/components/Surfaces.kt` (`SoftCard`) and `Cards.kt`.
- **Sample data** — `data/repository/SampleDataProvider.kt` (all dates are relative to *today*).
- **Progress rules** — `domain/model/ProgressCalculator.kt`.
- **User name** — `data/local/SettingsStore.kt` (default "Subarno").
- **Reminder timing** — `data/notifier/AlarmReminderScheduler.kt` (`REMINDER_HOUR`).
- **Time periods** — `domain/model/PlanningPeriod.kt`.

### Future-ready seams

- **Cloud sync / login / backup** — swap `AppContainer` internals; the repository is the single data gate.
- **Notifications** — implement `ReminderScheduler` differently (e.g. WorkManager, push) without touching screens.
- **Kotlin Multiplatform** — the `domain` layer is pure Kotlin + `java.time` and already platform-agnostic.
- **Habits / Pomodoro / analytics** — new domain models + repository methods follow the same pattern.

## Building

```bash
./gradlew :app:assembleDebug
```

The APK lands in `app/build/outputs/apk/debug/app-debug.apk`. Requires the Android SDK
(`local.properties` → `sdk.dir`), JDK 17+.

## Design language

Soft off-white background, white rounded cards with gentle borders/shadows, large typography,
and colorful gradient cards — blue/cyan for study & projects, purple/pink for personal, green for
success, orange for finance. Colorful but calm. The same hues are tuned into a calm dark palette
(muted near-black canvas, slightly raised card surfaces, brighter indigo accents) that kicks in
automatically in dark mode.
