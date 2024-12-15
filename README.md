
# **Gradebook Management System**

### **CS 410/510: Databases Final Project**

## **Overview**

The Gradebook Management System is a command-line Java application designed to manage grades for multiple classes. It provides a simple interface for managing classes, categories, assignments, students, and grades, ensuring comprehensive grade tracking and reporting capabilities.

---

## **Features**

### **Class Management**
- Create a new class.
- List all classes with the number of enrolled students.
- Activate a class to perform class-specific operations.

### **Category and Assignment Management**
- Add categories with associated weights for an active class.
- Add assignments to specific categories with a name, description, and point value.
- List assignments grouped by categories for an active class.

### **Student Management**
- Add new students and enroll them in the active class.
- Enroll existing students in the active class.
- Display all students in the active class or filter by name/username.

### **Grade Management**
- Assign grades to students for specific assignments.
- Automatically handle grade updates and point value limits.

### **Grade Reporting**
- View a student's grades by category and calculate their total and weighted grade.
- Display the gradebook for the active class, showing each student's total and attempted grades.

### **Database Integration**
- The system uses MySQL for persistent data storage, ensuring reliable performance and scalability.

---

## **Commands**

### **General Commands**
| **Command**               | **Description**                                    |
|---------------------------|----------------------------------------------------|
| `test-connection`         | Test the connection to the database.               |
| `list-classes`            | List all classes with the number of enrolled students. |

### **Class-Specific Commands**
| **Command**                          | **Description**                                                                                   |
|--------------------------------------|---------------------------------------------------------------------------------------------------|
| `new-class [course] [term] [section] [description]` | Add a new class.                                                                 |
| `select-class [course] [term] [section]` | Activate a class by specifying course, term, and section.                                       |
| `show-class`                         | Show details of the active class.                                                                |

### **Category and Assignment Commands**
| **Command**                                  | **Description**                                                                                   |
|---------------------------------------------|---------------------------------------------------------------------------------------------------|
| `add-category [name] [weight]`              | Add a new category to the active class.                                                          |
| `show-categories`                           | List all categories for the active class with their weights.                                     |
| `add-assignment [name] [category] [description] [points]` | Add an assignment to a specific category in the active class. |
| `show-assignments`                          | List all assignments for the active class, grouped by category.                                  |

### **Student Commands**
| **Command**                                          | **Description**                                                                                   |
|-----------------------------------------------------|---------------------------------------------------------------------------------------------------|
| `add-student [username] [studentid] [last] [first]` | Add a new student and enroll them in the active class.                                            |
| `add-student [username]`                            | Enroll an existing student in the active class.                                                  |
| `show-students`                                     | List all students enrolled in the active class.                                                  |
| `show-students [filter]`                            | List all students with the filter applied to name/username.                                      |

### **Grade Commands**
| **Command**                        | **Description**                                                                                   |
|-----------------------------------|---------------------------------------------------------------------------------------------------|
| `grade [assignment] [username] [grade]` | Assign or update a grade for a specific student.                                                 |
| `student-grades [username]`        | Show all grades for a specific student, grouped by category, with a total grade.                 |
| `gradebook`                        | Display the gradebook for the active class, including total and attempted grades for all students.|

---

## **Grade Calculation**

### **Total Grades**
- **Total Grade**: Calculated using all assignments (graded or not), with category weights rescaled to 100.
- **Attempted Grade**: Calculated using only graded assignments.

### **SQL-Driven Calculations**
The system performs as many computations as possible in SQL to ensure efficiency and consistency.

---

## **Project Structure**

```
GradeBook/
├── README.md               # Project documentation
├── src/
│   ├── Database.java       # Database connection and utilities
│   ├── GradebookShell.java # Main application logic and commands
│   ├── libs/               # MySQL JDBC driver
│   ├── sql/                # SQL schema and data dump
```

---

## **Setup Instructions**

1. **Install MySQL**:
   - Ensure MySQL is installed and running on your system.
   - Set up a database using the schema provided in `src/sql/schema.sql`.
   - Load the example data using `src/sql/dump.sql`.

2. **Configure the Application**:
   - Update database credentials in the `Database.java` file.

3. **Compile and Run**:
   ```bash
   javac -cp "src:src/libs/mysql-connector-j-9.0.0/mysql-connector-j-9.0.0.jar" src/*.java
   java -cp "src:src/libs/mysql-connector-j-9.0.0/mysql-connector-j-9.0.0.jar" GradebookShell
   ```

---

## **Example Commands**

1. **Create a Class**:
   ```text
   new-class CS410 Sp20 1 "Databases"
   ```

2. **Add a Category**:
   ```text
   add-category Homework 40
   ```

3. **Add an Assignment**:
   ```text
   add-assignment HW1 Homework "SQL queries homework" 50
   ```

4. **Add a Student**:
   ```text
   add-student jdoe 1 Doe John
   ```

5. **Assign a Grade**:
   ```text
   grade HW1 jdoe 45
   ```

6. **View the Gradebook**:
   ```text
   gradebook
   ```

---

## **Submission**
Include:
- `README.md`
- Source files (`Database.java`, `GradebookShell.java`)
- SQL files (`schema.sql`, `dump.sql`)
- Video link demonstrating the application functionality.

---

