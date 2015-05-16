package org.narwhal.core;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.*;
import org.narwhal.bean.Person;

import java.sql.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class QueryTemplateTest {

    private static QueryTemplate queryTemplate;

    private static final Date johnBirthday = new Date(new GregorianCalendar(1990, 6, 9).getTime().getTime());

    private static final Date doeBirthday  = new Date(new GregorianCalendar(1993, 3, 24).getTime().getTime());

    @BeforeClass
    public static void initDatabaseScheme() throws Exception {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:mem:test");
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUsername("user");
        dataSource.setPassword("password");

        queryTemplate = new QueryTemplate(dataSource);

        queryTemplate.update(new UpdateQuery() {
            @Override
            public int perform(DatabaseConnection connection) throws Exception {
                return connection.executeUpdate("CREATE TABLE Person(id INT PRIMARY KEY, name VARCHAR, birthday DATE);");
            }
        }, true);
    }

    @Before
    public void populate() throws Exception {
        queryTemplate.update(new UpdateQuery() {
            @Override
            public int perform(DatabaseConnection connection) throws Exception {
                int affectedRows = 0;

                affectedRows += connection.executeUpdate("INSERT INTO Person (id, name, birthday) VALUES (?, ?, ?)", 1, "John", johnBirthday);
                affectedRows += connection.executeUpdate("INSERT INTO Person (id, name, birthday) VALUES (?, ?, ?)", 2, "Doe", doeBirthday);

                return affectedRows;
            }
        }, true);

        DatabaseConnection.clearCache();
    }

    @After
    public void clear() throws Exception {
        queryTemplate.update(new UpdateQuery() {
            @Override
            public int perform(DatabaseConnection connection) throws Exception {
                return connection.executeUpdate("DELETE FROM Person");
            }
        }, true);
    }

    @Test
    public void transactionMethodsTest() throws Exception {
        int expectedRowAffected = 3;
        int result = queryTemplate.update(new UpdateQuery() {
            @Override
            public int perform(DatabaseConnection connection) throws Exception {
                int affectedRows = 0;

                affectedRows += connection.executeUpdate("INSERT INTO Person (id, name, birthday) VALUES (?, ?, ?)", 5, "Test", new Date(new java.util.Date().getTime()));
                affectedRows += connection.executeUpdate("UPDATE Person SET name = ? WHERE name = ?", "TestTest", "Test");
                affectedRows += connection.executeUpdate("DELETE FROM Person WHERE name = ?", "TestTest");

                return affectedRows;
            }
        }, true);

        Assert.assertEquals(expectedRowAffected, result);
    }

    @Test
    public void createTest() throws Exception {
        final Person person = new Person(3, "TestPerson", new Date(new java.util.Date().getTime()));
        int expectedRowAffected = 1;
        int result = queryTemplate.update(new UpdateQuery() {
            @Override
            public int perform(DatabaseConnection connection) throws Exception {
                return connection.persist(person);
            }
        }, true);

        Assert.assertEquals(expectedRowAffected, result);
    }

    @Test
    public void readTest() throws Exception {
        Person person = queryTemplate.query(new Query<Person>() {
            @Override
            public Person perform(DatabaseConnection connection) throws Exception {
                return connection.read(Person.class, 1);
            }
        }, false);

        Assert.assertEquals("John", person.getName());
        Assert.assertEquals(johnBirthday, person.getBirthday());
    }

    @Test
    public void updateTest() throws Exception {
        final Person person = new Person(1, "John Doe", new Date(new java.util.Date().getTime()));
        int expectedRowAffected = 1;
        int result = queryTemplate.update(new UpdateQuery() {
            @Override
            public int perform(DatabaseConnection connection) throws Exception {
                return connection.update(person);
            }
        }, true);

        Person anotherPerson = queryTemplate.query(new Query<Person>() {
            @Override
            public Person perform(DatabaseConnection connection) throws Exception {
                return connection.read(Person.class, 1);
            }
        }, false);

        Assert.assertNotNull(anotherPerson);
        Assert.assertEquals(expectedRowAffected, result);
    }

    @Test
    public void deleteTest() throws Exception {
        final Person person = new Person(1, "John", new Date(new java.util.Date().getTime()));
        int expectedRowAffected = 1;
        int result = queryTemplate.update(new UpdateQuery() {
            @Override
            public int perform(DatabaseConnection connection) throws Exception {
                return connection.delete(person);
            }
        }, true);

        Assert.assertEquals(expectedRowAffected, result);
    }

    @Test
    public void executeUpdateTest() throws Exception {
        final int doeId = 2;
        int expectedRowAffected = 1;
        int result = queryTemplate.update(new UpdateQuery() {
            @Override
            public int perform(DatabaseConnection connection) throws Exception {
                return connection.executeUpdate("UPDATE Person SET name = ? WHERE id = ?", "FunnyName", doeId);
            }
        }, true);

        Assert.assertEquals(expectedRowAffected, result);
    }

    @Test
    public void executeQueryTest() throws Exception {
        String expectedName = "John";
        final int joeId = 1;
        Person person = queryTemplate.query(new Query<Person>() {
            @Override
            public Person perform(DatabaseConnection connection) throws Exception {
                return connection.executeQuery("SELECT * FROM Person WHERE id = ?", Person.class, joeId);
            }
        }, false);

        Assert.assertNotNull(person);
        Assert.assertEquals(expectedName, person.getName());
    }

    @Test
    public void executeQueryForCollectionTest() throws Exception {
        int expectedSize = 2;
        List<Person> persons = queryTemplate.query(new Query<List<Person>>() {
            @Override
            public List<Person> perform(DatabaseConnection connection) throws Exception {
                return connection.executeQueryForCollection("SELECT * FROM Person", Person.class);
            }
        }, false);

        Assert.assertEquals(expectedSize, persons.size());
        Assert.assertEquals("John", persons.get(0).getName());
        Assert.assertEquals("Doe", persons.get(1).getName());
    }
}
