package dbpedia.api.model;

public class LogDataModel {

  private String requestKey;
  private int remainingQuotaDay;
  private int remainingQuotaHour;
  private int remainingQuotaMinute;
  private String query;
  private String exception;
  private int answerLength;

  private long startTime;

  public LogDataModel(String requestKey, int remainingQuotaDay,
      int remainingQuotaHour, int remainingQuotaMinute, String query, int answerLength,
      String exception) {
    this.requestKey = requestKey;
    this.remainingQuotaDay = remainingQuotaDay;
    this.remainingQuotaHour = remainingQuotaHour;
    this.remainingQuotaMinute = remainingQuotaMinute;
    this.query = query;
    this.exception = exception;
    this.answerLength = answerLength;
  }

  public LogDataModel() {
    this.startTime = System.currentTimeMillis();
  }

  public String getException() {
    return exception;
  }

  public void setAnswerLength(int answerLength) {
    this.answerLength = answerLength;
  }

  /**
   * Length of dbpedia response string
   *
   * @return answerLength
   */
  public int getAnswerLength() {
    return this.answerLength;
  }

  public void setException(String exception) {
    this.exception = exception;
  }

  public String getRequestKey() {
    return requestKey;
  }

  public int getRemainingQuotaDay() {
    return remainingQuotaDay;
  }

  public int getRemainingQuotaHour() {
    return remainingQuotaHour;
  }

  public int getRemainingQuotaMinute() {
    return remainingQuotaMinute;
  }

  public String getQuery() {
    return query;
  }

  public long getStartTime() {
    return startTime;
  }

  public void setRequestKey(String requestKey) {
    this.requestKey = requestKey;
  }

  public void setRemainingQuotaDay(int remainingQuotaDay) {
    this.remainingQuotaDay = remainingQuotaDay;
  }

  public void setRemainingQuotaHour(int getRemainingQuotaHour) {
    this.remainingQuotaHour = getRemainingQuotaHour;
  }


  public void setRemainingQuotaMinute(int remainingQuotaMinute) {
    this.remainingQuotaMinute = remainingQuotaMinute;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  @Override
  public String toString() {
    if (this.exception == null) {
      return ("\tquery: " + this.getQuery() + "\tkey: " + this.getRequestKey() + "\tday: " + this
          .getRemainingQuotaDay() + "\thour: " + this.getRemainingQuotaHour() + "\tminute: "
          + this.getRemainingQuotaMinute() + "\tlength: " + Integer.toString(this.answerLength)
      );
    } else {
      return ("\tquery: " + this.getQuery() + "\tkey: " + this.getRequestKey() + "\tday: " + this
          .getRemainingQuotaDay() + "\thour: " + this.getRemainingQuotaHour() + "\tminute: "
          + this.getRemainingQuotaMinute() + "\tlength: " + Integer.toString(this.answerLength)
          + "\texception: " + this.getException()
      );
    }

  }
}