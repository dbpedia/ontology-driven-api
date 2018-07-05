package dbpedia.api.model;

import java.util.Objects;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Filter implements Comparable {

  private ResourceModel filterProp;
  /**
   * This is always the identifier of a Resource with the namespace <http://dbpedia.org/resource/>
   * if you want to let the user choose, change this to be a ResourceModel and maybe add a filterLiteral of type jena.Literal
   */
  private String filterVal;
  /**
   * Comparison operator. Can be one of gt,lt,eq,ge,le,sw
   */
  private String filterOp;
  /**
   * Selects if Filter is Optional or Required
   */
  private Bool bool;

  /**
   * both for builder
   */
  public Filter(ResourceModel filterProp, String filterVal, String filterOp, Bool bool) {
    this.filterProp = filterProp;
    this.filterVal = filterVal;
    this.filterOp = filterOp;
    this.bool = bool;
  }

  private Filter() {
  } //for the builder


  public ResourceModel getFilterProp() {
    return filterProp;
  }

  public String getFilterVal() {
    return filterVal;
  }

  public String getFilterOp() {
    return filterOp;
  }

  public Bool getBool() {
    return bool;
  }

  public boolean validate() {
    return (this.filterVal != null || this.filterProp != null);
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (o == this) {
      return true;
    }
    if (!(o instanceof Filter)) {
      return false;
    }
    Filter filter = (Filter) o;
    boolean propEquality =
        Objects.equals(this.filterProp, filter.filterProp);
    boolean valEquality = Objects.equals(filter.getFilterVal(), this.getFilterVal());
    boolean opEqualty = Objects.equals(this.filterOp, filter.filterOp);
    boolean boolEquality = this.bool == filter.getBool();
    return propEquality && valEquality && opEqualty && boolEquality;
  }

  public Builder toBuilder() {
    return new Builder().copy(this);
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
        .append(this.bool)
        .append(this.filterOp)
        .append(this.filterVal)
        .append(this.filterProp)
        .build();
  }

  @Override
  public String toString() {
    return "Filter: {property=" + filterProp + " value=" + filterVal + " bool=" + bool
        + "}";
  }

  public static Filter.Builder newBuilder() {
    return new Builder();
  }

  @Override
  public int compareTo(Object o) {
    if (((Filter)o).getBool() == Bool.AND && this.bool == Bool.OR) {
      return 1;
    }
    else if (((Filter)o).getBool() == Bool.OR && this.bool == Bool.AND) {
      return -1;
    } else if (((Filter)o).getBool() == this.bool) {
      if (((Filter)o).getFilterVal() != null && this.getFilterVal() != null) {
        if (((Filter)o).getFilterVal().compareTo(this.getFilterVal()) < 0) {
          return -1;
        }
        if (((Filter)o).getFilterVal().compareTo(this.getFilterVal()) > 0) {
          return 1;
        } else {
          if (((Filter)o).getFilterProp() != null && this.getFilterProp() != null) {
            if (((Filter) o).getFilterProp().getIdentifier()
                .compareTo(this.getFilterProp().getIdentifier()) < 0) {
              return -1;
            }
            if (((Filter) o).getFilterProp().getIdentifier()
                .compareTo(this.getFilterProp().getIdentifier()) > 0) {
              return 1;
            } else {
              return 0;
            }
          }
        }
      }
    }
    return 0;
  }

  /**
   * creates a ResourceModel with prefix="dbr" and the filter value as the identifier
   */
  public ResourceModel getValueAsResource() {
    return new ResourceModel("dbr", this.getFilterVal());
  }

  /**
   * Checks if the value is a resource
   *
   * @return true if the value is a resource, otherwise false
   */
  public boolean isValueAResource() {
    return this.filterOp == null;
  }

  public enum Bool {
    AND,
    OR
  }

  public static class Builder {

    private Filter instance = new Filter();

    //these are used to build the ResourceModel if (if they are not null)
    private String prefix;
    private String propName;

    public Builder() {
      instance.bool = Bool.AND;
    }

    public Builder copy(Filter filter) {
      setFilterVal(filter.getFilterVal());
      setFilterOp(filter.getFilterOp());
      setFilterProps(filter.getFilterProp());
      setFilterBool(filter.getBool());
      return this;
    }

    public Builder setFilterProps(ResourceModel property) {
      instance.filterProp = property;
      this.prefix = null;
      this.propName = null;
      return this;
    }

    public Builder setFilterVal(String val) {
      instance.filterVal = val;
      return this;
    }

    public Builder setFilterOp(String val) {
      instance.filterOp = val;
      return this;
    }

    public Builder setFilterBool(Bool val) {
      instance.bool = val;
      return this;
    }

    /**
     * Sets a resource as the filter value.
     * Currently, this ignores the prefix of the resource and just replaces the filter value with
     * the resource identifier
     * @param resource
     * @return
     */
    public Builder setResourceValue(ResourceModel resource) {
      instance.filterVal = resource.getIdentifier();
      return this;
    }

    public Builder setPrefix(String val) {
      this.prefix = val;
      return this;
    }

    public Builder setPropName(String val) {
      this.propName = val;
      return this;
    }

    public Filter build() {
      if (this.prefix != null && this.propName != null) {
        instance.filterProp = new ResourceModel(this.prefix, this.propName);
      }
      if (!instance.validate()) {
        throw new IllegalStateException("Filter is not valid!");
      }
      return instance;
    }

  }

}

