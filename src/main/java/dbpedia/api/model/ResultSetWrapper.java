package dbpedia.api.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetFormatter;

/**
 * makes ResultSet serializable (needed for caching)
 */
public final class ResultSetWrapper implements Serializable {

  private ResultSet mem;

  public ResultSetWrapper(ResultSet mem) {
    this.mem = mem;
  }

  public ResultSet getResultSet() {
    return mem;
  }

  public ResultSetWrapper() {//needed for Serialization
  }

  private void writeObject(ObjectOutputStream objectOutputStream)
      throws IOException {
    ResultSetFormatter.outputAsJSON(objectOutputStream, ResultSetFactory.copyResults(mem));
  }

  private void readObject(ObjectInputStream objectInputStream)
      throws IOException, ClassNotFoundException {
    this.mem = ResultSetFactory.fromJSON(objectInputStream);
    objectInputStream.close();
  }
}