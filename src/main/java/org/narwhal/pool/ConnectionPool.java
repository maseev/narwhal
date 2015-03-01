package org.narwhal.pool;

import org.narwhal.core.ConnectionInformation;
import org.narwhal.core.DatabaseConnection;
import org.narwhal.core.Query;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The <code>ConnectionPool</code> class implements
 * basic functionality that allows end-users persist
 * variable number of database connection.
 * This class provides database connection pool and
 * takes care of lifetime and resource management.
 *
 * @author Miron Aseev
 */
public class ConnectionPool {

    private static final int DEFAULT_POOL_SIZE = 5;

    private static final int DEFAULT_ACQUIRE_INCREMENT = 5;

    private int size;

    private int acquireIncrement;

    private ConnectionInformation connectionInformation;

    private Lock connectionsLock;

    private Lock variableLock;

    private List<DatabaseConnection> connections;

    /**
     * Initializes a new instance of the ConnectionPool class.
     * The instance is specified by DatabaseInformation instance that
     * keeps all the information to be able to make connection to the database.
     * Default pool size is 5.
     * Acquire increment is 5.
     *
     * @param connectionInformation instance of {@code DatabaseInformation} class that includes
     *                            all the information for making connection to the database.
     * @throws SQLException If any database access problems happened.
     * @throws ClassNotFoundException If there's any problem with finding a jdbc driver class.
     * */
    public ConnectionPool(ConnectionInformation connectionInformation) throws SQLException, ClassNotFoundException {
        this(connectionInformation, DEFAULT_POOL_SIZE, DEFAULT_ACQUIRE_INCREMENT);
    }

    /**
     * Initializes a new instance of the ConnectionPool class.
     * The instance is specified by DatabaseInformation instance that
     * keeps all the information to make connection to the database.
     * Instance is also specified by the size and acquireIncrement variable.
     *
     * @param connectionInformation instance of {@code DatabaseInformation} class that includes
     *                            all the information for making connection to the database.
     * @param size the size of the pool.
     * @param acquireIncrement the acquire increment of the pool.
     * @throws SQLException If any database access problems happened.
     * @throws ClassNotFoundException If there's any problem with finding a jdbc driver class.
     * */
    public ConnectionPool(ConnectionInformation connectionInformation, int size, int acquireIncrement)
            throws SQLException, ClassNotFoundException {
        if (size < 1) {
            throw new IllegalArgumentException("Connection pool's size can't be less than 1");
        }

        if (acquireIncrement < 1) {
            throw new IllegalArgumentException("acquireIncrement's value can't be less than 1");
        }

        this.connectionInformation = connectionInformation;
        this.size = size;
        this.acquireIncrement = acquireIncrement;

        connectionsLock = new ReentrantLock();
        variableLock = new ReentrantLock();
        connections = createDatabaseConnections(size);
    }

    public <T> T query(Query<T> query) throws SQLException, ClassNotFoundException {
        DatabaseConnection connection = getConnection();

        try {
            return query.perform(connection);
        } finally {
            returnConnection(connection);
        }
    }

    public <T> T queryInTransaction(Query<T> query) throws SQLException, ClassNotFoundException {
        DatabaseConnection connection = getConnection();

        try {
            try {
                connection.beginTransaction();
                T result = query.perform(connection);
                connection.commit();

                return result;
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            }
        } finally {
            returnConnection(connection);
        }
    }

    /**
     * Returns DatabaseConnection object from the pool.
     * This method removes connection from the pool collection.
     * After working with DatabaseConnection object you should
     * return connection to the pool by invoking returnConnection() method.
     *
     * @return DatabaseConnection object.
     * @throws SQLException If any database access problems happened.
     * @throws ClassNotFoundException If there's any problem with finding a jdbc driver class.
     * */
    public DatabaseConnection getConnection() throws SQLException, ClassNotFoundException {
        connectionsLock.lock();
        try {
            if (connections.isEmpty()) {
                connections.addAll(createDatabaseConnections(getAcquireIncrement()));

                variableLock.lock();
                try {
                    size += acquireIncrement;
                } finally {
                    variableLock.unlock();
                }
            }

            return connections.remove(connections.size() - 1);
        } finally {
            connectionsLock.unlock();
        }
    }

    /**
     * Returns connection to the pool.
     *
     * @param connection Database connection which is going to be added to the pool.
     * */
    public void returnConnection(DatabaseConnection connection) {
        connectionsLock.lock();
        try {
            if (connections.size() == getSize()) {
                throw new IllegalArgumentException("Pool is full");
            }

            connections.add(connection);
        } finally {
            connectionsLock.unlock();
        }
    }

    /**
     * Closes all the database connections that is waiting in the pool.
     *
     * @throws SQLException If any database access problems happened.
     * */
    public void close() throws SQLException {
        connectionsLock.lock();
        try {
            for (DatabaseConnection connection : connections) {
                if (!connection.isClosed()) {
                    connection.close();
                }
            }

            connections.clear();
        } finally {
            connectionsLock.unlock();
        }
    }

    /**
     * Returns size of the pool.
     *
     * @return Size of the pool.
     * */
    public int getSize() {
        variableLock.lock();
        try {
            return size;
        } finally {
            variableLock.unlock();
        }
    }

    /**
     * Sets new size of the pool. If the new size of the pool is smaller than initial size,
     * then pool will be condensed to the new size. Unnecessary connections will be closed.
     * @param newSize The new pool's size.
     * @throws SQLException If any database access problems happened.
     * @throws ClassNotFoundException If there's any problem with finding a jdbc driver class.
     * */
    public void setSize(int newSize) throws SQLException, ClassNotFoundException {
        if (newSize < 1) {
            throw new IllegalArgumentException("Size value must be a positive number");
        }

        connectionsLock.lock();
        try {
            variableLock.lock();
            try {
                int numberOfConnections = Math.abs(newSize - size);

                if (newSize > size) {
                    connections.addAll(createDatabaseConnections(numberOfConnections));
                } else {
                    while (numberOfConnections > 0 && !connections.isEmpty()) {
                        connections.remove(connections.size() - 1).close();
                        numberOfConnections--;
                    }
                }

                this.size = newSize;
            } finally {
                variableLock.unlock();
            }
        } finally {
            connectionsLock.unlock();
        }
    }

    /**
     * Returns acquire increment value.
     *
     * @return Acquire increment value.
     * */
    public int getAcquireIncrement() {
        variableLock.lock();
        try {
            return acquireIncrement;
        } finally {
            variableLock.unlock();
        }
    }

    /**
     * Sets new acquire increment value.
     *
     * @param newAcquireIncrement New acquire increment value.
     * */
    public void setAcquireIncrement(int newAcquireIncrement) {
        if (newAcquireIncrement < 0) {
            throw new IllegalArgumentException("newAcquireIncrement must be a positive number");
        }

        variableLock.lock();
        try {
            this.acquireIncrement = newAcquireIncrement;
        } finally {
            variableLock.unlock();
        }
    }

    /**
     * Creates necessary number of database connections.
     *
     * @param requiredSize Number of the database connection.
     * @return List of the database connections.
     * @throws SQLException If any database access problems happened.
     * @throws ClassNotFoundException If there's any problem with finding a jdbc driver class.
     * */
    private List<DatabaseConnection> createDatabaseConnections(int requiredSize) throws SQLException,
                                                                                        ClassNotFoundException {
        List<DatabaseConnection> conn = new ArrayList<>(requiredSize);

        for (int i = 0; i < requiredSize; ++i) {
            conn.add(new DatabaseConnection(connectionInformation));
        }

        return conn;
    }
}
