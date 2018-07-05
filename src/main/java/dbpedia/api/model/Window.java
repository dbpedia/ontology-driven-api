package dbpedia.api.model;


public class Window {

  private int offset;
  private int limit;

  public Window(int offset, int limit) {
    if (checkIntegerRange(offset, limit)) {
      this.offset = offset;
      this.limit = limit;
    } else {
      this.offset = 0;
      this.limit = 0;
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Window)) {
      return false;
    }
    Window other = (Window) obj;
    return other.offset == this.offset && other.limit == this.limit;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  public int getOffset() {
    return offset;
  }

  public int getLimit() {
    return limit;
  }

  /**
   * check if offset and limit are greater than 0
   *
   * @return boolean
   */
  private boolean checkIntegerRange(int offset, int limit) {
    if (offset >= 0 && limit >= 0) {
      return true;
    } else {
      return false;
    }
  }
}
