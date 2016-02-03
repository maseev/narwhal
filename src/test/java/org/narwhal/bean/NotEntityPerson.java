package org.narwhal.bean;

import org.narwhal.annotation.Column;
import org.narwhal.annotation.Id;

public class NotEntityPerson {

  @Id
  @Column
  private String id;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
}
