package org.narwhal.core;

/**
 * The <code>DatabaseInformation</code> class is a simple POJO class
 * which contains the information that is used to make connection to the databases.
 * 
 * @author Miron Aseev
 */
public class ConnectionInformation {

    private String driver;

    private String url;

    private String username;

    private String password;

    /**
     * Initializes a new DatabaseInformation instance.
     * 
     * @param driver Database driver name.
     * @param url URL that describes a path to the particular database.
     * @param username Database username. 
     * @param password Database password.
     * */
    public ConnectionInformation(String driver, String url, String username, String password) {
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    /**
     * Returns the database driver name which is used for loading JDBC driver.
     *
     * @return Database driver name
     * */
    public String getDriver() {
        return driver;
    }

    /**
     * Returns the database url which is used for connecting to the particular database.
     *
     * @return URL which describes a path to the particular database.
     * */
    public String getUrl() {
        return url;
    }

    /**
     * Returns a string representation of database username.
     *
     * @return String representation of database username.
     * */
    public String getUsername() {
        return username;
    }

    /**
     * Returns password.
     *
     * @return String representation of database password.
     * */
    public String getPassword() {
        return password;
    }
}
