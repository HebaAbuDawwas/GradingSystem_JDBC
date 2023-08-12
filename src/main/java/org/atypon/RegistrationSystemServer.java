package org.atypon;


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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static org.atypon.constants.Constants.INVALID_PASSWORD_ERROR_MESSAGE;

public class RegistrationSystemServer {
    private static final EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("GradingSystemPU");
    private static final EntityManager entityManager = entityManagerFactory.createEntityManager();
    private ObjectOutputStream outputToFile;
    private ObjectInputStream inputFromClient;

    public RegistrationSystemServer() {
        try {
            ServerSocket serverSocket = new ServerSocket(8000);
            System.out.println("Server started ");


            while (true) {
                Socket socket = serverSocket.accept();


                inputFromClient = new ObjectInputStream(socket.getInputStream());
                outputToFile = new ObjectOutputStream(socket.getOutputStream());

                // Read from input
                Object object = inputFromClient.readObject();
                Student student = (Student) object;
                handleLogin(student.getUsername(), student.getPassword());


            }
        } catch (ClassNotFoundException | IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                inputFromClient.close();
                outputToFile.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        addSomeData();
        new RegistrationSystemServer();
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
            sendErrorMessageToClient(INVALID_PASSWORD_ERROR_MESSAGE);
        } else {
            Student student = students.get(0);
            if (student.getPassword().equals(password)) {
                String marksString = getMarks(student);
                sendMarksToClient(marksString);
            } else {
                sendErrorMessageToClient(INVALID_PASSWORD_ERROR_MESSAGE);
            }
        }
    }

    private void sendErrorMessageToClient(String errorMessage) {
        try {
            outputToFile.writeObject(errorMessage);
            outputToFile.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void sendMarksToClient(String marksString) {
        try {
            outputToFile.writeObject(marksString);
            outputToFile.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private String getMarks(Student student) {

        StringBuilder content = new StringBuilder();
        for (Course course : student.getCourses()) {
            content.append("Course: ").append(course.getName()).append(", Mark: ").append(getMark(student, course)).append("\n");
        }
        return content.toString();

    }

    private int getMark(Student student, Course course) {
        Query markQuery = entityManager.createQuery("SELECT m FROM Mark m WHERE m.student = :student AND m.course = :course");
        markQuery.setParameter("student", student);
        markQuery.setParameter("course", course);
        List<Mark> marks = markQuery.getResultList();

        return marks.isEmpty() ? -1 : marks.get(0).getMarks();
    }
    private static void addSomeData() {
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

}
