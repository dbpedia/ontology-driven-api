import static org.junit.Assert.assertEquals;

import dbpedia.api.model.LogDataModel;
import org.junit.Test;

public class LogDataModelTest {

  LogDataModel logmodel = new LogDataModel();

  @Test
  public void testAllTheGetandSet () {
    String exception = "FileNotFound-Exception";
    logmodel.setException(exception);
    assertEquals(exception,logmodel.getException());
    String requestKey = "4321";
    logmodel.setRequestKey(requestKey);
    assertEquals(requestKey,logmodel.getRequestKey());
    int remainingQuotaDay= 2;
    logmodel.setRemainingQuotaDay(remainingQuotaDay);
    assertEquals(remainingQuotaDay,logmodel.getRemainingQuotaDay());
    int remainingQuotaHour= 5;
    logmodel.setRemainingQuotaHour(remainingQuotaHour);
    assertEquals(remainingQuotaHour,logmodel.getRemainingQuotaHour());
    int remainingQuotaMinute= 7;
    logmodel.setRemainingQuotaMinute(remainingQuotaMinute);
    assertEquals(remainingQuotaMinute,logmodel.getRemainingQuotaMinute());
    String query = "blablabla";
    logmodel.setQuery(query);
    assertEquals(query,logmodel.getQuery());
    int answerLength = 17;
    logmodel.setAnswerLength(answerLength);
    assertEquals(answerLength,logmodel.getAnswerLength());
  }
}
