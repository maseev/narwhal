package org.narwhal.core;

import javax.sql.DataSource;
import java.sql.SQLException;

/**1
 * The <code>ConnectionPool</code> class implements
 * basic functionality that allows end-users persist
 * variable number of database connection.
 * This class provides database connection pool and
 * takes care of lifetime and resource management.
 *
 * @author Miron Aseev
 */
public class QueryTemplate {

    private DataSource dataSource;

    /**
     * Initializes a new instance of the ConnectionPool class.
     * The instance is specified by DatabaseInformation instance that
     * keeps all the information to be able to make connection to the database.
     * Default pool size is 5.
     * Acquire increment is 5.
     *
     * @param dataSource instance of {@code java.sql.DataSource} class that includes
     *                     all the information for making connection to the database.
     * @throws SQLException If any database access problems happened.
     * @throws ClassNotFoundException If there's any problem with finding a jdbc driver class.
     * */
    public QueryTemplate(DataSource dataSource) throws SQLException, ClassNotFoundException {
        this.dataSource = dataSource;
    }

    public <T> T run(Query<T> query, boolean runInTransaction) throws Exception {
        DatabaseConnection connection = getConnection();

        try {
            try {
                if (runInTransaction) {
                    connection.beginTransaction();
                }

                T result = query.perform(connection);

                if (runInTransaction) {
                    connection.commit();
                }

                return result;
            } catch (SQLException ex) {
                if (runInTransaction) {
                    connection.rollback();
                }

                throw ex;
            }
        } finally {
            connection.close();
        }
    }

    /**
     * Returns DatabaseConnection object from the pool.
     * This method removes connection from the pool collection.
     *
     * @return DatabaseConnection object.
     * @throws SQLException If any database access problems happened.
     * @throws ClassNotFoundException If there's any problem with finding a jdbc driver class.
     * */
    private DatabaseConnection getConnection() throws SQLException, ClassNotFoundException {
        return new DatabaseConnection(dataSource.getConnection());
    }
}
