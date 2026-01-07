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
```
# Create output directory
mkdir -p out

# Compile all Java files
find src -name "*.java" > sources.txt
javac -d out -cp "lib/*" @sources.txt
```

## ⚙️ Configuration

**Email Configuration** (`config/email.properties`)

```
fromEmail=your-email@gmail.com
appPassword=your-16-char-app-password
```

### How to get Gmail App Password:

1. **Enable 2-Step Verification** on your Google Account
2. Visit [App Passwords](https://myaccount.google.com/apppasswords)
3. Generate a new app password for **"Mail"**
4. **Copy the 16-character password** (no spaces)

### Movie Data Configuration (`resources/filme.json`)
```
[
  {
    "titlu": "Movie Title",
    "durata": 120,
    "imaginePath": "poster.jpg",
    "ore": ["18:00", "20:30"],
    "restrictieVarsta": 12,
    "gen": "Action",
    "dateRulare": [
      {"luna": 1, "zi": 15},
      {"luna": 1, "zi": 16}
    ],
    "sala": {
      "nume": "Sala 1",
      "randuri": 10,
      "coloane": 14
    }
  }
]
```
### Network Configuration
```
String host = "192.x.x.x";  // Change to your server IP
int port = 12345;
```

## 🎮 Usage
### Starting the Server
```
java -cp "out:lib/*" cinema.network.ServerCinema
```

**Expected output:**
```
Server Cinema rulează pe portul 12345
Server disponibil la: 192.x.x.x:12345
Filmele au fost încărcate pe server: 5 filme
```

### Starting the Client
```
java -cp "out:lib/*" CinemaMainApp.Main
```

**Offline Mode:** If the server is unavailable, the app operates in local mode with reduced functionality.

### Making a Reservation

1. **Select Date** - Choose month and day from dropdown menus
2. **Filter Showtime** - Optionally filter by specific time
3. **Search Movie** - Use search bar to find specific titles
4. **Select Seats** - Click on green seats to select (turns orange)
5. **Enter Email** - Provide valid email address
6. **Confirm** - Click "Rezervă" button

### Viewing Reservations

1. Click **"Rezervările mele"** button in top menu
2. Enter your email address
3. View grouped reservations by date/time
4. Click **"Anulează"** to cancel any booking

## 📂 Project Structure
```
src/
├── cinema/
│   ├── app/
│   │   └── CinemaApp.java                 # Application entry point
│   ├── gui/
│   │   ├── CinemaGUI.java                 # Main interface
│   │   └── AnulareRezervareFrame.java     # Cancellation dialog
│   ├── model/
│   │   ├── Film.java                      # Movie entity
│   │   ├── Sala.java                      # Theater hall entity
│   │   └── Scaun.java                     # Seat entity
│   ├── network/
│   │   ├── ServerCinema.java              # Server implementation
│   │   ├── ClientCinema.java              # Client implementation
│   │   └── Mesaj.java                     # Message protocol
│   ├── persistence/
│   │   ├── DatabaseManager.java           # SQLite operations
│   │   └── PersistentaRezervari.java      # Reservation persistence
│   └── service/
│       ├── RezervareService.java          # Business logic
│       ├── EmailService.java              # Email notifications
│       └── LocalDateAdapter.java          # JSON date serialization
└── CinemaMainApp/
    └── Main.java                          # Main launcher
```

## 🗄️ Database Schema
### `rezervari` Table
```
CREATE TABLE rezervari (
    ora_rezervare TEXT,      -- Timestamp of booking (HH:mm)
    titlu TEXT,              -- Movie title
    gen TEXT,                -- Movie genre
    varsta INTEGER,          -- Age restriction
    data TEXT,               -- Showdate (YYYY-MM-DD)
    ora_film TEXT,           -- Showtime (HH:mm)
    rand INTEGER,            -- Row number
    coloana INTEGER,         -- Column number
    email TEXT               -- User email
);
```
## 🛠️ Technologies Used

| Technology | Purpose |
| :--- | :--- |
| **Java 17+** | Core programming language |
| **Swing** | GUI framework |
| **SQLite** | Embedded database |
| **Gson** | JSON serialization |
| **Jakarta Mail** | Email functionality |
| **TCP Sockets** | Network communication |
| **Multithreading** | Concurrent client handling |

## 📡 API Documentation

### Message Protocol (JSON over TCP)
Sistemul utilizează un protocol de comunicare bazat pe **JSON** transmis prin socket-uri **TCP**, asigurând o arhitectură client-server decuplată și eficientă.

### Reservation Request
Fiecare cerere de rezervare trimisă de client este procesată de server, verificând în baza de date disponibilitatea locurilor în timp real pentru a preveni suprapunerile.
```
{
  "tip": "cerere_rezervare",
  "film": "Movie Title",
  "ora": "18:00",
  "data": "2026-01-15",
  "scaune": ["R5-C7", "R5-C8"],
  "email": "user@example.com"
}
```
### Reservation Response
```
{
  "tip": "raspuns",
  "status": "ok",
  "mesaj": "Rezervare efectuată cu succes! 2 scaun(e) rezervat(e)."
}
```
### Cancellation Request
```
{
  "tip": "cerere_anulare",
  "email": "user@example.com",
  "film": "Movie Title",
  "ora": "18:00",
  "data": "2026-01-15",
  "scaune": ["R5-C7"]
}
```
### Broadcast Update
```
{
  "tip": "update_sali",
  "mesaj": "Actualizare scaune ocupate"
}
```

## 🎨 UI Color Scheme

### Age Rating Colors
- **3+** - `#4CAF50` (Green)
- **7+** - `#FFEB3B` (Yellow)
- **12+** - `#FF9800` (Orange)
- **15+** - `#F44336` (Red)
- **18+** - `#FFFFFF` (White)

### Interface Colors
- **Background** - `#2D2D2D` (Dark Gray)
- **Panels** - `#3C3F41` (Medium Gray)
- **Available Seat** - `#00FF00` (Green)
- **Reserved Seat** - `#FF0000` (Red)
- **Selected Seat** - `#FFA500` (Orange)

## 🐛 Troubleshooting

### Server Won't Start
```
Error: Address already in use
```
### Solution: Kill the process using port 12345
```
# Linux/Mac
lsof -i :12345
kill -9 <PID>

# Windows
netstat -ano | findstr :12345
taskkill /PID <PID> /F
```

### Email Not Sending
```
Error: Authentication failed
```
**Solutions:**
1. Verify App Password is correct (16 characters, no spaces)
2. Check 2-Step Verification is enabled
3. Ensure Gmail allows less secure apps (if using older accounts)

### Client Can't Connect
```
Connection refused
```

## Coding Standards

* **Follow Java naming conventions**
* **Add JavaDoc comments for public methods**
* **Include unit tests for new features**
* **Maintain backward compatibility**

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🧑‍💻 Author

Your Name

* **GitHub:** [@sorin2net](https://github.com/sorin2net)
* **Email:** denisdumitriu95@gmail.com
