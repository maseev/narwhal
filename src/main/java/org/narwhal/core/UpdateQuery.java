package org.narwhal.core;

public interface UpdateQuery {

    void perform(DatabaseConnection connection) throws Exception;
}
