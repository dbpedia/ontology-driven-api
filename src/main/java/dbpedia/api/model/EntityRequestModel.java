package dbpedia.api.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class EntityRequestModel extends RequestModel {

  public static final String PATH = "/entities";
  public static final Boolean DEFAULT_ONLY_IMPORANT_VALUE = false;
  public static final String URI_VALUE = "value";
  public static final String URI_FILTER = "filter";
  public static final String URI_OPTIONAL_FILTER = "ofilter";


  private Set<Filter> filterList;
  private String classname;
  private boolean onlyImportant;

  private EntityRequestModel() {
  } //for the builder

  public EntityRequestModel(Set<Filter> filterList, boolean onlyImportant, String classname) {
    this.classname = classname;
    this.filterList = filterList;
    this.onlyImportant = onlyImportant;
  } //for testing, can be deleted later

  @Override
  public boolean validate() {
    return super.validate() &&
        ((this.filterList != null && !this.filterList.isEmpty()) || this.classname != null);

  }

  public Builder toBuilder() {
    return new Builder().copy(this);
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (!(o instanceof EntityRequestModel)) {
      return false;
    }
    EntityRequestModel other = (EntityRequestModel) o;
    return super.equals(o)
        && Objects.equals(other.filterList, filterList)
        && Objects.equals(other.onlyImportant, onlyImportant);
  }

  @Override
  public int hashCode() {
    return super.hashCode(); //TODO generate hashCode based on the equals method
  }

  @Override
  public String getPath() {
    return PATH;
  }

  public Set<Filter> getFilterList() {
    return Collections.unmodifiableSet(filterList);
  }


  public String getClassName() {
    return this.classname;
  }

  public boolean getonlyImportant() {
    return onlyImportant;
  }

  public static class Builder extends
      AbstractBuilder<EntityRequestModel, Builder> {


    public Builder() {
      super();
      this.instance.onlyImportant = DEFAULT_ONLY_IMPORANT_VALUE;
    }


    @Override
    protected EntityRequestModel createModelInstance() {
      return new EntityRequestModel();
    }

    @Override
    Builder getSubclassInstance() {
      return this;
    }

    @Override
    public Builder copy(EntityRequestModel model) {
      setonlyImportant(model.getonlyImportant());
      setFilterList(model.getFilterList());
      //set attributes in RequestModel and return this builder
      return super.copy(model);
    }

    public Builder setonlyImportant(Boolean onlyImportant) {
      instance.onlyImportant = onlyImportant;
      return this;
    }

    public Builder setFilterList(Set<Filter> filterList) {
      instance.filterList = new HashSet<>(filterList);
      return this;
    }

    public Builder setClassname(String classname) {
      instance.classname = classname;
      return this;
    }

  }

}
