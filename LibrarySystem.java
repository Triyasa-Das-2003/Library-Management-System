import java.io.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

// =================================================================================
// 1. DATA MODEL CLASSES (The "What")
// =================================================================================

/**
 * Represents a single book. Implements Serializable for saving to a file.
 */
class Book implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int id;
    private String title;
    private String author;
    private boolean isIssued;

    public Book(int id, String title, String author) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.isIssued = false;
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public boolean isIssued() { return isIssued; }

    // Setters
    public void setIssued(boolean issued) { isIssued = issued; }

    @Override
    public String toString() {
        return String.format("ID: %-5d | Title: %-30s | Author: %-25s | Status: %s",
                id, title, author, (isIssued ? "Issued" : "Available"));
    }
}

/**
 * Represents a library member.
 */
class Member implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int id;
    private String name;

    public Member(int id, String name) {
        this.id = id;
        this.name = name;
    }
    
    // Getters
    public int getId() { return id; }
    public String getName() { return name; }

    @Override
    public String toString() {
        return String.format("ID: %-5d | Name: %s", id, name);
    }
}

/**
 * Represents a transaction of a book being issued to a member.
 */
class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int bookId;
    private final int memberId;
    private final LocalDate issueDate;
    private final LocalDate dueDate;

    public Transaction(int bookId, int memberId) {
        this.bookId = bookId;
        this.memberId = memberId;
        this.issueDate = LocalDate.now();
        this.dueDate = this.issueDate.plusDays(14); // 14-day borrowing period
    }
    
    // Getters
    public int getBookId() { return bookId; }
    public int getMemberId() { return memberId; }
    public LocalDate getDueDate() { return dueDate; }

    @Override
    public String toString() {
        return String.format("Book ID: %-5d | Member ID: %-5d | Issue Date: %s | Due Date: %s",
                bookId, memberId, issueDate, dueDate);
    }
}

/**
 * A wrapper class to hold all library data. This single object will be serialized.
 */
class LibraryData implements Serializable {
    private static final long serialVersionUID = 1L;
    List<Book> books = new ArrayList<>();
    List<Member> members = new ArrayList<>();
    List<Transaction> transactions = new ArrayList<>();
}


// =================================================================================
// 2. CORE LOGIC CLASS (The "How")
// =================================================================================

/**
 * Manages all library operations and data.
 */
class Library {
    private LibraryData data;
    private static final String FILE_NAME = "library_data.dat";
    private static final long FINE_PER_DAY = 1; // Fine of ₹1 per day

    public Library() {
        this.data = loadData();
    }

    // --- Book Management ---
    public void addBook(int id, String title, String author) {
        if (findBookById(id) != null) {
            System.out.println("Error: Book with this ID already exists.");
            return;
        }
        data.books.add(new Book(id, title, author));
        System.out.println("Book added successfully!");
    }

    public Book findBookById(int id) {
        return data.books.stream().filter(b -> b.getId() == id).findFirst().orElse(null);
    }
    
    public List<Book> getAllBooks() {
        return data.books;
    }

    // --- Member Management ---
    public void addMember(int id, String name) {
        if (findMemberById(id) != null) {
            System.out.println("Error: Member with this ID already exists.");
            return;
        }
        data.members.add(new Member(id, name));
        System.out.println("Member added successfully!");
    }

    public Member findMemberById(int id) {
        return data.members.stream().filter(m -> m.getId() == id).findFirst().orElse(null);
    }
    
    public List<Member> getAllMembers() {
        return data.members;
    }
    
    // --- Transaction Management ---
    public void issueBook(int bookId, int memberId) {
        Book book = findBookById(bookId);
        Member member = findMemberById(memberId);

        if (book == null) {
            System.out.println("Error: Book not found.");
            return;
        }
        if (member == null) {
            System.out.println("Error: Member not found.");
            return;
        }
        if (book.isIssued()) {
            System.out.println("Error: Book is already issued.");
            return;
        }

        book.setIssued(true);
        data.transactions.add(new Transaction(bookId, memberId));
        System.out.println("Book issued successfully.");
    }

    public void returnBook(int bookId) {
        Book book = findBookById(bookId);
        if (book == null) {
            System.out.println("Error: Book not found.");
            return;
        }
        if (!book.isIssued()) {
            System.out.println("Error: Book is not currently issued.");
            return;
        }

        Transaction transaction = data.transactions.stream()
                .filter(t -> t.getBookId() == bookId)
                .findFirst().orElse(null);

        if (transaction != null) {
            long overdueDays = ChronoUnit.DAYS.between(transaction.getDueDate(), LocalDate.now());
            if (overdueDays > 0) {
                long fine = overdueDays * FINE_PER_DAY;
                System.out.printf("Book is overdue by %d days. Fine to be paid: ₹%d\n", overdueDays, fine);
            }
            data.transactions.remove(transaction);
        }
        
        book.setIssued(false);
        System.out.println("Book returned successfully.");
    }
    
    // --- Reporting ---
    public List<Transaction> getOverdueTransactions() {
        return data.transactions.stream()
            .filter(t -> LocalDate.now().isAfter(t.getDueDate()))
            .collect(Collectors.toList());
    }
    
    public List<Transaction> getAllTransactions() {
        return data.transactions;
    }

    // --- Data Persistence ---
    public void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(data);
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    private LibraryData loadData() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            return (LibraryData) ois.readObject();
        } catch (FileNotFoundException e) {
            System.out.println("No existing data file found. Starting with a new library.");
            return new LibraryData();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading data: " + e.getMessage());
            return new LibraryData(); // Start fresh if file is corrupt
        }
    }
}


// =================================================================================
// 3. USER INTERFACE CLASS (The "View/Controller")
// =================================================================================

/**
 * Handles all console input/output and menu navigation.
 */
class Menu {
    private final Library library;
    private final Scanner scanner;

    public Menu(Library library) {
        this.library = library;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        String choice;
        do {
            System.out.println("\n===== LIBRARY MANAGEMENT SYSTEM =====");
            System.out.println("1. Book Management");
            System.out.println("2. Member Management");
            System.out.println("3. Library Operations");
            System.out.println("4. Reports");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");
            choice = scanner.nextLine();

            switch (choice) {
                case "1": handleBookMenu(); break;
                case "2": handleMemberMenu(); break;
                case "3": handleOperationsMenu(); break;
                case "4": handleReportsMenu(); break;
                case "5": System.out.println("Saving data and exiting..."); break;
                default: System.out.println("Invalid choice. Please try again.");
            }
        } while (!choice.equals("5"));
    }
    
    private void handleBookMenu() {
        System.out.println("\n--- Book Management ---");
        System.out.println("1. Add a new book");
        System.out.println("2. View all books");
        System.out.print("Enter your choice: ");
        String choice = scanner.nextLine();

        if (choice.equals("1")) {
            System.out.print("Enter Book ID: ");
            int id = readInt();
            System.out.print("Enter Title: ");
            String title = scanner.nextLine();
            System.out.print("Enter Author: ");
            String author = scanner.nextLine();
            library.addBook(id, title, author);
        } else if (choice.equals("2")) {
            System.out.println("\n--- All Books ---");
            library.getAllBooks().forEach(System.out::println);
        } else {
             System.out.println("Invalid choice.");
        }
    }

    private void handleMemberMenu() {
        System.out.println("\n--- Member Management ---");
        System.out.println("1. Add a new member");
        System.out.println("2. View all members");
        System.out.print("Enter your choice: ");
        String choice = scanner.nextLine();

        if (choice.equals("1")) {
            System.out.print("Enter Member ID: ");
            int id = readInt();
            System.out.print("Enter Name: ");
            String name = scanner.nextLine();
            library.addMember(id, name);
        } else if (choice.equals("2")) {
            System.out.println("\n--- All Members ---");
            library.getAllMembers().forEach(System.out::println);
        } else {
             System.out.println("Invalid choice.");
        }
    }

    private void handleOperationsMenu() {
        System.out.println("\n--- Library Operations ---");
        System.out.println("1. Issue a book");
        System.out.println("2. Return a book");
        System.out.print("Enter your choice: ");
        String choice = scanner.nextLine();

        if (choice.equals("1")) {
            System.out.print("Enter Book ID to issue: ");
            int bookId = readInt();
            System.out.print("Enter Member ID: ");
            int memberId = readInt();
            library.issueBook(bookId, memberId);
        } else if (choice.equals("2")) {
            System.out.print("Enter Book ID to return: ");
            int bookId = readInt();
            library.returnBook(bookId);
        } else {
             System.out.println("Invalid choice.");
        }
    }
    
    private void handleReportsMenu() {
        System.out.println("\n--- Reports ---");
        System.out.println("1. View all issued books");
        System.out.println("2. View all overdue books");
        System.out.print("Enter your choice: ");
        String choice = scanner.nextLine();
        
        if (choice.equals("1")) {
             System.out.println("\n--- All Issued Books ---");
             library.getAllTransactions().forEach(System.out::println);
        } else if (choice.equals("2")) {
            System.out.println("\n--- Overdue Books ---");
            List<Transaction> overdue = library.getOverdueTransactions();
            if (overdue.isEmpty()) {
                System.out.println("No books are currently overdue.");
            } else {
                overdue.forEach(System.out::println);
            }
        } else {
            System.out.println("Invalid choice.");
        }
    }

    private int readInt() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a number: ");
            }
        }
    }
}


// =================================================================================
// 4. MAIN APPLICATION CLASS (The "Starter")
// =================================================================================
public class LibrarySystem {
    public static void main(String[] args) {
        Library library = new Library();
        Menu menu = new Menu(library);
        menu.start();
        library.saveData(); // Save data on exit
    }
}