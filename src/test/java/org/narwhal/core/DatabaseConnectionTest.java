package org.narwhal.core;

import org.junit.*;
import org.narwhal.bean.Person;
import org.narwhal.pool.ConnectionPool;

import java.sql.Date;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.List;

public class DatabaseConnectionTest {

    private static ConnectionInformation connectionInformation =
            new ConnectionInformation("org.h2.Driver", "jdbc:h2:mem:test", "user", "password");

    private static ConnectionPool connectionPool;

    private static final Date johnBirthday = new Date(new GregorianCalendar(1990, 6, 9).getTime().getTime());

    private static final Date doeBirthday  = new Date(new GregorianCalendar(1993, 3, 24).getTime().getTime());

    @BeforeClass
    public static void initDatabaseScheme() throws Exception {
        getConnectionPool().update(new UpdateQuery() {
            @Override
            public void perform(DatabaseConnection connection) throws Exception {
                connection.executeUpdate("CREATE TABLE Person(id INT PRIMARY KEY, name VARCHAR, birthday DATE);");
            }
        }, true);
    }

    @Before
    public void populate() throws Exception {
        getConnectionPool().update(new UpdateQuery() {
            @Override
            public void perform(DatabaseConnection connection) throws Exception {
                connection.executeUpdate("INSERT INTO Person (id, name, birthday) VALUES (?, ?, ?)", 1, "John", johnBirthday);
                connection.executeUpdate("INSERT INTO Person (id, name, birthday) VALUES (?, ?, ?)", 2,  "Doe", doeBirthday);
            }
        }, true);

        DatabaseConnection.clearCache();
    }

    @After
    public void clear() throws Exception {
        getConnectionPool().update(new UpdateQuery() {
            @Override
            public void perform(DatabaseConnection connection) throws Exception {
                connection.executeUpdate("DELETE FROM Person");
            }
        }, true);
    }

    @Test
    public void transactionMethodsTest() throws SQLException, ClassNotFoundException {
        DatabaseConnection connection = null;
        int expectedRowAffected = 3;
        int result = 0;

        try {
            connection = new DatabaseConnection(connectionInformation);

            try {
                connection.beginTransaction();

                result += connection.executeUpdate("INSERT INTO Person (id, name, birthday) VALUES (?, ?, ?)", 5, "Test", new Date(new java.util.Date().getTime()));
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

        Assert.assertEquals(expectedRowAffected, result);
    }

    @Test
    public void createTest() throws SQLException, ReflectiveOperationException {
        DatabaseConnection connection = null;
        Person person = new Person(3, "TestPerson", new Date(new java.util.Date().getTime()));
        int expectedRowAffected = 1;
        int result;

        try {
            connection = new DatabaseConnection(connectionInformation);
            result = connection.persist(person);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }

        Assert.assertEquals(expectedRowAffected, result);
    }

    @Test
    public void readTest() throws SQLException, ReflectiveOperationException {
        DatabaseConnection connection = null;
        Person person;

        try {
            connection = new DatabaseConnection(connectionInformation);
            person = connection.read(Person.class, 1);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }

        Assert.assertEquals("John", person.getName());
        Assert.assertEquals(johnBirthday, person.getBirthday());
    }

    @Test
    public void updateTest() throws SQLException, ReflectiveOperationException {
        DatabaseConnection connection = null;
        Person person = new Person(1, "John Doe", new Date(new java.util.Date().getTime()));
        int expectedRowAffected = 1;
        int result = 0;

        try {
            connection = new DatabaseConnection(connectionInformation);
            result = connection.update(person);
            Person anotherPerson = connection.read(Person.class, 1);
            Assert.assertNotNull(anotherPerson);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }

        Assert.assertEquals(expectedRowAffected, result);
    }

    @Test
    public void deleteTest() throws SQLException, ReflectiveOperationException {
        DatabaseConnection connection = null;
        Person person = new Person(1, "John", new Date(new java.util.Date().getTime()));
        int expectedRowAffected = 1;
        int result;

        try {
            connection = new DatabaseConnection(connectionInformation);
            result = connection.delete(person);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }

        Assert.assertEquals(expectedRowAffected, result);
    }

    @Test
    public void executeUpdateTest() throws SQLException, ClassNotFoundException {
        DatabaseConnection connection = null;
        int doeId = 2;
        int expectedRowAffected = 1;
        int result;

        try {
            connection = new DatabaseConnection(connectionInformation);
            result = connection.executeUpdate("UPDATE Person SET name = ? WHERE id = ?", "FunnyName", doeId);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }

        Assert.assertEquals(expectedRowAffected, result);
    }

    @Test
    public void executeQueryTest() throws SQLException, ReflectiveOperationException {
        DatabaseConnection connection = null;
        String expectedName = "John";
        Person person;
        int joeId = 1;

        try {
            connection = new DatabaseConnection(connectionInformation);
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
        DatabaseConnection connection = null;
        List<Person> persons;
        int expectedSize = 2;

        try {
            connection = new DatabaseConnection(connectionInformation);
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

    private static ConnectionPool getConnectionPool() throws SQLException, ClassNotFoundException {
        if (connectionPool == null) {
            connectionPool = new ConnectionPool(connectionInformation);
        }

        return connectionPool;
    }
}
