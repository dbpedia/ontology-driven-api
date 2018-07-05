package dbpedia.api.controller;

public class LimitExceededException extends Exception {

  LimitExceededException(String s) {
    super("Limit " + s + " exceeded");

  }
}
