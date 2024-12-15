import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GradebookShell {

    private static Integer activeClassId = null;

    private static Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://127.0.0.1:56671/gradebook";
        String username = "msandbox";
        String password = "user123";
        return DriverManager.getConnection(url, username, password);
    }

    // Test database connection
    public static void testConnection() {
        try (Connection connection = getConnection()) {
            System.out.println("Database connection successful.");
        } catch (SQLException e) {
            System.out.println("Failed to connect to the database.");
        }
    }

    // List all classes
    public static void listClasses() {
        String sql = "SELECT c.class_id, c.course_number, c.term, c.section_number, c.description, " +
                     "COUNT(sc.student_id) AS student_count " +
                     "FROM class c " +
                     "LEFT JOIN student_class sc ON c.class_id = sc.class_id " +
                     "GROUP BY c.class_id";
        try (Connection connection = getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("class_id | course_number | term | section | description | # students");
            System.out.println("-".repeat(80));
            while (rs.next()) {
                System.out.printf("%d | %s | %s | %s | %s | %d%n",
                        rs.getInt("class_id"),
                        rs.getString("course_number"),
                        rs.getString("term"),
                        rs.getString("section_number"),
                        rs.getString("description"),
                        rs.getInt("student_count"));
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving classes: " + e.getMessage());
        }
    }

    // Create a new class
    public static void createClass(String courseNumber, String term, String section, String description) {
        String sql = "INSERT INTO class (course_number, term, section_number, description) VALUES (?, ?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, courseNumber);
            stmt.setString(2, term);
            stmt.setString(3, section);
            stmt.setString(4, description);
            stmt.executeUpdate();
            System.out.println("Class created successfully.");
        } catch (SQLException e) {
            System.out.println("Error creating class: " + e.getMessage());
        }
    }

    // Select a class
public static void selectClass(String[] args) {
    String sql = null;

    if (args.length == 1) {
        sql = "SELECT MIN(class_id) AS class_id " +
              "FROM class WHERE LOWER(course_number) = LOWER(?) " +
              "GROUP BY term, section_number " +
              "HAVING COUNT(*) = 1";
    } else if (args.length == 2) {
        sql = "SELECT MIN(class_id) AS class_id " +
              "FROM class WHERE LOWER(course_number) = LOWER(?) AND LOWER(term) = LOWER(?) " +
              "GROUP BY section_number " +
              "HAVING COUNT(*) = 1";
    } else if (args.length == 3) {
        sql = "SELECT class_id " +
              "FROM class WHERE LOWER(course_number) = LOWER(?) AND LOWER(term) = LOWER(?) AND section_number = ?";
    } else {
        System.out.println("Invalid arguments for select-class.");
        return;
    }

    try (Connection connection = getConnection();
         PreparedStatement stmt = connection.prepareStatement(sql)) {

        for (int i = 0; i < args.length; i++) {
            stmt.setString(i + 1, args[i]);
        }

        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                activeClassId = rs.getInt("class_id");
                System.out.println("Class selected successfully. Active class ID: " + activeClassId);
            } else {
                System.out.println("Class not found or multiple sections available.");
            }
        }
    } catch (SQLException e) {
        System.out.println("Error selecting class: " + e.getMessage());
    }
}


    // Show the currently active class
    public static void showActiveClass() {
        if (activeClassId == null) {
            System.out.println("No class selected.");
            return;
        }

        String sql = "SELECT * FROM class WHERE class_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, activeClassId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.printf("Active Class: %s | %s | %s | %s%n",
                            rs.getString("course_number"),
                            rs.getString("term"),
                            rs.getString("section_number"),
                            rs.getString("description"));
                } else {
                    System.out.println("Active class not found.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error showing active class: " + e.getMessage());
        }
    }

    // Add a new category
    public static void addCategory(String name, double weight) {
        if (activeClassId == null) {
            System.out.println("No active class. Select a class first.");
            return;
        }
        String sql = "INSERT INTO category (category_name, weight, class_id) VALUES (?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setDouble(2, weight);
            stmt.setInt(3, activeClassId);
            stmt.executeUpdate();
            System.out.println("Category added successfully.");
        } catch (SQLException e) {
            System.out.println("Error adding category: " + e.getMessage());
        }
    }

    // Show categories for the active class
    public static void showCategories() {
        if (activeClassId == null) {
            System.out.println("No active class. Select a class first.");
            return;
        }
        String sql = "SELECT category_name, ROUND((weight / SUM(weight) OVER()) * 100, 2) AS rescaled_weight " +
             "FROM category " +
             "WHERE class_id = ?";


        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, activeClassId);
            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("category_id | category_name | weight");
                System.out.println("-".repeat(50));
                while (rs.next()) {
                    // System.out.printf("%d | %s | %.2f%n", rs.getInt("category_id"), rs.getString("category_name"), rs.getDouble("weight"));
                    System.out.printf("%s | %.2f\n", rs.getString("category_name"), rs.getDouble("rescaled_weight"));

                }
            }
        } catch (SQLException e) {
            System.out.println("Error showing categories: " + e.getMessage());
        }
    }

    // Add a new assignment
    public static void addAssignment(String name, String categoryName, String description, int points) {
        if (activeClassId == null) {
            System.out.println("No active class. Select a class first.");
            return;
        }
        String fetchCategoryIdSql = "SELECT category_id FROM category WHERE category_name = ? AND class_id = ?";
        String insertAssignmentSql = "INSERT INTO assignment (assign_name, description, point, category_id, class_id) VALUES (?, ?, ?, ?, ?)";
    
        try (Connection connection = getConnection();
             PreparedStatement fetchCategoryStmt = connection.prepareStatement(fetchCategoryIdSql);
             PreparedStatement insertAssignmentStmt = connection.prepareStatement(insertAssignmentSql)) {
    
            // Fetch the category_id for the given category_name and class_id
            fetchCategoryStmt.setString(1, categoryName);
            fetchCategoryStmt.setInt(2, activeClassId);
    
            try (ResultSet rs = fetchCategoryStmt.executeQuery()) {
                if (rs.next()) {
                    int categoryId = rs.getInt("category_id");
    
                    // Insert the assignment
                    insertAssignmentStmt.setString(1, name);
                    insertAssignmentStmt.setString(2, description);
                    insertAssignmentStmt.setInt(3, points);
                    insertAssignmentStmt.setInt(4, categoryId);
                    insertAssignmentStmt.setInt(5, activeClassId);
    
                    insertAssignmentStmt.executeUpdate();
                    System.out.println("Assignment added successfully.");
                } else {
                    System.out.println("Category not found for the active class.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error adding assignment: " + e.getMessage());
        }
    }
    

    // Show assignments grouped by category
    public static void showAssignments() {
        if (activeClassId == null) {
            System.out.println("No active class. Select a class first.");
            return;
        }
        String sql = "SELECT c.category_name, a.assign_name, a.description, a.point " +
                     "FROM assignment a " +
                     "JOIN category c ON a.category_id = c.category_id " +
                     "WHERE a.class_id = ? " +
                     "ORDER BY c.category_name";
    
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, activeClassId);
    
            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("category_name | assign_name | description | points");
                System.out.println("-".repeat(80));
                while (rs.next()) {
                    System.out.printf("%s | %s | %s | %.2f%n",
                            rs.getString("category_name"),
                            rs.getString("assign_name"),
                            rs.getString("description"),
                            rs.getDouble("point"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error showing assignments: " + e.getMessage());
        }
    }
    

   
    public static void addStudent(String username, String firstName, String lastName) {
        if (activeClassId == null) {
            System.out.println("No active class. Select a class first.");
            return;
        }

        String sqlStudent = "INSERT INTO student (username, first_name, last_name) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE first_name = VALUES(first_name), last_name = VALUES(last_name)";
        String sqlEnroll = "INSERT IGNORE INTO student_class (student_id, class_id) " +
                "SELECT student_id, ? FROM student WHERE username = ?";

        try (Connection connection = getConnection();
             PreparedStatement stmtStudent = connection.prepareStatement(sqlStudent);
             PreparedStatement stmtEnroll = connection.prepareStatement(sqlEnroll)) {

            // Insert or update the student
            stmtStudent.setString(1, username);
            stmtStudent.setString(2, firstName);
            stmtStudent.setString(3, lastName);
            stmtStudent.executeUpdate();

            // Enroll the student in the active class
            stmtEnroll.setInt(1, activeClassId);
            stmtEnroll.setString(2, username);
            stmtEnroll.executeUpdate();

            System.out.println("Student added and enrolled successfully.");
        } catch (SQLException e) {
            System.out.println("Error adding student: " + e.getMessage());
        }
    }

    

    // Enroll an existing student in the current class
public static void enrollStudent(String username) {
    if (activeClassId == null) {
        System.out.println("No active class. Select a class first.");
        return;
    }

    String sqlFetchStudent = "SELECT student_id FROM student WHERE username = ?";
    String checkEnrollment = "SELECT COUNT(*) FROM student_class WHERE student_id = ? AND class_id = ?";
    String sqlEnroll = "INSERT INTO student_class (student_id, class_id) VALUES (?, ?)";

    try (Connection connection = getConnection();
         PreparedStatement fetchStudentStmt = connection.prepareStatement(sqlFetchStudent);
         PreparedStatement checkEnrollmentStmt = connection.prepareStatement(checkEnrollment);
         PreparedStatement enrollStmt = connection.prepareStatement(sqlEnroll)) {

        fetchStudentStmt.setString(1, username);

        try (ResultSet rs = fetchStudentStmt.executeQuery()) {
            if (rs.next()) {
                int studentId = rs.getInt("student_id");

                // Check if the student is already enrolled
                checkEnrollmentStmt.setInt(1, studentId);
                checkEnrollmentStmt.setInt(2, activeClassId);

                try (ResultSet enrollmentRs = checkEnrollmentStmt.executeQuery()) {
                    if (enrollmentRs.next() && enrollmentRs.getInt(1) > 0) {
                        System.out.println("Student is already enrolled in this class.");
                        return;
                    }
                }

                // Enroll the student in the active class
                enrollStmt.setInt(1, studentId);
                enrollStmt.setInt(2, activeClassId);
                enrollStmt.executeUpdate();

                System.out.println("Student enrolled successfully.");
            } else {
                System.out.println("Student not found.");
            }
        }
    } catch (SQLException e) {
        System.out.println("Error enrolling student: " + e.getMessage());
    }
}

    

    // Show all students in the current class
    public static void showStudents(String filter) {
        if (activeClassId == null) {
            System.out.println("No active class. Select a class first.");
            return;
        }

        String sql = "SELECT s.username, s.student_id, s.first_name, s.last_name " +
             "FROM student s " +
             "JOIN student_class sc ON s.student_id = sc.student_id " +
             "WHERE sc.class_id = ?";



        if (filter != null) {
            sql += " AND (LOWER(s.username) LIKE ? OR LOWER(s.first_name) LIKE ? OR LOWER(s.last_name) LIKE ?)";
        }

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, activeClassId);
            if (filter != null) {
                String filterLike = "%" + filter.toLowerCase() + "%";
                stmt.setString(2, filterLike);
                stmt.setString(3, filterLike);
                stmt.setString(4, filterLike);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("username | student_id | last_name | first_name");
                System.out.println("-".repeat(60));
                while (rs.next()) {
                    System.out.printf("%s | %s | %s | %s%n",
                            rs.getString("username"),
                            rs.getString("student_id"),
                            rs.getString("last_name"),
                            rs.getString("first_name"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error showing students: " + e.getMessage());
        }
    }

    // Assign a grade to a student for an assignment
    public static void assignGrade(String assignmentName, String username, double grade) {
        if (activeClassId == null) {
            System.out.println("No active class. Select a class first.");
            return;
        }
    
        String fetchAssignSql = "SELECT assign_id FROM assignment WHERE assign_name = ? AND class_id = ?";
        String fetchStudentSql = "SELECT student_id FROM student WHERE username = ?";
        String insertGradeSql = "INSERT INTO grade (assign_id, student_id, grade) " +
                                "VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE grade = VALUES(grade)";
    
        try (Connection connection = getConnection();
             PreparedStatement fetchAssignStmt = connection.prepareStatement(fetchAssignSql);
             PreparedStatement fetchStudentStmt = connection.prepareStatement(fetchStudentSql);
             PreparedStatement insertGradeStmt = connection.prepareStatement(insertGradeSql)) {
    
            // Fetch assign_id
            fetchAssignStmt.setString(1, assignmentName);
            fetchAssignStmt.setInt(2, activeClassId);
            ResultSet rsAssign = fetchAssignStmt.executeQuery();
    
            if (!rsAssign.next()) {
                System.out.println("Assignment not found.");
                return;
            }
            int assignId = rsAssign.getInt("assign_id");
    
            // Fetch student_id
            fetchStudentStmt.setString(1, username);
            ResultSet rsStudent = fetchStudentStmt.executeQuery();
    
            if (!rsStudent.next()) {
                System.out.println("Student not found.");
                return;
            }
            int studentId = rsStudent.getInt("student_id");
    
            // Insert or update grade
            insertGradeStmt.setInt(1, assignId);
            insertGradeStmt.setInt(2, studentId);
            insertGradeStmt.setDouble(3, grade);
            insertGradeStmt.executeUpdate();
    
            System.out.println("Grade assigned successfully.");
        } catch (SQLException e) {
            System.out.println("Error assigning grade: " + e.getMessage());
        }
    }
    

   public static void showStudentGrades(String username) {
        if (activeClassId == null) {
            System.out.println("No active class. Select a class first.");
            return;
        }

        String sql = "SELECT c.category_name, " +
                "SUM(g.grade) AS total_grade, " +
                "SUM(a.point) AS total_points, " +
                "SUM(COALESCE((g.grade / a.point) * c.weight, 0)) AS weighted_grade " +
                "FROM assignment a " +
                "JOIN category c ON a.category_id = c.category_id " +
                "LEFT JOIN grade g ON a.assign_id = g.assign_id " +
                "WHERE a.class_id = ? AND " +
                "      g.student_id = (SELECT student_id FROM student WHERE username = ?) " +
                "GROUP BY c.category_name";

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, activeClassId);
            stmt.setString(2, username);

            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("category_name | total_grade | total_points | weighted_grade");
                System.out.println("-".repeat(80));
                double totalWeightedGrade = 0;

                while (rs.next()) {
                    double weightedGrade = rs.getDouble("weighted_grade");
                    totalWeightedGrade += weightedGrade;
                    System.out.printf("%s | %.2f | %.2f | %.2f%n",
                            rs.getString("category_name"),
                            rs.getDouble("total_grade"),
                            rs.getDouble("total_points"),
                            weightedGrade);
                }
                System.out.println("-".repeat(80));
                System.out.printf("Total Grade (out of 100): %.2f%n", totalWeightedGrade);
            }
        } catch (SQLException e) {
            System.out.println("Error showing student grades: " + e.getMessage());
        }
    }

    
    public static void showGradebook() {
        if (activeClassId == null) {
            System.out.println("No active class. Select a class first.");
            return;
        }
    
        String sql = """
           SELECT 
    s.username,
    s.student_id,
    s.first_name,
    s.last_name,
    ROUND(SUM(
        CASE 
            WHEN g.grade IS NOT NULL AND a.point > 0 
            THEN (g.grade / a.point) * (c.weight / 100.0) 
            ELSE 0 
        END
    ) * 100 / 5, 2) AS total_grade,
    ROUND(SUM(
        CASE 
            WHEN g.grade IS NOT NULL AND a.point > 0 
            THEN (g.grade / a.point) * (c.weight / 100.0) 
            ELSE 0 
        END
    ) * 100 / 5, 2) AS attempted_grade
FROM 
    student s
JOIN 
    student_class sc ON s.student_id = sc.student_id
LEFT JOIN 
    grade g ON s.student_id = g.student_id
LEFT JOIN 
    assignment a ON g.assign_id = a.assign_id
LEFT JOIN 
    category c ON a.category_id = c.category_id
WHERE 
    sc.class_id = ?
GROUP BY 
    s.username, s.student_id, s.first_name, s.last_name
ORDER BY 
    s.username;

        """;
    
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, activeClassId);
    
            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("username | student_id | first_name | last_name | total_grade | attempted_grade");
                System.out.println("-".repeat(80));
                while (rs.next()) {
                    System.out.printf("%s | %d | %s | %s | %.2f | %.2f%n",
                            rs.getString("username"),
                            rs.getInt("student_id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getDouble("total_grade"),
                            rs.getDouble("attempted_grade"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error showing gradebook: " + e.getMessage());
        }
    }
    
    


    public static List<String> parseArguments(String command) {
        List<String> commandArguments = new ArrayList<>();
        Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(command);
        while (m.find()) {
            String arg = m.group(1).replace("\"", "");
            commandArguments.add(arg);
        }
        return commandArguments;
    }
    // Main method
    public static void main(String[] args) {
        System.out.println("Welcome to the Gradebook Management System!");
        System.out.println("-".repeat(80));

        Scanner scanner = new Scanner(System.in);
        String command;

        do {
            System.out.print("Command: ");
            command = scanner.nextLine();
            List<String> commandArguments = parseArguments(command);
            if (commandArguments.isEmpty()) continue;

            String action = commandArguments.get(0).toLowerCase();
            commandArguments.remove(0);

            switch (action) {
                case "test-connection":
                    testConnection();
                    break;
                case "list-classes":
                    listClasses();
                    break;
                case "new-class":
                    if (commandArguments.size() >= 4) {
                        createClass(commandArguments.get(0), commandArguments.get(1), commandArguments.get(2),
                                String.join(" ", commandArguments.subList(3, commandArguments.size())));
                    } else {
                        System.out.println("Invalid arguments for new-class.");
                    }
                    break;
                case "select-class":
                    selectClass(commandArguments.toArray(new String[0]));
                    break;
                case "show-class":
                    showActiveClass();
                    break;
                case "add-category":
                    if (commandArguments.size() == 2) {
                        addCategory(commandArguments.get(0), Double.parseDouble(commandArguments.get(1)));
                    } else {
                        System.out.println("Invalid arguments for add-category.");
                    }
                    break;
                case "show-categories":
                    showCategories();
                    break;
                case "add-assignment":
                    if (commandArguments.size() >= 4) {
                        addAssignment(commandArguments.get(0), commandArguments.get(1),
                                String.join(" ", commandArguments.subList(2, commandArguments.size() - 1)),
                                Integer.parseInt(commandArguments.get(commandArguments.size() - 1)));
                    } else {
                        System.out.println("Invalid arguments for add-assignment.");
                    }
                    break;
                case "show-assignments":
                    showAssignments();
                    break;
                    case "add-student":
                    if (commandArguments.size() == 3) { // Ensure all three arguments are provided
                        addStudent(commandArguments.get(0), commandArguments.get(1), commandArguments.get(2));
                    } else if (commandArguments.size() == 1) { // For enrolling existing students
                        enrollStudent(commandArguments.get(0));
                    } else {
                        System.out.println("Invalid arguments for add-student. Usage: add-student [username] [first_name] [last_name]");
                    }
                    break;
                
                
                case "show-students":
                    showStudents(commandArguments.isEmpty() ? null : commandArguments.get(0));
                    break;
                case "grade":
                    if (commandArguments.size() == 3) {
                        assignGrade(commandArguments.get(0), commandArguments.get(1),
                                Integer.parseInt(commandArguments.get(2)));
                    } else {
                        System.out.println("Invalid arguments for grade.");
                    }
                    break;
                case "student-grades":
                    if (commandArguments.size() == 1) {
                        showStudentGrades(commandArguments.get(0));
                    } else {
                        System.out.println("Invalid arguments for student-grades.");
                    }
                    break;
                case "gradebook":
                    showGradebook();
                    break;
                case "help":
                    System.out.println("Available Commands:");
                    System.out.println("test-connection - Test database connection");
                    System.out.println("list-classes - List all classes");
                    System.out.println("new-class [course_number] [term] [section] [description] - Add a new class");
                    System.out.println("select-class [course_number] [term] [section] - Select a class");
                    System.out.println("show-class - Show details of the active class");
                    System.out.println("add-category [name] [weight] - Add a category to the active class");
                    System.out.println("show-categories - Show categories of the active class");
                    System.out.println("add-assignment [name] [category] [description] [points] - Add an assignment");
                    System.out.println("show-assignments - Show assignments of the active class");
                    System.out.println("add-student [username] [first_name] [last_name] - Add a new student and enroll them");
                    System.out.println("enroll-student [username] - Enroll an existing student in the active class");
                    System.out.println("show-students - Show students in the active class");
                    System.out.println("grade [assignment] [username] [grade] - Assign or update a grade");
                    System.out.println("student-grades [username] - Show grades for a student");
                    System.out.println("gradebook - Show the gradebook for the active class");
                    System.out.println("quit - Exit the program");
                    break;
                    
                case "exit":
                case "quit":
                    System.out.println("Goodbye!");
                    break;
                default:
                    System.out.println("Unknown command. Type 'help' for a list of commands.");
            }
            System.out.println("-".repeat(80));
        } while (!command.equalsIgnoreCase("quit") && !command.equalsIgnoreCase("exit"));

        scanner.close();
    }
}


