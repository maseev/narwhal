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
Notice, that your entity class has to have a default constructor, so Narwhal would be able to create an instance of that class.

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

Notice, in the example above you don't even have to to specify any getters and setters for class fields.
Narwhal uses reflection API in order to get or set some class's fields value if there's no getter or setter.
	
The following example illustrates creating a connection to PostgreSQL database:

```java
String driver   = "org.postgresql.Driver";
String url      = "jdbc:postgresql://localhost:5432/test?charSet=UTF8";
String username = "test";
String password = "test";
ConnectionInformation connectionInformation(driver, url, username, password);
DatabaseConnection connection = new DatabaseConnection(connectionInformation);
```

You can also use a connection pool in order to create a necessary number of database connections as well as use
a very handy query method which retrieves a database connection from the connection pool and performs a query against the database:
	
```java
ConnectionPool connectionPool = new ConnectionPool(connectionInformation);

List<Person> people = connectionPool.query(new Query<List<Person>>() {
    @Override
    public List<Person> perform(DatabaseConnection connection) {
        return connection.executeQueryForCollection("SELECT * FROM Person", Person.class);
    }
});
```

Performing a query to retrieve an entity:

```java
Person person = connection.executeQuery("SELECT id, name FROM person WHERE id = ?", Person.class, 1);
```	

Performing a query to retrieve a list of entities which satisfies the query:

```java
List<Person> persons = connection.executeQueryForCollection("SELECT * FROM person", Person.class);
```	
		
Here is an example of using insert, update and delete queries:

```java
connection.executeUpdate("INSERT INTO person (id, name, birthday) VALUES (?, ?, ?)", null, "John", new Date());
connection.executeUpdate("UPDATE person SET id = 1 WHERE name = ?", "John");
connection.executeUpdate("DELETE FROM person WHERE id > ?", 0);
```

The following example illustrates a process of using convenient methods such as ``` persist ```, ``` update ``` and ``` delete ``` as well as basic transaction management:

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