package org.narwhal.pool;

import org.junit.Assert;
import org.junit.Test;
import org.narwhal.core.ConnectionInformation;
import org.narwhal.core.DatabaseConnection;

public class ConnectionPoolTest {

    @Test
    public void setSizeTest() {
        final String driver = "org.h2.Driver";
        final String url = "jdbc:h2:mem:test";
        final String username = "user";
        final String password = "password";
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
