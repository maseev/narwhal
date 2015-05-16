package org.narwhal.core;

public interface UpdateQuery {

    int perform(DatabaseConnection connection) throws Exception;
}
