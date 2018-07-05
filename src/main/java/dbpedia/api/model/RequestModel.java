package dbpedia.api.model;

import java.util.Objects;

public abstract class RequestModel {


  public static final String URI_FORMAT = "format";
  public static final String URI_KEY = "key";
  public static final String URI_LIMIT = "limit";
  public static final String URI_OFFSET = "offset";
  public static final String URI_PRETTY = "pretty";

  public static final Style DEFAULT_STYLE = Style.NONE;
  public static final ReturnFormat DEFAULT_FORMAT = ReturnFormat.JSON;
  public static final String URI_OLD_VERSION = "oldVersion";

  private String version;
  private ReturnFormat format;
  private Style style;
  private String key;
  private Window window; //contains offset and window length
  private boolean allowIncompatibleVersion;

  protected RequestModel() {
  }

  public ReturnFormat getFormat() {
    return format;
  }

  public Style getStyle() {
    return style;
  }

  public String getKey() {
    return key;
  }

  public Window getWindow() {
    return window;
  }

  public boolean validate() {
    return this.version != null;
  }

  public abstract String getPath();

  protected RequestModel(String version) {
    this.version = version;
  }

  public String getVersion() {
    return version;
  }

  public boolean isAllowIncompatibleVersion() {
    return allowIncompatibleVersion;
  }

  public enum ReturnFormat {
    JSON, JSONLD, TSV, RDF, TURTLE, NTRIPLES, RDFXML, RDFJSON
  }

  public enum Style {
     NESTED, NONE, PREFIXED, SHORT
  }


  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (!(o instanceof RequestModel)) {
      return false;
    }
    RequestModel other = (RequestModel) o;
    return Objects.equals(other.format, format)
        && Objects.equals(other.style, style)
        && Objects.equals(other.key, key)
        && Objects.equals(other.version, version)
        && Objects.equals(other.window, window);
  }

  @Override
  public int hashCode() {
    return super.hashCode(); //TODO generate qualified hashcode based on the equals method
  }

  public abstract static class AbstractBuilder<T extends RequestModel, SubClass extends AbstractBuilder> {

    protected T instance;

    public AbstractBuilder() {
      instance = createModelInstance();
      ((RequestModel) instance).style = DEFAULT_STYLE;
      ((RequestModel) instance).format = DEFAULT_FORMAT;
    }

    abstract T createModelInstance();

    abstract SubClass getSubclassInstance();

    public SubClass copy(T model) {
      setStyle(model.getStyle());
      setVersion(model.getVersion());
      setFormat(model.getFormat());
      setWindow(model.getWindow());
      setKey(model.getKey());
      setAllowIncompatibleVersion(model.isAllowIncompatibleVersion());
      return getSubclassInstance();
    }

    public SubClass setFormat(ReturnFormat format) {
      ((RequestModel) instance).format = format;
      return getSubclassInstance();
    }

    public SubClass setAllowIncompatibleVersion(boolean allowIncompatibleVersion) {
      ((RequestModel) instance).allowIncompatibleVersion = allowIncompatibleVersion;
      return getSubclassInstance();
    }

    public SubClass setStyle(Style style) {
      ((RequestModel) instance).style = style;
      return getSubclassInstance();
    }

    public SubClass setKey(String key) {
      ((RequestModel) instance).key = key;
      return getSubclassInstance();
    }

    public SubClass setWindow(Window window) {
      ((RequestModel) instance).window = window;
      return getSubclassInstance();
    }

    public SubClass setVersion(String version) {
      ((RequestModel) instance).version = version;
      return getSubclassInstance();
    }

    public T build() {
      if (instance.getStyle() == Style.NESTED && !(instance.getFormat()== ReturnFormat.JSON)) {
        this.setStyle(Style.NONE);
      }
      if (!(instance.getFormat() == ReturnFormat.JSON) && !(instance.getFormat() == ReturnFormat.TSV)) {
        this.setStyle(Style.NONE);
      }
      if (!instance.validate()) {
        throw new IllegalStateException("Request not valid!");
      }
      return instance;
    }

  }

}
