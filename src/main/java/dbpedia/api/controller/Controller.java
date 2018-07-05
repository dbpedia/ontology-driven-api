package dbpedia.api.controller;

import dbpedia.api.configuration.Configuration;
import dbpedia.api.model.EntityRequestModel;
import dbpedia.api.model.Filter;
import dbpedia.api.model.Filter.Bool;
import dbpedia.api.model.LogDataModel;
import dbpedia.api.model.RequestModel;
import dbpedia.api.model.RequestModel.AbstractBuilder;
import dbpedia.api.model.RequestModel.ReturnFormat;
import dbpedia.api.model.RequestModel.Style;
import dbpedia.api.model.ResourceModel;
import dbpedia.api.model.ValueRequestModel;
import dbpedia.api.model.Window;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UrlPathHelper;
import springfox.documentation.annotations.ApiIgnore;

@org.springframework.stereotype.Controller
@RequestMapping(value = "/${uri.path:api}/{version}")
@RestController
@Api(value = "Controller", description = "Do a Dbpedia request", tags = "Request Types")
public class Controller {

  private RequestHandler requestHandler;
  private Configuration config;
  private static final Logger LOG = LogManager.getLogger(Controller.class.getName());
  private static final Logger QLOG = LogManager.getLogger("QueryLogger");
  private static final String PROPERTY_SEPARATOR = ":";
  private LogDataModel logData;
  private Map mappedProperties;

  //The infotexts for swagger
  private final String defaultKey = "1234";
  private final String formatText = "<b>The desired output format:</b><br>"
      + "- JSON<br>- JSONLD<br>"
      + "- TSV<br>- RDFJSON<br>- TURTLE<br>- NTRIPLES<br>- RDFXML <br>"
      + "The format parameter overrides the return type requested in the HTTP accept "
      + "header in order to use it in applications where it is not easy to set the accept head properly (e.g. web browser)";
  private final String formatValues = "JSON,JSONLD,TSV,RDFJSON,TURTLE,NTRIPLES,RDFXML";
  private final String prettyText = "<b>Prettification options:</b><br>"
      + "- NONE <br>- NESTED (only works with JSON)<br>- PREFIXED<br>- SHORT";
  private final String limitText = "<b>The maximum count of results</b>";
  private final String offsetText = "<b>Number of the elements which should be skipped (counting from the first one)</b>";
  private final String oldversionText = "<b>Allow to use an older API-version or not</b>";
  private final String keyText = "The API-key for authentication";
  private final String entityText = "<b>A list entities. In the RDF-graph they are the subjects";
  private final String propertyText = "Th attributes of the entities. In the RDF-graph they are the predicates";
  private final String oFilterText = "[prefix]:[property],[operator],[value]:<br>"
      + "The optional filter sorts out for the value, but only if the entity got the property.<br>"
      + "[prefix]:[property]:<br>"
      + "Adds the result of the property, but only if the entity got the property";
  private final String prettyValues = "PREFIXED,NESTED,SHORT,NONE";
  private final String filterText = "With this parameter you can add a filter. It has to be in the "
      + "form [prefix]:[identifier],[operator],[value] <br>operators: <br> - gt : greater than  "
      + "<br> - lt : lower than "
      + "<br> - eq : equals to"
      + "<br> - ge : equal or greater "
      + "<br> - le : equal or lower"
      + "<br> - sw : startswith (only recommended for strings)<br>"
      + "If there is no such element, the whole instance will be omitted in the output. ";
  private final String valueText = "With this parameter you specify a value and, optionally, a property. It can have the form [value]  or [value], [prefix]:[identifier] ";
  private final String versionText =
      "Specify the API version you want to use. For the latest version check out the info-Page of the API";
  private final String classnameText = "Name of the class of which you want to see the instances";
  private final String onlyimportantText = "Returns the important properties of the  class.";


  public Controller(RequestHandler requestHandler, Configuration config,
      @Qualifier("rml") Map mappedProperties) {
    this.requestHandler = requestHandler;
    this.config = config;
    this.mappedProperties = mappedProperties;
  }

  @GetMapping(value = ValueRequestModel.PATH, produces = {"application/JSON;",
      "application/JSONLD;", "application/NTRIPLES;", "application/TSV;", "application/RDFJSON",
      "application/RDFXML", "application/TURTLE;"
  })
  @ResponseBody
  @ApiImplicitParams({
      @ApiImplicitParam(
          name = "version", value = versionText,
          required = true, dataType = "string",
          paramType = "path", defaultValue = ""
      ),
      @ApiImplicitParam(
          name = "key", value = keyText,
          required = false, dataType = "String",
          paramType = "query", defaultValue = defaultKey
      ),
      @ApiImplicitParam(
          name = "oldVersion", value = oldversionText,
          required = false, dataType = "boolean",
          paramType = "query", defaultValue = "false"
      ),
      @ApiImplicitParam(
          name = "entities", value = entityText,
          required = false, dataType = "string",
          paramType = "query", defaultValue = "Barack_Obama,Hillary_Clinton"
      ),
      @ApiImplicitParam(
          name = "property", value = propertyText,
          required = false, allowMultiple = true, dataType = "string",
          paramType = "query", defaultValue = "foaf:surname" + "\n" + "foaf:gender"
      ),
      @ApiImplicitParam(
          name = "format", value = formatText,
          required = false, dataType = "string",
          paramType = "query", defaultValue = "",
          allowableValues = formatValues
      ),
      @ApiImplicitParam(
          name = "pretty", value = prettyText,
          required = false, dataType = "string",
          paramType = "query", defaultValue = "NONE",
          allowableValues = prettyValues
      ),
      @ApiImplicitParam(
          name = "limit", value = limitText,
          required = false, dataType = "string",
          paramType = "query", defaultValue = "100"
      ),
      @ApiImplicitParam(
          name = "offset", value = offsetText,
          required = false, dataType = "string",
          paramType = "query", defaultValue = "0"
      )

  })
  @ApiOperation(value = "Value Request", notes = "Set up a request for one or more entities. "
      + "If no property is given, all the properties of the entity and their values will be returned. "
      + "If a property is given, only the values of this property are returned. <br>"
      + " Press the 'Try out' button with the default values to see a request for the gender and surname properties of Barack Obama and Hillary Clinton."
  )
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Success"),
      @ApiResponse(code = 400, message = "Bad Request"),
      @ApiResponse(code = 500, message = "Internal Server Error")})
  public ResponseEntity valueRequest(
      //@PathVariable String path,
      @PathVariable String version,
      @RequestParam(value = ValueRequestModel.URI_ENTITY) @ApiIgnore List<String> entities,
      @RequestParam(value = "key", defaultValue = "") String key,
      @RequestParam(value = RequestModel.URI_OLD_VERSION, defaultValue = "false") boolean oldVersion,
      @RequestParam() @ApiIgnore MultiValueMap<String, String> parameters,
      @RequestHeader("Accept") @ApiIgnore String header,
      HttpServletRequest request
  ) {
    logData = new LogDataModel();
    logData.setQuery(ValueRequestModel.PATH + "?" + request.getQueryString());
    logData.setRequestKey(key);

    UrlPathHelper helper = new UrlPathHelper();
    LinkedMultiValueMap<String, String> map =
        (LinkedMultiValueMap<String, String>) helper.decodeMatrixVariables(request, parameters);

    ValueRequestModel.Builder builder =
        initBuilder(new ValueRequestModel.Builder(), version, key, oldVersion, map, header);

    Set<ResourceModel> propertySet = new HashSet<>();
    if (map.containsKey(ValueRequestModel.URI_PROPERTY)) {
      for (String property : map.get(ValueRequestModel.URI_PROPERTY)) {
        propertySet.add(parsePropertyString(property));
      }
    }
    builder.setEntities(new HashSet<>(entities));
    builder.setProperties(propertySet);
    builder.setMapping(false);

    try {
      return requestHandler.handle(builder.build(), logData);
    } catch (IllegalArgumentException e) {
      return illegalArgument(e.getMessage());
    }
  }
  @GetMapping(value = EntityRequestModel.PATH, produces = {"application/JSON;",
      "application/JSONLD;", "application/NTRIPLES;", "application/TSV;", "application/RDFJSON",
      "application/RDFXML", "application/TURTLE;"
  })//{"your/ContentType","your/type2"})
  @ResponseBody

  @ApiImplicitParams({
      @ApiImplicitParam(
          name = "version", value = versionText,
          required = true, dataType = "string",
          paramType = "path", defaultValue = ""
      ),
      @ApiImplicitParam(
          name = "key", value = keyText,
          required = false, dataType = "String",
          paramType = "query", defaultValue = defaultKey
      ),
      @ApiImplicitParam(
          name = "oldVersion", value = oldversionText,
          required = false, dataType = "boolean",
          paramType = "query", defaultValue = "false"
      ),
      @ApiImplicitParam(
          name = "value", value = valueText,
          required = false, allowMultiple = true, dataType = "string",
          paramType = "query", defaultValue = "Donald_Trump,dbp:nominee"
      ),
      @ApiImplicitParam(
          name = "filter", value = filterText,
          required = false, allowMultiple = true, dataType = "string",
          paramType = "query", defaultValue = ""
      ),
      @ApiImplicitParam(
          name = "ofilter", value = oFilterText,
          required = false, allowMultiple = true, dataType = "string",
          paramType = "query", defaultValue = "dbo:startDate"
      ),
      @ApiImplicitParam(
          name = "format", value = formatText,
          required = false, dataType = "string",
          paramType = "query", defaultValue = "",
          allowableValues = formatValues
      ),
      @ApiImplicitParam(
          name = "pretty", value = prettyText,
          required = false, dataType = "string",
          paramType = "query", defaultValue = "NONE",
          allowableValues = prettyValues
      ),
      @ApiImplicitParam(
          name = "limit", value = limitText,
          required = false, dataType = "string",
          paramType = "query", defaultValue = "100"
      ),
      @ApiImplicitParam(
          name = "offset", value = offsetText,
          required = false, dataType = "string",
          paramType = "query", defaultValue = "0"
      )
  })
  @ApiOperation(value = "Entity Request", notes = "Set up an entity request: Enter one or more "
      + "entities and optionally a property. You'll get all the entities, that share this given entity as the value of a property. "
      + " The results can also be filtered by a certain condition."
      + " For an example with the default values press the 'Try out'-button. You'll see the entities "
      + "where Donald Trump has been 'nominee' and the 'startDay' if they have one. "
      + "Additionally you can filter entities using the filter parameter"
  )
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Success"),
      @ApiResponse(code = 400, message = "Bad Request"),
      @ApiResponse(code = 500, message = "Internal Server Error")})
  public ResponseEntity entityRequest(
      @PathVariable String version,
      @RequestParam(value = "key", defaultValue = "") String key,
      @RequestParam(value = RequestModel.URI_OLD_VERSION, defaultValue = "false") boolean oldVersion,
      @RequestParam @ApiIgnore MultiValueMap<String, String> parameters,
      @RequestHeader("Accept") @ApiIgnore String header,
      HttpServletRequest request
  ) {

//   get type from produces
//    request.getHeader("Accept");

    logData = new LogDataModel();
    logData.setQuery(EntityRequestModel.PATH + "?" + request.getQueryString());
    logData.setRequestKey(key);

    UrlPathHelper helper = new UrlPathHelper();
    LinkedMultiValueMap<String, String> map =
        (LinkedMultiValueMap<String, String>) helper.decodeMatrixVariables(request, parameters);

    EntityRequestModel.Builder builder =
        initBuilder(new EntityRequestModel.Builder(), version, key, oldVersion, map, header);
    Set<Filter> filterSet = new HashSet<>();
    if (map.containsKey(EntityRequestModel.URI_FILTER)) {
      filterSet.addAll(parseFilterString(map.get(EntityRequestModel.URI_FILTER), Bool.AND));
    }
    if (map.containsKey(EntityRequestModel.URI_OPTIONAL_FILTER)) {
      filterSet.addAll(parseFilterString(map.get(EntityRequestModel.URI_OPTIONAL_FILTER), Bool.OR));
    }
    if (map.containsKey(EntityRequestModel.URI_VALUE)) {
      filterSet.addAll(
          parsePropertyValuePair(map.get(EntityRequestModel.URI_VALUE)));
    }
    if (!map.containsKey(EntityRequestModel.URI_FILTER)
        && !map.containsKey(EntityRequestModel.URI_OPTIONAL_FILTER)
        && !map.containsKey(EntityRequestModel.URI_VALUE)) {
      throw new IllegalArgumentException("Query parameters must be specified");
    }
    builder.setFilterList(filterSet);

    try {
      return requestHandler.handle(builder.build(), logData);
    } catch (IllegalArgumentException e) {
      return illegalArgument(e.getMessage());
    }
  }


  @GetMapping(value = "/instances/{classname}", produces = {"application/JSON;",
      "application/JSONLD;", "application/NTRIPLES;", "application/TSV;", "application/RDFJSON",
      "application/RDFXML", "application/TURTLE;"
  })
  @ResponseBody

  @ApiImplicitParams({
      @ApiImplicitParam(
          name = "key", value = keyText,
          required = false, dataType = "String",
          paramType = "query", defaultValue = defaultKey,
          example = "dadasd"
      ),
      @ApiImplicitParam(
          name = "classname", value = classnameText,
          required = true, dataType = "string",
          paramType = "path", defaultValue = "SpaceStation"
      ),
      @ApiImplicitParam(
          name = "onlyImportant", value = onlyimportantText,
          required = false, dataType = "boolean",
          paramType = "query", defaultValue = "false"
      ),
      @ApiImplicitParam(
          name = "oldVersion", value = oldversionText,
          required = false, dataType = "boolean",
          paramType = "query", defaultValue = "false"
      ),
      @ApiImplicitParam(
          name = "version", value = versionText,
          required = true, dataType = "string",
          paramType = "path", defaultValue = ""
      ),
      @ApiImplicitParam(
          name = "filter", value = filterText,
          required = false, allowMultiple = true, dataType = "string",
          paramType = "query", defaultValue = ""
      ),
      @ApiImplicitParam(
          name = "ofilter", value = oFilterText,
          required = false, allowMultiple = true, dataType = "string",
          paramType = "query", defaultValue = "dbp:launch\ndbp:orbits,gt,50000"
      ),
      @ApiImplicitParam(
          name = "value", value = valueText,
          required = false, allowMultiple = true, dataType = "string",
          paramType = "query", defaultValue = ""
      ),
      @ApiImplicitParam(
          name = "format", value = formatText,
          required = false, dataType = "string",
          paramType = "query", defaultValue = "",
          allowableValues = formatValues
      ),
      @ApiImplicitParam(
          name = "pretty", value = prettyText,
          required = false, dataType = "string",
          paramType = "query", defaultValue = "NONE",
          allowableValues = prettyValues
      ),
      @ApiImplicitParam(
          name = "limit", value = limitText,
          required = false, dataType = "string",
          paramType = "query", defaultValue = "100"
      ),
      @ApiImplicitParam(
          name = "offset", value = offsetText,
          required = false, dataType = "string",
          paramType = "query", defaultValue = "0"
      ),

  })
  @ApiOperation(value = "Instances Request", notes =
      "Set up a request for all instances of a certain class."
          + " You can add filters for the desired results and also specify values you want the results to contain. "
          + " For an example use the 'Try out'-button with the default values. This will show you all "
          + "instances of the class 'Space_Station' that orbited more than 500000 times, and their launch date. "
          + "To understand the difference between  'filter' and  'ofilter', add the property dbp:crew, "
          + "one time at 'filter' and the other time at 'ofilter'."
  )
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Success"),
      @ApiResponse(code = 400, message = "Bad Request"),
      @ApiResponse(code = 500, message = "Internal Server Error")})
  public ResponseEntity instancesRequest(
      @PathVariable String version,
      @PathVariable("classname") String classname,
      @RequestParam(value = "key", defaultValue = "") String key,
      @RequestParam(value = RequestModel.URI_OLD_VERSION, defaultValue = "false") boolean oldVersion,
      @RequestParam() @ApiIgnore MultiValueMap<String, String> parameters,
      @RequestHeader("Accept") @ApiIgnore  String header,
      HttpServletRequest request
  ) {

    logData = new LogDataModel();
    logData.setQuery("/instances/" + classname + "?" + request.getQueryString());
    logData.setRequestKey(key);

    UrlPathHelper helper = new UrlPathHelper();
    LinkedMultiValueMap<String, String> map =
        (LinkedMultiValueMap<String, String>) helper.decodeMatrixVariables(request, parameters);

    EntityRequestModel.Builder builder =
        initBuilder(new EntityRequestModel.Builder(), version, key, oldVersion, map, header);
    Set<Filter> filterSet = new HashSet<>();
    if (map.containsKey(EntityRequestModel.URI_FILTER)) {
      filterSet.addAll(parseFilterString(map.get(EntityRequestModel.URI_FILTER), Bool.AND));
    }
    if (map.containsKey(EntityRequestModel.URI_OPTIONAL_FILTER)) {
      filterSet.addAll(parseFilterString(map.get(EntityRequestModel.URI_OPTIONAL_FILTER), Bool.OR));
    }
    if (map.containsKey(EntityRequestModel.URI_VALUE)) {
      filterSet
          .addAll(parsePropertyValuePair(map.get(EntityRequestModel.URI_VALUE)));
      // this is hardcoded with 'intersection' because otherwise there would be no effect
      // (the value wouldn't filter anything)
    }

    if (map.containsKey("onlyImportant")) {
      if (map.getFirst("onlyImportant").toLowerCase().equals("true")) {
        builder.setonlyImportant(Boolean.TRUE);
        importantProperties(classname, filterSet);
      } else {
        builder.setonlyImportant(Boolean.FALSE);
      }
    }

    builder.setFilterList(filterSet);
    builder.setClassname(classname);
    try {
      return requestHandler.handle(builder.build(), logData);
    } catch (IllegalArgumentException e) {
      return illegalArgument(e.getMessage());
    }
  }

  /**
   * Method for requesting all instances and their "important" properties
   *
   * @param classname name of the class (instance)
   * @param filterSet set containing the propertynames
   */
  private void importantProperties(String classname, Set<Filter> filterSet) {
    if (mappedProperties.containsKey(classname)) {
      List<String> list = (List<String>) mappedProperties.get(classname);
      for (String prop : list) {
        Filter.Builder filterBuilder = new Filter.Builder();
        filterBuilder.setFilterProps(parsePropertyString(prop));
        filterBuilder.setFilterBool(Bool.OR);
        filterBuilder.setFilterOp(null);
        filterBuilder.setFilterVal(null);
        filterSet.addAll(buildTheFilter(filterBuilder));
      }
    }
  }

  /**
   * parses common parameters which occur at each request type
   */
  private <T extends AbstractBuilder> T initBuilder(T builder, String version, String key,
      boolean useOldVersion, LinkedMultiValueMap<String, String> map, String accHeader) {
    String format = map.getFirst(RequestModel.URI_FORMAT);
    String style = map.getFirst(RequestModel.URI_PRETTY);
    String limit = map.getFirst(RequestModel.URI_LIMIT);
    String offset = map.getFirst(RequestModel.URI_OFFSET);

    builder.setVersion(version);
    builder.setAllowIncompatibleVersion(useOldVersion);
    if (format != null) {
      builder.setFormat(ReturnFormat.valueOf(format.toUpperCase()));
    } else if (accHeader != null) {
      builder.setFormat(ReturnFormat.valueOf(accHeader.split("/")[1].toUpperCase()));
    }
    if (style != null) {
      builder.setStyle(Style.valueOf(style.toUpperCase()));
    }
    builder.setKey(key);

    // Windowing Logic
    // if both values are not null
    if (limit != null && offset != null) {
      // limit must be less than maxWindowLimit
      if (Integer.valueOf(limit) < config.getMaxWindowLimit()) {
        builder.setWindow(new Window(Integer.valueOf(offset), Integer.valueOf(limit)));
      } else {
        LOG.warn("Limit exceeds maximum window limit! limit is set to maximum value");
        builder.setWindow(new Window(Integer.valueOf(offset), config.getMaxWindowLimit()));
      }
      // only limit is given, set offset to 0
    } else if (limit != null) {
      if (Integer.valueOf(limit) < config.getMaxWindowLimit()) {
        builder.setWindow(new Window(0, Integer.valueOf(limit)));
      } else {
        LOG.warn("Limit exceeds maximum window limit! limit is set to maximum value");
        builder.setWindow(new Window(0, config.getMaxWindowLimit()));
      }
      // only offset is given, set limit to maxwindowLimit
    } else if (offset != null) {
      builder.setWindow(new Window(Integer.valueOf(offset), config.getMaxWindowLimit()));
    } else {
      builder.setWindow(new Window(0, config.getMaxWindowLimit()));
    }
    return builder;
  }

  /**
   * Properties given as composition of a prefix and an identifier, separated by ":"
   *
   * @param property something like dbo:netIncome
   * @return a ResourceModel containing the Property
   */

  public ResourceModel parsePropertyString(String property) {
    ResourceModel model = null;
    String[] splittedProperty = property.split(PROPERTY_SEPARATOR);
    if (splittedProperty.length != 2) {
      throw new IllegalArgumentException(
          "Properties must contain a namespace prefix " + "and an identifier, seperated by ':'");
    } else {
      model = new ResourceModel(splittedProperty[0], splittedProperty[1]);
    }
    return model;
  }

  /**
   * Parses a String that contains commaseparated values and creates a List of Filters, eacht Filter
   * containing only a value and the given Bool Used only for values in /instances request
   *
   * @param values RDF Objects, used as filter for instances of a certain class, like
   * Barack_Obama,Bill_Clinton
   * @param bool use optional in sparql query for the selection of this entity
   * @return Set of Filtermodels
   */

  public Set<Filter> parseValue(String values, Bool bool) {
    Set<Filter> filterList = new HashSet<>();
    if (values != null) {
      String[] valuelist = values.split(",");
      for (int i = 0; i < valuelist.length; i++) {
        Filter.Builder filterBuilder = new Filter.Builder();
        filterBuilder.setFilterOp(null);
        filterBuilder.setFilterBool(bool);
        filterBuilder.setFilterVal(valuelist[i]);
        filterList.add(filterBuilder.build());
      }
    }
    return filterList;
  }


  /**
   * Parses a List of parameters intended as triplestring for sparql selection and returns them as a
   * set of Filtermodels.
   *
   * @param values Can contain only values (rdf objects) like value=Barack_Obama, or a combination
   * of value and property, like Barack_Obama,dbp:nominee
   * @return contains the Filtermodels (with or without property set)
   */

  public Set<Filter> parsePropertyValuePair(List<String> values) {
    HashSet<Filter> filterList = new HashSet<>();
    if (!values.isEmpty()) {
      for (String valueList : values) {
        Filter.Builder filterBuilder = new Filter.Builder();
        filterBuilder.setFilterOp(null);
        filterBuilder.setFilterBool(Bool.AND);
        String[] splitPropertyValuePair = valueList.split(",");
        filterBuilder.setFilterVal(splitPropertyValuePair[0]);
        if (splitPropertyValuePair.length == 2) {
          ResourceModel property = parsePropertyString(splitPropertyValuePair[1]);
          filterBuilder.setFilterProps(property);
        }
        filterList.add(filterBuilder.build());
      }
    }
    return filterList;
  }

  /**
   * Builds a Filter
   *
   * @param filter something like filter=dbo:numberOfEmployees,gt,48000000 or filter=dbo:netIncome
   * @return The filter
   * @throws IllegalArgumentException When the filterString or the built filter is invalid
   */

  public Set<Filter> parseFilterString(List<String> filter, Bool bool)
      throws IllegalArgumentException {
    HashSet<Filter> filterSet = new HashSet<>();

    for (String filterString : filter) {
      Filter.Builder filterBuilder = new Filter.Builder();
      String[] splittedFilter = filterString.split(",");
      if (splittedFilter.length == 2 || splittedFilter.length > 3) {
        throw new IllegalArgumentException("Filter must contain either a property or, "
            + "commaseparated, a property, an operator and a value");
      } else {
        filterBuilder.setFilterProps(parsePropertyString(splittedFilter[0]));
        filterBuilder.setFilterOp(null);
        if (splittedFilter.length == 3) {
          filterBuilder.setFilterOp(determineFilterOperator(splittedFilter[1]));
          filterBuilder.setFilterVal(splittedFilter[2]);
        }
      }

      filterBuilder.setFilterBool(bool);
      filterSet.addAll(buildTheFilter(filterBuilder));
    }
    return filterSet;
  }

  /**
   * keeps exception handling from main logic, trying to build the Filter
   *
   * @param builder containing a property or property+operator+value
   * @return a Set of Filters
   */

  public Set<Filter> buildTheFilter(Filter.Builder builder) {
    HashSet<Filter> set = new HashSet<>();
    try {
      set.add(builder.build());
    } catch (IllegalStateException e) {
      throw new IllegalArgumentException("Filter " + builder + " is invalid!");
    }
    return set;
  }


  /**
   * Transforms the String in the URI into the Strings for the SPARQL Statements:<br> eq  -> '='
   * <br> lt  -> '<' <br> gt  -> '>' <br>
   *
   * @param uriOperator can be eq, lt,gt
   * @return can be =,<,>, defaults to null
   */
  private String determineFilterOperator(String uriOperator) {
    String sparqlOperator;
    uriOperator = uriOperator == null ? "null" : uriOperator;

    switch (uriOperator) {
      case ("eq"):
        sparqlOperator = "=";
        break;
      case ("gt"):
        sparqlOperator = ">";
        break;
      case ("lt"):
        sparqlOperator = "<";
        break;
      case ("null"):
        sparqlOperator = null;
        break;
      case ("le"):
        sparqlOperator = "<=";
        break;
      case ("ge"):
        sparqlOperator = ">=";
        break;
      case ("sw"):
        sparqlOperator = "startswith";
        break;
      default:
        throw new IllegalArgumentException("Filter operator must be one of gt,lt,eq,ge,le or sw");
    }
    return sparqlOperator;
  }

  // response entities for errors

  /**
   * Creates a Response when an IllegalArgumentException occures
   *
   * @param logMsg The message that is logged
   */
  private ResponseEntity<String> illegalArgument(String logMsg) {
    LOG.error(logMsg);
    logData.setException(logMsg + " - Illegal argument in URI");
    QLOG.error(logData.toString());
    return new ResponseEntity<>("Illegal Argument in URI.", HttpStatus.BAD_REQUEST);
  }

  /**
   * Creates a 404 Response
   */
  private ResponseEntity<String> pathNotFound(String path) {
    return new ResponseEntity<>(path + " not found.", HttpStatus.NOT_FOUND);
  }

}


