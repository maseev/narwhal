package org.narwhal.pool;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.narwhal.core.ConnectionInformation;
import org.narwhal.core.DatabaseConnection;
import org.narwhal.pool.ConnectionPool;

/**
 * @author Miron Aseev
 */
@RunWith(JUnit4.class)
public class ConnectionPoolTest {


    @Test
    public void setSizeTest() {
        final String driver   = "org.postgresql.Driver";
        final String url      = "jdbc:postgresql://localhost/test";
        final String username = "postgres";
        final String password = "admin";
        ConnectionInformation connectionInformation = new ConnectionInformation(driver, url, username, password);
        ConnectionPool pool;
        int expectedPoolSize = 10;
        int result = 0;

        DatabaseConnection.clearCache();

        try {
            pool = new ConnectionPool(connectionInformation, 1, 1);

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
