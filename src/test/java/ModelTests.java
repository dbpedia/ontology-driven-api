import static org.junit.Assert.assertEquals;

import dbpedia.api.model.Filter;
import dbpedia.api.model.Filter.Bool;
import dbpedia.api.model.ResourceModel;
import org.junit.Test;

public class ModelTests {

  @Test
  public void filterModelCopyTest() {
    Filter m1 = Filter.newBuilder()
        .setFilterBool(Bool.OR)
        .setFilterOp("<")
        .setFilterVal("someval")
        .setPrefix("prefix")
        .setPropName("prop")
        .build();

    assertEquals(Bool.OR, m1.getBool());
    assertEquals("<", m1.getFilterOp());
    assertEquals(new ResourceModel("prefix", "prop"), m1.getFilterProp());
    assertEquals("someval", m1.getFilterVal());
    assertEquals(m1, m1);

    Filter m2 = Filter.newBuilder().copy(m1).build();
    assertEquals(m1, m2);
    assertEquals(Bool.OR, m2.getBool());
    assertEquals("<", m2.getFilterOp());
    assertEquals(new ResourceModel("prefix", "prop"), m2.getFilterProp());
    assertEquals("someval", m2.getFilterVal());

    Filter m3 = m2.toBuilder().setPrefix("new prefix").setPropName("new prop").build();
    assertEquals("new prefix", m3.getFilterProp().getPrefix());
    assertEquals("new prop", m3.getFilterProp().getIdentifier());
  }
}
