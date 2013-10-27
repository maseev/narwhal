package org.narwhal.bean;

import org.narwhal.annotation.Column;
import org.narwhal.annotation.Table;

import java.sql.Date;


/**
 * @author Miron Aseev
 */
@Table
public class Person {

    @Column(primaryKey = true)
    private Integer id;
    private String name;
    private Date birthday;


    public Person() {
    }

    public Person(Integer id, String name, Date birthday) {
        this.id = id;
        this.name = name;
        this.birthday = birthday;
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
}
