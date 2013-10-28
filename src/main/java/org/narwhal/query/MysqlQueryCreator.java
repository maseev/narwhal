package org.narwhal.query;


/**
 * MysqlQueryCreator is a class which is responsible for creating
 * string representation of the CRUD SQL statements for MySQL database.
 * This class implementation depends on MySQL SQL syntax.
 *
 * This class was created for only one purpose - in order to distinguish query creator
 * for MySQL database from one for PostgreSQL.
 * */
public class MysqlQueryCreator extends QueryCreator {}
