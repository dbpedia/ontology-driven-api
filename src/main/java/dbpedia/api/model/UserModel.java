package dbpedia.api.model;

/**
 * Class for displaying a User
 */
public class UserModel {

  /**
   * Quota limit per day
   */
  private int quotaDay;
  /**
   * Quota limit per hour
   */
  private int quotaHour;
  /**
   * Quota limit per minute
   */
  private int quotaMinute;
  /**
   * Type of User, USER or ADMIN
   */
  private userType type = userType.USER;

  /**
   * Constructor
   */
  public UserModel(int startQuotaDay,
      int startQuotaHour,
      int startQuotaMinute) {

    this.quotaDay = startQuotaDay;
    this.quotaHour = startQuotaHour;
    this.quotaMinute = startQuotaMinute;

  }

  /**
   * Getter for the Quota left for this day
   *
   * @return quota
   */
  public int getQuotaDay() {
    return quotaDay;
  }

  /**
   * Getter for the Quota left this hour
   *
   * @return quotaHour
   */
  public int getQuotaHour() {
    return quotaHour;
  }

  /**
   * Getter for the Quota left this minute
   *
   * @return quotaMinute
   */
  public int getQuotaMinute() {
    return quotaMinute;
  }

  /**
   * Decreases the Quota left after a query
   */
  public void decrease() {
    this.quotaDay = quotaDay - 1;
    this.quotaHour = quotaHour - 1;
    this.quotaMinute = quotaMinute - 1;
  }

  /**
   * Getter for the userType
   *
   * @return type
   */
  public userType getUserType() {
    return type;
  }

  /**
   * Setter for the userType, if the userType isn't the default-type
   */
  public void setUserType(userType type) {
    this.type = type;
  }

  /**
   * The possible userTypes
   */
  public enum userType {
    ADMIN, USER
  }

  /**
   * Method for Resetting the Qutoas per day
   */
  public void resetDay(int startQuotaDay) {
    quotaDay = startQuotaDay;
  }

  /**
   * Method for Resetting the Qutoas per hour
   */
  public void resetHour(int startQuotaHour) {
    quotaHour = startQuotaHour;
  }

  /**
   * Method for Resetting the Qutoas per minute
   */
  public void resetMinute(int startQuotaMinute) {
    quotaMinute = startQuotaMinute;
  }

}
