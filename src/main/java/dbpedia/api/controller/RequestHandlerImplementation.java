package dbpedia.api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dbpedia.api.configuration.SwaggerConf;
import dbpedia.api.factory.DBpediaQueryFactory;
import dbpedia.api.model.EntityRequestModel;
import dbpedia.api.model.LogDataModel;
import dbpedia.api.model.RequestModel;
import dbpedia.api.model.RequestModel.ReturnFormat;
import dbpedia.api.model.RequestModel.Style;
import dbpedia.api.model.UserModel.userType;
import dbpedia.api.model.ValueRequestModel;
import dbpedia.api.model.Window;
import dbpedia.api.versioning.VersionHandler;
import dbpedia.api.versioning.VersionNotFoundException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import org.apache.jena.atlas.json.JSON;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryException;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.resultset.RDFOutput;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class RequestHandlerImplementation implements RequestHandler {

  private DBpediaClient client;
  private Map<Class<? extends RequestModel>, DBpediaQueryFactory> factories;
  private static final Logger LOG =
      LogManager.getLogger(RequestHandlerImplementation.class.getName());

  private static final Logger QLOG = LogManager.getLogger("QueryLogger");
  private APIKeyHandler apiKeyHandler;
  private VersionHandler versionHandler;
  private boolean usingAPIKeys;


  public RequestHandlerImplementation(
      Map<Class<? extends RequestModel>, DBpediaQueryFactory> factories, DBpediaClient client,
      APIKeyHandler apiKeyHandler, VersionHandler versionHandler, boolean usingAPIKeys) {
    this.factories = factories;
    this.client = client;
    this.apiKeyHandler = apiKeyHandler;
    this.versionHandler = versionHandler;
    this.usingAPIKeys = usingAPIKeys;
  }

  /**
   * Handles an Request: * checks for valid key * Log Request * Version Replacements * calls the
   * factory to generate the query * sends the query to dbpedia * transform the dbpedia response
   * (format + style) method casts RequestModels to concrete modelclasses
   *
   * @param request contains all information from URI
   * @return the http-Response containing the response from DBpedia and statuscode
   */
  public ResponseEntity handle(RequestModel request, LogDataModel logDataModel) {

    ResponseEntity result = null;

    try {

      if (usingAPIKeys) {
        LOG.info("ApiKeys are activated!");
        keyScan(request);

        logDataModel
            .setRemainingQuotaMinute(apiKeyHandler.getMap().get(request.getKey()).getQuotaMinute());
        logDataModel
            .setRemainingQuotaHour(apiKeyHandler.getMap().get(request.getKey()).getQuotaHour());
        logDataModel
            .setRemainingQuotaDay(apiKeyHandler.getMap().get(request.getKey()).getQuotaDay());
      }
      // patch version
      if (!versionHandler.isLatest(request)) {
        if (versionHandler.canPatchToLatest(request) || request.isAllowIncompatibleVersion()) {
          if (request instanceof ValueRequestModel) {
            request = versionHandler.patchValueRequestModel((ValueRequestModel) request);
          } else if (request instanceof EntityRequestModel) {
            request = versionHandler.patchEntityRequestModel((EntityRequestModel) request);
          }
        } else {
          return new ResponseEntity<>(
              "Version " + request.getVersion() + " is not compatible "
                  + "with the current version of the api. The result could be incorrect. "
                  + "Use &oldVersion=true if you want to access an incompatible version.",
              HttpStatus.BAD_REQUEST);
        }
      }
    } catch (VersionNotFoundException e) {
      logDataModel.setException("Version" + request.getVersion() + " not found");
      QLOG.error(logDataModel.toString());
      return new ResponseEntity<>("Version " + request.getVersion() + " not found",
          HttpStatus.BAD_REQUEST);

    } catch (LimitExceededException e) {
      logDataModel.setException(e.getMessage());
      QLOG.error(logDataModel.toString());
      return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);

    } catch (InvalidUserkeyException e) {
      logDataModel.setException("Invalid userkey");
      QLOG.error(logDataModel.toString());
      return new ResponseEntity<>("Invalid userkey", HttpStatus.UNAUTHORIZED);
    }

    // find the factory for the request and make the query
    DBpediaQueryFactory f = factories.get(request.getClass());
    if (f == null) {
      logDataModel.setException("No Factory found for request class " + request.getClass());
      QLOG.error(logDataModel.toString());
      throw new IllegalArgumentException(
          "No Factory found for request class " + request.getClass());
    }
    Query query = f.makeQuery(request);

    // setting windowing
    addWindowingToQuery(query, request.getWindow());

        // do stuff with the returned String according to transformation information in the map
    return send(query, request, f.getPrefixMap(), logDataModel);
  }

  /**
   * send query to DBpedia-Endpoint, adding http-statuscode
   *
   * @param query Query from DBpediaQueryFactory
   * @return response containing dbpedia-response-String and http-statuscode
   */

  private ResponseEntity send(Query query, RequestModel model, PrefixMappingImpl prefixMapping,
      LogDataModel logDataModel) {
    ResponseEntity result;
    try {
      String dBpediaResponse = styleSwitch(query, model, prefixMapping, logDataModel);
      ResponseEntity.BodyBuilder bb = ResponseEntity.ok();
      bb.contentType(MediaType.valueOf("application/"+model.getFormat()));
      result= bb.body(dBpediaResponse);
    } catch (QueryException e) {
      result = new ResponseEntity(HttpStatus.BAD_REQUEST);
      LOG.error(e);
      logDataModel.setException(e.toString());
      QLOG.error(logDataModel.toString());
    } catch (Exception e) {
      result = new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
      LOG.error(e);
      logDataModel.setException(e.toString());
      QLOG.error(logDataModel.toString());
    }

    LOG.info("key: " + model.getKey() + " version: " + model.getVersion() + " format: "
        + model.getFormat() + " style: " + model.getStyle() + " window limit/offset: "
        + model.getWindow().getLimit() + "/" + model.getWindow().getOffset() + " statuscode: "
        + result.getStatusCodeValue());

    if (result.getStatusCode() == HttpStatus.OK) {
      QLOG.info(
          logDataModel.toString() + " Duration(ms): " + ((System.currentTimeMillis()) - logDataModel
              .getStartTime()));
    }
  return result;
  }

  /**
   * Method for changing the outputsytle
   *
   * @param query the query to change
   * @param model the requestmodel containing the sytle
   * @param prefixMapping necessary for the getting prefixes
   * @return the changed Query-String
   */
  public String styleSwitch(Query query, RequestModel model,
      PrefixMappingImpl prefixMapping, LogDataModel logDataModel) {

    Map<String, String> prefixes = prefixMapping.getNsPrefixMap();
    if (prefixes.containsValue(null)) {
      throw new IllegalArgumentException("Prefix  not found!");
    }
    ResultSet resultSet = client.sendQuery(query).getResultSet();
    String dBpediaResponse = format(ResultSetFactory.copyResults(resultSet), model.getFormat(), model.getStyle());
    logDataModel.setAnswerLength(dBpediaResponse.length());
    switch (model.getStyle()) {
      case SHORT:
        dBpediaResponse = prettyJsonParser(dBpediaResponse, prefixMapping, Style.SHORT);
        if (model.getFormat() == ReturnFormat.TSV) {
          dBpediaResponse = prettyTSVParser(dBpediaResponse, Style.SHORT);
        }
        break;
      case PREFIXED:
        dBpediaResponse = prettyJsonParser(dBpediaResponse, prefixMapping, Style.PREFIXED);
        if (model.getFormat() == ReturnFormat.TSV) {
          dBpediaResponse = prettyTSVParser(dBpediaResponse, Style.PREFIXED);
        }
        break;
      case NESTED:
        dBpediaResponse = allMightyNested(new JSONObject(dBpediaResponse), prefixMapping);
        break;
      default:
        break;
    }

    return dBpediaResponse;
  }

  /**
   * Handle the output format of the resultset from DBpedia.
   *
   * @param resultSet The response from DBpedia. Its an iterable resultset that can be iterated with
   * a ResultSetFormatter or RDFOutput.
   * @param format Possible formats are given in the enum RequestModel.ReturnFormat. Default is
   * JSON.
   * @return the formatted result as String
   */
  public String format(ResultSet resultSet, ReturnFormat format, Style style) {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    try {
      switch (format) {
        case TSV:
          if (style == Style.NONE) {
            ResultSetFormatter.outputAsTSV(os, resultSet);
          } else {
            ResultSetFormatter.outputAsJSON(os, resultSet);
          }
          break;
        case JSON:
          ResultSetFormatter.outputAsJSON(os, resultSet);
          break;
        case JSONLD:
          RDFOutput.outputAsRDF(os, "JSON-LD", resultSet);
          break;
        case RDF:
          RDFOutput.outputAsRDF(os, "RDF/JSON", resultSet);
          break;
        case TURTLE:
          RDFOutput.outputAsRDF(os, "TURTLE", resultSet);
          break;
        case NTRIPLES:
          RDFOutput.outputAsRDF(os, "N-TRIPLES", resultSet);
          break;
        case RDFXML:
          RDFOutput.outputAsRDF(os, "RDF/XML", resultSet);
          break;
        case RDFJSON:
          RDFOutput.outputAsRDF(os, "RDF/JSON", resultSet);
          break;
      }
    } catch (Exception e) {
      LOG.error("Error when trying to format the resultset! " + e);
      throw e;
    }
    return new String(os.toByteArray());
  }

  /**
   * Method for testing Keys and Quotas
   *
   * @param request the actual request, containing the necassary information
   * @throws LimitExceededException if user has no quotas left
   * @throws InvalidUserkeyException if the userkey is unknown
   */
  private void keyScan(RequestModel request)
      throws LimitExceededException, InvalidUserkeyException {

    if (apiKeyHandler.getMap().containsKey(request.getKey())) {
      if (apiKeyHandler.getMap().get(request.getKey()).getUserType() != userType.ADMIN) {
      
      if ((apiKeyHandler.getMap().get(request.getKey()).getQuotaDay() <= 0
          || apiKeyHandler.getMap().get(request.getKey()).getQuotaHour() <= 0
          || apiKeyHandler.getMap().get(request.getKey()).getQuotaMinute() <= 0)) {
        if (apiKeyHandler.getMap().get(request.getKey()).getQuotaDay() <= 0) {
          throw new LimitExceededException("per day");
        } else if (apiKeyHandler.getMap().get(request.getKey()).getQuotaHour() <= 0) {
          throw new LimitExceededException("per hour");
        } else if (apiKeyHandler.getMap().get(request.getKey()).getQuotaMinute() <= 0) {
          throw new LimitExceededException("per minute");
        }
      }
      apiKeyHandler.getMap().get(request.getKey()).decrease();
      }
    } else {
      throw new InvalidUserkeyException();
    }
  }

  private void addWindowingToQuery(Query query, Window window) {

    query.setOffset(window.getOffset());
    query.setLimit(window.getLimit());
  }


  /**
   * Generates a pretty JSon-String from the DBpedia-result
   * @param jsonString the DBpedia-response as JSON
   * @param prefixes the prefix-map
   * @param style the Style to be in
   * @return a pretty JSON-String
   */


  public static String prettyJsonParser (String jsonString, PrefixMappingImpl prefixes, Style style) {
    JSONObject jsonObject = new JSONObject(jsonString);
    JSONArray results = jsonObject.getJSONObject("results").getJSONArray("bindings");
    JSONArray vars = jsonObject.getJSONObject("head").getJSONArray("vars");

    for (int i=0;i<results.length(); i++) {
      for (int j=0;j<vars.length();j++) {
        try {
          JSONObject obj = results.getJSONObject(i).getJSONObject(vars.getString(j));
          String target = "";
          if (obj.get("type").equals("uri")) {
            target = "value";
          } else if (obj.get("type").equals("literal")) {
            target = "datatype";
          }

          for (Map.Entry<String, String> entry : prefixes.getNsPrefixMap().entrySet()) {
            String value = entry.getValue();
            if (obj.getString(target).contains(value)) {

              String replaceString;
              if(style == Style.SHORT && dbrOrdbo(value)) {
                replaceString = "";
              } else {
                replaceString = entry.getKey() + ":";
              }
              String newString = obj.getString(target).replace(value, replaceString);
              obj.put(target, newString);
              break;
            }
          }
        } catch (JSONException jsonEx) {

        }
      }
    }
    return jsonObject.toString(2);
  }

  /**
   * Generates a prettier TSV-String from a already pretty JSON-Object
   * @param jsonString String of a pretty JSON-Object
   * @param style the Style to be in
   * @return a pretty TSV-String
   */

  private static String prettyTSVParser (String jsonString, Style style) {
    JSONObject jsonObject = new JSONObject(jsonString);
    JSONArray results = jsonObject.getJSONObject("results").getJSONArray("bindings");
    JSONArray vars = jsonObject.getJSONObject("head").getJSONArray("vars");
    StringBuilder stringBuilder = new StringBuilder();



    for (int i=0;i<vars.length();i++) {
      stringBuilder.append("?"+vars.get(i));
      if (i == vars.length()-1) {
        stringBuilder.append("\n");
      } else {
        stringBuilder.append("\t");
      }
    }

    for (int i=0;i<results.length();i++) {
      for (int j=0;j<vars.length();j++) {
        String entry = "";
        try {
          JSONObject obj = results.getJSONObject(i).getJSONObject(vars.getString(j));

          if (obj.get("type").equals("uri")) {
            if (style == Style.NONE) {
              entry = "<" + obj.getString("value") + ">";
            } else {
              entry =  obj.getString("value");
            }
          } else if (obj.get("type").equals("literal")) {
            try {
              if (style == Style.NONE) {
                entry = "\"" + obj.getString("value") + "\"^^<" + obj.getString("datatype") + ">";
              } else {
                entry = "\"" + obj.getString("value") + "\"^^" + obj.getString("datatype");
              }
            } catch (JSONException jsonEx) {
              try {
                entry = "\"" + obj.getString("value") + "\""+"@\""+obj.getString("xml:lang")+"\"";
              } catch (JSONException jsException) {
                entry = "\"" + obj.getString("value") + "\"";
              }
            }
          }
        } catch (JSONException jEx) {

        }
          stringBuilder.append(entry);
          if (j == vars.length() - 1) {
            stringBuilder.append("\n");
          } else {
            stringBuilder.append("\t");
          }
      }
    }

    return stringBuilder.toString();
  }

  /**
   * Determines if the JSONArray contains the Object with the id already
   * @param jarray Array to search in
   * @param id id to look for
   * @return
   */

  private static int jsonArrayContains (JSONArray jarray, String id) {

    for (int i=0; i<jarray.length(); i++) {
      JSONObject obj = jarray.getJSONObject(i);
      try {
        if (obj.getString("@id").equals(id)) {
          return i;
        }
      } catch (JSONException jEx) {

      }
    }
    return -1;
  }

  /**
   * Determines if the URI is either http://dbpedia.org/ontology/ or http://dbpedia.org/resource/
   * @param uri
   * @return
   */
  private static boolean dbrOrdbo (String uri) {
    if (uri.equals("http://dbpedia.org/ontology/") || uri.equals("http://dbpedia.org/resource/")) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Generates all the nestedJSON results for any query
   * @param object The result from DBpedia SPARQL-Endpoint as JSON
   * @param prefixes the prefix-map for the pretty Strings.
   * @return pretty nestedJSON String
   */

  public static String allMightyNested (JSONObject object, PrefixMappingImpl prefixes) {
    JSONArray result = new JSONArray();
    JSONArray sparqlresults = object.getJSONObject("results").getJSONArray("bindings");
    JSONArray vars = object.getJSONObject("head").getJSONArray("vars");
    ArrayList<String> realVars = new ArrayList<>();
    boolean propVal = false;

    for (int j=0; j<vars.length(); j++) {
      String var = vars.getString(j);

      if (!var.contains("Label") && !var.equals("entities")) {
        realVars.add(var);
      }
    }
    if (realVars.contains("properties") && realVars.contains("values")) {
      propVal = true;
    }

    for (int i=0; i<sparqlresults.length(); i++) {

      JSONObject resultline = sparqlresults.getJSONObject(i);
      String entString = resultline.getJSONObject("entities").getString("value");
      if (entString.contains("http://dbpedia.org/resource/")) {
        entString = entString.replace("http://dbpedia.org/resource/", "");
      }

      int index = jsonArrayContains(result, entString);

      if (index < 0) {
        result.put(new JSONObject().put("@id", entString));
        index = result.length()-1;
      }
      if (propVal) {
        String var = prettyString(resultline.getJSONObject("properties").getString("value"), prefixes);
        if (gotLabel(resultline, var)) {
          try {
            JSONObject obj = new JSONObject().put("@id",
                prettyString(resultline.getJSONObject("values").getString("value"), prefixes))
                .put("label", resultline.getJSONObject(var + "Label").getString("value"));
            try {
              if (!arrayContainsValue(result.getJSONObject(index).getJSONArray(var),
                  obj.toString())) {
                result.getJSONObject(index).append(var, obj);
              }

            } catch (JSONException jEx) {
              result.getJSONObject(index).append(var, obj);
            }
          } catch (JSONException jEx1) {

          }
        } else {
          try {
            String val = prettyString(resultline.getJSONObject("values").getString("value"), prefixes);
            try {
              if (!arrayContainsValue(result.getJSONObject(index).getJSONArray(var), val)) {
                result.getJSONObject(index).append(var, val);
              }
            } catch (JSONException jEx) {
              result.getJSONObject(index).append(var, val);
            }
          } catch (JSONException jEx2) {

          }
        }
      } else {
        for (String var : realVars) {
          if (gotLabel(resultline, var)) {
            try {
              JSONObject obj = new JSONObject().put("@id",
                  prettyString(resultline.getJSONObject(var).getString("value"), prefixes))
                  .put("label", resultline.getJSONObject(var + "Label").getString("value"));
              try {
                if (!arrayContainsValue(result.getJSONObject(index).getJSONArray(var),
                    obj.toString())) {
                  result.getJSONObject(index).append(var, obj);
                }

              } catch (JSONException jEx) {
                result.getJSONObject(index).append(var, obj);
              }
            } catch (JSONException jEx1) {

            }
          } else {
            try {
              String val = prettyString(resultline.getJSONObject(var).getString("value"), prefixes);
              try {
                if (!arrayContainsValue(result.getJSONObject(index).getJSONArray(var), val)) {
                  result.getJSONObject(index).append(var, val);
                }
              } catch (JSONException jEx) {
                result.getJSONObject(index).append(var, val);
              }
            } catch (JSONException jEx2) {

            }
          }
        }
      }
    }
    return result.toString(2);
  }

  /**
   * Determines if the Object got a Label in the SPARQL-Result from DBpedia
   * @param obj the Object to look in
   * @param var the variable to check for the label
   * @return true or false
   */

  public static boolean gotLabel (JSONObject obj, String var) {

    try {
      obj.get(var+"Label");
      return true;
    } catch (JSONException jEx) {
      return false;
    }
  }

  /**
   * Gets the pretty String
   * @param var the String to be pretty
   * @param prefixes the prefix-map
   * @return the pretty String
   */

  public static String prettyString (String var, PrefixMappingImpl prefixes) {
    String result = var;

    if (var.contains("http://dbpedia.org/ontology/")) {
      result = var.replace("http://dbpedia.org/ontology/","");
    } else if (var.contains("http://dbpedia.org/resource/")) {
      result = var.replace("http://dbpedia.org/resource/","");
    } else if (var.contains("http://www.w3.org/2001/XMLSchema#")) {
      result = var.replace("http://www.w3.org/2001/XMLSchema#", "xsd:");
    } else {

      for (Map.Entry<String, String> entry : prefixes.getNsPrefixMap().entrySet()) {
        String value = entry.getValue();
        if (var.contains(value)) {

          String replaceString = entry.getKey() + ":";
          result = var.replace(value, replaceString);
          break;
        }
      }
    }

    return result;
  }

  /**
   * Checks if a JSON-Array already contains value (can be String or a JSONObject)
   * @param jsonArray the JSON-Array to search in
   * @param value the value to search for
   * @return
   */

  public static boolean  arrayContainsValue (JSONArray jsonArray, String value) {

    ObjectMapper mapper = new ObjectMapper();
    boolean result = false;

    JsonNode node;

    try {
      node = mapper.readTree(jsonArray.toString());

    } catch (IOException ioEx) {
      node = null;
    }

    if (node.isArray()) {
      try {
        JsonNode object = mapper.readTree(value);

        for (int i = 0; i < node.size(); i++) {
          if (node.get(i).equals(object)) {
            result = true;
            break;
          }
        }
      } catch (IOException ioEx) {
        for (int i = 0; i < node.size(); i++) {
          String jsonValue = node.get(i).textValue();
          if (jsonValue != null && jsonValue.equals(value)) {
            result = true;
            break;
          }
        }
      }
    }

    return result;
  }
}

