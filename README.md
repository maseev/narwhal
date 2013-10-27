Narwhal
=======

JDBC wrapper library
-----------------------------

Narwhal is a cross-platform JDBC wrapper library which provides a convenient way to access to the relational databases.
The major aim was to create useful library that helps retrieve data from the relational databases.

Features
--------
* Provides convenient methods for manipulating entities (persist, read, update, delete).
* Supports basic transaction management.
* Supports connection pool;
* Provides an easy way to map class's fields to the database columns through annotations;
* Automatically creates instances of the mapped classes;
* Includes a small number of convenient methods that provide easy way to use prepared statements to query database;
* Does all the boring stuff for you (e.g. closes prepared statements and result sets, closes database connections when some error occurs);

How to build
------------
* Narwhal uses [Maven](http://maven.apache.org/) as a build tool, so make sure that you've got one installed on your machine.
* Type the following command in console in order to build the project - ``` mvn package ```

API
---
Here are the examples of using annotations to mark fields of the mapped class.
Here are couple of reasons why you would like to write the following class as it is.
First - fields of the mapped class and columns in the database table might have different names, so it quite obvious that you need to use
annotations in order to match your fields of the class with table's columns.
Second - you are probably in a very verbose mood :).
Notice, that you have to provide default constructor as well as getters and setters methods for the entity class.
Narwhal uses them in order to create instances of the specific class and populates them with data.

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
String url      = "jdbc:mysql://localhost/test";
String username = "test";
String password = "test";
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

// in the end
	
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
		
Here is an example of using insert, update and delete queries:

```java
connection.executeUpdate("INSERT INTO person (id, name, birthday) VALUES (?, ?, ?)", null, "John", new Date());
connection.executeUpdate("UPDATE person SET id = 1 WHERE name = ?", "John");
connection.executeUpdate("DELETE FROM person WHERE id > ?", 0);
```

The following example illustrates process of using convenient methods such as ``` persist ```, ``` update ``` and ``` delete ``` as well as basic transaction management:

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