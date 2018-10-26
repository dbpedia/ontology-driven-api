package dbpedia.api.factory;

import dbpedia.api.model.RequestModel.Style;
import dbpedia.api.model.ResourceModel;
import dbpedia.api.model.ValueRequestModel;
import java.util.ArrayList;
import java.util.List;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.springframework.stereotype.Component;

@Component
public class ValueRequestQueryFactory extends DBpediaQueryFactory<ValueRequestModel> {

  public ValueRequestQueryFactory(OntModel ontology, PrefixMappingImpl prefixMapping) {
    super(ontology, prefixMapping);
  }


  @Override
  public Query makeQuery(ValueRequestModel requestModel) {
    return getValueQuery(requestModel);
  }

  /**
   * <p>Generates all value-queries</p> <p> LF0040 -> should be clear <br> LF0041 -> everything
   * should be filled <br> LF0042 -> model.getProperties should be empty <br> LF0043 -> MappingBased
   * must be true <br> </p>
   */

  public Query getValueQuery(ValueRequestModel model) {
    ParameterizedSparqlString sparqlString = new ParameterizedSparqlString();
    ParameterizedSparqlString valuesString = new ParameterizedSparqlString();
    ParameterizedSparqlString tripleString = new ParameterizedSparqlString();
    List<String> returnvalues = new ArrayList<>();

    for (ResourceModel prop : model.getProperties()) {
      String uri = prefixMap.getNsPrefixURI(prop.getPrefix());
      if (uri == null) {
        throw new IllegalArgumentException("Prefix " + prop.getPrefix() + " not found!");
      } else {
        sparqlString.setNsPrefix(prop.getPrefix(), prefixMap.getNsPrefixURI(prop.getPrefix()));
      }
    }
    valuesString.append("VALUES ?entities {");

    returnvalues.add("entities");

    if (!model.getProperties().isEmpty()) {
      for (ResourceModel prop : model.getProperties()) {
        returnvalues.add(prop.getPrefix()+prop.getIdentifier());
        String propname = prop.getPrefix()+prop.getIdentifier();

        tripleString
            .append("OPTIONAL {\n ?entities " + getUnprefixedString(prop) + " ?" + propname + ".\n");
        if (model.getStyle() == Style.NESTED) {
          tripleString.append("OPTIONAL {?" + propname + " rdfs:label ?" + propname + "Label.\n"
              + "FILTER (LANG(?" + propname + "Label)=\"en\")}");
          returnvalues.add(propname + "Label");
        }
        tripleString.append("}");
        String uri = prefixMap.getNsPrefixURI(prop.getPrefix());
        if (uri == null) {
          throw new IllegalArgumentException("Prefix " + prop.getPrefix() + " not found!");
        } else {
          sparqlString.setNsPrefix(prop.getPrefix(), prefixMap.getNsPrefixURI(prop.getPrefix()));
        }
      }
    } else {
      tripleString.append("?entities ?properties ?values.\n");
      returnvalues.add("properties");
      returnvalues.add("values");
      tripleString.append("OPTIONAL {\n?values rdfs:label ?valuesLabel.\n"
          + "FILTER ( lang(?valuesLabel) = \"en\" )}");
    }

    for (String entity : model.getEntities()) {
      valuesString.append(" <http://dbpedia.org/resource/" + entity+">");
      sparqlString.setNsPrefix("dbr", prefixMap.getNsPrefixURI("dbr"));
      //model.
    }
    valuesString.append("}");

    setDefaultPrefixes(sparqlString);
    sparqlString.setCommandText(
        queryHead(returnvalues) + "\n" + valuesString + "\n" + tripleString + "\n}");
    return QueryFactory.create(sparqlString.asQuery());
  }
}

