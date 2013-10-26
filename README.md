Narwhal
=======

JDBC wrapper library
-----------------------------

Narwhal is a cross-platform JDBC wrapper library that provides a convenient way to access to the relational databases.
The major aim is to create useful library that helps to retrieve particular information from the relational databases.

Features
--------
* Provides convenient methods for manipulating entities (persist, read, update, delete).
* Supports basic transaction management.
* Supports connection pool;
* Provides easy way to map class's fields to the database columns through annotation;
* Automatically creates instances of the mapped classes;
* Includes a small number of convenient methods that provide easy way to use prepared statements to query database;
* Does all the boring stuff for you (e.g. closes prepared statements, result set, closes database connections when some error occurs);
* Provides logging support that makes easy to log any situations that occur while working with the database (Narwhal's using [slf4j](http://www.slf4j.org/) to do logging).

How to build
------------
* Narwhal uses [Maven](http://maven.apache.org/) as a build tool, so make sure that you've got one installed on your machine.
* Type the following command in console in order to build the project - ``` mvn package ```

API
---
Here are an examples of using annotations to mark fields of the mapped class.
Here are couple of reasons why you would like to write the following class as it is.
First - fields of the mapped class and columns in the database table might have different names, so it quite obvious that you need to use
annotations in order to match your fields of the class with table's columns.
Second - you are probably in a very verbose mood :)

```java
@Table("PERSON")
public class Person {
	@Column(value = "PERSONID", primaryKey = true)
	private int id;
	@Column("NAME")
	private String name;
	@Column("BIRTHDAY")
	private Date birthday;

	public Person() {
	}

	// getter and setter methods
}
```

The example above could be rewritten as:

```java
@Table
public class Person {
	@Column(primaryKey = true)
	private int personId;
	@Column
	private String name;
	@Column
	private Date birthday;

	public Person() {
	}

	// getter and setter methods
}
```

Or even like this:

```java
@Table
public class Person {
	@Column(primaryKey = true)
	private int id;
	private String name;
	private Date birthday;

	public Person() {
	}

	// getter and setter methods
}
```
	
The following example illustrates creating connection to the MySQL database:

```java
String driver   = "com.mysql.jdbc.Driver";
String url      = "jdbc:mysql://localhost/bank";
String username = "lrngsql";
String password = "lrngsql";
DatabaseInformation information = new DatabaseInformation(driver, url, username, password);
DatabaseConnection  connection  = new DatabaseConnection(information);
```
	
You can also use connection pool in order to create necessary number of database connections:
	
```java
DatabaseInformation information = new DatabaseInformation(driver, url, username, password);
ConnectionPool connectionPool   = new ConnectionPool(information);

DatabaseConnection connection = connectionPool.getConnection();
	
// using connection

connectionPool.returnConnection(connection);

// after all
	
connectionPool.close();
```

Performing query to retrieve a particular entity:

```java
Person person = connection.executeQuery("SELECT id, name FROM person WHERE id = ?", Person.class, 1);
```	

Performing query to retrieve a list of entities which satisfies the query:

```java
List<Person> persons = connection.executeQueryForCollection("SELECT * FROM person", Person.class);
```	
		
Here is the example of using insert, update and delete queries:

```java
connection.executeUpdate("INSERT INTO person (id, name, birthday) VALUES (?, ?)", null, "John", new Date());
connection.executeUpdate("UPDATE person SET id = 1 WHERE name = ?", "John");
connection.executeUpdate("DELETE FROM person WHERE id > ?", 0);
```

The following example illustrates using convenient methods to persist entity to the database as well as basic transaction management:

```java
try {
	connection.beginTransaction();
	
	connection.persist(new Person(1, "John");
	connection.update(person1);
	connection.delete(person2);
	
	connection.commit();
	
	Person person = connection.read(Person.class, 1);
} catch (SQLException ex) {
	ex.printStackTrace();
	connection.rollback();
}
```

Library dependencies
--------------------

* Narwhal depends on [slf4j](http://www.slf4j.org/) to do logging.