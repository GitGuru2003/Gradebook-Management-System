-- Create the database if it doesn't exist
CREATE DATABASE IF NOT EXISTS gradebook;
USE gradebook;

-- Class Table
CREATE TABLE IF NOT EXISTS class (
    class_id INT AUTO_INCREMENT PRIMARY KEY,
    course_number VARCHAR(50) NOT NULL,
    term VARCHAR(50) NOT NULL,
    section_number VARCHAR(50) NOT NULL,
    description TEXT
);

-- Category Table
CREATE TABLE IF NOT EXISTS category (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL,
    weight DECIMAL(5, 2) NOT NULL,
    class_id INT NOT NULL,
    FOREIGN KEY (class_id) REFERENCES class(class_id) ON DELETE CASCADE,
    INDEX (class_id)
);

-- Assignment Table
CREATE TABLE IF NOT EXISTS assignment (
    assign_id INT AUTO_INCREMENT PRIMARY KEY,
    assign_name VARCHAR(100) NOT NULL,
    description TEXT,
    point DECIMAL(10, 2) NOT NULL,
    category_id INT NOT NULL,
    class_id INT NOT NULL,
    FOREIGN KEY (category_id) REFERENCES category(category_id) ON DELETE CASCADE,
    FOREIGN KEY (class_id) REFERENCES class(class_id) ON DELETE CASCADE,
    UNIQUE (assign_name, class_id),
    INDEX (category_id),
    INDEX (class_id)
);

-- Student Table
CREATE TABLE IF NOT EXISTS student (
    student_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL
);

-- Student-Class Relationship Table
CREATE TABLE IF NOT EXISTS student_class (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    class_id INT NOT NULL,
    FOREIGN KEY (student_id) REFERENCES student(student_id) ON DELETE CASCADE,
    FOREIGN KEY (class_id) REFERENCES class(class_id) ON DELETE CASCADE,
    UNIQUE (student_id, class_id),
    INDEX (student_id),
    INDEX (class_id)
);

-- Grade Table
CREATE TABLE IF NOT EXISTS grade (
    grade_id INT AUTO_INCREMENT PRIMARY KEY,
    grade DECIMAL(6, 3) NOT NULL,
    assign_id INT NOT NULL,
    student_id INT NOT NULL,
    FOREIGN KEY (assign_id) REFERENCES assignment(assign_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES student(student_id) ON DELETE CASCADE,
    UNIQUE (assign_id, student_id),
    INDEX (assign_id),
    INDEX (student_id)
);
