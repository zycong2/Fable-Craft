package io.RPGCraft.FableCraft.commands.DONOTTOUCH;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface command {
  String name();
  String[] args() default  {};
  String[] aliases() default {};
  String permission() default "";
  String description() default "A Command";
  boolean playerOnly() default false;
  int arguments() default 0;
}

