package org.narwhal.util;

import org.narwhal.annotation.Column;
import org.narwhal.annotation.Entity;
import org.narwhal.annotation.Id;
import org.narwhal.query.QueryCreator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * The <code>MappedClassInformation</code> class keeps all the information about particular class.
 * Instance of this class holds data about set methods, constructors and columns name of the database
 * tables that mapped to the fields of class.
 * */
public class MappedClassInformation<T> {

    private Constructor<T> constructor;

    private List<Method> setMethods;

    private List<Method> getMethods;

    private List<String> columns;

    private List<Method> primaryKeysMethods;

    private Map<QueryType, String> queries;

    private List<String> primaryKeys;

    /**
     * Initializes a new instance of the MappedClassInformation class.
     * Instance is specified by the value of the Class<T>.
     * This constructor tries to retrieve all the necessary information about the class.
     *
     * @param mappedClass A Class, which is used for retrieving information about constructors, set methods etc.
     * @throws NoSuchMethodException If there is no appropriate method to invoke
     * */
    public MappedClassInformation(Class<T> mappedClass, QueryCreator queryCreator) throws NoSuchMethodException {
        List<Field> fields = Arrays.asList(mappedClass.getDeclaredFields());
        primaryKeysMethods = getPrimaryKeyMethods(mappedClass, fields);
        primaryKeys = getPrimaryKeys(mappedClass, fields);
        constructor = mappedClass.getConstructor();
        setMethods  = setters(mappedClass, fields);
        getMethods  = getters(mappedClass, fields);
        columns     = getColumns(fields);
        queries     = createQueries(mappedClass, queryCreator);
    }

    /**
     * Returns list of the set methods for corresponding fields of the class.
     *
     * @return List of set methods.
     * */
    public List<Method> getSetMethods() {
        return setMethods;
    }

    /**
     * Returns list of the get methods for corresponding fields of the class.
     *
     * @return List of get methods.
     * */
    public List<Method> getGetMethods() {
        return getMethods;
    }

    /**
     * Returns getter method for class field that annotated by Column annotation with primaryKey = true.
     *
     * @return Getter method for field of the class that maps to the primary key.
     * */
    public List<Method> getPrimaryKeysMethods() {
        return primaryKeysMethods;
    }

    /**
     * Returns string representation of the SQL query by the QueryType object.
     *
     * @return String representation of the SQL query.
     * */
    public String getQuery(QueryType queryType) {
        return queries.get(queryType);
    }

    /**
     * Returns list of the columns that have been retrieved from the annotated fields.
     *
     * @return Columns of the database table.
     * */
    public List<String> getColumns() {
        return columns;
    }

    /**
     * Returns a default constructor of a class.
     *
     * @return Default constructor of a class.
     * */
    public Constructor<T> getConstructor() {
        return constructor;
    }

    /**
     * Retrieves columns name of the database table from the annotated fields.
     *
     * @param fields Fields of class that have been annotated by {@literal @}Column annotation.
     * @return Columns of the database table.
     * */
    private List<String> getColumns(List<Field> fields) {
        List<String> columns = new ArrayList<>(fields.size());

        for (Field field : fields) {
            if (field.isAnnotationPresent(Column.class) && !field.getAnnotation(Column.class).value().isEmpty()) {
                columns.add(field.getAnnotation(Column.class).value());
            } else {
                columns.add(field.getName());
            }
        }

        return columns;
    }

    /**
     * Retrieves primary key name of the database table from the annotated fields.
     *
     * @param mappedClass A class, which is used for creating appropriate exception message.
     * @param fields Fields of class that have been annotated by {@literal @}Column annotation.
     * @throws IllegalArgumentException if field of the class wasn't annotated by the {@literal @}Column annotation
     *         with primaryKey = true.
     * */
    private List<String> getPrimaryKeys(Class<T> mappedClass, List<Field> fields) {
        List<String> primaryKeys = new ArrayList<>();

        for (Field field : fields) {
            if (field.isAnnotationPresent(Id.class)) {
                primaryKeys.add(field.getName());
            }
        }

        if (primaryKeys.isEmpty()) {
            String msg = "Class %s doesn't have a field that was annotated by %s annotation with primaryKey = true";
            throw new IllegalArgumentException(String.format(msg, mappedClass, Column.class));
        }

        return primaryKeys;
    }

    /**
     * Retrieves string representation of the table name that maps to the particular entity.
     *
     * @param mappedClass A class that is used for checking whether
     *                    class was annotated by a particular annotation or not.
     * @return string representation of the table name.
     * @throws IllegalArgumentException If class wasn't annotated by the Table annotation.
     * */
    private String getTableName(Class<T> mappedClass) {
        if (mappedClass.isAnnotationPresent(Entity.class)) {
            if (mappedClass.getAnnotation(Entity.class).value().isEmpty()) {
                String className = mappedClass.getName();
                int index = className.lastIndexOf('.') + 1;

                if (index < className.length()) {
                    return className.substring(index);
                } else {
                    return className;
                }
            } else {
                return mappedClass.getAnnotation(Entity.class).value();
            }
        }

        String msg = "Class %s wasn't annotated by %s annotation";
        throw new IllegalArgumentException(String.format(msg, mappedClass.toString(), Entity.class));
    }

    /**
     * Constructs string representation method by using field name and a prefix (get, set).
     *
     * @param prefix Prefix that uses to persist whether getter or setter.
     * @return String representation of the class method.
     * */
    private String getMethodName(Field field, String prefix) {
        char[] fieldNameArray = field.getName().toCharArray();
        fieldNameArray[0] = Character.toUpperCase(fieldNameArray[0]);

        return prefix + new String(fieldNameArray);
    }

    /**
     * Creates and returns name of the set method from string representation of the field.
     *
     * @return String representation of the set method.
     * */
    private String setter(Field field) {
        return getMethodName(field, "set");
    }

    /**
     * Creates and returns name of the get method from string representation of the field.
     *
     * @return String representation of the set method.
     * */
    private String getter(Field field) {
        Class type = field.getType();

        if (type.equals(boolean.class) || type.equals(Boolean.class)) {
            return getMethodName(field, "is");
        } else {
            return getMethodName(field, "get");
        }
    }

    /**
     * Returns all set methods from the class. This method uses list of fields
     * which is used for retrieving set methods from the class.
     *
     * @param mappedClass A Class, which is used for retrieving information about constructors, set methods etc.
     * @param fields Fields of the class.
     * @return Set methods of the class.
     * @throws NoSuchMethodException If there is no appropriate method to invoke.
     * */
    private List<Method> setters(Class<T> mappedClass, List<Field> fields) throws NoSuchMethodException {
        List<Method> methods = new ArrayList<>(fields.size());

        for (Field field : fields) {
            methods.add(mappedClass.getMethod(setter(field), field.getType()));
        }

        return methods;
    }

    /**
     * Returns all get methods from the class. This method uses list of fields
     * which is used for retrieving set methods from the class.
     *
     * @param mappedClass A Class, which is used for retrieving information about constructors, set methods etc.
     * @param fields Fields of the class.
     * @return Set methods of the class.
     * @throws NoSuchMethodException If there is no appropriate method to invoke.
     * */
    private List<Method> getters(Class<T> mappedClass, List<Field> fields) throws NoSuchMethodException {
        List<Method> methods = new ArrayList<>(fields.size());

        for (Field field : fields) {
            methods.add(mappedClass.getMethod(getter(field)));
        }

        return methods;
    }

    /**
     * Retrieves getter method for the class' field that was annotated by the
     * {@literal @}Column annotation with primaryKey = true.
     *
     * @param mappedClass A Class, which is used for retrieving information about constructors, set methods etc.
     * @param fields Fields of the class.
     * @return Getter method for the field that maps to the primary key.
     * @throws NoSuchMethodException If there is no appropriate method to invoke.
     * */
    private List<Method> getPrimaryKeyMethods(Class<T> mappedClass, List<Field> fields) throws NoSuchMethodException {
        List<Method> primaryKeyMethods = new ArrayList<>();

        for (Field field : fields) {
            if (field.isAnnotationPresent(Id.class)) {
                primaryKeyMethods.add(mappedClass.getMethod(getter(field)));
            }
        }

        if (primaryKeyMethods.isEmpty()) {
            String msg = "Class %s doesn't have a field that was annotated by %s annotation with primaryKey = true";
            throw new IllegalArgumentException(String.format(msg, mappedClass, Id.class));
        }

        return primaryKeyMethods;
    }

    /**
     * Return maps of the QueryType and string representation of the SQL query pairs.
     *
     * @param mappedClass A Class, which is used for retrieving information about constructors, set methods etc.
     * @return Map of QueryType and SQL query pairs.
     * */
    private Map<QueryType, String> createQueries(Class<T> mappedClass, QueryCreator creator) {
        String tableName = getTableName(mappedClass);
        Map<QueryType, String> queries = new HashMap<>();

        queries.put(QueryType.CREATE, creator.buildInsertQuery(tableName, columns));
        queries.put(QueryType.READ,   creator.buildSelectQuery(tableName, columns, primaryKeys));
        queries.put(QueryType.UPDATE, creator.buildUpdateQuery(tableName, columns, primaryKeys));
        queries.put(QueryType.DELETE, creator.buildDeleteQuery(tableName, primaryKeys));

        return queries;
    }
}
