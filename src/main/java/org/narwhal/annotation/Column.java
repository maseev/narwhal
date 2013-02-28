package org.narwhal.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * The <code>Column</code> annotation marks particular
 * field of the class which maps on the column of the database table.
 *
 * @author Miron Aseev
 */
@Target(value = ElementType.FIELD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Column {

    /**
     * Returns database column name.
     *
     * @return Database column name.
     * */
    String value();

    boolean primaryKey() default false;
}
