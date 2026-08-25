# COLLEGE OF TECHNOLOGY — KANDY
## NVQ Level 5 National Diploma in Information & Communication Technology

# DEPOT MANAGEMENT SYSTEM FOR BUS TRACKING
## PROJECT REPORT

- **Subject:** SOFTWARE PROGRAMMING
- **Batch:** 2023 / 2024
- **Project Title:** Depot Management System for Bus Tracking
- **Purpose:** Bus Fleet, Route, Staff, Fuel & GPS Management
- **Date of Submission:** 2025 / 2026
- **Prepared by:** P. SAJANI SANDEEPANI
- **Registration Number:** KY/23/ETB11/1/0007
- **Approved by:** ___________________________ (Lecturer Signature)

---

## ABSTRACT

The Sri Lanka Transport Board (SLTB / CTB) operates centralized bus depots nationwide, managing extensive vehicle fleets, daily route schedules, driver/conductor allocations, fuel transactions, and vehicle maintenance. Traditional depot management relies heavily on paper-based registers and fragmented spreadsheets, leading to operational bottlenecks, unmonitored fuel consumption, lost trip records, and compliance oversight.

This project introduces a desktop-based **Depot Management System for Bus Tracking** built using **Java 21**, **JavaFX 13**, and **Microsoft SQL Server (DepotDB)**. Architected according to the **Model-View-Controller (MVC)** design pattern, the system establishes a modular desktop solution. Key capabilities include bus fleet registration and live health monitoring, 9-column route management, employee license tracking, fuel log recording with expense metrics, live GPS location tracking rendered via an embedded OpenStreetMap interface, and structured report exports using iText PDF and Apache POI. The system automates depot workflow, eliminates administrative error, and enhances decision-making through real-time operational visibility.

---

## ACKNOWLEDGEMENTS

I express my sincere gratitude and appreciation to the **College of Technology, Kandy**, and the Department of Information & Communication Technology for providing the academic platform and infrastructure to execute this project.

Special thanks are extended to my course lecturers and mentors for their guidance, insightful feedback, and encouragement throughout the software development life cycle. I also acknowledge my peers and family for their unwavering support during the research, implementation, and testing phases of this project.

---

## TABLE OF CONTENTS

1. **Introduction**
2. **Purpose of the Project & Definition of the Problem**
   - 2.1 Definition of the Problem
   - 2.2 Purpose & Objectives of the Project
3. **Requirement Specification**
   - 3.1 Hardware Requirement Specification
   - 3.2 Software Requirement Specification
4. **System Development Life Cycle (SDLC)**
   - 4.1 Planning & Feasibility Study (Economic, Technical, Operational)
   - 4.2 Gathering Requirements & Analysis
   - 4.3 Functional & Non-Functional Requirements
   - 4.4 System Architecture & Detailed Design (MVC, ERD, Use Case, DFD)
   - 4.5 Development Phase & Core Modules
   - 4.6 System Testing & Verification
   - 4.7 Deployment & Maintenance
5. **Database Setup & Architecture**
6. **Database Table Structure**
7. **Output Screens & User Interface Architecture**
8. **Test Cases for Depot Management System**
9. **Conclusion & Future Scope**
10. **References**
- **Appendix: Core Source Code Snippets**

---

## 1. INTRODUCTION

Public bus transport forms the primary backbone of passenger transportation in Sri Lanka. Central bus depots operated by the Ceylon Transport Board (CTB / SLTB) manage dozens to hundreds of buses, serving local, intercity, and express routes daily. Efficient depot operation requires coordinated control across multiple domains: fleet condition, preventive maintenance, driver duty rosters, license validity, fuel dispensing, and real-time tracking.

The **Depot Management System for Bus Tracking** is a modern standalone Windows desktop application engineered to centralize, digitize, and automate Sri Lankan bus depot operations. Built using Java 21 and JavaFX 13 for native graphical user interface (GUI) rendering, the system connects directly to a Microsoft SQL Server database (`DepotDB`). It offers depot managers, traffic controllers, and administrative personnel an integrated dashboard for end-to-end operational control.

Key modern design choices include a custom CSS design system (`modern-dashboard.css`), asynchronous database loading via JavaFX background tasks to prevent UI freezing, secure role-based permissions (`ADMIN` / `STAFF`), and automated test-driven components.

---

## 2. PURPOSE OF THE PROJECT & DEFINITION OF THE PROBLEM

### 2.1 Definition of the Problem

Conventional bus depot administration in Sri Lanka relies predominantly on physical logbooks, paper receipts, and unintegrated spreadsheets. This legacy operational mode faces critical challenges:

* **Data Inefficiency:** Manual logbooks make retrieving historical maintenance logs or fuel usage tedious and prone to data loss.
* **Fuel & Expense Untracking:** Lack of real-time monitoring leads to unauthorized fuel consumption, unmonitored mileage, and difficulty detecting fuel theft.
* **Lack of Real-Time Visibility:** Without real-time GPS integration, station masters cannot determine exact bus locations or handle schedule delays effectively.
* **License & Duty Compliance:** Manual rosters frequently overlook driver/conductor driving license expiration dates, risking legal non-compliance.
* **Reporting Delays:** Calculating daily operational costs, fuel efficiency per vehicle, or route revenue requires days of manual compilation.

### 2.2 Purpose & Objectives of the Project

The principal objective of the Depot Management System is to eliminate manual friction by deploying an automated, reliable, and user-friendly digital system. Specific goals include:

* **Automated Fleet Management:** Maintain a complete digital registry of buses, condition statuses, mileage, and scheduled service dates.
* **Route & Schedule Control:** Define intercity and local routes, distances, estimated durations, fare structures, and operational hours.
* **Employee & License Tracking:** Track driver, conductor, and mechanic details, EPF numbers, and enforce license expiry verification.
* **Fuel Expense Tracking:** Log every fuel purchase (liters, fuel type, cost, station) and calculate fleet consumption metrics.
* **Live GPS Map Tracking:** Display live bus coordinates on an interactive OpenStreetMap WebView inside the JavaFX application.
* **Role-Based Access & Security:** Enforce secure system access via hashed credentials and role-based permissions.

---

## 3. REQUIREMENT SPECIFICATION

### 3.1 Hardware Requirement Specification

| Component | Minimum Requirement | Recommended Specification |
|---|---|---|
| **Processor** | Intel Core i3 / AMD Ryzen 3 @ 2.0 GHz | Intel Core i5 / AMD Ryzen 5 @ 2.5 GHz (4+ Cores) |
| **RAM** | 4 GB DDR4 | 8 GB DDR4 or higher |
| **Storage** | 500 MB free disk space (HDD/SSD) | 2 GB free disk space (NVMe SSD) |
| **Display Resolution** | 1280 x 720 pixels | 1920 x 1080 (Full HD) |
| **Network Interface** | 100 Mbps Ethernet / Wi-Fi | 1 Gbps LAN / High-speed Wi-Fi (for DB & GPS) |
| **Peripherals** | Standard Keyboard and Mouse | Standard Keyboard, Mouse, and Barcode/Thermal Printer |

### 3.2 Software Requirement Specification

| Component | Software Name | Version / Details |
|---|---|---|
| **Operating System** | Microsoft Windows 10 / 11 | 64-bit Edition |
| **Runtime Environment** | Java Development Kit (JDK) | Azul Zulu OpenJDK 21 (with JavaFX bundled) |
| **GUI Framework** | JavaFX Toolkit | JavaFX 13.0.2 (`javafx-controls`, `fxml`, `web`, `swing`) |
| **Database Engine** | Microsoft SQL Server | SQL Server 2019 / 2022 Express / Developer (`DepotDB`) |
| **Database Driver** | Microsoft JDBC Driver | `mssql-jdbc` 12.4.2.jre11 |
| **Build Automation** | Apache Maven | Maven 3.9+ (`pom.xml` dependency management) |
| **IDE / Editor** | NetBeans IDE / IntelliJ IDEA | NetBeans IDE 25 / Apache NetBeans |
| **Libraries & Utilities** | ControlsFX, Ikonli, Logback, iText | ControlsFX 11.1.2, Ikonli FontAwesome 12.3.1, iText 5.5.13 |

---

## 4. SYSTEM DEVELOPMENT LIFE CYCLE (SDLC)

The software development process followed the **Spiral SDLC Model** (Barry Boehm). The Spiral model combines iterative prototyping with systematic risk analysis and waterfall control, enabling continuous refinement of UI panels, DAO SQL queries, and database relations based on incremental testing.

### 4.1 Planning & Feasibility Study

A comprehensive feasibility analysis was performed across three key dimensions:

* **Economic Feasibility:** Development utilizes open-source Java technologies (JavaFX, Maven, OpenJDK) and SQL Server Developer edition. The financial investment is minimal, while operational return on investment (ROI) is substantial due to eliminated paper registers and reduced fuel loss.
* **Technical Feasibility:** Java 21 coupled with SQL Server JDBC provides high transactional reliability, thread safety, and cross-hardware stability. The team possessed strong core Java and relational database proficiency.
* **Operational Feasibility:** The interface features high contrast, clear font sizes, intuitive icon navigation (FontAwesome), and explicit action buttons (Save, Cancel, Delete). Station clerks require minimal training.

### 4.2 Gathering Requirements & Analysis

Requirements were gathered through direct stakeholder interviews (Depot Managers, Route Inspectors, Depot Clerks), observation of manual depot dispatching workflows, and document analysis of existing paper trip logs and fuel bills.

* **Stakeholders Identified:** Depot Manager, Bus Traffic Controller, Station Master, Fuel Station Clerk, System Administrator.
* **Methods Employed:** Interviews, manual log sheet inspection, operational workflow mapping.

### 4.3 Functional & Non-Functional Requirements

| Functional Requirement | Non-Functional Requirement |
|---|---|
| User Login with encrypted credentials and role check (`ADMIN` / `STAFF`). | Response time for table queries and filtering must be under 2 seconds. |
| CRUD operations for Bus fleet details, mileage, and condition monitoring. | UI must strictly follow modern UX design guidelines with dynamic CSS styling. |
| Route creation with start/end locations, distance, duration, and fare. | Data must be stored in ACID-compliant Microsoft SQL Server database. |
| Employee registry with driver/conductor designations and license expiry. | Application must handle database connection dropouts gracefully without crashing. |
| Fuel transaction logging with liters, cost, station, and bus link. | Password security enforced via SHA-256 hashing and 8-attempt lockout. |
| Live GPS bus location map rendered via embedded OpenStreetMap WebView. | Modular code structure adhering strictly to the MVC design pattern. |
| PDF and Excel export for fleet performance and expense summaries. | Compatibility with standard desktop resolution (1920x1080 and 1366x768). |

### 4.4 System Architecture & Detailed Design

The software follows the **MVC (Model-View-Controller)** Architectural Pattern:

```
lk.bustracking.depotmanagementsystem
├── models/         # Plain data entities (Bus, Route, Employee, User, FuelRecord...)
├── dao/            # Data Access Objects executing parameterized SQL (BusDAO, UserDAO)
├── services/       # Business logic (BusService, RouteService, EmployeeService...)
├── controllers/    # Event handlers connecting Views and Services
├── views/          # JavaFX screens (BusManagementView, RouteManagementView, DashboardView...)
├── db/             # Database.java — JDBC Connection Manager
├── utils/          # ValidationUtils, AppLogger, UIConstants, AnimationUtils
└── simulator/      # GPSSimulator testing utility
```

#### Entity Relationship Summary
* One `User` can perform multiple `Activity Logs` (1 : N).
* One `Route` can be assigned to multiple `Buses` (1 : N).
* One `Bus` can have multiple `Fuel Purchase Records` (1 : N).
* One `Bus` can have multiple `GPS Tracking Logs` (1 : N).
* One `Employee` (Driver) can possess multiple `Licenses` (1 : N).

### 4.5 Development Phase

Development proceeded in modular iterations: Database schema initialization, Model entity creation, DAO SQL layer development, Service business logic implementation, and JavaFX View layout composition using CSS tokens (`modern-dashboard.css`).

### 4.6 Testing Phase

Testing encompassed unit testing of services via JUnit 5, integration testing of DAO queries against SQL Server, UI validation testing via TestFX, and user scenario validation.

### 4.7 Deployment & Maintenance

The application is packaged into a standalone executable JAR using the Maven Shade Plugin and native installer bundles using the JPackage maven plugin. Maintenance routines include weekly database backups and automated log truncation.

---

## 5. DATABASE SETUP & ARCHITECTURE

The application connects to Microsoft SQL Server instance hosting the database `DepotDB`. Database connections are supplied on-demand using parameterized JDBC prepared statements to prevent SQL injection vulnerabilities.

* **Connection String:** `jdbc:sqlserver://localhost:1433;databaseName=DepotDB;encrypt=false`
* **Database User:** `sa` (Configurable in `Database.java`)
* **Isolation Level:** `TRANSACTION_READ_COMMITTED`
* **Connection Test:** `SELECT 1` ping validation before query execution

---

## 6. DATABASE TABLE STRUCTURE

The relational database consists of 8 core tables:

### 6.1 `Users` Table

| Column Name | Data Type | Constraints | Description |
|---|---|---|---|
| `user_id` | INT | PRIMARY KEY, IDENTITY(1,1) | Unique user identifier |
| `username` | VARCHAR(50) | NOT NULL, UNIQUE | Login username |
| `password` | VARCHAR(255) | NOT NULL | SHA-256 hashed password |
| `full_name` | VARCHAR(100) | NOT NULL | User's full name |
| `role` | VARCHAR(20) | NOT NULL | Role (`ADMIN` / `STAFF`) |
| `status` | VARCHAR(20) | DEFAULT 'ACTIVE' | Account status |
| `created_at` | DATETIME | DEFAULT GETDATE() | Record creation timestamp |

### 6.2 `Buses` Table

| Column Name | Data Type | Constraints | Description |
|---|---|---|---|
| `bus_id` | INT | PRIMARY KEY, IDENTITY(1,1) | Unique bus identifier |
| `bus_number` | VARCHAR(20) | NOT NULL, UNIQUE | Depot fleet number (e.g. NB-1234) |
| `registration_number` | VARCHAR(30) | NOT NULL, UNIQUE | Government registration number |
| `capacity` | INT | NOT NULL | Passenger seating capacity |
| `model` | VARCHAR(50) | NULL | Manufacturer & model |
| `manufacture_year` | INT | NULL | Year of manufacture |
| `status` | VARCHAR(20) | DEFAULT 'ACTIVE' | Status (`ACTIVE` / `MAINTENANCE` / `INACTIVE`) |
| `condition_status` | VARCHAR(20) | DEFAULT 'GOOD' | Vehicle condition (`EXCELLENT` / `GOOD` / `POOR`) |
| `current_mileage` | DOUBLE | DEFAULT 0.0 | Odometer reading in kilometers |
| `last_service_date` | DATE | NULL | Date of last maintenance service |
| `next_service_due` | DATE | NULL | Date when next service is due |
| `assigned_route_id` | INT | FOREIGN KEY (`Routes`) | ID of currently assigned route |
| `purchase_cost` | DECIMAL(12,2) | DEFAULT 0.00 | Bus acquisition cost in LKR |

### 6.3 `Routes` Table

| Column Name | Data Type | Constraints | Description |
|---|---|---|---|
| `route_id` | INT | PRIMARY KEY, IDENTITY(1,1) | Unique route identifier |
| `route_number` | VARCHAR(20) | NOT NULL, UNIQUE | Route code (e.g. Route 602) |
| `route_name` | VARCHAR(100) | NOT NULL | Route title (e.g. Kandy - Colombo) |
| `start_location` | VARCHAR(50) | NOT NULL | Origin terminal |
| `end_location` | VARCHAR(50) | NOT NULL | Destination terminal |
| `distance_km` | DOUBLE | NOT NULL | Total route length in km |
| `estimated_duration_minutes` | INT | NOT NULL | Estimated one-way travel duration |
| `route_type` | VARCHAR(30) | DEFAULT 'NORMAL' | Service type (`EXPRESS` / `INTERCITY` / `NORMAL`) |
| `fare` | DECIMAL(10,2) | NOT NULL | Ticket fare in LKR |
| `operating_hours` | VARCHAR(50) | NULL | Daily schedule window (e.g. 05:00 - 22:00) |
| `status` | VARCHAR(20) | DEFAULT 'ACTIVE' | Operational status (`ACTIVE` / `INACTIVE`) |

### 6.4 `Employees` Table

| Column Name | Data Type | Constraints | Description |
|---|---|---|---|
| `employee_id` | INT | PRIMARY KEY, IDENTITY(1,1) | Unique staff identifier |
| `epf_number` | VARCHAR(20) | NOT NULL, UNIQUE | EPF employee number |
| `full_name` | VARCHAR(100) | NOT NULL | Staff full name |
| `nic_number` | VARCHAR(15) | NOT NULL, UNIQUE | National Identity Card number |
| `designation` | VARCHAR(30) | NOT NULL | Designation (`DRIVER` / `CONDUCTOR` / `MECHANIC`) |
| `contact_number` | VARCHAR(15) | NULL | Phone contact |
| `joined_date` | DATE | NULL | Date of employment |
| `status` | VARCHAR(20) | DEFAULT 'ACTIVE' | Employment status |

### 6.5 `Employee_Licenses` Table

| Column Name | Data Type | Constraints | Description |
|---|---|---|---|
| `license_id` | INT | PRIMARY KEY, IDENTITY(1,1) | License record ID |
| `employee_id` | INT | FOREIGN KEY (`Employees`) | Associated employee ID |
| `license_number` | VARCHAR(30) | NOT NULL | Driving/Heavy Vehicle License No |
| `license_type` | VARCHAR(30) | NOT NULL | Type (`HEAVY_VEHICLE` / `BUS` / `GENERAL`) |
| `issue_date` | DATE | NULL | Date of license issue |
| `expiry_date` | DATE | NOT NULL | Expiration date |
| `status` | VARCHAR(20) | DEFAULT 'VALID' | Status (`VALID` / `EXPIRED`) |

### 6.6 `FuelPurchases` Table

| Column Name | Data Type | Constraints | Description |
|---|---|---|---|
| `record_id` | INT | PRIMARY KEY, IDENTITY(1,1) | Fuel transaction ID |
| `bus_id` | INT | FOREIGN KEY (`Buses`) | Target bus ID |
| `date` | DATE | NOT NULL | Date of refueling |
| `fuel_type` | VARCHAR(20) | DEFAULT 'DIESEL' | Fuel category (`DIESEL` / `SUPER_DIESEL`) |
| `quantity_liters` | DOUBLE | NOT NULL | Volume in liters |
| `total_cost` | DECIMAL(10,2) | NOT NULL | Total LKR cost |
| `driver_id` | INT | NULL | Driver receiving fuel |
| `station_name` | VARCHAR(100) | NULL | Filling station name |

### 6.7 `GPS_Tracking` Table

| Column Name | Data Type | Constraints | Description |
|---|---|---|---|
| `tracking_id` | INT | PRIMARY KEY, IDENTITY(1,1) | Tracking log ID |
| `bus_id` | INT | FOREIGN KEY (`Buses`) | Tracked bus ID |
| `latitude` | DOUBLE | NOT NULL | GPS Latitude coordinate |
| `longitude` | DOUBLE | NOT NULL | GPS Longitude coordinate |
| `speed` | DOUBLE | DEFAULT 0.0 | Current speed in km/h |
| `timestamp` | DATETIME | DEFAULT GETDATE() | GPS ping timestamp |

---

## 7. OUTPUT SCREENS & USER INTERFACE ARCHITECTURE

The user interface is designed using JavaFX layouts (`BorderPane`, `VBox`, `GridPane`, `TableView`, `ScrollPane`) styled with `modern-dashboard.css`.

* **7.1 Login View (`LoginView.java`):** Features centered card layout, username/password fields, visual password toggle, authentication progress indicator, and 8-attempt lockout safeguard.
* **7.2 Dashboard View (`DashboardView.java`):** Features top KPI summary cards (Total Fleet, Active Routes, Total Employees, Total Fuel Cost), live database connection status badge, quick action bar, and main navigation menu.
* **7.3 Bus Management View (`BusManagementView.java`):** Displays full bus fleet in a constrained TableView with status badges (`ACTIVE` / `MAINTENANCE` / `INACTIVE`). Includes search filter by bus number/status, '+ Add Bus' modal dialog, edit capabilities, and automatic 'Needs Attention' maintenance warnings.
* **7.4 Route Management View (`RouteManagementView.java`):** Presents a 9-column schedule table detailing Route No, Route Name, Start, End, Distance (km), Duration (min), Route Type, Fare (LKR), Operating Hours, and Status. Includes route modal creation dialog.
* **7.5 Employee Management View (`EmployeeManagementPanel.java`):** Lists depot staff filtered by designation (Drivers, Conductors, Mechanics). Features dedicated License Management Dialog to verify heavy vehicle license expiration dates.
* **7.6 Fuel Management View (`FuelManagementPanel.java`):** Displays historical fuel log records table, total fuel metrics (Total Liters, Total LKR Spent, Avg Price/L), alongside BarChart consumption trends and LineChart efficiency metrics.
* **7.7 GPS Tracking View (`GPSTrackingPanel.java`):** Embeds an interactive OpenStreetMap Leaflet interface inside JavaFX `WebView`, rendering real-time bus pins based on latitude/longitude query results from `GPS_Tracking`.
* **7.8 Reports & Analytics View (`AnalyticsService.java`):** Provides one-click PDF export via iText PDF and Excel spreadsheet generation via Apache POI for administrative record keeping.

---

## 8. TEST CASES FOR DEPOT MANAGEMENT SYSTEM

System testing was executed using structured test suites covering security, data entry, validation, and output generation:

| Test Case ID | Description | Input Steps | Test Data | Expected Result | Status |
|---|---|---|---|---|---|
| **TC001** | Valid Admin Login | a. Open Login Form<br>b. Enter valid credentials<br>c. Click 'Login' | Username: `admin`<br>Password: `admin123` | Authentication succeeds, main dashboard opens with admin privileges. | **PASS** |
| **TC002** | Invalid Password Login | a. Open Login Form<br>b. Enter correct user, wrong pass<br>c. Click 'Login' | Username: `admin`<br>Password: `wrongpass` | Error alert 'Invalid Credentials' displayed; login rejected. | **PASS** |
| **TC003** | Account Lockout Safeguard | a. Enter incorrect password 8 consecutive times | Username: `admin`<br>Failed Attempts: 8 | Account temporarily locked for 5 minutes; button disabled. | **PASS** |
| **TC004** | Create New Bus Record | a. Open Bus Management<br>b. Click '+ Add Bus'<br>c. Submit valid form | Bus No: `NB-4521`<br>Reg No: `WP-NA-4521`<br>Capacity: 54 | Bus added to SQL Server database; TableView auto-refreshes. | **PASS** |
| **TC005** | Duplicate Bus Reg Number | a. Attempt adding bus with existing registration number | Reg No: `WP-NA-4521`<br>(Already exists) | Validation alert 'Duplicate Registration Number' shown; save blocked. | **PASS** |
| **TC006** | Assign Bus to Route | a. Select bus from table<br>b. Click Edit<br>c. Select assigned route | Bus: `NB-4521`<br>Assigned Route: Route 602 | Foreign key `assigned_route_id` updated in Buses table. | **PASS** |
| **TC007** | Driver License Expiry Alert | a. Open Employee Panel<br>b. Check license status dialog | Employee: Driver Jayasinghe<br>Expiry: Past Date | License status marked 'EXPIRED' in red badge. | **PASS** |
| **TC008** | Log Fuel Purchase | a. Open Fuel Panel<br>b. Click 'Log Fuel'<br>c. Submit transaction | Bus: `NB-4521`<br>Liters: 120.5<br>Cost: 41,500 LKR | Record saved to `FuelPurchases`; total fuel cost stat updated. | **PASS** |
| **TC009** | Live GPS Coordinate Fetch | a. Open GPS Tracking Panel<br>b. Select bus `NB-4521` | Lat: 7.2906<br>Lng: 80.6337 | Map centers on Kandy coordinates and places bus marker. | **PASS** |
| **TC010** | Generate Fleet PDF Report | a. Open Reports Panel<br>b. Click 'Export PDF Summary' | Report Type: Fleet Summary<br>Format: PDF | PDF document generated successfully in target directory. | **PASS** |

---

## 9. CONCLUSION & FUTURE SCOPE

### 9.1 Conclusion

The **Depot Management System for Bus Tracking** successfully replaces legacy paper-based bus depot operations with a unified, high-performance digital desktop system. Developed using Java 21, JavaFX 13, and Microsoft SQL Server (`DepotDB`), the system provides seamless end-to-end management of bus fleets, route schedules, driver/conductor profiles, license validity, fuel purchases, and live GPS map tracking.

The system's modular MVC architecture guarantees maintainability, while centralized CSS styling delivers a modern visual experience. Automated data validation and role-based security safeguard operational integrity, delivering a powerful solution for Sri Lankan bus transport depots.

### 9.2 Future Scope & Recommended Enhancements

* **IoT Hardware GPS Telematics:** Integrate physical IoT GPS telematics hardware via cellular GSM modules directly into jSerialComm / REST endpoints.
* **Salted Bcrypt Password Hashing:** Upgrade password hashing from SHA-256 to salted Bcrypt using the included `jBCrypt` dependency.
* **HikariCP Connection Pooling:** Implement connection pooling via HikariCP to optimize multi-client database throughput.
* **Mobile Passenger App Integration:** Develop a companion Flutter mobile application for passengers to view real-time bus arrivals.
* **Predictive Maintenance Analytics:** Incorporate predictive maintenance algorithms analyzing mileage and service history.

---

## 10. REFERENCES

1. Oracle Corporation. (2024). *Java SE 21 Documentation*. https://docs.oracle.com/en/java/javase/21/
2. OpenJFX Community. (2024). *JavaFX 13 User Guide & API Specification*. https://openjfx.io/
3. Microsoft Corporation. (2024). *Microsoft SQL Server JDBC Driver Documentation*. https://learn.microsoft.com/en-us/sql/connect/jdbc/
4. Boehm, B. (1988). *A Spiral Model of Software Development and Enhancement*. IEEE Computer, 21(5), 61-72.
5. Gamma, E., Helm, R., Johnson, R., & Vlissides, J. (1994). *Design Patterns: Elements of Reusable Object-Oriented Software*. Addison-Wesley.

---

## APPENDIX: CORE SOURCE CODE SNIPPETS

### A.1 Application Entry Point (`DepotManagementSystem.java`)

```java
package lk.bustracking.depotmanagementsystem;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lk.bustracking.depotmanagementsystem.db.Database;
import lk.bustracking.depotmanagementsystem.views.LoginView;
import java.util.logging.Logger;

public class DepotManagementSystem extends Application {
    private static final Logger LOGGER = Logger.getLogger(DepotManagementSystem.class.getName());

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize database connection test
            Database.initialize();
            
            // Launch Login GUI Screen
            LoginView loginView = new LoginView(primaryStage);
            Scene scene = new Scene(loginView.getView(), 1024, 650);
            
            primaryStage.setTitle("Depot Management System - Bus Tracking & Fleet Operations");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            LOGGER.severe("Failed to launch application: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
```

### A.2 Database Connection Manager (`Database.java`)

```java
package lk.bustracking.depotmanagementsystem.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Database {
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=DepotDB;encrypt=false";
    private static final String USER = "sa";
    private static final String PASSWORD = "6238";

    public static Connection getConnection() throws SQLException {
        Properties props = new Properties();
        props.put("user", USER);
        props.put("password", PASSWORD);
        props.put("loginTimeout", "10");
        props.put("trustServerCertificate", "true");
        props.put("applicationName", "DepotManagementSystem");

        return DriverManager.getConnection(URL, props);
    }
}
```
