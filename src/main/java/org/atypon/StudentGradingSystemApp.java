package org.atypon;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.atypon.models.Course;
import org.atypon.models.Mark;
import org.atypon.models.Student;
import org.atypon.models.Teacher;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public class StudentGradingSystemApp {

    private final Stage primaryStage;
    private final EntityManager entityManager;

    public StudentGradingSystemApp(Stage primaryStage) {
        this.primaryStage = primaryStage;
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("GradingSystemPU");
        entityManager = entityManagerFactory.createEntityManager();
    }

    public void start() {
        addSomeData();
        primaryStage.setTitle("Student Grading System");

        VBox root = new VBox(20);
        root.setPadding(new Insets(20));

        Label titleLabel = new Label("Welcome to Student Grading System");
        titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();

        gridPane.add(usernameLabel, 0, 0);
        gridPane.add(usernameField, 1, 0);
        gridPane.add(passwordLabel, 0, 1);
        gridPane.add(passwordField, 1, 1);

        Button loginButton = new Button("Login");
        loginButton.setOnAction(event -> {
            String username = usernameField.getText();
            System.out.println("\n\n\n" + username);
            String password = passwordField.getText();
            System.out.println(password + "\n\n\n");
            handleLogin(username, password);
        });

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().add(loginButton);

        root.getChildren().addAll(titleLabel, gridPane, buttonBox);

        Scene scene = new Scene(root, 400, 250);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void addSomeData() {
        Teacher teacher1 = new Teacher("bashayrah", "m_bashayrah", "hrmad");
        Course course1 = new Course("MVC", "course mvc ", teacher1);
        Course course2 = new Course("Java", "course Java ", teacher1);
        Course course3 = new Course("Java", "course Java ", teacher1);
        List<Course> courseList1 = new ArrayList<>();
        courseList1.add(course1);
        courseList1.add(course2);
        courseList1.add(course3);
        Student student1 = new Student("heba", "hdawwas", "hrmad", courseList1);
        Mark mark1 = new Mark(student1, course1, 99);
        Mark mark2 = new Mark(student1, course2, 98);
        entityManager.getTransaction().begin();
        entityManager.persist(teacher1);
        entityManager.persist(course1);
        entityManager.persist(course2);
        entityManager.persist(course3);
        entityManager.persist(student1);
        entityManager.persist(mark1);
        entityManager.persist(mark2);
        entityManager.getTransaction().commit();
    }

    private void handleLogin(String username, String password) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Student> criteriaQuery = cb.createQuery(Student.class);
        Root<Student> studentRoot = criteriaQuery.from(Student.class);
        criteriaQuery.select(studentRoot);
        criteriaQuery.where(cb.equal(studentRoot.get("username"), username));
        Query query = entityManager.createQuery(criteriaQuery);

        List<Student> students = query.getResultList();

        if (students.isEmpty()) {
            showAlert("Invalid username. Please try again.");
        } else {
            Student student = students.get(0);
            if (student.getPassword().equals(password)) {
                Platform.runLater(() -> showMarks(student));
            } else {
                showAlert("Incorrect password. Please try again.");
            }
        }
    }

    private void showMarks(Student student) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Student Marks");
        alert.setHeaderText("Welcome, " + student.getName() + "!");

        StringBuilder content = new StringBuilder();
        for (Course course : student.getCourses()) {
            content.append("Course: ").append(course.getName()).append(", Mark: ").append(getMark(student, course)).append("\n");
        }

        alert.setContentText(content.toString());
        alert.showAndWait();
    }

    private int getMark(Student student, Course course) {
        Query markQuery = entityManager.createQuery("SELECT m FROM Mark m WHERE m.student = :student AND m.course = :course");
        markQuery.setParameter("student", student);
        markQuery.setParameter("course", course);
        List<Mark> marks = markQuery.getResultList();

        return marks.isEmpty() ? -1 : marks.get(0).getMarks();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Login Result");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
