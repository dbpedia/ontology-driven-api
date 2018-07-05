package dbpedia.api.controller;

public class InvalidUserkeyException extends Exception {

  InvalidUserkeyException() {
    super("Invalid userkey");
  }
}
