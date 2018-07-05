package dbpedia.api.configuration;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConf {

  
  private String infoText = null;
  
  @Bean
  public Docket api() {
    return new Docket(DocumentationType.SWAGGER_2)
        .select()
        .apis(RequestHandlerSelectors.basePackage("dbpedia.api.controller"))
        .paths(PathSelectors.any())
        .build()
        .apiInfo(apiInfo());
  }


  private void infoTexter() {
    
    URL formats = null;   
    try {
      formats = new URL( "https://jena.apache.org/documentation/io/#formats");
    } catch (MalformedURLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
   
    infoText = "DBpedia is one of the largest, open and freely accessible knowledge graphs about "
        + "entities (such as movies, actors, athletes, politicians, places, ...) of our everyday "
        + "life. Information contained in Wikipedia articles are extracted into DBpedia as RDF, "
        + "using mappings between Wikipedia infoboxes and the DBpedia Ontology to become accessible "
        + "for complex requests. \n"
        + "\n"
        + "This API provides a REST-conform interface. The API uses the DBpedia Ontology and "
        + "transforms HTTP requests into SPARQL queries, which are then sent to the DBpedia "
        + "endpoint (https://dbpedia.org/sparql). The results can be returned in a range of formats, "
        + "such as JSON, JSON-LD, TSV, and different styles (that means more or less human readable,"
        + " e.g. nested JSON).\n\n"

        + "There are three requests types: "
        + "<br><b> Value</b><br> Say you're interested in the properties of an entity that is represented "
        + "as a DBpedia resource: you can query either all the properties that are stored in the "
        + "DBpedia for this entity or you can ask for a specific property that you have in mind "
        + "(and that exists in the DBpedia). "
        + "In any case you will get the values of the properties (that is, the object in RDF terms). "
        + "A value can be either a literal (e.g. a number) or another entity. For example, you"
        + " can just give the entity 'Barack_Obama' and will get all the properties, or, "
        + "additionally to 'Barack_Obama', you ask for the property 'gender' and will get the value 'male'. "
        + "<br> <b>Entity</b><br> Similar to the values request, but the other way around: here, you're "
        + "interested in all the entities, that share the value of the property that you have at hand. "
        + "For example you ask for all the events, where Barack_Obama was 'nominee'."
        + "<br> <b>Instance</b><br> Here, you're interested in the instances of an ontology class,"
        + " e.g. 'Actor' or 'Company'. You can filter those instances by certain criteria. For example, "
        + "you can ask for all the instances of the class 'Company' that have more than 48000 employees. <br>"
        + "<br>"

        + "We also provide different output and prettyfication options:<br><br>"
        + "<b>Returnformats</b>:<br>"
        + "-JSON (default)<br>"
        + "-JSONLD<br>"
        + "-TSV<br>"
        + "-RDFJSON<br>"
        + "-TURTLE<br>"
        + "-NTRIPLES<br>"
        + "-RDFXML<br>"
        + " For more information about the different formats, visit " + formats +"<br><br>"

        + "<b>Prettyfications</b>(only for TSV and JSON):<br>"
        + "-PREFIXED replaces the DBpediaUris of the objects with their prefixes<br>"
        + "-NESTED every entity has its own JSON-Object with all the related properties. Only works with JSON.<br> "
        + "-SHORT no prefixes or DBpedia-URIs for http://dbpedia.org/ontology/ and http://dbpedia.org/resource/, everything else is prefixed <br>"
        + "-NONE (default) no prettyfication <br><br>"
        + "<b>Note:<br></b> You need a proper APIKey to use this service (contact the Admin)<br><br>"

        + "You can explore all three request types below. Note that there are query parameters "
        + "that occur in every request type (such as key, version, format, pretty, limit, offset), "
        + "and some that differ between the request types. <br><br>"

        + "Use the http://dbpedia.org/page/ pages for information about "
        + "which entries exist for properties in DBpedia (e.g. http://dbpedia.org/page/Space_Station "
        + "for the resource 'http://dbpedia.org/resource/Space_Station').";

  }
  
  private ApiInfo apiInfo() {

    infoTexter();
    return new ApiInfo(
        "Rest API for DBpedia",
        infoText,
        "1.0.0",
        "",
        new Contact("jf17a, students group at Universität Leipzig", "http://pcai042.informatik.uni-leipzig.de/~jf17a/",
            ""),
        "",
        "", Collections.emptyList()
    );
  }

}