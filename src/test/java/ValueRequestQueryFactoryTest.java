import static org.junit.Assert.assertEquals;

import dbpedia.api.configuration.Configuration;
import dbpedia.api.factory.ValueRequestQueryFactory;
import dbpedia.api.model.RequestModel.Style;
import dbpedia.api.model.ResourceModel;
import dbpedia.api.model.ValueRequestModel;
import dbpedia.api.model.ValueRequestModel.Builder;
import java.io.IOException;
import java.util.HashSet;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.junit.Test;

public class ValueRequestQueryFactoryTest {

  ValueRequestQueryFactory factory = new ValueRequestQueryFactory(
      Configuration.createOntModel("config/dbpedia_2016-10.owl"),
      Configuration.loadPrefixMapping("config/prefixes.json"));
   //ParameterizedSparqlString prefix = factory.getPrefix();

  public ValueRequestQueryFactoryTest() throws IOException {
  }
  @Test
  public void getValueQueryTest(){
	  HashSet<String> entities = new HashSet<>();
	  entities.add("Donald_Trump");
	  entities.add("Hillary_Clinton");
	  HashSet<ResourceModel> props = new HashSet<>();
	  props.add(new ResourceModel("dbo", "parent"));
	  props.add(new ResourceModel("dbo", "child"));

	  ValueRequestModel model = new ValueRequestModel(entities, props, true);
	  Query query = factory.getValueQuery(model);
  }


  @Test
  public void LF0041Test() {
    HashSet<String> entities = new HashSet<>();
    entities.add("Donald_Trump");
    entities.add("Hillary_Clinton");
    HashSet<ResourceModel> props = new HashSet<>();
    props.add(new ResourceModel("dbo", "parent"));
    props.add(new ResourceModel("dbo", "child"));

    ValueRequestModel model = new ValueRequestModel(entities, props, false);
    Query query = factory.getValueQuery(model);
    ParameterizedSparqlString expected = new ParameterizedSparqlString();
    //expected.append(prefix);
    expected.append("PREFIX dbo: <http://dbpedia.org/ontology/>\n"
    	+"PREFIX dbr: <http://dbpedia.org/resource/>\n"
      +"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
      +"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n");
    expected.append("SELECT DISTINCT  ?entities ?dbochild ?dboparent\n"
        + "WHERE\n"
        + "  { VALUES ?entities { dbr:Donald_Trump dbr:Hillary_Clinton }\n"
        + "    OPTIONAL\n"
        + "      { ?entities  dbo:child  ?dbochild }\n"
        + "    OPTIONAL\n"
        + "      { ?entities  dbo:parent  ?dboparent }\n"
        + "  }\n");

    assertEquals(QueryFactory.create(expected.asQuery()), query);

  }

  @Test
  public void LF0042Test() {
    HashSet<String> entities1 = new HashSet<>();
    entities1.add("Donald_Trump");
    entities1.add("Hillary_Clinton");

    ValueRequestModel model1 = new ValueRequestModel(entities1, new HashSet<ResourceModel>(),false);
    Query query1 = factory.getValueQuery(model1);
    ParameterizedSparqlString expected1 = new ParameterizedSparqlString();
    //expected1.append(prefix);
    expected1.append("PREFIX dbo: <http://dbpedia.org/ontology/>\n"
        +"PREFIX dbr: <http://dbpedia.org/resource/>\n"
        +"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
        +"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n");
    expected1.append("SELECT DISTINCT  ?entities ?properties ?values\n"
        + "WHERE\n"
        + "  { VALUES ?entities { dbr:Donald_Trump dbr:Hillary_Clinton }\n"
        + "    ?entities  ?properties  ?values\n"
        + "    OPTIONAL\n"
        + "      { ?values  rdfs:label  ?valuesLabel\n"
        + "        FILTER ( lang(?valuesLabel) = \"en\" )\n"
        + "      }\n"
        + "  }");

    assertEquals(QueryFactory.create(expected1.asQuery()), query1);
  }

  @Test
  public void nestedValueRequestTest () {
    HashSet<String> entities = new HashSet<>();
    entities.add("Donald_Trump");
    entities.add("Hillary_Clinton");
    HashSet<ResourceModel> props = new HashSet<>();
    props.add(new ResourceModel("dbo", "parent"));
    props.add(new ResourceModel("dbo", "child"));

    ValueRequestModel.Builder builder = new Builder();
    ValueRequestModel model1 = builder.setEntities(entities).setMapping(false).setStyle(
        Style.NESTED).setProperties(props).setVersion("1.2.1").build();
    Query query1 = factory.getValueQuery(model1);
    ParameterizedSparqlString expected = new ParameterizedSparqlString();
    expected.append("PREFIX dbo: <http://dbpedia.org/ontology/>\n"
        +"PREFIX dbr: <http://dbpedia.org/resource/>\n"
        +"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
        +"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n");
    expected.append("SELECT DISTINCT  ?entities ?dbochild ?dbochildLabel ?dboparent ?dboparentLabel\n"
        + "WHERE\n"
        + "  { VALUES ?entities { dbr:Donald_Trump dbr:Hillary_Clinton }\n"
        + "    OPTIONAL\n"
        + "      { ?entities  dbo:child  ?dbochild\n"
        + "        OPTIONAL\n"
        + "          { ?dbochild  rdfs:label  ?dbochildLabel\n"
        + "            FILTER ( lang(?dbochildLabel) = \"en\" )\n"
        + "          }\n"
        + "      }\n"
        + "    OPTIONAL\n"
        + "      { ?entities  dbo:parent  ?dboparent\n"
        + "        OPTIONAL\n"
        + "          { ?dboparent  rdfs:label  ?dboparentLabel\n"
        + "            FILTER ( lang(?dboparentLabel) = \"en\" )\n"
        + "          }\n"
        + "      }\n"
        + "  }\n");
    assertEquals(QueryFactory.create(expected.asQuery()),query1);
  }
}
