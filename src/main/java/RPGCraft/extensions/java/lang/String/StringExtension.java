package RPGCraft.extensions.java.lang.String;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import java.lang.String;

@Extension
public class StringExtension {
  public static void helloWorld(@This String thiz) {
    System.out.println("hello world!");
  }

  public static boolean isNullOrEmpty(@This String t) {return t.isEmpty() || t == null;}
}
