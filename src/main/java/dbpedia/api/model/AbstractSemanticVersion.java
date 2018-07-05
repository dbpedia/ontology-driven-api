package dbpedia.api.model;

public abstract class AbstractSemanticVersion implements Comparable<AbstractSemanticVersion> {

  private final int major;
  private final int minor;
  private final int patch;
  protected final String semanticVersioningString;

  public AbstractSemanticVersion(int major, int minor, int patch) {
    this.minor = minor;
    this.major = major;
    this.patch = patch;
    this.semanticVersioningString = createSemanticVersioningString(major, minor, patch);
  }

  /**
   * @return "[major].[minor].[patch]"
   */
  public String getSementicVersioningString() {
    return this.semanticVersioningString;
  }

  @Override
  public int compareTo(AbstractSemanticVersion other) {
    if (this.major != other.major) {
      return this.major - other.major;
    }
    if (this.minor != other.minor) {
      return this.minor - other.minor;
    }
    return this.patch - other.patch;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o == null) {
      return false;
    }
    if (!(o instanceof AbstractSemanticVersion)) {
      return false;
    }
    AbstractSemanticVersion other = (AbstractSemanticVersion) o;
    return other.major == this.major
        && other.minor == this.minor
        && other.patch == this.patch;
  }

  public int getMajor() {
    return major;
  }

  public int getMinor() {
    return minor;
  }

  public int getPatch() {
    return patch;
  }

  public String getSemanticVersioningString() {
    return semanticVersioningString;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  public static String createSemanticVersioningString(int major, int minor, int patch) {
    return major + "." + minor + "." + patch;
  }
}
