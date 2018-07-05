package dbpedia.api.model;

import java.util.Collections;
import java.util.Set;

public class ValueRequestModel extends RequestModel {


  public static final String PATH = "/values";
  public static final String URI_ENTITY = "entities";
  public static final String URI_PROPERTY = "property";

  private Set<String> entities;
  private Set<ResourceModel> properties;
  private boolean mapping;

  private ValueRequestModel() {
  } // for the builder


  public ValueRequestModel(Set<String> entities, Set<ResourceModel> properties, boolean mapping) {
    this.entities = entities;
    this.properties = properties;
    this.mapping = mapping;
  } //for testing, can be deleted later


  public boolean validate() {
    return super.validate() &&
        (this.entities != null && !this.entities.isEmpty());
  }

  public Set<String> getEntities() {
    return Collections.unmodifiableSet(entities);
  }

  public Builder toBuilder() {
    return new Builder().copy(this);
  }

  public Set<ResourceModel> getProperties() {
    return Collections.unmodifiableSet(properties);
  }

  public Boolean getMapping() {
    return mapping;
  }


  @Override
  public String getPath() {
    return PATH;
  }

  public static class Builder extends
      AbstractBuilder<ValueRequestModel, Builder> {

    @Override
    protected ValueRequestModel createModelInstance() {
      return new ValueRequestModel();
    }

    @Override
    Builder getSubclassInstance() {
      return this;
    }

    @Override
    public Builder copy(ValueRequestModel model) {
      setEntities(model.getEntities());
      setProperties(model.getProperties());
      setMapping(model.getMapping());
      return super.copy(model);
    }

    public Builder setEntities(Set<String> entities) {
      instance.entities = entities;
      return this;
    }

    public Builder setProperties(Set<ResourceModel> properties) {
      instance.properties = properties;
      return this;
    }

    public Builder setMapping(boolean mapping) {
      instance.mapping = mapping;
      return this;
    }

  }

}
