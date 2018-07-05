package dbpedia.api.versioning;

import dbpedia.api.model.ApiVersion;
import dbpedia.api.model.EntityRequestModel;
import dbpedia.api.model.Filter;
import dbpedia.api.model.RequestModel;
import dbpedia.api.model.ResourceModel;
import dbpedia.api.model.ValueRequestModel;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Component;


@Component
public class VersionHandler {

  /**
   * Contains all versions
   */
  private SemanticVersionSet<ApiVersion> versions;

  public VersionHandler(SemanticVersionSet<ApiVersion> versions) {
    this.versions = versions;
  }

  /**
   * Checks if it is possible to patch an request to the current version of the api according to
   * semantic versioning
   *
   * @return true if the major version of the request equals the current major, otherwise false
   */
  public boolean canPatchToLatest(RequestModel model) throws VersionNotFoundException {
    ApiVersion version = getVersionByModel(model);
    return version.getMajor() == versions.getLatest().getMajor();
  }

  /**
   * Patches a ValueRequestModel by applying the replacements of all version up to the latest
   *
   * @param oldModel The old request (contains the old version number)
   * @return The updated request
   * @throws VersionNotFoundException When the version is not found
   */
  public ValueRequestModel patchValueRequestModel(ValueRequestModel oldModel)
      throws VersionNotFoundException {

    ApiVersion version = getVersionByModel(oldModel);
    //make a copy of the property list where the replacements will be done
    Set<ResourceModel> updatedProperties = new HashSet<>(oldModel.getProperties());
    Set<String> updatedEntities = new HashSet<>(oldModel.getEntities());
    for (ApiVersion v : versions.iterateToLatest(version)) {
      replaceResourceSet(updatedProperties, v);
      replaceEntitiesSet(updatedEntities, v);
    }

    return oldModel.toBuilder()
        .setProperties(updatedProperties)
        .setVersion(versions.getLatest().getSementicVersioningString())
        .setEntities(updatedEntities)
        .build();
  }

  /**
   * checks if the api version of a request is the current version of the API
   *
   * @return true or false
   */
  public boolean isLatest(RequestModel model) throws VersionNotFoundException {
    ApiVersion modelVersion = getVersionByModel(model);
    return versions.getLatest().equals(modelVersion);
  }

  /**
   * patches an entity request model to the current version
   */
  public EntityRequestModel patchEntityRequestModel(EntityRequestModel oldModel)
      throws VersionNotFoundException {
    ApiVersion version = getVersionByModel(oldModel);
    //make a copy of the property list where the replacements will be done
    Set<Filter> updatedFilters = new HashSet<>(oldModel.getFilterList());
    String updatedClassName = oldModel.getClassName();
    for (ApiVersion v : versions.iterateToLatest(version)) {
      updateFilterSet(updatedFilters, v);
      updatedClassName = updateClassName(updatedClassName, v);
    }
    return oldModel.toBuilder()
        .setVersion(versions.getLatest().getSementicVersioningString())
        .setFilterList(updatedFilters)
        .setClassname(updatedClassName)
        .build();
  }

  private static void updateFilterSet(Set<Filter> filterSet, ApiVersion version) {
    Set<Filter> toRemove = new HashSet<>();
    Set<Filter> toAdd = new HashSet<>();
    for (Filter filter : filterSet) {
      Filter updatedFilter = updateFilter(filter, version);
      if (updatedFilter != null) {
        toRemove.add(filter);
        toAdd.add(updatedFilter);
      }
    }
    filterSet.removeAll(toRemove);
    filterSet.addAll(toAdd);
  }

  /**
   * Replaces the property and value resources according to the replacement map
   *
   * @return null if no replacements were made (so there is no need to check this with equals() )
   */
  private static Filter updateFilter(Filter filter, ApiVersion version) {
    ResourceModel updatedPropertyOrNull = null;
    ResourceModel updatedValOrNull = null;
    if (filter.isValueAResource()) {
      updatedValOrNull = updateResourceModel(filter.getValueAsResource(), version);
    }
    if (filter.getFilterProp() != null) {
      updatedPropertyOrNull = updateResourceModel(filter.getFilterProp(), version);
    }
    if (updatedPropertyOrNull == null && updatedValOrNull == null) {
      return null;
    }
    Filter.Builder builder = filter.toBuilder();
    if (updatedPropertyOrNull != null) {
      builder.setFilterProps(updatedPropertyOrNull);
    }
    if (updatedValOrNull != null) {
      builder.setResourceValue(updatedValOrNull);
    }
    return builder.build();
  }

  /**
   * Creates the updated version of a resource model according to the replacements of an ApiVersion
   *
   * @return null if the ResourceModel is not modified. So a check with equals() is not needed.
   */
  private static ResourceModel updateResourceModel(ResourceModel old, ApiVersion version) {
    //if there is a direct replacement for the old resource model, return the updated version from the map
    if (version.getResourceReplacements().containsKey(old)) {
      return version.getResourceReplacements().get(old);
    }
    // otherwise check if the prefix has to get replaced
    if (version.getPrefixReplacements().containsKey(old.getPrefix())) {
      String newPrefix = version.getPrefixReplacements().get(old.getPrefix());
      return new ResourceModel(newPrefix, old.getIdentifier());
    }
    //otherwise return null (= no changes are made)
    //so the calling method does not need to check this with equals()
    return null;
  }

  private static String updateClassName(String oldClassName, ApiVersion version) {
    ResourceModel classAsDboResource = new ResourceModel("dbo", oldClassName);
    ResourceModel updatedModel = updateResourceModel(classAsDboResource, version);
    // (maybe in a future release)
    if (updatedModel != null) {
      return updatedModel.getIdentifier();
    }
    return oldClassName;
  }

  /**
   * Replaces all resources in a set according to the replacements of a version
   */
  private static void replaceResourceSet(Set<ResourceModel> set,
      ApiVersion version) {
    // apply changes at the end of each version iteration to avoid conflicts
    // e.g. for a replacementMap like: A --replacedBy--> B and B --replacedBy--> A
    Set<ResourceModel> toRemove = new HashSet<>();
    Set<ResourceModel> toAdd = new HashSet<>();
    for (ResourceModel model : set) {
      ResourceModel updatedOrNull = updateResourceModel(model, version);
      if (updatedOrNull != null) {
        toAdd.add(updatedOrNull);
        toRemove.add(model);
      }
    }
    set.removeAll(toRemove);
    set.addAll(toAdd);
  }

  /**
   * Replaces all entity Strings in a version according to the replacements of a version The
   * entities are handled as resources with the prefix dbr
   */
  private static void replaceEntitiesSet(Set<String> set, ApiVersion version) {
    Set<String> toRemove = new HashSet<>();
    Set<String> toAdd = new HashSet<>();
    for (String entity : set) {
      ResourceModel entityResourceModel = new ResourceModel("dbr", entity);
      ResourceModel updatedOrNull = updateResourceModel(entityResourceModel, version);
      if (updatedOrNull != null) {
        toRemove.add(entityResourceModel.getIdentifier());
        toAdd.add(updatedOrNull.getIdentifier());
      }
    }
    set.removeAll(toRemove);
    set.addAll(toAdd);
  }

  /**
   * returns the version of a model by parsing its version name
   */
  private ApiVersion getVersionByModel(RequestModel model) throws VersionNotFoundException {
    String versionName = model.getVersion();
    if (!isSemanticVersioningString(versionName)) {
      throw new VersionNotFoundException();
    }
    return getVersionByName(versionName);
  }


  /**
   * Parses a version name and returns the corresponding version in the version set
   *
   * @throws VersionNotFoundException if the version was not found
   * @throws IndexOutOfBoundsException if the versionName is bad formatted
   * @throws NumberFormatException if the versionName is bad formatted
   */
  public ApiVersion getVersionByName(String versionName) throws VersionNotFoundException {
    String[] numbers = versionName.split("\\.");
    return versions.get(
        Integer.valueOf(numbers[0]), //major
        Integer.valueOf(numbers[1]), //minor
        Integer.valueOf(numbers[2]) //patch
    );
  }

  /**
   * checks if the versionName matches the format "[number].[number].[number]"
   */
  public static boolean isSemanticVersioningString(String versionName) {
    return versionName.matches("\\d*\\.\\d*\\.\\d*");
  }

  public String getLatest() {
    return versions.getLatest().getSemanticVersioningString();
  }
}
