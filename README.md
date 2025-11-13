# Supermarket Information System

A **Java-based supermarket management system** developed as part of the course **CSE 227 – Advanced Object-Oriented Programming (Spring 2025)**.  
The project demonstrates use of **OOP principles** — encapsulation, inheritance, composition, and polymorphism — to simulate a supermarket environment with role-based user functionality and persistent data storage.

---

## Project Overview

This system provides an interactive console-based environment for supermarket operations, including:

- **User roles:**  
  - **HR Manager** – manages employee accounts  
  - **Manager** – manages inventory and customer registrations  
  - **Cashier** – handles customer transactions and payments

Each role accesses a dedicated menu and can perform only the actions allowed for their position.

---

## Key Features

### HR Manager
- Register new employees and assign roles (Cashier or Manager)
- Delete employee accounts
- View and search employee information
- Save all employee data persistently to file (`employees.txt`)

### Manager
- Manage supermarket inventory (`products.txt`, `inventory.txt`)
- Add new products and update stock quantities
- Create weekly offers with 10% discount
- Register new customers for the **Bonus Card** program
- Generate reports on product availability and customer spending

### Cashier
- Process customer purchases and apply automatic discounts
- Check Bonus Card points and allow point redemption
- Update product stock and customer history after each transaction
- Print purchase receipts

---

## File Structure

Supermarket-Information-System/
│
├── src/
│ ├── Cashier.java
│ ├── ControlSystem.java
│ ├── Customer.java
│ ├── Employee.java
│ ├── FileHandler.java
│ ├── GeneralReport.java
│ ├── HRManager.java
│ ├── MainMenu.java
│ ├── Manager.java
│ ├── Person.java
│ ├── Product.java
│ ├── Storage.java
│ ├── TransactionLabel.java
│ └── TransactionSystem.java
│
├── employees.txt
├── customers.txt
├── products.txt
├── inventory.txt
├── customer_transactions.txt
├── .gitignore
└── Supermarket Information System.iml

## Data Persistence

All data is stored in `.txt` files for simplicity:
- `employees.txt` → Employee login credentials and roles  
- `customers.txt` → Customer details and Bonus Card data  
- `products.txt` → Product catalogue  
- `inventory.txt` → Stock quantities  
- `customer_transactions.txt` → Transaction history  

Upon launch, the system loads all files and restores the last saved state.

---

## Example Login Credentials

| Role       | Username    | Password    |
|------------|-------------|-------------|
| HR Manager | `HR_manager`| `HR_manager`|
| Manager    | `manager_1` | `manager_1` |
| Cashier    | `cashier_1` | `cashier_1` |

---

## Concepts Applied

- **Encapsulation:** Controlled access to class data via getters/setters  
- **Inheritance:** Hierarchical relationships (e.g., `Employee` → `Manager`, `Cashier`)  
- **Polymorphism:** Role-based behavior through overridden methods  
- **Composition:** Objects managing other objects (e.g., `Manager` uses `Storage` and `FileHandler`)  
- **Exception Handling:** Prevents runtime crashes during file I/O and input parsing  

---

## How to Run

1. Clone this repository:
   git clone https://github.com/Hydrocast/Supermarket-Information-System.git

2. Open the project in a Java IDE.

3. Run ControlSystem.java.

4. Log in using one of the test credentials above.

5. Follow the console menus to navigate.

## Example Output (Console)

Loading system data...
All data loaded successfully!
System initialized successfully!
=== SUPERMARKET MANAGEMENT SYSTEM ===

=== MAIN MENU ===
1. Login
2. Exit
Select option:

---

## Author

**Giannis Loizou**
- Bachelor’s in Computer Science and Engineering
- Cyprus University of Technology

## Technologies Used

- **Language**: Java
- **Paradigm**: Object-Oriented Programming (OOP)
- **Environment**: IntelliJ IDEA
- **Persistence**: Text file I/O
- **Version Control**: GitHub

## Course Info

- **Course**: CSE 227 – Advanced Object-Oriented Methodology and Programming (UML, Java)
- **Semester**: Spring 2025
- **Institution**: Cyprus University of Technology

