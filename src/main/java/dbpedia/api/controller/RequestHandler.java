package dbpedia.api.controller;

import dbpedia.api.model.LogDataModel;
import dbpedia.api.model.RequestModel;
import org.springframework.http.ResponseEntity;

/**
 * Stub interface for the RequestHandler. The only implementation is RequestHandlerImplementation
 * but some tests also create their own implementation to seperate the controller from the request
 * handler
 */
public interface RequestHandler {

  ResponseEntity handle(RequestModel request, LogDataModel logDataModel);
}
