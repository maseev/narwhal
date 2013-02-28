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

    private DatabaseConnection connection;


    public DatabaseConnectionTest() throws SQLException, ClassNotFoundException {
        String driver = "com.mysql.jdbc.Driver";
        String url = "jdbc:mysql://localhost/bank";
        String username = "lrngsql";
        String password = "lrngsql";
        DatabaseInformation information = new DatabaseInformation(driver, url, username, password);
        connection  = new DatabaseConnection(information);
    }

    @Test
    public void transactionMethodsTest() {

    }

    @Test
    public void createTest() throws InvocationTargetException, NoSuchMethodException, SQLException, IllegalAccessException {
        Person person = new Person(null, "TestPerson");
        int expectedRowAffected = 1;
        int result = connection.create(person);

        Assert.assertEquals(expectedRowAffected, result);

        connection.executeUpdate("DELETE FROM Person WHERE name = ?", person.getName());
    }

    @Test
    public void readTest() {

    }

    @Test
    public void updateTest() {

    }

    @Test
    public void deleteTest() {

    }

    @Test
    public void executeUpdateTest() throws SQLException {
        String queryName = "Doe";
        int expectedRowAffected = 1;
        int result = connection.executeUpdate("UPDATE Person SET name = ? WHERE name = ?", "FunnyName", queryName);

        Assert.assertEquals(expectedRowAffected, result);

        restoreDatabase();
    }

    @Test
    public void executeQueryTest() throws SQLException, IllegalAccessException,
            InstantiationException, NoSuchMethodException, InvocationTargetException {
        String expectedName = "John";
        Person person = connection.executeQuery("SELECT * FROM Person WHERE name = ?", Person.class, expectedName);

        Assert.assertNotNull(person);
        Assert.assertEquals(expectedName, person.getName());
    }

    @Test
    public void executeQueryForCollectionTest() throws SQLException, ClassNotFoundException,
            NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        List<Person> persons = connection.executeQueryForCollection("SELECT * FROM Person", Person.class);
        int expectedSize = 2;

        Assert.assertEquals(expectedSize, persons.size());
        Assert.assertEquals("John", persons.get(0).getName());
        Assert.assertEquals("Doe", persons.get(1).getName());
    }

    private void restoreDatabase() throws SQLException {
        String firstName = "John";
        String secondName = "Doe";

        try {
            connection.beginTransaction();

            connection.executeUpdate("DELETE FROM Person");
            connection.executeUpdate("INSERT INTO Person (id, name) VALUES (?, ?)", null, firstName);
            connection.executeUpdate("INSERT INTO Person (id, name) VALUES (?, ?)", null, secondName);

            connection.commit();
        } finally {
            connection.rollback();
        }
    }
}
