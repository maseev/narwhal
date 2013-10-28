package org.narwhal.query;


/**
 * PostgreSQLQueryCreator is a class which is responsible for creating
 * string representation of the CRUD SQL statements for PostgreSQL database.
 * This class implementation depends on PostgreSQL SQL syntax.
 * */
public class PostgreSQLQueryCreator extends QueryCreator {


    @Override
    public String makeInsertQuery(String tableName, String[] columns, String primaryColumnName) {
        StringBuilder builder = new StringBuilder("INSERT INTO ");
        builder.append(tableName);
        builder.append('(');
        for (int i = 0; i < columns.length; ++i) {
            if (!primaryColumnName.equals(columns[i])) {
                builder.append(columns[i]);

                if (i > 0 && i < columns.length - 1) {
                    builder.append(',');
                }
            }
        }
        builder.append(") VALUES (");

        for (int i = 0; i < columns.length; ++i) {
            if (!primaryColumnName.equals(columns[i])) {
                builder.append('?');

                if (i > 0 && i < columns.length - 1) {
                    builder.append(',');
                }
            }
        }
        builder.append(')');

        return builder.toString();
    }
}
