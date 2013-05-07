package org.narwhal.pool;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.narwhal.core.DatabaseInformation;

import java.sql.SQLException;

/**
 * @author Miron Aseev
 */
@RunWith(JUnit4.class)
public class ConnectionPoolTest {


    @Test
    public void setSizeTest() {
        final String driver   = "com.mysql.jdbc.Driver";
        final String url      = "jdbc:mysql://localhost/bank";
        final String username = "lrngsql";
        final String password = "lrngsql";
        DatabaseInformation databaseInformation = new DatabaseInformation(driver, url, username, password);
        ConnectionPool pool;
        int expectedPoolSize = 10;
        int result = 0;

        try {
            pool = new ConnectionPool(databaseInformation, 1, 1);

            try {
                pool.setSize(expectedPoolSize);
                result = pool.getSize();
            } finally {
                pool.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        Assert.assertEquals(expectedPoolSize, result);
    }
}
