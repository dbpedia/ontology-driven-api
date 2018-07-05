import static org.junit.Assert.assertEquals;

import dbpedia.api.configuration.Configuration;
import dbpedia.api.controller.APIKeyHandler;
import dbpedia.api.model.UserModel;
import java.util.Map;
import java.util.Set;
import org.junit.Test;


public class APIKeyHandlerTest{

  private APIKeyHandler keyHandler;
  private Configuration config;
  private Set keys;
  private String keyPath;

  public APIKeyHandlerTest(){
    config = new Configuration();
    keyPath = "src/test/resources/keys.txt";
  }

  @Test
  //Test whether the keys can be successfully read from the key file and if the file has the correct number of keys
  public void loadKeys(){
    keys = Configuration.loadApiKeys(keyPath);
    assertEquals(12, keys.size());
  }

  @Test
  //Test creation of user map and if all keys have an associated UserModel
  public void createUserMap(){
    keys = Configuration.loadApiKeys(keyPath);
    keyHandler = new APIKeyHandler(keys, config, 10, 10, 10);
    Map<String, UserModel> map = keyHandler.getMap();
    for(String s : map.keySet()){
      assertEquals(map.get(s) != null, true);
    }
  }
}
