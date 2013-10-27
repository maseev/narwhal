package org.narwhal.pool.mysql;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.narwhal.core.DatabaseConnection;
import org.narwhal.core.DatabaseInformation;
import org.narwhal.pool.ConnectionPool;
import org.narwhal.query.MysqlQueryCreator;


/**
 * @author Miron Aseev
 */
@RunWith(JUnit4.class)
public class ConnectionPoolTest {


    @Test
    public void setSizeTest() {
        final String driver   = "com.mysql.jdbc.Driver";
        final String url      = "jdbc:mysql://localhost/person";
        final String username = "person";
        final String password = "person";
        DatabaseInformation databaseInformation = new DatabaseInformation(driver, url, username, password);
        ConnectionPool pool;
        int expectedPoolSize = 10;
        int result = 0;

        DatabaseConnection.clearCache();

        try {
            pool = new ConnectionPool(databaseInformation, 1, 1, new MysqlQueryCreator());

            try {
                pool.setSize(expectedPoolSize);
                result = pool.getSize();
            } finally {
                pool.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Assert.assertEquals(expectedPoolSize, result);
    }
}
