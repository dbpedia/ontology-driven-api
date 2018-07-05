import java.util.Arrays;
import java.util.HashSet;

public class TestUtils {

  public static <T> HashSet<T> setOf(T... items) {
    return new HashSet<>(Arrays.asList(items));
  }
}
