package dbpedia.api.configuration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dbpedia.api.factory.DBpediaQueryFactory;
import dbpedia.api.model.ApiVersion;
import dbpedia.api.model.RequestModel;
import dbpedia.api.versioning.SemanticVersionSet;
import dbpedia.api.versioning.VersionLoader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@org.springframework.context.annotation.Configuration
@EnableCaching
@Component
public class Configuration {

  private static final Logger LOG = LogManager.getLogger(Configuration.class.getName());

  @Value("${ontology.file}")
  private String ontologyFile;

  @Value("${keys.file}")
  private String apiKeyFile;

  @Value("${window.maxWindowLimit}")
  public int maxWindowLimit;

  public int getMaxWindowLimit() {
    return maxWindowLimit;
  }

  public String getApiKeyFileName() {
    return apiKeyFile;
  }


  @Value("${keys.usingKeys}")
  private boolean usingKeys;

  @Bean()
  public boolean getUsingKeys() {
    return usingKeys;
  }

  @Value("${prefixes.file}")
  private String prefixesFile;

  @Value("${versions.dir}")
  private String versionsDir;

  @Value("${keys.startQuotaDay}")
  private int startQuotaDay;

  @Bean(name = "startQuotaDay")
  public int getStartQuotaDay() {
    return startQuotaDay;
  }

  @Value("${keys.startQuotaHour}")
  private int startQuotaHour;

  @Bean(name = "startQuotaHour")
  public int getStartQuotaHour() {
    return startQuotaHour;
  }

  @Value("${keys.startQuotaMinute}")
  private int startQuotaMinute;

  @Value("${uri.path}")
  private String uriPath;

  @Value("${dbpedia.sparqlEndpoint}")
  private String sparqlEndpoint;

  @Bean(name = "startQuotaMinute")
  public int getStartQuotaMinute() {
    return startQuotaMinute;
  }

  public String getUriPath() {
    return uriPath;
  }

  public void setUriPath(String uriPath) {
    this.uriPath = uriPath;
  }

  @Bean
  public Map<Class<? extends RequestModel>, DBpediaQueryFactory> createFactoryMap(
      List<DBpediaQueryFactory<?>> factories) {
    Map<Class<? extends RequestModel>, DBpediaQueryFactory> ret = new HashMap<>();
    for (DBpediaQueryFactory<?> f : factories) {
      ret.put(f.getGenericType(), f);
    }
    return ret;
  }

  @Bean
  public OntModel createOntModel() {
    return createOntModel(ontologyFile);
  }

  public static OntModel createOntModel(String filename) {
    OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
    InputStream in = null;
    try {
      in = FileUtils.openInputStream(new File(filename));
    } catch (IOException e) {
      LOG.error("Cannot open the ontology file " + filename, e.getMessage());
    }
    try {
      model.read(in, null);
      LOG.info("Ontology " + filename + " loaded.");
    } catch (Exception e) {
      LOG.error("Error when loading Ontology file " + filename);
      throw e;
    }

    return model;
  }

  @Bean
  public SemanticVersionSet<ApiVersion> loadVersions() throws IOException {
    VersionLoader loader = new VersionLoader();
    return loader.loadFromDirectory(new File(versionsDir));
  }

  @Bean
  public Set loadApiKeys() {
    return loadApiKeys(apiKeyFile);
  }

  @Bean
  public PrefixMappingImpl createPrefixMapping() throws IOException {
    return loadPrefixMapping(prefixesFile);
  }

  /**
   * Reads the prefix mapping file, creates a PrefixMappingImpl from it and locks it
   */
  public static PrefixMappingImpl loadPrefixMapping(String path) throws IOException {
    try {
      HashMap<?, ?> map = new ObjectMapper().readValue(new File(path), HashMap.class);
      //prefMap = map;
      PrefixMappingImpl prefixMapping = new PrefixMappingImpl();
      prefixMapping.setNsPrefixes((Map<String, String>) map);
      prefixMapping.lock();
      LOG.info("Prefix mapping loaded from file " + path + ".");
      return prefixMapping;
    } catch (IOException e) {
      LOG.error("Error while loading the prefixes from " + path);
      throw e;
    }
  }

  public static Set loadApiKeys(String filename) {
    Set<String> apiKeyList = new HashSet<>();
    try (FileReader filereader = new FileReader(filename);
        BufferedReader bufferedReader = new BufferedReader(filereader)) {
      String line;
      while ((line = bufferedReader.readLine()) != null) {
        apiKeyList.add(line);
      }
      LOG.info("ApikeyList " + filename + " loaded.");

    } catch (FileNotFoundException fnfEx) {
      LOG.error("File " + filename + " not found.\n" + fnfEx);
    } catch (IOException ioEx) {
      LOG.error("Error when loading Apikeys File " + filename + "\n" + ioEx);
    }
    return apiKeyList;
  }


  public String getSparqlEndpoint() {
    return sparqlEndpoint;
  }


  @Value("${mappedProperties}")
  public String mappedProperties;

  @Bean(name = "rml")
  public Map<String, List<String>> getMappedProperties()
      throws IOException {
    return loadMappedProperties(mappedProperties);
  }

  public static Map<String, List<String>> loadMappedProperties(String fileName)
      throws IOException {
    JsonNode root = null;
    List<JsonNode> nodeList;
    Map<String, List<String>> map = new HashMap<>();
    String instance;
    List<String> set;
    root = new ObjectMapper().readTree(new File(fileName));
    Iterator<JsonNode> it = root.elements();
    while (it.hasNext()) {
      String[] s;
      String shortInstance;
      set = new ArrayList<>();
      JsonNode node = it.next();
      instance = node.get("@id").asText();
      s = instance.split(":");
      shortInstance = s[1];
      nodeList = node.findValues("@id");
      for (JsonNode o : nodeList) {
        if (!(o.asText().equals(instance))) {
          set.add(o.asText());
        }
      }
      map.put(shortInstance, set);
    }
    return map;
  }
}
