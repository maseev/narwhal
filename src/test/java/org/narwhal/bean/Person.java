package org.narwhal.bean;

import org.narwhal.annotation.Column;
import org.narwhal.annotation.Table;

import java.util.Date;


/**
 * @author Miron Aseev
 */
@Table("PERSON")
public class Person {

    @Column(value = "ID", primaryKey = true)
    private Integer id;
    @Column("NAME")
    private String name;
    @Column("BIRTHDAY")
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
