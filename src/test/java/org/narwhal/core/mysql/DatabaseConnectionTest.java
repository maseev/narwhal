package org.narwhal.core.mysql;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.narwhal.bean.Person;
import org.narwhal.core.DatabaseConnection;
import org.narwhal.core.DatabaseInformation;
import org.narwhal.query.MysqlQueryCreator;

import java.sql.Date;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * @author Miron Aseev
 */
@RunWith(JUnit4.class)
public class DatabaseConnectionTest {

    private static final String DRIVER_NAME = "com.mysql.jdbc.Driver";

    private static final String CONNECTION_URL = "jdbc:mysql://localhost/person";

    private static final String USERNAME = "person";

    private static final String PASSWORD = "person";

    private static final Date johnBirthday = new Date(new GregorianCalendar(1990, 6, 9).getTime().getTime());

    private static final Date doeBirthday  = new Date(new GregorianCalendar(1993, 3, 24).getTime().getTime());

    @Test
    public void transactionMethodsTest() throws SQLException, ClassNotFoundException {
        DatabaseInformation databaseInformation = new DatabaseInformation(DRIVER_NAME, CONNECTION_URL, USERNAME, PASSWORD);
        DatabaseConnection connection = null;
        int expectedRowAffected = 3;
        int result = 0;

        DatabaseConnection.clearCache();

        try {
            connection = new DatabaseConnection(databaseInformation, new MysqlQueryCreator());

            try {
                connection.beginTransaction();

                result += connection.executeUpdate("INSERT INTO Person (id, name, birthday) VALUES (?, ?, ?)", null, "Test", new Date(new java.util.Date().getTime()));
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
    public void createTest() throws SQLException, ReflectiveOperationException {
        DatabaseInformation databaseInformation = new DatabaseInformation(DRIVER_NAME, CONNECTION_URL, USERNAME, PASSWORD);
        DatabaseConnection connection = null;
        Person person = new Person(null, "TestPerson", new Date(new java.util.Date().getTime()));
        int expectedRowAffected = 1;
        int result;

        DatabaseConnection.clearCache();

        try {
            connection = new DatabaseConnection(databaseInformation, new MysqlQueryCreator());
            result = connection.persist(person);
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
    public void readTest() throws SQLException, ReflectiveOperationException {
        DatabaseInformation databaseInformation = new DatabaseInformation(DRIVER_NAME, CONNECTION_URL, USERNAME, PASSWORD);
        DatabaseConnection connection = null;
        Person person;

        DatabaseConnection.clearCache();

        try {
            connection = new DatabaseConnection(databaseInformation, new MysqlQueryCreator());
            person = connection.read(Person.class, 1);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }

        try {
            Assert.assertEquals("John", person.getName());
            Assert.assertEquals(johnBirthday, person.getBirthday());
        } finally {
            restoreDatabase();
        }
    }

    @Test
    public void updateTest() throws SQLException, ReflectiveOperationException {
        DatabaseInformation databaseInformation = new DatabaseInformation(DRIVER_NAME, CONNECTION_URL, USERNAME, PASSWORD);
        DatabaseConnection connection = null;
        Person person = new Person(1, "John Doe", new Date(new java.util.Date().getTime()));
        int expectedRowAffected = 1;
        int result;

        DatabaseConnection.clearCache();

        try {
            connection = new DatabaseConnection(databaseInformation, new MysqlQueryCreator());
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
    public void deleteTest() throws SQLException, ReflectiveOperationException {
        DatabaseInformation databaseInformation = new DatabaseInformation(DRIVER_NAME, CONNECTION_URL, USERNAME, PASSWORD);
        DatabaseConnection connection = null;
        Person person = new Person(1, "John", new Date(new java.util.Date().getTime()));
        int expectedRowAffected = 1;
        int result;

        DatabaseConnection.clearCache();

        try {
            connection = new DatabaseConnection(databaseInformation, new MysqlQueryCreator());
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
    public void executeUpdateTest() throws SQLException, ReflectiveOperationException {
        DatabaseInformation databaseInformation = new DatabaseInformation(DRIVER_NAME, CONNECTION_URL, USERNAME, PASSWORD);
        DatabaseConnection connection = null;
        int doeId = 2;
        int expectedRowAffected = 1;
        int result;

        DatabaseConnection.clearCache();

        try {
            connection = new DatabaseConnection(databaseInformation, new MysqlQueryCreator());
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
    public void executeQueryTest() throws SQLException, ReflectiveOperationException {
        DatabaseInformation databaseInformation = new DatabaseInformation(DRIVER_NAME, CONNECTION_URL, USERNAME, PASSWORD);
        DatabaseConnection connection = null;
        String expectedName = "John";
        Person person;
        int joeId = 1;

        DatabaseConnection.clearCache();

        try {
            connection = new DatabaseConnection(databaseInformation, new MysqlQueryCreator());
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
    public void executeQueryForCollectionTest() throws SQLException, ReflectiveOperationException {
        DatabaseInformation databaseInformation = new DatabaseInformation(DRIVER_NAME, CONNECTION_URL, USERNAME, PASSWORD);
        DatabaseConnection connection = null;
        List<Person> persons;
        int expectedSize = 2;

        DatabaseConnection.clearCache();

        try {
            connection = new DatabaseConnection(databaseInformation, new MysqlQueryCreator());
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
        DatabaseInformation databaseInformation = new DatabaseInformation(DRIVER_NAME, CONNECTION_URL, USERNAME, PASSWORD);
        DatabaseConnection connection = null;

        try {
            connection = new DatabaseConnection(databaseInformation, new MysqlQueryCreator());

            try {
                connection.beginTransaction();

                connection.executeUpdate("DELETE FROM Person");
                connection.executeUpdate("INSERT INTO Person (id, name, birthday) VALUES (?, ?, ?)", 1, "John", johnBirthday);
                connection.executeUpdate("INSERT INTO Person (id, name, birthday) VALUES (?, ?, ?)", 2,  "Doe", doeBirthday);

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
