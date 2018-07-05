package dbpedia.api.versioning;

import dbpedia.api.model.ApiVersion;
import dbpedia.api.model.ResourceModel;
import dbpedia.api.versioning.SemanticVersionSet.Builder;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Reads the version files from a directory
 */
public class VersionLoader {

  private String extension = "version.json";

  /**
   * Reads all files with a specific extension from a path and creates a ApiVersion for each of it
   */
  public SemanticVersionSet<ApiVersion> loadFromDirectory(File path) throws IOException {
    if (!path.isDirectory()) {
      throw new IllegalArgumentException("The path must exist and must be a directory!");
    }
    Collection<File> versionFiles = FileUtils.listFiles(path, new String[]{extension}, false);
    SemanticVersionSet.Builder<ApiVersion> builder = new Builder<>();
    for (File versionFile : versionFiles) {
      String json = FileUtils.readFileToString(versionFile, Charset.defaultCharset());
      try {
        builder.add(createVersionFromJson(json));
      } catch (JSONException e) {
        throw new IOException("File " + versionFile + " has bad json. " + e.getMessage());
      }
    }
    return builder.build();
  }

  /**
   * Parses JSON to ApiVersion
   */
  public static ApiVersion createVersionFromJson(String jsonString) throws JSONException {
    JSONObject json = new JSONObject(jsonString);
    int major = json.getInt("major");
    int minor = json.getInt("minor");
    int patch = json.getInt("patch");

    Map<ResourceModel, ResourceModel> resourceReplacements = new HashMap<>();
    if (json.has("resourceReplacements")) {
      JSONArray replacements = json.getJSONArray("resourceReplacements");
      for (int i = 0; i < replacements.length(); i++) {
        JSONObject replacement = replacements.getJSONObject(i);
        ResourceModel before = new ResourceModel(
            replacement.getString("prefixBefore"),
            replacement.getString("identifierBefore"));
        ResourceModel after = new ResourceModel(
            replacement.getString("prefixNow"),
            replacement.getString("identifierNow"));
        resourceReplacements.put(before, after);
      }
    }

    Map<String, String> prefixReplacements = new HashMap<>();
    if (json.has("prefixReplacements")) {
      JSONObject obj = json.getJSONObject("prefixReplacements");
      for (String key : obj.keySet()) {
        prefixReplacements.put(key, obj.getString(key));
      }
    }

    return new ApiVersion(major, minor, patch, resourceReplacements, prefixReplacements);
  }

  public String getExtension() {
    return extension;
  }

  public void setExtension(String extension) {
    this.extension = extension;
  }
}
