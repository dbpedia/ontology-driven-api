package dbpedia.api.controller;

import dbpedia.api.configuration.Configuration;
import dbpedia.api.model.UserModel;
import dbpedia.api.model.UserModel.userType;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Class for managing users and API-keys
 */
@Component
public class APIKeyHandler {

  /**
   * Contains a Set of Keys
   */
  private Set<String> data;
  /**
   * Contains a HashMap of users
   */
  private Map<String, UserModel> map;
  /**
   * Necessary for reloading the Apikeyfile on the fly
   */
  private Configuration configuration;
  /**
   * Quota per day
   */
  private int startQuotaDay;
  /**
   * Quota per hour
   */
  private int startQuotaHour;
  /**
   * Quota per minute
   */
  private int startQuotaMinute;

  /**
   * Constructor
   */
  public APIKeyHandler(Set keys, Configuration configuration,
      @Qualifier("startQuotaDay") int startQuotaDay,
      @Qualifier("startQuotaHour") int startQuotaHour,
      @Qualifier("startQuotaMinute") int startQuotaMinute) {

    this.data = keys;
    this.configuration = configuration;
    this.startQuotaDay = startQuotaDay;
    this.startQuotaHour = startQuotaHour;
    this.startQuotaMinute = startQuotaMinute;
    userMap();
  }

  /**
   * Creates a HashMap containing users of all types
   */
  private void userMap() {
    map = new HashMap<>();
    for (String s : data) {
      UserModel user = new UserModel(startQuotaDay, startQuotaHour, startQuotaMinute);
      if (s.endsWith("_ADMIN")) {
        user.setUserType(userType.ADMIN);
      } else {
        user.setUserType(userType.USER);
      }
      map.put(s, user);
    }
  }

  /**
   * Reloads the apikeyfile after a specified amount of time
   */
  //TODO change timing
  @Scheduled(fixedRate = 10000000)
  public void reloadFile() {
    this.data = Configuration.loadApiKeys(configuration.getApiKeyFileName());
    for (String s : data) {
      if (!map.containsKey(s)) {
        UserModel user = new UserModel(startQuotaDay, startQuotaHour, startQuotaMinute);
        if (s.endsWith("_ADMIN")) {
          user.setUserType(userType.ADMIN);
        } else {
          user.setUserType(userType.USER);
        }
        map.put(s, user);
      }
    }
  }

  /**
   * resets QuotaDay
   */
  @Scheduled(fixedRate = 86400000)
  public void dayReset() {
    for (String s : data) {
      map.get(s).resetDay(startQuotaDay);
    }
  }

  /**
   * resets QuotaHour
   */
  @Scheduled(fixedRate = 3600000)
  public void hourReset() {
    for (String s : data) {
      map.get(s).resetHour(startQuotaHour);
    }
  }

  /**
   * resets QuotaMinute
   */
  @Scheduled(fixedRate = 60000)
  public void minuteReset() {
    for (String s : data) {
      map.get(s).resetMinute(startQuotaMinute);
    }
  }

  public Set<String> getData() {
    return this.data;
  }

  public Map<String, UserModel> getMap() {
    return this.map;
  }
}
