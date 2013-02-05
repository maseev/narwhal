Narwhal
=======

Simple JDBC wrapper library
-----------------------------

Narwhal is a cross-platform JDBC wrapper library that provides a convenient way to access to the relational databases.
The major aim is to create useful library that helps to retrieve particular information from the relational databases.

Features
--------

* Supports connection pool;
* Provides easy way to map fields of a class to the columns of the database through annotation;
* Automatically creating instance of the mapped class;
* Includes succint number of convenient methods that provide the easy way to use prepared statements to query database;
* Automated work with resources (e.g. closing prepared statements, result set, closing database connection when some error occurs);
* Provides logging support that makes easy to log any situations that occur when working with database (Narwhal's using [slf4j](http://www.slf4j.org/) to do logging).

API
---
Here's an example of using annotation to mark fields of the mapped class.
Notice! All the mapped classes have to use annotation to map fields to the corresponding columns of the database table:

```java
public class Person {
	@Column("person_id")
	private int id;
	@Column("name")
	private String name;

	public Person() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
```
	
The following example illustrates creating connection to the MySQL database:

```java
String driver = "com.mysql.jdbc.Driver";
String url = "jdbc:mysql://localhost/bank";
String username = "lrngsql";
String password = "lrngsql";
DatabaseInformation information = new DatabaseInformation(driver, url, username, password);
DatabaseConnection  connection  = new DatabaseConnection(information);
```
	
You can also use connection pool for creating necessary number of database connection:
	
```java
DatabaseInformation information = new DatabaseInformation(driver, url, username, password);
ConnectionPool connectionPool  = new ConnectionPool(information);

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

Performing query to retrieve a list of entities which satisfy the query:

```java
List<Person> persons = connection.executeQueryForCollection("SELECT * FROM person", Person.class);
```	
		
Here are the examples of using insert, update and delete queries:

```java
connection.executeUpdate("INSERT INTO person (id, name) VALUES (?, ?)", null, "John");
connection.executeUpdate("UPDATE person SET id = 1 WHERE name = ?", "John");
connection.executeUpdate("DELETE FROM person WHERE id > ?", 0);
```	
	
Library dependencies
--------------------

* Narwhal depends on [slf4j](http://www.slf4j.org/) to do logging.

License
-------

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to
deal in the Software without restriction, including without limitation the
rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
sell copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS 
BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE 
OR OTHER DEALINGS IN THE SOFTWARE.