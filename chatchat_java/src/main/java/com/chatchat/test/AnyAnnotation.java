
package com.chatchat.test;


import java.lang.annotation.*;

// AnyAnnotation.java
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.PACKAGE, ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.FIELD,
        ElementType.LOCAL_VARIABLE, ElementType.PARAMETER})
public @interface AnyAnnotation {
    int order() default 0;
    String desc() default "";
}

