package dbpedia.api.factory;

import dbpedia.api.model.EntityRequestModel;
import dbpedia.api.model.Filter;
import dbpedia.api.model.RequestModel.Style;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.springframework.stereotype.Component;

@Component
public class EntityRequestQueryFactory extends DBpediaQueryFactory<EntityRequestModel> {

  public EntityRequestQueryFactory(OntModel ontology, PrefixMappingImpl prefixMapping) {
    super(ontology, prefixMapping);
  }


  @Override
  public Query makeQuery(EntityRequestModel model) {

    return getEntitiesQuery(model);
  }

  /**
   * Gets the whole String which can be appended in the filterString, like str(?label)=str("Moon")
   */
  private String getFullFilterstring(Filter filter) {
    String fullValue;
    RDFDatatype type;
    String propname = filter.getFilterProp().getPrefix() + filter.getFilterProp().getIdentifier();

    if (!filter.getFilterOp().equals("startswith")) {
      if (getRdfDataType(filter.getFilterProp().getIdentifier()) == null) {

        fullValue =
            "str( ?" + propname + " ) " + filter.getFilterOp() + " str(\""
                + filter.getFilterVal() + "\")";

      } else {
        type = getRdfDataType(filter.getFilterProp().getIdentifier());
        Literal filterLiteral = ResourceFactory.createTypedLiteral(
            filter.getFilterVal(),
            type);
        String[] splittedLiteral = filterLiteral.toString().split("\\^\\^");
        fullValue = "?" + propname + " " + filter.getFilterOp() + "\""
            + splittedLiteral[0] + "\"^^<" + splittedLiteral[1] + ">";

      }
    } else {
      fullValue = "STRSTARTS ( str( ?"+filter.getFilterProp().getIdentifier()+" ), \""+filter.getFilterVal()+"\" )";
    }
    return fullValue;
  }

  /**
   * <p> Generates all EntitiesQueries and Instance-Queries If you want to filter for sth filterOP
   * MUST be != null If you want all Properties, the ResourceModel in Filter MUST BE null </p> <p>
   * LF0050 -> filter.getProperty must be null <br> LF0051 -> filterValue should be the Entity,
   * dataType should be dbr <br> LF0060 -> filterValue should be the class, dataType=class <br>
   * LF0061 -> filterValue should be empty (exept you want to filter for sth) <br> LF0062 ->
   * filterValue should be the Value for what you want to filter, e.g. "1966-03-22" <br> LF0063 ->
   * get the important Things <br> LF0070 -> just combine 50 and 60 <br> </p>
   */

  public Query getEntitiesQuery(EntityRequestModel model) {
    ParameterizedSparqlString sparqlString = new ParameterizedSparqlString();
    ParameterizedSparqlString reqString = new ParameterizedSparqlString();
    ParameterizedSparqlString optionalString = new ParameterizedSparqlString();
    ParameterizedSparqlString reqFilterString = new ParameterizedSparqlString();
    ArrayList<String> returnvalues = new ArrayList<>();

    returnvalues.add("entities");

    setDefaultPrefixes(sparqlString);
    List<Filter> filterList = sortFilterSet(model.getFilterList());
    for (Filter filter : filterList) {
      if (filter.getFilterProp() != null) {
        sparqlString.setNsPrefix(filter.getFilterProp().getPrefix(),
            prefixMap.getNsPrefixURI(filter.getFilterProp().getPrefix()));
      }
    }

    boolean filterInit = false;
    boolean reqOptionalLabel = false;

    if (model.getClassName() != null) {
      reqString.append(" ?entities rdf:type dbo:" + model.getClassName() + ".\n");
    }

    for (Filter filter : filterList) {
      String propname = "";
      if (!(filter.getFilterProp() == null)) {
        propname =
            filter.getFilterProp().getPrefix() + filter.getFilterProp().getIdentifier();
      }

      switch (filter.getBool()) {
        case AND:
          reqString.append(getTripleString(filter, returnvalues)+"\n");
          if (filter.getFilterOp() != null) {
            if (filterInit) {
              reqFilterString.append("&& " + getFullFilterstring(filter));
            } else {
              reqFilterString.append("\nFILTER (" + getFullFilterstring(filter));
              filterInit = true;
            }
          }
          if (model.getStyle() == Style.NESTED && (filter.getFilterVal() == null || (!(filter.getFilterVal()==null) && !(filter.getFilterOp() == null)))) {
            optionalString.append("\nOPTIONAL {\n?"+propname + " rdfs:label ?" + propname+"Label.\nFILTER ( lang( ?"+propname+"Label ) = \"en\" )}");
            returnvalues.add(propname+"Label");
          }
          break;
        case OR:
          String sparqlOP = "OPTIONAL";
          optionalString
              .append(sparqlOP + " { " + getTripleString(filter, returnvalues));

          if (model.getStyle() == Style.NESTED && (filter.getFilterVal() == null || (!(filter.getFilterVal()==null) && !(filter.getFilterOp() == null)))) {
            optionalString.append("\nOPTIONAL {\n?"+ propname + " rdfs:label ?" + propname+"Label.\nFILTER ( lang( ?"+propname+"Label ) = \"en\" )}");
            returnvalues.add(propname+"Label");
          }
          optionalString.append("}");
          if (filter.getFilterOp() != null) {
            optionalString.append("\nFILTER ( "+ "bound(?"+ propname+")=false || bound(?"+ propname+
                ")=true &&"+getFullFilterstring(filter) + " ) \n");
          }
          break;
      }
    }
    sparqlString.setNsPrefix("dbr", prefixMap.getNsPrefixURI("dbr"));
    sparqlString.setNsPrefix("dbo", prefixMap.getNsPrefixURI("dbo"));

    sparqlString.append(queryHead(returnvalues));
    sparqlString.append(" \n");
    sparqlString.append(reqString);
    if (filterInit) {
      reqFilterString.append(" )\n ");
      sparqlString.append(reqFilterString);
    }
    sparqlString.append(optionalString);

    sparqlString.append(" } ");

    return QueryFactory.create(sparqlString.asQuery());
  }



  /**
   * Sorts the Filterset so that getEntitiesQuery() works properly
   */

  private List<Filter> sortFilterSet(Set<Filter> filterSet) {
    ArrayList<Filter> sortedList = new ArrayList<>(filterSet);
    java.util.Collections.sort(sortedList);
    return sortedList;
  }

  /**
   * Returns the SPARQL-Triple from a Filter
   * @param filter
   * @param returnvalues
   * @return
   */
  public static String getTripleString(Filter filter, ArrayList<String> returnvalues) {
    String result;

    if (filter.getFilterProp() == null) {
      result = "?entities ?properties dbr:" + filter.getFilterVal() + ".\n";
      returnvalues.add("properties");

    } else {
      if (filter.getFilterOp() == null && filter.getFilterVal() != null) {
        result = "\n?entities " + filter.getFilterProp().toString() + " dbr:" + filter
            .getFilterVal()
            + ".";
      } else {
        String propname = filter.getFilterProp().getPrefix() + filter.getFilterProp().getIdentifier();
        result = "?entities " + filter.getFilterProp().toString() + " ?" + propname + ".";
        returnvalues.add(propname);

      }
    }
    return result;
  }
}


