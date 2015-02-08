package org.narwhal.query;

/**
 * QueryCreator is a class which is responsible for creating string representation of the CRUD SQL statements.
 * This class implementation doesn't depend on any particular database syntax.
 * */
public abstract class QueryCreator {

    /**
     * Builds up a string representation of an INSERT SQL statement for the subsequent usage in a prepared statement.
     *
     * @param tableName String representation of the table name that maps to the particular entity.
     * @param columns Array of the table's columns
     * @param primaryColumnName String representation of the primary column.
     * @return String representation of the INSERT SQL statement.
     * */
    public String makeInsertQuery(String tableName, String[] columns, String primaryColumnName) {
        StringBuilder builder = new StringBuilder("INSERT INTO ");
        builder.append(tableName);
        builder.append(" VALUES (");

        for (int i = 0; i < columns.length; ++i) {
            if (i > 0) {
                builder.append(',');
            }

            builder.append('?');
        }
        builder.append(')');

        return builder.toString();
    }

    /**
     * Builds up a string representation of an SELECT SQL statement for the subsequent usage in a prepared statement.
     *
     * @param tableName String representation of the table name that maps to the particular entity.
     * @param columns Array of the table's columns
     * @param primaryColumnName String representation of the primary column.
     * @return String representation of the SELECT SQL statement.
     * */
    public String makeSelectQuery(String tableName, String[] columns, String primaryColumnName) {
        StringBuilder builder = new StringBuilder("SELECT ");

        for (int i = 0; i < columns.length; ++i) {
            if (i > 0) {
                builder.append(',');
            }

            builder.append(columns[i]);
        }

        builder.append(" FROM ");
        builder.append(tableName);
        builder.append(" WHERE ");
        builder.append(primaryColumnName);
        builder.append(" = ?");

        return builder.toString();
    }

    /**
     * Builds up a string representation of an DELETE SQL statement for the subsequent usage in a prepared statement.
     *
     * @param tableName String representation of the table name that maps to the particular entity.
     * @param primaryColumnName String representation of the primary column.
     * @return String representation of the DELETE SQL statement.
     * */
    public String makeDeleteQuery(String tableName, String primaryColumnName) {
        StringBuilder builder = new StringBuilder("DELETE FROM ");
        builder.append(tableName);
        builder.append(" WHERE ");
        builder.append(primaryColumnName);
        builder.append(" = ?");

        return builder.toString();
    }

    /**
     * Builds up a string representation of an UPDATE SQL statement for the subsequent usage in a prepared statement.
     *
     * @param tableName String representation of the table name that maps to the particular entity.
     * @param columns Array of the table's columns
     * @param primaryColumnName String representation of the primary column.
     * @return String representation of the UPDATE SQL statement.
     * */
    public String makeUpdateQuery(String tableName, String[] columns, String primaryColumnName) {
        StringBuilder builder = new StringBuilder("UPDATE ");
        builder.append(tableName);
        builder.append(" SET ");

        for (int i = 0; i < columns.length; ++i) {
            if (i > 0) {
                builder.append(',');
            }

            builder.append(columns[i]);
            builder.append(" = ?");
        }
        builder.append(" WHERE ");
        builder.append(primaryColumnName);
        builder.append(" = ?");

        return builder.toString();
    }
}
