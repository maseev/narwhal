package org.narwhal.core;

public interface Query<T> {

  T perform(DatabaseConnection connection) throws Exception;
}
