package com.freedom.messagebus.server.daemon;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DaemonService {
    String value() default "";

    RunPolicy policy() default RunPolicy.ONCE;
}
