package io.RPGCraft.FableCraft.Utils.Gemstone;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GemstoneAnnotation{
    /**
     * The display name of the gemstone.
     *
     * @return the display name of the gemstone
     */
     String name() default "Red Gemstone";

  /**
   * The type of the gemstone.
   *
   * @return the type of the gemstone
   */
    GemstoneType type() default GemstoneType.RED;
}
