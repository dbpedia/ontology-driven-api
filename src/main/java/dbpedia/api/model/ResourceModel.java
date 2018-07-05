package dbpedia.api.model;


import java.util.Objects;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ResourceModel {

  public static final String URI_NAMESPACE = "prefix";
  public static final String URI_PROPERTY_NAME = "name";

  //TODO make to enum
  private String prefix;
  private String identifier;

  public ResourceModel(String namespace, String propertyName) {
    this.prefix = namespace;
    this.identifier = propertyName;
  }

  private ResourceModel() {
  } //for the builder

  public String getPrefix() {
    return prefix;
  }

  public String getIdentifier() {
    return identifier;
  }

  private boolean validate() {
    return this.identifier != null;
  }

  @Override
  public String toString() {
    return this.prefix + ":" + this.identifier;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof ResourceModel)) {
      return false;
    }
    ResourceModel other = (ResourceModel) obj;
    return Objects.equals(other.identifier, this.identifier)
        && Objects.equals(other.prefix, this.prefix);
  }

  @Override
  public int hashCode() {

    return new HashCodeBuilder()
        .append(this.prefix)
        .append(this.identifier)
        .build();
  }


  public static class Builder {

    private ResourceModel instance = new ResourceModel();

    public Builder setNameSpace(String nameSpace) {
      instance.prefix = nameSpace;
      return this;
    }

    public Builder setPropertyName(String propertyName) {
      instance.identifier = propertyName;
      return this;
    }

    public Builder copyProperty(ResourceModel property) {
      instance.prefix = property.prefix;
      instance.identifier = property.identifier;
      return this;
    }

    public ResourceModel build() {
      if (!instance.validate()) {
        throw new IllegalStateException("Property is not valid");
      }
      return instance;
    }
  }

}