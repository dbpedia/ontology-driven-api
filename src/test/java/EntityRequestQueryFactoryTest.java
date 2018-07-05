import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import dbpedia.api.configuration.Configuration;
import dbpedia.api.factory.EntityRequestQueryFactory;
import dbpedia.api.model.EntityRequestModel;
import dbpedia.api.model.EntityRequestModel.Builder;
import dbpedia.api.model.Filter;
import dbpedia.api.model.Filter.Bool;
import dbpedia.api.model.RequestModel.ReturnFormat;
import dbpedia.api.model.RequestModel.Style;
import dbpedia.api.model.ResourceModel;
import java.io.IOException;
import java.text.Format;
import java.util.HashSet;
import java.util.Set;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.junit.Ignore;
import org.junit.Test;

public class EntityRequestQueryFactoryTest {


  EntityRequestQueryFactory factory = new EntityRequestQueryFactory(

      Configuration.createOntModel("config/dbpedia_2016-10.owl"),
      Configuration.loadPrefixMapping("config/prefixes.json"));

  public EntityRequestQueryFactoryTest() throws IOException {
  }

  @Test
  public void LF0050Test() {

    Set<Filter> filters4 = new HashSet<>();
    filters4.add(new Filter(
        null,
        "Barack_Obama",
        null,
        Bool.AND));

    Query query4 = factory.getEntitiesQuery(
        new EntityRequestModel(filters4, false, null));

    ParameterizedSparqlString expected4 = new ParameterizedSparqlString();
    //expected4.append(prefix);
    expected4.setNsPrefix("dbo", "http://dbpedia.org/ontology/");
    expected4.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
    expected4.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    expected4.setNsPrefix("dbr", "http://dbpedia.org/resource/");
    expected4.append("\n"
        + "SELECT DISTINCT  ?entities ?properties\n"
        + "WHERE\n"
        + "  { ?entities  ?properties  dbr:Barack_Obama }");
    assertEquals(QueryFactory.create(expected4.asQuery()), query4);
  }


  @Test
  public void LF0051Test() {
    Set<Filter> filters1 = new HashSet<>();

    filters1.add(new Filter(
        new ResourceModel("dbp", "nominee"),
        "Hillary_Clinton",
        null,
        Bool.OR));

    filters1.add(new Filter(
        new ResourceModel("dbp", "nominee"),
        "Donald_Trump",
        null,
        Bool.OR));

    Query query1 = factory.getEntitiesQuery(
        new EntityRequestModel(filters1, false, null));

    ParameterizedSparqlString expected3 = new ParameterizedSparqlString();

    expected3.setNsPrefix("dbo", "http://dbpedia.org/ontology/");
    expected3.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    expected3.setNsPrefix("dbp", "http://dbpedia.org/property/");
    expected3.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
    expected3.setNsPrefix("dbr", "http://dbpedia.org/resource/");

    expected3.append("SELECT DISTINCT  ?entities\n"
        + "WHERE\n"
        + "  { OPTIONAL\n"
        + "      { ?entities  dbp:nominee  dbr:Hillary_Clinton }\n"
        + "    OPTIONAL\n"
        + "      { ?entities  dbp:nominee  dbr:Donald_Trump }\n"
        + "  }");

    assertEquals(QueryFactory.create(expected3.asQuery()), query1);
  }

  @Test
  public void LF0060Test() {

    Query query3 = factory.getEntitiesQuery(
        new EntityRequestModel(new HashSet<>(), false, "Person"));

    ParameterizedSparqlString expected3 = new ParameterizedSparqlString();

    expected3.setNsPrefix("dbo", "http://dbpedia.org/ontology/");
    expected3.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    expected3.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
    expected3.setNsPrefix("dbr", "http://dbpedia.org/resource/");

    expected3.append("SELECT DISTINCT  ?entities\n"
        + "WHERE\n"
        + "  { ?entities  rdf:type  dbo:Person }");

    assertEquals(QueryFactory.create(expected3.asQuery()), query3);
  }

  @Test
  public void LF0061Test() {
    Set<Filter> filters2 = new HashSet<>();

    filters2.add(new Filter(
        new ResourceModel("dbo", "birthDate"),
        null,
        null,
        Bool.OR));

    Query query2 = factory.getEntitiesQuery(
        new EntityRequestModel(filters2,
            false,
            "Person"));

    ParameterizedSparqlString expected2 = new ParameterizedSparqlString();

    expected2.setNsPrefix("dbo", "http://dbpedia.org/ontology/");
    expected2.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    expected2.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
    expected2.setNsPrefix("dbr", "http://dbpedia.org/resource/");

    expected2.append("SELECT DISTINCT  ?entities ?dbobirthDate\n"
        + "WHERE\n"
        + "  { ?entities  rdf:type  dbo:Person\n"
        + "    OPTIONAL\n"
        + "      { ?entities  dbo:birthDate  ?dbobirthDate }\n"
        + "  }");

    assertEquals(QueryFactory.create(expected2.asQuery()), query2);
  }

  @Test
  public void LF0062Test() {
    Set<Filter> filters2 = new HashSet<>();

    filters2.add(new Filter(
        new ResourceModel("dbp", "type"),
        "author",
        "=",
        Bool.OR));

    filters2.add(new Filter(
        new ResourceModel("dbo", "birthDate"),
        "1947-09-21",
        "=",
        Bool.AND));

    Query query2 = factory.getEntitiesQuery(
        new EntityRequestModel(filters2, false, null));

    ParameterizedSparqlString expected1 = new ParameterizedSparqlString();
    //expected1.append(prefix);
    expected1.setNsPrefix("dbo", "http://dbpedia.org/ontology/");
    expected1.setNsPrefix("dbp", "http://dbpedia.org/property/");
    expected1.setNsPrefix("dbr", "http://dbpedia.org/resource/");
    expected1.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    expected1.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");

    expected1.append("SELECT DISTINCT  ?entities ?dbobirthDate ?dbptype\n"
        + "WHERE\n"
        + "  { ?entities  dbo:birthDate  ?dbobirthDate\n"
        + "    FILTER ( ?dbobirthDate = \"1947-09-21\"^^<http://www.w3.org/2001/XMLSchema#date> )\n"
        + "    OPTIONAL\n"
        + "      { ?entities  dbp:type  ?dbptype }\n"
        + "    FILTER ( ( bound(?dbptype) = false ) || ( ( bound(?dbptype) = true ) && ( str(?dbptype) = str(\"author\") ) ) )\n"
        + "  }");

    assertEquals(QueryFactory.create(expected1.asQuery()), query2);

  }

  @Test
  public void fixTest () {
    Set<Filter> filters2 = new HashSet<>();

    filters2.add(new Filter(new ResourceModel("dbo", "starring"),
        "Terence_Hill", null, Bool.AND));
    filters2
        .add(new Filter(new ResourceModel("dbo", "starring"),
            "Bud_Spencer", null, Bool.AND));

    filters2
        .add(new Filter(new ResourceModel("dbo", "birthDate"),
            "1980-01-01", "<", Bool.OR));

    EntityRequestModel.Builder builder = new Builder();
    EntityRequestModel model = builder.setStyle(Style.PREFIXED).setFilterList(filters2).
        setFormat(ReturnFormat.TSV).setonlyImportant(false).setVersion("1.2.1").build();

    Query query2 = factory.getEntitiesQuery(model);

    ParameterizedSparqlString expected1 = new ParameterizedSparqlString();
    expected1.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
    expected1.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    expected1.setNsPrefix("dbo", "http://dbpedia.org/ontology/");
    expected1.setNsPrefix("dbr", "http://dbpedia.org/resource/");
    expected1.append("SELECT DISTINCT  ?entities ?dbobirthDate\n"
        + "WHERE\n"
        + "  { ?entities  dbo:starring  dbr:Terence_Hill ;\n"
        + "              dbo:starring  dbr:Bud_Spencer\n"
        + "    OPTIONAL\n"
        + "      { ?entities  dbo:birthDate  ?dbobirthDate }\n"
        + "    FILTER ( ( bound(?dbobirthDate) = false ) || ( ( bound(?dbobirthDate) = true ) && ( ?dbobirthDate < \"1980-01-01\"^^<http://www.w3.org/2001/XMLSchema#date> ) ) )\n"
        + "  }\n");

    assertEquals(QueryFactory.create(expected1.asQuery()), query2);
  }

  @Test
  public void nestedQuerytest () {
    Set<Filter> filters2 = new HashSet<>();

    filters2.add(new Filter(new ResourceModel("dbo", "child"),
        null, null, Bool.OR));


    EntityRequestModel.Builder builder = new Builder();
    EntityRequestModel model = builder.setStyle(Style.NESTED).setFilterList(filters2).
        setFormat(ReturnFormat.JSON).setonlyImportant(false).setVersion("1.2.1").setClassname("Person").build();

    ParameterizedSparqlString expected1 = new ParameterizedSparqlString();
    expected1.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
    expected1.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    expected1.setNsPrefix("dbo", "http://dbpedia.org/ontology/");
    expected1.setNsPrefix("dbr", "http://dbpedia.org/resource/");
    expected1.append("SELECT DISTINCT  ?entities ?dbochild ?dbochildLabel\n"
        + "WHERE\n"
        + "  { ?entities  rdf:type  dbo:Person\n"
        + "    OPTIONAL\n"
        + "      { ?entities  dbo:child  ?dbochild\n"
        + "        OPTIONAL\n"
        + "          { ?dbochild  rdfs:label  ?dbochildLabel\n"
        + "            FILTER ( lang(?dbochildLabel) = \"en\" )\n"
        + "          }\n"
        + "      }\n"
        + "  }");

    Query query2 = factory.getEntitiesQuery(model);
    assertEquals(QueryFactory.create(expected1.asQuery()), query2);
  }

}
