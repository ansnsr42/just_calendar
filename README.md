# 📅 Java Calendar Project – Overview

## 🎯 Goal

Build a GUI-based calendar application in Java that supports local event management using SQLite. This project serves as a learning platform for Java, Swing, and database integration.

## 🧰 Technologies

- **Language:** Java 
- **GUI Toolkit:** Swing
- **Database:** SQLite
- **Project Type:** Standalone desktop application

## ✅ Current Progress

- Basic GUI created with:
  - `JFrame` for the main window
  - `JPanel` as layout container
  - `JLabel` for the welcome message
  - `JButton` for event creation

- SQLite database (`calendar.db`) setup:

  ```sql
  CREATE TABLE events (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      title TEXT NOT NULL,
      date TEXT NOT NULL,
      time TEXT NOT NULL,
      description TEXT
  );
  ```

## 🧭 Next Steps

- [ ] Implement dialog to add events  
- [ ] Display events for selected date  
- [ ] Add edit/delete functionality  

## 🗂 Feature Roadmap

| Feature            | Status     |
|--------------------|------------|
| GUI Setup          | 🔜 Planned |
| Add Event Dialog   | 🔜 Planned |
| Event Listing      | 🔜 Planned |
| Edit/Delete Event  | 🔜 Planned |

---

# 🧩 Planned Class Structure

## `CalendarApp`

- **Type:** Main class  
- **Responsibilities:**
  - Starts the application
  - Initializes GUI and database setup

---

## `DatabaseManager`

- **Type:** Utility class  
- **Responsibilities:**
  - Manage database connection
  - Perform CRUD operations for events

- **Planned Methods:**

  ```java
  void connect();
  void addEvent(Event e);
  List<Event> getEventsForDate(LocalDate date);
  void deleteEvent(int id);
  ```

---

## `Event`

- **Type:** Plain Old Java Object (POJO)  
- **Fields:**

  ```java
  int id;
  String title;
  LocalDate date;
  LocalTime time;
  String description;
  ```

- **Responsibilities:** Stores event data

---

## `EventDialog`

- **Type:** GUI component (`JDialog`)  
- **Responsibilities:**
  - Input form for adding or editing an event
  - Validate input and pass data to `DatabaseManager`

---

## `CalendarView`

- **Type:** GUI component (`JPanel`)  
- **Responsibilities:**
  - Display monthly calendar view
  - Allow date selection and visualize events

---

## ✍️ Author

This project is developed as a learning exercise to become proficient in Java GUI development and working with databases.
