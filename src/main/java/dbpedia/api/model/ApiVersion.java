package dbpedia.api.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a version of the API each version contains a replacement map for resources (e.g.
 * dbo:numOfEmployees -> dbo:numberOfEmployees) and a replacement maps for prefixes (e.g. dbpr ->
 * dbr) for when a prefix permanently changes its name
 */
public class ApiVersion extends AbstractSemanticVersion {

  /**
   * Contains the resource replacements which occurred from the last version (=key) to this
   * version(=value)+ e.g. dbo:numOfEmployees -> dbo:numberOfEmployees
   */
  private Map<ResourceModel, ResourceModel> resourceReplacements;

  /**
   * Contains prefix replacements which apply to all resources, e.g. dbpr -> dbr
   */
  private Map<String, String> prefixReplacements;

  public ApiVersion(int major, int minor, int patch,
      Map<ResourceModel, ResourceModel> replacementMap, Map<String, String> prefixReplacements) {
    super(major, minor, patch);
    if (replacementMap == null || prefixReplacements == null) {
      throw new IllegalArgumentException("Replacement Map must not be null");
    }
    this.resourceReplacements = new HashMap<>(replacementMap);
    this.prefixReplacements = new HashMap<>(prefixReplacements);
  }

  /**
   * Checks if all attributes are equal
   */
  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (o == this) {
      return true;
    }
    if (!(o instanceof ApiVersion)) {
      return false;
    }
    ApiVersion other = (ApiVersion) o;
    return super.equals(o) && other.resourceReplacements.equals(this.resourceReplacements);
  }

  @Override
  public int hashCode() {
    int hash = 1;
    hash *= resourceReplacements.hashCode();
    hash *= prefixReplacements.hashCode();
    return hash;
  }

  @Override
  public String toString() {
    return "Version " + semanticVersioningString;
  }

  /**
   * @return unmodifiable map of the resource replacements
   */
  public Map<ResourceModel, ResourceModel> getResourceReplacements() {
    return Collections.unmodifiableMap(resourceReplacements);
  }

  /**
   * @return unmodifiable map of the prefix replacements
   */
  public Map<String, String> getPrefixReplacements() {
    return Collections.unmodifiableMap(prefixReplacements);
  }

}
