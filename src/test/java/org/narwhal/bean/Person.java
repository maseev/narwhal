package org.narwhal.bean;

import org.narwhal.annotation.Column;
import org.narwhal.annotation.Entity;
import org.narwhal.annotation.Id;

import java.sql.Date;

/**
 * @author Miron Aseev
 */
@Entity
public class Person {

    @Id
    @Column
    private Integer id;

    private String name;

    private Date birthday;

    @Column("EMPLOYED")
    private boolean employed;

    public Person() {
    }

    public Person(Integer id, String name, Date birthday, boolean employed) {
        this.id = id;
        this.name = name;
        this.birthday = birthday;
        this.employed = employed;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

  public boolean isEmployed() {
    return employed;
  }

  public void setEmployed(boolean employed) {
    this.employed = employed;
  }
}
