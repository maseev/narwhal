package org.narwhal.query;

import java.util.Collections;
import java.util.List;

/**
 * QueryCreator is a class which is responsible for creating string representation of the CRUD SQL statements.
 * */
public class QueryCreator {

    /**
     * Builds up a string representation of an INSERT SQL statement for the subsequent usage in a prepared statement.
     *
     * @param tableName String representation of the table name that maps to the particular entity.
     * @param columns Array of the table's columns
     * @return String representation of the INSERT SQL statement.
     * */
    public String buildInsertQuery(String tableName, List<String> columns) {
        return "INSERT INTO " + tableName + " (" + join(columns, ",") + ") VALUES (" + join("?", columns.size(), ",") + ')';
    }

    /**
     * Builds up a string representation of an SELECT SQL statement for the subsequent usage in a prepared statement.
     *
     * @param tableName String representation of the table name that maps to the particular entity.
     * @param columns Array of the table's columns
     * @param primaryKeys Primary column names.
     * @return String representation of the SELECT SQL statement.
     * */
    public String buildSelectQuery(String tableName, List<String> columns, List<String> primaryKeys) {
        return "SELECT " + join(columns, ",") + " FROM " + tableName + buildWhereClause(primaryKeys);
    }

    /**
     * Builds up a string representation of an DELETE SQL statement for the subsequent usage in a prepared statement.
     *
     * @param tableName String representation of the table name that maps to the particular entity.
     * @param primaryKeys Primary column names.
     * @return String representation of the DELETE SQL statement.
     * */
    public String buildDeleteQuery(String tableName, List<String> primaryKeys) {
        return "DELETE FROM " + tableName + buildWhereClause(primaryKeys);
    }

    /**
     * Builds up a string representation of an UPDATE SQL statement for the subsequent usage in a prepared statement.
     *
     * @param tableName String representation of the table name that maps to the particular entity.
     * @param columns Array of the table's columns
     * @param primaryKeys Primary column names.
     * @return String representation of the UPDATE SQL statement.
     * */
    public String buildUpdateQuery(String tableName, List<String> columns, List<String> primaryKeys) {
        return "UPDATE " + tableName + " SET " + join(columns, ",", " = ?") + buildWhereClause(primaryKeys);
    }

    private String buildWhereClause(List<String> primaryKeys) {
        return " WHERE " + join(primaryKeys, " AND ", " = ?");
    }

    private String join(String filler, int size, String delimiter) {
        return join(Collections.nCopies(size, filler), delimiter);
    }

    private String join(List<String> values, String delimiter) {
        return join(values, delimiter, "");
    }

    private String join(List<String> values, String delimiter, String afterValue) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                builder.append(delimiter);
            }

            builder.append(values.get(i));
            builder.append(afterValue);
        }

        return builder.toString();
    }
}
