package dbpedia.api.controller;

import dbpedia.api.configuration.Configuration;
import dbpedia.api.model.ResultSetWrapper;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * Sends SPARQL Queries to the SPARQL Endpoint
 */
@Component
public class DBpediaClient {

  private static final Logger LOG = LogManager.getLogger(DBpediaClient.class.getName());
  private final String endpoint;

  public DBpediaClient(Configuration config) {
    endpoint = config.getSparqlEndpoint();
  }

  /**
   * sends query to DB service
   *
   * @param query The request query
   * @return String response from DB service
   */

  @Cacheable(value = "DBpediaClient.sendQuery", key = "#query.toString()")
  public ResultSetWrapper sendQuery(Query query) {
    ResultSet resultSet;
    try (QueryExecution execution = QueryExecutionFactory.sparqlService(endpoint, query)) {
      resultSet = execution.execSelect();
      LOG.info("Query sent to: " + endpoint);
      return new ResultSetWrapper(ResultSetFactory.copyResults(resultSet));
    } catch (Exception e) {
      LOG.warn("Tried to send the query to the SparqlService... " + e.getMessage());
      throw e;
    }
  }

}

