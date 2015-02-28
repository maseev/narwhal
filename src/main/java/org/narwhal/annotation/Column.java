package org.narwhal.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The <code>Column</code> annotation is used to mark the particular
 * field of the class which maps on table's column.
 *
 * @author Miron Aseev
 */
@Target(value = ElementType.FIELD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Column {

    /**
     * Returns the database column name.
     *
     * @return Database column name.
     * */
    String value() default "";
}
