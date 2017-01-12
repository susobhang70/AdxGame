package adx.util;

/**
 * A simple logging class.
 */
public class Logging {

  public final static boolean LOGGING = true;

  public static void log(String message) {
    if (LOGGING) {
      System.out.println(message);
    }
  }

  public static void log(Object o) {
    if (LOGGING) {
      System.out.println(o);
    }
  }

}
