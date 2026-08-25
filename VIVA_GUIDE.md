# CTB Depot Management System — VIVA Guide

A study document to help you explain this project and make small live changes during your exam.

---

## 1. What This Application Is (30-second answer)

A desktop application for a Sri Lankan bus depot (CTB) to manage its fleet: buses, routes, employees (drivers/conductors/mechanics), fuel purchases, and GPS tracking, with a dashboard showing live status. It's a **standalone Windows desktop app**, not a website — built with JavaFX (Java's GUI framework) and a SQL Server database.

**Stack:**
- **Java 21** — the programming language.
- **JavaFX 13** — the GUI toolkit (draws windows, buttons, tables — think of it as Java's version of a UI framework).
- **SQL Server** (database name `DepotDB`) — stores all the real data.
- **Maven** — build tool that downloads libraries and compiles the project (`pom.xml` lists every dependency).

If asked "why JavaFX and not a web app?" — it's a diploma coursework requirement to build a native Java desktop application demonstrating Java + database + GUI skills together, not a production deployment choice.

---

## 2. How the Code is Organized (Architecture)

The project follows the **MVC pattern (Model-View-Controller)**, split into layers under the base package `lk.bustracking.depotmanagementsystem`:

```
models/       Plain data classes (Bus, Route, Employee, User, FuelRecord...)
              — just fields + getters/setters, no logic. Represents one row of a DB table.

dao/          Data Access Objects (BusDAO, UserDAO)
              — the ONLY layer allowed to write raw SQL. Talks directly to the database.

services/     Business logic (BusService, RouteService, EmployeeService,
              FuelManagementService, DashboardService, AnalyticsService, UserService)
              — sits between the DAO and the UI. Some services call DAOs, some run
              their own SQL directly (a bit inconsistent — see section 8).

controllers/  Glue between a View and its Service/DAO (BusController, LoginController,
              RouteController, DashboardController, FuelManagementController)
              — handles button clicks, calls the service, updates the view.

views/        The actual JavaFX screens (BusManagementView, RouteManagementView,
              EmployeeManagementPanel, FuelManagementPanel, GPSTrackingPanel,
              DashboardView, LoginView) — builds all the visible UI.

db/           Database.java — one class that opens a JDBC connection to SQL Server.

utils/        Small helpers: ValidationUtils (input checks), AppLogger, UIConstants,
              AnimationUtils.

simulator/    GPSSimulator.java — a standalone tool that CAN generate fake GPS
              coordinates for testing. It is NOT wired into the running app —
              nothing calls it. Safe to mention as "a testing utility we built but
              don't use in the live app," not a hidden source of fake data.
```

**Why this matters for the exam:** if an examiner asks "where would you change X," the answer is almost always "in the `views/` file for that screen" (for anything visual) or "in the matching `services/` or `dao/` file" (for anything about *what data* is shown).

### The one-sentence architecture explanation
"A View asks its Controller to do something (like save a bus). The Controller asks a Service to do the business logic. The Service asks a DAO to run SQL against the database. The result flows back up the same chain to update the screen."

---

## 3. Walking Through One Full Request (use this as your main demo answer)

**Example: adding a new bus in Bus Management.**

1. User clicks **"+ Add"** in `BusManagementView.java` → opens `showBusEditDialog(null)`, a form (`GridPane`) with fields like bus number, registration, capacity.
2. User fills the form and clicks **"Create Bus"** → the button's `setOnAction` builds a `Bus` object from the field values and calls `controller.handleCreateBus(busToSave)`.
3. `BusController.handleCreateBus()` calls `BusService`, which calls `BusDAO`.
4. `BusDAO` runs an `INSERT INTO Buses (...)` SQL statement using a `PreparedStatement` (prevents SQL injection — good thing to mention if asked about security).
5. On success, the controller tells the view to refresh, which re-queries `BusDAO.getAllBuses()` and repopulates the table (`ObservableList<Bus>` — JavaFX automatically redraws the table when this list changes).

This same shape (View → Controller → Service → DAO → SQL → back up) repeats for every module (Route, Employee, Fuel).

---

## 4. The Database

- **Server:** local SQL Server, database name `DepotDB`.
- **Connection code:** `db/Database.java` — opens a **new connection per request** (`DriverManager.getConnection(...)` called fresh each time), not a pooled connection, despite HikariCP being listed as a dependency in `pom.xml`. If asked "do you use connection pooling?" — honest answer: "the library is included but not actually used; each query opens its own connection."
- **Login credentials are hardcoded** in `Database.java` (`sa` / a fixed password, `localhost:1433`). This is a known weakness — acceptable to say plainly: "for a coursework project the credentials are hardcoded locally; in production these would come from an environment variable or config file, not source code."

**Key tables:** `Buses`, `Routes`, `Employees`, `Users`, `FuelPurchases`, `Fuel_Transactions`, `GPS_Tracking`, `Trip_Logs`, `Trips`, `Bus_Maintenance`, `Employee_Licenses`, plus assignment/junction tables. Two things worth knowing if pressed:
- There are **two fuel tables** (`FuelPurchases` and `Fuel_Transactions`) — a legacy duplication, not by design.
- `Trip_Logs` and `Trips` currently have **zero rows** in the real database — that's why anything depending on trip history (on-time %, revenue trends, fuel efficiency per bus) honestly shows "No data yet" instead of a number.

---

## 5. Login & Security

**Flow:** `LoginView` → `LoginController.handleLogin()` → validates input → runs authentication on a background thread (`Task<User>`, so the UI doesn't freeze) → `UserDAO.validateLogin(username, password)` → runs `SELECT ... FROM Users WHERE username = ? AND password = ?` comparing a **hashed** password.

**Password hashing:** SHA-256, **unsalted**, done in `UserDAO.hashPassword()`. This is a known weakness — the project includes the `jbcrypt` library (a proper password-hashing library) as a dependency but doesn't actually use it. If asked: "SHA-256 alone is fast to brute-force and has no salt, so identical passwords hash identically. The fix would be bcrypt, which is already a listed dependency — it just isn't wired in yet."

**Roles:** two roles exist, `ADMIN` and `STAFF` (checked in `UserService.getUserPermissions()`), each with a different permission list (e.g., only `ADMIN` gets `USER_MANAGEMENT` and `SYSTEM_SETTINGS`).

**Account lockout:** implemented in `LoginController` — 8 failed attempts locks the account for 5 minutes. This is **in-memory only** (a field on the controller), so it resets if the app restarts — not a server-side/database-backed lockout.

**Session management:** `UserService.validateUserSession()` exists but is a stub — always returns true if the user still exists in the DB. No real session expiry.

---

## 6. Module-by-Module Guide

For each module: what's real, what's not, and the main file to point to if asked to edit it live.

### Dashboard (`DashboardView.java`, `DashboardService.java`)
- Real: fleet counts, GPS online-check (`Database.testConnection()` + recent `GPS_Tracking` rows), system status.
- Fixed in this project's cleanup: removed `Random`-generated fake activity feed and fake KPI numbers; empty states now honestly show "No recent activity" / zero instead of invented numbers.

### Bus Management (`BusManagementView.java`, `BusService.java`, `BusDAO.java`)
- Fully real data: bus list, status, condition, GPS online/offline, mileage, service dates, purchase cost.
- The "Needs Attention" summary count is computed live from `Bus.needsAttention()` (checks maintenance overdue, poor condition, GPS issues) — a good one to point to as "self-documenting business logic."
- The bus details popup honestly shows "No trip data yet" for on-time%/fuel-efficiency (Trip_Logs is empty) instead of inventing numbers.

### Route Management (`RouteManagementView.java`, `RouteService.java`)
- Fully real: 9 table columns (route number, name, from/to, distance, duration, type, fare, hours, status), all from the `Routes` table. No known fake data.

### Employee Management (`EmployeeManagementPanel.java`, `EmployeeService.java`)
- Fully real: employee list, licenses (`EmployeeLicense`, via `LicenseManagementDialog`), stats (driver/mechanic/conductor counts). No known fake data.

### Fuel Management (`FuelManagementPanel.java`, `FuelManagementService.java`)
- **Mixed** — be ready to explain honestly:
  - Real: total fuel/cost stats (computed from actual `fuelRecords`), the records table (date/bus/type/quantity/cost).
  - **Still fake:** the "Fuel Consumption Trend" chart uses hardcoded Mon–Fri numbers; the "Fuel Efficiency by Bus" chart uses hardcoded sample bars for buses `CTB-245/189/156`; `avgEfficiency` is a hardcoded `12.5`. `FuelRecord.getFuelRecords()` silently falls back to fake sample records if the SQL query fails, instead of showing an error.
  - If asked about this: "This module was next in line for the honest-data cleanup we'd already done for Dashboard, Bus, Route, and Employee — the charts are the known remaining fake data."

### GPS Tracking (`views/GPSTrackingPanel.java`)
- Real: queries `Buses` (for the searchable bus list) and reads live coordinates from `GPS_Tracking`. Map is rendered with a `WebView` loading OpenStreetMap/Leaflet-style JavaScript.
- `simulator/GPSSimulator.java` exists as a **separate, unused utility** — not called from the running app. Good to mention if asked "is the GPS real?": "yes, it reads real rows from the GPS_Tracking table; there's a simulator class in the codebase for generating test data, but it isn't wired into the app."

### Reports & Analytics (`DashboardView.createReportsPanel()`, `AnalyticsService.java`)
- **Known bug, be upfront if asked:** `AnalyticsService` queries a table called `Maintenance` that doesn't exist (the real table is `Bus_Maintenance`) and columns `Trips.Revenue` / `Trips.OnTimePerformance` that don't exist either — so those specific queries always fail and silently fall back to hardcoded numbers (e.g. `8750.25`). This is the most "fake" part of the app if an examiner digs into Reports.

---

## 7. The UI Design System (recent redesign — good to show you understand your own recent work)

Originally, every screen hand-wrote its own inline styles in Java (`button.setStyle("-fx-background-color: #10b981; ...")`, repeated dozens of times per file with tiny variations) — inconsistent and hard to maintain. This project now has a **shared stylesheet** at `src/main/resources/styles/modern-dashboard.css` with reusable classes:

- `.btn-primary` / `.btn-success` / `.btn-warning` / `.btn-danger` / `.btn-secondary` — color-coded button variants (blue/green/amber/red/gray), each with a built-in `:hover` effect.
- `.btn-small` — a compact modifier for in-table action buttons.
- `.stat-card` — the small KPI boxes at the top of each screen.
- `.section-card` — the white rounded container used for headers/table wrappers/dialog sections.
- `.status-badge` + `.status-badge-success/warning/danger/neutral` — colored text for status columns (Active/Inactive, etc.).
- `.table-view` — the shared table look (header row color, cell padding, borders).

A Java view applies one of these with `button.getStyleClass().add("btn-primary")` instead of writing a style string. This is loaded once on the app's main `Scene` in `DashboardView`, so every embedded screen gets it automatically.

**Design principle applied:** headers, stat cards, and toolbars are kept deliberately small (short titles, no decorative subtitles, compact padding) — the **table is the important content**, so it gets the majority of screen space. If an examiner says the UI feels cramped or spacious, this is the reasoning to give.

---

## 8. Known Limitations (say these proactively if asked "what would you improve?")

1. **Password hashing** — unsalted SHA-256 instead of bcrypt (library is already a dependency, just unused).
2. **Hardcoded DB credentials** in `Database.java` instead of an external config file.
3. **No real connection pooling** — HikariCP is a dependency but every query opens a fresh connection.
4. **AnalyticsService** queries a nonexistent `Maintenance` table and nonexistent `Trips` columns, so Reports numbers fall back to hardcoded placeholders in places.
5. **Fuel Management charts** still use hardcoded/sample data (see section 6).
6. **Session management** is a stub — no real expiry, and login lockout is in-memory only (resets on app restart).
7. **Two fuel tables** (`FuelPurchases`, `Fuel_Transactions`) — legacy duplication.

Framing these honestly is a strength, not a weakness — it shows you understand the system well enough to critique it, which is exactly what a VIVA is testing.

---

## 9. "Change This Live" Cheat Sheet

Examiners will likely ask you to make a small visible change. Here's exactly where common requests live:

| Request | What to do | Where |
|---|---|---|
| Change a button's color | Swap the CSS class, e.g. `.getStyleClass().add("btn-primary")` → `"btn-danger"` | The view file for that screen |
| Add a new table column | Copy an existing `TableColumn` block, point `setCellValueFactory` at a new getter on the model, add it to `table.getColumns().addAll(...)` | e.g. `BusManagementView.createBusTable()` |
| Change a label's text | Find the `Label` / button text string directly in the view file | Any view file |
| Add a new form field | Add a `TextField`/`ComboBox`, add it to the `GridPane` with `formGrid.add(...)`, read its value in the save handler | e.g. `showBusEditDialog()` in `BusManagementView.java` |
| Change table row/column width | `setMinWidth(...)` on the `TableColumn` (tables use `CONSTRAINED_RESIZE_POLICY` so they stretch to fill the window) | Table-building method in each view |
| Add a new stat/summary card | Call the existing `createSummaryCard(title, value, color, icon)` helper and add it to the row | e.g. `createFleetSummaryCards()` |
| Change global button/card look everywhere at once | Edit the shared CSS class | `src/main/resources/styles/modern-dashboard.css` |
| Fix a query pulling from the wrong table | Find the SQL string in the matching `*DAO.java` or `*Service.java` file and correct the table/column name | e.g. `AnalyticsService.java` for the `Maintenance` table bug |

**Tip:** if asked to add a brand-new field end-to-end (e.g. "add a bus's insurance expiry date to the table"), the honest answer is it already exists as a real column and field (`Bus.getInsuranceExpiry()`), it's just not currently read out of the database or shown anywhere — a good example of "the model has more fields than the UI currently uses."

---

## 10. Anticipated Questions & Short Answers

**Q: Why MVC / this layered structure?**
A: Separation of concerns — the UI code (views) never talks to the database directly; it goes through services and DAOs. This means the same business logic (e.g. "is this bus overdue for maintenance") is written once and reused, and the database access code is isolated in one place per entity, making it easy to test or swap out.

**Q: Why JavaFX TableView with `CONSTRAINED_RESIZE_POLICY`?**
A: It's JavaFX's built-in policy that makes table columns stretch to fill the available width proportionally, instead of leaving dead space or requiring the window to be manually resized.

**Q: How is SQL injection prevented?**
A: Every DAO uses `PreparedStatement` with `?` placeholders and `.setString()`/`.setInt()` etc., never string-concatenated SQL.

**Q: How does the dashboard update in real time?**
A: `DashboardController` runs a background `Task` on a loop (every 30 seconds) that re-fetches data and calls `Platform.runLater(...)` to safely update the UI from the background thread (JavaFX requires all UI updates to happen on the "FX Application Thread").

**Q: What happens if the database is down?**
A: `Database.testConnection()` is checked at startup; most screens catch `SQLException` and either show an error state or (in a few known spots — see section 8) silently fall back to old hardcoded sample data, which is one of the acknowledged weaknesses.

**Q: Why does the login button disable itself while logging in?**
A: Prevents double-submission while the background authentication task is running (`isAuthenticating` flag), and there's a 10-second timeout that cancels the task if the database doesn't respond.

---

## 11. Running the App

```
$env:JAVA_HOME = "C:\java\zulu21.40.17-ca-fx-jdk21.0.6-win_x64\zulu21.40.17-ca-fx-jdk21.0.6-win_x64"
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path
.\mvnw.cmd clean compile      # build only
.\mvnw.cmd javafx:run         # build and launch
```
(Or just double-click `run.cmd` in the project root — it sets this up automatically.) Requires local SQL Server running with the `DepotDB` database available.
