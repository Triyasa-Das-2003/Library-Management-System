Library Management System
A simple, console-based library management system built in Java. This application allows for the management of books, members, and borrowing transactions. It's a demonstration of core Java concepts including Object-Oriented Programming (OOP), file I/O for data persistence, and basic data management.

About The Project
This project implements a command-line interface (CLI) to manage a small library. It can:

Track books and their availability.

Register and manage library members.

Handle the process of issuing and returning books.

Calculate fines for overdue books.

Persist all data to a local file, so information is not lost between sessions.

The code is structured into four main components for clarity and maintainability:

Data Model Classes (Book, Member, Transaction, LibraryData): These classes define the core objects of the system.

Core Logic Class (Library): This class contains all the business logic for managing the library's operations.

User Interface Class (Menu): This class is responsible for all console input and output, presenting the user with menus and capturing their choices.

Main Application Class (LibrarySystem): The entry point of the application that initializes and starts the system.

Features
Book Management:

Add new books with a unique ID, title, and author.

View a list of all books, including their current status (Available/Issued).

Member Management:

Add new members with a unique ID and name.

View a list of all registered members.

Library Operations:

Issue a book to a member, updating the book's status.

Return a book, making it available again.

Fine Calculation:

Automatically calculates fines for overdue books upon return (₹1 per day).

Reporting:

Generate a report of all currently issued books.

Generate a report of all overdue books.

Data Persistence:

All library data (books, members, and transactions) is automatically saved to a library_data.dat file upon exiting the application.

Data is automatically loaded from the file when the application starts.
