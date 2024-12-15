USE gradebook;

-- Disable foreign key checks to truncate tables safely
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE grade;
TRUNCATE TABLE student_class;
TRUNCATE TABLE assignment;
TRUNCATE TABLE category;
TRUNCATE TABLE student;
TRUNCATE TABLE class;
SET FOREIGN_KEY_CHECKS = 1;

-- Populate Class Table
INSERT INTO class (course_number, term, section_number, description) VALUES
('CS101', 'Fall2024', '001', 'Introduction to Computer Science'),
('CS102', 'Fall2024', '001', 'Data Structures'),
('CS201', 'Spring2024', '002', 'Operating Systems'),
('CS301', 'Fall2024', '001', 'Advanced Algorithms'),
('CS401', 'Spring2024', '001', 'Artificial Intelligence');

-- Populate Category Table
INSERT INTO category (category_name, weight, class_id) VALUES
('Homework', 40.00, 1),
('Exams', 60.00, 1),
('Homework', 30.00, 2),
('Projects', 70.00, 2),
('Midterm', 50.00, 3),
('Final Exam', 50.00, 3),
('Assignments', 40.00, 4),
('Tests', 60.00, 4),
('Projects', 100.00, 5);

-- Populate Assignment Table
INSERT INTO assignment (assign_name, description, point, category_id, class_id) VALUES
('HW1', 'First homework assignment', 10.00, 1, 1),
('HW2', 'Second homework assignment', 20.00, 1, 1),
('Exam1', 'Midterm exam', 50.00, 2, 1),
('HW1', 'First homework for data structures', 15.00, 3, 2),
('Project1', 'Data structures project', 70.00, 4, 2),
('Midterm', 'Midterm for Operating Systems', 50.00, 5, 3),
('FinalExam', 'Final exam for Operating Systems', 50.00, 6, 3),
('Assignment1', 'Algorithm analysis assignment', 10.00, 7, 4),
('Test1', 'First test on algorithms', 25.00, 8, 4),
('AIProject1', 'Build a basic AI model', 100.00, 9, 5);

-- Populate Student Table
INSERT INTO student (username, first_name, last_name) VALUES
('jdoe', 'John', 'Doe'),
('asmith', 'Alice', 'Smith'),
('mjones', 'Mark', 'Jones'),
('rwilson', 'Rachel', 'Wilson'),
('lgreen', 'Linda', 'Green'),
('bwhite', 'Barry', 'White'),
('tblack', 'Tina', 'Black'),
('kbell', 'Karen', 'Bell');

-- Populate Student-Class Relationship Table
INSERT INTO student_class (student_id, class_id) VALUES
(1, 1), -- John Doe in CS101
(2, 1), -- Alice Smith in CS101
(3, 2), -- Mark Jones in CS102
(4, 2), -- Rachel Wilson in CS102
(5, 3), -- Linda Green in CS201
(6, 3), -- Barry White in CS201
(7, 4), -- Tina Black in CS301
(8, 5); -- Karen Bell in CS401

-- Populate Grade Table
INSERT INTO grade (grade, assign_id, student_id) VALUES
(9.00, 1, 1), -- John Doe HW1 CS101
(8.50, 1, 2), -- Alice Smith HW1 CS101
(19.00, 2, 1), -- John Doe HW2 CS101
(50.00, 3, 1), -- John Doe Exam1 CS101
(25.00, 3, 2), -- Alice Smith Exam1 CS101
(12.00, 4, 3), -- Mark Jones HW1 CS102
(65.00, 5, 4), -- Rachel Wilson Project1 CS102
(45.00, 6, 5), -- Linda Green Midterm CS201
(47.00, 7, 6), -- Barry White FinalExam CS201
(8.00, 8, 7), -- Tina Black Assignment1 CS301
(20.00, 9, 8); -- Karen Bell AIProject1 CS401
