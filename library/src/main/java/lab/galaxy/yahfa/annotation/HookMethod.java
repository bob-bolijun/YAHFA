package lab.galaxy.yahfa.annotation;


import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HookMethod {
    String name() default ""; //for construct function
    String signature();
}
