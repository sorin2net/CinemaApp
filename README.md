# 🎬 Cinema Reservation System

A comprehensive cinema ticket booking system built with **Java Swing**, featuring real-time seat management, email notifications, and client-server architecture for synchronized multi-user reservations.

<img src="cale_catre_imaginea_interfetei.png" width="600">

---

## ✨ Features

### Core Functionality

* 🎟️ **Real-time Seat Reservation** - Visual seat selection with live availability updates
* ✉️ **Email Notifications** - Automatic confirmation and cancellation emails via Gmail SMTP
* 🌐 **Client-Server Architecture** - Multi-user support with synchronized seat management
* 📅 **Advanced Date Filtering** - Browse movies by month, day, and showtime
* 🔍 **Movie Search** - Quick search functionality by movie title
* 👤 **User Reservations** - View and manage personal bookings by email
* 🎭 **Age Restrictions** - Color-coded age rating system (3+, 7+, 12+, 15+, 18+)
* 💾 **Persistent Storage** - SQLite database with audit logging

### Technical Features

* ⚡ **Concurrent Operations** - Thread-safe reservation handling
* 🔄 **Real-time Synchronization** - Broadcast updates to all connected clients
* 🛡️ **Input Validation** - Email format and reservation conflict checking
* 📊 **Audit Trail** - Complete transaction logging in `logs/server.log`
* 🎨 **Modern UI** - Dark-themed Swing interface with custom styling


## 🏗️ Architecture

The application follows a **client-server** model with layered architecture:

### 💻 Client Layer
* `CinemaGUI` - Main user interface
* `ClientCinema` - Client-side business logic
* `EmailService` - Notification handling

### 🌐 Network I/O (TCP/JSON)
* Communication protocol for real-time data exchange

### ⚙️ Server Layer
* `ServerCinema` - Main server controller
* `RezervareService` - Reservation management logic
* `DatabaseMgr` - Database connectivity and operations

### 📂 Persistence Layer
* `cinema.db` (SQLite) - Movie and reservation data
* `server.log` (Audit Trail) - Transaction history


## 🧩 Key Design Patterns

Aplicația utilizează design pattern-uri consacrate pentru a asigura o structură modulară și ușor de întreținut:

* **MVC Pattern** - Separation of model, view, and controller logic
* **Observer Pattern** - Real-time GUI updates via message callbacks
* **Singleton Pattern** - Database connection management
* **Repository Pattern** - Data access abstraction with `PersistentaRezervari`



## 📸 Screenshots

### Main Interface
<img src="cale_catre_imagine_interfata_principala.png" width="700">
Browse movies with date and time filters, search functionality

### Seat Selection
<img src="cale_catre_imagine_selectie_locuri.png" width="700">
Interactive seat map with real-time availability (Green: Available, Red: Reserved, Orange: Selected)

### User Reservations
<img src="cale_catre_imagine_rezervari.png" width="700">
View and cancel personal bookings by email

### Email Confirmation
<img src="cale_catre_imagine_confirmare_email.png" width="700">
Automatic email confirmation with booking details

## 📦 Prerequisites

* **Java Development Kit (JDK) 17 or higher**
* **Maven** (optional, for dependency management)
* **Gmail Account** with **App Password** enabled
* **Network Access** for client-server communication

## 🚀 Installation

### 1. Clone the Repository
```bash
git clone [https://github.com/sorin2net/CinemaApp/](https://github.com/sorin2net/CinemaApp/)
```

### 2. Download Required Libraries

Place the following JAR files in the `lib/` directory:

* `gson-2.10.1.jar` - JSON serialization/deserialization
* `sqlite-jdbc-3.50.3.0.jar` - SQLite database driver
* `jakarta.mail-api-2.1.3.jar` - Email API
* `jakarta.activation-api-2.1.3.jar` - Activation framework
* `angus-mail-2.0.3.jar` - Email implementation
* `json-simple-1.1.1.jar` - JSON parsing

### 3. Project Structure Setup

```text
cinema-reservation-system/
├── src/
│   └── cinema/
│       ├── app/
│       ├── gui/
│       ├── model/
│       ├── network/
│       ├── persistence/
│       ├── service/
│       └── CinemaMainApp/
├── lib/                 # JAR dependencies
├── resources/
│   ├── filme.json       # Movie data configuration
│   └── [movie-posters]/ # Movie poster images
├── config/
│   └── email.properties # Email configuration
├── logs/                # Server audit logs
└── data/                # SQLite database
```

### 4. Compile the Project
