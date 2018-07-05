package dbpedia.api.versioning;

import dbpedia.api.model.AbstractSemanticVersion;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A set of versions. Use the SemanticVersionSet.Builder to build it.
 */
public class SemanticVersionSet<V extends AbstractSemanticVersion> {

  /**
   * Ascending-ordered List of all versions
   */
  private List<V> versions = new ArrayList<>();

  /**
   * maps the versions to its semantic version names this map is used for a quick lookup of the
   * versions
   */
  private Map<String, V> lookupMap = new HashMap<>();

  private SemanticVersionSet() {
  } //used by the Builder

  /**
   * returns the most recent version (highest major, major and patch)
   */
  public V getLatest() {
    return lastItemOfList(versions);
  }

  /**
   * Creates an iterable which iterates all versions from an old version to the latest The iterator
   * increases first the patch version and then the minor version The iterator will also increase
   * the major versions, so the version returned by next() must not necessarily be compatible to the
   * old version
   */
  public Iterable<V> iterateToLatest(AbstractSemanticVersion oldVersion) {
    Iterator<V> listIterator = versions.iterator();

    //forward to oldVerison
    while (listIterator.hasNext()) {
      if (listIterator.next().compareTo(oldVersion) == 0) {
        break;
      }
    }
    return () -> listIterator;
  }

  /**
   * gets a version by its version number
   *
   * @throws VersionNotFoundException If the version is not found
   */
  public V get(int major, int minor, int patch) throws VersionNotFoundException {
    V result = lookupMap
        .get(AbstractSemanticVersion.createSemanticVersioningString(major, minor, patch));
    if (result == null) {
      throw new VersionNotFoundException();
    }
    return result;
  }

  /**
   * Checks if a version number exists in this set
   */
  public boolean contains(int major, int minor, int patch) {
    String versionName = AbstractSemanticVersion
        .createSemanticVersioningString(major, minor, patch);
    return lookupMap.containsKey(versionName);
  }

  /**
   * @return the number of all versions in this set
   */
  public int size() {
    return versions.size();
  }


  public static class Builder<V extends AbstractSemanticVersion> {

    SemanticVersionSet<V> instance = new SemanticVersionSet<>();

    public Builder<V> add(V version) {
      instance.versions.add(version);
      instance.lookupMap.put(version.getSemanticVersioningString(), version);
      return this;
    }

    /**
     * Sorts the version lists by their version number and returns the instance
     */
    public SemanticVersionSet<V> build() {
      Collections.sort(instance.versions);
      return instance;
    }

  }

  /**
   * Utility method that returns the last item of a list (or null if the list is null or has size
   * 0)
   */
  private static <T> T lastItemOfList(List<T> list) {
    if (list == null || list.size() == 0) {
      return null;
    }
    return list.get(list.size() - 1);
  }

  public List<V> getAsList() {
    return Collections.unmodifiableList(this.versions);
  }
}
