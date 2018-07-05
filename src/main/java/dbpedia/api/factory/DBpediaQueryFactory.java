package dbpedia.api.factory;


import dbpedia.api.model.Filter;
import dbpedia.api.model.RequestModel;
import dbpedia.api.model.RequestModel.Style;
import java.util.ArrayList;
import java.util.List;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.springframework.core.GenericTypeResolver;
import org.springframework.stereotype.Component;

@Component
public abstract class DBpediaQueryFactory<T extends RequestModel> {

  private final Class<T> genericType;
  protected OntModel ontology;
  protected ParameterizedSparqlString prefix;
  protected final PrefixMappingImpl prefixMap;

  public DBpediaQueryFactory(OntModel ontology, PrefixMappingImpl prefixMapping) {
    this.ontology = ontology;
    //this is needed because java cannot determine the class of a generic type at runtime
    //only spring can do this
    this.genericType = (Class<T>) GenericTypeResolver
        .resolveTypeArgument(getClass(), DBpediaQueryFactory.class);
    this.prefixMap = prefixMapping;
  }

  /**
   * generates the "SELECT ?a, ?b, ?c... WHERE {" part
   *
   * @param returnValues The ?a, ?b, ?c... variables
   * @return The correct Select statement and beginning of the "where-clause"as SparqlString
   */

  protected ParameterizedSparqlString queryHead(List<String> returnValues) {
    return queryHead(new ParameterizedSparqlString(), returnValues);
  }

  protected ParameterizedSparqlString queryHead(ParameterizedSparqlString builder,
      List<String> returnValues) {
    builder.append("SELECT distinct");
    for (String value : returnValues) {
      builder.append(" ?" + value + " ");
    }
    builder.append("WHERE {");
    return builder;
  }

  protected ParameterizedSparqlString queryShoulder(ParameterizedSparqlString shoulder,
      List<String> entities) {
    shoulder.append("VALUES ?entities {");
    for (String entity : entities) {
      shoulder.append(" " + entity);
    }
    shoulder.append("}");
    return shoulder;
  }

  /**
   * looks in the ontology to get the range of a DatatypeProperty
   **/
  protected RDFDatatype getRdfDataType(String property) {
    DatatypeProperty prop = ontology.getDatatypeProperty("http://dbpedia.org/ontology/" + property);
    if (prop == null) {
      return null;
    } else {
      OntResource range = prop.getRange();
      return NodeFactory.getType(range.toString());
    }
  }

  abstract public Query makeQuery(T model);

  public Class<T> getGenericType() {
    return genericType;
  }

  /**
   * Defines the prefixes which are needed in a lot of queries.
   *
   * @param sparqlString The main String which contains the prefix definition
   */
  protected final void setDefaultPrefixes(ParameterizedSparqlString sparqlString) {
    sparqlString.setNsPrefix("rdfs", prefixMap.getNsPrefixURI("rdfs"));
    sparqlString.setNsPrefix("rdf", prefixMap.getNsPrefixURI("rdf"));
    sparqlString.setNsPrefix("dbo", prefixMap.getNsPrefixURI("dbo"));
    sparqlString.setNsPrefix("dbr", prefixMap.getNsPrefixURI("dbr"));
  }

  public PrefixMappingImpl getPrefixMap() {
    return this.prefixMap;
  }

  /**
   * Returns the RDF-triple for a specific filter, and adds the returnvalues if needed
   */


}