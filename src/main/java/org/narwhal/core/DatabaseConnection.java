package org.narwhal.core;

import org.narwhal.query.QueryCreator;
import org.narwhal.util.MappedClassInformation;
import org.narwhal.util.QueryType;

import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * The <code>DatabaseConnection</code> represents a connection to a database.
 * This class includes methods for retrieving the particular information from the relational databases.
 * It automatically maps retrieved result set to entity (java class).
 * DatabaseConnection class also manages all resources like database connection,
 * prepared statements, result sets etc.
 * This class also provides basic transaction management and convenient methods for persist,
 * update, delete, read entity from a database.
 * </p>
 *
 * <p>
 *     Here's an example how to use the annotation:
 *
 *     <p><code>
 *         {@literal @}Entity
 *         public class Person {
 *             {@literal @}Id
 *             private int id;
 *
 *             private String name;
 *
 *             // other stuff
 *         }
 *     </code></p>
 *
 *     The methods of the <code>DatabaseConnection</code> class use the annotations and annotated fields
 *     to retrieve necessary information from the database and to invoke set methods through the Java reflection API.
 * </p>

 * <p>Here are some examples how DatabaseConnection can be used:</p>

 * <p><code>
 * DatabaseConnection connection = new DatabaseConnection(new DatabaseInformation(driver, url, username, password));
 *
 * connection.executeUpdate("UPDATE person SET name = ? WHERE id = ?", name, id);
 *
 * Person person = connection.executeQuery("SELECT * FROM person WHERE id = ?", Person.class, id);
 * </code>
 * </p>
 *
 * @author Miron Aseev
 * @see ConnectionInformation
 */
public class DatabaseConnection {

    private static final Map<Class, MappedClassInformation> cache = new ConcurrentHashMap<>();

    private Connection connection;

    private QueryCreator queryCreator;

    /**
     * Initializes a new instance of the DatabaseConnection class and trying to connect to the database.
     *
     * @param connectionInformation instance of {@code DatabaseInformation} class that includes
     *                            all the information for making connection to the database.
     * @throws SQLException If any database access problems happened.
     * @throws ClassNotFoundException IF there's any error with finding a jdbc driver class.
     * */
    public DatabaseConnection(ConnectionInformation connectionInformation) throws SQLException, ClassNotFoundException {
        connection = getConnection(connectionInformation);
        queryCreator = new QueryCreator();
    }

    /**
     * Clears cache that has information about mapped classes.
     * */
    public static void clearCache() {
        cache.clear();
    }

    /**
     * Starts a new transaction.
     *
     * @throws SQLException If any database access problems happened.
     * */
    public void beginTransaction() throws SQLException {
        connection.setAutoCommit(false);
    }

    /**
     * Makes all changes which have been made since the previous commit/rollback permanent
     * and releases any database locks which are currently held by this Connection object.
     * This method should be used only when auto-commit mode is disabled.
     *
     * @throws SQLException If any database access problems happened.
     * */
    public void commit() throws SQLException {
        connection.commit();
        connection.setAutoCommit(true);
    }

    /**
     * Undoes all changes which have been made in the current transaction and releases any database
     * locks which are currently held by this Connection object.
     * This method should be used only when auto-commit mode is disabled.
     *
     * @throws SQLException If any database access problems happened.
     * */
    public void rollback() throws SQLException {
        connection.rollback();
    }

    /**
     * Persists entity in the database.
     *
     * @param object Entity object that should be persisted in the database.
     * @return Number of rows that have been affected after performing sql query.
     * @throws SQLException If any database access problems happened.
     * @throws ReflectiveOperationException If there's any problem which has connection with Reflection API.
     * */
    public int persist(Object object) throws SQLException, ReflectiveOperationException {
        MappedClassInformation classInformation = getMappedClassInformation(object.getClass());
        String query = classInformation.getQuery(QueryType.CREATE);

        return executeUpdate(query, getParameters(object));
    }

    /**
     * Retrieves a particular entity from the database by using primary key.
     *
     * @param mappedClass A Class, whose annotated fields will be used for creating corresponding entity.
     * @param primaryKeys primary keys that are used to find a particular row in the database.
     * @throws SQLException If any database access problems happened.
     * @throws ReflectiveOperationException If there's any problem which has connection with Reflection API.
     * */
    public <T> T read(Class<T> mappedClass, Object... primaryKeys) throws SQLException, ReflectiveOperationException {
        MappedClassInformation classInformation = getMappedClassInformation(mappedClass);
        String query = classInformation.getQuery(QueryType.READ);

        return executeQuery(query, mappedClass, primaryKeys);
    }

    /**
     * Updates a particular entity in the database.
     *
     * @param object entity which data will be used to update row in the database.
     * @return Number of rows that have been affected after performing sql query.
     * @throws SQLException If any database access problems happened.
     * @throws ReflectiveOperationException If there's any problem which has connection with Reflection API.
     * */
    public int update(Object object) throws SQLException, ReflectiveOperationException {
        List<Object> parameters = new ArrayList<>(Arrays.asList(getParameters(object)));
        MappedClassInformation classInformation = getMappedClassInformation(object.getClass());
        String query = classInformation.getQuery(QueryType.UPDATE);
        parameters.addAll(Arrays.asList(getPrimaryKeyMethodValues(object)));

        return executeUpdate(query, parameters.toArray());
    }

    /**
     * Deletes a particular entity from the database.
     *
     * @param  object entity that will be deleted from the database.
     * @return Number of rows that have been affected after performing sql query.
     * @throws SQLException If any database access problems happened.
     * @throws ReflectiveOperationException If there's any problem which has connection with Reflection API.
     * */
    public int delete(Object object) throws SQLException, ReflectiveOperationException {
        MappedClassInformation classInformation = getMappedClassInformation(object.getClass());
        String query = classInformation.getQuery(QueryType.DELETE);
        Object[] primaryKeysValues = getPrimaryKeyMethodValues(object);

        return executeUpdate(query, primaryKeysValues);
    }

    /**
     * Builds and executes UPDATE SQL query. This method returns the number of rows that have been affected.
     *
     * Here is an example of usage:
     * <p>
     *     <code>
     *         connection.executeUpdate("DELETE FROM person WHERE id = ? AND name = ?", id, name);
     *     </code>
     * </p>
     *
     * As you could see in the example above, this method takes the string representation of SQL update query
     * and the variable number of parameters.
     * This method combines two parameters, so let's assume that <code>id </code> variable is assigned to 1
     * and <code>name</code> variable is assigned to "John".
     *
     * Then you'll get the following SQL query:
     *
     * <p>
     *     <code>
     *         "DELETE FROM person WHERE id = 1 AND name = 'John'"
     *     </code>
     * </p>
     *
     * @param query SQL update query (UPDATE, DELETE, INSERT) that can include wildcard symbol - "?".
     * @param parameters Arbitrary number of parameters that will be used to substitute wildcard
     *                   symbols in the SQL query parameter.
     * @return Number of rows that have been affected.
     * @throws SQLException If any database access error happened
     * */
    public int executeUpdate(String query, Object... parameters) throws SQLException {
        PreparedStatement preparedStatement = null;
        int result = 0;

        try {
            try {
                preparedStatement = createPreparedStatement(query, parameters);
                result = preparedStatement.executeUpdate();
            } finally {
                close(preparedStatement);
            }
        } catch (SQLException ex) {
            close();
            throw ex;
        }

        return result;
    }

    /**
     * Executes prepared SQL query.
     * This method returns the corresponding object, whose type was pointed as a second parameter.
     *
     * Here is an example of usage:
     * <p>
     *     <code>
     *         Person person = connection.executeQuery("SELECT * FROM person WHERE id = ?", Person.class, id);
     *     </code>
     * </p>
     *
     * As you could see in the example above, this method takes the string representation of SQL update query
     * and the variable number of parameters. Its also takes a type of class that will be used for building result object.
     * This method combines two parameters, so let's assume that <code>id </code> variable is assigned to 1
     *
     * Then you'll get the following SQL query:
     *
     * <p>
     *     <code>
     *         SELECT * FROM person WHERE id = 1"
     *     </code>
     * </p>
     *
     * @param query A SQL select query that can have the wildcard symbols.
     * @param mappedClass A Class, whose annotated fields will be used for creating corresponding entity.
     * @param parameters Arbitrary number of parameters that will be used to substitute wildcard
     *                   symbols in the SQL query parameter.
     * @return Mapped object that was created by based on the data from the result set.
     * @throws SQLException If any database access problems happened.
     * @throws ReflectiveOperationException If there's any problem which has connection with Reflection API.
     * */
    public <T> T executeQuery(String query, Class<T> mappedClass,
                              Object... parameters) throws SQLException, ReflectiveOperationException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        T result = null;

        try {
            try {
                preparedStatement = createPreparedStatement(query, parameters);
                resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    result = createEntity(resultSet, mappedClass);
                }
            } finally {
                close(resultSet);
                close(preparedStatement);
            }
        } catch (SQLException ex) {
            close();
            throw ex;
        }

        return result;
    }

    /**
     * Executes prepared SQL query.
     * This method returns the collection of the corresponding objects,
     * whose type was pointed as a second parameter.
     *
     * Here is example of usage:
     * <p>
     * <code>
     * List<Person> persons = connection.executeQueryForCollection("SELECT * FROM person WHERE id = ?", Person.class, id);
     * </code>
     * </p>
     *
     * As you could see in the example above, this method takes the string representation of SQL update query
     * and the variable number of parameters. Its also takes a type of class that will be used for building result object.
     * This method combines two parameters, so let's assume that <code>id </code> variable is assigned to 1
     *
     * Then you'll get the following SQL query:
     *
     * <p>
     *     <code>
     *         SELECT * FROM person WHERE id = 1"
     *     </code>
     * </p>
     *
     * @param query A SQL select query that can have the wildcard symbols.
     * @param mappedClass A Class, whose annotated fields will be used for creating corresponding entity.
     * @param parameters An Arbitrary number of parameters that will be used to substitute wildcard
     *                   symbols in the SQL query parameter.
     * @return A List of the entity objects. Objects have type that was pointed as a second parameter.
     * @throws SQLException If any database access problems happened.
     * @throws ReflectiveOperationException If there's any problem which has connection with Reflection API.
     * */
    public <T> List<T> executeQueryForCollection(String query,
                                                 Class<T> mappedClass,
                                                 Object... parameters) throws SQLException,
                                                                              ReflectiveOperationException {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<T> collection = new ArrayList<>();

        try {
            try {
                preparedStatement = createPreparedStatement(query, parameters);
                resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    collection.add(createEntity(resultSet, mappedClass));
                }
            } finally {
                close(resultSet);
                close(preparedStatement);
            }
        } catch (SQLException ex) {
            close();
            throw ex;
        }

        return collection;
    }

    /**
     * Closes database connection.
     *
     * @throws SQLException If any database access problems happened.
     * */
    public void close() throws SQLException {
        connection.close();
    }

    /**
     * Tests whether database connection is closed or not.
     *
     * @return true if database connection is closed. false otherwise
     * @throws SQLException If any database access problems happened.
     * */
    public boolean isClosed() throws SQLException {
        return connection.isClosed();
    }

    /**
     * Returns array of the parameters for the subsequent creating prepared statement.
     *
     * @param object Entity class whose data fields are used to persist array of the parameters.
     * @return Array of the parameters.
     * @throws ReflectiveOperationException If there's any problem which has connection with Reflection API.
     * */
    @SuppressWarnings("unchecked")
    private Object[] getParameters(Object object) throws ReflectiveOperationException {
        MappedClassInformation classInformation = getMappedClassInformation(object.getClass());
        List<Method> getMethods = classInformation.getGetMethods();

        return retrieveParameters(object, getMethods);
    }

    /**
     * Returns an array which contains a values of the class's instance fields.
     *
     * @return Array which contains a values of the class's instance fields.
     * @param object Instance of a particular class from which the parameters will be extracted.
     * @param getters Array which contains all getters of a particular class.
     * @throws ReflectiveOperationException If there's any problem which has connection with Reflection API.
     * */
    private Object[] retrieveParameters(Object object, List<Method> getters) throws ReflectiveOperationException {
        Object[] parameters = new Object[getters.size()];

        for (int i = 0; i < getters.size(); ++i) {
            parameters[i] = getters.get(i).invoke(object);
        }

        return parameters;
    }

    /**
     * Builds prepared statement. This method takes the string representation of SQL query and the variable
     * number of wildcard parameters.
     * This method substitutes every wildcard symbol in the SQL query on the corresponding wildcard
     * parameter that was placed like the second and subsequent argument after SQL query.
     *
     * Here's how it works:
     * In the example below, you could see the two wildcard symbols and two wildcard parameters.
     *
     * <code>
     *     createPreparedStatement("SELECT * FROM person WHERE id = ? AND name = ?", 1, "John");
     * </code>
     *
     * The query above will be converted to the following representation:
     * <code>
     *     "SELECT * FROM person WHERE id = 1 AND name = 'John'"
     * </code>
     *
     * @param query SQL query that can keep wildcard symbols.
     * @param parameters Arbitrary number of parameters that will be used
     *                   to substitute the wildcard symbols in the SQL query.
     * @return object of the PreparedStatement class.
     * @throws SQLException If any database access problems happened.
     * */
    private PreparedStatement createPreparedStatement(String query, Object... parameters) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(query);

        for (int parameterIndex = 0; parameterIndex < parameters.length; ++parameterIndex) {
            preparedStatement.setObject(parameterIndex + 1, parameters[parameterIndex]);
        }

        return preparedStatement;
    }

    /**
     * Closes result set object
     *
     * @throws SQLException If any database access problems happened.
     * */
    private void close(ResultSet resultSet) throws SQLException {
        if (resultSet != null) {
            resultSet.close();
        }
    }

    /**
     * Closes prepared statement object
     *
     * @throws SQLException If any database access problems happened.
     * */
    private void close(Statement statement) throws SQLException {
        if (statement != null) {
            statement.close();
        }
    }

    /**
     * Returns instance of the MappedClassInformation class from the cache.
     * If there's no corresponding instance of the MappedClassInformation class,
     * then a new one will be created and putted to the cache.
     *
     * @param mappedClass A Class, whose annotated fields will be used for creating corresponding entity.
     * @return Instance of the MappedClassInformation class that describes
     *         all information about a particular class (methods, constructors etc.).
     * @throws NoSuchMethodException If there's no method to invoke.
     * */
    @SuppressWarnings("unchecked")
    private MappedClassInformation getMappedClassInformation(Class mappedClass) throws NoSuchMethodException{
        if (cache.containsKey(mappedClass)) {
            return cache.get(mappedClass);
        }

        MappedClassInformation classInformation = new MappedClassInformation(mappedClass, queryCreator);
        cache.put(mappedClass, classInformation);

        return classInformation;
    }

    /**
     * Returns field's value of the particular object that was annotated by {@literal Id}
     *
     * @param object Entity class which method is used to be invoked.
     * @throws ReflectiveOperationException If there's any problem which has connection with Reflection API.
     * */
    private Object[] getPrimaryKeyMethodValues(Object object) throws ReflectiveOperationException{
        MappedClassInformation classInformation = getMappedClassInformation(object.getClass());
        List<Method> primaryKeysMethods = classInformation.getPrimaryKeysMethods();
        List<Object> primaryKeysValues = new ArrayList<>(primaryKeysMethods.size());

        for (Method method : primaryKeysMethods) {
            primaryKeysValues.add(method.invoke(object));
        }

        return primaryKeysValues.toArray();
    }

    /**
     * Registers JDBC driver and trying to connect to the database.
     *
     * @param connectionInformation Instance of the DatabaseInformation class that keeps all the information
     *                            about database connection like database driver's name, url, username and password.
     * @return A new Connection object associated with particular database.
     * @throws SQLException If any database access problems happened.
     * @throws ClassNotFoundException If there's any problem with finding a JDBC driver class.
     * */
    private Connection getConnection(ConnectionInformation connectionInformation) throws SQLException, ClassNotFoundException {
        String url = connectionInformation.getUrl();
        String username = connectionInformation.getUsername();
        String password = connectionInformation.getPassword();

        Class.forName(connectionInformation.getDriver());
        connection = DriverManager.getConnection(url, username, password);

        return connection;
    }

    /**
     * Gets the data from the result set and tries to build an entity object.
     * The entity class must have a constructor with corresponding number
     * of parameters that have an appropriate types according to data from the result set.
     *
     * @param resultSet Result set that was retrieved from the database.
     * @param mappedClass Class, whose annotated fields will be used for creating corresponding entity.
     * @return Instance of the class that has been pointed as a second parameter.
     * @throws SQLException If any database access problems happened.
     * @throws ReflectiveOperationException If there's any problem which has connection with Reflection API.
     * */
     @SuppressWarnings("unchecked")
     private <T> T createEntity(ResultSet resultSet, Class<T> mappedClass) throws SQLException, ReflectiveOperationException {
        return (T) createEntitySupporter(resultSet, getMappedClassInformation(mappedClass));
     }

    /**
     * Gets the information from the result set that has been pointed
     * as a first argument and trying to build instance of the particular class.
     * Information about the class holds in the MappedClassInformation instance
     * that has been pointed as a second argument.
     * This method invokes all set methods of the mapped class for setting fields
     * information that has been retrieved from the result set.
     *
     * @param resultSet Result set that was retrieved from the database.
     * @param classInformation Instance of MappedClassInformation that holds all information about mapped class.
     * @return Instance of the class that has been pointed as a second parameter.
     * @throws SQLException If any database access problems happened.
     * @throws ReflectiveOperationException If there's any problem which has connection with Reflection API.
     * */
     private <T> T createEntitySupporter(ResultSet resultSet,
                                         MappedClassInformation<T> classInformation) throws SQLException,
                                                                                            ReflectiveOperationException {
        List<Method> setMethods = classInformation.getSetMethods();
        List<String> columns = classInformation.getColumns();
        T result = classInformation.getConstructor().newInstance();

        for (int i = 0; i < columns.size(); ++i) {
            Object data = resultSet.getObject(columns.get(i));
            setMethods.get(i).invoke(result, data);
        }

        return result;
     }
}