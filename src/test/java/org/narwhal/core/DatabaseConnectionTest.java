package org.narwhal.core;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.narwhal.bean.Person;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;

/**
 * @author Miron Aseev
 */
@RunWith(JUnit4.class)
public class DatabaseConnectionTest {

    private static String driver = "com.mysql.jdbc.Driver";
    private static String url = "jdbc:mysql://localhost/bank";
    private static String username = "lrngsql";
    private static String password = "lrngsql";


    @Test
    public void transactionMethodsTest() throws SQLException, ClassNotFoundException {
        DatabaseInformation databaseInformation = new DatabaseInformation(driver, url, username, password);
        DatabaseConnection connection = null;
        int expectedRowAffected = 3;
        int result = 0;

        try {
            connection = new DatabaseConnection(databaseInformation);

            try {
                connection.beginTransaction();

                result += connection.executeUpdate("INSERT INTO Person (id, name) VALUES (?, ?)", null, "Test");
                result += connection.executeUpdate("UPDATE Person SET name = ? WHERE name = ?", "TestTest", "Test");
                result += connection.executeUpdate("DELETE FROM Person WHERE name = ?", "TestTest");

                connection.commit();
            } catch (Exception ex) {
                connection.rollback();
            }
        } finally {
            if (connection != null) {
                connection.close();
            }
        }

        try {
            Assert.assertEquals(expectedRowAffected, result);
        } finally {
            restoreDatabase();
        }
    }

    @Test
    public void createTest() throws InvocationTargetException, NoSuchMethodException, SQLException, IllegalAccessException, ClassNotFoundException {
        DatabaseInformation databaseInformation = new DatabaseInformation(driver, url, username, password);
        DatabaseConnection connection = null;
        Person person = new Person(null, "TestPerson");
        int expectedRowAffected = 1;
        int result;

        try {
            connection = new DatabaseConnection(databaseInformation);
            result = connection.create(person);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }

        try {
            Assert.assertEquals(expectedRowAffected, result);
        } finally {
            restoreDatabase();
        }
    }

    @Test
    public void readTest() throws InvocationTargetException, NoSuchMethodException,
            InstantiationException, SQLException, IllegalAccessException, ClassNotFoundException {
        DatabaseInformation databaseInformation = new DatabaseInformation(driver, url, username, password);
        DatabaseConnection connection = null;
        Person person;

        try {
            connection = new DatabaseConnection(databaseInformation);
            person = connection.read(Person.class, 1);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }

        try {
            Assert.assertEquals("John", person.getName());
        } finally {
            restoreDatabase();
        }
    }

    @Test
    public void updateTest() throws SQLException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        DatabaseInformation databaseInformation = new DatabaseInformation(driver, url, username, password);
        DatabaseConnection connection = null;
        Person person = new Person(1, "John Doe");
        int expectedRowAffected = 1;
        int result;

        try {
            connection = new DatabaseConnection(databaseInformation);
            result = connection.update(person);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }

        try {
            Assert.assertEquals(expectedRowAffected, result);
        } finally {
            restoreDatabase();
        }
    }

    @Test
    public void deleteTest() throws SQLException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        DatabaseInformation databaseInformation = new DatabaseInformation(driver, url, username, password);
        DatabaseConnection connection = null;
        Person person = new Person(1, "John");
        int expectedRowAffected = 1;
        int result;

        try {
            connection = new DatabaseConnection(databaseInformation);
            result = connection.delete(person);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }

        try {
            Assert.assertEquals(expectedRowAffected, result);
        } finally {
            restoreDatabase();
        }
    }

    @Test
    public void executeUpdateTest() throws SQLException, ClassNotFoundException {
        DatabaseInformation databaseInformation = new DatabaseInformation(driver, url, username, password);
        DatabaseConnection connection = null;
        int doeId = 2;
        int expectedRowAffected = 1;
        int result;

        try {
            connection = new DatabaseConnection(databaseInformation);
            result = connection.executeUpdate("UPDATE Person SET name = ? WHERE id = ?", "FunnyName", doeId);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }

        try {
            Assert.assertEquals(expectedRowAffected, result);
        } finally {
            restoreDatabase();
        }
    }

    @Test
    public void executeQueryTest() throws SQLException, IllegalAccessException,
            InstantiationException, NoSuchMethodException, InvocationTargetException, ClassNotFoundException {
        DatabaseInformation databaseInformation = new DatabaseInformation(driver, url, username, password);
        DatabaseConnection connection = null;
        String expectedName = "John";
        Person person;
        int joeId = 1;

        try {
            connection = new DatabaseConnection(databaseInformation);
            person = connection.executeQuery("SELECT * FROM Person WHERE id = ?", Person.class, joeId);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }

        Assert.assertNotNull(person);
        Assert.assertEquals(expectedName, person.getName());
    }

    @Test
    public void executeQueryForCollectionTest() throws SQLException, ClassNotFoundException,
            NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        DatabaseInformation databaseInformation = new DatabaseInformation(driver, url, username, password);
        DatabaseConnection connection = null;
        List<Person> persons;
        int expectedSize = 2;

        try {
            connection = new DatabaseConnection(databaseInformation);
            persons = connection.executeQueryForCollection("SELECT * FROM Person", Person.class);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }

        Assert.assertEquals(expectedSize, persons.size());
        Assert.assertEquals("John", persons.get(0).getName());
        Assert.assertEquals("Doe", persons.get(1).getName());
    }

    private void restoreDatabase() throws SQLException, ClassNotFoundException {
        DatabaseInformation databaseInformation = new DatabaseInformation(driver, url, username, password);
        DatabaseConnection connection = null;

        try {
            connection = new DatabaseConnection(databaseInformation);

            try {
                connection.beginTransaction();

                connection.executeUpdate("DELETE FROM Person");
                connection.executeUpdate("INSERT INTO Person (id, name) VALUES (?, ?)", 1, "John");
                connection.executeUpdate("INSERT INTO Person (id, name) VALUES (?, ?)", 2,  "Doe");

                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
            }
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }
}
