import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import dbpedia.api.configuration.Configuration;
import dbpedia.api.controller.RequestHandlerImplementation;
import java.io.IOException;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class RequestHandlerImplementationTest {



  @Test
  public void getNestedJSONTest () {
    JSONObject json = new JSONObject("{ \"head\": {"
        + "\"vars\": [ \"entities\" , \"child\" , \"childLabel\" , \"gender\" , \"genderLabel\" ] } , "
        + "\"results\": { \"bindings\": [ { "
        + "\"entities\": { \"type\": \"uri\" , \"value\": \"http://dbpedia.org/resource/Donald_Trump\" } , "
        + "\"child\": { \"type\": \"uri\" , \"value\": \"http://dbpedia.org/resource/Donald_Trump_Jr.\" } , "
        + "\"childLabel\": { \"type\": \"literal\" , \"xml:lang\": \"en\" , \"value\": \"Donald Trump Jr.\" } , "
        + "\"gender\": { \"type\": \"literal\" , \"xml:lang\": \"en\" , \"value\": \"male\" } } , { "
        + "\"entities\": { \"type\": \"uri\" , \"value\": \"http://dbpedia.org/resource/Donald_Trump\" } , "
        + "\"child\": { \"type\": \"uri\" , \"value\": \"http://dbpedia.org/resource/Tiffany_Trump\" } , "
        + "\"childLabel\": { \"type\": \"literal\" , \"xml:lang\": \"en\" , \"value\": \"Tiffany Trump\" } , "
        + "\"gender\": { \"type\": \"literal\" , \"xml:lang\": \"en\" , \"value\": \"male\" } } , { "
        + "\"entities\": { \"type\": \"uri\" , \"value\": \"http://dbpedia.org/resource/Donald_Trump\" } , "
        + "\"child\": { \"type\": \"uri\" , \"value\": \"http://dbpedia.org/resource/Eric_Trump\" } , "
        + "\"childLabel\": { \"type\": \"literal\" , \"xml:lang\": \"en\" , \"value\": \"Eric Trump\" } , "
        + "\"gender\": { \"type\": \"literal\" , \"xml:lang\": \"en\" , \"value\": \"male\" } } , { "
        + "\"entities\": { \"type\": \"uri\" , \"value\": \"http://dbpedia.org/resource/Donald_Trump\" } , "
        + "\"child\": { \"type\": \"uri\" , \"value\": \"http://dbpedia.org/resource/Ivanka_Trump\" } , "
        + "\"childLabel\": { \"type\": \"literal\" , \"xml:lang\": \"en\" , \"value\": \"Ivanka Trump\" } , "
        + "\"gender\": { \"type\": \"literal\" , \"xml:lang\": \"en\" , \"value\": \"male\" } } , { "
        + "\"entities\": { \"type\": \"uri\" , \"value\": \"http://dbpedia.org/resource/Barack_Obama\" } , "
        + "\"gender\": { \"type\": \"literal\" , \"xml:lang\": \"en\" , \"value\": \"male\" } } ] } } ");
    JSONArray expected = new JSONArray("[\n"
        + "  {\n"
        + "    \"gender\": [\"male\"],\n"
        + "    \"@id\": \"Donald_Trump\",\n"
        + "    \"child\": [\n"
        + "      {\n"
        + "        \"@id\": \"Donald_Trump_Jr.\",\n"
        + "        \"label\": \"Donald Trump Jr.\"\n"
        + "      },\n"
        + "      {\n"
        + "        \"@id\": \"Tiffany_Trump\",\n"
        + "        \"label\": \"Tiffany Trump\"\n"
        + "      },\n"
        + "      {\n"
        + "        \"@id\": \"Eric_Trump\",\n"
        + "        \"label\": \"Eric Trump\"\n"
        + "      },\n"
        + "      {\n"
        + "        \"@id\": \"Ivanka_Trump\",\n"
        + "        \"label\": \"Ivanka Trump\"\n"
        + "      }\n"
        + "    ]\n"
        + "  },\n"
        + "  {\n"
        + "    \"gender\": [\"male\"],\n"
        + "    \"@id\": \"Barack_Obama\"\n"
        + "  }\n"
        + "]");

    PrefixMappingImpl prefixMap;
    try {
      prefixMap = Configuration.loadPrefixMapping("config/prefixes.json");
    } catch ( IOException ioEx) {
      prefixMap = null;
    }

    assertEquals(expected.toString(2),RequestHandlerImplementation.allMightyNested(json, prefixMap));
  }

  @Test
  public void getEntitiesJSONTest () {
    JSONObject jsonObject = new JSONObject("{ \"head\": { \"link\": [], \"vars\": [\"entities\", \"label\"] },\n"
        + "  \"results\": { \"distinct\": false, \"ordered\": true, \"bindings\": [\n"
        + "    { \"entities\": { \"type\": \"uri\", \"value\": \"http://dbpedia.org/resource/Stephen_King\" }\t,"
        + " \"label\": { \"type\": \"literal\", \"xml:lang\": \"en\", \"value\": \"Stephen King\" }},\n"
        + "    { \"entities\": { \"type\": \"uri\", \"value\": \"http://dbpedia.org/resource/Miroslav_\\u0160ustek\" }\t,"
        + " \"label\": { \"type\": \"literal\", \"xml:lang\": \"en\", \"value\": \"Miroslav \\u0160ustek\" }} ] } }");
    String expected = "[\n"
        + "  {\n"
        + "    \"@id\": \"Stephen_King\",\n"
        + "    \"label\": [\"Stephen King\"]\n"
        + "  },\n"
        + "  {\n"
        + "    \"@id\": \"Miroslav_Šustek\",\n"
        + "    \"label\": [\"Miroslav Šustek\"]\n"
        + "  }\n"
        + "]";

    PrefixMappingImpl prefixMap;
    try {
      prefixMap = Configuration.loadPrefixMapping("config/prefixes.json");
    } catch ( IOException ioEx) {
      prefixMap = null;
    }
    assertEquals(expected,RequestHandlerImplementation.allMightyNested(jsonObject, prefixMap));
  }

  @Test
  public void getPropValNESTEDTest () {
    JSONObject jsonObject = new JSONObject("{ \"head\": { \"link\": [], \"vars\": [\"entities\", \"properties\", \"values\", \"label\"] },\n"
        + "  \"results\": { \"distinct\": false, \"ordered\": true, \"bindings\": [\n"
        + "    { \"entities\": { \"type\": \"uri\", \"value\": \"http://dbpedia.org/resource/St_Loup\" }\t, \"properties\": { \"type\": \"uri\", \"value\": \"http://dbpedia.org/ontology/wikiPageRedirects\" }\t, \"values\": { \"type\": \"uri\", \"value\": \"http://dbpedia.org/resource/Lupus_of_Sens\" }\t, \"label\": { \"type\": \"literal\", \"xml:lang\": \"en\", \"value\": \"Lupus of Sens\" }},\n"
        + "    { \"entities\": { \"type\": \"uri\", \"value\": \"http://dbpedia.org/resource/St_Loup\" }\t, \"properties\": { \"type\": \"uri\", \"value\": \"http://www.w3.org/2000/01/rdf-schema#label\" }\t, \"values\": { \"type\": \"literal\", \"xml:lang\": \"en\", \"value\": \"St Loup\" }},\n"
        + "    { \"entities\": { \"type\": \"uri\", \"value\": \"http://dbpedia.org/resource/St_Loup\" }\t, \"properties\": { \"type\": \"uri\", \"value\": \"http://dbpedia.org/ontology/wikiPageID\" }\t, \"values\": { \"type\": \"typed-literal\", \"datatype\": \"http://www.w3.org/2001/XMLSchema#integer\", \"value\": \"1070303\" }},\n"
        + "    { \"entities\": { \"type\": \"uri\", \"value\": \"http://dbpedia.org/resource/St_Loup\" }\t, \"properties\": { \"type\": \"uri\", \"value\": \"http://dbpedia.org/ontology/wikiPageRevisionID\" }\t, \"values\": { \"type\": \"typed-literal\", \"datatype\": \"http://www.w3.org/2001/XMLSchema#integer\", \"value\": \"140511142\" }},\n"
        + "    { \"entities\": { \"type\": \"uri\", \"value\": \"http://dbpedia.org/resource/St_Loup\" }\t, \"properties\": { \"type\": \"uri\", \"value\": \"http://xmlns.com/foaf/0.1/isPrimaryTopicOf\" }\t, \"values\": { \"type\": \"uri\", \"value\": \"http://en.wikipedia.org/wiki/St_Loup\" }},\n"
        + "    { \"entities\": { \"type\": \"uri\", \"value\": \"http://dbpedia.org/resource/St_Loup\" }\t, \"properties\": { \"type\": \"uri\", \"value\": \"http://www.w3.org/ns/prov#wasDerivedFrom\" }\t, \"values\": { \"type\": \"uri\", \"value\": \"http://en.wikipedia.org/wiki/St_Loup?oldid=140511142\" }} ] } }");

    String expected = "[{\n"
        + "  \"prov:wasDerivedFrom\": [\"wiki:St_Loup?oldid=140511142\"],\n"
        + "  \"wikiPageRedirects\": [\"Lupus_of_Sens\"],\n"
        + "  \"rdfs:label\": [\"St Loup\"],\n"
        + "  \"foaf:isPrimaryTopicOf\": [\"wiki:St_Loup\"],\n"
        + "  \"wikiPageID\": [\"1070303\"],\n"
        + "  \"wikiPageRevisionID\": [\"140511142\"],\n"
        + "  \"@id\": \"St_Loup\"\n"
        + "}]";

    PrefixMappingImpl prefixMap;
    try {
      prefixMap = Configuration.loadPrefixMapping("config/prefixes.json");
    } catch ( IOException ioEx) {
      prefixMap = null;
    }

    assertEquals(expected,RequestHandlerImplementation.allMightyNested(jsonObject, prefixMap));
  }

  @Test
  public void anotherNestedTest () {
    JSONObject source = new JSONObject("{\n"
        + "  \"head\": {\n"
        + "    \"vars\": [\n"
        + "      \"entities\",\n"
        + "      \"orbits\",\n"
        + "      \"launch\"\n"
        + "    ]\n"
        + "  },\n"
        + "  \"results\": {\n"
        + "    \"bindings\": [\n"
        + "      {\n"
        + "        \"entities\": {\n"
        + "          \"type\": \"uri\",\n"
        + "          \"value\": \"http://dbpedia.org/resource/Kosmos_557\"\n"
        + "        },\n"
        + "        \"orbits\": {\n"
        + "          \"type\": \"literal\",\n"
        + "          \"datatype\": \"http://www.w3.org/1999/02/22-rdf-syntax-ns#langString\",\n"
        + "          \"value\": \"~175\"\n"
        + "        },\n"
        + "        \"launch\": {\n"
        + "          \"type\": \"literal\",\n"
        + "          \"datatype\": \"http://www.w3.org/2001/XMLSchema#date\",\n"
        + "          \"value\": \"1973-05-11\"\n"
        + "        }\n"
        + "      },\n"
        + "      {\n"
        + "        \"entities\": {\n"
        + "          \"type\": \"uri\",\n"
        + "          \"value\": \"http://dbpedia.org/resource/BA_2100\"\n"
        + "        }\n"
        + "      },\n"
        + "      {\n"
        + "        \"entities\": {\n"
        + "          \"type\": \"uri\",\n"
        + "          \"value\": \"http://dbpedia.org/resource/Galaxy_(spacecraft)\"\n"
        + "        }\n"
        + "      },\n"
        + "      {\n"
        + "        \"entities\": {\n"
        + "          \"type\": \"uri\",\n"
        + "          \"value\": \"http://dbpedia.org/resource/Bigelow_Commercial_Space_Station\"\n"
        + "        }\n"
        + "      },\n"
        + "      {\n"
        + "        \"entities\": {\n"
        + "          \"type\": \"uri\",\n"
        + "          \"value\": \"http://dbpedia.org/resource/Manned_Orbiting_Laboratory\"\n"
        + "        }\n"
        + "      }\n"
        + "    ]\n"
        + "  }\n"
        + "}");

    JSONArray expected = new JSONArray("[\n"
        + "  {\n"
        + "    \"orbits\": [\"~175\"],\n"
        + "    \"launch\": [\"1973-05-11\"],\n"
        + "    \"@id\": \"Kosmos_557\"\n"
        + "  },\n"
        + "  {\"@id\": \"BA_2100\"},\n"
        + "  {\"@id\": \"Galaxy_(spacecraft)\"},\n"
        + "  {\"@id\": \"Bigelow_Commercial_Space_Station\"},\n"
        + "  {\"@id\": \"Manned_Orbiting_Laboratory\"}\n"
        + "]");
    PrefixMappingImpl prefixMap;
    try {
       prefixMap = Configuration.loadPrefixMapping("config/prefixes.json");
    } catch ( IOException ioEx) {
      prefixMap = null;
    }

    assertEquals(expected.toString(2), RequestHandlerImplementation.allMightyNested(source, prefixMap));
  }
}
