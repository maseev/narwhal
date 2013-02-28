package org.narwhal.bean;

import org.narwhal.annotation.Column;
import org.narwhal.annotation.Table;

/**
 * @author Miron Aseev
 */
@Table("PERSON")
public class Person {

    @Column(value = "ID", primaryKey = true)
    private int id;
    @Column("NAME")
    private String name;


    public Person() {
    }

    public Person(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
