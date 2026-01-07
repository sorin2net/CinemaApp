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
