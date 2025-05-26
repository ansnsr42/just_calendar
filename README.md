# 📅 Java Calendar Project

## 🎯 Goal

Build a GUI-based calendar application in Java that supports local event management using SQLite. 

---

## 🔧 Requirements

| Tool  | Minimum version | Check command        |
|-------|-----------------|----------------------|
| JDK   | **17** or newer | `java -version`      |
| Maven | **3.9**         | `mvn -version`       |

---

## 🚀 Quick start

```bash
# 1. Clone the repo
git clone https://github.com/ansnsr42/just_calendar.git
cd just_calendar

# 2. Build and run
mvn clean package     # compiles into target/
mvn exec:java         # launches the calendar

```
## 🧰 Technologies

- **Language:** Java 
- **GUI Toolkit:** Swing
- **Database:** SQLite
- **Build:** Maven 

## 🗃 Database

The calendar application uses **SQLite** to store event data persistently. SQLite is a lightweight, serverless database that stores data in a local file, making it a great fit for a desktop application like this.

### Database Structure

The database consists of a single table, `events`, with the following columns:

- **id** (INTEGER): A unique identifier for each event (Primary Key).
- **title** (TEXT): The title or name of the event.
- **date** (TEXT): The date of the event (formatted as `YYYY-MM-DD`).
- **time** (TEXT): The time of the event (formatted as `HH:MM`).
- **description** (TEXT): A short description or additional details about the event.
- **recurrence** (TEXT):NONE(default)|DAILY|WEEKLY|MONTHLY|YEARLY
- **until** (TEXT): optional end date

---
## 🧩 Key Classes

| Class              | Type            | Responsibility                                                                                 |
|--------------------|-----------------|------------------------------------------------------------------------------------------------|
| **`CalendarApp`**  | `main` class    | Boots the app, wires UI ↔ DB, handles search/filter, keeps the currently-selected date.        |
| **`CalendarView`** | `JPanel`        | Renders the monthly grid, highlights busy days, emits selection callbacks.                     |
| **`EventDialog`**  | `JDialog`       | Form to create/edit an event (incl. recurrence and optional end date).                         |
| **`DatabaseManager`** | Utility     | JDBC connection management, CRUD operations, full-text search, recurrence expansion.           |
| **`Event`**        | POJO            | Stores event data and implements `occursOn(LocalDate)` for recurrence calculation.             |
| **`RecurrenceType`** | `enum`       | Defines **NONE / DAILY / WEEKLY / MONTHLY / YEARLY** repeat patterns.                          |
| **`ReminderService`** | `Runnable`  | Background thread that pops up a Swing notification 5 minutes before each event.               |



## 📋 Weekly Plan Overview

| Week      | Milestone                         | Description                                                                  |
|-----------|------------------------------------|------------------------------------------------------------------------------|
| Week 1    | Project Kickoff & Planning         | Define goals, choose tools, set up development environment.                 |
| Week 2    | GUI Structure (Swing)              | Create main window with `JFrame`, layout with `JPanel`, buttons, and label. |
| Week 3    | Database Integration (SQLite)      | Create SQLite database, connect via JDBC, and create `events` table.        |
| Week 4    | Event Model Class (`Event.java`)   | Implement the event data model class and test object creation.              |
| Week 5    | Implement `DatabaseManager`        | Add methods to insert, fetch, update, and delete events from database.      |
| Week 6    | Event Dialog (Swing Form)          | Create form dialog to input event data with validation.                     |
| Week 7    | Display Events in Calendar View    | Show events for selected date in the main interface.                        |
| Week 8    | Edit/Delete Events & Final Cleanup | Allow editing/deleting of events. Final testing and code cleanup.           |
