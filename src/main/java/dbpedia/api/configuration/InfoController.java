package dbpedia.api.configuration;

import dbpedia.api.configuration.Configuration;
import dbpedia.api.model.ApiVersion;
import dbpedia.api.versioning.SemanticVersionSet;
import io.swagger.annotations.Api;
import java.util.List;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for the /info resource
 */
@Api(value = "InfoController", description = "Get information about the API (like the most recent version)", tags = "Info")
@org.springframework.stereotype.Controller
@RestController
public class InfoController {

  private Configuration config;
  private SemanticVersionSet<ApiVersion> versionSet;

  public InfoController(Configuration config, SemanticVersionSet<ApiVersion> versionSet) {
    this.config = config;
    this.versionSet = versionSet;
  }


  @GetMapping(value = "/${uri.path:api}/info", produces = {"application/JSON;"})
  public ResponseEntity info() {

    JSONObject infoObject = new JSONObject();


    infoObject.put("latestVersion", versionSet.getLatest().getSemanticVersioningString());

    List<ApiVersion> versionList = versionSet.getAsList();
    for (int i = 0; i < versionList.size(); i++) {
      infoObject.append("allVersions", versionList.get(i).getSemanticVersioningString());
    }


    return new ResponseEntity<>(infoObject.toString(), HttpStatus.OK);
  }

  private ResponseEntity pathNotFound(String path) {
    return new ResponseEntity<>("Path '" + path + "' not found.", HttpStatus.NOT_FOUND);
  }
}
