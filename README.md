Narwhal
=======

JDBC wrapper library
-----------------------------

Narwhal is a tiny JDBC wrapper library which provides an easy way to access to the relational databases.
Narwhal generates generic SQL queries for CRUD operations so it is supposed to work equally with popular relational databases.

Features
--------
* Provides convenient methods for manipulating entities (persist, read, update, delete).
* Supports basic transaction management.
* Supports connection pool;
* Provides an easy way to map class fields to table columns through annotations;
* Automatically creates instances of the mapped classes;
* Includes a small set of convenient methods that provide an easy way to use prepared statements to query database;
* Does all boring stuff for you (e.g. closes prepared statements and result sets, closes database connections when some error occurs);

How to build
------------
* Narwhal uses [Maven](http://maven.apache.org/) as a build tool, so make sure that you've got one installed on your machine.
* Type the following command in console to build the project - ``` mvn package ```

API
---
Here are some examples of using annotations to mark fields of the mapped class.
Here are couple of reasons why you would like to write the following class as it is.
First - fields of the mapped class and columns in the database table might have different names, so it's quite obvious that you have to use
annotations in order to match your class fields with table columns.
Second - you are probably in a very verbose mood :).
Notice, that your entity class must have a default constructor, so Narwhal would be able to create instances of that class.

```java
@Entity("PERSON")
public class Person {
    @Id
	@Column(value = "ID")
	private int id;

	@Column("NAME")
	private String name;

	@Column("BIRTHDAY")
	private Date birthday;

	// getter and setter methods
}
```

The example above could be rewritten as:

```java
@Entity
public class Person {
    @Id
	private int id;

	private String name;

	private Date birthday;
}
```

Notice, in the example above you don't even have to to create any getters and setters for class fields.
Narwhal uses reflection API in order to get and set class's fields values if there's no getter or setter.
	
The following example illustrates creating a connection to PostgreSQL database:

```java
HikariDataSource dataSource = new HikariDataSource();
dataSource.setJdbcUrl("jdbc:postgresql://localhost:5432/test?charSet=UTF8");
dataSource.setDriverClassName("org.postgresql.Driver");
dataSource.setUsername("test");
dataSource.setPassword("test");

QueryTemplate queryTemplate = new QueryTemplate(dataSource);
```

Performing a query to retrieve a single entity:

```java
Person person = queryTemplate.run(new Query<Person>() {
	@Override
	public Person perform(DatabaseConnection connection) throws Exception {
		return connection.executeQuery("SELECT * FROM Person WHERE id = ?", Person.class, 1);
	}
}, false);
```	

Performing a query to retrieve a list of entities:

```java
List<Person> people = queryTemplate.run(new Query<List<Person>>() {
    @Override
    public List<Person> perform(DatabaseConnection connection) {
        return connection.executeQueryForCollection("SELECT * FROM Person", Person.class);
    }
}, false);
```	
		
Here is an example of using insert, update and delete queries:

```java
queryTemplate.run(new UpdateQuery() {
	@Override
	public Integer perform(DatabaseConnection connection) throws Exception {
		int affectedRows = 0;

		affectedRows += connection.executeUpdate("INSERT INTO Person (id, name, birthday) VALUES (?, ?, ?)", 5, "Test", new Date(new java.util.Date().getTime()));
		affectedRows += connection.executeUpdate("UPDATE Person SET name = ? WHERE name = ?", "TestTest", "Test");
		affectedRows += connection.executeUpdate("DELETE FROM Person WHERE name = ?", "TestTest");

		return affectedRows;
	}
}, true);
```

The following example illustrates a process of using convenient methods such as ``` persist ```, ``` update ``` and ``` delete ```:

```java
queryTemplate.run(new UpdateQuery() {
	@Override
	public Integer perform(DatabaseConnection connection) throws Exception {
		int affectedRows = 0;

		affectedRows += connection.persist(new Person(1, "John");
		affectedRows += connection.update(person1);
		affectedRows += connection.delete(person2);

		return affectedRows;
	}
}, true);
```